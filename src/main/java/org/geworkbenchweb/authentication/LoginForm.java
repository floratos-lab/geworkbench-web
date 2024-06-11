package org.geworkbenchweb.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.pojos.UserActivityLog;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.authentication.exceptions.AccountLockedException;
import org.vaadin.appfoundation.authentication.exceptions.InvalidCredentialsException;
import org.vaadin.appfoundation.authentication.util.AuthenticationUtil;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

public class LoginForm extends VerticalLayout {

	private static final long serialVersionUID = -469128068617789982L;
	private static Log log = LogFactory.getLog(LoginForm.class);

	/* try to log in geWorkbench. throw exceptions when it fails. */
	private void login(final String username, final String password)
			throws InvalidCredentialsException, AccountLockedException, Exception {
		User user = AuthenticationUtil.authenticate(username, password);
		UMainLayout uMainLayout = new UMainLayout();

		uMainLayout.getMainToolBar().setUsername(user.getUsername());
		uMainLayout.getMainToolBar().setPassword(user.getPassword());

		UI.getCurrent().setContent(uMainLayout);
		new PendingNodeProcessor(uMainLayout).start();
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
					log.debug(username + "logged in");
				} catch (InvalidCredentialsException e) {
					status = "fail_2";
					feedbackLabel.setValue("Either username or password was wrong");
				} catch (AccountLockedException e) {
					feedbackLabel.setValue("The given account has been locked.");
					status = "fail_3";
				} catch (javax.persistence.PersistenceException e) {
					feedbackLabel.setValue("PersistenceException caught. Please consult geworkbench log.");
					log.warn(e.getMessage());
					return; // do no try to log
				} catch (Exception e) {
					if (e.getMessage() == null) { /* this may happen due to library code */
						feedbackLabel.setValue("Undocumented exception happened.");
						e.printStackTrace();
					} else {
						feedbackLabel.setValue(e.getMessage());
					}
					status = "fail:generic exception";
				}

				UserActivityLog ual = new UserActivityLog(username, UserActivityLog.ACTIVITY_TYPE.LOG_IN.toString(),
						status);
				FacadeFactory.getFacade().store(ual);
			}
		});

		login.setClickShortcut(KeyCode.ENTER);

		Button forgotPasswd = new Button("Forgot password?");
		forgotPasswd.setStyleName(BaseTheme.BUTTON_LINK);
		forgotPasswd.setDescription("Reset Password for geWorkbench Account");
		forgotPasswd.addClickListener(new ForgotListener(forgotPasswd));

		Button forgotUsername = new Button("Forgot username?");
		forgotUsername.setStyleName(BaseTheme.BUTTON_LINK);
		forgotUsername.setDescription("Find Username for geWorkbench Account");
		forgotUsername.addClickListener(new ForgotListener(forgotUsername));

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
	}
}
