/**
 * 
 */
package org.geworkbenchweb.plugins;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;

import com.vaadin.ui.ComponentContainer;

/**
 * The parameter UI for an analysis plug-in.
 * 
 * @author zji
 *
 */
public interface AnalysisUI extends ComponentContainer {
	/**
	 * Set the current dataset Id and update the parameter options that depend on it.
	 * 
	 * @param dataId
	 */
	// TODO using an arbitrary, machine-generated ID to identify the dataset may not be the best approach 
	void setDataSetId(Long dataId);
	
	// TODO this eventually (or conceptually) should be part of Analysis, not AnalysisUI
	Class<?> getResultType();
	// TODO this is not clean design either, just easier to improve the overall design
	// return the result data note's name
	String execute(Long resultId, DSDataSet<?> dataset, HashMap<Serializable, Serializable> parameters) throws IOException;
}
