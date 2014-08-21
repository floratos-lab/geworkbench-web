/**
 * 
 */
package org.geworkbenchweb.authentication;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import nl.captcha.Captcha;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
 
import org.vaadin.appfoundation.authentication.data.User;
 
import org.vaadin.appfoundation.authentication.exceptions.PasswordRequirementException;
import org.vaadin.appfoundation.authentication.exceptions.PasswordsDoNotMatchException;
import org.vaadin.appfoundation.authentication.exceptions.TooShortPasswordException;
import org.vaadin.appfoundation.authentication.exceptions.TooShortUsernameException;
import org.vaadin.appfoundation.authentication.exceptions.UsernameExistsException;
 
import org.vaadin.appfoundation.authentication.util.UserUtil;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
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
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;
 

/**
 * @author zji
 * 
 */
public class RegistrationForm extends VerticalLayout {

	private static final long serialVersionUID = 6837549393946888607L;

	private static final String fromEmail = GeworkbenchRoot.getAppProperty("from.email");
	private static final String fromPassword = GeworkbenchRoot.getAppProperty("from.password");
	private Pattern emailPattern = Pattern.compile("[0-9a-zA-Z()-_.]+@[0-9a-zA-Z()-_.]+\\.[a-zA-Z]+");
	private Log log = LogFactory.getLog(RegistrationForm.class);

	final Label feedbackLabel = new Label();

	public RegistrationForm() {

		final Panel registrationPanel = new Panel();
		final TextField username = new TextField("Username");
		final PasswordField password = new PasswordField("Password");
		final PasswordField verifyPassword = new PasswordField(
				"Verify password");
		final TextField realName = new TextField("Real name");
		final TextField email = new TextField("Email address");
		final TextField captchaCode = new TextField("Enter the code above");
		
		FormLayout layout = new FormLayout();

		username.setWidth("145px");
		password.setWidth("145px");
		verifyPassword.setWidth("145px");
		realName.setWidth("145px");
		email.setWidth("145px");
		captchaCode.setWidth("145px");

		registrationPanel.setStyleName(Reindeer.PANEL_LIGHT);
		registrationPanel.setWidth("350px");

		ThemeResource resource = new ThemeResource("img/geWorkbench.png");
		Embedded image = new Embedded("", resource);		

		ThemeResource resourceCaptcha = new ThemeResource("img/simpleCaptcha.jpg");
		Embedded imageCaptcha = new Embedded("", resourceCaptcha);
		imageCaptcha.setImmediate(true);		
		imageCaptcha.setWidth("145px");
		imageCaptcha.setHeight("30px");

		username.setNullRepresentation("");
		password.setNullRepresentation("");
		verifyPassword.setNullRepresentation("");
		realName.setNullRepresentation("");
		email.setNullRepresentation("");
		captchaCode.setNullRepresentation("");
		
		layout.setSpacing(true);
		layout.addComponent(feedbackLabel);
		layout.addComponent(username);
		layout.addComponent(password);
		layout.addComponent(verifyPassword);
		layout.addComponent(realName);
		layout.addComponent(email);
		layout.addComponent(imageCaptcha);
		layout.addComponent(captchaCode);

		Button registerButton = new Button("Register", new ClickListener() {

			private static final long serialVersionUID = 9048069425045731789L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				Boolean resetPassword = false;
				try {
					register(username, password, verifyPassword, realName,
							email, captchaCode);
				} catch (TooShortPasswordException e) {
					feedbackLabel
							.setValue("Password is too short, it needs to be at least "
									+ UserUtil.getMinPasswordLength()
									+ " characters long");
					resetPassword = true;

				} catch (TooShortUsernameException e) {

					feedbackLabel
							.setValue("Username is too short, it needs to be at least "
									+ UserUtil.getMinUsernameLength()
									+ " characters long");
					resetPassword =true;

				} catch (PasswordsDoNotMatchException e) {

					feedbackLabel.setValue("Password verification has failed");
					resetPassword =true;

				} catch (UsernameExistsException e) {

					feedbackLabel
							.setValue("The chosen username already exists, please pick another one");
					resetPassword =true;

				} catch (PasswordRequirementException e) {
					feedbackLabel
							.setValue("Password does not meet the set requirements");
					resetPassword =true;
				}

				if ( resetPassword )
				{
					password.setValue(null);				
				    verifyPassword.setValue(null);
				}

			}
		});

		registerButton.setClickShortcut(KeyCode.ENTER);

		HorizontalLayout group = new HorizontalLayout();
		group.setSpacing(true);
		group.addComponent(registerButton);
		layout.addComponent(group);

		Button backtoLogin = new Button("Back to Login Page");
		backtoLogin.setStyleName(BaseTheme.BUTTON_LINK);
		backtoLogin.setDescription("Go Back to the Login Page");
		backtoLogin.addListener(new ClickListener() {

			private static final long serialVersionUID = -4747422973711586108L;

			@Override
			public void buttonClick(ClickEvent event) {
				getApplication().close();
			}
			
		});
		layout.addComponent(backtoLogin);
		layout.setComponentAlignment(backtoLogin, Alignment.MIDDLE_CENTER);
		registrationPanel.addComponent(layout);

		this.setSizeFull();
		this.addComponent(image);
		this.setComponentAlignment(image, Alignment.BOTTOM_CENTER);
		this.addComponent(registrationPanel);
		this.setComponentAlignment(registrationPanel, Alignment.TOP_CENTER);
	}

	private void register(TextField username, PasswordField password,
			PasswordField verifyPassword, TextField realName, TextField email, TextField captchaCode)
			throws TooShortPasswordException, TooShortUsernameException,
			PasswordsDoNotMatchException, UsernameExistsException,
			PasswordRequirementException {
		final String usernameText = (String) username.getValue();
		final String passwordText = (String) password.getValue();
		final String captchaText = (String) captchaCode.getValue();
		
		ApplicationContext cntxt = getApplication().getContext();
		WebApplicationContext wcntxt = (WebApplicationContext)cntxt;
	   
	    Captcha captcha =  (Captcha)wcntxt.getHttpSession().getAttribute(Captcha.NAME);
	    if (!captcha.isCorrect(captchaText))
        {
	    	feedbackLabel
			.setValue("Captcha code does not match");	
	    	getApplication().getMainWindow().executeJavaScript("window.location.reload();");
	    	 
	    	return;
        }
	    if(email.getValue() == null || ! emailPattern.matcher(email.getValue().toString()).matches()) 
	    {
	    	feedbackLabel.setValue("Email address is invalid");	
			return;
	    }
		log.debug("before registering a new user");
		User user = UserUtil.registerUser(usernameText, passwordText,
				(String) verifyPassword.getValue());
		
		log.debug("user object is created");

		user.setName((String) realName.getValue());
		user.setEmail((String) email.getValue());
		user.setAccountLocked(true);
		Random random = new Random();
		user.setReasonForLockedAccount(new Integer(random.nextInt()).toString() + "(waiting for confirmation from user)");
		if(emailPattern.matcher(user.getEmail()).matches()){
			sendMail(user);
		}		

		FacadeFactory.getFacade().store(user);
		log.debug("user object is stored");

		String confirmPage = this.getApplication().getURL().toString() + "VAADIN/pages/confirm.html";
		getApplication().close();
		
		getApplication().setLogoutURL(confirmPage);
		 
	}
	
	private void sendMail(User user){
		String title = "Registration Confirmation for Your geWorkbench Account";
		String realName = user.getName();
		if(realName.length() == 0) realName = "Guest";
		String hrefStr = this.getApplication().getURL().toString() 
				+ "servlet/ConfirmUser?userID=" + user.getId()
				+ "&key=" + user.getReasonForLockedAccount().split("\\(")[0];
		String content = "<font face=\"Monogram\">Welcome " + realName +"!<p>"
				+ "Thanks for signing up with geWorkbench-web!"
				+ "<p>Here is your geWorkbench account information: "
				+ "<p>User name: " + user.getUsername()
				+ "<br>Real name: " + user.getName()
				+ "<br>Email: " + user.getEmail()
				+ "<p>Please click on the following link<br>"
				+ "<a href=\"" + hrefStr + "\" >"
				+ hrefStr + "<a>"
				+ "<br>to activate your geWorkbench-web account."
				+ "<p>Thank you,<br>The geWorkbench Team</font>";

		Properties props = new Properties() {
			private static final long serialVersionUID = -3842038014435217159L;
			{
				put("mail.smtp.auth", "true");
				put("mail.smtp.host", "smtp.gmail.com");
				put("mail.smtp.port", "587");
				put("mail.smtp.starttls.enable", "true");
			}
		};
		Session mailSession = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
	        protected PasswordAuthentication getPasswordAuthentication() {
	            return new PasswordAuthentication(fromEmail, fromPassword);
	        }
	    });
		MimeMessage mailMessage = new MimeMessage(mailSession);
		try{
			title = MimeUtility.encodeText(title, "utf-8", null);
			mailMessage.setSubject(title);
			mailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
			mailMessage.setContent(content, "text/html");
			Transport.send(mailMessage);
		}catch(MessagingException e){
			e.printStackTrace();
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
	}
}
