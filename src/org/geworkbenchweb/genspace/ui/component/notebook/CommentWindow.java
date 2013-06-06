package org.geworkbenchweb.genspace.ui.component.notebook;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.XMLGregorianCalendar;

import org.geworkbench.components.genspace.server.stubs.AnalysisComment;
import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin;

public class CommentWindow extends Window{
	
	private final static SimpleDateFormat format = new SimpleDateFormat(
			"M/d/yy h:mm a");
	
	public static Date convertToDate(XMLGregorianCalendar cal) {
		return DatatypeConverter.parseDateTime(cal.toXMLFormat()).getTime();
	}
	
	private List<AnalysisComment> commentList;
	
	private AnalysisEvent e;
	
	private String username;
	
	private String toolname;
	
	private String evtTime;
	
	private String caption;
	
	private VerticalLayout mainLayout = new VerticalLayout();
	
	private Panel textPanel = new Panel();
	
	private Table commentTable = new Table();
	
	private GenSpaceLogin login;
	
	public CommentWindow(AnalysisEvent e, List commentList, GenSpaceLogin login) {
		this.e = e;
		this.username = this.e.getTransaction().getUser().getUsername();
		this.toolname = this.e.getToolname();
		this.evtTime = format.format(convertToDate(this.e.getCreatedAt()));;
		this.commentList = commentList;
		this.login = login;
		
		this.caption = username + "'s " + toolname + " at " + evtTime;
		this.setCaption(caption);
		
		this.setHeight("300px");
		this.setWidth("400px");
		
		this.addComponent(mainLayout);
		
		this.textPanel.setScrollable(true);
		
		this.commentTable.addContainerProperty("Comments", Component.class, null);
		this.commentTable.setSizeFull();
		
		this.mainLayout.addComponent(textPanel);
		this.mainLayout.addComponent(commentTable);
		
		this.makeTextPanel();
		this.makeCommentPanel();
	}
	
	private void makeTextPanel() {
		this.textPanel.removeAllComponents();
		final TextArea ta = new TextArea();
		ta.setSizeFull();
		this.textPanel.addComponent(ta);
		
		Button save = new Button("Save");
		save.addListener(new Button.ClickListener() {
			public void buttonClick(Button.ClickEvent event) {
				login.getGenSpaceServerFactory().getPrivUsageFacade().saveAnalysisEventComment(e.getId(), ta.getValue().toString());
				updateWindow();
				login.getPusher().push();
			}
		});
		this.textPanel.addComponent(save);
	}
	
	private void makeCommentPanel() {
		commentTable.removeAllItems();
		
		AnalysisComment tmp;
		for (int i = 0 ; i < this.commentList.size(); i++) {
			tmp = commentList.get(i);

			Panel commentPanel = new Panel();
			
			Label usrComment = new Label();
			String info = tmp.getUser().getUsername() + ": " + tmp.getComment();
			usrComment.setCaption(info);
			commentPanel.addComponent(usrComment);
			
			Label time = new Label(tmp.getCreatedAt().toString());
			commentPanel.addComponent(time);
			
			this.commentTable.addItem(new Object[] {commentPanel}, i);
		}
	}
	
	private void updateWindow() {
		this.commentList = login.getGenSpaceServerFactory().getPrivUsageFacade().getAnalysisEventComment(e.getId());
		this.makeTextPanel();
		this.makeCommentPanel();
	}
	
}
