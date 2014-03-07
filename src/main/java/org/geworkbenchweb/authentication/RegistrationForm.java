/**
 * 
 */
package org.geworkbenchweb.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.pojos.ActiveWorkspace;
import org.geworkbenchweb.pojos.Workspace;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.authentication.exceptions.PasswordRequirementException;
import org.vaadin.appfoundation.authentication.exceptions.PasswordsDoNotMatchException;
import org.vaadin.appfoundation.authentication.exceptions.TooShortPasswordException;
import org.vaadin.appfoundation.authentication.exceptions.TooShortUsernameException;
import org.vaadin.appfoundation.authentication.exceptions.UsernameExistsException;
import org.vaadin.appfoundation.authentication.util.UserUtil;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
import de.steinwedel.vaadin.MessageBox.Icon;

/**
 * @author zji
 * @version $Id$
 * 
 */
public class RegistrationForm extends VerticalLayout {

	private static final long serialVersionUID = 6837549393946888607L;

	private Log log = LogFactory.getLog(RegistrationForm.class);

	public RegistrationForm() {

		final Panel registrationPanel = new Panel();
		final Label feedbackLabel = new Label();
		final TextField username = new TextField("Username");
		final PasswordField password = new PasswordField("Password");
		final PasswordField verifyPassword = new PasswordField(
				"Verify password");
		final TextField realName = new TextField("Real name");
		final TextField email = new TextField("Email address");
		FormLayout layout = new FormLayout();

		username.setWidth("145px");
		password.setWidth("145px");
		verifyPassword.setWidth("145px");
		realName.setWidth("145px");
		email.setWidth("145px");

		registrationPanel.setStyleName(Reindeer.PANEL_LIGHT);
		registrationPanel.setWidth("300px");

		ThemeResource resource = new ThemeResource("img/geWorkbench.png");
		Embedded image = new Embedded("", resource);

		username.setNullRepresentation("");
		password.setNullRepresentation("");
		verifyPassword.setNullRepresentation("");
		realName.setNullRepresentation("");
		email.setNullRepresentation("");

		layout.setSpacing(true);
		layout.addComponent(feedbackLabel);
		layout.addComponent(username);
		layout.addComponent(password);
		layout.addComponent(verifyPassword);
		layout.addComponent(realName);
		layout.addComponent(email);

		Button registerButton = new Button("Register", new ClickListener() {

			private static final long serialVersionUID = 9048069425045731789L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					register(username, password, verifyPassword, realName,
							email);
				} catch (TooShortPasswordException e) {
					feedbackLabel
							.setValue("Password is too short, it needs to be at least "
									+ UserUtil.getMinPasswordLength()
									+ " characters long");

				} catch (TooShortUsernameException e) {

					feedbackLabel
							.setValue("Username is too short, it needs to be at least "
									+ UserUtil.getMinUsernameLength()
									+ " characters long");

				} catch (PasswordsDoNotMatchException e) {

					feedbackLabel.setValue("Password verification has failed");

				} catch (UsernameExistsException e) {

					feedbackLabel
							.setValue("The chosen username already exists, please pick another one");

				} catch (PasswordRequirementException e) {
					feedbackLabel
							.setValue("Password does not meet the set requirements");
				}

				password.setValue(null);
				verifyPassword.setValue(null);

			}
		});

		Button backLogin = new Button("Login", new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				getApplication().close();
			}
		});
		registerButton.setClickShortcut(KeyCode.ENTER);

		HorizontalLayout group = new HorizontalLayout();
		group.setSpacing(true);
		group.addComponent(backLogin);
		group.addComponent(registerButton);
		layout.addComponent(group);
		registrationPanel.addComponent(layout);

		this.setSizeFull();
		this.addComponent(image);
		this.setComponentAlignment(image, Alignment.BOTTOM_CENTER);
		this.addComponent(registrationPanel);
		this.setComponentAlignment(registrationPanel, Alignment.TOP_CENTER);
	}

	private void register(TextField username, PasswordField password,
			PasswordField verifyPassword, TextField realName, TextField email)
			throws TooShortPasswordException, TooShortUsernameException,
			PasswordsDoNotMatchException, UsernameExistsException,
			PasswordRequirementException {
		log.debug("before registering a new user");
		User user = UserUtil.registerUser((String) username.getValue(),
				(String) password.getValue(),
				(String) verifyPassword.getValue());
		log.debug("user object is created");

		user.setName((String) realName.getValue());
		user.setEmail((String) email.getValue());

		FacadeFactory.getFacade().store(user);
		log.debug("user object is stored");

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

		String dialogCaption = "Registration Successed";
		Icon dialogIcon = Icon.INFO;
		String message = "Welcome, " + user.getName() + "(" + username
				+ ")!\nYou have successfully registered.";

		MessageBox mb = new MessageBox(getWindow(), dialogCaption, dialogIcon,
				message, new MessageBox.ButtonConfig(MessageBox.ButtonType.OK,
						"Back to Log-in", "150px"));
		mb.show(new MessageBox.EventListener() {

			private static final long serialVersionUID = -8489356760651132447L;

			@Override
			public void buttonClicked(ButtonType buttonType) {
				getApplication().close();
			}
		});
	}
}
