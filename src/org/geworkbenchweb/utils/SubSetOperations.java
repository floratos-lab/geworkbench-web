package org.geworkbenchweb.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.CurrentContext;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.pojos.SubSetContext;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/**
 * Purpose of this class is to have all the operations on the subset table
 * @author Nikhil
 */

public class SubSetOperations {


	public static boolean storeData(ArrayList<String> arrayList, String setType,
			String name, long l) {

		SubSet subset  	= 	new SubSet();

		subset.setName(name);
		subset.setType(setType);
		subset.setOwner(SessionHandler.get().getId());
		subset.setParent(l);
		subset.setPositions(arrayList);

		try {
			FacadeFactory.getFacade().store(subset);
		} catch (Exception e) {
			return false;	
		}

		return true;
	}

	public static List<?> getMarkerSet(Long setId) {

		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("id", setId);
		parameters.put("type", "marker");

		List<?> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.id=:id " +
				"and p.type=:type", parameters);
		return data;
	}

	public static List<?> getArraySet(Long setId) {

		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("id", setId);
		parameters.put("type", "microarray");

		List<?> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.id=:id " +
				"and p.type=:type", parameters);
		return data;
	}

	public static List<?> getMarkerSets(Long dataSetId) {

		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("parent", dataSetId);
		parameters.put("type", "marker");

		List<?> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.parent=:parent and p.type=:type ", parameters);

		return data;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void storeSignificance(List<String> data, Long dataSetId, Long userId) {
 
		int  significanSetNum = SubSetOperations.getSignificanceSetNum(dataSetId);
		 
		SubSet subset  	= 	new SubSet();
		if (significanSetNum == 0)
		   subset.setName("Significant Genes ");
		else	 
		   subset.setName("Significant Genes(" + significanSetNum + ") ");
		 
		subset.setOwner(userId);
		subset.setType("marker");
	    subset.setParent(dataSetId);
	    subset.setPositions((ArrayList)data);
	    FacadeFactory.getFacade().store(subset);
	}
	
	public static int getSignificanceSetNum(Long dataSetId) {

		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("parent", dataSetId);
		parameters.put("type", "marker");
		parameters.put("name", "Significant Genes%");
		                         
		List<?> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.parent=:parent and p.type=:type and p.name like :name", parameters);

		if (data != null)	  
			return data.size();
		else
			return 0;
	}


	public static List<?> getArraySets(Long dataSetId) {

		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("parent", dataSetId);
		parameters.put("type", "microarray");

		List<?> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.parent=:parent and p.type=:type ", parameters);

		return data;
	}

	/**
	 * get all Contexts for dataset
	 * @param dataSetId
	 * @return all Contexts for dataset
	 */
	public static List<Context> getAllContexts(Long dataSetId){
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("datasetid", dataSetId);

		List<Context> contexts = FacadeFactory.getFacade().list("Select a from Context a where a.datasetid=:datasetid", parameters);

		return contexts;
	}
	
	/**
	 * get arrays SubSets in current context
	 * @param datasetId
	 * @return all arrays SubSets in context
	 */
	public static List<SubSet> getArraySetsForCurrentContext(long datasetId) {
		return getArraySetsForContext(getCurrentContext(datasetId));
	}

	/**
	 * get arrays SubSets in context
	 * @param context
	 * @return all arrays SubSets in context
	 */
	public static List<SubSet> getArraySetsForContext(Context context) {
		List<SubSet> arraysets = new ArrayList<SubSet>();
		if (context == null) return arraysets;

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("contextid", context.getId());
		List<SubSetContext> subcontexts = FacadeFactory.getFacade().list("Select a from SubSetContext a where a.contextid=:contextid", parameters);

		for (SubSetContext subcontext : subcontexts){
			SubSet arrayset = FacadeFactory.getFacade().find(SubSet.class, subcontext.getSubsetId());
			if (arrayset!=null) arraysets.add(arrayset);
		}
		return arraysets;
	}
	
	/**
	 * get current Context for dataset
	 * @param datasetId
	 * @return current Context
	 */
	public static Context getCurrentContext(long datasetId){
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		parameters.put("datasetid", datasetId);
		List<CurrentContext> cc =  FacadeFactory.getFacade().list("Select p from CurrentContext as p where p.datasetid=:datasetid", parameters);
		if (cc.isEmpty()) return null;
		return FacadeFactory.getFacade().find(Context.class, cc.get(0).getContextId());
	}
	
	/**
	 * set CurrentContext for dataset
	 * @param datasetId
	 * @param context
	 */
	public static void setCurrentContext(long datasetId, Context context){
		if (context == null) return;
		CurrentContext cc = null;

		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		parameters.put("datasetid", datasetId);
		List<CurrentContext> ccs =  FacadeFactory.getFacade().list("Select p from CurrentContext as p where p.datasetid=:datasetid", parameters);
		if (ccs.isEmpty()){
			cc = new CurrentContext();
			cc.setDatasetId(datasetId);
		}else{
			cc = ccs.get(0);
			if (cc.getContextId() == context.getId()) return;
		}
		cc.setContextId(context.getId());
		FacadeFactory.getFacade().store(cc);
	}

	/**
	 * store arrays SubSet and SubSetContext
	 * @param arrayList names of arrays in arrayset
	 * @param name      arrayset name
	 * @param datasetId parent dataset id
	 * @param contextId context containing this arrayset
	 * @return arrays SubSet Id
	 */
	public static Long storeArraySetInContext(ArrayList<String> arrayList,
			String name, long datasetId, long contextId) {

		SubSet subset  	= 	new SubSet();

		subset.setName(name);
		subset.setType("microarray");
		subset.setOwner(SessionHandler.get().getId());
		subset.setParent(datasetId);
		subset.setPositions(arrayList);
		FacadeFactory.getFacade().store(subset);

		SubSetContext subcontext = new SubSetContext();
		subcontext.setContextId(contextId);
		subcontext.setSubsetId(subset.getId());
		FacadeFactory.getFacade().store(subcontext);

		return subset.getId();
	}
	
	/**
	 * store arrays SubSet and SubSetContext in current context
	 * @param arrayList names of arrays in arrayset
	 * @param name      arrayset name
	 * @param datasetId parent dataset id
	 * @return arrays SubSet Id
	 */
	public static Long storeArraySetInCurrentContext(ArrayList<String> arrayList,
			String name, long datasetId) {
		return storeArraySetInContext(arrayList, name, datasetId, getCurrentContext(datasetId).getId());
	}

	/**
	 * store markers SubSet
	 * @param arrayList names of markers in markerset
	 * @param name      markerset name
	 * @param datasetId parent dataset id
	 * @return markers SubSet Id
	 */
	public static Long storeMarkerSet(ArrayList<String> arrayList,
			String name, long datasetId) {

		SubSet subset  	= 	new SubSet();

		subset.setName(name);
		subset.setType("marker");
		subset.setOwner(SessionHandler.get().getId());
		subset.setParent(datasetId);
		subset.setPositions(arrayList);
		FacadeFactory.getFacade().store(subset);

		return subset.getId();
	}
	/**
	 * This method is used to delete all the Marker and Array sets for given dataSet
	 * @input dataSet ID
	 * 
	 */
	public static void deleteAllSets(Long dataSetId) {



	}

	public static SubSet getSubSet(Long subSetId) {

		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		parameters.put("id", subSetId);
		List<?> data =  FacadeFactory.getFacade().list("Select p from SubSet as p where p.id=:id", parameters);
		return (SubSet) data.get(0);
	}
}
