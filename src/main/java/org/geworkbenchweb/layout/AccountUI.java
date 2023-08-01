package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.Map;

import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.authentication.exceptions.InvalidCredentialsException;
import org.vaadin.appfoundation.authentication.exceptions.PasswordRequirementException;
import org.vaadin.appfoundation.authentication.exceptions.PasswordsDoNotMatchException;
import org.vaadin.appfoundation.authentication.exceptions.TooShortPasswordException;
import org.vaadin.appfoundation.authentication.exceptions.TooShortUsernameException;
import org.vaadin.appfoundation.authentication.exceptions.UsernameExistsException;
import org.vaadin.appfoundation.authentication.util.PasswordUtil;
import org.vaadin.appfoundation.authentication.util.UserUtil;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.Icon;

public class AccountUI extends VerticalLayout implements ClickListener {
	private static final long serialVersionUID = -3984441409433266815L;

	final private Panel accountPanel = new Panel();
	final private TextField username = new TextField("Username");
	final private PasswordField oldpassword = new PasswordField("Current password");
	final private PasswordField newpassword = new PasswordField("New password");
	final private PasswordField verifyPassword = new PasswordField(
			"Verify new password");
	final private CheckBox changePassword = new CheckBox("Change password");
	final private TextField realName = new TextField("Real name");
	final private TextField email = new TextField("Email address");
	final private Button updateButton = new Button("Update Account");
	final private FormLayout layout = new FormLayout();

	public AccountUI(){
		username.setWidth("145px");
		realName.setWidth("145px");
		email.setWidth("145px");
		oldpassword.setWidth("145px");
		newpassword.setWidth("145px");
		verifyPassword.setWidth("145px");

		accountPanel.setStyleName(Reindeer.PANEL_LIGHT);
		accountPanel.setWidth("300px");

		username.setNullRepresentation("");
		realName.setNullRepresentation("");
		email.setNullRepresentation("");
		oldpassword.setNullRepresentation("");
		newpassword.setNullRepresentation("");
		verifyPassword.setNullRepresentation("");
		
		User user = SessionHandler.get();
		username.setValue(user.getUsername());
		realName.setValue(user.getName());
		email.setValue(user.getEmail());

		updateButton.setImmediate(true);
		updateButton.addListener(this);
		updateButton.setClickShortcut(KeyCode.ENTER);

		newpassword.setVisible(false);
		verifyPassword.setVisible(false);
		changePassword.setImmediate(true);
		changePassword.addListener(new ValueChangeListener(){
			private static final long serialVersionUID = -4598771538357581782L;
			public void valueChange(ValueChangeEvent event) {
				if((Boolean)event.getProperty().getValue()){
					newpassword.setVisible(true);
					verifyPassword.setVisible(true);
				}else{
					newpassword.setVisible(false);
					verifyPassword.setVisible(false);
				}
			}
		});

		layout.setSpacing(true);
		layout.addComponent(username);
		layout.addComponent(realName);
		layout.addComponent(email);
		layout.addComponent(oldpassword);
		layout.addComponent(changePassword);
		layout.addComponent(newpassword);
		layout.addComponent(verifyPassword);
		layout.addComponent(updateButton);
		accountPanel.setContent(layout);

		this.setSizeFull();
		this.addComponent(accountPanel);
		this.setComponentAlignment(accountPanel, Alignment.TOP_CENTER);
		
	}

	@Override
	public void buttonClick(ClickEvent event) {
		String message = "";
		try {
			message = update(username, oldpassword, newpassword,
					verifyPassword, realName, email);
		} catch (TooShortPasswordException e) {
			message = "New password is too short, it needs to be at least "
					+ UserUtil.getMinPasswordLength() + " characters long";
		} catch (TooShortUsernameException e) {
			message = "Username is too short, it needs to be at least "
					+ UserUtil.getMinUsernameLength() + " characters long";
		} catch (PasswordsDoNotMatchException e) {
			message = "New password verification has failed";
		} catch (UsernameExistsException e) {
			message = "The chosen username already exists, please pick another one";
		} catch (PasswordRequirementException e) {
			message = "Password does not meet the set requirements";
		} catch (InvalidCredentialsException e) {
			message = "Current password is incorrect";
		}

		MessageBox mb = new MessageBox(getWindow(), "Account Update Status", Icon.INFO,
				message, new MessageBox.ButtonConfig(MessageBox.ButtonType.OK,
						"Ok"));
		mb.show();

		oldpassword.setValue(null);
		newpassword.setValue(null);
		verifyPassword.setValue(null);
	}
	
	private String update(TextField username, PasswordField oldpassword,
			PasswordField newpassword, PasswordField verifyPassword,
			TextField realName, TextField email)
			throws TooShortPasswordException, TooShortUsernameException,
			PasswordsDoNotMatchException, UsernameExistsException,
			PasswordRequirementException, InvalidCredentialsException {

		String usernameStr  = (String)username.getValue();
		String oldpasswdStr = (String)oldpassword.getValue();
		String newpasswdStr = (String)newpassword.getValue();
		String verpasswdStr = (String)verifyPassword.getValue();
		String realnameStr  = (String)realName.getValue();
		String emailStr     = (String)email.getValue();

		User user = SessionHandler.get();
		
		if(usernameStr.equals(user.getUsername())
				&& realnameStr.equals(user.getName())
				&& emailStr.equals(user.getEmail())
				&& !changePassword.booleanValue())
			return "You haven't changed any account information.";
		
		if(empty(oldpasswdStr)) return "Please enter current password.";

		if (!PasswordUtil.verifyPassword(user, oldpasswdStr)) 
			throw new InvalidCredentialsException();

		if(changePassword.booleanValue()){
			if(empty(newpasswdStr)) return "Please enter new password.";
			if(empty(verpasswdStr)) return "Please verifiy new password.";
			UserUtil.changePassword(user, oldpasswdStr, newpasswdStr, verpasswdStr);
		}

		if(!usernameStr.equals(user.getUsername())){
			if (usernameStr.length() < UserUtil.getMinUsernameLength())
				throw new TooShortUsernameException();

			String query = "SELECT u FROM User u WHERE u.username = :username";
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("username", usernameStr);
			if (FacadeFactory.getFacade().find(query, parameters) != null)
				throw new UsernameExistsException();

			user.setUsername(usernameStr);
		}

		if (!realnameStr.equals(user.getName()))
			user.setName(realnameStr);

		if (!emailStr.equals(user.getEmail()))
			user.setEmail(emailStr);

		FacadeFactory.getFacade().store(user);

		return  "You have successfully updated your account.";
	}
	
	private boolean empty(String s){
		return s == null || s.length() == 0;
	}
	
}