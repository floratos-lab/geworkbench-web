package org.geworkbenchweb.genspace.ui.component;

import org.geworkbench.components.genspace.server.stubs.User;
import org.vaadin.addon.borderlayout.BorderLayout;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class ProfilePanel extends SocialPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private GenSpaceLogin login;
	
	private BorderLayout bLayout;

	private Panel profilePanel;
	
	private Form profileForm;
	
	private String panelTitle;
	
	private String firstNameTitle = "My First Name: ";
	
	private String lastNameTitle = "My Last Name: ";
	
	private String labTitle = "My Lab: ";
	
	private String jobTitle = "Job Title: ";
	
	private String emailAdd = "Email Address: ";
	
	private String phoneName = "Phone: ";
	
	private String addressName = "Address: ";
	
	private String cityInfo = "City: ";
	
	private String stateInfo = "State: ";
	
	private String postCode = "Postal Code: ";
	
	private String researchInterest = "My Research Interests: ";
	
	private String saveString = "Save";
	
	private String unLogged = "Please log in to utilize GenSpace's social features";
	
	private TextField firstName;
	
	private String firstNameString;
	
	private TextField lastName;
	
	private String lastNameString;
	
	private TextField labTitleField;
	
	private String labString;
	
	private TextField jobTitleField;
	
	private String jobString;
	
	private TextField emailAddField;
	
	private String emailString;
	
	private TextField phoneNameField;
	
	private String phoneString;
	
	private TextField addressNameField;
	
	private String addNameString;
	
	private TextField addressNameField2;
	
	private String addNameString2;
	
	private TextField cityInfoField;
	
	private String cityString;
	
	private TextField stateInfoField;
	
	private String stateString;
	
	private TextField postCodeField;
	
	private String postString;
	
	private TextArea researchArea;
	
	private String researchString;
	
	public ProfilePanel(String panelTitle, GenSpaceLogin login) {
		this.login = login;
		
		bLayout = new BorderLayout();
		setCompositionRoot(bLayout);
		
		this.panelTitle = panelTitle;
		
		profilePanel = new Panel(this.panelTitle);
		this.createProfileForm();
	}
	
	public String getPanelTitle() {
		return this.panelTitle;
	}
	
	public void updatePanel() {
		this.createProfileForm();
	}
		
	public void createProfileForm() {
		if(this.profilePanel != null)
			this.profilePanel.removeAllComponents();
		
		if(!login.getGenSpaceServerFactory().isLoggedIn()) {
			Label unloggedLabel = new Label(this.unLogged);
			this.profilePanel.addComponent(unloggedLabel);
			this.bLayout.addComponent(profilePanel, BorderLayout.Constraint.CENTER);
			return ;
		}

		this.retrieveUserInfo();
		
		profileForm = new Form();
		firstName = new TextField(firstNameTitle);
		lastName = new TextField(lastNameTitle);
		labTitleField = new TextField(labTitle);
		jobTitleField = new TextField(jobTitle);
		emailAddField = new TextField(emailAdd);
		phoneNameField = new TextField(phoneName);
		addressNameField = new TextField(addressName);
		addressNameField2 = new TextField();
		cityInfoField = new TextField(cityInfo);
		stateInfoField = new TextField(stateInfo);
		postCodeField = new TextField(postCode);
		researchArea = new TextArea(researchInterest);
		researchArea.setWidth(String.valueOf(postCodeField.getWidth()));
		
		if (firstNameString != null)
			firstName.setValue(firstNameString);
		if (lastNameString != null)
			lastName.setValue(lastNameString);
		if (emailString != null)
			emailAddField.setValue(emailString);
		if (phoneString != null)
			phoneNameField.setValue(phoneString);
		if (addNameString != null)
			addressNameField.setValue(addNameString);
		if (addNameString2 != null)
			addressNameField2.setValue(addNameString2);
		if (cityString != null)
			cityInfoField.setValue(cityString);
		if (stateString != null)
			stateInfoField.setValue(stateString);
		if (postString != null)
			postCodeField.setValue(postString);
		if (labString != null)
			labTitleField.setValue(labString);
		if (researchString != null)
			researchArea.setValue(researchString);
		if (jobString != null)
			jobTitleField.setValue(jobString);
		
		Button saveButton = new Button(saveString);
		saveButton.addListener(new ClickListener(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(Button.ClickEvent event) {
				login.getGenSpaceServerFactory().getUser().setFirstName(firstName.getValue().toString());
				login.getGenSpaceServerFactory().getUser().setLastName(lastName.getValue().toString());
				login.getGenSpaceServerFactory().getUser().setLabAffiliation(labTitleField.getValue().toString());
				login.getGenSpaceServerFactory().getUser().setWorkTitle(jobTitleField.getValue().toString());
				login.getGenSpaceServerFactory().getUser().setEmail(emailAddField.getValue().toString());
				login.getGenSpaceServerFactory().getUser().setPhone(phoneNameField.getValue().toString());
				login.getGenSpaceServerFactory().getUser().setAddr1(addressNameField.getValue().toString());
				login.getGenSpaceServerFactory().getUser().setAddr2(addressNameField2.getValue().toString());
				login.getGenSpaceServerFactory().getUser().setCity(cityInfoField.getValue().toString());
				login.getGenSpaceServerFactory().getUser().setState(stateInfoField.toString());
				login.getGenSpaceServerFactory().getUser().setZipcode(postCodeField.toString());
				login.getGenSpaceServerFactory().getUser().setInterests(researchArea.getValue().toString());
				
				login.getGenSpaceServerFactory().userUpdate();
			}
		});
		
		profileForm.getLayout().addComponent(firstName);
		profileForm.getLayout().addComponent(lastName);
		profileForm.getLayout().addComponent(labTitleField);
		profileForm.getLayout().addComponent(jobTitleField);
		profileForm.getLayout().addComponent(emailAddField);
		profileForm.getLayout().addComponent(phoneNameField);
		profileForm.getLayout().addComponent(addressNameField);
		profileForm.getLayout().addComponent(addressNameField2);
		profileForm.getLayout().addComponent(cityInfoField);
		profileForm.getLayout().addComponent(stateInfoField);
		profileForm.getLayout().addComponent(postCodeField);
		profileForm.getLayout().addComponent(researchArea);
		profileForm.getLayout().addComponent(saveButton);
		profilePanel.addComponent(profileForm);
		
		bLayout.addComponent(profilePanel, BorderLayout.Constraint.CENTER);
	}
	
	private void retrieveUserInfo() {
		if (login.getGenSpaceServerFactory().isLoggedIn()) {
			User u = login.getGenSpaceServerFactory().getUser();
			firstNameString = u.getFirstName();
			lastNameString = u.getLastName();
			emailString = u.getEmail();
			phoneString = u.getPhone();
			addNameString = u. getAddr1();
			addNameString2 = u.getAddr2();
			cityString = u.getCity();
			stateString = u.getState();
			postString = u.getZipcode();
			labString = u.getLabAffiliation();
			researchString = u.getInterests();
			jobString = u.getWorkTitle();
		} else {
			return ;
		}
	}

}
