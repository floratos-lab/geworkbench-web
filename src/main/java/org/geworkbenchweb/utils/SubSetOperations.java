package org.geworkbenchweb.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.CurrentContext;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.pojos.SubSetContext;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;

/**
 * Purpose of this class is to have all the operations on the subset table
 * @author Nikhil
 */

public class SubSetOperations {

	public static List<AbstractPojo> getMarkerSet(Long setId) {

		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("id", setId);
		parameters.put("type", "marker");

		List<AbstractPojo> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.id=:id " +
				"and p.type=:type", parameters);
		return data;
	}

	public static List<AbstractPojo> getArraySet(Long setId) {

		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("id", setId);
		parameters.put("type", "microarray");

		List<AbstractPojo> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.id=:id " +
				"and p.type=:type", parameters);
		return data;
	}

	public static List<AbstractPojo> getMarkerSets(Long dataSetId) {

		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("parent", dataSetId);
		parameters.put("type", "marker");

		List<AbstractPojo> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.parent=:parent and p.type=:type ", parameters);

		return data;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void storeSignificance(List<String> data, Long dataSetId, Long userId) {
 
		int  significanSetNum = SubSetOperations.getSignificanceSetNum(dataSetId);
		String significanSetName = null;
		if (significanSetNum == 0)
			significanSetName = "Significant Genes ";
		else	 
			significanSetName = "Significant Genes(" + significanSetNum + ") ";
		storeMarkerSetInCurrentContext((ArrayList)data, significanSetName, dataSetId);
	 
	}
	
	public static int getSignificanceSetNum(Long dataSetId) {

		List<SubSet> list = getMarkerSetsForCurrentContext(dataSetId);
		int count = 0;
		if (list != null && list.size() > 0)
		{
			for(SubSet s : list)
			{
				if (s.getName().contains("Significant Genes"))
					count++;
			}
		}
		return count;
		 
	}


	public static List<AbstractPojo> getArraySets(Long dataSetId) {

		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("parent", dataSetId);
		parameters.put("type", "microarray");

		List<AbstractPojo> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.parent=:parent and p.type=:type ", parameters);

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
	 * get all Contexts for dataset by type
	 * @param dataSetId
 	 * @param type
	 * @return all Contexts for dataset by type
	 */
	public static List<Context> getContextsForType(Long dataSetId, String type){
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("datasetid", dataSetId);
		parameters.put("type", type);

		List<Context> contexts = FacadeFactory.getFacade().list("Select a from Context a where a.datasetid=:datasetid and a.type=:type", parameters);

		return contexts;
	}

	/**
	 * get all array Contexts for dataset
	 * @param dataSetId
	 * @return all array Contexts for dataset
	 */
	public static List<Context> getArrayContexts(Long dataSetId){
		return getContextsForType(dataSetId, "microarray");
	}
	
	/**
	 * get all marker Contexts for dataset
	 * @param dataSetId
	 * @return all marker Contexts for dataset
	 */
	public static List<Context> getMarkerContexts(Long dataSetId){
		return getContextsForType(dataSetId, "marker");
	}
	
	/**
	 * get arrays SubSets in current context
	 * @param datasetId
	 * @return all arrays SubSets in context
	 */
	public static List<SubSet> getArraySetsForCurrentContext(long datasetId) {
		return getSubSetsForContext(getCurrentArrayContext(datasetId));
	}

	/**
	 * get markers SubSets in current context
	 * @param datasetId
	 * @return all markers SubSets in context
	 */
	public static List<SubSet> getMarkerSetsForCurrentContext(long datasetId) {
		return getSubSetsForContext(getCurrentMarkerContext(datasetId));
	}

	/**
	 * get SubSets in context
	 * @param context
	 * @return all SubSets in context
	 */
	public static List<SubSet> getSubSetsForContext(Context context) {
		List<SubSet> subsets = new ArrayList<SubSet>();
		if (context == null) return subsets;

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("contextid", context.getId());
		List<SubSetContext> subcontexts = FacadeFactory.getFacade().list("Select a from SubSetContext a where a.contextid=:contextid", parameters);

		for (SubSetContext subcontext : subcontexts){
			SubSet subset = FacadeFactory.getFacade().find(SubSet.class, subcontext.getSubsetId());
			if (subset!=null) subsets.add(subset);
		}
		return subsets;
	}
	
	/**
	 * get current Context for dataset by type
	 * @param datasetId
	 * @param type
	 * @return current Context by type
	 */
	public static Context getCurrentContextForType(long datasetId, String type){
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		parameters.put("datasetid", datasetId);
		parameters.put("type", type);
		List<CurrentContext> cc =  FacadeFactory.getFacade().list("Select p from CurrentContext as p where p.datasetid=:datasetid and p.type=:type", parameters);
		if (cc.isEmpty()) return null;
		return FacadeFactory.getFacade().find(Context.class, cc.get(0).getContextId());
	}
	
	/**
	 * get current array Context for dataset
	 * @param datasetId
	 * @return current array Context
	 */
	public static Context getCurrentArrayContext(long datasetId){
		return getCurrentContextForType(datasetId, "microarray");
	}
	
	/**
	 * get current marker Context for dataset
	 * @param datasetId
	 * @return current maker Context
	 */
	public static Context getCurrentMarkerContext(long datasetId){
		return getCurrentContextForType(datasetId, "marker");
	}
	
	/**
	 * set CurrentContext for dataset by type
	 * @param datasetId
	 * @param context
	 * @param type
	 */
	public static void setCurrentContextForType(long datasetId, Context context, String type){
		if (context == null) return;
		CurrentContext cc = null;

		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		parameters.put("datasetid", datasetId);
		parameters.put("type", type);
		List<CurrentContext> ccs =  FacadeFactory.getFacade().list("Select p from CurrentContext as p where p.datasetid=:datasetid and p.type=:type", parameters);
		if (ccs.isEmpty()){
			cc = new CurrentContext();
			cc.setDatasetId(datasetId);
			cc.setType(type);
		}else{
			cc = ccs.get(0);
			if (cc.getContextId() == context.getId()) return;
		}
		cc.setContextId(context.getId());
		FacadeFactory.getFacade().store(cc);
	}
	
	/**
	 * set current array Context for dataset
	 * @param datasetId
	 * @param context
	 */
	public static void setCurrentArrayContext(long datasetId, Context context){
		setCurrentContextForType(datasetId, context, "microarray");
	}

	/**
	 * set current marker Context for dataset
	 * @param datasetId
	 * @param context
	 */
	public static void setCurrentMarkerContext(long datasetId, Context context){
		setCurrentContextForType(datasetId, context, "marker");
	}

	/**
	 * store arrays SubSet and SubSetContext
	 * @param itemList  names of items in subset
	 * @param name      subset name
	 * @param type		subset type
	 * @param datasetId parent subset id
	 * @param context   context containing this subset
	 * @return SubSet Id
	 */
	public static Long storeSubSetInContext(ArrayList<String> itemList,
			String name, String type, long datasetId, Context context) {
		if (context == null || !type.equals(context.getType())) return null;

		DataSet dataset	=	FacadeFactory.getFacade().find(DataSet.class, datasetId);
		SubSet subset  	= 	new SubSet();

		subset.setName(name);
		subset.setType(type);
		subset.setOwner(dataset.getOwner());
		subset.setParent(datasetId);
		subset.setPositions(itemList);
		FacadeFactory.getFacade().store(subset);

		SubSetContext subcontext = new SubSetContext();
		subcontext.setContextId(context.getId());
		subcontext.setSubsetId(subset.getId());
		FacadeFactory.getFacade().store(subcontext);

		return subset.getId();
	}
	
	/**
	 * store arrays SubSet and SubSetContext
	 * @param arrayList names of arrays in arrayset
	 * @param name      arrayset name
	 * @param datasetId parent dataset id
	 * @param context   context containing this arrayset
	 * @return arrays SubSet Id
	 */
	public static Long storeArraySetInContext(ArrayList<String> arrayList,
			String name, long datasetId, Context context) {
		return storeSubSetInContext(arrayList, name, "microarray", datasetId, context);
	}

	/**
	 * store markers SubSet and SubSetContext
	 * @param markerList names of markers in markerset
	 * @param name       markerset name
	 * @param datasetId  parent dataset id
	 * @param context    context containing this markerset
	 * @return markers SubSet Id
	 */
	public static Long storeMarkerSetInContext(ArrayList<String> markerList,
			String name, long datasetId, Context context) {
		return storeSubSetInContext(markerList, name, "marker", datasetId, context);
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
		return storeArraySetInContext(arrayList, name, datasetId, getCurrentArrayContext(datasetId));
	}

	/**
	 * store markers SubSet and SubSetContext in current context
	 * @param markerList names of markers in arrayset
	 * @param name       markerset name
	 * @param datasetId  parent dataset id
	 * @return markers SubSet Id
	 */
	public static Long storeMarkerSetInCurrentContext(ArrayList<String> markerList,
			String name, long datasetId) {
		return storeMarkerSetInContext(markerList, name, datasetId, getCurrentMarkerContext(datasetId));
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
		List<AbstractPojo> data =  FacadeFactory.getFacade().list("Select p from SubSet as p where p.id=:id", parameters);
		return (SubSet) data.get(0);
	}
	
	
	
	public static ArrayList<String> getArrayData(long setNameId) {

		@SuppressWarnings("rawtypes")
		List subSet = SubSetOperations.getArraySet(setNameId);
		ArrayList<String> positions = (((SubSet) subSet.get(0)).getPositions());

		return positions;
	}
	
	public static ArrayList<String> getMarkerData(long setNameId) {

		@SuppressWarnings("rawtypes")
		List subSet = SubSetOperations.getMarkerSet(setNameId);
		ArrayList<String> positions = (((SubSet) subSet.get(0)).getPositions());
		return positions;
	}
}
