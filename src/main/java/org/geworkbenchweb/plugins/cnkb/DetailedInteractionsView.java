/**
 * 
 */
package org.geworkbenchweb.plugins.cnkb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.geworkbenchweb.pojos.CNKBResultSet;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.visualizations.InteractionColorMosaic;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * @author zji
 *
 */
public class DetailedInteractionsView extends Window {

	private static final long serialVersionUID = -4712749272144439069L;

	final private CNKBResultSet cnkbResult;
	final private Label geneLabel = new Label("", Label.CONTENT_XHTML);
	final private Label linkoutLabel = new Label("", Label.CONTENT_XHTML);
	final private Label descriptionLabel = new Label("", Label.CONTENT_XHTML);

	final InteractionDetailTableView tableview = new InteractionDetailTableView();

	public void display(String gene, String markerLabel, CNKBResultsUI parent, final Map<String, String> confidentTypeMap) {
		Map<String, String> map = getGeneSymbolToDescriptionMap(parent.getDatasetId());
		
		geneLabel.setValue("<b>Query Gene Symbol</b>: "+gene);
		tableview.setTargetGeneData(getTargetGenes(markerLabel), confidentTypeMap, map);
		tableview.setSizeFull();

		descriptionLabel.setValue("<b>Description</b>: " + map.get(gene));
		linkoutLabel.setValue("Entrez Gene: <a href='http://www.ncbi.nlm.nih.gov/gene?cmd=Search&term=" + gene
				+ "' target='_blank'>linkout</a>   Gene Cards: <a href='http://www.genecards.org/cgi-bin/carddisp.pl?gene="
				+ gene + "&alias=yes' target='_blank'>linkout</a>");
		
		this.setWidth("50%");;
		this.setHeight("50%");;
		Window mainWindow = parent.getApplication().getMainWindow();
		if (mainWindow == null) {
			MessageBox mb = new MessageBox(getWindow(), "No main window", MessageBox.Icon.ERROR,
					"Unexpected case of no main window.", new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();
			return;
		}
		mainWindow.addWindow(this);
	}

	private static final String TABLE_VIEW = "Table View";
	private static final String COLOR_MOSAIC_VIEW = "Color Mosaic View";
			
	public DetailedInteractionsView(final CNKBResultSet cnkbResult) {
		this.cnkbResult = cnkbResult;

		this.setModal(true);
		this.setClosable(true);
		((AbstractOrderedLayout) this.getContent()).setSpacing(true);
		this.setResizable(true);
		this.setCaption("Interaction Details");
		this.setImmediate(true);

		this.addComponent(geneLabel);
		this.addComponent(descriptionLabel);
		this.addComponent(linkoutLabel);

		List<String> views = Arrays.asList(new String[] { TABLE_VIEW, COLOR_MOSAIC_VIEW });

		OptionGroup viewSelect = new OptionGroup("Views", views);
		viewSelect.setImmediate(true);
		viewSelect.select(TABLE_VIEW);
		this.addComponent(viewSelect);
		final Component colormosaicview = new InteractionColorMosaic("SOME ATTRIBUTE");
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
	}
	
	static class InteractomeAndDetail {
		String interactome;
		InteractionDetail detail;
		
		InteractomeAndDetail(String interactome, InteractionDetail detail) {
			this.interactome = interactome;
			this.detail = detail;
		}
	}
	
	/* detailed information of target genes to be shown in the detail table view */
	private Map<String, InteractomeAndDetail> getTargetGenes(String markerLabel) {
		Vector<CellularNetWorkElementInformation> hits = cnkbResult.getCellularNetWorkElementInformations();

		Map<String, InteractomeAndDetail> target = new HashMap<String, InteractomeAndDetail>();
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
						target.put(g, new InteractomeAndDetail(interactome, interaction));
					}
				}
				break;
			}
		}
		return target;
	}

	static private Map<String, String> getGeneSymbolToDescriptionMap(Long resultDataSetId) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("id", resultDataSetId);
		List<ResultSet> data = FacadeFactory.getFacade().list("Select p from ResultSet as p where p.id=:id",
				parameters);
		Long parentId = data.get(0).getParent();

		parameters.clear();
		parameters.put("dataSetId", parentId);
		DataSetAnnotation dataSetAnnotation = FacadeFactory.getFacade()
				.find("SELECT d FROM DataSetAnnotation AS d WHERE d.datasetid=:dataSetId", parameters);
		Map<String, String> map = new HashMap<String, String>();
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
				System.out.println((String) obj[0] + "~" + (String) obj[1]);
			}
		}
		return map;
	}

}
