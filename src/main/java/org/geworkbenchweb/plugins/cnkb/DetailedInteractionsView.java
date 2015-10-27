/**
 * 
 */
package org.geworkbenchweb.plugins.cnkb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.geworkbenchweb.pojos.CNKBResultSet;
import org.geworkbenchweb.visualizations.InteractionColorMosaic;

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
	private Label geneLabel = new Label("", Label.CONTENT_XHTML);

	final InteractionDetailTableView tableview = new InteractionDetailTableView();

	public void display(String gene, String markerLabel, CNKBResultsUI parent) {
		geneLabel.setValue("<b>Query Gene Symbol</b>: "+gene);
		tableview.setTargetGeneData(getTargetGenes(markerLabel));
		tableview.setSizeFull();

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
		this.addComponent(new Label("Description: [to be implemented]"));
		this.addComponent(new Label("Entrez Gene: linkout   Gene Cards: linkout"));

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
	
	/* detailed information of target genes to be shown in the detail table view */
	private Map<String, String> getTargetGenes(String markerLabel) {
		Vector<CellularNetWorkElementInformation> hits = cnkbResult.getCellularNetWorkElementInformations();
		List<String> selectedTypes = getInteractionTypes(cnkbResult);
		final Short confidentType = cnkbResult.getCellularNetworkPreference().getSelectedConfidenceType();

		Map<String, String> target = new HashMap<String, String>();
		for (CellularNetWorkElementInformation c : hits) {
			String label = c.getMarkerLabel();
			if (markerLabel.equals(label)) {
				ArrayList<InteractionDetail> interactionDetail = c.getSelectedInteractions(selectedTypes,
						confidentType);
				String interactome = c.getInteractome();
				// let's take care of the possible empty cases of the existing results
				if(interactome==null) interactome = "NULL";
				else if(interactome.trim().length()==00) interactome = "EMPTY";
				for (InteractionDetail interaction : interactionDetail) {
					for (InteractionParticipant p : interaction.getParticipantList()) {
						String g = p.getGeneName();
						target.put(g, interactome);
					}
				}
				break;
			}
		}
		return target;
	}

	// copied from CNKBResultsUI. It seems many duplicated queries to be cleaned up. TODO
	static private List<String> getInteractionTypes(CNKBResultSet resultSet) {
		Vector<CellularNetWorkElementInformation> hits = resultSet.getCellularNetWorkElementInformations();
		short confidentType = resultSet.getCellularNetworkPreference().getSelectedConfidenceType();
		List<String> interactionTypes = resultSet.getCellularNetworkPreference().getDisplaySelectedInteractionTypes();

		List<String> selectedTypes = new ArrayList<String>();
		for (int j = 0; j < hits.size(); j++) {

			ArrayList<InteractionDetail> interactionDetail = hits.get(j).getSelectedInteractions(interactionTypes,
					confidentType);
			if (interactionDetail != null) {
				for (InteractionDetail interaction : interactionDetail) {
					String interactionType = interaction.getInteractionType();
					if (selectedTypes.contains(interactionType))
						continue;
					else
						selectedTypes.add(interactionType);

				}
			}
		}
		return selectedTypes;
	}
};
