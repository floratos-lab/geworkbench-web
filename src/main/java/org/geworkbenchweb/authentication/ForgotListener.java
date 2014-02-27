package org.geworkbenchweb.authentication;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.geworkbenchweb.GeworkbenchRoot;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.authentication.util.PasswordUtil;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ForgotListener implements ClickListener{
	private static final long serialVersionUID = 6330247049027869269L;
	private Button forgotBtn, closeBtn;
	private Label message;
	private VerticalLayout layout;
	private static final String fromEmail = GeworkbenchRoot.getAppProperty("from.email");
	private static final String fromPassword = GeworkbenchRoot.getAppProperty("from.password");

	private Pattern emailPattern = Pattern.compile("[0-9a-zA-Z()-_.]+@[0-9a-zA-Z()-_.]+\\.[a-zA-Z]+");
	
	public ForgotListener(Button btn){
		forgotBtn = btn;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		final Window window = new Window(forgotBtn.getDescription());
		window.setWidth("400px");
		window.setHeight("250px");
		window.center();

		layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		window.setContent(layout);

		message = new Label("", Label.CONTENT_XHTML);
		message.setWidth("300px");
		final TextField textField = new TextField();
		final OptionGroup options = new OptionGroup();
		options.setImmediate(true);
		options.addItem("Email");
		options.setValue("Email");
		
		if(forgotBtn.getCaption().contains("password")){ //forgot password
			options.setCaption("Please enter the username or email address you registered with.");
			options.addItem("Username");
		}else{ //forgot username
			options.setCaption("Please enter the email address you registered with.");
		}
		
		//FIXME:no email or other people's email/username
		
		Button submit = new Button("Submit", new ClickListener(){
			private static final long serialVersionUID = 8292680051084017446L;
			@Override
			public void buttonClick(ClickEvent event) {
				User user = null;
				String option = options.getValue().toString().toLowerCase();
				String value = textField.getValue().toString();
				if (value.isEmpty()){
					message.setValue("Please enter " + option + " in the text field.");
				} else if (option.equalsIgnoreCase("Email")
						&& !emailPattern.matcher(value).matches()) {
					message.setValue("Invalid email.");
				}else{
					user = findUser(option, value);
					if(user != null && !emailPattern.matcher(user.getEmail()).matches()){
						message.setValue("Invalid email.");
						user = null;
					}
				}
				if(user != null){
					sendMail(forgotBtn.getCaption(), user);
					layout.removeAllComponents();
					message.setValue("An email has been sent to you.<br>Please follow the instructions in the email.");
					layout.addComponent(message);
					layout.addComponent(closeBtn);
				}
			}					
		});
		
		closeBtn = new Button("Close", new ClickListener(){
			private static final long serialVersionUID = 8292680051084017447L;
			@Override
			public void buttonClick(ClickEvent event) {
				forgotBtn.getApplication().getMainWindow().removeWindow(window);
			}
		});
		
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		buttons.addComponent(submit);
		buttons.addComponent(closeBtn);
		
		layout.addComponent(options);
		layout.addComponent(textField);
		layout.addComponent(buttons);
		layout.addComponent(message);
		
		forgotBtn.getApplication().getMainWindow().addWindow(window);
	}
	
	private User findUser(String field, String value){
		String query = "SELECT u FROM User u WHERE u." + field + " = :" + field;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(field, value);
		User user = FacadeFactory.getFacade().find(query, parameters);
		if (user == null)
			message.setValue("User with " + field + " " + value + " is not found.");
		return user;
	}
		
	private void sendMail(String forgotType, User user){
		String title = "";
		String firstName = user.getName().split(" ")[0];
		if(firstName.length() == 0) firstName = "Guest";
		String content = "<font face=\"Monogram\">Dear " + firstName +",<p>";
		
		if(forgotType.contains("password")){
			String tmppasswd = generatePassword();
			user.setPassword(PasswordUtil.generateHashedPassword(tmppasswd));
			FacadeFactory.getFacade().store(user);

			title = "Requested Password Reset for Your geWorkbench Account";
			content += "You recently requested that your geWorkbench account password be reset."
					+ "<p>The temporary password for your geWorkbench account is: "
					+ "<font color=\"red\">" + tmppasswd + "</font>"
					+ "<br>Please login with it, then change password from geWorkbench GUI.";
		}else{
			title = "Requested Username for Your geWorkbench Account";
			content += "You recently requested the username for your geWorkbench account."
					+ "<p>The username for your geWorkbench account is: "
					+ "<font color=\"red\">" + user.getUsername() + "</font>";
		}
		content += "<p>Thank you,<br>The geWorkbench Team</font>";

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
	
	private String generatePassword(){
		ArrayList<Character> chars = new ArrayList<Character>();
		for(char i = '0'; i <= '9'; i++) chars.add(i);
		for(char i = '@'; i <= 'Z'; i++) chars.add(i);
		for(char i = 'a'; i <= 'z'; i++) chars.add(i);
		for(int i = 33; i < 44; i++) chars.add((char)i);
		int charsSize = chars.size();

		int passwdLength = 8;
		char[] passwd = new char[passwdLength];
		for(int i = 0; i < passwdLength; i++){
			passwd[i] = chars.get((int)(Math.random()*charsSize));
		}
		
		return new String(passwd);
	}


}
