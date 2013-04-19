package org.geworkbenchweb.genspace.ui.component;

import com.vaadin.ui.CustomComponent;

public abstract class AbstractGenspaceTab extends CustomComponent implements GenSpaceTab {
	protected GenSpaceLogin login;
	public AbstractGenspaceTab(GenSpaceLogin login)
	{
		this.login = login;
	}
	@Override
	public GenSpaceLogin getGenspaceLogin() {
		return login;
	}
}
