package org.geworkbenchweb.genspace.ui.component;

import java.io.Serializable;

import org.geworkbench.components.genspace.server.stubs.UserNetwork;

public class UserNetworkWrapper implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UserNetwork userNetwork;
	
	private int id;
	
	private String name;
	
	public UserNetworkWrapper(UserNetwork userNetwork) {
		this.userNetwork = userNetwork;
		this.id = userNetwork.getId();
		this.name = userNetwork.getNetwork().getName();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public UserNetwork getUserNetwork() {
		return this.userNetwork;
	}
	
	public void setUserNetwork(UserNetwork un) {
		this.userNetwork = un;
	}
}
