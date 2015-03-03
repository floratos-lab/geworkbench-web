/**
 * 
 */
package org.geworkbenchweb.plugins.geneontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.AnnotationEntry;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.GOResult;
import org.geworkbenchweb.utils.GOTerm;
import org.geworkbenchweb.utils.GeneOntologyTree;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;

/**
 * @author zji
 *
 */
public class GeneTable extends Table {
	private static Log log = LogFactory.getLog(GeneTable.class);
			
	private static final long serialVersionUID = -3857059757022002759L;
	private static final Object HEADER_SYMBOL = "Gene Symbol";
	private static final Object HEADER_DESCRIPTION = "Description";

	private final IndexedContainer container = new IndexedContainer();
	
	private final GOResult result;
	private final Map<String, String> details;
	
	GeneTable(GOResult result, final Long parentId) {
		this.result = result;
		container.addContainerProperty(HEADER_SYMBOL, String.class, null);
		container.addContainerProperty(HEADER_DESCRIPTION, String.class, null);
		this.setContainerDataSource(container);
		
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("dataSetId", parentId);
		DataSetAnnotation dataSetAnnotation = FacadeFactory.getFacade().find(
				"SELECT d FROM DataSetAnnotation AS d WHERE d.datasetid=:dataSetId", parameter);
		details = new HashMap<String, String>();
		if(dataSetAnnotation!=null) {
			Long annotationId = dataSetAnnotation.getAnnotationId();
			Annotation annotation = FacadeFactory.getFacade().find(Annotation.class, annotationId);
			for(AnnotationEntry entry : annotation.getAnnotationEntries()) {
				details.put(entry.getGeneSymbol(), entry.getGeneDescription());
			}
		} else {
			log.debug("annotation is null for dataset "+parentId);
		}
	}

	public void updateData(int goId, String geneFrom) {
		boolean includeDescendants = true; // TODO
		
		Set<Integer> processedTerms = new TreeSet<Integer>();
		Set<String> genes = genesFomrTermAndDescendants(processedTerms, goId, includeDescendants);
		if(geneFrom.equals(GOResultUI.GENE_FROM_OPTIONS[0])) {
			genes.retainAll(result.getChangedGenes());
		} else if(geneFrom.equals(GOResultUI.GENE_FROM_OPTIONS[1])) {
			genes.retainAll(result.getReferenceGenes());
		}
		
		boolean r= container.removeAllItems();
		log.debug("cleaned-up? "+r);
		for(String g : genes) {
			Item item = container.addItem(g);
			item.getItemProperty(HEADER_SYMBOL).setValue(g);
			item.getItemProperty(HEADER_DESCRIPTION).setValue( details.get(g) );
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
		return result.getTerm2Gene().get(goTermId);
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
