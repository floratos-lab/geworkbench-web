package org.geworkbenchweb.genspace.rating;

import java.text.DecimalFormat;

import org.geworkbenchweb.genspace.RuntimeEnvironmentSettings;
import org.geworkbenchweb.genspace.ui.GenSpaceComponent;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin;
import org.geworkbenchweb.genspace.wrapper.ToolWrapper;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;
import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.ToolRating;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.server.stubs.WorkflowRating;
import org.vaadin.addon.borderlayout.BorderLayout;

import com.vaadin.event.LayoutEvents;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

public class StarRatingPanel extends Panel implements LayoutEvents.LayoutClickListener{

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

	private Panel starPanel = new Panel();
	
	private GenSpaceLogin login;


	public StarRatingPanel(GenSpaceLogin login) {
		this("", null, login);
	}

	public StarRatingPanel(String titleText, Tool tool, final GenSpaceLogin login) {
		this.login = login;

		// basic setup
		//BorderLayout bLayout = new BorderLayout();
		//contentPanel.addComponent(bLayout);
		this.tool = tool;

		// add title
		title = new Label(titleText);
		this.addComponent(title);
		//contentPanel.addComponent(title, BorderLayout.Constraint.NORTH);
		//contentPanel.add(title, BorderLayout.NORTH);
		
		HorizontalLayout hLayout = new HorizontalLayout();
		this.addComponent(hLayout);
		
		// add stars
		stars = new Star[5];
		for (int i = 0; i < 5; i++)
			stars[i] = new Star(this, i + 1);
		
		//contentPanel.add(starPanel, BorderLayout.WEST);
		//bLayout.addComponent(starPanel, BorderLayout.Constraint.WEST);
		hLayout.addComponent(starPanel);
		
		HorizontalLayout starLayout = new HorizontalLayout();
		starPanel.addComponent(starLayout);
		starLayout.addListener(new LayoutClickListener() {
			@Override
			public void layoutClick(LayoutClickEvent event) {
				// TODO Auto-generated method stub
				if (event.getButton() == MouseEvents.ClickEvent.BUTTON_RIGHT) {
					if (!clickable)
						return;
					
					int starIndex = ((Star)event.getClickedComponent()).getStarValue() - 1;
					
					System.out.println("Test evt: " + event.getClickedComponent().getClass().getName());
					System.out.println("Test star value: " + starIndex);
					
					for (int i = 0; i < 5; i++) {
						if (i <= starIndex)
							stars[i].setStar(Star.FULL);
						else
							stars[i].setStar(Star.EMPTY);
					}
					login.getPusher().push();
				} else if (event.getButton() == MouseEvents.ClickEvent.BUTTON_LEFT && event.isDoubleClick()) {
					System.out.println("Test left double click");
	
					
					if (clickable) {
						int index = ((Star)event.getClickedComponent()).getStarValue();
						
						System.out.println("Test evt: " + event.getClickedComponent().getClass().getName());
						System.out.println("Test star value: " + index);
						if(workflow != null) {
							System.out.println("Start to rate workflow");
							rateWorkflow(index);
						} else {
							rateTool(index);
						}
						System.out.println("Test value: " + value);
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
		hLayout.addComponent(ratingInfo);
		//bLayout.addComponent(ratingInfo, BorderLayout.Constraint.EAST);
		//contentPanel.add(ratingInfo, BorderLayout.EAST);
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
			setVisible(true);
		
		RateWorker rw = new RateWorker(workflow.getId());
	}
	
	public void loadRating(final Tool tn) {

		this.tool = tn;
		// see if we can even execute the query
//		if (tn == null || tn.getId() < 1) {
//			System.out.println("Setting not visible");
//			setVisible(false);
//			return;
//		} else
//			setVisible(true);

		RateWorker rw = new RateWorker(tool.getId());
	}

	public void setRatingValue(double rating, long totalRatings) {
		System.out.println("Test totalRatings: " + totalRatings);
		
		if (totalRatings != 0) {
			setStarValue(rating);
			DecimalFormat twoDigit = new DecimalFormat("#,##0.00");

			ratingInfo.setCaption("(" + twoDigit.format(rating) + " by "
					+ totalRatings + " users.)");
		} else {
			setStarValue(0);
			ratingInfo.setCaption("Not yet rated.");
		}
		System.out.println("Test caption: " + ratingInfo.getCaption());
		login.getPusher().push();
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
		login.getPusher().push();
	}

	public Panel getThisPanel() {
		return this;
	}
	
	public void rateWorkflow(final int rating) {
		System.out.println("Before workflow rating thread");
		WFRater wfRater = new WFRater(rating);
	}
	public void rateTool(final int rating) {
		ToolRater tRater = new ToolRater(rating);
	}

	public boolean isClickable() {
		return clickable;
	}

	public void setClickable(boolean c) {
		clickable = c;
	}

	/*@Override
	public void mouseClicked(MouseEvent e) {
		if (clickable)
			if(workflow != null)
				rateWorkflow(((Star) e.getComponent()).getValue());
			else
				rateTool(((Star) e.getComponent()).getValue());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (!clickable)
			return;

		int starIndex = ((Star) e.getComponent()).getValue() - 1;
		for (int i = 0; i < 5; i++) {
			if (i <= starIndex)
				stars[i].setStar(Star.FULL);
			else
				stars[i].setStar(Star.EMPTY);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (!clickable)
			return;
		setStarValue(value);
	}

	// these aren't needed.
	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}*/

	@Override
	public void layoutClick(LayoutClickEvent event) {
		// TODO Auto-generated method stub
		if (event.getButton() == MouseEvents.ClickEvent.BUTTON_LEFT) {
			if (!clickable)
				return;
			
			System.out.println("Test evt: " + event.getClickedComponent().toString());
			
			/*int starIndex = ((Star) event.getComponent()).getValue() - 1;
			for (int i = 0; i < 5; i++) {
				if (i <= starIndex)
					stars[i].setStar(Star.FULL);
				else
					stars[i].setStar(Star.EMPTY);
			}*/
		}
		
	}
	
	private class RateWorker implements Runnable {
		
		private int id;
		
		private Thread realWorker;
		
		public RateWorker(int id) {
			this.id = id;
			realWorker = new Thread(this);
			realWorker.start();
		}
		
		public void run() {
			WorkflowRating rating = login.getGenSpaceServerFactory().getPrivUsageFacade().getMyWorkflowRating(this.id);
			if (rating == null)
				setClickable(true);
			else
				setClickable(true);
			
			Workflow rateWorkflow = login.getGenSpaceServerFactory().getPrivUsageFacade().getWorkflow(this.id);
			if(rateWorkflow != null)
			{
				WorkflowWrapper rat = new WorkflowWrapper(rateWorkflow);
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
			System.out.println("Start running");
			Workflow result = login.getGenSpaceServerFactory().getPrivUsageFacade().saveWorkflowRating(workflow.getId(), rating);
			
			workflow.setSumRating(result.getSumRating());
			workflow.setNumRating(result.getNumRating());
			System.out.println("Test rating in run " + rating);
			setStarValue(rating);
			WorkflowWrapper wrap = new WorkflowWrapper(result);
			setRatingValue(wrap.getOverallRating(),
					wrap.getNumRating());

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
			Tool result = login.getGenSpaceServerFactory().getPrivUsageFacade().saveToolRating(tool.getId(), rating);

			RuntimeEnvironmentSettings.tools.put(result.getId(), result);
			setStarValue(rating);
			ToolWrapper wrap = new ToolWrapper(result);
			setRatingValue(wrap.getOverallRating(),
					wrap.getNumRating());

			setTitle("Thanks!");
			getThisPanel().requestRepaint();
		}
	}
}
