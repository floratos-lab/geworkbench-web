package org.geworkbenchweb.pojos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
public class GOResult extends AbstractPojo {

	private static final long serialVersionUID = 5458449478601448594L;

	private java.sql.Timestamp timestamp;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name="GORESULT_GORESULTROW", joinColumns=@JoinColumn(name="ID"))
	@MapKeyColumn(name="GO_ID", table="GORESULT_GORESULTROW")
	private Map<Integer, GOResultRow> result = new HashMap<Integer, GOResultRow>();
	
	private Map<Integer, Set<String>> term2Gene;

	private Set<String> referenceGenes;
	private Set<String> changedGenes;

	public GOResult() {}
	
	public GOResult(Map<Integer, GOResultRow> result, Map<Integer, Set<String>> term2Gene, Set<String> referenceGenes, Set<String> changedGenes) {
		this.result = result;
		this.term2Gene = term2Gene;
		this.referenceGenes = referenceGenes;
		this.changedGenes = changedGenes;
	}
	
	public Map<Integer, GOResultRow> getResult() {
		return result;
	}
	public void setResult(Map<Integer, GOResultRow> result) {
		this.result = result;
	}
	public java.sql.Timestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(java.sql.Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public Map<Integer, Set<String>> getTerm2Gene() {
		return term2Gene;
	}

	public void setTerm2Gene(Map<Integer, Set<String>> term2Gene) {
		this.term2Gene = term2Gene;
	}

	public Set<String> getReferenceGenes() {
		return referenceGenes;
	}

	public void setReferenceGenes(Set<String> referenceGenes) {
		this.referenceGenes = referenceGenes;
	}

	public Set<String> getChangedGenes() {
		return changedGenes;
	}

	public void setChangedGenes(Set<String> changedGenes) {
		this.changedGenes = changedGenes;
	}
}
