package org.geworkbenchweb.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.dataset.MicroarraySet;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.AnnotationEntry;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.MicroarrayRow;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/**
 * A collection of utility methods that simplify JPA code.
 * When these methods are called, the performance implication must be considered carefully.
 * 
 * @author Nikhil
 */

public class DataSetOperations {

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
	
	public static MicroarraySet getMicroarraySet(Long dataSetId) {
		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class,
				dataSetId);
		Long id = dataset.getDataId();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(
				MicroarrayDataset.class, id);

		List<String> arrayLabels = microarray.getArrayLabels();
		List<String> markerLabels = microarray.getMarkerLabels();
		int arrayNumber = arrayLabels.size();
		int markerNumber = markerLabels.size();
		List<MicroarrayRow> rows = microarray.getRows();
		float[][] values = new float[markerNumber][arrayNumber];
		for (int i = 0; i < markerNumber; i++) {
			float[] v = rows.get(i).getValueArray();
			for (int j = 0; j < arrayNumber; j++) {
				values[i][j] = v[j];
			}
		}

		return new MicroarraySet(arrayNumber, markerNumber,
				arrayLabels.toArray(new String[0]),
				markerLabels.toArray(new String[0]), values, null);
	}
	
	public static float[][] getValues(MicroarrayDataset microarray) {
		List<String> arrayLabels = microarray.getArrayLabels();
		List<String> markerLabels = microarray.getMarkerLabels();
		int arrayNumber = arrayLabels.size();
		int markerNumber = markerLabels.size();
		List<MicroarrayRow> rows = microarray.getRows();
		float[][] values = new float[markerNumber][arrayNumber];
		for (int i = 0; i < markerNumber; i++) {
			float[] v = rows.get(i).getValueArray();
			for (int j = 0; j < arrayNumber; j++) {
				values[i][j] = v[j];
			}
		}
		return values;
	}
}
