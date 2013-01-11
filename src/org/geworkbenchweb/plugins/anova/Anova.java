package org.geworkbenchweb.plugins.anova;

import org.geworkbenchweb.plugins.Analysis;

public class Anova implements Analysis {

	@Override
	public String getName() {
		return "ANOVA";
	}

	@Override
	public String getDescription() {
		return "The geWorkbench ANOVA component implements a one-way analysis of variance calculation " +
				"derived from TIGR's MeV (MultiExperiment Viewer) (Saeed, 2003). At least three groups of " +
				"arrays must be specified by defining and activating them in the Arrays/Phenotypes component.";
	}
	
}
