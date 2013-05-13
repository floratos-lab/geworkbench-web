package org.geworkbenchweb.plugins.markus.results;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.geworkbench.bison.datastructure.bioobjects.structure.MarkUsResultDataSet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.utils.UserDirUtils;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class MarkusResultsUI extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = 7444564617594626988L;
	private static final String MARKUS_RESULT_URL = "http://bhapp.c2b2.columbia.edu/MarkUs/cgi-bin/browse.pl?pdb_id=";

	final private Long datasetId;
	
	public MarkusResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) return;
		
		Object object = null;
		try {
			object = UserDirUtils.deserializeResultSet(dataSetId);
		} catch (FileNotFoundException e) { 
			// TODO pending node should be designed and implemented explicitly as so, eventually
			// let's make a naive assumption for now that "file not found" means pending computation
			addComponent(new Label("Pending computation - ID "+ dataSetId));
			return;
		} catch (IOException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			return;
		} catch (ClassNotFoundException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			return;
		}
		if(! (object instanceof MarkUsResultDataSet)) {
			String type = null;
			if(object!=null) type = object.getClass().getName();
			addComponent(new Label("Result (ID "+ dataSetId+ ") has wrong type: "+type));
			return;
		}
		MarkUsResultDataSet resultset = (MarkUsResultDataSet) object;
		String results = resultset.getResult();

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
