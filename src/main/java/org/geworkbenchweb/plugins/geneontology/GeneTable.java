/**
 * 
 */
package org.geworkbenchweb.plugins.geneontology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geworkbenchweb.pojos.GOResult;
import org.geworkbenchweb.utils.GOTerm;
import org.geworkbenchweb.utils.GeneOntologyTree;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

/**
 * @author zji
 *
 */
public class GeneTable extends Table {

	private static final long serialVersionUID = -3857059757022002759L;
	private static final Object HEADER_SYMBOL = "Gene Symbol";
	private static final Object HEADER_DESCRIPTION = "Description";

	private final IndexedContainer container = new IndexedContainer();
	
	private final GOResult result;
	
	GeneTable(GOResult result) {
		this.result = result;
		container.addContainerProperty(HEADER_SYMBOL, String.class, null);
		container.addContainerProperty(HEADER_DESCRIPTION, String.class, null);
		this.setContainerDataSource(container);
	}

	public void addData(int goId) {
		boolean includeDescendants = true; // TODO
		
		Set<Integer> processedTerms = new TreeSet<Integer>();
		Set<String> genes = genesFomrTermAndDescendants(processedTerms, goId, includeDescendants);
		
		for(String g : genes) {
			Item item = container.addItem(g);
			item.getItemProperty(HEADER_SYMBOL).setValue(g);
			item.getItemProperty(HEADER_DESCRIPTION).setValue("detail .... wukong");
		}
	}
	
	private Set<String> genesFomrTermAndDescendants(Set<Integer> processedTerms, int goId, boolean includeDescendants) {
		Set<String> genes = new HashSet<String>();
		Set<String> annotatedGenes = getAnnotatedGenes(goId);
		if(annotatedGenes!=null)
			genes.addAll(annotatedGenes);
		
		if(includeDescendants) {
			List<Integer> children = getOntologyChildren(goId);
			if(children!=null) {
				for(Integer child: children) {
					if(!processedTerms.contains(child)) {
						genes.addAll(genesFomrTermAndDescendants(processedTerms, child, includeDescendants));
					}
				}
			}
		}
		processedTerms.add(goId);
		return genes;
	}
	
	private Set<String> getAnnotatedGenes(int goTermId) {
		return null; //FIXME result.getTerm2Gene().get(goTermId);
	}
	
	/**
	 * return list of child IDs for a GO term ID
	 * @param goTermId
	 * @return
	 */
	static public List<Integer> getOntologyChildren(int goTermId) {
		List<Integer> list = new ArrayList<Integer>();
		
		if(goTermId==0) {
			for(Integer id: getNamespaceIds()) {
				list.add(id);
			}
			return list;
		}

		GeneOntologyTree geneOntologyTree = GeneOntologyTree.getInstanceUntilAvailable();
		for(GOTerm g: geneOntologyTree.getTerm(goTermId).getChildren()) {
			list.add(g.getId());
		}
		return list;
	}
	
	private static Set<Integer> namespaceIds = new TreeSet<Integer>();
	static public  Set<Integer>  getNamespaceIds() {
		if(namespaceIds.size()>0) {
			return namespaceIds;
		} else {
			GeneOntologyTree geneOntologyTree = GeneOntologyTree.getInstanceUntilAvailable();
			for(int i=0; i<geneOntologyTree.getNumberOfRoots(); i++)
				namespaceIds.add(geneOntologyTree.getRoot(i).getId());
			return namespaceIds;
		}
	}

}
