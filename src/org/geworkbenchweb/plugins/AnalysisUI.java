/**
 * 
 */
package org.geworkbenchweb.plugins;

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
}
