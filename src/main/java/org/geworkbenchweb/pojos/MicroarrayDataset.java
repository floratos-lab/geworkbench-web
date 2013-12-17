package org.geworkbenchweb.pojos;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.geworkbenchweb.dataset.MicroarraySet;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="MicroarrayDataset")
public class MicroarrayDataset extends AbstractPojo {

	private static final long serialVersionUID = 7126862943986074859L;
	
	private String[] markerLabels;
	private String[] arrayLabels;
	private int markerNumber;
	private int arrayNumber;
	
	@OneToMany(cascade = CascadeType.PERSIST)
	private List<MicroarrayRow> rows;
	
	public MicroarrayDataset(){}

	public MicroarrayDataset(MicroarraySet microarraySet) {
		this.markerLabels = microarraySet.markerLabels;
		this.arrayLabels = microarraySet.arrayLabels;
		this.markerNumber = microarraySet.markerNumber;
		this.arrayNumber = microarraySet.arrayNumber;

		rows = new ArrayList<MicroarrayRow>();
		for(int i=0; i<markerNumber; i++) {
			float[] rowValues = microarraySet.values[i];
			MicroarrayRow row = new MicroarrayRow(rowValues);
			rows.add(row);
		}
	}

	public List<MicroarrayRow> getRows() {
		return rows;
	}

	public void setRows(List<MicroarrayRow> rows) {
		this.rows = rows;
	}

	public String[] getArrayLabels() {
		return arrayLabels;
	}

	public void setArrayLabels(String[] arrayLabels) {
		this.arrayLabels = arrayLabels;
	}

	public int getArrayNumber() {
		return arrayNumber;
	}

	public void setArrayNumber(int arrayNumber) {
		this.arrayNumber = arrayNumber;
	}

	public String[] getMarkerLabels() {
		return markerLabels;
	}

	public void setMarkerLabels(String[] markerLabels) {
		this.markerLabels = markerLabels;
	}

	public int getMarkerNumber() {
		return markerNumber;
	}

	public void setMarkerNumber(int markerNumber) {
		this.markerNumber = markerNumber;
	}

	/* convert the expression values to a 2-d array */
	public float[][] getExpressionValues() {
		float[][] values = new float[markerNumber][arrayNumber];
		for(int i=0; i<markerNumber; i++) {
			float[] row = rows.get(i).getValueArray();
			for(int j=0; j<arrayNumber; j++) {
				values[i][j] = row[j];
			}
		}
		return values;
	}
}
