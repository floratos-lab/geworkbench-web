package org.geworkbenchweb.authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
import de.steinwedel.vaadin.MessageBox.Icon;

/**
 * Authentication and Registration are handled here.
 * TODO: Refactor the code 
 * @author Nikhil
 * @version $Id$
 */
public class UUserAuth extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(UUserAuth.class);
	
	private Window aboutWindow = null;
	
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
					feedbackLabel.setValue("Some other exception: "+e.getMessage());
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
		
        Button aboutButton = new Button("About", new ClickListener() {

			private static final long serialVersionUID = -2258433668354584723L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(aboutWindow==null) {
					aboutWindow = buildAboutWindow();
				}
		        if (aboutWindow.getParent() == null) { // not already showing
		        	Window parent = getWindow();
		        	aboutWindow.setHeight(parent.getHeight()/2, UNITS_PIXELS);
		        	aboutWindow.setWidth(parent.getWidth()/2, UNITS_PIXELS);
                    parent.addWindow(aboutWindow);
                }
			}
			
		});
		
		HorizontalLayout group = new HorizontalLayout();
		group.setSpacing(true);
		group.addComponent(register);
		group.addComponent(login);
		group.addComponent(aboutButton);
		
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
		
		loginPanel.addComponent(cssLayout);
		loginPanel.setComponentAlignment(cssLayout, Alignment.TOP_CENTER);
		
		layout.addComponent(loginPanel);
		layout.setComponentAlignment(loginPanel, Alignment.MIDDLE_CENTER);
	}
	
	private Window buildAboutWindow() {
		final Window aboutWindow = new Window("About");
		aboutWindow.setModal(true);
        VerticalLayout aboutWindowLayout = (VerticalLayout) aboutWindow.getContent();
        aboutWindowLayout.setMargin(true);
        aboutWindowLayout.setSpacing(true);
        
        DownloadStream downloadStream = new ClassResource("aboutMessage.html", getApplication()).getStream();
        InputStream inputstream = downloadStream.getStream();
        String text = "Sorry, 'About' text is missing."; // default text
        StringBuilder sb = new StringBuilder();
        if(inputstream!=null) {
	        BufferedReader br = new BufferedReader(new InputStreamReader (inputstream) );
			try {
				String line = br.readLine();
		        while(line!=null) {
		        	sb.append(line);
		        	line = br.readLine();
		        }
			} catch (IOException e) {
				e.printStackTrace();
			}
			text = sb.toString();
        }

        Label aboutMessage = new Label(text);
        aboutMessage.setContentMode(Label.CONTENT_XHTML);
        aboutWindow.addComponent(aboutMessage);

        Button close = new Button("Close", new Button.ClickListener() {
			private static final long serialVersionUID = 8498615289854877295L;

            public void buttonClick(ClickEvent event) {
                (aboutWindow.getParent()).removeWindow(aboutWindow);
            }
        });
        aboutWindowLayout.addComponent(close);
        aboutWindowLayout.setComponentAlignment(close, Alignment.TOP_RIGHT);
        
        return aboutWindow;
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
					log.debug("before registering a new user");
					User user =  UserUtil.registerUser((String) username
							.getValue(), (String) password.getValue(),
							(String) verifyPassword.getValue());
					log.debug("user object is created");
					
					user.setName	((String) realName.getValue());
					user.setEmail	((String) email.getValue());

					FacadeFactory.getFacade().store(user);
					log.debug("user object is stored");
					
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
				   
					String dialogCaption = "Registration Successed";
					Icon dialogIcon = Icon.INFO;
					String message = "Welcome, " + user.getName() + "("
							+ username
							+ ")!\nYou have successfully registered.";
					MessageBox.ButtonType buttonType = MessageBox.ButtonType.OK;

					String errorMessage = UserDirUtils.CreateUserDirectory(user
							.getId());
					if (errorMessage != null
							&& errorMessage.trim().length() > 0) {
						dialogCaption = "Failed in Creating User Data Directory";
						dialogIcon = Icon.WARN;
						message = errorMessage;
						buttonType = MessageBox.ButtonType.ABORT;
					}

					MessageBox mb = new MessageBox(getWindow(), dialogCaption,
							dialogIcon, message, new MessageBox.ButtonConfig(
									buttonType, "Back to Log-in"));
					mb.show(new MessageBox.EventListener() {

						private static final long serialVersionUID = -8489356760651132447L;

						@Override
						public void buttonClicked(ButtonType buttonType) {
							getApplication().close();
						}
					});
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
		
		content.setSizeFull();
		content.addComponent(image);
		content.setComponentAlignment(image, Alignment.BOTTOM_CENTER);	
		content.addComponent(registrationPanel);
		content.setComponentAlignment(registrationPanel, Alignment.TOP_CENTER);
		
		return content;
	
	}
}
