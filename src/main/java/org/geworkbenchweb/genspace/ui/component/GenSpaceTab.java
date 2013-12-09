package org.geworkbenchweb.genspace.ui.component;

import org.geworkbenchweb.genspace.ui.GenSpacePluginView;

public interface GenSpaceTab {
	public void tabSelected();
	public void loggedIn();
	public void loggedOut();
	public GenSpaceLogin_1 getGenspaceLogin();
}
