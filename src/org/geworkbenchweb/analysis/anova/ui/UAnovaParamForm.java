package org.geworkbenchweb.analysis.anova.ui;

 
import java.util.List;
import java.util.ArrayList;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 
import org.geworkbench.components.anova.PValueEstimation;
import org.geworkbench.components.anova.FalseDiscoveryRateControl; 
import org.geworkbenchweb.analysis.anova.AnovaAnalysis; 
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.OptionGroup; 
import com.vaadin.ui.themes.Reindeer;
 
import com.vaadin.ui.Label;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Select;

 
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.terminal.UserError;

/**
 * Builds the Parameter Form for Anova Analysis
 * 
 * @author Min You
 */
public class UAnovaParamForm extends VerticalLayout {

	private static final long serialVersionUID = -738580934848570913L;
 
	private ListSelect markerSetSelect;
	private ListSelect arraySetSelect;
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
 

	public UAnovaParamForm(final DSMicroarraySet maSet, long dataSetId) {

		this.dataSetId = dataSetId;
	 
		final GridLayout gridLayout1 = new GridLayout(2, 2);
		final GridLayout gridLayout2 = new GridLayout(4, 3);
		final GridLayout gridLayout3 = new GridLayout(3, 1);

		gridLayout1.setSpacing(true);
		gridLayout2.setSpacing(true);
		gridLayout3.setSpacing(true);
		gridLayout1.setImmediate(true);
		gridLayout2.setImmediate(true);
		gridLayout3.setImmediate(true);

		setSpacing(true);
		setImmediate(true);

		List<?> subMarkerSets = SubSetOperations.getMarkerSets(dataSetId);
		List<?> subArraySets = SubSetOperations.getArraySets(dataSetId);

		markerSetSelect = new ListSelect("Select Marker Sets:");
		markerSetSelect.setMultiSelect(true);
		markerSetSelect.setRows(5);
		markerSetSelect.setColumns(10);
		markerSetSelect.setImmediate(true);
		

		arraySetSelect = new ListSelect("Select array sets:");
		arraySetSelect.setMultiSelect(true);
		arraySetSelect.setRows(5);
		arraySetSelect.setColumns(10);
		arraySetSelect.setItemCaptionMode(Select.ITEM_CAPTION_MODE_EXPLICIT);
		arraySetSelect.setImmediate(true);

		if (subMarkerSets != null)
			for (int m = 0; m < (subMarkerSets).size(); m++) {

				markerSetSelect.addItem(((SubSet) subMarkerSets.get(m)).getId());
				markerSetSelect.setItemCaption(((SubSet) subMarkerSets.get(m)).getId(), ((SubSet) subMarkerSets.get(m)).getName());

			}

		if (subArraySets != null)
			for (int m = 0; m < (subArraySets).size(); m++) {

				arraySetSelect.addItem(((SubSet) subArraySets.get(m)).getId().longValue());
				arraySetSelect.setItemCaption(((SubSet) subArraySets.get(m)).getId(), ((SubSet) subArraySets.get(m)).getName());
				
			}

	 

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

		pValEstCbx.addListener(new Property.ValueChangeListener() {

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
					  
					  gridLayout3.setVisible(false);
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
						  gridLayout3.setVisible(true);
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
		
		og.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				 
					  if (og.getValue().equals(FalseDiscoveryRateControl.number.ordinal()) ) 
					  {
						  gridLayout3.setVisible(true);
						  falseSignificantGenesLimit.setValue(10);
						  falseSignificantGenesLimit.removeAllValidators();
						  falseSignificantGenesLimit.addValidator(new IntegerValidator("Not an integer"));
						  
					  }
		              else if ( og.getValue().equals(FalseDiscoveryRateControl.proportion.ordinal()) ) 
		              {
				 
					     gridLayout3.setVisible(true);
					     falseSignificantGenesLimit.setValue(0.05);
					     falseSignificantGenesLimit.removeAllValidators();
						  falseSignificantGenesLimit.addValidator(new DoubleValidator("Not a double"));
		              }
		              else		            	  
		            	  gridLayout3.setVisible(false);
			}
		}); 
		
		
		falseSignificantGenesLimit = new TextField();
		falseSignificantGenesLimit.setImmediate(true);
		Label permutationsOnly = new Label("  (permutations only)");
		permutationsOnly.setStyleName(Reindeer.LABEL_SMALL);
		 
		submitButton = new Button("Submit", new SubmitListener(maSet, this));

		gridLayout1.addComponent(markerSetSelect, 0, 0);
		gridLayout1.addComponent(arraySetSelect, 1, 0);

		addComponent(gridLayout1);
		addComponent(pValEstLabel);

		gridLayout2.addComponent(pValEstCbxLabel, 0, 0);
		gridLayout2.addComponent(pValEstCbx, 1, 0);
		gridLayout2.addComponent(permNumberLabel, 2, 0);
		gridLayout2.addComponent(permNumber, 3, 0);
		gridLayout2.addComponent(pValThresholdLabel, 0, 1);
		gridLayout2.addComponent(pValThreshold, 1, 1);

		addComponent(gridLayout2);
		addComponent(pValCorrectionLabel);
		addComponent(og);
		
		gridLayout3.addComponent(new Label("             "), 0, 0);
		gridLayout3.addComponent(falseSignificantGenesLimit, 1, 0);
		gridLayout3.addComponent(permutationsOnly, 2, 0);
		gridLayout3.setVisible(false);
		
		
		addComponent(gridLayout3);
		addComponent(new Label("   "));
		addComponent(submitButton);

	}

	

	private class SubmitListener implements ClickListener {

		private static final long serialVersionUID = 831124091338570481L;
		private UAnovaParamForm paramform = null;
		private DSMicroarraySet dataSet = null;

		public SubmitListener(final DSMicroarraySet dataSet,
				UAnovaParamForm paramform) {
			this.paramform = paramform;
			this.dataSet = dataSet;

		}

		@Override
		public void buttonClick(ClickEvent event) {
			 
	        if (validInputData(dataSet))
	        {
			   AnovaAnalysis analysis = new AnovaAnalysis(dataSet, paramform, dataSetId);
			   analysis.execute();
			}
		}
	}
	
	
	public  String[] getSelectedMarkerSet() {
		String[] selectList = null;
		String selectStr = markerSetSelect.getValue().toString();
		if (!selectStr.equals("[]"))
		{
			selectList = selectStr.substring(1, selectStr.length()-1).split(",");			 
			
		}
			
		return selectList;
	}
	
	public  String[] getSelectedArraySet() {		 
		String[] selectList = null;
		String selectStr = arraySetSelect.getValue().toString();
		if (!selectStr.equals("[]"))
		{
			selectList = selectStr.substring(1, selectStr.length()-1).split(",");			 
			
		}
			
		return selectList;
	}
	
	public  String[] getSelectedArraySetNames() {		 
		String[] selectList = null;
		String selectStr = arraySetSelect.getValue().toString();
		if (!selectStr.equals("[]"))
		{
			selectList = selectStr.substring(1, selectStr.length()-1).split(",");			 
		    for(int i=0; i<selectList.length; i++ )
		    	selectList[i] = arraySetSelect.getItemCaption(Long.parseLong(selectList[i].trim()));
		}		
			
		return selectList;
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
	
	
	public boolean validInputData(DSMicroarraySet dataSet)
    {
		 
		String[] selectedArraySet = null;
	 
		/* check for minimum number of activated groups */		 
		selectedArraySet = getSelectedArraySet();
		 
		if ( selectedArraySet == null || selectedArraySet.length < 3)
		{	arraySetSelect.setComponentError(
	                new UserError("Minimum of 3 array groups must be activated."));
		    return false;
		}
		 
		List<String> microarrayPosList = new ArrayList<String>();
		/* for each group */
		for (int i = 0; i < selectedArraySet.length; i++) {
			String arrayPositions = getArrayData(Long
					.parseLong(selectedArraySet[i].trim()));
			String[] temp = (arrayPositions.substring(1,
					arrayPositions.length() - 1)).split(",");
			 
			if (temp.length < 2)
			{	arraySetSelect.setComponentError(
		                new UserError("Each microarray group must contains at least 2 arrays."));
			    return false;
			}	 
			 
			for (int j = 0; j < temp.length; j++) {
				if (microarrayPosList.contains(temp[j].trim()))
				{				
					arraySetSelect.setComponentError(
			                new UserError("Same array (" + dataSet.get(Integer.parseInt(temp[j].trim()))
									+ ") exists in multiple groups."));
				    
					return false;
				}
				microarrayPosList.add(temp[j].trim());
				totalSelectedArrayNum++;
			}

		}
		arraySetSelect.setComponentError(null);
	 
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
	
	
	/**
	 * Create Array Data for selected markerSet
	 */
	public String getArrayData(long setNameId) {

		@SuppressWarnings("rawtypes")
		List subSet = SubSetOperations.getArraySet(setNameId);

		String positions = (((SubSet) subSet.get(0)).getPositions()).trim();

		return positions;
	}
	
	/**
	 * Create Marker Data for selected markerSet
	 */
	public String getMarkerData(long setNameId) {

		@SuppressWarnings("rawtypes")
		List subSet = SubSetOperations.getMarkerSet(setNameId);
		String positions = (((SubSet) subSet.get(0)).getPositions()).trim();
		return positions;
	}


	
	
}
