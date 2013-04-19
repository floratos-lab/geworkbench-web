package org.geworkbenchweb.genspace.ui.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.User;
import org.vaadin.addon.borderlayout.BorderLayout;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class FriendPanel extends SocialPanel{
	
	private String panelTitle;
	
	private List<User> friendList;
	
	private Panel friendPanel;
	
	private VerticalLayout mainLayout;
	
	private BorderLayout blLayout;
	
	private String invisibleUser = "This user is not visible to you. " +
			"They will be visible once they confirm your pending friend requrest";
	
	private String back = "Back";
	
	private GenSpaceLogin login;
	
	public FriendPanel(String panelTitle, GenSpaceLogin login) {
		this.panelTitle = panelTitle;
		this.login = login;
		
		this.blLayout = new BorderLayout();
		this.setCompositionRoot(blLayout);
		this.friendPanel = new Panel(this.panelTitle);
		this.friendPanel.setWidth("500px");
		//this.friendPanel.setHeight("1000px");
		this.friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
		
		this.createMainLayout();
		this.friendPanel.addComponent(mainLayout);
		blLayout.addComponent(friendPanel, BorderLayout.Constraint.CENTER);
	}
	
	public String getPanelTitle() {
		return this.panelTitle;
	}
	
	public void updatePanel() {
		this.friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
		if (this.blLayout.getComponentCount() > 0) {
			this.blLayout.removeAllComponents();
		}
		
		if (this.friendPanel.getComponentIterator().hasNext()) {
			this.friendPanel.removeAllComponents();
		}
		
		this.createMainLayout();
		this.friendPanel.addComponent(mainLayout);
		this.blLayout.addComponent(friendPanel, BorderLayout.Constraint.CENTER);
	}
	
	public void createMainLayout() {
		this.mainLayout = new VerticalLayout();
		Panel tempPanel;
		User tempUser;
		Label tempAffLabel;
		String tempPanelTitle;
		String tempAffiliation;
		Iterator<User> friendIT = this.friendList.iterator();
		while(friendIT.hasNext()) {
			tempUser = friendIT.next();
			final User forListener = tempUser;
			if (tempUser.getFirstName().isEmpty() || tempUser.getLastName().isEmpty()) {
				tempPanelTitle = tempUser.getUsername();
			} else {
				tempPanelTitle = tempUser.getFirstName() + " " + tempUser.getLastName() + " " +  "(" + tempUser.getUsername() +")";
			}
			tempAffiliation = tempUser.getLabAffiliation();
			tempPanel = new Panel(tempPanelTitle);
			tempPanel.setWidth("200px");
			tempPanel.addListener(new ClickListener(){

				private static final long serialVersionUID = 1L;
				
				private String userPanelTitle;
				
				private Panel userPanel;

				public void click(ClickEvent event) {
					if (event.isDoubleClick()) {
						userPanelTitle = event.getComponent().getCaption() + "'s " + "genSpace profile";
						userPanel = new Panel(userPanelTitle);
						userPanel.setScrollable(true);
						userPanel.setWidth("800px");
						//userPanel.setHeight("1000px");
						
						VerticalLayout vLayout = new VerticalLayout();
						userPanel.addComponent(vLayout);
						
						List<String> paramList = new ArrayList<String>();
						Panel uPanel;
						if (!forListener.isVisible()) {
							paramList.add(invisibleUser);
							uPanel = createPanel(event.getComponent().getCaption(), paramList);
							vLayout.addComponent(uPanel);
							mainLayout.removeAllComponents();
							mainLayout.addComponent(userPanel);
							return ;
						}
						
						paramList.add(forListener.getLabAffiliation());
						uPanel = createPanel(event.getComponent().getCaption(), paramList);

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
						System.out.println(mainLayout.getParent().getCaption());
						mainLayout.removeAllComponents();
						mainLayout.addComponent(userPanel);
						
						System.out.println("Test click event: " + event.getSource());
					}
				}
				
				private void createForVisibleUser(){
					
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
				
			});
			tempAffLabel = new Label(tempAffiliation);
			tempPanel.addComponent(tempAffLabel);
			mainLayout.addComponent(tempPanel);
		}
	}

}
