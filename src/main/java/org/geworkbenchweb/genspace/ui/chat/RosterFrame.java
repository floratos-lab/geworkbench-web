/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * RosterFrame.java
 *
 * Created on Jul 11, 2009, 2:23:51 PM
 */

package org.geworkbenchweb.genspace.ui.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.geworkbenchweb.events.ChatStatusChangeEvent;
import org.geworkbenchweb.events.ChatStatusChangeEvent.ChatStatusChangeEventListener;
import org.geworkbenchweb.events.FriendStatusChangeEvent;
import org.geworkbenchweb.events.FriendStatusChangeEvent.FriendStatusChangeListener;
import org.geworkbenchweb.genspace.chat.ChatReceiver;
import org.geworkbenchweb.genspace.ui.GenSpaceWindow;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.vaadin.addon.borderlayout.BorderLayout;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.MenuBar.MenuItem;

/**
 * A RosterFrame displays roster (buddy list) information
 * 
 * @author jsb2125
 */
public class RosterFrame extends Panel implements RosterListener, ChatStatusChangeEventListener, FriendStatusChangeListener {
	public Set<String> removedCache = new HashSet<String>();
	
	private static String[] statuses = { "Available", "Away", "Offline" };

	private GenSpaceLogin_1 login;
	
	private int myID;
	
	private ChatReceiver cr;
	
	private String username;
	
	private String rosterCaption = "Communicator";
	
	private String online = "img/online.png";
	
	private String offline = "img/offline.png";
	
	private String leave = "img/leave.png";
	
	private ThemeResource onlineIcon = new ThemeResource(online);
	
	private ThemeResource offlineIcon = new ThemeResource(offline);
	
	private ThemeResource leaveIcon = new ThemeResource(leave);
	
	private Embedded onlineEmbed = new Embedded(null, onlineIcon);
	
	private Embedded offlineEmbed = new Embedded(null, offlineIcon);
	
	private Embedded leaveEmbed = new Embedded(null, leaveIcon);
	
	private HorizontalLayout statusLayout;
	
	private HorizontalLayout iconLayout;
	
	private ICEPush pusher = new ICEPush();
	
	public void refresh()
	{
		if (this.cr.getConnection().isConnected()) {
			this.rosterTree.removeAllItems();
			
			this.cr.getConnection().getRoster().reload();
			//this.roster = this.cr.getConnection().getRoster();
			this.roster.reload();
			this.setUpRosterTree();
			//this.addComponent(pusher);
			//System.out.println("refreshed!");
		} 
	}
	
	public void cleanSettings() {
		this.removeAllComponents();
		//getApplication().getMainWindow().removeWindow(this);
	}
	
	private static final long serialVersionUID = 7609367478611608296L;
	
	private class RosterModel extends Tree{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Roster roster;
		private ArrayList<RosterGroup> rootGroups;
		private HashMap<RosterGroup, ArrayList<RosterEntry>> children;
		public RosterModel() {
			clear();
		}
	
		public void clear() {
			roster = null;
			rootGroups = null;
			children = null;
		}

		public void setData(Roster roster) {
			this.roster = roster;
			rootGroups = new ArrayList<RosterGroup>();
			children = new HashMap<RosterGroup, ArrayList<RosterEntry>>();

			for(RosterGroup g: roster.getGroups())
			{
				rootGroups.add(g);
				children.put(g, new ArrayList<RosterEntry>());
				for(RosterEntry e: g.getEntries())
				{
					if(! e.getUser().equalsIgnoreCase(RosterFrame.this.login.getGenSpaceServerFactory().getUser().getUsername() + "@genspace") && !(removedCache.contains(e.getUser()) && g.getName().equals("Friends")))
						children.get(g).add(e);
				}
				if(children.get(g).size() == 0)
				{
					children.remove(g);
					rootGroups.remove(g);
				}
				if(g != null && children != null && children.get(g) != null)
					Collections.sort(children.get(g),new Comparator<RosterEntry>() {
	
						@Override
						public int compare(RosterEntry o1, RosterEntry o2) {
							return o1.getName().compareTo(o2.getName());
						}
						
					});
			}
			
			Collections.sort(rootGroups, new Comparator<RosterGroup>() {

				@Override
				public int compare(RosterGroup l, RosterGroup r) {
					if(l.getName().equals("Friends"))
						return -1;
					else
						return l.getName().compareTo(r.getName());
				}
				
		
			});
		}
		
		public ArrayList<RosterGroup> getRosterGroup() {
			return this.rootGroups;
		}
		
		public Object getChild(Object parent, int index) {
			if(parent instanceof RosterGroup)
			{
				RosterGroup g = (RosterGroup) parent;
				return children.get(g).get(index);
			}
			else if(parent instanceof Roster)
			{
				return rootGroups.get(index);
			}
			return roster;
		}

		public int getChildCount(Object parent) {

			if(parent instanceof RosterGroup)
			{
				RosterGroup g = (RosterGroup) parent;
				if(children == null || children.get(g) == null)
					return 0;
				return children.get(g).size();	
			}
			else if(parent instanceof Roster)
			{
				return rootGroups.size();
			}
			return 0;
		}

		public int getIndexOfChild(Object parent, Object child) {
			if(parent instanceof RosterGroup)
			{
				RosterGroup g = (RosterGroup) parent;
				return children.get(g).indexOf(child);
			}
			else if(parent instanceof Roster)
			{
				return rootGroups.indexOf(child);
			}
			return 0;
		}

		public Object getRoot() {
			return roster;
		}

		public boolean isLeaf(Object node) {
			return (node instanceof RosterEntry);
		}
	}

	@Override
	public void entriesAdded(Collection<String> r) {
		this.setRoster(cr.getConnection().getRoster());
		rosterTree.requestRepaint();
	}

	@Override
	public void entriesDeleted(Collection<String> r) {
		this.setRoster(cr.getConnection().getRoster());
		rosterTree.requestRepaint();
	}

	@Override
	public void entriesUpdated(Collection<String> r) {
		this.setRoster(cr.getConnection().getRoster());
		rosterTree.requestRepaint();
	}

	@Override
	public void presenceChanged(Presence p) {
		if (p.getType().equals(Presence.Type.unavailable)) {
			return ;
		}
		this.setRoster(cr.getConnection().getRoster());
		rosterTree.requestRepaint();
	}
	Roster roster;
	/**
	 * Update the roster
	 * 
	 * @param newr
	 *            new Roster
	 */
	public void setRoster(Roster newr) {
		roster = newr;
		roster.addRosterListener(this);
		this.setUpRosterTree();
	}
	
	private void setUpRosterTree() {
		RosterModel model = new RosterModel();
		model.setData(roster);
		Iterator<RosterGroup> rgIT = model.getRosterGroup().iterator();
		RosterGroup tmpRG;
		
		HierarchicalContainer hBeans = new HierarchicalContainer();
		hBeans.addContainerProperty("name", String.class, "");
		hBeans.addContainerProperty("rGroup", RosterGroup.class, null);
		hBeans.addContainerProperty("rEntry", RosterEntry.class, null);
		hBeans.addContainerProperty("group", Boolean.class, true);
		hBeans.addContainerProperty("icon", Resource.class, null);

		while(rgIT.hasNext()) {
			tmpRG = rgIT.next();
			this.setHierarchicalContainer(hBeans, tmpRG);
		}
		//rosterTree.setContainerDataSource(rgBeans);
		rosterTree.setContainerDataSource(hBeans);
		rosterTree.setItemCaptionPropertyId("name");
		rosterTree.setItemIconPropertyId("icon");
		rosterTree.setItemCaptionMode(Tree.ITEM_CAPTION_MODE_PROPERTY);
		
		Iterator idIT = rosterTree.getItemIds().iterator();
		Object itemID;
		while(idIT.hasNext()) {
			itemID = idIT.next();
			
			if(rosterTree.getItem(itemID).getItemProperty("group").getValue().toString().equals("true")) {
				rosterTree.expandItemsRecursively(itemID);
			}
		}
	}
	
	private void setHierarchicalContainer(HierarchicalContainer hBeans, RosterGroup rg)
	{
		Object groupID = hBeans.addItem();
		hBeans.getContainerProperty(groupID, "name").setValue(rg.getName());
		hBeans.getContainerProperty(groupID, "group").setValue(true);
		hBeans.getContainerProperty(groupID, "rGroup").setValue(rg);
		hBeans.getContainerProperty(groupID, "rEntry").setValue(null);
		
		Collection<RosterEntry> entryList;
		Iterator<RosterEntry> elList;
		RosterEntry tmpEntry;
		
		if(rg.getEntryCount() > 0) {
			entryList = rg.getEntries();
			elList = entryList.iterator();
			Object entryID;
			
			while(elList.hasNext()) {
				tmpEntry = elList.next();
				entryID = hBeans.addItem();
				//System.out.println("check roster frame: "+tmpEntry.getUser());
				hBeans.getContainerProperty(entryID, "name").setValue(tmpEntry.getUser().replace("@genspace", ""));
				hBeans.getContainerProperty(entryID, "group").setValue(false);
				hBeans.getContainerProperty(entryID, "rGroup").setValue(null);
				hBeans.getContainerProperty(entryID, "rEntry").setValue(tmpEntry);
				Presence p = this.roster.getPresence(tmpEntry.getUser());
				if (p.getType().equals(Presence.Type.unavailable))
					hBeans.getContainerProperty(entryID, "icon").setValue(this.offlineIcon);
				else {
					if (p.getMode() != null
							&& (p.getMode().equals(Mode.away) || p.getMode()
									.equals(Mode.dnd))) {
						hBeans.getContainerProperty(entryID, "icon").setValue(this.leaveIcon);
					} else {
						hBeans.getContainerProperty(entryID, "icon").setValue(this.onlineIcon);
					}
					
				}
				//hBeans.getContainerProperty(entryID, "icon").setValue(new ThemeResource(this.online));

				hBeans.setParent(entryID, groupID);
				hBeans.setChildrenAllowed(entryID, false);
			}
		}
	}

	/*public void setAvailable()
	{
		Presence pr = new Presence(Presence.Type.available);
		pr.setStatus("On genSpace...");
		cr.getConnection().sendPacket(pr);
		cmbStatus.setValue("Available");
		bringToFront();
	}*/
	/** Creates new form RosterFrame */
	public RosterFrame(final GenSpaceLogin_1 login2, final ChatReceiver cr) {
		//setWidth("300px");
		setHeight("340px");

		this.login = login2;
		this.myID = this.login.getGenSpaceServerFactory().getUser().getId();
		this.cr = cr;
		this.username = this.login.getGenSpaceServerFactory().getUsername();
		//System.out.println("Check username in Roster: "+this.username);
		setCaption(this.rosterCaption);
		initComponents();
		/*addListener(new Window.CloseListener() {
			private static final long serialVersionUID = 1L;
			
			public void windowClose(CloseEvent e) {
				Presence pr = new Presence(Presence.Type.unavailable);
				pr.setStatus("genSpace hidden");
				cr.getConnection().sendPacket(pr);
				
				//System.out.println("DBUG: Close RosterFrame");
			};
		});*/
	}

	/**
	 * Change the current presence
	 * 
	 * @param e
	 */
	private void cmbStatusActionPerformed(ValueChangeEvent e) {
		
		if (e.getProperty().getValue() == null) {
			//System.out.println("Check property: " + e.getProperty());
			return ;
		}
		String status = e.getProperty().getValue().toString();
		Presence pr;		
		if (status.equalsIgnoreCase(statuses[0])) {
			pr = new Presence(Presence.Type.available);
			pr.setMode(Presence.Mode.available);
		
			iconLayout.removeAllComponents();
			iconLayout.addComponent(this.onlineEmbed);
			//System.out.println("change to available!");
		} else if (status.equalsIgnoreCase(statuses[1])) {
			pr = new Presence(Presence.Type.available);
			pr.setMode(Presence.Mode.away);
		
			iconLayout.removeAllComponents();
			iconLayout.addComponent(this.leaveEmbed);
			//System.out.println("change to leave!");
		} else {
			pr = new Presence(Presence.Type.unavailable);
			
			iconLayout.removeAllComponents();
			iconLayout.addComponent(this.offlineEmbed);
			//System.out.println("change to unavailable!");
		}
		//iconLayout.addComponent(pusher);
		pr.setStatus(status);
		cr.getConnection().sendPacket(pr);
		//System.out.println("send status change event!");
		GenSpaceWindow.getGenSpaceBlackboard().fire(new ChatStatusChangeEvent(this.username));
		//System.out.println("start refresh!");
		this.refresh();
			
		//System.out.println("DEBUG: " + this.username + " fire a status event");
	};
	
	private VerticalLayout vMainLayout;

	private Panel vScrollPane1;
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {
		this.removeAllComponents();
		
		BorderLayout bLayout = new BorderLayout();
		
		vMainLayout = new VerticalLayout();
		vMainLayout.setSpacing(true);
		
		bLayout.addComponent(vMainLayout, BorderLayout.Constraint.CENTER);
		this.addComponent(bLayout);
		vMainLayout.addComponent(pusher);
		//Panel for RosterGroups
		vScrollPane1 = new Panel();
		vScrollPane1.getContent().setSizeUndefined();
		vScrollPane1.setScrollable(true);
		vScrollPane1.setHeight("200px");
		vMainLayout.addComponent(vScrollPane1);

		rosterTree = new Tree();

		rosterTree.addListener(new ItemClickListener() {
			private static final long serialVersionUID = 1L;
			
			public void itemClick(ItemClickEvent event) {
				Object tempID = event.getItemId();
				Object rEntityObject = rosterTree.getItem(tempID).getItemProperty("rEntry").getValue();
				RosterEntry e;
				
				if (rEntityObject != null) {
					e = (RosterEntry)rEntityObject;
					
					Presence p = roster.getPresence(e.getUser());
					//System.out.println("^^^"+e.getUser()+" "+username);
					String fname = e.getUser().substring(0, e.getUser().indexOf('@'));
					if (p.getType().equals(Presence.Type.unavailable) || fname.equals(username))
						return ;

					//System.out.println("Test tree listener: " + e.getName());
					//System.out.println("Test manager: " + cr.getManager());
					cr.getManager().createChat(e.getUser(), null);
					//System.out.println("create chat finished!");
				}
			}
		});
		vScrollPane1.addComponent(rosterTree);

		lblStatus = new Label();
		
		cmbStatus = new ComboBox();
		for(int i = 0; i < statuses.length; i++) {
			cmbStatus.addItem(statuses[i]);
			if(statuses[i].equals("Available"))
				cmbStatus.setValue(statuses[i]);
		}
		cmbStatus.setImmediate(true);

		cmbStatus.addListener(new ComboBox.ValueChangeListener() {
			public void valueChange(ValueChangeEvent e) {
				//System.out.println("Value change: " + e.toString());
				cmbStatusActionPerformed(e);
			}
		});
		
		statusLayout = new HorizontalLayout();
		statusLayout.setHeight("50px");
		statusLayout.addComponent(cmbStatus);
		
		Label emptyLabel = new Label();
		emptyLabel.setWidth("15px");
		
		statusLayout.addComponent(emptyLabel);
		
		iconLayout = new HorizontalLayout();
	
		iconLayout.addComponent(this.onlineEmbed);
		//iconLayout.addComponent(pusher);
		statusLayout.addComponent(iconLayout);
		vMainLayout.addComponent(statusLayout);

		vMenuBar1 = new MenuBar();
		vMenu1 = vMenuBar1.addItem("File", null, null);
		vMenu2 = vMenuBar1.addItem("Edit", null, null);

		lblStatus.setCaption("YourStatus: ");

	}

	public void attachPusher(ICEPush pusher){
		addComponent(pusher);
	}
	
	private MenuItem vMenu1;
	private MenuItem vMenu2;
	private MenuBar vMenuBar1;
	private Label lblStatus;
	private Tree rosterTree;
	private ComboBox cmbStatus;

	@Override
	public void changeStatus(ChatStatusChangeEvent evt) {
		// TODO Auto-generated method stub
		this.refresh();
		if (getPusher().getApplication() != null ) { 
			getPusher().push();
		}

	}
	
	@Override
	public void changeFriendStatus(FriendStatusChangeEvent evt) {
		//System.out.println("Get event from " + evt.getFriendName());
		if (myID == evt.getMyID() || myID == evt.getFriendID()) {
			this.refresh();
			//this.initComponents();
			if (getPusher().getApplication() != null ) { 
				getPusher().push();
			}
			//this.login.getPusher().push();
		}
	}
	
	
	private ICEPush getPusher() {		
		if (this.pusher == null) {
			this.pusher = new ICEPush();
			this.vMainLayout.addComponent(this.pusher);
		} else if (this.pusher.getApplication() == null) {
			this.pusher = new ICEPush();
			this.vMainLayout.addComponent(this.pusher);
		}
		
		return this.pusher;
	}
}
