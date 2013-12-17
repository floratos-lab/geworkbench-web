package org.geworkbenchweb.genspace.wrapper;



public class ToolWrapper {
	org.geworkbench.components.genspace.server.stubs.Tool delegate;
	
	public org.geworkbench.components.genspace.server.stubs.Tool getDelegate() {
		return delegate;
	}
	public ToolWrapper(org.geworkbench.components.genspace.server.stubs.Tool delegate)
	{
		this.delegate = delegate;
	}
	
	public String getDescription() {
		return delegate.getDescription();
	}

	public void setDescription(String description) {
		delegate.setDescription(description);
	}

	public int getId() {
		return delegate.getId();
	}

	public void setId(int id) {
		delegate.setId(id);
	}

	public String getMostCommonParameters() {
		return delegate.getMostCommonParameters();
	}

	public void setMostCommonParameters(String mostCommonParameters) {
		delegate.setMostCommonParameters(mostCommonParameters);
	}

	public int getMostCommonParametersCount() {
		return delegate.getMostCommonParametersCount();
	}

	public void setMostCommonParametersCount(int mostCommonParametersCount) {
		delegate.setMostCommonParametersCount(mostCommonParametersCount);
	}

	public String getName() {
		return delegate.getName();
	}

	public void setName(String name) {
		delegate.setName(name);
	}

	public int getNumRating() {
		return delegate.getNumRating();
	}

	public void setNumRating(int numRating) {
		delegate.setNumRating(numRating);
	}


	public int getSumRating() {
		return delegate.getSumRating();
	}

	public void setSumRating(int sumRating) {
		delegate.setSumRating(sumRating);
	}

	public int getUsageCount() {
		return delegate.getUsageCount();
	}

	public void setUsageCount(int usageCount) {
		delegate.setUsageCount(usageCount);
	}

	public int getWfCountHead() {
		return delegate.getWfCountHead();
	}

	public void setWfCountHead(int wfCountHead) {
		delegate.setWfCountHead(wfCountHead);
	}

	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}


	public int hashCode() {
		return delegate.getName().hashCode();
	}

	public String toString() {
		return getName();
	}
		
	public double getOverallRating() {
		if(getNumRating() == 0)
			return 0;
		else
			return (double) getSumRating() / (double) getNumRating();
	}
	
	public void incrementUsageCount() {
		setUsageCount(getUsageCount() + 1);
	}
}
