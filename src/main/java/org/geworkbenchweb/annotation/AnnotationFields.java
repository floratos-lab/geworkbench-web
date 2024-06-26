package org.geworkbenchweb.annotation;

import java.io.Serializable;

public class AnnotationFields implements Serializable {
	private static final long serialVersionUID = -3571880185587329070L;

	public String getMolecularFunction() {
		return molecularFunction;
	}

	void setMolecularFunction(String molecularFunction) {
		this.molecularFunction = molecularFunction;
	}

	public String getCellularComponent() {
		return cellularComponent;
	}

	void setCellularComponent(String cellularComponent) {
		this.cellularComponent = cellularComponent;
	}

	public String getBiologicalProcess() {
		return biologicalProcess;
	}

	void setBiologicalProcess(String biologicalProcess) {
		this.biologicalProcess = biologicalProcess;
	}

	String getUniGene() {
		return uniGene;
	}

	void setUniGene(String uniGene) {
		this.uniGene = uniGene;
	}

	public String getDescription() {
		return description;
	}

	void setDescription(String description) {
		this.description = description;
	}

	public String getGeneSymbol() {
		return geneSymbol;
	}

	void setGeneSymbol(String geneSymbol) {
		this.geneSymbol = geneSymbol;
	}

	public String getLocusLink() {
		return locusLink;
	}

	void setLocusLink(String locusLink) {
		this.locusLink = locusLink;
	}

	String getSwissProt() {
		return swissProt;
	}

	void setSwissProt(String swissProt) {
		this.swissProt = swissProt;
	}

	public void setRefSeq(String refSeq) {
		this.refSeq = refSeq;
	}

	public String getRefSeq() {
		return refSeq;
	}

	private String molecularFunction, cellularComponent, biologicalProcess;
	private String uniGene, description, geneSymbol, locusLink, swissProt;
	private String refSeq;
}