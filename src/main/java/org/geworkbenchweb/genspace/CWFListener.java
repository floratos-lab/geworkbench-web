package org.geworkbenchweb.genspace;

import org.geworkbench.components.genspace.server.stubs.Workflow;

public interface CWFListener {

	public abstract void cwfUpdated(Workflow workflow);
}