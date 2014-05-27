package org.geworkbenchweb.genspace.ui.component;

import com.vaadin.ui.CustomComponent;

public abstract class AbstractGenspaceTab extends CustomComponent implements GenSpaceTab {
	protected GenSpaceLogin_1 login;
	public AbstractGenspaceTab(GenSpaceLogin_1 login2)
	{
		this.login = login2;
	}
	@Override
	public GenSpaceLogin_1 getGenspaceLogin() {
		return login; 
	}
}
