package org.geworkbenchweb.genspace.ui.component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;
import org.geworkbench.components.genspace.server.stubs.AnalysisEventParameter;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbenchweb.events.FriendStatusChangeEvent;
import org.geworkbenchweb.events.FriendStatusChangeEvent.FriendStatusChangeListener;
import org.geworkbenchweb.events.LogCompleteEvent;
import org.geworkbenchweb.events.LogCompleteEvent.LogCompleteEventListener;
import org.geworkbenchweb.genspace.GenSpaceServerFactory;
import org.geworkbenchweb.genspace.ui.GenSpaceWindow;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class ActivityFeedWindow extends Panel implements LogCompleteEventListener, FriendStatusChangeListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int evtBefore = -14;
	
	private static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

	private String afCaption = "Activity Feeder";
	
	private VerticalLayout afLayout;
	
	private GenSpaceLogin_1 login;
	
	private List<AnalysisEvent> evtList;
	
	private String queryLimit = "";
	
	private String newPath = "img/new.png";
	
	private String faviPath = "img/favicon.png";
	
	private ThemeResource newIcon = new ThemeResource(newPath);
	
	private ThemeResource faviIcon = new ThemeResource(faviPath);
	private ICEPush pusher = new ICEPush();
	
	private int myID;
	
	public ActivityFeedWindow(GenSpaceLogin_1 genSpaceLogin_1) {
		this.login = genSpaceLogin_1;
		this.myID = this.login.getGenSpaceServerFactory().getUser().getId();
		
		this.setScrollable(true);
		this.setHeight("300px"); 
		
		this.afLayout = new VerticalLayout();
		this.setContent(this.afLayout);
		this.setCaption(this.afCaption);
		
		this.updateQueryString();

		this.evtList = this.login.getGenSpaceServerFactory().getFriendOps().getMyFriendsEvents(this.queryLimit);		

		this.makeAFLayout();
	}
	
	public static XMLGregorianCalendar convertToXMLDate(Date d) {
		GregorianCalendar c= new GregorianCalendar();
		c.setTime(d);
		
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			GenSpaceServerFactory.handleException(e);
		}
		return null;
	}
	
	private void updateQueryString() {
		Date currentDate = new Date();
		Calendar d = Calendar.getInstance();
		d.setTime(currentDate);
		d.add(Calendar.DATE, evtBefore);
		this.queryLimit = sf.format(d.getTime());
	}
	
	private void makeAFLayout() {
		this.afLayout.removeAllComponents();
		this.afLayout.addComponent(pusher);
		Iterator<AnalysisEvent> evtIT = evtList.iterator();
		Label toolName;
		Label datasetName;
		Label date;
		while(evtIT.hasNext()) {
			final AnalysisEvent evt = evtIT.next();
			final Panel evtPanel = new Panel(evt.getTransaction().getUser().getUsername());
			evtPanel.setWidth("250px");
			evtPanel.addListener(new ClickListener() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
					// TODO Auto-generated method stub
					Window paramWindow = new Window(evtPanel.getCaption());
					paramWindow.setWidth("300px");
					paramWindow.setHeight("200px");
					
					Label toolName = new Label(evt.getToolname());
					paramWindow.addComponent(toolName);
					
					Iterator<AnalysisEventParameter> paramIT = evt.getParameters().iterator();
					while(paramIT.hasNext()) {
						AnalysisEventParameter param = paramIT.next();
						Label paramLabel = new Label(param.getParameterKey() + ": " + param.getParameterValue());
						paramWindow.addComponent(paramLabel);
					}
					getApplication().getMainWindow().addWindow(paramWindow);
					paramWindow.setPositionX(/*ActivityFeedWindow.this.getPositionX() +*/ 20);
					paramWindow.setPositionY(/*ActivityFeedWindow.this.getPositionY() +*/ 20);
				}
				
			});
			
			GridLayout tNameLayout = new GridLayout(4, 1);
			String tName = "<b>" + evt.getToolname() + "</b>";
			toolName = new Label(tName, Label.CONTENT_XHTML);
			toolName.setWidth("100px");
			tNameLayout.addComponent(toolName);
			
			Label emptyLabel = new Label();
			emptyLabel.setWidth("20px");
			tNameLayout.addComponent(emptyLabel);
			
			tNameLayout.addComponent(new Embedded(null, this.faviIcon));
			evtPanel.addComponent(tNameLayout);
			
			Date now = new Date();
			XMLGregorianCalendar nowCal = ActivityFeedWindow.convertToXMLDate(now);
			XMLGregorianCalendar evtCal = evt.getCreatedAt();
			
			if (nowCal.getYear() == evtCal.getYear() && nowCal.getMonth() == evtCal.getMonth() && nowCal.getDay() == evtCal.getDay()) {
				tNameLayout.addComponent(new Embedded(null, this.newIcon));
			}
			
			datasetName = new Label(evt.getTransaction().getDataSetName());
			date = new Label(evt.getCreatedAt().toString());
			evtPanel.addComponent(datasetName);
			evtPanel.addComponent(date);
			this.afLayout.addComponent(evtPanel);
			this.afLayout.setComponentAlignment(evtPanel, Alignment.MIDDLE_CENTER);
		}
	}
	
	@Override
	public void completeLog(LogCompleteEvent evt) {
		int id = evt.getID();
		if (this.myID == id) {
			return ;
		}
		
		if (this.isFriend(id)) {
			this.updateQueryString();
			this.evtList = this.login.getGenSpaceServerFactory().getFriendOps().getMyFriendsEvents(this.queryLimit);
			this.makeAFLayout();
			GenSpaceWindow.sPush(this, this.getPusher());
		}
	}
	
	@Override
	public void changeFriendStatus(FriendStatusChangeEvent evt) {
		if (this.myID == evt.getMyID() || this.myID == evt.getFriendID()) {
			this.updateQueryString();
			this.evtList = this.login.getGenSpaceServerFactory().getFriendOps().getMyFriendsEvents(this.queryLimit);
			this.makeAFLayout();
		}
	}
	
	private boolean isFriend(int id) {
		List<User> friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
		
		for (User u: friendList) {
			if (u.getId() == id) {
				return true;
			}
		}
		
		return false;
	}
	
	private ICEPush getPusher() {
		if (this.pusher == null) {
			this.pusher = new ICEPush();
		}
		if (this.pusher.getApplication() == null) {
			this.addComponent(this.pusher);
		}
		return this.pusher;
	}
	
}

