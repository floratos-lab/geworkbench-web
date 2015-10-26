package org.geworkbenchweb.plugins.cnkb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geworkbenchweb.utils.GOTerm;
import org.geworkbenchweb.utils.GeneOntologyTree;

/**
 * All CNKB results for ONE queried marker. It is uniquely identified by the markerLabel AND interactome+version.
 * 
 * This is based on the class from geWorkbench core that has the same name.
 * Dependency on bison types is removed; name is kept to avoid too much immediate change of other code.
 */
public class CellularNetWorkElementInformation implements java.io.Serializable {

	private static final long serialVersionUID = -4163326138016520667L;

	private final String markerLabel;
	private final String interactome;
	
	private final InteractionDetail[] interactionDetails;
	private double threshold;

	private final int[] molecularFunctionGoIds;
	private final int[] biologicalProcessGoIds;
	
	public CellularNetWorkElementInformation(String markerLabel, String interactome, int[] molecularFunctionGoIds, int[] biologicalProcessGoIds,
			List<InteractionDetail> arrayList) {
		this.markerLabel = markerLabel;
		this.interactome = interactome;

		this.biologicalProcessGoIds = biologicalProcessGoIds;
		this.molecularFunctionGoIds = molecularFunctionGoIds;

		if (arrayList != null && arrayList.size() > 0) {
			interactionDetails = new InteractionDetail[arrayList.size()];
			
			for(int i=0; i<arrayList.size(); i++) {
				interactionDetails[i] = arrayList.get(i);
			}
		} else { /* when there is no result back from CNKB db */
			interactionDetails = null;
		}
	}

	public ArrayList<InteractionDetail> getSelectedInteractions(
			List<String> interactionIncludedList, short selectedConfidenceType) {
		ArrayList<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();
		if (interactionDetails != null && interactionDetails.length > 0) {
			for (int i = 0; i < interactionDetails.length; i++) {
				InteractionDetail interactionDetail = interactionDetails[i];
				if (interactionDetail != null
						&& interactionDetail.getConfidenceValue(selectedConfidenceType) >= threshold) {
					if (interactionIncludedList.contains(interactionDetail
							.getInteractionType())) {
						arrayList.add(interactionDetail);
					}

				}
			}
		}
		return arrayList;
	}

	public ArrayList<InteractionDetail> getSelectedInteractions(
			String interactionType, short selectedConfidenceType) {
		ArrayList<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();
		if (interactionDetails != null && interactionDetails.length > 0) {
			for (int i = 0; i < interactionDetails.length; i++) {
				InteractionDetail interactionDetail = interactionDetails[i];
				if (interactionDetail != null
						&& interactionDetail.getConfidenceValue(selectedConfidenceType) >= threshold) {
					if (interactionType.equals(interactionDetail
							.getInteractionType())) {
						arrayList.add(interactionDetail);
					}

				}
			}
		}
		return arrayList;
	}

	public void setThreshold(double threshold) {
			this.threshold = threshold; 
	}

	public String getMarkerLabel() {
		return markerLabel;
	}

	public String getInteractome() {
		return interactome;
	}

	String getGoInfoStr() {
		if (GeneOntologyTree.getInstance()==null)
			 return  "pending";
		else if( biologicalProcessGoIds == null || biologicalProcessGoIds.length ==0 )
			 return "---";	
		
		String goInfoStr = "";
		
		/* get all GO terms */
		Set<GOTerm> set = null;
		GeneOntologyTree tree = GeneOntologyTree.getInstance();
		 
		if (tree != null) {
			set = new HashSet<GOTerm>();
			for (int goId : biologicalProcessGoIds) {				 
			    if (tree.getTerm(goId) != null)
				   set.add(tree.getTerm(goId));
				 
			}
		}

		if (set != null)
		{
			for (GOTerm goTerm : set) 
		    goInfoStr += goTerm.getName() + "; ";			 
		} 
		return goInfoStr;
	}
 
	String getGeneType() {
		if (GeneOntologyTree.getInstance()==null)
			 return  "pending";
		else if( molecularFunctionGoIds == null || molecularFunctionGoIds.length ==0 )
			 return "---";		 
		return checkMarkerFunctions();
	}
	
	double getThreshold()
	{
		return this.threshold;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CellularNetWorkElementInformation) {
			return markerLabel.equals(
							((CellularNetWorkElementInformation) obj)
									.markerLabel);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return markerLabel.hashCode();
	}

	/* this method is moved here from GeneOntologyUtil */
	private String checkMarkerFunctions() {
		final String KINASE = "K";
		final String TF = "TF";
		final String PHOSPATASE = "P";
		final int KINASE_GOTERM_ID = 16301;
		final int TF_GOTERM_ID = 3700;
		final int PHOSPATASE_GOTERM_ID = 4721;
		
		for (int goId : molecularFunctionGoIds) {					 
				for(GOTerm goterm: CellularNetWorkElementInformation.getAncestors(goId)) {
					int gotermId = goterm.getId();
					if (gotermId==KINASE_GOTERM_ID) {
						return KINASE;
				    } else if (gotermId==TF_GOTERM_ID) {
						return TF;
					} else if (gotermId==PHOSPATASE_GOTERM_ID) {
					    return PHOSPATASE;
					}
				}				 
				 
		}
		
		return ""; // all other cases
	}

	/* The following two methods are moved from GenOntologyTree to here because no other code uses them. */
	/**
	 * Gets all the ancestor terms for the term with the given ID. By
	 * definition, a term is an ancestor of itself.
	 */
	private static Set<GOTerm> getAncestors(int id) {
		GeneOntologyTree tree = GeneOntologyTree.getInstance();
		HashSet<GOTerm> set = new HashSet<GOTerm>();
		getAncestorsHelper(tree.getTerm(id), set);
		return set;
	}

	private static void getAncestorsHelper(GOTerm term, Set<GOTerm> set) {
		if (term != null) {
			set.add(term);
			GOTerm[] parents = term.getParents();
			for (GOTerm parent : parents) {
				getAncestorsHelper(parent, set);
			}
		}else{
			//System.out.println("EMPTY GOTERM ID:" + term);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("CellularNetWorkElementInformation of ");
		sb.append(markerLabel);
		if(interactionDetails!=null) {
			sb.append(" with ").append(interactionDetails.length).append(" InteractionDetail's: ");
			for(InteractionDetail detail : interactionDetails) {
				sb.append('|').append(detail);
			}
		}
		return sb.toString();
	}
}
