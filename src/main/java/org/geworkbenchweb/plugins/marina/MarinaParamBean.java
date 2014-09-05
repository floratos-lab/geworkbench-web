package org.geworkbenchweb.plugins.marina;

import java.io.Serializable;

public class MarinaParamBean implements Serializable{

	private static final long serialVersionUID = -2555236882500050764L;
	private Integer minimumTargetNumber = 20;
	private Integer minimumSampleNumber = 7;
	private Integer gseaPermutationNumber = 1000;
	private Integer gseaTailNumber = 2;
	private Double shadowPValue = 0.01;
	private Double synergyPValue = 0.01;
	private Double gseaPValue = 0.01;
	private String retrievePriorResultWithId = "";
	private String class1 = "";
	private String class2 = "";
	private String network = "";

	public void reset(){
		minimumTargetNumber = 20;
		minimumSampleNumber = 7;
		gseaPermutationNumber = 1000;
		gseaTailNumber = 2;
		shadowPValue = 0.01;
		synergyPValue = 0.01;
		gseaPValue = 0.01;
		retrievePriorResultWithId = "";
		class1 = "";
		class2 = "";
		network = "";
	}

	public void setRetrievePriorResultWithId(String n){
		retrievePriorResultWithId = n;
	}
	public String getRetrievePriorResultWithId(){
		return retrievePriorResultWithId;
	}
	public void setClass1(String n){
		class1 = n;
	}
	public String getClass1(){
		return class1;
	}
	public void setClass2(String n){
		class2 = n;
	}
	public String getClass2(){
		return class2;
	}
	public void setNetwork(String n){
		network = n;
	}
	public String getNetwork(){
		return network;
	}
	public void setMinimumTargetNumber(Integer n){
		minimumTargetNumber = n;
	}

	public Integer getMinimumTargetNumber(){
		return minimumTargetNumber;
	}

	public void setMinimumSampleNumber(Integer n){
		minimumSampleNumber = n;
	}

	public Integer getMinimumSampleNumber(){
		return minimumSampleNumber;
	}

	public void setGseaPermutationNumber(Integer n){
		gseaPermutationNumber = n;
	}

	public Integer getGseaPermutationNumber(){
		return gseaPermutationNumber;
	}

	public void setGseaTailNumber(Integer n){
		gseaTailNumber = n;
	}

	public Integer getGseaTailNumber(){
		return gseaTailNumber;
	}

	public void setShadowPValue(Double n){
		shadowPValue = n;
	}

	public Double getShadowPValue(){
		return shadowPValue;
	}

	public void setSynergyPValue(Double n){
		synergyPValue = n;
	}

	public Double getSynergyPValue(){
		return synergyPValue;
	}

	public void setGseaPValue(Double n){
		gseaPValue = n;
	}

	public Double getGseaPValue(){
		return gseaPValue;
	}
}
