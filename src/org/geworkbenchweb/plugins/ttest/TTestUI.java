package org.geworkbenchweb.plugins.ttest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.UserDirUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * TTest Analysis for microarray dataset
 * @author Nikhil Reddy
 */
public class TTestUI extends VerticalLayout implements AnalysisUI {

	private static final long serialVersionUID = 1L;
	
	private Long dataSetId;
	private Long userId;
	
	private Accordion tabs;
	
	private ComboBox selectCase;
	
	private ComboBox selectControl;
	
	private ComboBox pValue;
	
	private ComboBox logNorm;
	
	private TextField criticalValue;
	
	private OptionGroup correctionMethod;
	
	private OptionGroup stepMethod;
	
	private OptionGroup groupVariances;
	
	private Button submit;
	
	private OptionGroup perOp;
	
	private TextField groupTimes;
	
	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();
	
	public TTestUI(Long dId) {
		
		this.dataSetId = dId;
		
		setSpacing(true);
		setImmediate(true);
		
		tabs 			= 	new Accordion();
		selectCase		=	new ComboBox();
		selectControl 	=	new ComboBox();	
		
		tabs.addTab(buildPValuePanel(), "P-Value Parameters", null);
		tabs.addTab(buildAlphaCorrections(), "Alpha Corrections", null);
		tabs.addTab(buildDegOfFreedom(), "Degree of Freedom", null);
		
		selectCase.setNullSelectionAllowed(false);
		selectCase.setInputPrompt("Select Case from Phenotypes sets");
		selectCase.setWidth("400px");
		selectCase.setImmediate(true);
	
		selectControl.setNullSelectionAllowed(false);
		selectControl.setWidth("400px");
		selectControl.setInputPrompt("Select Control from Phenotypes sets");
		selectControl.setImmediate(true);
		
		setDataSetId(dId);
		
		submit = new Button("Submit", new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					if(selectCase.getValue().equals(null) || selectControl.getValue().equals(null)) { }
				}catch (NullPointerException e) {
					MessageBox mb = new MessageBox(getWindow(), 
							"Warning", 
							MessageBox.Icon.INFO, 
							"Please select case array and control array ",
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
					return;
				}
				params.put(TTestParameters.CASEARRAY, (Serializable) selectCase.getValue());
				params.put(TTestParameters.CONTROLARRAY, (Serializable) selectControl.getValue());
				params.put(TTestParameters.ALPHA, (Serializable) criticalValue.getValue());
				if(pValue.getValue().toString().equalsIgnoreCase("t-distribution")) {
					params.put(TTestParameters.ISPERMUT, (Serializable) false);
					params.put(TTestParameters.CORRECTIONMETHOD, (Serializable) correctionMethod.getValue());
				}else {
					params.put(TTestParameters.ISPERMUT, (Serializable) true);
					if(stepMethod.getValue() != null) {
						params.put(TTestParameters.CORRECTIONMETHOD, (Serializable) stepMethod.getValue());
					}else {
						params.put(TTestParameters.CORRECTIONMETHOD, (Serializable) correctionMethod.getValue());
					}
				}
				params.put(TTestParameters.LOGNORMALIZED, (Serializable) logNorm.getValue());
				params.put(TTestParameters.WELCHDIFF, (Serializable) groupVariances.getValue());
				if(!pValue.getValue().toString().equalsIgnoreCase("t-distribution")) {
					if(perOp.getValue().toString().equalsIgnoreCase("Use all Permutations")) {
						params.put(TTestParameters.ALLCOMBINATATIONS, (Serializable) true);
					}else {
						params.put(TTestParameters.ALLCOMBINATATIONS, (Serializable) false);
						params.put(TTestParameters.NUMCOMBINATIONS, (Serializable) groupTimes.getValue());
					}
				}
				
				DSMicroarraySet maSet = (DSMicroarraySet) ObjectConversion
						.toObject(UserDirUtils.getDataSet(dataSetId));

				ResultSet resultSet = new ResultSet();
				java.sql.Date date = new java.sql.Date(System
						.currentTimeMillis());
				resultSet.setDateField(date);
				String dataSetName = "TTest - Pending";
				resultSet.setName(dataSetName);
				resultSet.setType(getResultType().getName());
				resultSet.setParent(dataSetId);
				userId = SessionHandler.get().getId();
				resultSet.setOwner(userId);
				FacadeFactory.getFacade().store(resultSet);

				NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
				GeworkbenchRoot.getBlackboard().fire(resultEvent);

				AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
						maSet, resultSet, params, TTestUI.this);
				GeworkbenchRoot.getBlackboard().fire(analysisEvent);
				
			}
		});
		addComponent(selectCase);
		addComponent(selectControl);
		addComponent(tabs);
		addComponent(submit);
	}

	/**
	 * Builds P-value tab
	 * @return GridLayout
	 */
	private GridLayout buildPValuePanel() {
		GridLayout a = new GridLayout();
		
		a.setColumns(2);
		a.setRows(3);
		a.setImmediate(true);
		a.setSpacing(true);
		a.setHeight("200px");
		a.setMargin(true);
		
		pValue = new ComboBox();
		pValue.setNullSelectionAllowed(false);
		pValue.setCaption("Select Correction Method");
		pValue.addItem("t-distribution");
		pValue.addItem("permutation");
		pValue.select("t-distribution");
		pValue.setImmediate(true);
		pValue.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if(event.getProperty().getValue().toString().equalsIgnoreCase("permutation")) {
					perOp.setEnabled(true);
					groupTimes.setEnabled(true);
					stepMethod.setEnabled(true);
				} else {
					perOp.setEnabled(false);
					groupTimes.setEnabled(false);
					stepMethod.setEnabled(false);
				}
			}
		});
		
		logNorm = new ComboBox();
		logNorm.setNullSelectionAllowed(false);
		logNorm.setCaption("Data is log2-transformed");
		logNorm.addItem("No");
		logNorm.addItem("Yes");
		logNorm.select("No");
		logNorm.setImmediate(true);
		
		perOp = new OptionGroup();
		perOp.setEnabled(false);
		perOp.setNullSelectionAllowed(false);
		perOp.addItem("Randomly group experiments");
		perOp.addItem("Use all Permutations");
		perOp.select("Randomly group experiments");
		
		groupTimes = new TextField();
		groupTimes.setCaption("(#times)");
		groupTimes.setEnabled(false);
		groupTimes.setValue("100");
		groupTimes.setNullSettingAllowed(false);
		
		criticalValue = new TextField();
		criticalValue.setCaption("Overall Alpha");
		criticalValue.setValue("0.02");
		criticalValue.setNullSettingAllowed(false);
		
		a.addComponent(pValue, 0, 0);
		a.addComponent(criticalValue, 1, 0);
		a.addComponent(perOp, 0, 1);
		a.addComponent(groupTimes, 1, 1);
		a.addComponent(logNorm, 0, 2);
		return a;
	}

	/**
	 * Builds Aplha corrections tab
	 * @return VerticalLayout
	 */
	private VerticalLayout buildAlphaCorrections() {
		VerticalLayout b = new VerticalLayout();
		
		b.setImmediate(true);
		b.setSpacing(true);
		b.setHeight("175px");
		b.setMargin(true);
		
		correctionMethod = new OptionGroup();
		correctionMethod.setCaption("Select Case from Phenotypes sets");
		correctionMethod.addItem("Just alpha (no-correction)");
		correctionMethod.addItem("Standard Bonferroni Correction");
		correctionMethod.addItem("Adjusted Bonferroni Correction");
		correctionMethod.select("Just alpha (no-correction)");
		correctionMethod.setImmediate(true);
		correctionMethod.addListener(new Property.ValueChangeListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				try {
					if(!event.getProperty().getValue().toString().equals(null)) {
						stepMethod.select(null);
					}
				}catch(Exception e) {
					//System.out.println("debug");
				}
			}
		});
		
		stepMethod = new OptionGroup();
		stepMethod.setCaption("Step down westfall and young methods (for permutation only)");
		stepMethod.addItem("minP");
		stepMethod.addItem("maxT");
		stepMethod.addItem("minP");
		stepMethod.setImmediate(true);
		stepMethod.setEnabled(false);
		stepMethod.addListener(new Property.ValueChangeListener() {
		
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				try {
					if(!event.getProperty().getValue().toString().equals(null)) {
						correctionMethod.select(null);
					}
				}catch(Exception e) {	
					//System.out.println("debug");
				}
			}
		});
		
		b.addComponent(correctionMethod);
		b.addComponent(stepMethod);
		return b;
	}

	/**
	 * Builds Degree of Freedom tab 
	 * @return VerticalLayout
	 */
	private VerticalLayout buildDegOfFreedom() {
		VerticalLayout c = new VerticalLayout();
		
		c.setImmediate(true);
		c.setSpacing(true);
		c.setHeight("100px");
		c.setMargin(true);
		
		groupVariances = new OptionGroup();
		groupVariances.setNullSelectionAllowed(false);
		groupVariances.setCaption("Group Variences");
		groupVariances.addItem("Unequal (Welch approximation)");
		groupVariances.addItem("Equal");
		groupVariances.select("Unequal (Welch approximation)");
		groupVariances.setImmediate(true);
		
		c.addComponent(groupVariances);
		
		return c;
	}

	@Override
	public void setDataSetId(Long dataId) {
		this.dataSetId = dataId;
		
		List<?> arraySubSets = SubSetOperations.getArraySets(dataSetId);

		selectCase.removeAllItems();
		for (int m = 0; m < (arraySubSets).size(); m++) {
			selectCase.addItem(((SubSet) arraySubSets.get(m)).getId());
			selectCase.setItemCaption(
					((SubSet) arraySubSets.get(m)).getId(),
					((SubSet) arraySubSets.get(m)).getName());
		}

		selectControl.removeAllItems();
		for (int m = 0; m < (arraySubSets).size(); m++) {
			selectControl.addItem(((SubSet) arraySubSets.get(m)).getId());
			selectControl.setItemCaption(
					((SubSet) arraySubSets.get(m)).getId(),
					((SubSet) arraySubSets.get(m)).getName());
		}
	}

	@Override
	public Class<?> getResultType() {
		return DSSignificanceResultSet.class;
	}

	@Override
	public String execute(Long resultId, DSDataSet<?> dataset,
			HashMap<Serializable, Serializable> parameters) {
		TTestAnalysisWeb analyze = new TTestAnalysisWeb((DSMicroarraySet) dataset, params);
		DSSignificanceResultSet<DSGeneMarker> sigSet = analyze.execute();
		UserDirUtils.saveResultSet(resultId, ObjectConversion.convertToByte(sigSet));
		if (!sigSet.getSignificantMarkers().isEmpty())
		{			 
			List<String> significantMarkerNames = new ArrayList<String>();
			for(int i=0; i<sigSet.getSignificantMarkers().size(); i++)
				significantMarkerNames.add(sigSet.getSignificantMarkers().get(i).getLabel());
			java.util.Collections.sort(significantMarkerNames);
			SubSetOperations.storeSignificance(significantMarkerNames, dataSetId, userId);
		
		} 
		 
		return "TTest";
	}
}
