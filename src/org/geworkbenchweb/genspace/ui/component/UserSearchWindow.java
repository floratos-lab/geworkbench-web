package org.geworkbenchweb.genspace.ui.component;

import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbenchweb.genspace.GenSpaceServerFactory;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class UserSearchWindow extends Window implements RefreshListener{
	
	private GenSpaceLogin login;
	
	private SocialNetworkHome sHome;
	
	private Refresher refresher;
	
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
		this.sHome = sHome;
		this.friend = friend;
		
		setModal(true);
		setWidth("20%");
		setHeight("40%");
		
		center();
		
		this.caption = this.friend.getUsername() + "'s  genSpace profile";
		setCaption(this.caption);
		
		this.addComponent(vLayout);
		
		/*this.refresher = new Refresher();
		this.refresher.setRefreshInterval(100);
		this.addComponent(refresher);*/
		
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
		userNamePanel.addComponent(organization);
		vLayout.addComponent(userNamePanel);
		
		Panel researchPanel = new Panel("Research Interests");
		Label interest = new Label();
		String rInterest = this.friend.getInterests();
		if (rInterest != null && !rInterest.isEmpty()) {
			interest.setValue(rInterest);
		} else {
			interest.setValue(this.noInfo);
		}
		researchPanel.addComponent(interest);
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
		contact.addComponent(phone);
		contact.addComponent(email);
		contact.addComponent(mail);
		vLayout.addComponent(contact);
		
		Label friendSituation = new Label();
		if (isFriend) {
			System.out.println("Is a Friend");
			friendString = friendString.replace("xxx", this.friend.getUsername());
			friendSituation.setValue(friendString);
			Button remove = new Button("Remove friend");
			remove.addListener(new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(Button.ClickEvent event) {
					try {
						login.getGenSpaceServerFactory().getFriendOps().removeFriend(friend.getId());
						removeFriend = removeFriend.replace("xxx", friend.getUsername());
						refreshDB();
						getApplication().getMainWindow().showNotification(removeFriend);
												
						sHome.getInstance().updateForm();
						/*login.getChatHandler().getRosterFrame().removedCache.add(friend.getUsername() + "@genspace");
						login.getChatHandler().getRosterFrame().refresh();*/
						updateWindowContents();
						login.getPusher().push();
					} catch (Exception e) {
						GenSpaceServerFactory.handleException(e);
					}

				}
			});
			vLayout.addComponent(friendSituation);
			vLayout.addComponent(remove);
		} else if (sHome.getInstance().pendingFriendRequestTo(friend)) {
			System.out.println("A pending friend");
			friendString = requestFriendString.replace("xxx", this.friend.getUsername());
			friendSituation.setValue(friendString);
			Button cancel = new Button("Cancel friend request");
			cancel.addListener(new Button.ClickListener() {
				
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(Button.ClickEvent event) {
					try {
						login.getGenSpaceServerFactory().getFriendOps().removeFriend(friend.getId());
						cancelFriend = cancelFriend.replace("xxx", friend.getUsername());
						refreshDB();
						getApplication().getMainWindow().showNotification(cancelFriend);
						
						sHome.getInstance().updateForm();
						/*login.getChatHandler().getRosterFrame().removedCache.add(friend.getUsername() + "@genspace");
						login.getChatHandler().getRosterFrame().refresh();*/
						updateWindowContents();
						login.getPusher().push();
					} catch (Exception e) {
						GenSpaceServerFactory.handleException(e);
					}
				}
			});
			vLayout.addComponent(friendSituation);
			vLayout.addComponent(cancel);
		} else {
			System.out.println("Not a friend");
			friendString = noFriendString.replace("xxx", this.friend.getUsername());
			friendSituation.setValue(friendString);
			Button add = new Button("Add as a friend");
			add.addListener(new Button.ClickListener() {
				
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(Button.ClickEvent event) {
					try {
						login.getGenSpaceServerFactory().getFriendOps().addFriend(friend.getId());
						addFriend = addFriend.replace("xxx", friend.getUsername());
						refreshDB();
						getApplication().getMainWindow().showNotification(addFriend);
						
						sHome.getInstance().updateForm();
						//login.getChatHandler().getRosterFrame().refresh();
						updateWindowContents();
						login.getPusher().push();
					} catch (Exception e) {
						GenSpaceServerFactory.handleException(e);
					}
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

	@Override
	public void refresh(Refresher source) {
		// TODO Auto-generated method stub
		updateWindowContents();
	}
}
