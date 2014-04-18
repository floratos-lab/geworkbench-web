/**
 * 
 */
package org.geworkbenchweb.authentication;

import java.io.FileInputStream;
import java.util.Properties;

import org.geworkbenchweb.genspace.GenSpaceServerFactory;
import org.geworkbenchweb.genspace.wrapper.UserWrapper;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.pojos.ActiveWorkspace;
import org.geworkbenchweb.pojos.Workspace;
import org.vaadin.alump.fancylayouts.FancyCssLayout;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.authentication.exceptions.AccountLockedException;
import org.vaadin.appfoundation.authentication.exceptions.InvalidCredentialsException;
import org.vaadin.appfoundation.authentication.exceptions.PasswordRequirementException;
import org.vaadin.appfoundation.authentication.exceptions.PasswordsDoNotMatchException;
import org.vaadin.appfoundation.authentication.exceptions.TooShortPasswordException;
import org.vaadin.appfoundation.authentication.exceptions.TooShortUsernameException;
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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * @author zji
 * @version $Id$
 */
public class LoginForm extends VerticalLayout {

	private static final long serialVersionUID = -469128068617789982L;
	private GenSpaceServerFactory genSpaceServerFactory = new GenSpaceServerFactory();
	
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

				boolean needToLogin = true;
				while(needToLogin) {
					needToLogin = false;
					try {	
					    // Try logging in geWorkbench 
						AuthenticationUtil.authenticate(username, password);
						getApplication().getMainWindow().setContent(
										new UMainLayout());

						// System.out.printf("[DEBUG] User [%s] exists in geWorkbench.\n", username);
						
						// If user exists in geWorkbench, then check whether it exists in genSpace
						if (!genSpaceServerFactory.userExists(username)) {
							// System.out.printf("[DEBUG] User [%s] does NOT exsit in genSpace.\n", username);
							// System.out.printf("[DEBUG] Try registerring new user [%s] in genSpace.\n", username);
													
							UserWrapper u = new UserWrapper(
									new org.geworkbench.components.genspace.server.stubs.User(), 
									null);
							u.setUsername(username);
							u.setPasswordClearText(password);
							u.setFirstName("");
							u.setLastName("");
							if(genSpaceServerFactory.userRegister(u.getDelegate())) {
								// System.out.printf("[DEBUG] Successfully register new user [%s] in genSpace.\n",
								//		username);
							}
							else {
								// System.out.printf("[DEBUG] Fail to register new user [%s] in genSpace.\n", 
								//		username);
							}
						}
						else {

							// System.out.printf("[DEBUG] User [%s] exists in genSpace.\n", username);
							// Change password to empty string
							
							if (!genSpaceServerFactory.userLogin(username, password)) {
								UserWrapper u = genSpaceServerFactory.getWrappedUser();
								u.setPasswordClearText(password);
								genSpaceServerFactory.userUpdate();
							}
							else {
								// the most complicated case
							}
						}
						
					} catch (InvalidCredentialsException e) {
						String err_msg = "Either username or password was wrong";
						
						//If user does NOT exist in geWorkbench, then try logging in genSpace
						// System.out.printf("[DEBUG] User [%s] does NOT exist in geWorkbench.\n", username);
						

						if (!genSpaceServerFactory.userLogin(username, password)) {
							// if user does NOT exist in genSpace
							// System.out.printf("[DEBUG] User [%s] dose NOT exist in genSpace.\n", username);
						}
						else {
							// if user exists in genSpace, then register a new user in geWorkbench
							// System.out.printf("[DEBUG] User [%s] exists in genSpace.\n", username);
							
							User user;
							// System.out.printf("[DEBUG] Try registering new user [%s] in geWorkbench.\n",
							//		username);
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
								
								// System.out.printf("[DEBUG] Successfully register new user [%s] in genWorkbench.\n",
								//		username);
								needToLogin = true;
								
							} catch (TooShortPasswordException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (TooShortUsernameException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (PasswordsDoNotMatchException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (UsernameExistsException e1) {
								// TODO Auto-generateds catch block
								e1.printStackTrace();
							} catch (PasswordRequirementException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
		
						}
						
						feedbackLabel
								.setValue(err_msg);
							
						} catch (AccountLockedException e) {
							// feedbackLabel.setValue("The given account has been locked");
						} catch (Exception e) {
							// e.p:rintStackTrace();
							// feedbackLabel.setValue("Some other exception: "
							//		+ e.getMessage());
						}
					}
			} //while()

		});

		login.setClickShortcut(KeyCode.ENTER);

		HorizontalLayout group = new HorizontalLayout();
		group.setSpacing(true);
		group.addComponent(switchToRegisterButton);
		group.addComponent(login);

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
		this.addComponent(group);
		this.setComponentAlignment(group, Alignment.MIDDLE_CENTER);
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
