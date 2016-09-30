package org.geworkbenchweb.plugins.microarray;

import org.geworkbenchweb.plugins.DataTypeMenuPage;

/** 
 * 'Menu' page for microarray dataset.
 */
public class MicroarrayUI extends DataTypeMenuPage {

	private static final long serialVersionUID = 1L;
	
	public MicroarrayUI(final Long dataSetId) {
		super("List of analysis and visualization modules that can process microarray data sets.", "Microarray Data", org.geworkbenchweb.pojos.MicroarrayDataset.class, dataSetId);
    }

}
