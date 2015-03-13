package org.geworkbenchweb.plugins.geneontology;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.GOResult;
import org.geworkbenchweb.pojos.GOResultRow;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class GOResultUI  extends VerticalLayout implements Visualizer {

	private static Log log = LogFactory.getLog(GOResultUI.class);
			
	private static final long serialVersionUID = 1781213075723503210L;

	final private Long datasetId;

	private static final String[] namespaces = {"All", "Molecular Function", "Biological Process", "Cellular Component"};
	
	private static final String[] GENE_FOR_OPTIONS = {"Term", "Term and its descendants"};
	static final String[] GENE_FROM_OPTIONS = {"Changed gene list", "Rerefence list"};
	
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

		final GeneTable geneTable = new GeneTable(result, resultSet.getParent());
		geneTable.setWidth("500px");
		
		final Table table= new Table();
		table.setSelectable(true);
		table.setImmediate(true);

		final OptionGroup geneForSelect = new OptionGroup("Show Genes For", Arrays.asList(GENE_FOR_OPTIONS));
		final OptionGroup geneFromSelect = new OptionGroup("Show Genes From", Arrays.asList(GENE_FROM_OPTIONS));
		geneForSelect.setValue(GENE_FOR_OPTIONS[0]);
		geneForSelect.addStyleName("horizontal");
		geneForSelect.setImmediate(true);
		geneForSelect.addListener(new Table.ValueChangeListener() {

			private static final long serialVersionUID = -7615528381968321116L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Integer goId = (Integer) table.getValue();
				if(goId==null) return;
				String f = (String)event.getProperty().getValue();
				boolean d = f.equals(GENE_FOR_OPTIONS[1]);
				geneTable.updateData(goId , d, (String)geneFromSelect.getValue());
			}
			
		});
		geneFromSelect.setValue(GENE_FROM_OPTIONS[0]);
		geneFromSelect.addStyleName("horizontal");
		geneFromSelect.setImmediate(true);
		geneFromSelect.addListener(new Table.ValueChangeListener() {

			private static final long serialVersionUID = -7615528381968321116L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				String newFrom = (String)event.getProperty().getValue();
				Integer goId = (Integer) table.getValue();
				if(goId==null) return;
				String f = (String)geneForSelect.getValue();
				boolean d = f.equals(GENE_FOR_OPTIONS[1]);
				geneTable.updateData(goId, d, newFrom);
			}
			
		});
		
		final IndexedContainer c = new IndexedContainer();
		fillContainer(c, result);
		table.setContainerDataSource(c);
		table.addListener(new Table.ValueChangeListener() {
			private static final long serialVersionUID = -1643400715266392213L;

			public void valueChange(ValueChangeEvent event) {
				Integer goId = (Integer)event.getProperty().getValue();
				if(goId==null) return; // unselect
				String f = (String)geneForSelect.getValue();
				boolean d = f.equals(GENE_FOR_OPTIONS[1]);
				geneTable.updateData(goId, d, (String)geneFromSelect.getValue());
            }
		});
		
		setSizeFull();
		
		HorizontalLayout mainLayout = new HorizontalLayout();
		VerticalLayout leftLayout = new VerticalLayout();
		VerticalLayout rightLayout = new VerticalLayout();
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		mainLayout.addComponent(leftLayout);
		mainLayout.addComponent(rightLayout);
		
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
		
		leftLayout.setSpacing(true);
		leftLayout.addComponent(namespaceSelect);
		leftLayout.addComponent(table);
		leftLayout.addComponent( geneForSelect );
		leftLayout.addComponent( geneFromSelect );
		leftLayout.addComponent(geneTable);
		
		rightLayout.addComponent(new SingleTermView());

		addComponent(mainLayout);
	}
	
	private static final String HEADER_ID = "GO:ID";
	private static final String HEADER_NAME = "Name";
	private static final String HEADER_NAMESPACE = "Namespace";
	private static final String HEADER_P_VALUE = "P-value";
	private static final String HEADER_ADJUSTED_P_VALUE = "Adjusted P-value";
	private static final String HEADER_POPULATION_COUNT = "Population count";
	private static final String HEADER_STUDY_COUNT = "Study count";
	
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
