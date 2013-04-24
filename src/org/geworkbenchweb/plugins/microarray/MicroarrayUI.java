package org.geworkbenchweb.plugins.microarray;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.plugins.DataTypeMenuPage;
import org.geworkbenchweb.plugins.DataTypeUI;

/** 
 * 'Menu' page for microarray dataset.
 */
public class MicroarrayUI extends DataTypeMenuPage implements DataTypeUI {

	private static final long serialVersionUID = 1L;
	
	public MicroarrayUI(final Long dataSetId) {
		super("Microarray Description", "Microarray Data", DSMicroarraySet.class, dataSetId);
    }

}
