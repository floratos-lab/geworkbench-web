/**
 * 
 */
package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

/**
 * @author zji
 * 
 */
@Entity
@Table(name="AnnotationEntry")
public class AnnotationEntry extends AbstractPojo {
	private static final long serialVersionUID = 1981567155538167500L;
	
	private String probeSetId;
	private String geneSymbol;
	private String geneDescription;
	private String entrezId;
	
	public AnnotationEntry() {
	}
	
	public AnnotationEntry(String probeSetId, String geneSymbol,
			String geneDescription, String entrezId) {
		this.probeSetId = probeSetId;
		this.geneSymbol = geneSymbol;
		this.geneDescription = geneDescription;
		this.entrezId = entrezId;
	}

	public String getGeneDescription() {
		return geneDescription;
	}
	
	public void setGeneDescription(String geneDescription) {
		this.geneDescription = geneDescription;
	}
	
	public String getGeneSymbol() {
		return geneSymbol;
	}
	
	public void setGeneSymbol(String geneSymbol) {
		this.geneSymbol = geneSymbol;
	}
	
	public String getProbeSetId() {
		return probeSetId;
	}
	
	public void setProbeSetId(String probeSetId) {
		this.probeSetId = probeSetId;
	}

	public String getEntrezId() {
		return entrezId;
	}

	public void setEntrezId(String entrezId) {
		this.entrezId = entrezId;
	}
}
