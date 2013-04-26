/*
 * ChatWindow.java
 *
 * Created on Jan, 2013
 */

package org.geworkbenchweb.genspace.ui.chat;


import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.ui.chat.ChatWindow.messageTypes;
import org.geworkbench.engine.config.GUIFramework;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin;
import org.geworkbenchweb.genspace.ui.component.WorkflowVisualizationPanel;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.vaadin.addon.borderlayout.BorderLayout;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;

/**
 * 
 * @author mikefhsu
 */
public class ChatWindow extends Window implements Action.Handler{
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
	//private ScreenSharingReceiver screenReceiver;
	private HashMap<Integer, Integer> tileHashes = new HashMap<Integer, Integer>();
	private final static int TILE_SIZE = 32;
	private GenSpaceLogin login;

	public static boolean sharingScreen = false;

	/** Creates new form ChatWindow */
	public ChatWindow(GenSpaceLogin login) {
		/*this.setWidth("15%");
		this.setHeight("35%");*/
		this.setWidth("400px");
		this.setHeight("350px");
		initComponents();
		this.login = login;
		/*ogm.requestFocusInWindow();
		this.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				ogm.requestFocusInWindow();
			}

			@Override
			public void windowLostFocus(WindowEvent e) {

			};
		});*/
	}

	/**
	 * Set the chat that this window is in reference to
	 * 
	 * @param c
	 */
	public void setChat(Chat c) {
		this.chat = c;
		//this.setTitle("Chat with " + c.getParticipant().replace("@genspace", "").replace("/Smack",""));
		//this.setCaption("Chat with " + c.getParticipant().replace("@genspace", "").replace("/Smack",""));
		this.setCaption("Chat with " + c.getParticipant().replace("@genspace", "").replaceAll("([0-9a-zA-Z.]*)(/)([0-9a-zA-Z.]*)", "$1"));
	}
	
	public Chat getChat() {
		return this.chat;
	}

	//private ScreenShareListener screenListener;

	/**
	 * Handle a standard "text message"
	 * 
	 * @param m
	 */
	private void processTextMessage(Message m) {
		System.out.println("In processTextMessage: " + m.getBody());
		System.out.println("Last chat: " + last);
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
		System.out.println("Test chatTest: " + chatText);
		System.out.println("Test label: " + txtMsging.getValue());
		this.txtMsging.setValue("<html><body>" + chatText + "</body></html>");
//		this.txtMsging.requestRepaint();
		this.requestRepaintAll();
		System.out.println("Test label after: " + txtMsging.getValue());
		
		//txtMsging.setCaretPosition(txtMsging.getDocument().getLength());
		//txtMsging.setCursorPosition(txtMsging.getValue().toString().length());
	}

	/**
	 * Triage incoming messages
	 * 
	 * @param m
	 *            Message
	 */
	public void processMessage(Message m) {
		System.out.println("In ChatWindow.processMessage");
		System.out.println("In ChatWinow get message body: " + m.getBody());
		if (m.getBody() != null) {
			// If this window has been hidden, unhide it
			if (!this.isVisible()) {
				this.setVisible(true);
				this.focus();
			}
			
			System.out.println("Test processMessage in ChatWindow");
			
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
				// Stop sending screens
				//screenPublisher.stop();
				sharingScreen = false;
				/*mnuShareScreen.setText("Share Screen");
				screenSendTimer.stop();*/
			} else if (m.getProperty("specialType").equals(
					messageTypes.SCREEN_RX_END)) {
				// Stop showing
				/*sharingScreen = false;
				screenListener.stop();
				screenShareFrame.setVisible(false);*/
			} else {
				// This is an unknown screen type.
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

	//static JFrame screenShareFrame;

	/**
	 * Start receiving screens based upon this message
	 * 
	 * @param m
	 */
	private void processScreenRequestMessage(Message m) {
		/*if (screenReceiver == null) {
			screenShareFrame = new JFrame();
			screenShareFrame.setSize(600, 600);
			screenReceiver = new ScreenSharingReceiver();
			screenShareFrame.add(screenReceiver);
			screenShareFrame.setVisible(true);
			screenShareFrame
					.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			screenListener = new ScreenShareListener(screenReceiver);

			screenShareFrame.addWindowListener(new WindowListener() {

				@Override
				public void windowActivated(WindowEvent e) {
					
				}

				@Override
				public void windowClosed(WindowEvent e) {
					screenShareFrame.setVisible(false);
					screenShareFrame.dispose();
					screenListener.stop();
					Message m = new Message();
					m.setProperty("specialType", messageTypes.SCREEN_TX_END);
					try {
						chat.sendMessage(m);
					} catch (XMPPException e1) {
						GenSpaceServerFactory.logger.warn("Error", e1);
					}


				}

				@Override
				public void windowClosing(WindowEvent e) {

				}

				@Override
				public void windowDeactivated(WindowEvent e) {

				}

				@Override
				public void windowDeiconified(WindowEvent e) {

				}

				@Override
				public void windowIconified(WindowEvent e) {

				}

				@Override
				public void windowOpened(WindowEvent e) {
				}
			});
			Message ret = new Message(m.getFrom());
			ret.setBody("IM LISTENING");
			ret.setProperty("specialType", messageTypes.SCREEN_HANDSHAKE);
			ret.setProperty("IP", screenListener.getLocalAddress());
			ret.setProperty("port", screenListener.getLocalPort());
			try {
				chat.sendMessage(ret);
			} catch (XMPPException e) {
				GenSpaceServerFactory.logger.warn("Error", e);
			}
		}*/
	}

	/**
	 * Process an incoming workflow visualization
	 * 
	 * @param m
	 */
	private void processWorkflowVisualizationMessage(Message m) {
		//JFrame fr = new JFrame();
		/*Window fr = new Window();
		WorkflowVisualizationPanel p = new WorkflowVisualizationPanel();
		fr.setWidth("600");
		fr.setHeight("500");
		
		fr.addComponent(p);
		p.setWidth("600");
		p.setHeight("500");
		fr.setVisible(true);
		
		p.setSize(600, 500);
		fr.setVisible(true);*/

		//p.render(new WorkflowWrapper((Workflow) m.getProperty("workflow")));
		//fr.setTitle("Workflow from " + m.getFrom());
//		p.render(m.getBody(), "Workflow from " + m.getFrom());
		
		//fr.setCaption("Workflow from" + m.getFrom());
		Window workflowWindow = new Window();
		WorkflowVisualizationPanel panel = new WorkflowVisualizationPanel();
		workflowWindow.setWidth("600");
		workflowWindow.setHeight("100");
		workflowWindow.setScrollable(true);
		
		workflowWindow.addComponent(panel);
		ArrayList<WorkflowWrapper> fakeList = new ArrayList<WorkflowWrapper>();
		//System.out.println("WF received: " + new WorkflowWrapper((Workflow) m.getProperty("workflow")).toString());
		fakeList.add(new WorkflowWrapper((Workflow) m.getProperty("workflow")));
		workflowWindow.setCaption("Workflow from " + m.getFrom().replace("/Smack", ""));
		panel.render(fakeList);
		
		getApplication().getMainWindow().addWindow(workflowWindow);
	}

	/**
	 * Send a text message in this chat
	 * 
	 * @param evt
	 */
	private void ogmKeyPressed(java.awt.event.KeyEvent evt) {
		/*if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			// Send the message
			try {
				Message m = new Message();
				m.setBody(ogm.getText());
				m.setProperty("specialType", messageTypes.CHAT);
				chat.sendMessage(m);
			} catch (XMPPException e) {
				GenSpace.logger.warn("Error",e);
			}
			if (!last.equals(lastChatter.ME)) {
				chatText += "<br><font color=\"green\">You      "
						+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
						+ ":"
						+ Calendar.getInstance().get(Calendar.MINUTE)
						+ "</font>";
			}
			last = lastChatter.ME;
			chatText += "<br>" + ogm.getText();
			txtMsging.setText("<html><body>" + chatText + "</body></html>");
			txtMsging.setCaretPosition(txtMsging.getDocument().getLength());
			ogm.setText("");

		}*/
	}
	
	private void handleEnter() {
		try {
			Message m = new Message();
			m.setBody(ogm.getValue().toString());
			System.out.println("ChatWindow ogm.getValue: " + ogm.getValue().toString());
						
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
		
		this.login.getPusher().push();
	}

	/**
	 * End the chat, but never fully dispose of it
	 * 
	 * @param evt
	 */
	private void mnuEndChatActionPerformed() {
		//this.setVisible(false);
		this.getParent().removeWindow(this);
	}

	//private ScreenSharePublisher screenPublisher;
	//Timer screenSendTimer;
	private final int SCREEN_FPS = 300;

	/**
	 * Create a timer to automatically send the screen
	 * 
	 * @param ip
	 *            Destination address
	 * @param port
	 *            Destination port
	 */
	private void startSendingScreen(InetAddress ip, Integer port) {
		/*screenPublisher = new ScreenSharePublisher(ip, port);

		screenSendTimer = new Timer(SCREEN_FPS, new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				sendScreenUpdate();
			};
		});
		screenSendTimer.start();*/
	}

	private boolean amSharing = false;

	/**
	 * Either start sending or stop sending the screen
	 * 
	 * @param evt
	 */
	private void mnuShareScreenActionPerformed(java.awt.event.ActionEvent evt) {
		/*if (amSharing) {
			// Tell the other guy to stop

			screenPublisher.stop();
			Message m = new Message();
			m.setBody("hidden");
			m.setProperty("specialType", messageTypes.SCREEN_RX_END);
			try {
				chat.sendMessage(m);
			} catch (XMPPException e) {
				GenSpace.logger.warn("Error",e);
			}
			screenSendTimer.stop();
			amSharing = false;
		} else if (sharingScreen) {
			JDialog warning = new JDialog(this, "Can't start screen share",
					true);
			warning.setVisible(true);
		} else {
			amSharing = true;
			sharingScreen = true;
			mnuShareScreen.setText("Stop Sharing Screen");
			try {
				Message m = new Message();
				m.setBody("SEND TO ME");
				m.setProperty("specialType", messageTypes.SCREEN_REQUEST);
				chat.sendMessage(m);
			} catch (XMPPException e) {
				GenSpace.logger.warn("Error",e);
			}
		}*/

	}

	/**
	 * Send a single screen update message
	 */
	private void sendScreenUpdate() {
		/*Robot r;
		try {
			r = new Robot();
			BufferedImage n = r.createScreenCapture(GUIFramework.getFrame()
					.getBounds());
			// BufferedImage n = r.createScreenCapture(new
			// Rectangle(0,0,10,10));
			int imgNum;
			HashMap<Integer, byte[]> output = new HashMap<Integer, byte[]>();

			// Find updated tiles
			int numTiles = 0;
			for (int j = 0; j < n.getWidth(null) && numTiles < 100; j += TILE_SIZE) {
				for (int k = 0; k < n.getHeight(null) && numTiles < 100; k += TILE_SIZE) {
					imgNum = n.getWidth(null) * k + j;

					Rectangle this_tile = new Rectangle(j, k,
							(j + TILE_SIZE > n.getWidth(null) ? n
									.getWidth(null) - j : TILE_SIZE),
							(k + TILE_SIZE > n.getHeight(null) ? n
									.getHeight(null) - k : TILE_SIZE));
					WritableRaster tile = (WritableRaster) n.getData(this_tile);

					Raster tile_for_code = n.getData(this_tile);
					int[] ints = null;
					ints = tile_for_code.getPixels(tile_for_code.getMinX(),
							tile_for_code.getMinY(), tile_for_code.getWidth(),
							tile_for_code.getHeight(), ints);
					int code = 0;
					for (int l = 0; l < ints.length; l++)
						code += ints[l] % ints.length;
					if (!tileHashes.containsKey(imgNum)
							|| !tileHashes.get(imgNum).equals(code)) {
						tileHashes.put(imgNum, code);
						WritableRaster child = tile.createWritableChild(
								tile.getMinX(), tile.getMinY(),
								tile.getWidth(), tile.getHeight(), 0, 0, null);

						BufferedImage child_img = new BufferedImage(
								n.getColorModel(), child, false, null);
						ByteArrayOutputStream bs = new ByteArrayOutputStream();
						Jimi.putImage("image/png", child_img, bs);

						numTiles++;
						output.put(imgNum, bs.toByteArray());
					}

				}
			}

			if (!output.isEmpty()) {
				// Assemble the message
				HashMap<String, Object> m = new HashMap<String, Object>();
				m.put("width", n.getWidth());
				m.put("height", n.getHeight());
				m.put("indices", output.keySet().toArray());
				for (Integer tile : output.keySet())
					m.put("img" + tile, output.get(tile));
				screenPublisher.sendMessage(m);
			}

		} catch (AWTException e) {
			//GenSpace.logger.warn("Error",e);
			GenSpaceServerFactory.logger.warn("Error", e);
		} catch (JimiException e) {
			//GenSpace.logger.warn("Error",e);
			GenSpaceServerFactory.logger.warn("Error", e);
		}*/
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

		/*ogm = new javax.swing.JTextField();
		jScrollPane1 = new javax.swing.JScrollPane();
		txtMsging = new javax.swing.JTextPane();
		jMenuBar1 = new javax.swing.JMenuBar();
		jMenu1 = new javax.swing.JMenu();
		mnuSendWorkflow = new javax.swing.JMenuItem();
		mnuShareScreen = new javax.swing.JMenuItem();
		mnuEndChat = new javax.swing.JMenuItem();*/
		
		blLayout = new BorderLayout();
		vMainLayout = new VerticalLayout();
		blLayout.addComponent(vMainLayout, BorderLayout.Constraint.CENTER);
		
		this.setCaption("Chat with XXX");
		this.addComponent(blLayout);
		
		//setTitle("Chat with XXX");

		/*ogm.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent evt) {
				ogmKeyPressed(evt);
			}
		});*/

		//http://docs.oracle.com/javase/6/docs/api/javax/swing/JScrollPane.html
		/*txtMsging.setContentType("text/html");
		txtMsging.setEditable(false);
		jScrollPane1.setViewportView(txtMsging);

		jMenu1.setText("File");*/
		
		/*mnuSendWorkflow.setText("Send Current Workflow");
		mnuSendWorkflow.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				mnuSendWorkflowActionPerformed(evt);
			}
		});
		jMenu1.add(mnuSendWorkflow);*/
		
		/*mnuShareScreen.setText("Share Screen");
		mnuShareScreen.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				mnuShareScreenActionPerformed(evt);
			}
		});*/
//		jMenu1.add(mnuShareScreen);
		
		/*mnuEndChat.setText("End Chat");
		mnuEndChat.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				mnuEndChatActionPerformed(evt);
			}
		});
		mnuEndChat.setMnemonic('W');
		jMenu1.add(mnuEndChat);*/

		//jMenuBar1.add(jMenu1);
		
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
				System.out.println("Test sendWorkFlow");
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
		

		
		//ogm.setWidth("100%");
		//ogm.setHeight("100%");
		//vScrollPanel = new Panel();
		
		txtMsging = new Label();
		txtMsging.setContentMode(Label.CONTENT_XHTML);
		txtMsging.setWidth("100%");
		txtMsging.setHeight("200px");
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
		
		/*txtPanel = new Panel();
		txtPanel.setWidth("300px");
		txtPanel.setHeight("300px");
		txtPanel.addComponent(txtMsging);*/
		
		//Add handler for shortcut
		this.addActionHandler(this);
		
		/*final Refresher refresher = new Refresher();
		refresher.setRefreshInterval(50);
		refresher.addListener(this);
		vMainLayout.addComponent(refresher);*/
		
		/*setJMenuBar(jMenuBar1);

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(ogm, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271,
						Short.MAX_VALUE)
				.add(jScrollPane1,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271,
						Short.MAX_VALUE));
		layout.setVerticalGroup(layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(org.jdesktop.layout.GroupLayout.TRAILING,
						layout.createSequentialGroup()
								.add(jScrollPane1,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										150, Short.MAX_VALUE)
								.addPreferredGap(
										org.jdesktop.layout.LayoutStyle.RELATED)
								.add(ogm,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

		pack();*/
	}// </editor-fold>

	// Variables declaration - do not modify
	private BorderLayout blLayout;
	
	private VerticalLayout vMainLayout;
	
	//private javax.swing.JMenu jMenu1;
	private MenuItem vMenuItem;
	//private javax.swing.JMenuBar jMenuBar1;
	private MenuBar vMenuBar;
	//private javax.swing.JScrollPane jScrollPane1;
	private Panel vScrollPanel;
	//private javax.swing.JMenuItem mnuEndChat;
	private Item mnuEndchat;
	//private javax.swing.JMenuItem mnuSendWorkflow;
	private Item mnuSendWorkflow;
	//private javax.swing.JMenuItem mnuShareScreen;
	private Item mnuShareScrenn;
	//private javax.swing.JTextField ogm;
	private Panel txtPanel;
	private TextField ogm;
	//private javax.swing.JTextPane txtMsging;
	private Label txtMsging;
	// End of variables declaration
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
	

	/*@Override
	public void refresh(Refresher source) {
		// TODO Auto-generated method stub
		//System.out.println("Good news: we are refreshed!!!");
	}*/
}
