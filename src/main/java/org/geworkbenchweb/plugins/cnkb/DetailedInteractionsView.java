/**
 * 
 */
package org.geworkbenchweb.plugins.cnkb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.AnnotationEntry;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.visualizations.InteractionColorMosaic;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Window;

/**
 * @author zji
 *
 */
public class DetailedInteractionsView extends Window {

	private static final long serialVersionUID = -4712749272144439069L;

	final InteractionDetailTableView tableview = new InteractionDetailTableView();

	private static final String TABLE_VIEW = "Table View";
	private static final String COLOR_MOSAIC_VIEW = "Color Mosaic View";
			
	public DetailedInteractionsView(final Vector<CellularNetWorkElementInformation> hits, String gene,
			String markerLabel, Long resultDatasetId, final Map<String, String> confidentTypeMap) {
		this.setModal(true);
		this.setClosable(true);
		((AbstractOrderedLayout) this.getContent()).setSpacing(true);
		this.setResizable(true);
		this.setCaption("Interaction Details");
		this.setImmediate(true);

		Map<String, String> map = getGeneSymbolToDescriptionMap(resultDatasetId);

		this.addComponent(new Label("<b>Query Gene Symbol</b>: " + gene, Label.CONTENT_XHTML));
		this.addComponent(new Label("<b>Description</b>: " + map.get(gene), Label.CONTENT_XHTML));
		this.addComponent(
				new Label("<b>Entrez Gene</b>: <a href='http://www.ncbi.nlm.nih.gov/gene?cmd=Search&term=" + gene
						+ "' target='_blank'>linkout</a>   <b>Gene Cards</b>: <a href='http://www.genecards.org/cgi-bin/carddisp.pl?gene="
						+ gene + "&alias=yes' target='_blank'>linkout</a>", Label.CONTENT_XHTML));

		List<String> views = Arrays.asList(new String[] { TABLE_VIEW, COLOR_MOSAIC_VIEW });

		OptionGroup viewSelect = new OptionGroup("Views", views);
		viewSelect.setImmediate(true);
		viewSelect.select(TABLE_VIEW);
		this.addComponent(viewSelect);
		Map<String, Map<String, InteractionDetail>> targetGeneInfo = getTargetGenes(markerLabel, hits);
		final Component colormosaicview = new InteractionColorMosaic(targetGeneInfo);
		this.addComponent(tableview);
		
		viewSelect.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 3816899447811925504L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Property property = event.getProperty();
				Object value = property.getValue();
				if(value.equals(TABLE_VIEW)) {
					DetailedInteractionsView.this.removeComponent(colormosaicview);
					DetailedInteractionsView.this.addComponent(tableview);
				} else if(value.equals(COLOR_MOSAIC_VIEW)) {
					DetailedInteractionsView.this.removeComponent(tableview);
					DetailedInteractionsView.this.addComponent(colormosaicview);
				}
			}
			
		});

		tableview.setTargetGeneData(targetGeneInfo , confidentTypeMap, map);
		tableview.setSizeFull();

		this.setWidth("50%");;
		this.setHeight("50%");;
	}
	
	/* detailed information of target genes to be shown in the detail table view */
	static private Map<String, Map<String, InteractionDetail>> getTargetGenes(String markerLabel,
			final Vector<CellularNetWorkElementInformation> hits) {
		Map<String, Map<String, InteractionDetail>> target = new HashMap<String, Map<String, InteractionDetail>>();
		for (CellularNetWorkElementInformation c : hits) {
			String label = c.getMarkerLabel();
			if (markerLabel.equals(label)) {
				String interactome = c.getInteractome();
				// let's take care of the possible empty cases of the existing results
				if(interactome==null) interactome = "NULL";
				else if(interactome.trim().length()==00) interactome = "EMPTY";
				InteractionDetail[] interactionDetail = c.getAllInteractionDetails();
				if(interactionDetail==null) break; 
				for (InteractionDetail interaction : interactionDetail) {
					for (InteractionParticipant p : interaction.getParticipantList()) {
						String g = p.getGeneName();
						Map<String, InteractionDetail> map = target.get(g);
						if(map==null) {
							map = new HashMap<String, InteractionDetail>();
							target.put(g, map);
						}
						map.put(interactome, interaction);
					}
				}
			}
		}
		return target;
	}

	static private Map<String, String> getGeneSymbolToDescriptionMap(Long resultDataSetId) {
		Map<String, String> map = new HashMap<String, String>();
		if(resultDataSetId==null) {  // using default annotation
			Annotation a = DataSetOperations.getDefaultAnnotation();
			for (AnnotationEntry entry : a.getAnnotationEntries()) {
				// geneSymbol ~ description
				map.put(entry.getGeneSymbol(), entry.getGeneDescription());
			}
			return map;
		}

		ResultSet resultDataset = FacadeFactory.getFacade().find(ResultSet.class, resultDataSetId);
		Long parentId = resultDataset.getParent();

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("dataSetId", parentId);
		DataSetAnnotation dataSetAnnotation = FacadeFactory.getFacade()
				.find("SELECT d FROM DataSetAnnotation AS d WHERE d.datasetid=:dataSetId", parameters);
		if (dataSetAnnotation != null) {
			Long annotationId = dataSetAnnotation.getAnnotationId();
			Map<String, Object> pm = new HashMap<String, Object>();
			pm.put("id", annotationId);
			List<?> entries = FacadeFactory.getFacade().list(
					"SELECT entries.geneSymbol, entries.geneDescription FROM Annotation a JOIN a.annotationEntries entries WHERE a.id=:id",
					pm);
			for (Object entry : entries) {
				Object[] obj = (Object[]) entry;
				// geneSymbol ~ description
				map.put((String) obj[0], (String) obj[1]);
			}
		}
		return map;
	}
}
