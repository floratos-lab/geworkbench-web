package org.geworkbenchweb.plugins.ttest;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.geworkbench.components.ttest.data.TTestOutput;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.pojos.TTestResult;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * t-Test Analysis for microarray dataset
 * @author Nikhil Reddy
 * @version $Id$
 */
public class TTestUI extends VerticalLayout implements AnalysisUI {

	private static final long serialVersionUID = 1L;
	
	private Long dataSetId;
	private Long userId;
	
	private Accordion tabs;
	
	private CaseControlSelector caseControlSelector;
	 
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
	
	public TTestUI() {
		this(0L);
	}
	
	public TTestUI(Long dId) {
		
		this.dataSetId = dId;
		userId = SessionHandler.get().getId();
		
		setSpacing(true);
		setImmediate(true);
		
		tabs 			= 	new Accordion();
		caseControlSelector = new CaseControlSelector(dataSetId, userId, "TTestUI");	 
		
		tabs.addTab(buildPValuePanel(), "P-Value Parameters", null);
		tabs.addTab(buildAlphaCorrections(), "Alpha Corrections", null);
		tabs.addTab(buildDegOfFreedom(), "Degree of Freedom", null);
		 
		
		setDataSetId(dId);
		
		submit = new Button("Submit", new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				   
					String[] selectedCaseSets = caseControlSelector.getSelectedCaseSet();
					String[] selectedControlSets = caseControlSelector.getSelectedControlSet();
					 String warnMsg = validInputData(selectedCaseSets, selectedControlSets);
					if( warnMsg != null ) 
					{ 
						MessageBox mb = new MessageBox(getWindow(), 
					 
							"Warning", 
							MessageBox.Icon.INFO, 
							warnMsg,
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					    mb.show();
					    return;
					}
				 
				params.put(TTestParameters.CASEARRAY, (Serializable) selectedCaseSets);
				params.put(TTestParameters.CONTROLARRAY, (Serializable) selectedControlSets);
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
				
				ResultSet resultSet = new ResultSet();
				java.sql.Timestamp timestamp =	new java.sql.Timestamp(System.currentTimeMillis());
				resultSet.setTimestamp(timestamp);
				String dataSetName = "TTest - Pending";
				resultSet.setName(dataSetName);
				resultSet.setType(getResultType().getName());
				resultSet.setParent(dataSetId);		
				resultSet.setOwner(userId);
				FacadeFactory.getFacade().store(resultSet);

				GeworkbenchRoot app = (GeworkbenchRoot) TTestUI.this
						.getApplication();
				app.addNode(resultSet);

				AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
						resultSet, params, TTestUI.this);
				GeworkbenchRoot.getBlackboard().fire(analysisEvent);
				
			}
		});
		 
		addComponent(caseControlSelector);
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
		caseControlSelector.setData(dataId, userId);	 
	}

	@Override
	public Class<?> getResultType() {
		return org.geworkbenchweb.pojos.TTestResult.class;
	}

	private String validInputData(String[] selectedCaseSets, String[] selectedControlSets)
	{    
	 
		if(selectedCaseSets == null || selectedCaseSets.length == 0 || selectedControlSets == null || selectedControlSets.length ==0 ) 
		{ 
			 return "Please select case array and control array. ";
		}
		List<String> microarrayPosList = new ArrayList<String>();
		List<String> caseSetList = new ArrayList<String>();
		/* for each group */
		for (int i = 0; i < selectedCaseSets.length; i++) {			
			 caseSetList.add(selectedCaseSets[i].trim());
			 List<String> arrays = SubSetOperations.getArrayData(Long
						.parseLong(selectedCaseSets[i].trim()));	 
			 
			 for (int j = 0; j < arrays.size(); j++) {
				if (microarrayPosList.contains(arrays.get(j)))  				
					 return "Same array (" + arrays.get(j) + ") exists in case array groups.";				 
				microarrayPosList.add(arrays.get(j));				 
			}
		}
		microarrayPosList.clear();
		for (int i = 0; i < selectedControlSets.length; i++) {
			 if (caseSetList.contains(selectedControlSets[i].trim()))
			 {
				  SubSet subset = (SubSet)SubSetOperations.getArraySet(Long
							.parseLong(selectedControlSets[i].trim())).get(0);
				 return "Case and control groups have same array set " + subset.getName() + ".";
			 }
			 List<String> arrays = SubSetOperations.getArrayData(Long
						.parseLong(selectedControlSets[i].trim()));	 			 
			 for (int j = 0; j < arrays.size(); j++) {
				if (microarrayPosList.contains(arrays.get(j)))  				
					 return "Same array (" + arrays.get(j) + ") exists in control array groups.";				 
				microarrayPosList.add(arrays.get(j));				 
			}
		}
		
		return null;
		
	}

	@Override
	public String execute(Long resultId,
			HashMap<Serializable, Serializable> parameters, Long userId)
			throws IOException, Exception {
		TTestAnalysisWeb analyze = new TTestAnalysisWeb(dataSetId, params);
		TTestOutput tTestOutput = analyze.execute();
		TTestResult resultSet = new TTestResult(tTestOutput);
		FacadeFactory.getFacade().store(resultSet);
		
		ResultSet result = FacadeFactory.getFacade().find(ResultSet.class, resultId);
		result.setDataId(resultSet.getId());
		FacadeFactory.getFacade().store(result);
		
		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		Long id = dataset.getDataId();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(MicroarrayDataset.class, id);
		String[] markerLabels = microarray.getMarkerLabels();
		
		int[] significantIndex = resultSet.getSignificantIndex();
		if (significantIndex!=null)
		{			 
			List<String> significantMarkerNames = new ArrayList<String>();
			for(int i=0; i<significantIndex.length; i++) {
				String markerLabel = markerLabels[ significantIndex[i] ];
				significantMarkerNames.add(markerLabel);
			}
			java.util.Collections.sort(significantMarkerNames);
			SubSetOperations.storeSignificance(significantMarkerNames, dataSetId, userId);
		
		} 
		 
		return "TTest";
	}	
	
}
