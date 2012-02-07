package org.geworkbenchweb.analysis.hierarchicalclustering.client.ui.components.bioheatmap;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;

import org.geworkbenchweb.analysis.hierarchicalclustering.client.ui.components.bioheatmap.BioHeatMapModel.HeatMapColumn;
import org.geworkbenchweb.analysis.hierarchicalclustering.client.ui.visualizations.BioHeatMap;
import org.geworkbenchweb.analysis.hierarchicalclustering.client.ui.visualizations.BioHeatMap.Options;

public class BioHeatMapPanel extends Composite {

	/** Data model for BioHeatMap visualisation.*/
	private DataTable dataModel;
	/** Options for BioHeatMap visualisation.*/
	private Options options;

	/** Bio heat map visualisation object. */
	private BioHeatMap bioHeatMap;

	public BioHeatMapPanel(BioHeatMapModel heatMapModel) {
		
		options = BioHeatMap.Options.create();
		options.setCellWidth(20);
		options.setCellHeight(4);
		options.setNumberOfColors(256);

		dataModel = createDataModel(heatMapModel);
		bioHeatMap = new BioHeatMap(dataModel, options);

		initWidget(bioHeatMap);
	}

	/** Updates bioheatmap panel with new data. */
	public void update(BioHeatMapModel bioHeatMapModel) {
		
		int numOfRows = bioHeatMapModel.getColumns().get(0).getValues().size();
		int numOfColumns =  bioHeatMapModel.getColumns().size();
		
		if(numOfRows > dataModel.getNumberOfRows()) {
			throw new IllegalArgumentException("Can't update bio heat map. New numOfRows is bigger than numOfRows in data model.");
		}
		if(numOfColumns > dataModel.getNumberOfColumns()) {
			throw new IllegalArgumentException("Can't update bio heat map. New numOfColumns is bigger than numOfColumns in data model.");
		}
		
		for (int rowIndex = 0; rowIndex < numOfRows; rowIndex++) {
			for (int columnIndex = 0; columnIndex <numOfColumns; columnIndex++) {
				dataModel.setValue(rowIndex, columnIndex + 1, bioHeatMapModel.getColumns().get(columnIndex).getValues().get(rowIndex).getCellValue());
			}
		}

		bioHeatMap.draw(dataModel, options);
	}

	/**
	 * Creates data model for bioHeatMap visualisation.
	 * 
	 * @param heatMapModel
	 *            
	 * @return
	 */
	private DataTable createDataModel(BioHeatMapModel heatMapModel) {

		DataTable data = DataTable.create();
		data.addColumn(ColumnType.STRING, "Y_label");
		for (HeatMapColumn column: heatMapModel.getColumns()) {
			data.addColumn(ColumnType.NUMBER, column.getLabel());
		}
		int numOfRows = heatMapModel.getColumns().get(0).getValues().size();
		data.addRows(numOfRows);
		for (int rowIndex = 0; rowIndex < numOfRows; rowIndex++) {
			String yAxisLabel = rowIndex % 5 == 0 ? "" + heatMapModel.getColumns().get(0).getValues().get(rowIndex).getRowValue() : "";
			data.setValue(rowIndex, 0, yAxisLabel);
			for (int columnIndex = 0; columnIndex < heatMapModel.getColumns().size(); columnIndex++) {
				data.setValue(rowIndex, columnIndex + 1, heatMapModel.getColumns().get(columnIndex).getValues().get(rowIndex).getCellValue());
			}
		}

		return data;
	}

}
