package org.geworkbenchweb.plugins.cnkb;

import org.geworkbenchweb.plugins.Analysis;

public class CNKB implements Analysis {

	@Override
	public String getName() {
		return "Cellular Network Knowledge Base";
	}

	@Override
	public String getDescription() {
		return "The Cellular Network Knowledge Base (CNKB) is a repository of molecular interactions, " +
				"including ones both computationally and experimentally derived. Sources for interactions " +
				"include both publicly available databases such as BioGRID and HPRD, as well as reverse-engineered " +
				"cellular regulatory interactomes developed in the lab of Dr. Andrea Califano at Columbia University.";
	}

}
