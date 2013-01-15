package org.geworkbenchweb.authentication;

import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.pojos.ActiveWorkspace;
import org.geworkbenchweb.pojos.Workspace;
import org.geworkbenchweb.utils.UserDirUtils;
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
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

/**
 * Authentication and Registration are handled here.
 * TODO: Refactor the code 
 * @author Nikhil
 */
public class UUserAuth extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	
	public UUserAuth() {
		setSizeFull();
		addStyleName("background");
	}

	/*
	 *  Here we are building login screen
	 */
	public void buildLoginForm() {
		
		final VerticalLayout layout 		= 	new VerticalLayout();
		final VerticalLayout loginPanel		=	new VerticalLayout();
		final Label feedbackLabel 			= 	new Label();        
		final TextField usernameField 		= 	new TextField();
		final PasswordField passwordField 	= 	new PasswordField();
		
		usernameField.setWidth("145px");
		usernameField.setInputPrompt("Username");
		passwordField.setWidth("145px");
		passwordField.setInputPrompt("Password");
		loginPanel.setSpacing(true);
		
		addComponent(layout);
		layout.setSizeFull();
		setComponentAlignment(layout, Alignment.BOTTOM_CENTER);

		ThemeResource resource = new ThemeResource("img/geWorkbench.png");
	    Embedded image = new Embedded("", resource);
		
		Button login = new Button("Login", new ClickListener() {

			private static final long serialVersionUID = -5577423546946890721L;

			public void buttonClick(ClickEvent event) {

				String username = (String) usernameField.getValue();
				String password = (String) passwordField.getValue();
				
				try {
					AuthenticationUtil.authenticate(username, password);
					getApplication().getMainWindow().setContent(new UMainLayout());
				} catch (InvalidCredentialsException e) {
					feedbackLabel.setValue("Either username or password was wrong");
				} catch (AccountLockedException e) {
					feedbackLabel.setValue("The given account has been locked");
				} catch (Exception e) {
					e.printStackTrace();
					feedbackLabel.setValue("Some other exception");
				}
			}
		});

		login.setClickShortcut(KeyCode.ENTER);
		
		Button register = new Button("Register", new ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				Component registrationWindow = buildRegisterForm();
				layout.removeAllComponents();
				layout.addComponent(registrationWindow);
			}
		});
		
		HorizontalLayout group = new HorizontalLayout();
		group.setSpacing(true);
		group.addComponent(register);
		group.addComponent(login);
		
		loginPanel.addComponent(image);
		loginPanel.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
		loginPanel.addComponent(usernameField);
		loginPanel.setComponentAlignment(usernameField, Alignment.MIDDLE_CENTER);
		loginPanel.addComponent(passwordField);
		loginPanel.setComponentAlignment(passwordField, Alignment.MIDDLE_CENTER);
		loginPanel.addComponent(group);
		loginPanel.setComponentAlignment(group, Alignment.MIDDLE_CENTER);
	
		loginPanel.addComponent(feedbackLabel);
		loginPanel.setComponentAlignment(feedbackLabel, Alignment.MIDDLE_CENTER);
		
		final FancyCssLayout cssLayout = new FancyCssLayout();
		cssLayout.setSlideEnabled(true);
		cssLayout.setWidth("700px");
		
		CustomLayout custom = new CustomLayout("about");
	    cssLayout.addComponent(custom);
		loginPanel.addComponent(cssLayout);
		loginPanel.setComponentAlignment(cssLayout, Alignment.TOP_CENTER);
		
		layout.addComponent(loginPanel);
		layout.setComponentAlignment(loginPanel, Alignment.MIDDLE_CENTER);
	}

	/*
	 * Here we are building registration screen
	 * 
	 */
	public Component buildRegisterForm() {

		final VerticalLayout content 		= 	new VerticalLayout();
		final Panel registrationPanel 		= 	new Panel();
		final Label feedbackLabel 			= 	new Label();
		final TextField username 			= 	new TextField("Username");
		final PasswordField password 		= 	new PasswordField("Password");
		final PasswordField verifyPassword 	= 	new PasswordField("Verify password");
		final TextField realName 			= 	new TextField("Real name");
		final TextField email 				= 	new TextField("Email address");
		FormLayout layout 					=	new FormLayout();
		
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
					User user =  UserUtil.registerUser((String) username
							.getValue(), (String) password.getValue(),
							(String) verifyPassword.getValue());
					
					user.setName	((String) realName.getValue());
					user.setEmail	((String) email.getValue());

					FacadeFactory.getFacade().store(user);
					
					/* Creating default workspace */
					Workspace workspace = 	new Workspace();
					workspace.setOwner(user.getId());	
					workspace.setName("Default Workspace");
				    FacadeFactory.getFacade().store(workspace);
				    
				    /* Setting active workspace */
				    ActiveWorkspace active = new ActiveWorkspace();
				    active.setOwner(user.getId());
				    active.setWorkspace(workspace.getId());
				    FacadeFactory.getFacade().store(active);
				   
				    boolean success = UserDirUtils.CreateUserDirectory(user.getId());
				    if(success != true){ 
				    	getApplication().getMainWindow().showNotification("Couldn't create user. Please contact admin", 
				    			Notification.TYPE_ERROR_MESSAGE);
				    }
					getApplication().getMainWindow().showNotification( "You have successfully registered.");
					getApplication().getMainWindow().removeAllComponents();
					
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
				
				} catch (Exception e) {
					e.printStackTrace();
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
		
		content.setSizeFull();
		content.addComponent(image);
		content.setComponentAlignment(image, Alignment.BOTTOM_CENTER);	
		content.addComponent(registrationPanel);
		content.setComponentAlignment(registrationPanel, Alignment.TOP_CENTER);
		
		return content;
	
	}
}
