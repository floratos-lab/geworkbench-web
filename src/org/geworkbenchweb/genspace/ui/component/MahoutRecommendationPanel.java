package org.geworkbenchweb.genspace.ui.component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.TasteUser;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;
import org.vaadin.addon.borderlayout.BorderLayout;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;

public class MahoutRecommendationPanel extends Panel {
	
	/**
	 * 
	 */
	private final long serialVersionUID = -9025891419883690754L;
	private HorizontalLayout mahoutSuggestionsPanel;
	private Panel workflowsPanel;
	private Panel peoplePanel;
	private TextArea workflowSuggestionsArea;
	private TextArea peopleSuggestionsArea;
	private Panel networkFilteringPanel;
	private GenSpaceLogin login;
	//private JCheckBox networkFilterCheckBox;
	
	public MahoutRecommendationPanel(GenSpaceLogin login) {
		this.login = login;
		initComponents();
	}
		
	private void initComponents() {
		
		mahoutSuggestionsPanel = new HorizontalLayout();
		this.addComponent(mahoutSuggestionsPanel);
		
		workflowsPanel = new Panel();
		peoplePanel = new Panel();
		
		mahoutSuggestionsPanel.addComponent(workflowsPanel);
		mahoutSuggestionsPanel.addComponent(peoplePanel);
		
		final Label wfLabel = new Label("Your Recommended Workflows");
        /*wfLabel.setFont(new Font(wfLabel.getFont().getName(), Font.BOLD, 14));
        wfLabel.setText("Your Recommended Workflows");
        wfLabel.setHorizontalAlignment(JLabel.CENTER);*/
        
        final Label ppLabel = new Label("People Like You");
        
		workflowSuggestionsArea = new TextArea();
		peopleSuggestionsArea = new TextArea();
		
		peopleSuggestionsArea.setValue("No similar user");
		workflowSuggestionsArea.setValue("No recommendation");
		
		workflowSuggestionsArea.setReadOnly(true);
		peopleSuggestionsArea.setReadOnly(true);
		
		workflowsPanel.addComponent(wfLabel);
		workflowsPanel.addComponent(workflowSuggestionsArea);
		workflowsPanel.setWidth("300px");
		
		Label emptyLabel = new Label();
		emptyLabel.setWidth("20px");
		mahoutSuggestionsPanel.addComponent(emptyLabel);
		
		peoplePanel.addComponent(ppLabel);
		peoplePanel.addComponent(peopleSuggestionsArea);
		peoplePanel.setWidth("300px");
		
		this.setScrollable(true);
		workflowsPanel.setScrollable(true);
		peoplePanel.setScrollable(true);
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
	
	public void displayRecommedations() {
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
			
			if (login.getGenSpaceServerFactory().getUser() != null) {
				if (!peopleInNetwork.isEmpty() && peopleInNetwork != null) {
					peopleSuggestionsArea.setValue(peopleInNetwork);
				} 
				
				if (!workflowsWithinNetworkString.isEmpty() && workflowsWithinNetworkString != null) {
					workflowSuggestionsArea.setValue(workflowsWithinNetworkString);
				}
			}
			
			/*if (!networkFilterCheckBox.isSelected()) {
			
				workflowSuggestionsArea.setText("<html><body><ol>"+wfs+"</ol></body></html>");
				
				peopleSuggestionsArea.setText("<html><body><ol>"+people+"</ol></body></html>");
			
			} else {  
			
				if (GenSpaceServerFactory.getUser() != null) {
	
					peopleSuggestionsArea.setText("<html><body><ol>"+peopleInNetwork+"</ol></body></html>");
			
					workflowSuggestionsArea.setText("<html><body><ol>"+workflowsWithinNetworkString+"</ol></body></html>");
				}
			}*/
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
}
