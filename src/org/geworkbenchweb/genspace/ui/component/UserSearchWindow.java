package org.geworkbenchweb.genspace.ui.component;

import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbenchweb.events.FriendStatusChangeEvent;
import org.geworkbenchweb.genspace.GenSpaceServerFactory;
import org.geworkbenchweb.genspace.ui.GenSpaceWindow;
import org.geworkbenchweb.utils.LayoutUtil;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class UserSearchWindow extends Window {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private GenSpaceLogin login;
	
	private int myID;
	
	private SocialNetworkHome sHome;
	
	private VerticalLayout vLayout = new VerticalLayout();
	
	private User friend;
	
	private String caption;
	
	private String noInfo = "No information";
	
	private String friendString = "(xxx is a friend)";
	
	private String requestFriendString = "(You have requested xxx to add you as a friend, but they have not responded yet)";
	
	private String noFriendString = "(xxx is not a friend)";
	
	private String removeFriend = "Your are no longer friend with xxx";
	
	private String cancelFriend = "You have canceld your friend request to xxx";
	
	private String addFriend = "A friend sent for xxx's approval. You will not become friends until he or she accepts your request.";

	public UserSearchWindow(User friend, GenSpaceLogin login, SocialNetworkHome sHome) {
		this.login = login;
		this.myID = this.login.getGenSpaceServerFactory().getUser().getId();
		this.friend = friend;
		this.sHome = sHome;
		
		setModal(true);
		setWidth("20%");
		setHeight("40%");
		
		center();
		
		this.caption = this.friend.getUsername() + "'s  genSpace profile";
		setCaption(this.caption);
		
		this.setContent(vLayout);		
		this.updateWindowContents();
	}
	
	private void updateWindowContents() {
		if (vLayout.getComponentCount() > 0) {
			vLayout.removeAllComponents();
			refreshDB();
		}
		
		boolean isFriend = this.friend.isFriendsWith();

		Panel userNamePanel = new Panel(this.friend.getUsername());
		Label organization = new Label();
		String affiliation = this.friend.getLabAffiliation();
		if (affiliation != null && !affiliation.isEmpty()) {
			organization.setValue(affiliation);
		} else {
			organization.setValue(this.noInfo);
		}
		userNamePanel.setContent(LayoutUtil.addComponent(organization));
		vLayout.addComponent(userNamePanel);
		
		Panel researchPanel = new Panel("Research Interests");
		Label interest = new Label();
		String rInterest = this.friend.getInterests();
		if (rInterest != null && !rInterest.isEmpty()) {
			interest.setValue(rInterest);
		} else {
			interest.setValue(this.noInfo);
		}
		researchPanel.setContent(LayoutUtil.addComponent(interest));
		vLayout.addComponent(researchPanel);
		
		Panel contact = new Panel("Contact Information");
		Label phone = new Label();
		Label email = new Label();
		Label mail = new Label();
		String phoneString = this.friend.getPhone();
		String emailString = this.friend.getEmail();
		String mailString = this.friend.getAddr1() + " " + this.friend.getAddr2();
		if (phoneString != null && !phoneString.isEmpty()) {
			phone.setValue("Phone: " + phoneString);
		} else {
			phone.setValue("Phone: " + this.noInfo);
		}
		if (emailString != null && !emailString.isEmpty()) {
			email.setValue("Email: " + emailString);
		} else {
			email.setValue("Email: " + this.noInfo);
		}
		if (mailString != null && !mailString.isEmpty()) {
			mail.setValue("Mailing Address: " + mailString);
		} else {
			mail.setValue("Mailing Address: " + this.noInfo);
		}
		VerticalLayout clayout = LayoutUtil.addComponent(phone);
		clayout.addComponent(email);
		clayout.addComponent(mail);
		contact.setContent(clayout);
		vLayout.addComponent(contact);
		
		Label friendSituation = new Label();
		if (isFriend) {
			//System.out.println("Is a Friend");
			friendString = friendString.replace("xxx", this.friend.getUsername());
			friendSituation.setValue(friendString);
			Button remove = new Button("Remove friend");
			remove.addClickListener(new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(Button.ClickEvent event) {
					UI.getCurrent().access(new Runnable(){
						@Override
						public void run(){
							try {
								login.getGenSpaceServerFactory().getFriendOps().removeFriend(friend.getId());
								removeFriend = removeFriend.replace("xxx", friend.getUsername());
								refreshDB();
								Notification.show(removeFriend);
														
								sHome.getInstance().updateForm();
								updateWindowContents();
								//When user decide to remove a friend, fire the event.
								//The other two button invokes nothing, because user has to wait his/her requesting recipient to response
								GenSpaceWindow.getGenSpaceBlackboard().fire(new FriendStatusChangeEvent(myID, friend.getId()));
							} catch (Exception e) {
								GenSpaceServerFactory.handleException(e);
							}
						}
					});
				}
			});
			vLayout.addComponent(friendSituation);
			vLayout.addComponent(remove);
		} else if (sHome.getInstance().pendingFriendRequestTo(friend)) {
			System.out.println("A pending friend");
			friendString = requestFriendString.replace("xxx", this.friend.getUsername());
			friendSituation.setValue(friendString);
			Button cancel = new Button("Cancel friend request");
			cancel.addClickListener(new Button.ClickListener() {
				
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(Button.ClickEvent event) {
					UI.getCurrent().access(new Runnable(){
						@Override
						public void run(){
							try {
								login.getGenSpaceServerFactory().getFriendOps().removeFriend(friend.getId());
								cancelFriend = cancelFriend.replace("xxx", friend.getUsername());
								refreshDB();
								Notification.show(cancelFriend);
								
								sHome.getInstance().updateForm();
								updateWindowContents();
							} catch (Exception e) {
								GenSpaceServerFactory.handleException(e);
							}
						}
					});
				}
			});
			vLayout.addComponent(friendSituation);
			vLayout.addComponent(cancel);
		} else {
			System.out.println("Not a friend");
			friendString = noFriendString.replace("xxx", this.friend.getUsername());
			friendSituation.setValue(friendString);
			Button add = new Button("Add as a friend");
			add.addClickListener(new Button.ClickListener() {
				
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(Button.ClickEvent event) {
					UI.getCurrent().access(new Runnable(){
						@Override
						public void run(){
							try {
								login.getGenSpaceServerFactory().getFriendOps().addFriend(friend.getId());
								addFriend = addFriend.replace("xxx", friend.getUsername());
								refreshDB();
								Notification.show(addFriend);
								
								sHome.getInstance().updateForm();
								updateWindowContents();
							} catch (Exception e) {
								GenSpaceServerFactory.handleException(e);
							}
						}
					});
				}
			});
			vLayout.addComponent(friendSituation);
			vLayout.addComponent(add);
		}
	}
	
	public void refreshDB()
	{
		login.getGenSpaceServerFactory().userUpdate();
		login.getGenSpaceServerFactory().updateCachedUser();
		login.getGenSpaceServerFactory().otherUserUpdate(friend);
		login.getGenSpaceServerFactory().getFriendOps().getFriends();
		this.friend = login.getGenSpaceServerFactory().getUserOps().getProfile(this.friend.getUsername());
	}
}
