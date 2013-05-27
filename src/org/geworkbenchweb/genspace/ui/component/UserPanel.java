package org.geworkbenchweb.genspace.ui.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.User;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class UserPanel extends Panel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static String invisibleUser = "This user is not visible to you. " +
			"They will be visible once they confirm your pending friend requrest";
	
	private String userPanelTitle;
	
	private User forListener;
	
	public UserPanel(String userPanelTitle, User forListener) {
		this.userPanelTitle = userPanelTitle;
		this.forListener = forListener;
		
		this.setScrollable(true);
		this.setWidth("800px");
		this.makeLayout();
	}
	
	public void makeLayout() {
		VerticalLayout vLayout = new VerticalLayout();
		this.addComponent(vLayout);
		
		List<String> paramList = new ArrayList<String>();
		Panel uPanel;
		if (!forListener.isVisible()) {
			paramList.add(invisibleUser);
			uPanel = createPanel(this.userPanelTitle, paramList);
			vLayout.addComponent(uPanel);
			/*mainLayout.removeAllComponents();
			mainLayout.addComponent(this);*/
			return ;
		}
		
		paramList.add(forListener.getLabAffiliation());
		uPanel = createPanel(this.userPanelTitle, paramList);

		String interest = forListener.getInterests();
		if (interest == null) {
			interest = "not disclosed";
		}
		paramList = new ArrayList<String>();
		paramList.add(interest);
		Panel researchPanel = createPanel("Research Interests: ", paramList);
		
		paramList = new ArrayList<String>();
		String phone = forListener.getPhone();
		if (phone.isEmpty()) {
			phone = "N\\A";
		}
		
		String email = forListener.getEmail();
		if (email.isEmpty()) {
			email = "N\\A";
		}
		
		String mailAddress;
		if (forListener.getAddr1().isEmpty()){
			mailAddress = "not provided";
		}	else {
			mailAddress = forListener.getAddr1() + forListener.getAddr2() + 
					forListener.getCity() + ", " + forListener.getState() + ", " + forListener.getZipcode();
		}

		String cInPhone = "Phone: " + phone;
		String cInEmail = "Email: " + email;
		String cInMail = "Mailing Address: " + mailAddress;
		paramList.add(cInPhone);
		paramList.add(cInEmail);
		paramList.add(cInMail);
		Panel contactInfo = createPanel("Contact Information: ", paramList);
		
		vLayout.addComponent(uPanel);
		vLayout.addComponent(researchPanel);
		vLayout.addComponent(contactInfo);
		//System.out.println(mainLayout.getParent().getCaption());
		/*mainLayout.removeAllComponents();
		mainLayout.addComponent(this);*/
		
	}
	
	private Panel createPanel(String panelName, List<String> panelContent) {
		Panel panel = new Panel(panelName);
		Iterator<String> pIT = panelContent.iterator();
		while(pIT.hasNext()){
			Label label = new Label(pIT.next());
			panel.addComponent(label);
		}
		return panel;
	}

}
