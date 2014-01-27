package org.geworkbenchweb.plugins.anova;

 
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.geworkbench.components.anova.FalseDiscoveryRateControl;
import org.geworkbench.components.anova.PValueEstimation;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.AnovaResult;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.MarkerArraySelector;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * Builds the Parameter Form for Anova Analysis
 * 
 * @author Min You
 */
public class AnovaUI extends VerticalLayout implements AnalysisUI {

	private static final long serialVersionUID = -738580934848570913L;
 
	private MarkerArraySelector markerArraySelector;	 
	private Label pValEstLabel;
	private Label pValEstCbxLabel;
	private ComboBox pValEstCbx;
	private Label permNumberLabel;
	private TextField permNumber;
	private Label pValThresholdLabel;
	private TextField pValThreshold;

	private Label pValCorrectionLabel;
	
	private Long dataSetId;

	private OptionGroup og;
	
	private TextField falseSignificantGenesLimit;

	private Button submitButton;
	
	private int totalSelectedArrayNum= 0;
	
	private Long userId  = null;
	
	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();

	public AnovaUI() {
		this(0L);
	}
	
	public AnovaUI(Long dataSetId) {
		User user = SessionHandler.get();
		if(user!=null)
			userId  = user.getId();

		this.dataSetId = dataSetId;
	 
		final GridLayout gridLayout1 = new GridLayout(4, 3);
		final GridLayout gridLayout2 = new GridLayout(3, 1);

	 
		gridLayout1.setSpacing(true);
		gridLayout2.setSpacing(true);
		 
		gridLayout1.setImmediate(true);
		gridLayout2.setImmediate(true);

		setSpacing(true);
		setImmediate(true);
		
		markerArraySelector = new MarkerArraySelector(dataSetId, userId, "AnovaUI");

	  

		pValEstLabel = new Label(
				"P-Value Estimation----------------------------------------------------------------------");
		pValEstLabel.setStyleName(Reindeer.LABEL_SMALL);

		pValEstCbxLabel = new Label("P-Value based on");
		pValEstCbx = new ComboBox();
		pValEstCbx.setImmediate(true);
		pValEstCbx.setNullSelectionAllowed(false);
		pValEstCbx.addItem(PValueEstimation.fdistribution.ordinal());
		pValEstCbx.addItem(PValueEstimation.permutation.ordinal());
		pValEstCbx.setItemCaption(PValueEstimation.fdistribution.ordinal(), "F-distribution");
		pValEstCbx.setItemCaption(PValueEstimation.permutation.ordinal(), "Permutation");
		pValEstCbx.select(PValueEstimation.fdistribution.ordinal());

		pValEstCbx.addValueChangeListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				  if (pValEstCbx.getValue().equals(PValueEstimation.fdistribution.ordinal()))
				  {
					  permNumberLabel.setEnabled(false);
					  permNumber.setEnabled(false);
					  og.setItemEnabled(FalseDiscoveryRateControl.westfallyoung.ordinal(), false);					 
					  og.setItemEnabled(FalseDiscoveryRateControl.number.ordinal(), false);				 
					  og.setItemEnabled(FalseDiscoveryRateControl.proportion.ordinal(), false);
					  if (og.getValue().equals(FalseDiscoveryRateControl.westfallyoung.ordinal()) || og.getValue().equals(FalseDiscoveryRateControl.number.ordinal()) || og.getValue().equals(FalseDiscoveryRateControl.proportion.ordinal()))
					  {
						  og.select(FalseDiscoveryRateControl.alpha.ordinal());
					  }
					  
					  gridLayout2.setVisible(false);
				  }
		          else
		          {
		        	  permNumberLabel.setEnabled(true);
		        	  permNumber.setEnabled(true);
					  og.setItemEnabled(FalseDiscoveryRateControl.westfallyoung.ordinal(), true);					 
					  og.setItemEnabled(FalseDiscoveryRateControl.number.ordinal(), true);				 
					  og.setItemEnabled(FalseDiscoveryRateControl.proportion.ordinal(), true);
					 
					  if (og.getValue().equals(FalseDiscoveryRateControl.number.ordinal()) || og.getValue().equals(FalseDiscoveryRateControl.proportion.ordinal()))
					  {
						  gridLayout2.setVisible(true);
					  }
					 
		          }

			}
		}); 
		
		permNumberLabel = new Label("Permutations #");
		permNumberLabel.setEnabled(false);
		permNumber = new TextField();
		permNumber.setValue("100");
		permNumber.setNullSettingAllowed(false);
		permNumber.setEnabled(false);
		permNumber.setRequired(true);
		permNumber.addValidator(new IntegerValidator("Not an integer"));
		
		pValThresholdLabel = new Label("P-Value Threshold");
		pValThreshold = new TextField();
		pValThreshold.setValue("0.05");
		permNumber.setRequired(true);
		permNumber.addValidator(new DoubleValidator("Not a double"));

		pValCorrectionLabel = new Label(
				"P-value Corrections And False Discovery Control---------------------------");
		pValCorrectionLabel.setStyleName(Reindeer.LABEL_SMALL);

		og = new OptionGroup();
        og.setImmediate(true);
		og.addItem(FalseDiscoveryRateControl.alpha.ordinal());
		og.addItem(FalseDiscoveryRateControl.bonferroni.ordinal());
		og.addItem(FalseDiscoveryRateControl.adjbonferroni.ordinal());
		og.addItem(FalseDiscoveryRateControl.westfallyoung.ordinal());
		og.addItem(FalseDiscoveryRateControl.number.ordinal());
		og.addItem(FalseDiscoveryRateControl.proportion.ordinal());
		
		og.setItemCaption(FalseDiscoveryRateControl.alpha.ordinal(), "Just alpha (no correction)");
		og.setItemCaption(FalseDiscoveryRateControl.bonferroni.ordinal(), "Standard Bonferroni");
		og.setItemCaption(FalseDiscoveryRateControl.adjbonferroni.ordinal(), "Adjusted Bonferroni");
		og.setItemCaption(FalseDiscoveryRateControl.westfallyoung.ordinal(), "Westfall-Young step down");
		og.setItemEnabled(FalseDiscoveryRateControl.westfallyoung.ordinal(), false);
		og.setItemCaption(FalseDiscoveryRateControl.number.ordinal(), "The number of false significant genes should not exceed:");
		og.setItemEnabled(FalseDiscoveryRateControl.number.ordinal(), false);
		og.setItemCaption(FalseDiscoveryRateControl.proportion.ordinal(), "The proportion of false significant genes should not exceed:");
		og.setItemEnabled(FalseDiscoveryRateControl.proportion.ordinal(), false);
				 
		og.select(FalseDiscoveryRateControl.alpha.ordinal());
		
		og.addValueChangeListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				 
					  if (og.getValue().equals(FalseDiscoveryRateControl.number.ordinal()) ) 
					  {
						  gridLayout2.setVisible(true);
						  falseSignificantGenesLimit.setValue("10");
						  falseSignificantGenesLimit.removeAllValidators();
						  falseSignificantGenesLimit.addValidator(new IntegerValidator("Not an integer"));
						  
					  }
		              else if ( og.getValue().equals(FalseDiscoveryRateControl.proportion.ordinal()) ) 
		              {
				 
					     gridLayout2.setVisible(true);
					     falseSignificantGenesLimit.setValue("0.05");
					     falseSignificantGenesLimit.removeAllValidators();
						  falseSignificantGenesLimit.addValidator(new DoubleValidator("Not a double"));
		              }
		              else		            	  
		            	  gridLayout2.setVisible(false);
			}
		}); 
		
		
		falseSignificantGenesLimit = new TextField();
		falseSignificantGenesLimit.setValue("0");
		falseSignificantGenesLimit.setImmediate(true);
		Label permutationsOnly = new Label("  (permutations only)");
		permutationsOnly.setStyleName(Reindeer.LABEL_SMALL);
		 
		submitButton = new Button("Submit", new SubmitListener());
 
		addComponent(markerArraySelector);
		addComponent(pValEstLabel);

		gridLayout1.addComponent(pValEstCbxLabel, 0, 0);
		gridLayout1.addComponent(pValEstCbx, 1, 0);
		gridLayout1.addComponent(permNumberLabel, 2, 0);
		gridLayout1.addComponent(permNumber, 3, 0);
		gridLayout1.addComponent(pValThresholdLabel, 0, 1);
		gridLayout1.addComponent(pValThreshold, 1, 1);

		addComponent(gridLayout1);
		addComponent(pValCorrectionLabel);
		addComponent(og);
		
		gridLayout2.addComponent(new Label("             "), 0, 0);
		gridLayout2.addComponent(falseSignificantGenesLimit, 1, 0);
		gridLayout2.addComponent(permutationsOnly, 2, 0);
		gridLayout2.setVisible(false);
		
		
		addComponent(gridLayout2);
		addComponent(new Label("   "));
		addComponent(submitButton);

	}

	
	public Long getDataSetId() {
		return this.dataSetId;
	}
	
	public Long getUserId() {
		return this.userId;
	}
	
	private class SubmitListener implements ClickListener {

		private static final long serialVersionUID = 831124091338570481L;

		@Override
		public void buttonClick(ClickEvent event) {
			 
			if (validInputData()) {
				
				params.put("form", AnovaUI.this);
				ResultSet resultSet = new ResultSet();
				java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
				resultSet.setDateField(date);
				String dataSetName = "Anova - Pending";
				resultSet.setName(dataSetName);
				resultSet.setType(getResultType().getName());
				resultSet.setParent(dataSetId);
				resultSet.setOwner(SessionHandler.get().getId());
				FacadeFactory.getFacade().store(resultSet);

				NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
				GeworkbenchRoot.getBlackboard().fire(resultEvent);

				AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(dataSetId, resultSet, params, AnovaUI.this);
				GeworkbenchRoot.getBlackboard().fire(analysisEvent);
			}
		}
	}
	
	
	 

	public int getPValueEstimation() {		 
		return Integer.parseInt(pValEstCbx.getValue().toString().trim());
	}
	
	public int getPermNumber() {	
		if (permNumber.isEnabled())
		   return Integer.parseInt(permNumber.getValue().toString().trim());
		else
		   return 100;
	}
		
	public double getPValThreshold() {			
		return Double.parseDouble(pValThreshold.getValue().toString().trim());
	}
	
	public int getFalseDiscoveryRateControl() {	
		return Integer.parseInt(og.getValue().toString().trim());		 
	}
	
	public float getFalseSignificantGenesLimit() {	
	    if (!falseSignificantGenesLimit.isVisible())
		   return 0;	
	    return Float.parseFloat(falseSignificantGenesLimit.getValue().toString().trim());
		 
	}
	
	public int getTotalSelectedArrayNum() {	
	   
	    return this.totalSelectedArrayNum;
		 
	}
	
	
	public boolean validInputData()
    {
		 
		String[] selectedArraySet = null;
	 
		/* check for minimum number of activated groups */		 
		selectedArraySet = markerArraySelector.getSelectedArraySet();
		 
		if ( selectedArraySet == null || selectedArraySet.length < 3)
		{	markerArraySelector.getArraySetSelect().setComponentError(
	                new UserError("Minimum of 3 array groups must be activated."));
		    return false;
		}
		 
		List<String> microarrayPosList = new ArrayList<String>();
		/* for each group */
		for (int i = 0; i < selectedArraySet.length; i++) {
			
			 ArrayList<String> arrays = getArrayData(Long
						.parseLong(selectedArraySet[i].trim()));
			
			if (arrays.size() < 2)
			{	markerArraySelector.getArraySetSelect().setComponentError(
		                new UserError("Each microarray group must contains at least 2 arrays."));
			    return false;
			}	 
			 
			for (int j = 0; j < arrays.size(); j++) {
				if (microarrayPosList.contains(arrays.get(j))) {				
					markerArraySelector.getArraySetSelect().setComponentError(
			                new UserError("Same array (" + arrays.get(j)
									+ ") exists in multiple groups."));
				    
					return false;
				}
				microarrayPosList.add(arrays.get(j));
				totalSelectedArrayNum++;
			}

		}
		markerArraySelector.getArraySetSelect().setComponentError(null);
	 
	   if (!permNumber.isValid())
	   {
		   permNumber.setComponentError(	    
            new UserError("Must be an integer"));
		   return false;
	   }
	   else
		 permNumber.setComponentError(null);
	   
	   
	   if (!pValThreshold.isValid() || getPValThreshold() <= 0 || getPValThreshold() >= 1)
	   {
		   pValThreshold.setComponentError(
	          new UserError("P-Value threshold should be a float number between 0.0 and 1.0."));
	       return false;
	   
	   }   
		else
			   pValThreshold.setComponentError(null);
		   
	   if (og.getValue().equals(FalseDiscoveryRateControl.number.ordinal()))
	   {
		   if (!falseSignificantGenesLimit.isValid()) 
		   {
			   falseSignificantGenesLimit.setComponentError(	    
			            new UserError("Must be an integer"));
					   return false;
		   }
		   else
			   falseSignificantGenesLimit.setComponentError(null);
				   
	   } 
		
	   if (og.getValue().equals(FalseDiscoveryRateControl.proportion.ordinal()))
	   {
		   if (!falseSignificantGenesLimit.isValid()) 
		   {
			   falseSignificantGenesLimit.setComponentError(	    
			            new UserError("Proportion should be a float number between 0.0 and 1.0."));
			   
			   return false;
		   }
		   else
			   falseSignificantGenesLimit.setComponentError(null);
				   
	   } 
		
	    submitButton.setComponentError(null);
		return true;
	}
	
	

	public  String[] getSelectedMarkerSet() {	 
		return markerArraySelector.getSelectedMarkerSet();
	}
	
	public  String[] getSelectedArraySet() {			
		return markerArraySelector.getSelectedArraySet();
	}
	
	public  String[] getSelectedArraySetNames() {	 
		return markerArraySelector.getSelectedArraySetNames();
	}	
	
	
	/**
	 * Create Array Data for selected markerSet
	 */
	public ArrayList<String> getArrayData(long setNameId) {

		@SuppressWarnings("rawtypes")
		List subSet = SubSetOperations.getArraySet(setNameId);
		ArrayList<String> positions = (((SubSet) subSet.get(0)).getPositions());

		return positions;
	}
	
	/**
	 * Create Marker Data for selected markerSet
	 */
	public  ArrayList<String> getMarkerData(long setNameId) {

		@SuppressWarnings("rawtypes")
		List subSet = SubSetOperations.getMarkerSet(setNameId);
		ArrayList<String> positions = (((SubSet) subSet.get(0)).getPositions());
		return positions;
	}


	// TODO this is not a final design. needed only if we decide to reuse the instance
	@Override
	public void setDataSetId(Long dataSetId) {
		User user = SessionHandler.get();
		if (user != null) {
			userId = user.getId();
		}

		this.dataSetId = dataSetId;
		markerArraySelector.setData(dataSetId, userId);
		 
	}


	@Override
	public Class<?> getResultType() {
		return AnovaResult.class;
	}

	@Override
	public String execute(Long resultId, Long datasetId,
			HashMap<Serializable, Serializable> parameters, Long userId) throws IOException,
			Exception {
		AnovaAnalysis analysis = new AnovaAnalysis(datasetId, (AnovaUI) params.get("form"));
		AnovaResult result = analysis.execute();
		FacadeFactory.getFacade().store(result);

		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, resultId);
		resultSet.setDataId(result.getId());
		FacadeFactory.getFacade().store(resultSet);
		
		return "Anova";
	}
	
}
