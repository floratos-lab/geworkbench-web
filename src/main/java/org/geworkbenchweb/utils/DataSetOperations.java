package org.geworkbenchweb.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.dataset.MicroarraySet;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.AnnotationEntry;
import org.geworkbenchweb.pojos.Comment;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.CurrentContext;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.pojos.SubSetContext;
import org.geworkbenchweb.pojos.Workspace;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/**
 * A collection of utility methods that simplify JPA code.
 * When these methods are called, the performance implication must be considered carefully.
 * 
 * @author Nikhil
 */

public class DataSetOperations {
	private static Log log = LogFactory.getLog(DataSetOperations.class);
	
	/* Get a 'default' annotation that is independent from microarray dataset. */
	static public Annotation getDefaultAnnotation() {
		String annotationName = GeworkbenchRoot.getAppProperty("default.annotation");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", annotationName);
		List<Annotation> annots = FacadeFactory.getFacade().list("Select a from Annotation as a where a.name=:name",
				params);
		if (annots.size() > 0) // assume the first one is always what we want
			return annots.get(0);
		else
			return null;
	}
	
	/* get the map from probeset ID to gene symbol, using the 'default' annotation */
	static public Map<String, String> getDefaultAnnotationMap() {
		Map<String, String> annotationMap = new HashMap<String, String>();
		Annotation a = DataSetOperations.getDefaultAnnotation();
		for (AnnotationEntry entry : a.getAnnotationEntries()) {
			annotationMap.put(entry.getProbeSetId(), entry.getGeneSymbol());
		}
		return annotationMap;
	}

	/* build a probeSetId-geneSymbol map for efficiency */
	static public Map<String, String> getAnnotationMap(Long dataSetId) {
		if(dataSetId==null) {
			return getDefaultAnnotationMap();
		}
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("dataSetId", dataSetId);
		DataSetAnnotation dataSetAnnotation = FacadeFactory
				.getFacade()
				.find("SELECT d FROM DataSetAnnotation AS d WHERE d.datasetid=:dataSetId",
						parameter);
		Map<String, String> annotationMap = new HashMap<String, String>();
		if (dataSetAnnotation != null) {
			Long annotationId = dataSetAnnotation.getAnnotationId();
			Map<String, Object> pm = new HashMap<String, Object>();
			pm.put("id", annotationId);
			List<?> entries = FacadeFactory
					.getFacade()
					.list("SELECT entries.probeSetId, entries.geneSymbol FROM Annotation a JOIN a.annotationEntries entries WHERE a.id=:id",
							pm);
			for (Object entry : entries) {
				Object[] obj = (Object[]) entry;
				// probeSetId ~ geneSymbol
				annotationMap.put((String) obj[0], (String) obj[1]);
			}
		}
		return annotationMap;
	}
	
	
	/* build a probeSetId , geneSymbol, geneDescription map for efficiency */
	static public Map<String, String[]> getAnnotationInfoMap(Long dataSetId) {
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("dataSetId", dataSetId);
		DataSetAnnotation dataSetAnnotation = FacadeFactory
				.getFacade()
				.find("SELECT d FROM DataSetAnnotation AS d WHERE d.datasetid=:dataSetId",
						parameter);
		Map<String, String[]> annotationMap = new HashMap<String, String[]>();
		if (dataSetAnnotation != null) {			
			Long annotationId = dataSetAnnotation.getAnnotationId();
			Map<String, Object> pm = new HashMap<String, Object>();
			pm.put("id", annotationId);
			List<?> entries = FacadeFactory
					.getFacade()
					.list("SELECT entries.probeSetId, entries.geneSymbol, entries.geneDescription, entries.entrezId FROM Annotation a JOIN a.annotationEntries entries WHERE a.id=:id",
							pm);
			for (Object entry : entries) {
				Object[] obj = (Object[]) entry;
				// probeSetId ~ geneSymbol
				String[] values = new String[3];
				values[0] = (String) obj[1];
				values[1] = (String) obj[2];
				values[2] = (String) obj[3];
				annotationMap.put((String) obj[0], values);
			}
		}
		return annotationMap;
	}
	

	/* get number of marker labels or microarray labels in a MicroarrayDataset */
	static public Integer getNumber(String fieldName, Long id) {
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("id", id);
		List<?> list = FacadeFactory.getFacade().getFieldValues(
				MicroarrayDataset.class, fieldName, "p.id=:id", parameter);
		if (list.size() != 1) {
			log.error("incorrect count of query results returned");
			return 0;
		}
		return (Integer) list.get(0);
	}	
	
	
	/* get marker labels or microarray labels of a MicroarrayDataset */
	static public String[] getStringLabels(String fieldName, Long id) {
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("id", id);
		List<?> list = FacadeFactory.getFacade().getFieldValues(
				MicroarrayDataset.class, fieldName, "p.id=:id", parameter);
		if (list.size() != 1) {
			log.error("incorrect count of query results returned");
			return null;
		}
		return (String[]) list.get(0);
	}
	
	public static MicroarraySet getMicroarraySet(Long dataSetId) {
		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class,
				dataSetId);
		Long id = dataset.getDataId();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(
				MicroarrayDataset.class, id);

		return new MicroarraySet(microarray.getArrayNumber(), microarray.getMarkerNumber(),
				microarray.getArrayLabels(),
				microarray.getMarkerLabels(), microarray.getExpressionValues(), null);
	}
	
	/* remove result set for a given ID. this does NOT remove the GUI representation */
	/* the only children of result set are the comments */
	public static void deleteResultSet(Long resultSetId) {
		Map<String, Object> cParam = new HashMap<String, Object>();
		cParam.put("parent", resultSetId);

		List<Comment> comments = FacadeFactory.getFacade().list(
				"Select p from Comment as p where p.parent =:parent", cParam);
		for (Comment comment : comments) {
			FacadeFactory.getFacade().delete(comment);
		}
		ResultSet result = FacadeFactory.getFacade().find(ResultSet.class,
				resultSetId);
		FacadeFactory.getFacade().delete(result);
		PreferenceOperations.deleteAllPreferences(resultSetId);
	}
	
	/* remove the dataset for the given ID, and all the related data, including result sets, preference etc. */
	/* code originally copied from RemoveButtonListener. should refactor */
	public static void deleteDataSet(Long dataId) {

		DataSet data = FacadeFactory.getFacade().find(DataSet.class, dataId);
		if (data == null) {
			log.error("dataset to be deleted not found for ID " + dataId);
		}

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("parent", dataId);

		List<SubSet> labelsets = FacadeFactory.getFacade().list(
				"Select p from SubSet as p where p.parent =:parent", params);
		for (SubSet s : labelsets) {
			FacadeFactory.getFacade().delete(s);
		}

		List<ResultSet> resultSets = FacadeFactory.getFacade().list(
				"Select p from ResultSet as p where p.parent =:parent", params);
		for (ResultSet resultSet : resultSets) {
			deleteResultSet(resultSet.getId());
		}

		List<Comment> comments = FacadeFactory.getFacade().list(
				"Select p from Comment as p where p.parent =:parent", params);
		for (Comment comment : comments) {
			FacadeFactory.getFacade().delete(comment);
		}

		Map<String, Object> cParam = new HashMap<String, Object>();
		cParam.put("datasetid", dataId);
		List<DataSetAnnotation> dsannot = FacadeFactory
				.getFacade()
				.list("Select p from DataSetAnnotation as p where p.datasetid=:datasetid",
						cParam);
		if (dsannot.size() > 0) {
			Long annotId = dsannot.get(0).getAnnotationId();
			FacadeFactory.getFacade().delete(dsannot.get(0));

			Annotation annot = FacadeFactory.getFacade().find(Annotation.class,
					annotId);
			if (annot != null && annot.getOwner() != null) {
				Map<String, Object> aiParam = new HashMap<String, Object>();
				aiParam.put("annotationid", annotId);
				List<Annotation> annots = FacadeFactory
						.getFacade()
						.list("select p from DataSetAnnotation as p where p.annotationid=:annotationid",
								aiParam);
				if (annots.size() == 0)
					FacadeFactory.getFacade().delete(annot);
			}
		}

		List<Context> contexts = SubSetOperations.getAllContexts(dataId);
		for (Context c : contexts) {
			Map<String, Object> ciParam = new HashMap<String, Object>();
			ciParam.put("contextid", c.getId());
			List<SubSetContext> subcontexts = FacadeFactory
					.getFacade()
					.list("Select a from SubSetContext a where a.contextid=:contextid",
							ciParam);
			FacadeFactory.getFacade().deleteAll(subcontexts);
			FacadeFactory.getFacade().delete(c);
		}

		Map<String, Object> ccParam = new HashMap<String, Object>();
		ccParam.put("datasetid", dataId);
		List<CurrentContext> cc = FacadeFactory
				.getFacade()
				.list("Select p from CurrentContext as p where p.datasetid=:datasetid",
						ccParam);
		if (cc.size() > 0)
			FacadeFactory.getFacade().deleteAll(cc);

		FacadeFactory.getFacade().delete(data);

		// delete dataset preference
		PreferenceOperations.deleteAllPreferences(dataId);
	}

	/* remove workspace for a given ID */
	public static void deleteWorkspace(Long workspaceId) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("workspace", workspaceId);

		List<DataSet> datasets = FacadeFactory.getFacade().list(
				"Select p from DataSet as p where p.workspace =:workspace",
				param);
		for (DataSet dataset : datasets) {
			deleteDataSet(dataset.getId());
		}
		Workspace workspace = FacadeFactory.getFacade().find(Workspace.class,
				workspaceId);
		if(workspace==null) {
			log.error("workspace to be deleted for ID "+workspaceId+" not found");
			return;
		}
		FacadeFactory.getFacade().delete(workspace);
	}
	
	public static int getSubDatasetNum(Long parentId, String type) {
		Map<String, Object> cParam = new HashMap<String, Object>();
		cParam.put("parent", parentId);	 
		cParam.put("type", type);

		List<ResultSet> results = FacadeFactory.getFacade().list(
				"Select p from ResultSet as p where p.parent =:parent and p.type = :type", cParam);
		 
		return results.size();
	}
	
	
}
