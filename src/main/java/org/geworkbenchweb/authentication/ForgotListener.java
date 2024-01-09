package org.geworkbenchweb.authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.authentication.util.PasswordUtil;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ForgotListener implements ClickListener {
	private static final long serialVersionUID = 6330247049027869269L;
	private Button forgotBtn, closeBtn;
	private Label message;
	private VerticalLayout layout;

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);

	public ForgotListener(Button btn) {
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

		message = new Label("", ContentMode.HTML);
		message.setWidth("300px");
		final TextField textField = new TextField();
		final OptionGroup options = new OptionGroup();
		options.setImmediate(true);
		options.addItem("Email");
		options.setValue("Email");

		if (forgotBtn.getCaption().contains("password")) { // forgot password
			options.setCaption("Please enter the username or email address you registered with.");
			options.addItem("Username");
		} else { // forgot username
			options.setCaption("Please enter the email address you registered with.");
		}

		// FIXME:no email or other people's email/username

		Button submit = new Button("Submit", new ClickListener() {
			private static final long serialVersionUID = 8292680051084017446L;

			@Override
			public void buttonClick(ClickEvent event) {
				List<User> users = null;
				String option = options.getValue().toString().toLowerCase();
				String value = textField.getValue().toString();
				if (value.isEmpty()) {
					message.setValue("Please enter " + option + " in the text field.");
				} else if (option.equalsIgnoreCase("Email")
						&& !emailPattern.matcher(value).matches()) {
					message.setValue("Invalid email.");
				} else {
					users = findUser(option, value);
					if (users != null) {
						String email = users.get(0).getEmail();
						if (email == null || !emailPattern.matcher(email).matches()) {
							message.setValue("Invalid email.");
							users = null;
						}
					}
				}
				if (users != null) {
					try {
						if (users.size() == 1)
							sendMail(forgotBtn.getCaption(), users.get(0));
						else
							sendMail(forgotBtn.getCaption(), users);
						layout.removeAllComponents();
						message.setValue(
								"An email has been sent to you.<br>Please follow the instructions in the email.");
					} catch (MessagingException e) {
						layout.removeAllComponents();
						message.setValue("System error in sending email.<br>Please contact geWorkbench support team.");
					}
					layout.addComponent(message);
					layout.addComponent(closeBtn);
				}
			}
		});

		closeBtn = new Button("Close", new ClickListener() {
			private static final long serialVersionUID = 8292680051084017447L;

			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().removeWindow(window);
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

		UI.getCurrent().addWindow(window);
	}

	private List<User> findUser(String field, String value) {
		String query = "SELECT u FROM User u WHERE u." + field + " = :" + field;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(field, value);
		List<User> users = FacadeFactory.getFacade().list(query, parameters);
		if (users.size() == 0) {
			message.setValue("User with " + field + " " + value + " is not found.");
			return null;
		}
		return users;
	}

	private static void sendMail(String forgotType, User user) throws MessagingException {
		String title = "";
		String realName = user.getName();
		if (realName.length() == 0)
			realName = "Guest";
		String content = "<font face=\"Monogram\">Dear " + realName + ",<p>";

		if (forgotType.contains("password")) {
			String tmppasswd = generatePassword();
			user.setPassword(PasswordUtil.generateHashedPassword(tmppasswd));
			FacadeFactory.getFacade().store(user);

			title = "Requested Password Reset for Your geWorkbench Account";
			content += "You recently requested that your geWorkbench account password be reset."
					+ "<p>The temporary password for your geWorkbench account is: "
					+ "<font color=\"red\">" + tmppasswd + "</font>"
					+ "<br>Please login with it, then change password from geWorkbench GUI.";
		} else {
			title = "Requested Username for Your geWorkbench Account";
			content += "You recently requested the username for your geWorkbench account."
					+ "<p>The username for your geWorkbench account is: "
					+ "<font color=\"red\">" + user.getUsername() + "</font>";
		}
		content += "<p>Thank you,<br>The geWorkbench Team</font>";

		Emailer emailer = new Emailer();
		emailer.send(user.getEmail(), title, content);
	}

	// Send mail for user who has multiple account
	private static void sendMail(String forgotType, List<User> users) throws MessagingException {
		String title = "";
		String realName = users.get(0).getName();
		if (realName.length() == 0)
			realName = "Guest";
		String content = "<font face=\"Monogram\">Dear " + realName + ",<p>";

		String userNames = "";
		for (User u : users)
			userNames += "<br><font color=\"red\">" + u.getUsername() + "</font>";

		if (forgotType.contains("password")) {
			title = "Requested Password Reset for Your geWorkbench Account";
			content += "You recently requested that your geWorkbench account password be reset."
					+ "<p>However, you have multiple accounts with email address " + users.get(0).getEmail() + ": "
					+ userNames
					+ "<br>Please use the option to change your password by user account name for one of these accounts.";
		} else {
			title = "Requested Username for Your geWorkbench Account";
			content += "You recently requested the username for your geWorkbench account."
					+ "<p>The username for your geWorkbench accounts are: "
					+ userNames;
		}

		content += "<p>Thank you,<br>The geWorkbench Team</font>";

		Emailer emailer = new Emailer();
		emailer.send(users.get(0).getEmail(), title, content);
	}

	private static String generatePassword() {
		ArrayList<Character> chars = new ArrayList<Character>();
		for (char i = '0'; i <= '9'; i++)
			chars.add(i);
		for (char i = '@'; i <= 'Z'; i++)
			chars.add(i);
		for (char i = 'a'; i <= 'z'; i++)
			chars.add(i);
		for (int i = 33; i < 44; i++)
			chars.add((char) i);
		int charsSize = chars.size();

		int passwdLength = 8;
		char[] passwd = new char[passwdLength];
		for (int i = 0; i < passwdLength; i++) {
			passwd[i] = chars.get((int) (Math.random() * charsSize));
		}

		return new String(passwd);
	}
}
