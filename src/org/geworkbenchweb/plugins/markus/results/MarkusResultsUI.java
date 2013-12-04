package org.geworkbenchweb.plugins.markus.results;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.MarkUsResult;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;

public class MarkusResultsUI extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = 7444564617594626988L;
	private static final String MARKUS_RESULT_URL = "http://bhapp.c2b2.columbia.edu/MarkUs/cgi-bin/browse.pl?pdb_id=";

	final private Long datasetId;
	
	public MarkusResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) return;
		
		ResultSet resultset = FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
		MarkUsResult markUsResult = FacadeFactory.getFacade().find(MarkUsResult.class, resultset.getDataId());
		String results = markUsResult.getResult();
		// bison MarkUsResultDataSet is ignored on purpose

		Button refreshBtn = new Button("Refresh");
		refreshBtn.setHeight("25px");
		addComponent(refreshBtn);
		setComponentAlignment(refreshBtn, Alignment.TOP_RIGHT);
	
		Embedded browser = new Embedded("", new ExternalResource(MARKUS_RESULT_URL+results));
		browser.setType(Embedded.TYPE_BROWSER);
		browser.setImmediate(true);
		browser.setSizeFull();
		addComponent(browser);
		setWidth("100%");
		setHeight("100%");

		setExpandRatio((Component)browser, 1.0f);			
		refreshBtn.addListener(new RefreshListener(browser));
	}

	private class RefreshListener implements Button.ClickListener{
		private static final long serialVersionUID = 5620460689584816498L;
		private Embedded browser = null;

		public RefreshListener(Embedded b){
			browser = b;
		}
		@Override
		public void buttonClick(ClickEvent event) {
			browser.requestRepaint();
		}
	}

	@Override
	public PluginEntry getPluginEntry() {
		return GeworkbenchRoot.getPluginRegistry().getVisualizerPluginEntry(this.getClass());
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}
}
