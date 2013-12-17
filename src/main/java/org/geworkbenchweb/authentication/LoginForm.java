/**
 * 
 */
package org.geworkbenchweb.authentication;

import org.geworkbenchweb.layout.UMainLayout;
import org.vaadin.alump.fancylayouts.FancyCssLayout;
import org.vaadin.appfoundation.authentication.exceptions.AccountLockedException;
import org.vaadin.appfoundation.authentication.exceptions.InvalidCredentialsException;
import org.vaadin.appfoundation.authentication.util.AuthenticationUtil;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * @author zji
 * @version $Id$
 */
public class LoginForm extends VerticalLayout {

	private static final long serialVersionUID = -469128068617789982L;

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

				try {
					AuthenticationUtil.authenticate(username, password);
					getApplication().getMainWindow().setContent(
							new UMainLayout());
				} catch (InvalidCredentialsException e) {
					feedbackLabel
							.setValue("Either username or password was wrong");
				} catch (AccountLockedException e) {
					feedbackLabel.setValue("The given account has been locked");
				} catch (Exception e) {
					e.printStackTrace();
					feedbackLabel.setValue("Some other exception: "
							+ e.getMessage());
				}
			}
		});

		login.setClickShortcut(KeyCode.ENTER);

		HorizontalLayout group = new HorizontalLayout();
		group.setSpacing(true);
		group.addComponent(switchToRegisterButton);
		group.addComponent(login);

		this.addComponent(image);
		this.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
		this.addComponent(usernameField);
		this.setComponentAlignment(usernameField, Alignment.MIDDLE_CENTER);
		this.addComponent(passwordField);
		this.setComponentAlignment(passwordField, Alignment.MIDDLE_CENTER);
		this.addComponent(group);
		this.setComponentAlignment(group, Alignment.MIDDLE_CENTER);
		this.addComponent(feedbackLabel);
		this.setComponentAlignment(feedbackLabel, Alignment.MIDDLE_CENTER);
		feedbackLabel.setWidth("50%");

		final FancyCssLayout cssLayout = new FancyCssLayout();
		cssLayout.setSlideEnabled(true);
		cssLayout.setWidth("700px");

		this.addComponent(cssLayout);
		this.setComponentAlignment(cssLayout, Alignment.TOP_CENTER);
	}
}
