package org.geworkbenchweb.utils;

import org.geworkbenchweb.pojos.DataSet;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/**
 * Purpose of this class is to have all the operations on the DataSet table
 * @author Nikhil
 */

public class DataSetOperations {

	public static DataSet getDataSet(Long dataSetId) {
		DataSet data = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		return data;
	}
}
