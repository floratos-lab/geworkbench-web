package org.geworkbenchweb.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.dataset.MicroarraySet;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.AnnotationEntry;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/**
 * A collection of utility methods that simplify JPA code.
 * When these methods are called, the performance implication must be considered carefully.
 * 
 * @author Nikhil
 */

public class DataSetOperations {
	private static Log log = LogFactory.getLog(DataSetOperations.class);
	
	/* build a probeSetId-geneSymbol map for efficiency */
	static public Map<String, String> getAnnotationMap(Long dataSetId) {
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("dataSetId", dataSetId);
		DataSetAnnotation dataSetAnnotation = FacadeFactory
				.getFacade()
				.find("SELECT d FROM DataSetAnnotation AS d WHERE d.datasetid=:dataSetId",
						parameter);
		Map<String, String> annotationMap = new HashMap<String, String>();
		if (dataSetAnnotation != null) {
			Long annotationId = dataSetAnnotation.getAnnotationId();
			Annotation annotation = FacadeFactory.getFacade().find(
					Annotation.class, annotationId);
			for (AnnotationEntry entry : annotation.getAnnotationEntries()) {
				String probeSetId = entry.getProbeSetId();
				annotationMap.put(probeSetId, entry.getGeneSymbol());
			}
		}
		return annotationMap;
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
}
