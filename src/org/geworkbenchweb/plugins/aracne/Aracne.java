package org.geworkbenchweb.plugins.aracne;

import org.geworkbenchweb.plugins.Analysis;

public class Aracne implements Analysis {

	@Override
	public String getName() {
		return "ARACNe";
	}

	@Override
	public String getDescription() {
		return "ARACNe (Algorithm for the Reconstruction of Accurate Cellular Networks) " +
				"\n(Basso 2005, Margolin 2006a, 2006b) is an information-theoretic algorithm used " +
				"\nto identify transcriptional interactions between gene products using microarray " +
				"\ngene expression profile data.\n\n";
	}
}
