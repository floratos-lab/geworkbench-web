package org.geworkbenchweb.plugins.geneontology;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.AnnotationEntry;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.GOResult;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.GeneOntologyTree;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;

public class GOUI extends VerticalLayout implements AnalysisUI {

	private static final long serialVersionUID = -2602777981043175022L;
	private static Log log = LogFactory.getLog(GOUI.class);
	
	private Long dataSetId;
	private final HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();

	private final ComboBox referenceGene = new ComboBox("Reference Gene List");
	private final ComboBox changedGene = new ComboBox("Changed Gene List (From Set)");
	private final ComboBox cmbAnnotationFile = new ComboBox("Annotation File");
	private final ComboBox calculationName = new ComboBox("Enrichment Method",
			Arrays.asList(calculationMethodName));
	private final ComboBox correctionName = new ComboBox(
			"Multiple Testing Correction", Arrays.asList(correctionMethodName));

	private final Map<String, String> geneMap = new HashMap<String, String>();

	public GOUI(Long dataId) {

		setSpacing(true);
		setImmediate(true);
		setDefaultParameters(params);
		
		setDataSetId(dataId);
		
		referenceGene.addItem("All Genes");
		referenceGene.select("All Genes");
		
		String[] allGenes = geneMap.values().toArray(new String[geneMap.size()]);
		params.put(PARAM_REFERENCE_GENE_LIST, allGenes);
		
		changedGene.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_EXPLICIT_DEFAULTS_ID);
		changedGene.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = -332132852289677807L;
			private Long oldValue = null;
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				Object newValue = event.getProperty().getValue();
				if (newValue != null && !((Long) newValue).equals(oldValue)) {
					List<String> markerList = SubSetOperations
							.getMarkerData((Long) newValue);
					String[] genes = new String[markerList.size()];
					for(int i=0; i<markerList.size(); i++) {
						genes[i] = geneMap.get(markerList.get(i));
					}
					params.put(PARAM_CHANGED_GENE_LIST, genes);
					oldValue = (Long)newValue;
				}
			}
			
		});
		
		cmbAnnotationFile.setImmediate(true);
		params.put(PARAM_ASSOCIATION_FILE, (String)cmbAnnotationFile.getValue());
		cmbAnnotationFile.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = -332132852289677807L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object newValue = event.getProperty().getValue();
				String oldValue = (String)params.get(PARAM_ASSOCIATION_FILE);
				if (newValue != null && !((String) newValue).equals(oldValue)) {
					params.put(PARAM_ASSOCIATION_FILE, (String) newValue);
				}
			}
		});
		
		calculationName.setValue(calculationMethodName[3]);
		calculationName.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 5935627057320534374L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object newValue = event.getProperty().getValue();
				String oldValue = (String)params.get(PARAM_CALCULATION_METHOD);
				if (newValue != null && !((String) newValue).equals(oldValue)) {
					params.put(PARAM_CALCULATION_METHOD, (String) newValue);
				}
			}
			
		});
		
		correctionName.setValue(correctionMethodName[4]);
		correctionName.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 5935627057320534374L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object newValue = event.getProperty().getValue();
				String oldValue = (String)params.get(PARAM_CORRECTION);
				if (newValue != null && !((String) newValue).equals(oldValue)) {
					params.put(PARAM_CORRECTION, (String) newValue);
				}
			}
			
		});
		
		Button submitButton = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 3931108003633578684L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (!validateInput())
					return;

				ResultSet resultSet = new ResultSet();
				java.sql.Timestamp timestamp = new java.sql.Timestamp(System
						.currentTimeMillis());
				resultSet.setTimestamp(timestamp);
				String dataSetName = "Gene Ontology - Pending";
				resultSet.setName(dataSetName);
				resultSet.setType(getResultType().getName());
				resultSet.setParent(dataSetId);
				resultSet.setOwner(SessionHandler.get().getId());
				FacadeFactory.getFacade().store(resultSet);

				GeworkbenchRoot app = (GeworkbenchRoot) GOUI.this
						.getApplication();
				app.addNode(resultSet);

				AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
						resultSet, params, GOUI.this);
				app.getBlackboard().fire(analysisEvent);
			}
		});

		addComponent(referenceGene);
		addComponent(changedGene);
		addComponent(cmbAnnotationFile);
		addComponent(calculationName);
		addComponent(correctionName);
		addComponent(submitButton);
	}
	
	@Override
	public void attach() {
		super.attach();
		String username = SessionHandler.get().getUsername();
		populateAnnotationFiles(cmbAnnotationFile, username);
	}

	private static void populateAnnotationFiles(ComboBox cmbAnnotationFile, String username) {
		cmbAnnotationFile.removeAllItems();		
		String dirName = GeworkbenchRoot.getBackendDataDirectory()
				+ System.getProperty("file.separator")
				+ username
				+ System.getProperty("file.separator") + "annotation";
		File dir = new File(dirName);
		if (!dir.isDirectory()) {
			log.error(dirName + " is not directory.");
			return;
		}
		if (dir.listFiles() == null || dir.listFiles().length <= 0) {
			log.error(dirName + " is empty.");
			return;
		}
		for(File f : dir.listFiles()) {
			String fullpath = f.getAbsolutePath();
			if(!fullpath.endsWith(".csv")) continue;
			cmbAnnotationFile.addItem(fullpath);
			if(cmbAnnotationFile.getValue()==null) {
				cmbAnnotationFile.setValue(fullpath);
			}
		}
	}

	private void populateSetsForChangedGenes(Long dataSetId) {
		changedGene.removeAllItems();
		
		List<AbstractPojo> markerSubSets = SubSetOperations
				.getMarkerSets(dataSetId);
		for (AbstractPojo s : markerSubSets) {
			if (!(s instanceof SubSet)) {
				log.error("wrong type in markerSubSets");
				continue;
			}
			SubSet subset = (SubSet)s;
			Long subsetId = subset.getId();
			changedGene.addItem( subsetId );
			changedGene.setItemCaption( subsetId, subset.getName() );
		}
	}

	private static String[] getAllGenes(Long dataSetId) {
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("dataSetId", dataSetId);
		DataSetAnnotation dataSetAnnotation = FacadeFactory.getFacade().find(
				"SELECT d FROM DataSetAnnotation AS d WHERE d.datasetid=:dataSetId", parameter);
		Set<String> geneSymbols = new HashSet<String>();
		if(dataSetAnnotation!=null) {
			Long annotationId = dataSetAnnotation.getAnnotationId();
			Annotation annotation = FacadeFactory.getFacade().find(Annotation.class, annotationId);
			for(AnnotationEntry entry : annotation.getAnnotationEntries()) {
				geneSymbols.add(entry.getGeneSymbol());
			}
		}
		return geneSymbols.toArray(new String[geneSymbols.size()]);
	}
	
	public static final String PARAM_REFERENCE_GENE_LIST = "reference gene list";
	public static final String PARAM_CHANGED_GENE_LIST = "changed gene list";
	public static final String PARAM_CALCULATION_METHOD = "calculation method";
	public static final String PARAM_CORRECTION = "correction";
	public static final String PARAM_OBO_FILE = "obo file";
	public static final String PARAM_ASSOCIATION_FILE = "association file";
	
	/*
	 * these names must match the exact same names coded in ontologizer2.0's
	 * classes that implement interface AbstractTestCorrection.
	 */
	private static final String[] correctionMethodName = { "Benjamini-Hochberg",
			"Benjamini-Yekutieli", "Bonferroni", "Bonferroni-Holm", "None",
			"Westfall-Young-Single-Step", "Westfall-Young-Step-Down" };
	/*
	 * these names must match the exact same names coded in ontologizer2.0's
	 * classes that implement interface ICalculation.
	 */
	private static final String[] calculationMethodName = { "Parent-Child-Union",
			"Parent-Child-Intersection", "Probabilistic", "Term-For-Term",
			"Topology-Elim", "Topology-Weighted" };

	private static void setDefaultParameters(Map<Serializable, Serializable> params) {
		params.put(PARAM_REFERENCE_GENE_LIST, new String[]{});
		params.put(PARAM_CHANGED_GENE_LIST, new String[]{}) ; //getGeneListFromSet(dataSetId)); // new String[]{});
		params.put(PARAM_CALCULATION_METHOD, calculationMethodName[3]); //"Term-For-Term");
		params.put(PARAM_CORRECTION, correctionMethodName[4]); //"None");
		
		URL defaultObo = GOUI.class.getClassLoader().getResource(GeneOntologyTree.DEFAULT_OBO_FILE);
		params.put(PARAM_OBO_FILE, defaultObo.getPath()); //GeworkbenchRoot.getBackendDataDirectory()
				//+ "/go" + GeneOntologyTree.DEFAULT_OBO_FILE);
		params.put(PARAM_ASSOCIATION_FILE, "");
	}
	
	private boolean validateInput() {
		if(referenceGene.getValue()==null) {
			referenceGene.setComponentError(new UserError(
					"No reference gene option is selected"));
			return false;
		}
		if (params.get(PARAM_ASSOCIATION_FILE) == null
				|| ((String) params.get(PARAM_ASSOCIATION_FILE)).trim()
						.length() == 0) {
			cmbAnnotationFile.setComponentError(new UserError(
					"No associatation file is selected"));
			return false;
		}
		return true;
	}

	@Override
	public void setDataSetId(Long dataSetId) {
		if(dataSetId==0) return;
		
		this.dataSetId = dataSetId;
		params.put(PARAM_REFERENCE_GENE_LIST, getAllGenes(dataSetId));
		populateSetsForChangedGenes(dataSetId);
		
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("dataSetId", dataSetId);
		DataSetAnnotation dataSetAnnotation = FacadeFactory.getFacade().find(
				"SELECT d FROM DataSetAnnotation AS d WHERE d.datasetid=:dataSetId", parameter);
		if(dataSetAnnotation!=null) {
			Long annotationId = dataSetAnnotation.getAnnotationId();
			Annotation annotation = FacadeFactory.getFacade().find(Annotation.class, annotationId);
			for(AnnotationEntry entry : annotation.getAnnotationEntries()) {
				geneMap.put(entry.getProbeSetId(), entry.getGeneSymbol());
			}
		} else {
			log.warn("no annotation found for dataset ID "+dataSetId);
		}
	}

	@Override
	public Class<?> getResultType() {
		return GOResult.class;
	}

	@Override
	public String execute(Long resultId,
			HashMap<Serializable, Serializable> parameters, Long userId)
			throws IOException, Exception {
		GOAnalysis analysis = new GOAnalysis(dataSetId, params);
		GOResult output = analysis.execute();
		FacadeFactory.getFacade().store(output);
		ResultSet result = FacadeFactory.getFacade().find(ResultSet.class,
				resultId);
		result.setDataId(output.getId());
		FacadeFactory.getFacade().store(result);

		return "GO Result";
	}
}