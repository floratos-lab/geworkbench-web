package org.geworkbenchweb.plugins.markus.results;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.bioobjects.structure.MarkUsResultDataSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;

public class MarkusResultsUI extends VerticalLayout{

	private static final long serialVersionUID = 7444564617594626988L;
	private static final String MARKUS_RESULT_URL = "http://bhapp.c2b2.columbia.edu/MarkUs/cgi-bin/browse.pl?pdb_id=";

	public MarkusResultsUI(Long dataSetId) {
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		parameters.put("id", dataSetId);
		List<ResultSet> data = FacadeFactory.getFacade().list("Select p from ResultSet as p where p.id=:id", parameters);
		MarkUsResultDataSet resultset = (MarkUsResultDataSet)ObjectConversion.toObject(data.get(0).getData());
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
}
