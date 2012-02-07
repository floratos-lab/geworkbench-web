package org.geworkbenchweb.analysis.hierarchicalclustering.client.ui.components.bioheatmap;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import org.geworkbenchweb.analysis.hierarchicalclustering.client.ui.server.HeatMapModelFactory;

@SuppressWarnings("unused")
public class BioHeatMapModel implements IsSerializable{

	private List<HeatMapColumn> columns;

	public BioHeatMapModel() {
	}
	
	public BioHeatMapModel(List<HeatMapColumn> columns) {
		this.columns = columns;
	}
	
	public List<HeatMapColumn> getColumns() {
		return columns;
	}
	
	
	
	/**Returns sum of all values for all cells in the matrix.*/
	public double getTotal() {
		double total=0;
		for(HeatMapColumn column: columns) {
			for(HeatMapValue value: column.getValues()) {
				total += value.getCellValue();
			}
		}
		return total;
	}
	
	public static class HeatMapColumn implements IsSerializable{

		private String label;
		private List<HeatMapValue> values;

		public HeatMapColumn() {}
		
		public HeatMapColumn(String label, List<HeatMapValue> values) {
			this.label = label;
			this.values = values;			
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public List<HeatMapValue> getValues() {
			return values;
		}
		
		/**Returns sum of all values for all cells in the matrix.*/
		public double getTotal() {
			double total=0;	
				for(HeatMapValue value: values) {
					total += value.getCellValue();
				}
			return total;
		}
	}

	public static class HeatMapValue implements IsSerializable {

		private double rowValue;
		private double cellValue;

		public HeatMapValue() {
		}
		
		public HeatMapValue(double rowValue, double cellValue) {
			this.rowValue = rowValue;
			this.cellValue = cellValue;
		}

		public double getRowValue() {
			return rowValue;
		}

		public double getCellValue() {
			return cellValue;
		}

		public void setCellValue(double cellValue) {
			this.cellValue = cellValue;
		}

		public void setRowValue(double rowValue) {
			this.rowValue = rowValue;
		}
		
	}
}
