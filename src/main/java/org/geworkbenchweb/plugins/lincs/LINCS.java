package org.geworkbenchweb.plugins.lincs;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Embedded;

public class LINCS extends Embedded {

	private static final long serialVersionUID = -4804209648411124987L;

	public LINCS() {
		super(null, new ExternalResource("/lincsweb/")); // deployed in the same tomcat
		this.setType(Embedded.TYPE_BROWSER);
		this.setSizeFull();
	}
}
