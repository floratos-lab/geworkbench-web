package org.geworkbenchweb.genspace.rating;

import java.text.DecimalFormat;

import org.geworkbenchweb.genspace.RuntimeEnvironmentSettings;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.geworkbenchweb.genspace.wrapper.ToolWrapper;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;
import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.ToolRating;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.server.stubs.WorkflowRating;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.event.MouseEvents;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

public class StarRatingPanel extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7212044466813119614L;
	public static final int SMALL = 1;
	public static final int MEDIUM = 2;
	public static final int LARGE = 3;

	private boolean clickable = true;
	private Star[] stars;
	private double value = 0;
	private Workflow workflow;
	private Tool tool;
	private Label title;
	private Label ratingInfo;
	private HorizontalLayout hLayout;
	private ICEPush pusher = new ICEPush();

	private Panel starPanel = new Panel();
	
	private GenSpaceLogin_1 login;


	public StarRatingPanel(GenSpaceLogin_1 login2) {
		this("", null, login2);
	}

	public StarRatingPanel(String titleText, Tool tool, final GenSpaceLogin_1 login2) {
		this.login = login2;

		// basic setup
		this.tool = tool;

		// add title
		title = new Label(titleText);
		this.addComponent(title);
		
		hLayout = new HorizontalLayout();
		this.addComponent(hLayout);
		
		// add stars
		stars = new Star[5];
		for (int i = 0; i < 5; i++)
			stars[i] = new Star(this, i + 1);
		
		hLayout.addComponent(starPanel);
		
		HorizontalLayout starLayout = new HorizontalLayout();
		starPanel.addComponent(starLayout);
		starLayout.addListener(new LayoutClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				// TODO Auto-generated method stub
				if (event.getButton() == MouseEvents.ClickEvent.BUTTON_RIGHT) {
					if (!clickable)
						return;
					
					int starIndex = ((Star)event.getClickedComponent()).getStarValue() - 1;

					for (int i = 0; i < 5; i++) {
						if (i <= starIndex)
							stars[i].setStar(Star.FULL);
						else
							stars[i].setStar(Star.EMPTY);
					}
					login2.getPusher().push();
				} else if (event.getButton() == MouseEvents.ClickEvent.BUTTON_LEFT && event.isDoubleClick()) {
					
					if (clickable) {
						int index = ((Star)event.getClickedComponent()).getStarValue();
						
						if(workflow != null) {
							rateWorkflow(index);
						} else { 
							rateTool(index);
						}
						setStarValue(value);
					} else {
						return ;
					}
				}
			}
		});
		starLayout.setWidth("180px");
		
		
		for (int i = 0; i < 5; i++) {
			starLayout.addComponent(stars[i]);
		}
		
		// add rating info
		ratingInfo = new Label();
	
	}
	
	public void attachPusher() {
		this.addComponent(this.pusher);
	}

	public void setTitle(String t) {
		this.title.setCaption(t);
	}
	
	public void loadRating(Workflow wf) {
		this.workflow = wf;
		// see if we can even execute the query
		if (workflow == null || workflow.getId() < 1) {
			this.setVisible(false);
			return;
		} else
			this.setVisible(true);
		
		RateWorker rw = new RateWorker(workflow.getId(), false);
		this.updateUI();
	}
	
	public void loadRating(final Tool tn) {

		this.tool = tn;
		if (this.tool == null || tool.getId() < 2) {
			this.setVisible(false);
			return ;
		} else
			this.setVisible(true);

		RateWorker rw = new RateWorker(tool.getId(), true);
		this.updateUI();
	}

	public void setRatingValue(double rating, long totalRatings) {
		if (totalRatings != 0) {
			setStarValue(rating);
			DecimalFormat twoDigit = new DecimalFormat("#,##0.00");

			ratingInfo.setCaption("(" + twoDigit.format(rating) + " by " 
					+ totalRatings + " users.)");
		} else {
			setStarValue(0);
			ratingInfo.setCaption("Not yet rated.");
		}
		hLayout.addComponent(ratingInfo);
		ratingInfo.setVisible(true);
		this.updateUI();
	}
	
	private void updateUI() {
		if (this.getApplication() != null)
			this.pusher.push();
	}

	public void setStarValue(double value) {
		this.value = value;

		for (int i = 1; i <= 5; i++) {
			if (value >= i)
				stars[i - 1].setStar(Star.FULL);
			else if (value > i - 1)
				stars[i - 1].setStar(Star.HALF);
			else
				stars[i - 1].setStar(Star.EMPTY);
		}
		this.updateUI();
	}

	public Panel getThisPanel() {
		return this;
	}
	
	public void rateWorkflow(final int rating) {
		WFRater wfRater = new WFRater(rating);
		this.setClickable(false);
	}
	public void rateTool(final int rating) {
		ToolRater tRater = new ToolRater(rating);
		this.setClickable(false);
	}

	public boolean isClickable() {
		return clickable;
	}

	public void setClickable(boolean c) {
		clickable = c;
	}
	
	private class RateWorker implements Runnable {
		
		private int id;
		
		private Thread realWorker;
		
		private boolean isTool = true;
		
		public RateWorker(int id, boolean isTool) {
			this.id = id;
			this.isTool = isTool;
			realWorker = new Thread(this);
			realWorker.start();
		}
		
		public void run() {
			if (this.isTool) {
				this.loadTool();
			} else {
				this.loadWorkflow();
			}
		}
		
		private void loadWorkflow() {
			WorkflowRating rating = login.getGenSpaceServerFactory().getPrivUsageFacade().getMyWorkflowRating(this.id);
			setClickable(true);
			Workflow rateWorkflow = login.getGenSpaceServerFactory().getPrivUsageFacade().getWorkflow(this.id);
			if(rateWorkflow != null)
			{
				WorkflowWrapper rat = new WorkflowWrapper(rateWorkflow);
				setRatingValue(rat.getOverallRating(), rat.getNumRating());
			}
		}
		
		private void loadTool() {
			ToolRating rating = login.getGenSpaceServerFactory().getPrivUsageFacade().getMyToolRating(this.id);
			setClickable(true);
			
			Tool rateTool= login.getGenSpaceServerFactory().getPrivUsageFacade().getTool(this.id);
			if (rateTool != null) {
				ToolWrapper rat = new ToolWrapper(rateTool);
				setRatingValue(rat.getOverallRating(), rat.getNumRating());
			}
			
		}
	}
	
	private class WFRater implements Runnable {
		
		private int rating;
		private Thread realWorker;
		
		public WFRater(int rating) {
			this.rating = rating;
			this.realWorker = new Thread(this);
			this.realWorker.start();
		}
		
		public void run() {
			Workflow result = login.getGenSpaceServerFactory().getPrivUsageFacade().saveWorkflowRating(workflow.getId(), this.rating);
			
			if (result == null) {
				getApplication().getMainWindow().showNotification("Fail to set work flow rating");
				return ;
			}
			
			workflow.setSumRating(result.getSumRating());
			workflow.setNumRating(result.getNumRating());
			setStarValue(rating);
			WorkflowWrapper wrap = new WorkflowWrapper(result);
			setRatingValue(wrap.getOverallRating(), wrap.getNumRating());

			setTitle("Thanks!");
			getThisPanel().requestRepaint();
		}
	}
	
	private class ToolRater implements Runnable {
		private int rating;
		private Thread realWorker;
		
		public ToolRater(int rating) {
			this.rating = rating;
			this.realWorker = new Thread(this);
			this.realWorker.start();
		}
		
		public void run() {
			Tool result = login.getGenSpaceServerFactory().getPrivUsageFacade().saveToolRating(tool.getId(), this.rating);
			
			if (result == null) {
				getApplication().getMainWindow().showNotification("Fail to set tool rating");
				return ;
			}

			RuntimeEnvironmentSettings.tools.put(result.getId(), result);
			setStarValue(rating);
			ToolWrapper wrap = new ToolWrapper(result);
			setRatingValue(wrap.getOverallRating(), wrap.getNumRating());

			setTitle("Thanks!");
			getThisPanel().requestRepaint();
		}
	}
}
