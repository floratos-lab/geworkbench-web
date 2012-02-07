package org.geworkbenchweb.analysis.hierarchicalclustering.client.ui.server;

import java.util.ArrayList;
import java.util.List;

import org.geworkbenchweb.analysis.hierarchicalclustering.client.ui.components.bioheatmap.BioHeatMapModel;
import org.geworkbenchweb.analysis.hierarchicalclustering.client.ui.components.bioheatmap.BioHeatMapModel.HeatMapColumn;
import org.geworkbenchweb.analysis.hierarchicalclustering.client.ui.components.bioheatmap.BioHeatMapModel.HeatMapValue;


public class HeatMapModelFactory {

	public static BioHeatMapModel createHeatMap(BioHeatMapModel ds, double min, double max) {

		List<HeatMapColumn> columns = new ArrayList<HeatMapColumn>();
		
		for (int columnIndex = 0; columnIndex <  ds.getColumns().size(); columnIndex++) {
			HeatMapColumn column = ds.getColumns().get(columnIndex);
			
			/**Create 100 empty values*/
			List<HeatMapValue> values = new ArrayList<HeatMapValue>();
			for(int rowIndex=0;rowIndex<101;rowIndex++) {
				double rowValue = ((double)rowIndex/100  * (max - min) + min);
				values.add(new HeatMapValue(rowValue, 0));
			}	
			
			/**Reduce input values.*/
			for (int rowIndex=0;rowIndex<column.getValues().size();rowIndex++) {
				double prob = 1 / column.getValues().get(rowIndex).getRowValue();
				if (prob >= min && prob <= max) {
					double scaledProb = (prob - min) / (max - min);
					HeatMapValue value = values.get((int) (scaledProb * 100));
					value.setCellValue(value.getCellValue() + column.getValues().get(rowIndex).getCellValue());
				}
			}
			
			columns.add(new HeatMapColumn( column.getLabel(), values));
		}
		
		BioHeatMapModel heatMapModel = new BioHeatMapModel(columns );
		return heatMapModel;
	}
	

	public static BioHeatMapModel delta(BioHeatMapModel heatMapModel1, BioHeatMapModel heatMapModel2) {
		
		if(heatMapModel2.getColumns().size()!= heatMapModel1.getColumns().size()) {
			throw new IllegalArgumentException("Number of columns in both models is not the same.");
		}
	
		List<HeatMapColumn> columns = new ArrayList<HeatMapColumn>();
		for(int columnIndex=0;columnIndex<heatMapModel2.getColumns().size();columnIndex++) {
			HeatMapColumn model2Column = heatMapModel2.getColumns().get(columnIndex);
			HeatMapColumn model1Column = heatMapModel1.getColumns().get(columnIndex);
			
			if(model2Column.getValues().size()!=model1Column.getValues().size()) {
				throw new IllegalArgumentException("Number of values in both columns for both models is not the same.");
			}
			
			List<HeatMapValue> values = new ArrayList<HeatMapValue>();
			for(int valueIndex=0;valueIndex<model2Column.getValues().size();valueIndex++) {
				HeatMapValue model2Value = model2Column.getValues().get(valueIndex);
				HeatMapValue model1Value = model1Column.getValues().get(valueIndex);
					
				values.add(new HeatMapValue(model2Value.getRowValue(),model2Value.getCellValue() - model1Value.getCellValue()));
			}
			
			columns.add(new HeatMapColumn(model2Column.getLabel(),values));
		}
		BioHeatMapModel delta = new BioHeatMapModel(columns);
		return delta;
	}

}
