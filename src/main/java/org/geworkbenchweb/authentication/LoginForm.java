package org.geworkbenchweb.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.genspace.GenSpaceServerFactory;
import org.geworkbenchweb.genspace.wrapper.UserWrapper;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.pojos.ActiveWorkspace;
import org.geworkbenchweb.pojos.UserActivityLog;
import org.geworkbenchweb.pojos.Workspace;
import org.vaadin.alump.fancylayouts.FancyCssLayout;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.authentication.exceptions.AccountLockedException;
import org.vaadin.appfoundation.authentication.exceptions.InvalidCredentialsException;
import org.vaadin.appfoundation.authentication.exceptions.UsernameExistsException;
import org.vaadin.appfoundation.authentication.util.AuthenticationUtil;
import org.vaadin.appfoundation.authentication.util.UserUtil;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

public class LoginForm extends VerticalLayout {

	private static final long serialVersionUID = -469128068617789982L;
	private static Log log = LogFactory.getLog(LoginForm.class);
			
	private GenSpaceServerFactory genSpaceServerFactory = new GenSpaceServerFactory();

	/* try to log in geWorkbench. throw exceptions when it fails. */
	private void login(final String username, final String password) 
			throws InvalidCredentialsException, AccountLockedException, Exception {
		User user = AuthenticationUtil.authenticate(username, password);
		UMainLayout uMainLayout = new UMainLayout();
		uMainLayout.getMainToolBar().setUsername(user.getUsername());
		uMainLayout.getMainToolBar().setPassword(user.getPassword());
		if (GeworkbenchRoot.genespaceEnabled()) {
			uMainLayout.getMainToolBar().initGenspaceLogin(uMainLayout.getGenSpaceLogger());
		}

		getApplication().getMainWindow().setContent(uMainLayout);
		new PendingNodeProcessor(uMainLayout).start();

		if (GeworkbenchRoot.genespaceEnabled() && !genSpaceServerFactory.userLogin(username, password)) {
			UserWrapper u = new UserWrapper(
				new org.geworkbench.components.genspace.server.stubs.User(), 
				null);
			u.setUsername(username);
			u.setPasswordClearText(password);
			u.setFirstName("");
			u.setLastName("");
			if(!genSpaceServerFactory.userRegister(u.getDelegate())) {
				log.warn("genSpaceServerFactory.userRegister returns false");
			}	
			else {
				genSpaceServerFactory.getUser().setLogData(1);
				genSpaceServerFactory.userUpdate();
			}
		}
	}

	public LoginForm(Button switchToRegisterButton) {

		final Label feedbackLabel = new Label();
		final TextField usernameField = new TextField();
		final PasswordField passwordField = new PasswordField();

		usernameField.setWidth("145px");
		usernameField.setInputPrompt("Username");
		passwordField.setWidth("145px");
		passwordField.setInputPrompt("Password");

		this.setSpacing(true);

		ThemeResource resource = new ThemeResource("img/geWorkbench.png");
		Embedded image = new Embedded("", resource);

		Button login = new Button("Login", new ClickListener() {

			private static final long serialVersionUID = -5577423546946890721L;
			
			public void buttonClick(ClickEvent event) {

				String username = (String) usernameField.getValue();
				String password = (String) passwordField.getValue();
				String status = "unknown";

				try {	
					login(username, password);
					status = "success";
				} catch (InvalidCredentialsException e) {
					status = "fail_2";
					feedbackLabel.setValue("Either username or password was wrong");
					/* give genSpace user a second chance */
					if (GeworkbenchRoot.genespaceEnabled() 
						&& genSpaceServerFactory.userLogin(username, password)) {

						User user;
						try {
							/* Create user object */
							user = UserUtil.registerUser(username,password,password);

							user.setName("");
							user.setEmail("");

							FacadeFactory.getFacade().store(user);

							/* Creating default workspace */
							Workspace workspace = new Workspace();
							workspace.setOwner(user.getId());
							workspace.setName("Default Workspace");
							FacadeFactory.getFacade().store(workspace);

							/* Setting active workspace */
							ActiveWorkspace active = new ActiveWorkspace();
							active.setOwner(user.getId());
							active.setWorkspace(workspace.getId());
							FacadeFactory.getFacade().store(active);

							login(username, password);
							status = "success_2";
							feedbackLabel.setValue("success with genSpace account");
						} catch (InvalidCredentialsException e1) {
							log.info("log-in failed. no third chance for genSpace account. "+e);
							status = "fail_2_a";
						} catch (UsernameExistsException e1) {
							// trying to log with genSpace's password but unable to register the new account due to an existing user
							log.info("log-in failed. no new account created for genSpace account. "+e);
							status = "fail_2_b";
						} catch (Exception e1) {
							e1.printStackTrace();
							status = "fail_2_c";
						}
					}
				} catch (AccountLockedException e) {						 
					feedbackLabel.setValue("The given account has been locked.");
					status = "fail_3";
				} catch (Exception e) {
					if(e.getMessage()==null) { /* this may happen due to library code */
						feedbackLabel.setValue("Undocumened exception happened.");
						e.printStackTrace();
					} else {
						feedbackLabel.setValue(e.getMessage());
					}
					status = "fail_4";
				}

				UserActivityLog ual = new UserActivityLog(username, UserActivityLog.ACTIVITY_TYPE.LOG_IN.toString(), status);
				FacadeFactory.getFacade().store(ual);
			}
		});

		login.setClickShortcut(KeyCode.ENTER);

		Button forgotPasswd = new Button("Forgot password?");
		forgotPasswd.setStyleName(BaseTheme.BUTTON_LINK);
		forgotPasswd.setDescription("Reset Password for geWorkbench Account");
		forgotPasswd.addListener(new ForgotListener(forgotPasswd));
		
		Button forgotUsername = new Button("Forgot username?");
		forgotUsername.setStyleName(BaseTheme.BUTTON_LINK);
		forgotUsername.setDescription("Find Username for geWorkbench Account");
		forgotUsername.addListener(new ForgotListener(forgotUsername));
		
		this.addComponent(image);
		this.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
		this.addComponent(usernameField);
		this.setComponentAlignment(usernameField, Alignment.MIDDLE_CENTER);
		this.addComponent(passwordField);
		this.setComponentAlignment(passwordField, Alignment.MIDDLE_CENTER);
		this.addComponent(login);
		this.setComponentAlignment(login, Alignment.MIDDLE_CENTER);
		Label orLabel = new Label("or");
		orLabel.setWidth("15px");
		this.addComponent(orLabel);
		this.setComponentAlignment(orLabel, Alignment.MIDDLE_CENTER);
		this.addComponent(switchToRegisterButton);
		this.setComponentAlignment(switchToRegisterButton, Alignment.MIDDLE_CENTER);
		this.addComponent(forgotPasswd);
		this.setComponentAlignment(forgotPasswd, Alignment.MIDDLE_CENTER);
		this.addComponent(forgotUsername);
		this.setComponentAlignment(forgotUsername, Alignment.MIDDLE_CENTER);
		this.addComponent(feedbackLabel);
		this.setComponentAlignment(feedbackLabel, Alignment.MIDDLE_CENTER);
		feedbackLabel.setWidth("50%");

		final FancyCssLayout cssLayout = new FancyCssLayout();
		cssLayout.setSlideEnabled(true);
		cssLayout.setWidth("700px");

		this.addComponent(cssLayout);
		this.setComponentAlignment(cssLayout, Alignment.TOP_CENTER);
	}
}
