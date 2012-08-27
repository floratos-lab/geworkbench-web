package org.geworkbenchweb.analysis.anova.ui;

 
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 
import org.geworkbench.components.anova.PValueEstimation;
import org.geworkbench.components.anova.FalseDiscoveryRateControl;
 
import org.geworkbenchweb.analysis.anova.AnovaAnalysis;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
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
 

 
import com.vaadin.data.validator.IntegerValidator;
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
	
	

	private OptionGroup og;
	
	private TextField falseSignificantGenesLimit;

	private Button submitButton;

	public UAnovaParamForm(final DSMicroarraySet maSet) {

	 
		String dataSetName = maSet.getDataSetName();

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

		List<?> data = DataSetOperations.getDataSet(dataSetName);
		List<?> subMarkerSets = SubSetOperations.getMarkerSets(((DataSet) data
				.get(0)).getId());
		List<?> subArraySets = SubSetOperations.getArraySets(((DataSet) data
				.get(0)).getId());

		markerSetSelect = new ListSelect("Select Marker Sets:");
		markerSetSelect.setMultiSelect(true);
		markerSetSelect.setRows(5);
		markerSetSelect.setColumns(10);
		markerSetSelect.setImmediate(true);

		arraySetSelect = new ListSelect("Select array sets:");
		arraySetSelect.setMultiSelect(true);
		arraySetSelect.setRows(5);
		arraySetSelect.setColumns(10);
		arraySetSelect.setImmediate(true);

		if (subMarkerSets != null)
			for (int m = 0; m < (subMarkerSets).size(); m++) {

				markerSetSelect.addItem(((SubSet) subMarkerSets.get(m))
						.getName());

			}

		if (subArraySets != null)
			for (int m = 0; m < (subArraySets).size(); m++) {

				arraySetSelect
						.addItem(((SubSet) subArraySets.get(m)).getName());

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
		pValThreshold.setNullSettingAllowed(false);

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
					  }
		              else if ( og.getValue().equals(FalseDiscoveryRateControl.proportion.ordinal()) ) 
		              {
				 
					     gridLayout3.setVisible(true);
					     falseSignificantGenesLimit.setValue(0.01);
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
			 
	            // Put the component in error state and
	            // set the error message.
			 if (!permNumber.isValid())
			 	permNumber.setComponentError(
	                new UserError("Must be letters and numbers"));
			 else
				 permNumber.setComponentError(null);
			 
			
			AnovaAnalysis analysis = new AnovaAnalysis(dataSet, paramform);
			analysis.execute();
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

	public int getPValueEstimation() {		 
		return Integer.parseInt(pValEstCbx.getValue().toString().trim());
	}
	
	public int getPermNumber() {	 
		return Integer.parseInt(permNumber.getValue().toString().trim());
	}
		
	public double getPValThreshold() {			
		return Double.parseDouble(pValThreshold.getValue().toString().trim());
	}
	
	public int getFalseDiscoveryRateControl() {	
		return Integer.parseInt(og.getValue().toString().trim());		 
	}
	
	public float getFalseSignificantGenesLimit() {	
		return Float.parseFloat(permNumber.getValue().toString().trim());
		 
	}	 
	
}
