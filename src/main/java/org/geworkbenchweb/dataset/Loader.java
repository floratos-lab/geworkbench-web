package org.geworkbenchweb.dataset;

import java.io.File;

import org.geworkbenchweb.pojos.DataSet;

/** Data set loader. */
// this should be an interface now.
public abstract class Loader {
		
	/**
	 * parse the file and store it (make it persistent)
	 * 
	 * @throws GeWorkbenchLoaderException
	 */
	// TODO return or indicate the type of data set thus created
	public abstract void load(File file, DataSet dataset) throws GeWorkbenchLoaderException;
}
