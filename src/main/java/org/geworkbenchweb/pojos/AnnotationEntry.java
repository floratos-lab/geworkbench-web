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
	private int[] biologicalProcess;
	private int[] cellularComponent;
    private int[] molecularFunction;	
	
	public AnnotationEntry() {
	}
	
	public AnnotationEntry(String probeSetId, String geneSymbol,
			String geneDescription, String entrezId, int[] biologicalProcess, int[] cellularComponent, int[] molecularFunction) {
		this.probeSetId = probeSetId;
		this.geneSymbol = geneSymbol;
		this.geneDescription = geneDescription;
		this.entrezId = entrezId;
		this.biologicalProcess = biologicalProcess;
		this.cellularComponent = cellularComponent;
		this.molecularFunction = molecularFunction;
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
	
	public int[] getBiologicalProcess() {
		return biologicalProcess;
	}

	public void setBiologicalProcess(int[] biologicalProcess) {
		this.biologicalProcess = biologicalProcess;
	}
	
	public int[] getCellularComponent() {
		return cellularComponent;
	}

	public void setCellularComponent(int[] cellularComponent) {
		this.cellularComponent = cellularComponent;
	}
	
	public int[] getMolecularFunction() {
		return molecularFunction;
	}

	public void setMolecularFunction(int[] molecularFunction) {
		this.molecularFunction = molecularFunction;
	}
}
