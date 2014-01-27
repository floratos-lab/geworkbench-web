package org.geworkbenchweb.genspace.ui.component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbenchweb.genspace.wrapper.UserWrapper;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class GenSpaceRegistration extends CustomComponent implements
		TextChangeListener, ClickListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6627550806528655509L;

	private TextField userId;
	private PasswordField password;
	private PasswordField passwordDup;
	private TextField fname;
	private TextField lname;
	private TextField labaff;
	private TextField email;
	private TextField phone;
	private TextField addr1;
	private TextField addr2;
	private TextField city;
	private TextField state;
	private TextField zipcode;

	Button save, reset, b_login;

	GenSpaceLogin_1 login;
	
	private GridLayout gridLayout = new GridLayout(2, 15);
	
	private VerticalLayout vLayout = new VerticalLayout();
	
	private Panel regPanel = new Panel();

	public GenSpaceRegistration(GenSpaceLogin_1 genSpaceLogin_1) {
		this.login = genSpaceLogin_1;
		initComponents();
		setCompositionRoot(vLayout);
		this.vLayout.addComponent(regPanel);
		this.regPanel.setContent(gridLayout);
		this.regPanel.setWidth("400px");
		this.regPanel.setHeight("600px");
		//setCompositionRoot(gridLayout);
		gridLayout.setWidth("100%");
	}

	private void initComponents() {
		// this.setSize(500, 500);
		// this.setLayout(new GridLayout(15, 2));
		Label label = new Label("Enter sign in user id *");
		userId = new TextField();
		gridLayout.addComponent(label);
		gridLayout.addComponent(userId);

		Label jp = new Label("Select your password *");
		password = new PasswordField();
		password.addTextChangeListener(this);

		gridLayout.addComponent(jp);
		gridLayout.addComponent(password);

		Label jpc = new Label("Confirm your password *");
		passwordDup = new PasswordField();
		gridLayout.addComponent(jpc);
		gridLayout.addComponent(passwordDup);

		Label j2 = new Label("First Name");
		fname = new TextField();
		gridLayout.addComponent(j2);
		gridLayout.addComponent(fname);

		Label j3 = new Label("Last Name");
		lname = new TextField();
		gridLayout.addComponent(j3);
		gridLayout.addComponent(lname);

		Label j4 = new Label("Lab Affiliation *");
		labaff = new TextField();
		gridLayout.addComponent(j4);
		gridLayout.addComponent(labaff);

		Label emailLabel = new Label("Email Address");
		email = new TextField();
		gridLayout.addComponent(emailLabel);
		gridLayout.addComponent(email);

		Label phoneLabel = new Label("Phone");
		phone = new TextField();
		gridLayout.addComponent(phoneLabel);
		gridLayout.addComponent(phone);

		Label j5 = new Label("Address 1");
		addr1 = new TextField();
		gridLayout.addComponent(j5);
		gridLayout.addComponent(addr1);

		Label j6 = new Label("Address 2");
		addr2 = new TextField();
		gridLayout.addComponent(j6);
		gridLayout.addComponent(addr2);

		Label j7 = new Label("City");
		city = new TextField();
		gridLayout.addComponent(j7);
		gridLayout.addComponent(city);

		Label j9 = new Label("State");
		state = new TextField();
		gridLayout.addComponent(j9);
		gridLayout.addComponent(state);

		Label j8 = new Label("ZIP Code");
		zipcode = new TextField();
		gridLayout.addComponent(j8);
		gridLayout.addComponent(zipcode);

		save = new Button("Save");
		save.addClickListener(this);
		reset = new Button("Reset");
		reset.addClickListener(this);
		b_login = new Button("Login");
		b_login.addClickListener(this);

		gridLayout.addComponent(save);
		gridLayout.addComponent(reset);
		gridLayout.addComponent(b_login);

		save.setEnabled(true);
	}

	@Override
	public void textChange(TextChangeEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void buttonClick(ClickEvent e) {
		if (e.getButton() == save) {
			save.setEnabled(false);
			StringBuffer errMsg = new StringBuffer();
			if (isValid(errMsg)) {

				if (login.getGenSpaceServerFactory().userRegister(getNewUser())) {
					String msg = "User Registered";

					Notification.show(msg,
							Type.TRAY_NOTIFICATION);

					callLogin();
				} 
				else {
					if (errMsg.toString().equals("")) {
						errMsg.append("Error: This username is already in use");
					}
					Notification.show("Registration Failed",
							errMsg.toString(), Type.ERROR_MESSAGE);
				}

			} else {

			}
			save.setEnabled(true);

		} else if (e.getButton() == reset) {

		} else if (e.getButton() == b_login) {
			callLogin();
		}
	}

	private boolean empty(String str) {
		if ("".equalsIgnoreCase(str) || null == str)
			return true;
		else
			return false;
	}

	public boolean isValid(StringBuffer msg) {
		String id = (String) userId.getValue();

		String pw = (String) password.getValue();
		String confirm = (String) passwordDup.getValue();

		String labaffStr = (String) labaff.getValue();

		String pho = (String) phone.getValue();
		String em = (String) email.getValue();

		boolean valid = true;
		if (empty(id)) {
			msg.append("UserId cannot be empty\n");
			valid = false;
		}
		if (empty(pw)) {
			msg.append("Password cannot be empty\n");
			valid = false;
		}
		if (empty(confirm)) {
			msg.append("Confirm password field cannot be empty\n");
			valid = false;
		}
		if (empty(labaffStr)) {
			msg.append("Lab affiliation cannot be empty\n");
			valid = false;
		}
		if (!empty(pw) && !empty(confirm)) {
			if (!pw.equals(confirm)) {
				msg.append("Password confirmation does not match password\n");
				valid = false;

			}
		}

		Pattern pattern;
		Matcher matcher;

		// user name special character validation
		if (!empty(id)) {
			pattern = Pattern.compile("[^0-9a-zA-Z()-_]");

			matcher = pattern.matcher(id);

			if (matcher.find()) {
				msg.append("Invalid user name.\n");
				valid = false;
			}
		}

		// Phone number validation
		if (!empty(pho)) {
			pattern = Pattern.compile("[^0-9a-zA-Z()-]");

			matcher = pattern.matcher(pho);

			if (matcher.find()) {
				msg.append("Phone number contains invalid characters\n");
				valid = false;
			}
		}

		// email validation
		if (!empty(em)) {
			pattern = Pattern.compile("[0-9a-zA-Z()-_.]+@[0-9a-zA-Z()-_.]+");

			matcher = pattern.matcher(em);

			if (!matcher.find()) {
				msg.append("Invalid Email.\n");
				valid = false;
			}
		}
		return valid;
	}

	private User getNewUser() {
		UserWrapper u = new UserWrapper(new User(), this.login);

		u.setUsername((String) userId.getValue());

		String pass = (String) password.getValue();
		u.setPasswordClearText(pass);

		u.setFirstName((String) fname.getValue());
		u.setLastName((String) lname.getValue());

		u.setLabAffiliation((String) labaff.getValue());

		u.setEmail((String) email.getValue());

		u.setPhone((String) phone.getValue());

		u.setAddr1((String) addr1.getValue());
		u.setAddr2((String) addr2.getValue());
		u.setCity((String) city.getValue());
		u.setState((String) state.getValue());
		u.setZipcode((String) zipcode.getValue());

		return u.getDelegate();
	}

	private void callLogin() {
		login.loggedOut();
	}

}
