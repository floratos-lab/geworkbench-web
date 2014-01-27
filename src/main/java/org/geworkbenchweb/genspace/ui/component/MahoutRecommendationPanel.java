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
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class MahoutRecommendationPanel extends Panel implements ValueChangeListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private HorizontalLayout mahoutSuggestionsPanel;
	private VerticalLayout mainPanel = new VerticalLayout();
	private Panel workflowsPanel;
	private Panel peoplePanel;
	private Label workflowSuggestionsArea;
	private Label peopleSuggestionsArea;
	private GenSpaceLogin_1 login;
	private CheckBox filter;
	//private JCheckBox networkFilterCheckBox;
	
	public MahoutRecommendationPanel(GenSpaceLogin_1 genSpaceLogin_1) {
		this.login = genSpaceLogin_1;
		initComponents();
	}
		
	private void initComponents() {
		
		mahoutSuggestionsPanel = new HorizontalLayout();
		this.setContent(mainPanel);
		
		workflowsPanel = new Panel();
		peoplePanel = new Panel();
		filter = new CheckBox("Filter to My Networks");
		filter.setImmediate(true);
		filter.addValueChangeListener(this);
		mainPanel.addComponent(filter);

		mahoutSuggestionsPanel.addComponent(workflowsPanel);
		mahoutSuggestionsPanel.addComponent(peoplePanel);
		//mahoutSuggestionsPanel.addComponent(filter);
		mainPanel.addComponent(mahoutSuggestionsPanel);

		final Label wfLabel = new Label("Your Recommended Workflows");
        wfLabel.setStyleName(Reindeer.LABEL_H2);
        wfLabel.setContentMode(ContentMode.PREFORMATTED);
        
        final Label ppLabel = new Label("People Like You");
        ppLabel.setStyleName(Reindeer.LABEL_H2);
        ppLabel.setContentMode(ContentMode.PREFORMATTED);
        
		workflowSuggestionsArea = new Label();
		peopleSuggestionsArea = new Label();
		
		workflowSuggestionsArea.setWidth("280px");
		workflowSuggestionsArea.setHeight("150px");
		peopleSuggestionsArea.setWidth("280px");
		peopleSuggestionsArea.setHeight("150px");
		
		peopleSuggestionsArea.setValue("");
		workflowSuggestionsArea.setValue("");

		VerticalLayout wlayout = LayoutUtil.addComponent(wfLabel);
		wlayout.addComponent(workflowSuggestionsArea);
		workflowsPanel.setContent(wlayout);
		workflowsPanel.setWidth("300px");
		//workflowsPanel.setStyleName(Reindeer.PANEL_LIGHT);
		
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
					workflowSuggestionsArea.setValue("");
				}
			
				if (!people.isEmpty() && people != null) {
					peopleSuggestionsArea.setValue(people);
				} else {
					peopleSuggestionsArea.setValue("");
				}
				
			} else {  
			
				if (login.getGenSpaceServerFactory().getUser() != null) {
	
					if (!peopleInNetwork.isEmpty() && peopleInNetwork != null) {
						peopleSuggestionsArea.setValue(peopleInNetwork);
					} else {
						peopleSuggestionsArea.setValue("");
					}
					
					if (!workflowsWithinNetworkString.isEmpty() && workflowsWithinNetworkString != null) {
						workflowSuggestionsArea.setValue(workflowsWithinNetworkString);
					} else {
						workflowSuggestionsArea.setValue("");
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
	public void valueChange(ValueChangeEvent evt) {
		displayRecommendations();
	}
}
