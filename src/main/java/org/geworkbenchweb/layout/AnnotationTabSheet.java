/**
 * 
 */
package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.pojos.Comment;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.ExperimentInfo;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author zji
 * @version $Id$
 *
 */
/* ATTENTION: please be aware of our over-use of the word 'annotation'!
 * 'annotation' here refers to the additional information of the dataset, e.g. user comment, history, experimental info, etc.,
 * NOT the annotation for the microaaray dataset, e.g. the AffyMatrix annotation*/
public class AnnotationTabSheet extends TabSheet {

	private static final long serialVersionUID = -727899117302032942L;

	AnnotationTabSheet(final Long dataSetId) {

		HorizontalSplitPanel dLayout 	=  	new HorizontalSplitPanel();
		dLayout.setSplitPosition(60);
		dLayout.setSizeFull();
		dLayout.setImmediate(true);
		dLayout.setStyleName(Reindeer.SPLITPANEL_SMALL);
		dLayout.setLocked(true);
		
		final VerticalLayout commentsLayout = new VerticalLayout();
		commentsLayout.setImmediate(true);
		commentsLayout.setMargin(true);
		commentsLayout.setSpacing(true);
		commentsLayout.setSizeUndefined();
		
		Label cHeading 		=	new Label("User Comments:");
		cHeading.setStyleName(Reindeer.LABEL_H2);
		cHeading.setContentMode(ContentMode.PREFORMATTED);
		commentsLayout.addComponent(cHeading);
		
		Map<String, Object> params 		= 	new HashMap<String, Object>();
		params.put("parent", dataSetId);

		List<AbstractPojo> comments =  FacadeFactory.getFacade().list("Select p from Comment as p where p.parent =:parent", params);
		if(comments.size() != 0){
			for(int i=0;i<comments.size();i++) {
				java.sql.Date date = ((Comment) comments.get(i)).getDate();
				Label comment = new Label(date.toString()+
						" - " +
						((Comment) comments.get(i)).getComment());
				commentsLayout.addComponent(comment);
			}
		}

		dLayout.setFirstComponent(commentsLayout);
		
		VerticalLayout commentArea = new VerticalLayout();
		commentArea.setImmediate(true);
		commentArea.setMargin(true);
		commentArea.setSpacing(true);
		
		Label commentHead 		=	new Label("Enter new comment here:");
		commentHead.setStyleName(Reindeer.LABEL_H2);
		commentHead.setContentMode(ContentMode.PREFORMATTED);
		final TextArea dataArea = 	new TextArea();
		dataArea.setRows(6);
		dataArea.setWidth("100%");
		Button submitComment	=	new Button("Add Comment", new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(!dataArea.getValue().toString().isEmpty()) {
					java.sql.Date date 	=	new java.sql.Date(System.currentTimeMillis());
					
					Comment c = new Comment();
					c.setParent(dataSetId);
					c.setComment(dataArea.getValue().toString());
					c.setDate(date);
					FacadeFactory.getFacade().store(c);
					
					Label comment = new Label(date.toString()+
							" - " +
							dataArea.getValue().toString());
					commentsLayout.addComponent(comment);
					dataArea.setValue("");
				}
			}
		});
		submitComment.setClickShortcut(KeyCode.ENTER);
		commentArea.addComponent(commentHead);
		commentArea.setComponentAlignment(commentHead, Alignment.MIDDLE_LEFT);
		commentArea.addComponent(dataArea);
		commentArea.setComponentAlignment(dataArea, Alignment.MIDDLE_LEFT);
		commentArea.addComponent(submitComment);
		commentArea.setComponentAlignment(submitComment, Alignment.MIDDLE_LEFT);
		dLayout.setSecondComponent(commentArea);
		
		HorizontalSplitPanel infoSplit 	=  	new HorizontalSplitPanel();
		/*infoSplit.setSplitPosition(50);
		infoSplit.setSizeFull();
		infoSplit.setImmediate(true);
		infoSplit.setStyleName(Reindeer.SPLITPANEL_SMALL);
		infoSplit.setLocked(true);*/
				
		VerticalLayout dataHistory 	= 	new VerticalLayout();
		VerticalLayout expInfo		=	new VerticalLayout();
		
		dataHistory.setSizeUndefined();
		dataHistory.setMargin(true);
		dataHistory.setSpacing(true);
		dataHistory.setImmediate(true);

		Label historyHead 		=	new Label("Data History:");
		historyHead.setStyleName(Reindeer.LABEL_H2);
		historyHead.setContentMode(ContentMode.PREFORMATTED);
		dataHistory.addComponent(historyHead);
		
		Map<String, Object> eParams 		= 	new HashMap<String, Object>();
		eParams.put("parent", dataSetId);

		List<AbstractPojo> histories =  FacadeFactory.getFacade().list("Select p from DataHistory as p where p.parent =:parent", eParams);
		for(int i=0; i<histories.size(); i++) {
			DataHistory dH = (DataHistory) histories.get(i);
			Label d = new Label(dH.getData());
			d.setContentMode(ContentMode.PREFORMATTED);
			dataHistory.addComponent(d);
		}
		
		expInfo.setSizeUndefined();
		expInfo.setMargin(true);
		expInfo.setSpacing(true);
		
		Label infoHead 		=	new Label("Experiment Information:");
		infoHead.setStyleName(Reindeer.LABEL_H2);
		infoHead.setContentMode(ContentMode.PREFORMATTED);
		expInfo.addComponent(infoHead);
		
		Map<String, Object> iParams 		= 	new HashMap<String, Object>();
		iParams.put("parent", dataSetId);

		List<AbstractPojo> info =  FacadeFactory.getFacade().list("Select p from ExperimentInfo as p where p.parent =:parent", iParams);
		for(int i=0; i<info.size(); i++) {
			ExperimentInfo eI = (ExperimentInfo) info.get(i);
			Label d = new Label(eI.getInfo());
			d.setContentMode(ContentMode.PREFORMATTED);
			expInfo.addComponent(d);
		}
	
		infoSplit.setFirstComponent(dataHistory);
		infoSplit.setSecondComponent(expInfo);

		this.setStyleName(Reindeer.TABSHEET_SMALL);
		this.setSizeFull();
		this.setImmediate(true);
	
		Panel p = new Panel();
		p.setContent(infoSplit);
		this.addTab(p, "Data Information");
		//this.addTab(infoSplit, "Data Information");
		this.addTab(dLayout, "User Comments");			
	}
}
