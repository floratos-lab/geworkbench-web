package org.geworkbenchweb.genspace.ui.component;


import org.geworkbenchweb.genspace.FBManager;
import org.geworkbenchweb.genspace.GenSpaceServerFactory;
import org.geworkbenchweb.genspace.GenspaceLogger;
import org.geworkbenchweb.genspace.chat.ChatReceiver;

import org.geworkbenchweb.genspace.ui.GenspaceLayout;

import org.geworkbenchweb.genspace.ui.chat.RosterFrame;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;
import org.geworkbenchweb.layout.UMainLayout;
import org.jivesoftware.smack.packet.Presence;

import org.vaadin.addon.borderlayout.BorderLayout;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.artur.icepush.ICEPush;




import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;


import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class GenSpaceLogin_1 extends VerticalLayout implements ClickListener{

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
    private GenspaceLogger genspaceLogger;
	private GenspaceLayout genSpaceParent;	
	private RosterFrame rf = null;
	private Window uiMainWindow= null; 
	
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
		//System.out.println("Check genSpaceServerFactory before return: " + this.genSpaceServerFactory.getUsername());
		return this.genSpaceServerFactory;
	}
	
	//@Override
	public void loggedIn() {
		String user = genSpaceServerFactory.getUsername();
		//System.out.println("User name in GesnSpaceLogin: " + user);
		GenSpaceSecurityPanel p = new GenSpaceSecurityPanel(user, genSpaceParent, this);
		mainLayout.removeAllComponents();
		mainLayout.addComponent(p);
		mainLayout.setExpandRatio(p, 1.0f);
		
		MahoutRecommendationPanel mPanel = new MahoutRecommendationPanel(this);
		mainLayout.addComponent(mPanel);
		mPanel.displayRecommendations();
		
		this.push = this.genSpaceParent.getPusher();
		this.mainLayout.addComponent(this.push);
		
		
		//Remove this code since can NOT fire userlogin event before RosterFrame is initialized 
		//GenSpaceWindow.getGenSpaceBlackboard().fire(new ChatStatusChangeEvent(user));
	}
	
	//@Override
	public void loggedOut() {
		this.setParent(null);
		mainLayout.removeAllComponents();
		mainLayout.addComponent(borderLayout);
		mainLayout.setExpandRatio(borderLayout, 1.0f);
		//getApplication().getMainWindow().removeWindow(this.chatHandler.rf);
		//getApplication().getMainWindow().removeWindow(this.afWindow);
	}
	
	
	

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
	private GenSpaceLogin_1(GenspaceLayout genspaceParent) {
		//super(null);
		this.genSpaceParent = genspaceParent ;
		// No need to build mainlayout
		// buildMainLayout();	
		this.mainLayout = new VerticalLayout();
	
		this.push = this.genSpaceParent.getPusher();
		this.mainLayout.addComponent(push);
		
		
		this.addComponent(this.mainLayout);

//		initListeners();
//		setCompositionRoot(mainLayout);

		// TODO add user code here
	}
	
	public GenSpaceLogin_1() {
		this.genSpaceParent = null;
		this.mainLayout = new VerticalLayout();
		this.addComponent(this.mainLayout);
	}
	
	public void resetParent(GenspaceLayout genSpaceParent) {
		this.genSpaceParent = genSpaceParent;
		this.push = this.genSpaceParent.getPusher();
		this.mainLayout.addComponent(push);
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
		HorizontalLayout horizontalLayout_2 = new HorizontalLayout();
		horizontalLayout.setWidth("450px");
		horizontalLayout.addComponent(label_1);		
		horizontalLayout.addComponent(txtUsername);
		horizontalLayout.addComponent(label_2);
		horizontalLayout.addComponent(txtPassword);
		horizontalLayout_2.setSpacing(true);
		horizontalLayout_2.addComponent(button_1);
		horizontalLayout_2.addComponent(button_2);
		horizontalLayout_2.addComponent(button_3);
		horizontalLayout_2.addComponent(lblResult);
		horizontalLayout.addStyleName("genspaceback");
		horizontalLayout_2.addStyleName("");
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

		GridLayout gridLayout = new GridLayout(1, 3);
		gridLayout.setHeight("300px");
		gridLayout.addComponent(horizontalLayout, 0, 0);
		gridLayout.addComponent(horizontalLayout_2, 0 ,1);
		gridLayout.addComponent(msgPanel, 0, 2);

		CssLayout cssLayout = new CssLayout();
		cssLayout.addComponent(gridLayout);

		borderLayout.addComponent(cssLayout, BorderLayout.Constraint.CENTER);

		return mainLayout;
	}
		
	public VerticalLayout authorizeLayout() {
		
		mainLayout.removeAllComponents();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);
		mainLayout.addStyleName("genspaceback");
		
		borderLayout = new BorderLayout();
		mainLayout.addComponent(borderLayout);
		mainLayout.setExpandRatio(borderLayout, 1);
		
		
		// label_2
		label_2 = new Label();
		label_2.setImmediate(false);
		label_2.setWidth("-1px");
		label_2.setHeight("-1px");
		label_2.setValue("Your Password for genSpace");
//		mainLayout.addComponent(label_2, "top:60.0px;left:20.0px;");
		
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
		button_1.setCaption("Authorize");
		button_1.setImmediate(true);
		button_1.setWidth("-1px");
		button_1.setHeight("-1px");
//		mainLayout.addComponent(button_1, "top:100.0px;left:173.0px;");
		

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		HorizontalLayout horizontalLayout_2 = new HorizontalLayout();
		horizontalLayout.setWidth("450px");
		horizontalLayout.addComponent(label_2);
		horizontalLayout.addComponent(txtPassword);
		// horizontalLayout_2.setSpacing(true);
		horizontalLayout_2.addComponent(button_1);
		horizontalLayout_2.addStyleName("genspaceback");
		horizontalLayout_2.addStyleName("");
			
		HorizontalLayout horizontalLayout_info = new HorizontalLayout();
		Label label_info = new Label("System detects password conflict between geWorkbench and genSpace."
								+"Please enter your password in genSpace:");
		horizontalLayout_info.setWidth("450px");
		horizontalLayout_info.addComponent(label_info);	
		
		button_1.addListener(this);


		GridLayout gridLayout = new GridLayout(1, 3);
		gridLayout.setHeight("300px");
		gridLayout.addComponent(horizontalLayout_info, 0, 0);
		gridLayout.addComponent(horizontalLayout, 0, 1);
		gridLayout.addComponent(horizontalLayout_2, 0 ,2);

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

		chatHandler = new ChatReceiver(this);
		chatHandler.login(username, password);
		
		//getApplication().getMainWindow().addWindow(chatHandler.rf);
		//chatHandler.rf.setPositionX(getApplication().getMainWindow().getBrowserWindowWidth()/2 + 100);
		// addLisener twice: one for ChatStatusChangeEventListener, FriendStatusChangeListener
		//GenSpaceWindow.getGenSpaceBlackboard().addListener(chatHandler.rf);
		//GenSpaceWindow.getGenSpaceBlackboard().addListener(chatHandler.rf);

		//this.createAFWindow();
		
		//this.chatHandler.rf.focus();
		//this.afWindow.focus();
		
		genSpaceParent.fireLoggedIn();
	}
	
	
	public boolean autoLogin(String username, String password, boolean shouldFire) {


		if (!genSpaceServerFactory.userLogin(username, password)) {
			return false;
		}
			
		chatHandler = new ChatReceiver(this);
		if (!chatHandler.login(username, password)) {
			return false;
		}
		
		if (shouldFire) {
			genSpaceParent.fireLoggedIn();
		}
		else {
			Presence pr = new Presence(Presence.Type.available);
			pr.setStatus("On genspace...");
			chatHandler.getConnection().sendPacket(pr);
		}
		return true;
	}
	
	
	public void changePassword(ClickEvent event) {
		
		String usernameStr = SessionHandler.get().getUsername();
		String passwordStr = ((String) txtPassword.getValue());

		if(!genSpaceServerFactory.userLogin(usernameStr, passwordStr)) {	
			Window mainWindow = getApplication().getMainWindow();
			Notification msg = new Notification("Password wrong.",
					Notification.TYPE_ERROR_MESSAGE);
			mainWindow.showNotification(msg);
			return;
		}
		
		int j = 0;
		UMainLayout uMainLayout = (UMainLayout)getApplication().getMainWindow().getContent();
		String newPassword = uMainLayout.getMainToolBar().getPassword();
		while (true) {

			genSpaceServerFactory.userLogin(usernameStr, passwordStr);
			genSpaceServerFactory.getWrappedUser().setPasswordClearText(newPassword);	
			genSpaceServerFactory.userUpdate();
			
			j++;
			if (genSpaceServerFactory.userLogin(usernameStr, newPassword) || j==5) {
				break;
			}
		}
		
		if (j == 5) {
			Window mainWindow = getApplication().getMainWindow();
			Notification msg = new Notification("Fail to update password. Please try it later.",
					Notification.TYPE_ERROR_MESSAGE);
			mainWindow.showNotification(msg);
		}
		else {
			autoLogin(usernameStr, newPassword, true);
		}
		
	}
	
	/*public void createAFWindow() {
		if (afWindow != null)
			afWindow = null;
		
		
		afWindow = new ActivityFeedWindow(this);
		getApplication().getMainWindow().addWindow(afWindow);
		afWindow.setPositionX(chatHandler.rf.getPositionX());
		// addLisener twice: one for ChatStatusChangeEventListener, FriendStatusChangeListener
		GenSpaceWindow.getGenSpaceBlackboard().addListener(afWindow);
		GenSpaceWindow.getGenSpaceBlackboard().addListener(afWindow);
	}*/
	
	/*public void createAFWindow() {
		if (afWindow != null)
			afWindow = null;
		
		
		afWindow = new ActivityFeedWindow(this);
		getApplication().getMainWindow().addWindow(afWindow);
		afWindow.setPositionX(chatHandler.rf.getPositionX());
		// addLisener twice: one for ChatStatusChangeEventListener, FriendStatusChangeListener
		GenSpaceWindow.getGenSpaceBlackboard().addListener(afWindow);
		GenSpaceWindow.getGenSpaceBlackboard().addListener(afWindow);
	}*/
	


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
			changePassword(event);
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
	
	public boolean isLogin() {
		return (this != null) 
				&& (this.getGenSpaceServerFactory() != null) 
				&& (this.genSpaceServerFactory.getUsername() != null);
	}
	
	public GenspaceLayout getGenSpaceParent() {
		return this.genSpaceParent;
	}
	
	public void setFBManager(FBManager fbManager) {
		this.fbManager = fbManager;
	}
	
	public FBManager getFBManager() {
		return this.fbManager;
	}
	
	public RosterFrame getRf() {
		return rf;
	}

	public void setRf(RosterFrame rf) {
		this.rf = rf;
	}
	
	public Window getUIMainWindow() {
		return this.uiMainWindow;
	}
	
	public void setUIMainWindow(Window uiMainWindow) {
		this.uiMainWindow = uiMainWindow;
	}
	
	public GenspaceLogger getGenSpaceLogger() {
		return this.genspaceLogger;
	}
	
	public void setGenSpaceLogger(GenspaceLogger genspaceLogger) {
		this.genspaceLogger = genspaceLogger;
	}

}
