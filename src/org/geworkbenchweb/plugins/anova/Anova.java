package org.geworkbenchweb.plugins.anova;

import org.geworkbenchweb.layout.VisualPlugin;
import org.geworkbenchweb.plugins.Analysis;

// FIXME like other implementation classes of VisualPlugin, this needs to be fixed together with the design of VisualPlugin
public class Anova extends VisualPlugin implements Analysis {

	private Long dataSetId;
	
	public Anova(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	
	@Override
	public String getName() {
		return "ANOVA";
	}

	@Override
	public Long getDataSetId() {
		return dataSetId;
	}

	@Override
	public String getDescription() {
		return "The geWorkbench ANOVA component implements a one-way analysis of variance calculation " +
				"derived from TIGR's MeV (MultiExperiment Viewer) (Saeed, 2003). At least three groups of " +
				"arrays must be specified by defining and activating them in the Arrays/Phenotypes component.";
	}

	@Override
	public boolean checkForVisualizer() {
		return false;
	}
	
}
