package org.geworkbenchweb.plugins.aracne;

import org.geworkbenchweb.layout.VisualPlugin;

public class Aracne extends VisualPlugin {

	private Long dataSetId;

	public Aracne(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ARACne";
	}

	@Override
	public String getDescription() {
		return "ARACNe (Algorithm for the Reconstruction of Accurate Cellular Networks) " +
				"\n(Basso 2005, Margolin 2006a, 2006b) is an information-theoretic algorithm used " +
				"\nto identify transcriptional interactions between gene products using microarray " +
				"\ngene expression profile data.\n\n";
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}

}
