package org.geworkbenchweb.plugins.lincs;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;

public class LINCS extends Embedded {

	private static final long serialVersionUID = -4804209648411124987L;

	public LINCS() {
		super(null, new ExternalResource("http://geworkbench.c2b2.columbia.edu/lincsweb/"));
		this.setType(Embedded.TYPE_BROWSER);
		this.setSizeFull();
	}
}
