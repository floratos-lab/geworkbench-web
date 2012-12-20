package org.geworkbenchweb.plugins.ttest.results;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class TTestResultsUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	public TTestResultsUI(Long dataSetId) {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("id", dataSetId);
		List<ResultSet> data = FacadeFactory.getFacade().list(
				"Select p from ResultSet as p where p.id=:id", parameters);

		@SuppressWarnings("unchecked")
		DSSignificanceResultSet<DSGeneMarker> sigSet =  (DSSignificanceResultSet<DSGeneMarker>) ObjectConversion.toObject(data
				.get(0).getData());

		
		setImmediate(true);
		setSizeFull();		 
		setWidth("100%");
		Label dataLabel = new Label("Under Construction");
		dataLabel.setStyleName(Reindeer.LABEL_H1);
		addComponent(dataLabel);
		setComponentAlignment(dataLabel, Alignment.MIDDLE_CENTER);
	}
}
