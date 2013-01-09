package org.geworkbenchweb.plugins.tabularview;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.UserDirUtils;

import com.host900.PaginationBar.PaginationBar;
import com.host900.PaginationBar.PaginationBarListener;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * Displays Tabular View for Microarray Data.
 * For the begining display it shows 50 markers and then loaded
 * using lazy loading principle as per user request.
 * Uses PaginationBar Addon from Vaadin.
 * @author Nikhil
 */
public class TabularViewUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	private static final Object MARKER_HEADER = "Marker";

	private PaginationBarListener paginationBarListener;
	private PaginationBar paginationBar;
	
	private DSMicroarraySet maSet;
	private int pageSize = 50;
	
	public TabularViewUI(Long dataSetId) {
		setSizeFull();
		setImmediate(true);

		final Table table = new Table();
		table.setSizeFull();
		table.setImmediate(true);
		table.setStyleName(Reindeer.TABLE_STRONG);

		DataSet data 	= 	DataSetOperations.getDataSet(dataSetId);
		maSet 			= 	(DSMicroarraySet) ObjectConversion.toObject(UserDirUtils.getDataSet(data.getId()));

		table.setContainerDataSource(tabularView(1));
		table.setColumnWidth(MARKER_HEADER, 150);

		addComponent(table);
		setExpandRatio(table, 1);

		paginationBarListener=new PaginationBarListener() {
			@Override
			public void pageRequested(int pageIndexRequested) {
				table.setContainerDataSource(tabularView(pageIndexRequested));
			}
		};

		paginationBar=new PaginationBar((int) Math.ceil((double) maSet.getMarkers().size()/(double) pageSize), paginationBarListener);

		HorizontalLayout pageBar=paginationBar.createPagination();
		addComponent(pageBar);
	}

	/**
	 * Method is called everytime user wants to to see more items in the table.
	 * Implements lazy loading principle.
	 * @param pageIndex
	 * @return IndexedContainer with Table Items
	 */
	public IndexedContainer tabularView(int pageIndex) {

		String[] colHeaders = new String[(maSet.size()) + 1];
		IndexedContainer dataIn = new IndexedContainer();

		/* Last page might not have all 50 elements */
		int flag = maSet.getMarkers().size() + 1;;
		if(pageIndex == (int) Math.ceil((double) maSet.getMarkers().size()/(double) pageSize)) {
			flag = maSet.getMarkers().size();
		}
		for(int i=((pageIndex-1)*pageSize+1);i<=(pageIndex-1)*pageSize+pageSize;i++){
			Item item = dataIn.addItem(i-1);
			for (int k = 0; k <= maSet.size(); k++) {
				if (k == 0) {
					colHeaders[k] = (String) MARKER_HEADER;
					dataIn.addContainerProperty(colHeaders[k], String.class,
							null);
					item.getItemProperty(colHeaders[k]).setValue(
							maSet.getMarkers().get(i-1).getLabel());
				} else {
					colHeaders[k] = maSet.get(k - 1).toString();
					dataIn.addContainerProperty(colHeaders[k], Float.class,
							null);
					item.getItemProperty(colHeaders[k]).setValue(
							(float) maSet.getValue(i-1, k - 1));
				}
			}
			if (i == flag) break;
		}
		return dataIn;
	}
}
