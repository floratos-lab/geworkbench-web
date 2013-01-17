package org.geworkbenchweb.plugins.ttest;

import org.geworkbenchweb.plugins.Analysis;

public class TTest implements Analysis {

	@Override
	public String getName() {
		return "Differential Expression (T-Test)";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "A t-Test analysis can be used to identify markers with statistically " +
				"significant differential expression between two sets of microarrays.";
	}
}
