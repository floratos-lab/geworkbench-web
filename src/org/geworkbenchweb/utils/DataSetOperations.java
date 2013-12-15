package org.geworkbenchweb.utils;

import java.util.List;

import org.geworkbenchweb.dataset.MicroarraySet;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.MicroarrayRow;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/**
 * Purpose of this class is to have all the operations on the DataSet table
 * 
 * @author Nikhil
 */

public class DataSetOperations {

	public static DataSet getDataSet(Long dataSetId) {
		DataSet data = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		return data;
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
