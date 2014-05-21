/*
 * ChatWindow.java
 *
 * Created on Jan, 2013
 */

package org.geworkbenchweb.genspace.ui.chat;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;

import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.geworkbenchweb.genspace.ui.component.WorkflowVisualizationPanel;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.vaadin.addon.borderlayout.BorderLayout;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ChatWindow extends Window implements Action.Handler{
	
	enum messageTypes {
		WORKFLOW, SCREEN_REQUEST, SCREEN_HANDSHAKE, CHAT, SCREEN_TX_END, SCREEN_RX_END
	};
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6960828216997367807L;
	private Chat chat;
	private String chatText = "";

	private enum lastChatter {
		YOU, ME, NONE
	};

	private lastChatter last = lastChatter.NONE;
	private GenSpaceLogin_1 login;
	private ICEPush pusher = new ICEPush();


	public static boolean sharingScreen = false;

	/** Creates new form ChatWindow */
	public ChatWindow(GenSpaceLogin_1 login2) {
		this.setWidth("400px");
		this.setHeight("350px");
		initComponents();
		this.login = login2;
	}

	/**
	 * Set the chat that this window is in reference to
	 * 
	 * @param c
	 */
	public void setChat(Chat c) {
		this.chat = c;
		this.setCaption("Chat with " + c.getParticipant().replace("@genspace", "").replaceAll("([0-9a-zA-Z.]*)(/)([0-9a-zA-Z.]*)", "$1"));
	}
	
	public Chat getChat() {
		return this.chat;
	}


	/**
	 * Handle a standard "text message"
	 * 
	 * @param m
	 */
	private void processTextMessage(Message m) {
		if (!last.equals(lastChatter.YOU)) {
			chatText += "<br><font color=\"green\">"
					+ chat.getParticipant().replace("@genspace", "").replaceAll("([0-9a-zA-Z.]*)(/)([0-9a-zA-Z.]*)", "$1")
					+ "      "
					+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
					+ ":"
					+ Calendar.getInstance().get(Calendar.MINUTE)
					+ "</font>";
		}
		last = lastChatter.YOU;
		chatText += "<br>" + m.getBody();
		this.txtMsging.setValue("<html><body>" + chatText + "</body></html>");
		this.requestRepaintAll();
	}

	/**
	 * Triage incoming messages 
	 * 
	 * @param m
	 *            Message
	 */
	public void processMessage(Message m) {
		if (m.getBody() != null) {
			// If this window has been hidden, unhide it
			if (!this.isVisible()) {
				this.setVisible(true);
				this.focus();
			}
			
			if(m.getProperty("specialType") == null) {
				processTextMessage(m);
			} else if (m.getProperty("specialType").equals(messageTypes.CHAT)) {
				processTextMessage(m);
			} else if (m.getProperty("specialType").equals(
					messageTypes.WORKFLOW)) {
				processWorkflowVisualizationMessage(m);
			} else if (m.getProperty("specialType").equals(
					messageTypes.SCREEN_REQUEST)) {
				processScreenRequestMessage(m);
			} else if (m.getProperty("specialType").equals(
					messageTypes.SCREEN_HANDSHAKE)) {
				processScreenHandshakeMessage(m);
			} else if (m.getProperty("specialType").equals(
					messageTypes.SCREEN_TX_END)) {
				sharingScreen = false;
			} else if (m.getProperty("specialType").equals(
					messageTypes.SCREEN_RX_END)) {
			} else {
			}
		}
	}

	/**
	 * Handle a screen handshake message
	 * 
	 * @param m
	 */
	private void processScreenHandshakeMessage(Message m) {
		InetAddress ip = (InetAddress) m.getProperty("IP");
		Integer port = (Integer) m.getProperty("port");
		startSendingScreen(ip, port);
	}

	/**
	 * Start receiving screens based upon this message
	 * No screen sharing in web version temporarily.
	 * Keep the logic here
	 * @param m
	 */
	private void processScreenRequestMessage(Message m) {
	}

	/**
	 * Process an incoming workflow visualization
	 * 
	 * @param m
	 */
	private void processWorkflowVisualizationMessage(Message m) {
		Window workflowWindow = new Window();
		WorkflowVisualizationPanel panel = new WorkflowVisualizationPanel();
		panel.setGenSpaceLogin(this.login);
		workflowWindow.setWidth("600");
		workflowWindow.setHeight("100");
		workflowWindow.setScrollable(true);
		
		workflowWindow.addComponent(panel);
		ArrayList<WorkflowWrapper> fakeList = new ArrayList<WorkflowWrapper>();
		fakeList.add(new WorkflowWrapper((Workflow) m.getProperty("workflow")));
		workflowWindow.setCaption("Workflow from " + m.getFrom().replace("/Smack", ""));
		panel.render(fakeList);
		
		getApplication().getMainWindow().addWindow(workflowWindow);
	}
	
	private void handleEnter() {
		try {
			Message m = new Message();
			m.setBody(ogm.getValue().toString());
						
			m.setProperty("specialType", messageTypes.CHAT);
			chat.sendMessage(m);
		} catch (XMPPException e) {
			login.getGenSpaceServerFactory().logger.warn("Error", e);
		}
		
		if (!last.equals(lastChatter.ME)) {
			chatText += "<br><font color=\"green\">You	"
					+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
					+ ":"
					+ Calendar.getInstance().get(Calendar.MINUTE)
					+ "</font>";
		}
		
		last = lastChatter.ME;		
		chatText += "<br>" + ogm.getValue();
		txtMsging.setValue("<html><body>" + chatText + "</body></html>");
		ogm.setValue("");
		this.addComponent(this.pusher);
		this.pusher.push();

	}

	/**
	 * End the chat, but never fully dispose of it
	 * 
	 * @param evt
	 */
	private void mnuEndChatActionPerformed() {

		this.getParent().removeWindow(this);
	}

	/**
	 * Create a timer to automatically send the screen
	 * 
	 * @param ip
	 *            Destination address
	 * @param port
	 *            Destination port
	 */
	private void startSendingScreen(InetAddress ip, Integer port) {
	}

	/**
	 * Send the current workflow to this chat partner
	 * 
	 * @param evt
	 */
	private void mnuSendWorkflowActionPerformed() {
		if(login.currentWorkflow == null)
		{
			return;
		}
		Message m = new Message(chat.getParticipant());
		m.setProperty("specialType", messageTypes.WORKFLOW);

		m.setBody("Workflow attached");
		m.setProperty("workflow", login.currentWorkflow.getDelegate());
		try {
			chat.sendMessage(m);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {		
		blLayout = new BorderLayout();
		vMainLayout = new VerticalLayout();
		blLayout.addComponent(vMainLayout, BorderLayout.Constraint.CENTER);
		
		this.setCaption("Chat with XXX");
		this.addComponent(blLayout);
		
		vMenuBar = new MenuBar();
		vMenuBar.setWidth("100%");
		vMenuBar.setHeight("30px");
		
		vMenuItem = vMenuBar.addItem("File", null, null);
		
		Command workFlowCommand = new Command(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void menuSelected(MenuItem selectedItem) {
				//TODO: Implement body for sendWorkFlow
				mnuSendWorkflowActionPerformed();
			}
		};
		
		vMenuItem.addItem("Send Current Workflow", null, workFlowCommand);
		
		Command chatCommand = new Command() {
			private static final long serialVersionUID = 1L;
			public void menuSelected(MenuItem selectedItem) {
				/*TODO: Implement body of endChat
				 */
				mnuEndChatActionPerformed();
			}
		};
		
		vMenuItem.addItem("End chat", null, chatCommand);
		
		vMainLayout.addComponent(vMenuBar);
		
		txtMsging = new Label();
		txtMsging.setContentMode(Label.CONTENT_XHTML);
		txtMsging.setWidth("100%");
		txtMsging.setImmediate(true);
		
		txtPanel = new Panel();
		txtPanel.setWidth("100%");
		txtPanel.setHeight("200px");
		txtPanel.setScrollable(true);
		txtPanel.addComponent(txtMsging);
		vMainLayout.addComponent(txtPanel);
		
		Label emptyLabel = new Label();
		emptyLabel.setHeight("20px");
		vMainLayout.addComponent(emptyLabel);
		
		ogm = new TextField();
		ogm.setWidth("100%");
		ogm.setHeight("30px");

		vMainLayout.addComponent(ogm);
		
		//Add handler for shortcut
		this.addActionHandler(this);
	}

	// Variables declaration - do not modify
	private BorderLayout blLayout;
	
	private VerticalLayout vMainLayout;
	
	private MenuItem vMenuItem;
	private MenuBar vMenuBar;
	private Panel txtPanel;
	private TextField ogm;
	private Label txtMsging;
	private static final Action wEsc = new ShortcutAction("Shortcut: ESC", ShortcutAction.KeyCode.ESCAPE, null);
	
	private static final Action enterAction = new ShortcutAction("Enter", ShortcutAction.KeyCode.ENTER, null);
	
	private static final Action[] actions = new Action[]{wEsc, enterAction};

	@Override
	public Action[] getActions(Object target, Object sender) {
		// TODO Auto-generated method stub
		return actions;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		// TODO Auto-generated method stub
		if (action == wEsc) {
			this.getParent().removeWindow(this);
		}
		
		if (action == enterAction) {
			this.handleEnter();
		}
	}
}
