package org.geworkbenchweb.genspace.ui.component;

import java.util.Date;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;
import org.geworkbench.components.genspace.server.stubs.AnalysisEventParameter;
import org.geworkbenchweb.genspace.FBManager;
import org.geworkbenchweb.genspace.ui.component.notebook.NotebookPanel;
import org.vaadin.artur.icepush.ICEPush;

import com.restfb.types.Comment;
import com.restfb.types.Post;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class FBCommentWindow extends Window{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static String username = "Username";
	
	private static String comments = "Comment";
	
	private static String comDate = "Comment Date";
	
	private GenSpaceLogin_1 login;
	
	private String title;
	
	private AnalysisEvent e;
	
	private IndexedContainer model = new IndexedContainer();
	
	private Table commentTable = new Table();
	
	private String searchString;
	
	private List<AnalysisEventParameter> aepList;
	
	private Post curpost;
	
	private ThemeResource gefb = new ThemeResource("img/gefbtrans.png");
	
	private Label fbUserNotebook;
	private ICEPush pusher = new ICEPush();
	
	public FBCommentWindow(GenSpaceLogin_1 login2, AnalysisEvent e, Label fbUser) {
		this.login = login2;
		this.e = e;
		this.title = e.getToolname() + ": " + e.getCreatedAt();
		this.fbUserNotebook = fbUser;
		this.setCaption(this.title);
		
		this.setWidth("500px");
		this.setHeight("400px");
		
		this.aepList = this.login.getGenSpaceServerFactory().getPrivUsageFacade().getAnalysisParameters(e.getId());
		this.searchString = FBManager.generateSearchString(this.e.getTool().getName(), this.e.getTransaction().getDataSetName(), aepList, NotebookPanel.convertToDate(e.getCreatedAt()));
		this.curpost = this.login.getFBManager().searchExistingPost(this.searchString);
		
		this.model.addContainerProperty(username, String.class, null);
		this.model.addContainerProperty(comments, String.class, null);
		this.model.addContainerProperty(comDate, Date.class, null);
		
		this.setData();
		this.commentTable.setContainerDataSource(this.model);
		this.commentTable.setSizeFull();
		this.commentTable.setColumnHeaders(new String [] {username, comments, comDate});

		this.makeLayout();
	}
	
	private void setData() {
		if (this.curpost == null)
			return ;
		
		this.model.removeAllItems();
		
		List<Comment> commentList = this.curpost.getComments().getData();
		
		Item item;
		Comment c;
		for (int i = commentList.size() - 1; i >= 0; i--) {
			c = commentList.get(i);
			item = model.addItem(i);
			item.getItemProperty(username).setValue(c.getFrom().getName());
			item.getItemProperty(comments).setValue(c.getMessage());
			item.getItemProperty(comDate).setValue(c.getCreatedTime());
		}
	}
	
	private void update() {
		this.curpost = this.login.getFBManager().searchExistingPost(this.searchString);
		this.setData();
		this.makeLayout();
		this.login.getPusher().push();
	}
	
	private void makeLayout() {
		this.removeAllComponents();
		
		VerticalLayout vLayout = new VerticalLayout();
		this.addComponent(vLayout);
		
		Panel inputPanel = new Panel("New Comments");
		vLayout.addComponent(inputPanel);
		
		VerticalLayout yLayout = new VerticalLayout();
		inputPanel.addComponent(yLayout);
		
		HorizontalLayout hLayout = new HorizontalLayout();
		yLayout.addComponent(hLayout);
		
		final TextArea noteText = new TextArea();
		noteText.setWidth("200px");
		hLayout.addComponent(noteText);
		
		Label emptyLabel = new Label();
		emptyLabel.setWidth("10px");
		hLayout.addComponent(emptyLabel);
		
		Embedded geFB = new Embedded(null, this.gefb);
		hLayout.addComponent(geFB);
		
		Button publish = new Button("Publish");
		publish.addListener(new Button.ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(Button.ClickEvent evt) {
				if (login.getFBManager() != null) {
					//login.getFBManager().publishAnalysisResult(e.getTool().getName(), e.getTransaction().getDataSetName(), aepList, NotebookPanel.convertToDate(e.getCreatedAt()), noteText.getValue().toString());
					login.getFBManager().publishAnalysisResult(searchString, noteText.getValue().toString());
					update();
				}
			}
		});
		yLayout.addComponent(publish);
		
		if (this.curpost == null)
				return ;
		Panel historyPanel = new Panel("History Comments on Facebook");
		vLayout.addComponent(historyPanel);
		historyPanel.addComponent(this.commentTable);
		
	}

}
