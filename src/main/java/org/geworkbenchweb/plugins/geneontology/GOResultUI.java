package org.geworkbenchweb.plugins.geneontology;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.GOResult;
import org.geworkbenchweb.pojos.GOResultRow;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.Application;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

public class GOResultUI  extends VerticalLayout implements Visualizer {

	private static Log log = LogFactory.getLog(GOResultUI.class);
			
	private static final long serialVersionUID = 1781213075723503210L;

	final private Long datasetId;

	private static final String[] namespaces = {"All", "Molecular Function", "Biological Process", "Cellular Component"};
	
	public GOResultUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) {
			log.debug("dataset ID is null");
			return;
		}
		
		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
		Long id = resultSet.getDataId();
		if(id==null) { // pending node
			addComponent(new Label("Pending computation - ID "+ dataSetId));
			return;
		}
		GOResult result = FacadeFactory.getFacade().find(GOResult.class, id);

		final SingleTermView singleTermView = new SingleTermView();

		final Table table= new Table();
		table.setSelectable(true);
		table.setImmediate(true);

		final GenePanel genePanel = new GenePanel(table, result, resultSet.getParent());
		
		final IndexedContainer c = new IndexedContainer();
		fillContainer(c, result);
		table.setContainerDataSource(c);
		table.addListener(new Table.ValueChangeListener() {
			private static final long serialVersionUID = -1643400715266392213L;

			public void valueChange(ValueChangeEvent event) {
				Integer goId = (Integer)event.getProperty().getValue();
				if(goId==null) return; // unselect
				genePanel.update(goId);
				singleTermView.updateDataSource(goId);
            }
		});
		table.setSizeFull();
		
		this.setSizeFull();
		
		final VerticalSplitPanel mainLayout = new VerticalSplitPanel();
		
		OptionGroup namespaceSelect = new OptionGroup("GO subontology (Namespaces)", Arrays.asList(namespaces ));
		namespaceSelect.addStyleName("horizontal");
		namespaceSelect.setImmediate(true);
		namespaceSelect.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = -4086755829181855195L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				c.removeAllContainerFilters();
				String ns = (String)event.getProperty().getValue();
				if(!ns.equalsIgnoreCase("All")) {
					Filter filter = new SimpleStringFilter(HEADER_NAMESPACE, ns.substring(0,1), true, true);
					c.addContainerFilter(filter);
				}
			}
			
		});
		
		MenuBar menuBar =  new MenuBar();
		menuBar.setStyleName("transparent");
		menuBar.addItem("Export", new Command() {

			private static final long serialVersionUID = 4134110157930322533L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				/*
				 * use the container instead of the original result so the
				 * export is filtered the same way as on GUI
				 */
				List<OutputRow> list = new ArrayList<OutputRow>();
				for (Object id : c.getItemIds()) {
					Item item = c.getItem(id);
					list.add(OutputRow.getInstance(item));
				}
				Collections.sort(list);
				
				final Application app = getApplication();
				String dir = GeworkbenchRoot.getBackendDataDirectory()
						+ System.getProperty("file.separator")
						+ SessionHandler.get().getUsername()
						+ System.getProperty("file.separator") + "export";
				if (!new File(dir).exists())
					new File(dir).mkdirs();

				final File file = new File(dir, "GO_RESULT_"
						+ System.currentTimeMillis() + ".csv");
				PrintWriter pw = null;
				try {
					pw = new PrintWriter(new FileWriter(file));
					pw.println(HEADER_ID + "," + HEADER_NAME + ","
							+ HEADER_NAMESPACE + "," + HEADER_P_VALUE + ","
							+ HEADER_ADJUSTED_P_VALUE + ","
							+ HEADER_POPULATION_COUNT + ","
							+ HEADER_STUDY_COUNT);
					for (OutputRow row : list) {
						pw.println(row);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (pw != null)
						pw.close();
				}
				Resource resource = new FileResource(file, app);
				app.getMainWindow().open(resource);
			}
		}).setStyleName("plugin");
		
		VerticalLayout topLayout = new VerticalLayout();
		topLayout.setSpacing(true);
		topLayout.addComponent(menuBar);
		topLayout.addComponent(namespaceSelect);
		topLayout.addComponent(table);
		
		final HorizontalSplitPanel bottomLayout = new HorizontalSplitPanel();
		bottomLayout.addComponent(genePanel);
		bottomLayout.addComponent(singleTermView);

		mainLayout.addComponent(topLayout);
		mainLayout.addComponent(bottomLayout);
		addComponent(mainLayout);
	}
	
	static final String HEADER_ID = "GO:ID";
	static final String HEADER_NAME = "Name";
	static final String HEADER_NAMESPACE = "Namespace";
	static final String HEADER_P_VALUE = "P-value";
	static final String HEADER_ADJUSTED_P_VALUE = "Adjusted P-value";
	static final String HEADER_POPULATION_COUNT = "Population count";
	static final String HEADER_STUDY_COUNT = "Study count";
	
	private static void fillContainer(IndexedContainer container,
			GOResult result) {
		container.addContainerProperty(HEADER_ID, String.class, null);
		container.addContainerProperty(HEADER_NAME, String.class, null);
		container.addContainerProperty(HEADER_NAMESPACE, String.class, null);
		container.addContainerProperty(HEADER_P_VALUE, Double.class, null);
		container.addContainerProperty(HEADER_ADJUSTED_P_VALUE, Double.class, null);
		container.addContainerProperty(HEADER_POPULATION_COUNT, Integer.class, null);
		container.addContainerProperty(HEADER_STUDY_COUNT, Integer.class, null);
		Map<Integer, GOResultRow> map = result.getResult();
		for (Integer id : map.keySet()) {
			GOResultRow row = map.get(id);
			Item item = container.addItem(id);
			item.getItemProperty(HEADER_ID).setValue(id);
			item.getItemProperty(HEADER_NAME).setValue(row.getName());
			item.getItemProperty(HEADER_NAMESPACE).setValue(row.getNamespace());
			item.getItemProperty(HEADER_P_VALUE).setValue(row.getP());
			item.getItemProperty(HEADER_ADJUSTED_P_VALUE).setValue(row.getpAdjusted());
			item.getItemProperty(HEADER_POPULATION_COUNT).setValue(row.getPopCount());
			item.getItemProperty(HEADER_STUDY_COUNT).setValue(row.getStudyCount());
		}
		// container.sort(new Object[] { "... proper name ..." },
		// new boolean[] { true });
	}
	
	@Override
	public Long getDatasetId() {
		return datasetId;
	}
}
