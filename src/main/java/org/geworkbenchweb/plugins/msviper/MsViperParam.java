package org.geworkbenchweb.plugins.msviper;

import java.io.Serializable; 
import java.util.HashMap;
import java.util.Map;

public class MsViperParam implements Serializable {

	private static final long serialVersionUID = 935291015959638487L;

	private String context = "";
	private Map<String, String> classCase = new HashMap<String, String>();
	private Map<String, String> classControl = new HashMap<String, String>();
	private String caseGroups = "";
	private String controlGroups = "";
	private String network = "";

	private boolean gesfilter = true;
	private int minAllowedRegulonSize = 25;
	private boolean bootstrapping = false;
	private String method = "mean";
	private boolean shadow = false;
	private float shadowValue = 25;

	public void reset() {

		classCase = new HashMap<String, String>();
		classControl = new HashMap<String, String>();
		network = "";
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getContext() {
		return  context.trim().replaceAll("[^A-Za-z0-9]", "_") ;
	}

	public void setClassCase(Map<String, String> classCase) {
		this.classCase = classCase;
	}

	public Map<String, String> getClassCase() {
		return classCase;
	}

	public void setClassControl(Map<String, String> classControl) {
		this.classControl = classControl;
	}

	public Map<String, String> getClassControl() {
		return this.classControl;
	}

	public void setCaseGroup(String caseGroups) {
		this.caseGroups = caseGroups;
	}

	public String getCaseGroups() {

		return "\"" + this.caseGroups + "\"";

	}

	public void setControlGroup(String controlGroups) {
		this.controlGroups = controlGroups;
	}

	public String getControlGroups() {

		return "\"" + this.controlGroups + "\"";

	}

	public void setNetwork(String network) {
		this.network = network.replace(" ", "");
	}

	public String getNetwork() {
		return network;
	}

	public void setGesfilter(boolean value) {
		gesfilter = value;
	}

	public Boolean getGesFilter() {
		return gesfilter;
	}

	public void setMinAllowedRegulonSize(Integer n) {
		minAllowedRegulonSize = n;
	}

	public Integer getMinAllowedRegulonSize() {
		return minAllowedRegulonSize;
	}

	public void setBootstrapping(boolean bootstrapping) {
		this.bootstrapping = bootstrapping;
	}

	public Boolean getBootstrapping() {
		return bootstrapping;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return this.method;
	}

	public void setShadow(boolean shadow) {
		this.shadow = shadow;
	}

	public Boolean getShadow() {
		return shadow;
	}

	public void setShadowValue(Float shadowPValue) {
		this.shadowValue = shadowPValue;
	}

	public float getShadowValue() {
		return shadowValue;
	}

}