package org.geworkbenchweb.dataset;

import java.io.File;

import org.geworkbenchweb.pojos.DataSet;

/** Data set loader. */
public interface Loader {
		
	// TODO return or indicate the type of data set thus created
	public abstract void load(File file, DataSet dataset) throws GeWorkbenchLoaderException;
}
