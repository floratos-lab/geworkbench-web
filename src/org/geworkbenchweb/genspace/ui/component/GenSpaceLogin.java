package org.geworkbenchweb.genspace.ui.component;

import java.util.HashMap;

import javax.swing.JLabel;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.ChatStatusChangeEvent;
import org.geworkbenchweb.events.ChatStatusChangeEvent.ChatStatusChangeEventListener;
import org.geworkbenchweb.events.FriendStatusChangeEvent.FriendStatusChangeListener;
import org.geworkbenchweb.genspace.FBManager;
import org.geworkbenchweb.genspace.GenSpaceServerFactory;
import org.geworkbenchweb.genspace.chat.ChatReceiver;
import org.geworkbenchweb.genspace.ui.GenSpaceComponent;
import org.geworkbenchweb.genspace.ui.GenSpaceWindow;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;
import org.vaadin.addon.borderlayout.BorderLayout;
import org.vaadin.artur.icepush.ICEPush;



import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;

import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class GenSpaceLogin extends AbstractGenspaceTab implements GenSpaceTab, ClickListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2600542282749100899L;

	private GenSpaceServerFactory genSpaceServerFactory = new GenSpaceServerFactory();
	private ChatReceiver chatHandler;
	private ActivityFeedWindow afWindow;
	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private Label lblResult;
	@AutoGenerated
	private Button button_1;
	@AutoGenerated
	private Button button_2;
	@AutoGenerated
	private Button button_3;
	@AutoGenerated
	private PasswordField txtPassword;
	@AutoGenerated
	private TextField txtUsername;
	@AutoGenerated
	private Label label_2;
	@AutoGenerated
	private Label label_1;
	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */
	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */
	
	private BorderLayout borderLayout;
	public WorkflowWrapper currentWorkflow;
	private ICEPush push;
	private FBManager fbManager;

	public ChatReceiver getChatHandler() {
		if (this.chatHandler != null)
			return this.chatHandler;
		else
			return null;
	}
	
	public ICEPush getPusher() {
		return this.push;
	}
	
	public GenSpaceServerFactory getGenSpaceServerFactory()
	{
		return this.genSpaceServerFactory;
	}
	
	@Override
	public void loggedIn() {
		//String user = GenSpaceServerFactory.getUsername();
		String user = genSpaceServerFactory.getUsername();
		GenSpaceSecurityPanel p = new GenSpaceSecurityPanel(user, genSpaceParent, this);
		mainLayout.removeAllComponents();
		mainLayout.addComponent(p);
		mainLayout.setExpandRatio(p, 1.0f);
		this.push = this.genSpaceParent.getPusher();
		this.mainLayout.addComponent(this.push);
		GenSpaceWindow.getGenSpaceBlackboard().fire(new ChatStatusChangeEvent(user));
		//push = new ICEPush();
	}
	
	@Override
	public void loggedOut() {
		mainLayout.removeAllComponents();
		mainLayout.addComponent(borderLayout);
		mainLayout.setExpandRatio(borderLayout, 1.0f);
		getApplication().getMainWindow().removeWindow(this.chatHandler.rf);
		getApplication().getMainWindow().removeWindow(this.afWindow);
	}
	
	@Override
	public void tabSelected() {
		// TODO Auto-generated method stub
		
	}
	
	private GenSpaceComponent genSpaceParent;
	
	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */
	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */
	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */
	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */
	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public GenSpaceLogin(GenSpaceComponent genSpaceParent) {
		super(null);
		this.genSpaceParent = genSpaceParent;
		buildMainLayout();
		this.push = this.genSpaceParent.getPusher();
		this.mainLayout.addComponent(push);
//		initListeners();
		setCompositionRoot(mainLayout);

		// TODO add user code here
	}

	@SuppressWarnings("deprecation")
	@AutoGenerated
	private AbstractLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);
		mainLayout.addStyleName("genspaceback");
		
		borderLayout = new BorderLayout();
		mainLayout.addComponent(borderLayout);
		mainLayout.setExpandRatio(borderLayout, 1);
		
		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");
		
		// label_1
		label_1 = new Label();
		label_1.setImmediate(false);
		label_1.setWidth("-1px");
		label_1.setHeight("-1px");
		label_1.setValue("Username");
//		mainLayout.addComponent(label_1, "top:20.0px;left:20.0px;");
		
		// label_2
		label_2 = new Label();
		label_2.setImmediate(false);
		label_2.setWidth("-1px");
		label_2.setHeight("-1px");
		label_2.setValue("Password");
//		mainLayout.addComponent(label_2, "top:60.0px;left:20.0px;");
		
		// txtUsername
		txtUsername = new TextField();
		txtUsername.setImmediate(false);
		txtUsername.setWidth("-1px");
		txtUsername.setHeight("-1px");
		txtUsername.setRequired(true);
		txtUsername.setSecret(false);
//		mainLayout.addComponent(txtUsername, "top:20.0px;left:103.0px;");
		
		// txtPassword
		txtPassword = new PasswordField();
		txtPassword.setImmediate(false);
		txtPassword.setWidth("-1px");
		txtPassword.setHeight("-1px");
		txtPassword.setRequired(true);
//		txtPassword.setSecret(false);
//		mainLayout.addComponent(txtPassword, "top:60.0px;left:103.0px;");
		
		// button_1
		button_1 = new Button();
		button_1.setCaption("Login");
		button_1.setImmediate(true);
		button_1.setWidth("-1px");
		button_1.setHeight("-1px");
//		mainLayout.addComponent(button_1, "top:100.0px;left:173.0px;");
		
		// button_2
		button_2 = new Button();
		button_2.setCaption("Clear");
		button_2.setImmediate(true);
		button_2.setWidth("-1px");
		button_2.setHeight("-1px");
		
		// button_3
		button_3 = new Button();
		button_3.setCaption("Register");
		button_3.setImmediate(true);
		button_3.setWidth("-1px");
		button_3.setHeight("-1px");
		
		// lblResult
		lblResult = new Label();
		lblResult.setImmediate(false);
		lblResult.setWidth("-1px");
		lblResult.setHeight("-1px");
		lblResult.setValue(" ");
//		mainLayout.addComponent(lblResult, "top:122.0px;left:40.0px;");
		
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidth("800px");
		horizontalLayout.addComponent(label_1);		
		horizontalLayout.addComponent(txtUsername);
		horizontalLayout.addComponent(label_2);
		horizontalLayout.addComponent(txtPassword);
		horizontalLayout.addComponent(button_1);
		horizontalLayout.addComponent(button_2);
		horizontalLayout.addComponent(button_3);
		horizontalLayout.addComponent(lblResult);
		horizontalLayout.addStyleName("genspaceback");
		button_1.addListener(this);
		button_2.addListener(this);
		button_3.addListener(this);
		
		GridLayout msgPanel = new GridLayout(1, 7);

		Label msgText = new Label("Not a registered user yet? ");
		Label msgText1 = new Label(
				"Register to take advantage of genSpace security features.");
		Label msgText2 = new Label("As a registered user you will be able to:");
		Label msgText3 = new Label("1. Set your data visbility preferences.");
		Label msgText4 = new Label("2. Post comments and rate workflows and tools.");
		Label msgText5 = new Label(
				"You can also choose to continue using genSpace without a login in which case");
		Label msgText6 = new Label("default security preferences will be applied.");

		msgPanel.addComponent(msgText, 0, 0);
		msgPanel.addComponent(msgText1, 0, 1);
		msgPanel.addComponent(msgText2, 0, 2);
		msgPanel.addComponent(msgText3, 0, 3);
		msgPanel.addComponent(msgText4, 0, 4);
		msgPanel.addComponent(msgText5, 0, 5);
		msgPanel.addComponent(msgText6, 0, 6);
		
		GridLayout gridLayout = new GridLayout(1, 2);
		gridLayout.setHeight("300px");
		gridLayout.addComponent(horizontalLayout, 0, 0);
		gridLayout.addComponent(msgPanel, 0, 1);
		
		CssLayout cssLayout = new CssLayout();
		cssLayout.addComponent(gridLayout);
		
		borderLayout.addComponent(cssLayout, BorderLayout.Constraint.CENTER);
		
		return mainLayout;
	}
	
	public void login(ClickEvent event) {
		String username = (String) txtUsername.getValue();
		String password = (String) txtPassword.getValue();
		Window mainWindow = getApplication().getMainWindow();
		if (!genSpaceServerFactory.userLogin(username, password)) {
			Notification msg = new Notification("User Log in failed.",
					Notification.TYPE_ERROR_MESSAGE);
			mainWindow.showNotification(msg);
			return;
		}
		Notification notification = new Notification("User Logged in",
				Notification.TYPE_TRAY_NOTIFICATION);
		mainWindow.showNotification(notification);
		
		//System.out.println("Chat and activity feed test in the beginning");
		chatHandler = new ChatReceiver(this);
		chatHandler.login(username, password);
		
		getApplication().getMainWindow().addWindow(chatHandler.rf);
		chatHandler.rf.setPositionX(getApplication().getMainWindow().getBrowserWindowWidth()/2 + 100);
		// addLisener twice: one for ChatStatusChangeEventListener, FriendStatusChangeListener
		GenSpaceWindow.getGenSpaceBlackboard().addListener(chatHandler.rf);
		GenSpaceWindow.getGenSpaceBlackboard().addListener(chatHandler.rf);

		this.createAFWindow();
		
		genSpaceParent.fireLoggedIn();
	}
	
	public void createAFWindow() {
		if (afWindow == null) {
			afWindow = new ActivityFeedWindow(this);
			getApplication().getMainWindow().addWindow(afWindow);
			afWindow.setPositionX(chatHandler.rf.getPositionX());
			// addLisener twice: one for ChatStatusChangeEventListener, FriendStatusChangeListener
			GenSpaceWindow.getGenSpaceBlackboard().addListener(afWindow);
			GenSpaceWindow.getGenSpaceBlackboard().addListener(afWindow);
		}
	}
	
	public ActivityFeedWindow getAFWindow() {
		return this.afWindow;
	}

	public void clear(ClickEvent event) {
		txtUsername.setValue("");
		txtPassword.setValue("");
		lblResult.setVisible(false);
	}

	public void register(ClickEvent event) {
		callRegisterMember();

		if (genSpaceServerFactory.getUser() != null) {
			lblResult.setValue("Login Created");
		} else {
			lblResult.setValue("Login Creation Failed");
		}
		lblResult.setVisible(true);		
	}
	
	private void callRegisterMember() {
		GenSpaceRegistration panel = new GenSpaceRegistration(this);
		mainLayout.removeAllComponents();
		mainLayout.addComponent(panel);
		mainLayout.setExpandRatio(panel, 1.0f);
	}
	
	public void addMahoutPanel() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == button_1) {
			login(event);
		}
		else if (event.getButton() == button_2) {
			clear(event);
		}
		else if (event.getButton() == button_3) {
			button_3.setEnabled(false);
			register(event);
			button_3.setEnabled(true);
		}
	}
	
	public GenSpaceComponent getGenSpaceParent() {
		return this.genSpaceParent;
	}
	
	public void setFBManager(FBManager fbManager) {
		this.fbManager = fbManager;
	}
	
	public FBManager getFBManager() {
		return this.fbManager;
	}

}
