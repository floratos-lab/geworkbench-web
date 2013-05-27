package org.geworkbenchweb.genspace.ui.component;

import org.geworkbench.components.genspace.server.stubs.UserNetwork;
import org.geworkbenchweb.genspace.wrapper.UserWrapper;

public class UserNetworkReqWrapper {
	
	private GenSpaceLogin login;
	
	private UserNetwork un;
	
	private UserWrapper uw;
	
	private String name;
	
	private int id;
	
	public UserNetworkReqWrapper(UserNetwork un, GenSpaceLogin login) {
		this.un = un;
		this.uw = new UserWrapper(un.getUser(), login);
		this.name = this.un.getNetwork().getName();
		this.id = this.un.getId();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
}
