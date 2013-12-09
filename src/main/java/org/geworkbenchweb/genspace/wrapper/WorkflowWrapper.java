package org.geworkbenchweb.genspace.wrapper;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;


import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.server.stubs.WorkflowTool;
import org.geworkbenchweb.genspace.RuntimeEnvironmentSettings;


public class WorkflowWrapper {
	 org.geworkbench.components.genspace.server.stubs.Workflow delegate;
	 public WorkflowWrapper( org.geworkbench.components.genspace.server.stubs.Workflow delegate)
	 {
		 this.delegate = delegate;
	 }
	public int getCachedChildrenCount() {
		return delegate.getCachedChildrenCount();
	}
	public void setCachedChildrenCount(int cachedChildrenCount) {
		delegate.setCachedChildrenCount(cachedChildrenCount);
	}
	public int getCachedParentId() {
		return delegate.getCachedParentId();
	}
	public void setCachedParentId(int cachedParentId) {
		delegate.setCachedParentId(cachedParentId);
	}

	public boolean equals(Object obj) {
		if(obj instanceof Workflow)
		{
			return ((Workflow) obj).getId() == getId();
		}
		return false;
	}
	public List<Workflow> getChildren() {
		return delegate.getChildren();
	}
	public XMLGregorianCalendar getCreatedAt() {
		return delegate.getCreatedAt();
	}
	public User getCreator() {
		return delegate.getCreator();
	}
	public int getId() {
		return delegate.getId();
	}
	public String getIdstr() {
		return delegate.getIdstr();
	}
	public int getNumRating() {
		return delegate.getNumRating();
	}
	public Object getParent() {
		return delegate.getParent();
	}
	public int getSumRating() {
		return delegate.getSumRating();
	}
	public List<Integer> getToolIds() {
		return delegate.getToolIds();
	}
	public List<WorkflowTool> getTools() {
		return delegate.getTools();
	}
	public int getUsageCount() {
		return delegate.getUsageCount();
	}
	public Object getRef() {
		return delegate.getRef();
	}
	public int hashCode() {
		return delegate.hashCode();
	}
	public void setCreatedAt(XMLGregorianCalendar value) {
		delegate.setCreatedAt(value);
	}
	public void setCreator(User value) {
		delegate.setCreator(value);
	}
	public void setId(int value) {
		delegate.setId(value);
	}
	public void setIdstr(String value) {
		delegate.setIdstr(value);
	}
	public void setNumRating(int value) {
		delegate.setNumRating(value);
	}
	public void setParent(Object value) {
		delegate.setParent(value);
	}
	public void setSumRating(int value) {
		delegate.setSumRating(value);
	}
	public void setUsageCount(int value) {
		delegate.setUsageCount(value);
	}
	public void setRef(Object value) {
		delegate.setRef(value);
	}
	public double getAvgRating() {
		return ((double) getSumRating())/((double) getNumRating());
	}
	
	public int getNumComments()
	{
		return delegate.getNumComments();
	}
	
	@Override
	public String toString() {
		String r = "";
		for(WorkflowTool wt : getTools())
		{
			r += wt.getTool().getName() + ", ";
		}
		if(r.length() > 2)
			r = r.substring(0,r.length()-2);
		return r;
	}
//	public void updateRatingsCache()
//	{
//		//TODO make this called automatically
//		int numRating =0;
//		int totalRating =0;
//		for(WorkflowRating tr : getRatings())
//		{
//			numRating++;
//			totalRating += tr.getRating();
//		}
//		setNumRating(numRating);
//		setSumRating(totalRating);
//	}
	
	public ToolWrapper getLastTool()
	{
		return new ToolWrapper(getTools().get(getTools().size() -1).getTool());
	}
	
	public String getLastToolName()
	{
		return this.getLastTool().getName();
	}
	
	public double getOverallRating() {
		if(getNumRating() == 0)
			return 0;
		else
			return (double) getSumRating() / (double) getNumRating();
	}
	
	public void loadToolsFromCache()
	{
		if(getToolIds() != null && RuntimeEnvironmentSettings.tools != null)
		{
			getTools().clear();
			ArrayList<WorkflowTool> ret = new ArrayList<WorkflowTool>();
			int j = 1;
			for(int i : getToolIds())
			{
				WorkflowTool t = new WorkflowTool();
				t.setOrder(j);
				t.setTool(RuntimeEnvironmentSettings.tools.get(i));
				t.setWorkflow(this.delegate);
				ret.add(t);
				j++;
			}
			getTools().addAll(ret);
		}
	}
	
	public org.geworkbench.components.genspace.server.stubs.Workflow getDelegate() {
		return delegate;
	}
}


