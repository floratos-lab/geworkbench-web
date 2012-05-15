package org.geworkbenchweb.authentication;

import org.geworkbenchweb.GeworkbenchApplication;
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
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

public class UUserAuth extends HorizontalLayout {

	private static final long serialVersionUID = 1L;
	
	private GeworkbenchApplication app;

	public UUserAuth(GeworkbenchApplication app) {
		
		this.app = app;
		this.app.getMainWindow().setCaption("geWorkbench");
		setSizeFull();	
		setStyleName(Reindeer.LAYOUT_BLUE);
		addComponent(buildLoginForm());
	}

	/*
	 *  Here we are building login screen
	 */
	public Layout buildLoginForm() {

		final VerticalLayout content 		= 	new VerticalLayout();
		final Panel loginPanel 				= 	new Panel();
		final FormLayout layout 			= 	new FormLayout();
		final Label feedbackLabel 			= 	new Label();        
		final TextField usernameField 		= 	new TextField("Username");
		final PasswordField passwordField 	= 	new PasswordField("Password");
		
		loginPanel.setStyleName(Reindeer.PANEL_LIGHT);
		loginPanel.setWidth("270px");
		
		Button login = new Button("Login", new ClickListener() {

			private static final long serialVersionUID = -5577423546946890721L;

			public void buttonClick(ClickEvent event) {

				String username = (String) usernameField.getValue();
				String password = (String) passwordField.getValue();
				
				try {
					
					AuthenticationUtil.authenticate(username,
							password);
					getApplication().getMainWindow().removeAllComponents();		
					app.initView(getApplication().getMainWindow());

				} catch (InvalidCredentialsException e) {
					
					feedbackLabel
					.setValue("Either username or password was wrong");
				
				} catch (AccountLockedException e) {
					
					feedbackLabel.setValue("The given account has been locked");
				
				}
			}
		});

		login.setClickShortcut(KeyCode.ENTER);
		login.addStyleName("primary");
		
		Button register = new Button("Click here to register", new ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				content.removeAllComponents();
				Component registrationWindow = buildRegisterForm();
				content.addComponent(registrationWindow);
				content.setComponentAlignment(registrationWindow, Alignment.MIDDLE_CENTER);
		
			}

		});
		
		register.setStyleName(BaseTheme.BUTTON_LINK);
		register.setIcon(new ThemeResource("../runo/icons/32/note.png"));
		
		layout.addComponent(usernameField);
		layout.addComponent(passwordField);
		layout.addComponent(login);
		layout.addComponent(register);
		
		layout.addComponent(feedbackLabel);
		loginPanel.addComponent(layout);
		content.setSizeFull();
		content.addComponent(loginPanel);
		content.setComponentAlignment(loginPanel, Alignment.MIDDLE_CENTER);		
		return content;
		
	}

	/*
	 * Here we are building registration screen
	 * 
	 */
	public Component buildRegisterForm() {

		final Panel registrationPanel 		= 	new Panel();
		final Label feedbackLabel 			= 	new Label();
		final TextField username 			= 	new TextField("Username");
		final PasswordField password 		= 	new PasswordField("Password");
		final PasswordField verifyPassword 	= 	new PasswordField("Verify password");
		final TextField realName 			= 	new TextField("Real name");
		final TextField email 				= 	new TextField("Email address");
		FormLayout layout 					=	new FormLayout();
		
		registrationPanel.setStyleName(Reindeer.PANEL_LIGHT);
		registrationPanel.setWidth("300px");
		
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
					User user = UserUtil.registerUser((String) username
							.getValue(), (String) password.getValue(),
							(String) verifyPassword.getValue());
					
					user.setName	((String) realName.getValue());
					user.setEmail	((String) email.getValue());

					FacadeFactory.getFacade().store(user);
					app.getMainWindow().removeAllComponents();
					app.getMainWindow().showNotification("Registration", 
							"You have successfully registered.", 
							Notification.TYPE_WARNING_MESSAGE);
					
					app.getMainWindow().addComponent(buildLoginForm());

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
		
		layout.addComponent(registerButton);	
		
		Button backLogin = new Button("Click here to login", new ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				app.close();
			
			}

		});
		
		backLogin.setStyleName(BaseTheme.BUTTON_LINK);
		backLogin.setIcon(new ThemeResource("../runo/icons/32/note.png"));
		layout.addComponent(backLogin);
		registrationPanel.addComponent(layout);
		return registrationPanel;
	
	}
}
