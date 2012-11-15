package org.geworkbenchweb.plugins.tabularview;

import java.util.List;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion;

import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class TabularViewUI extends VerticalLayout {
	
	private static final long serialVersionUID = 1L;
	private static final Object MARKER_HEADER = "Marker";

	public TabularViewUI(Long dataSetId) {
		setSizeFull();
		setImmediate(true);
		PagedTable table = new PagedTable();
		table.setSizeFull();
		table.setImmediate(true);
		table.setStyleName(Reindeer.TABLE_STRONG);
		List<DataSet> data = DataSetOperations.getDataSet(dataSetId);
		DSMicroarraySet maSet = (DSMicroarraySet) ObjectConversion.toObject(data.get(0).getData());
		table.setContainerDataSource(tabularView(maSet));
		table.setColumnWidth(MARKER_HEADER, 150);
		addComponent(table);
		addComponent(table.createControls());
		table.setPageLength(25);
		setExpandRatio(table, 1);
	}
	
	public static IndexedContainer tabularView(DSMicroarraySet maSet) {

		String[] colHeaders = new String[(maSet.size()) + 1];
		IndexedContainer dataIn = new IndexedContainer();

		for (int j = 0; j < maSet.getMarkers().size(); j++) {

			Item item = dataIn.addItem(j);
			for (int k = 0; k <= maSet.size(); k++) {
				if (k == 0) {
					colHeaders[k] = (String) MARKER_HEADER;
					dataIn.addContainerProperty(colHeaders[k], String.class,
							null);
					item.getItemProperty(colHeaders[k]).setValue(
							maSet.getMarkers().get(j).getLabel());
				} else {
					colHeaders[k] = maSet.get(k - 1).toString();
					dataIn.addContainerProperty(colHeaders[k], Float.class,
							null);
					item.getItemProperty(colHeaders[k]).setValue(
							(float) maSet.getValue(j, k - 1));
				}
			}
		}
		return dataIn;
	}
}
