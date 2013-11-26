package org.geworkbenchweb.pojos;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="MicroarrayDataset")
public class MicroarrayDataset extends AbstractPojo {

	private static final long serialVersionUID = 7126862943986074859L;
	
	private List<String> markerLabels;
	private List<String> arrayLabels;
	
	@OneToMany(cascade = CascadeType.PERSIST)
	private List<MicroarrayRow> rows;
	
	public MicroarrayDataset(){}

	public MicroarrayDataset(List<String> markerLabels, List<String> arrayLabels, List<MicroarrayRow> rows){
		this.setMarkerLabels(markerLabels);
		this.arrayLabels = arrayLabels;
		this.rows = rows;
	}

	public List<MicroarrayRow> getRows() {
		return rows;
	}

	public void setRows(List<MicroarrayRow> rows) {
		this.rows = rows;
	}

	public List<String> getArrayLabels() {
		return arrayLabels;
	}

	public void setArrayLabels(List<String> arrayLabels) {
		this.arrayLabels = arrayLabels;
	}

	public List<String> getMarkerLabels() {
		return markerLabels;
	}

	public void setMarkerLabels(List<String> markerLabels) {
		this.markerLabels = markerLabels;
	}
}
