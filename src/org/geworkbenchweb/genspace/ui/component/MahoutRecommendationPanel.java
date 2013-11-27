package org.geworkbenchweb.genspace.ui.component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.TasteUser;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;
import org.geworkbenchweb.utils.LayoutUtil;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class MahoutRecommendationPanel extends Panel implements ValueChangeListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private HorizontalLayout mahoutSuggestionsPanel;
	private Panel workflowsPanel;
	private Panel peoplePanel;
	private Label workflowSuggestionsArea;
	private Label peopleSuggestionsArea;
	private GenSpaceLogin login;
	private CheckBox filter;
	//private JCheckBox networkFilterCheckBox;
	
	public MahoutRecommendationPanel(GenSpaceLogin login) {
		this.login = login;
		initComponents();
	}
		
	private void initComponents() {
		
		mahoutSuggestionsPanel = new HorizontalLayout();
		this.setContent(mahoutSuggestionsPanel);
		
		workflowsPanel = new Panel();
		peoplePanel = new Panel();
		filter = new CheckBox("Filter to My Networks");
		filter.setImmediate(true);
		filter.addValueChangeListener(this);
		
		mahoutSuggestionsPanel.addComponent(workflowsPanel);
		mahoutSuggestionsPanel.addComponent(peoplePanel);
		mahoutSuggestionsPanel.addComponent(filter);
		
		final Label wfLabel = new Label("Your Recommended Workflows");
        
        final Label ppLabel = new Label("People Like You");
        
		workflowSuggestionsArea = new Label();
		peopleSuggestionsArea = new Label();
		
		workflowSuggestionsArea.setWidth("280px");
		workflowSuggestionsArea.setHeight("150px");
		peopleSuggestionsArea.setWidth("280px");
		peopleSuggestionsArea.setHeight("150px");
		
		peopleSuggestionsArea.setValue("No similar user");
		workflowSuggestionsArea.setValue("No recommendation");

		VerticalLayout wlayout = LayoutUtil.addComponent(wfLabel);
		wlayout.addComponent(workflowSuggestionsArea);
		workflowsPanel.setContent(wlayout);
		workflowsPanel.setWidth("300px");
		
		Label emptyLabel = new Label();
		emptyLabel.setWidth("20px");
		mahoutSuggestionsPanel.addComponent(emptyLabel);
		
		VerticalLayout playout = LayoutUtil.addComponent(ppLabel);
		playout.addComponent(peopleSuggestionsArea);
		peoplePanel.setContent(playout);
		peoplePanel.setWidth("300px");
		
	}
	
	/*public void handleLogin() {
		this.add(networkFilteringPanel, BorderLayout.EAST, 1);
		displayRecommedations();
	}
	
	public void handleLogout() {
		networkFilterCheckBox.setSelected(false);
		this.remove(1);
		displayRecommedations();
	}*/
	
	public void displayRecommendations() {
		User user = login.getGenSpaceServerFactory().getUser();
		
		TasteUser tu = null;
		if (user != null) {
			tu = login.getGenSpaceServerFactory().getPublicFacade().getTasteUserByUser(user);
		} else {
			String hostname = "";
			try {
				hostname = InetAddress.getLocalHost().getHostName();
				
				tu = login.getGenSpaceServerFactory().getPublicFacade().getTasteUserByHostname(hostname);
			} catch (UnknownHostException e) {
				//e.printStackTrace();
				System.out.println("Unknown host");
			}
		}
	
		String wfs = "";
		String peopleInNetwork = "";
		String workflowsWithinNetworkString = "";
		String people = "";
		
		if (tu != null) {
			
			List<Workflow> mahoutSuggestions = getRealTimeMahoutToolSuggestion(tu);

			int lim = 10;
			for(Workflow wa : mahoutSuggestions)
			{
				WorkflowWrapper w = new WorkflowWrapper(wa);
				w.loadToolsFromCache();
				wfs = wfs + "<li>" + w.toString() + "</li>";
				lim--;
				if (lim <= 0)
					break;
			}
			//wfs = wfs.substring(0, wfs.length()-5);
		
			List<TasteUser> peopleLikeYou = getRealTimeMahoutUserSuggestion(tu);
		
			for (TasteUser user1 : peopleLikeYou) {
			if (user1.getUser() == null)
				people += "<li> Anonymous " + user1.getId() + "</li>";
			else
				people += "<li>" + user1.getUser().getUsername() + "</li>";
			}
		
			if (login.getGenSpaceServerFactory().getUser() != null) {
				List<TasteUser> peopleLikeYouInNetwork = getRealTimeMahoutUserWithinNetworkSuggestion(tu);
			
				for (TasteUser user1 : peopleLikeYouInNetwork) {
					if (user1.getUser() == null)
						peopleInNetwork += "<li> Anonymous " + user1.getId() + "</li>";
					else
						peopleInNetwork += "<li>" + user1.getUser().getUsername() + "</li>";
				}
			
				lim = 10;
				List<Workflow> workflowsWithinNetwork = getRealTimeMahoutNetworkWorkflowSuggestion(tu);
				for(Workflow wa : workflowsWithinNetwork)
				{
					WorkflowWrapper w = new WorkflowWrapper(wa);
					w.loadToolsFromCache();
					workflowsWithinNetworkString += "<li>" + w.toString() + "</li>";
					lim--;
					if (lim <= 0)
						break;
				}
			}
						
			if (!filter.getValue()) {
				
				if (!wfs.isEmpty() && wfs != null) {
					workflowSuggestionsArea.setValue(wfs);
				} else {
					workflowSuggestionsArea.setValue("No recommendation");
				}
			
				if (!people.isEmpty() && people != null) {
					peopleSuggestionsArea.setValue(people);
				} else {
					peopleSuggestionsArea.setValue("No similar user");
				}
				
			} else {  
			
				if (login.getGenSpaceServerFactory().getUser() != null) {
	
					if (!peopleInNetwork.isEmpty() && peopleInNetwork != null) {
						peopleSuggestionsArea.setValue(peopleInNetwork);
					} else {
						peopleSuggestionsArea.setValue("No similar user");
					}
					
					if (!workflowsWithinNetworkString.isEmpty() && workflowsWithinNetworkString != null) {
						workflowSuggestionsArea.setValue(workflowsWithinNetworkString);
					} else {
						workflowSuggestionsArea.setValue("No recommendation");
					}
				}
			}
		}
	}
	
	private List<org.geworkbench.components.genspace.server.stubs.Workflow> getRealTimeMahoutToolSuggestion(TasteUser tu) {
		
		try {
			return (login.getGenSpaceServerFactory().getUsageOps().getMahoutToolSuggestion(tu.getId(), 0));
			
		} catch (Exception e) {
			return null;
		}
	}
	
	private List<org.geworkbench.components.genspace.server.stubs.TasteUser> getRealTimeMahoutUserSuggestion(TasteUser tu) {
		
		try {
			return (login.getGenSpaceServerFactory().getUsageOps().getMahoutUserSuggestion(tu.getId(), 0));
		} catch (Exception e) {
			return null;
		}
	}
	
	private List<org.geworkbench.components.genspace.server.stubs.TasteUser> 
		getRealTimeMahoutUserWithinNetworkSuggestion(TasteUser tu) {
		
		try {
			return (login.getGenSpaceServerFactory().getUsageOps().getMahoutUserSuggestion(tu.getId(), 1));
		} catch (Exception e) {
			return null;
		}
	}
	
	private List<org.geworkbench.components.genspace.server.stubs.Workflow> 
		getRealTimeMahoutNetworkWorkflowSuggestion(TasteUser tu) {
		
		try {
			return (login.getGenSpaceServerFactory().getUsageOps().getMahoutUserWorkflowsSuggestion(tu.getId(), 1));
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		displayRecommendations();
	}
}
