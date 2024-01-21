package org.geworkbenchweb.plugins;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import com.vaadin.ui.ComponentContainer;

/**
 * UI for an analysis plug-in.
 */
public interface AnalysisUI extends ComponentContainer {
	/**
	 * Set the current dataset Id and update the parameter options that depend on
	 * it.
	 * 
	 * @param dataId
	 */
	void setDataSetId(Long dataId);

	// TODO this eventually (or conceptually) should be part of Analysis, not
	// AnalysisUI
	Class<?> getResultType();

	// TODO return String is not the best idea
	String execute(Long resultId, HashMap<Serializable, Serializable> parameters,
			Long userId) throws IOException, Exception;
}
