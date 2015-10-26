package org.geworkbenchweb.plugins.cnkb;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.pojos.CNKBResultSet;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * User interface for 'standalone' CNKB.
 * 
 */
public class CNKBStandaloneUI extends VerticalLayout {
	private static Log log = LogFactory.getLog(CNKBStandaloneUI.class);

	private static final long serialVersionUID = 6992822172488124700L;

	static final String GENE_SYMBOLS = "Gene Symbols";

	final DirectGeneEntry geneEntry = new DirectGeneEntry();
	private List<VersionDescriptor> versionList = new ArrayList<VersionDescriptor>();

	/*
	 * Initialization in this method instead of constructors is done in the main
	 * GUI thread instead of a background thread.
	 */
	@Override
	public void attach() {

		super.attach();

		/* Create a connection with the server. */
		final CNKBServletClient interactionsConnection = new CNKBServletClient();

		List<String> interactomeList = null;
		try {
			interactomeList = interactionsConnection.getDatasetAndInteractioCount();
		} catch (UnAuthenticatedException uae) {
			uae.printStackTrace();
		} catch (ConnectException e1) {
			e1.printStackTrace();
		} catch (SocketTimeoutException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		final ListSelect interactomeBox;
		final ListSelect versionBox;
		final Label interactomeDes = new Label();
		final Label versionDes = new Label();

		interactomeBox = new ListSelect("Select Interactome:");
		interactomeBox.setRows(4);
		interactomeBox.setColumns(25);
		interactomeBox.setImmediate(true);
		interactomeBox.setNullSelectionAllowed(false);
		for (int j = 0; j < interactomeList.size(); j++) {
			interactomeBox.addItem(interactomeList.get(j));
		}

		versionBox = new ListSelect("Select Version:");

		versionBox.setRows(4);
		versionBox.setColumns(15);
		versionBox.setImmediate(true);
		versionBox.setNullSelectionAllowed(false);
		versionBox.setEnabled(false);
		interactomeBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {
					interactomeDes.setValue(interactionsConnection.getInteractomeDescription(
							valueChangeEvent.getProperty().getValue().toString().split(" \\(")[0].trim()));
					versionBox.setEnabled(true);
					versionBox.removeAllItems();
					versionList = interactionsConnection.getVersionDescriptor(
							valueChangeEvent.getProperty().getValue().toString().split(" \\(")[0].trim());
					for (int k = 0; k < versionList.size(); k++) {
						versionBox.addItem(versionList.get(k).getVersion());
					}

				} catch (ConnectException e) {
					e.printStackTrace();
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (UnAuthenticatedException uae) {
					uae.printStackTrace();
				}
			}
		});

		versionBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				versionDes.setValue("");
				Object version = valueChangeEvent.getProperty().getValue();
				if (version == null || version.toString().trim().length() == 0)
					return;
				String versionValue = version.toString().trim();

				for (VersionDescriptor vd : versionList) {
					if (vd.getVersion().trim().equals(versionValue)) {
						if (vd.getVersionDesc() != null && vd.getVersionDesc().trim().length() > 0
								&& !vd.getVersionDesc().trim().equals("null"))
							versionDes.setValue(vd.getVersionDesc());
						else
							versionDes.setValue("NO DESCRIPTION");
						break;
					}
				}
			}
		});

		final Button submitButton = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				String warningMesaage = null;
				String[] selectedMarkers = geneEntry.getItemAsArray();
				if (selectedMarkers == null || selectedMarkers.length == 0)
					warningMesaage = "Please select at least one gene.";
				if (interactomeBox.getValue() == null)
					warningMesaage = "Please select interactome.";
				if (versionBox.getValue() == null)
					warningMesaage = "Please select version.";
				if (warningMesaage != null) {
					MessageBox mb = new MessageBox(getWindow(), "Warning", MessageBox.Icon.WARN, warningMesaage,
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
					return;
				}

				HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();
				params.put(GENE_SYMBOLS, selectedMarkers);
				params.put(CNKBParameters.INTERACTOME, interactomeBox.getValue().toString());
				params.put(CNKBParameters.VERSION, versionBox.getValue().toString());
				queryCNKB(params);
			}
		});

		interactomeDes.setStyleName(Reindeer.LABEL_SMALL);
		interactomeDes.setImmediate(true);

		versionDes.setStyleName(Reindeer.LABEL_SMALL);
		versionDes.setImmediate(true);

		setSpacing(true);
		addComponent(geneEntry);
		addComponent(interactomeBox);
		addComponent(interactomeDes);
		addComponent(versionBox);
		addComponent(versionDes);
		addComponent(submitButton);
	}

	// TODO review both input and output
	private void queryCNKB(final Map<Serializable, Serializable> params) {
		// this part about session must be called from front end
		Application app = getApplication();
		if (app == null) { // this should never happens
			log.error("getApplication() returns null");
			return;
		}
		ApplicationContext cntxt = app.getContext();
		WebApplicationContext wcntxt = (WebApplicationContext) cntxt;
		HttpSession session = wcntxt.getHttpSession();
		if (session == null) {
			log.error("cannot get session properly");
			return;
		}
		String userInfo = null;
		if (session.getAttribute(CNKBParameters.CNKB_USERINFO) != null) {
			userInfo = session.getAttribute(CNKBParameters.CNKB_USERINFO).toString();
		}

		try {
			CNKBResultSet result = getInteractions(params, userInfo);
			System.out.println(result);

			// direct show the orphan result
			Window w = getApplication().getMainWindow();
			ComponentContainer content = w.getContent();
			if (content instanceof UMainLayout) {
				UMainLayout m = (UMainLayout) content;
				m.setPluginViewContent(new CNKBResultsUI(result));
			} else {
				log.error("wrong type of plugin view content: " + content);
			}
		} catch (ConnectException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (UnAuthenticatedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main function of this class: query the CNKB db for the interactions.
	 * Difference from the original version: no session part; no annotation
	 * part; no entrez ID option
	 */
	static CNKBResultSet getInteractions(Map<Serializable, Serializable> params, final String userInfo, final Long parentId)
			throws UnAuthenticatedException, ConnectException, SocketTimeoutException, IOException {

		String context = ((String) params.get(CNKBParameters.INTERACTOME)).split("\\(")[0].trim();
		String version = (String) params.get(CNKBParameters.VERSION);

		String[] geneSymbols = (String[]) params.get(GENE_SYMBOLS);

		CNKBServletClient cnkb = new CNKBServletClient();

		CellularNetworkPreference cnkbPref = new CellularNetworkPreference("Throttle Graph(" + context + version + ")");
		List<String> interactionTypes = cnkb.getInteractionTypesByInteractomeVersion(context, version);
		cnkbPref.getDisplaySelectedInteractionTypes().addAll(interactionTypes);

		Vector<CellularNetWorkElementInformation> hits = new Vector<CellularNetWorkElementInformation>();
		for (String geneSymbol : geneSymbols) {

			// GO stuff
			int[] mf = new int[0];
			int[] bp = new int[0];

			List<InteractionDetail> interactionDetails = cnkb.getInteractionsByGeneSymbol(geneSymbol, context, version,
					userInfo);
			CellularNetWorkElementInformation element = new CellularNetWorkElementInformation(geneSymbol, context+" "+version, mf, bp,
					interactionDetails);
			hits.addElement(element);

			/*
			 * FIXME update preference. this is inherited from the earlier code.
			 * I don't think it does the correct thing considering this is
			 * repeated in this loop of all markers.
			 */
			if (interactionDetails != null && interactionDetails.size() > 0) {

				for (InteractionDetail detail : interactionDetails) {
					List<Short> typeIdList = detail.getConfidenceTypes();
					for (int j = 0; j < typeIdList.size(); j++) {
						Short typeId = typeIdList.get(j);
						Double maxConfidenceValue = cnkbPref.getMaxConfidenceValue(typeId);
						double confidenceValue = detail.getConfidenceValue(typeId);
						if (maxConfidenceValue == null)
							cnkbPref.getMaxConfidenceValueMap().put(typeId, new Double(confidenceValue));
						else {
							if (maxConfidenceValue < confidenceValue) {
								cnkbPref.getMaxConfidenceValueMap().put(typeId, new Double(confidenceValue));
							}
						}
						if (!cnkbPref.getConfidenceTypeList().contains(typeId))
							cnkbPref.getConfidenceTypeList().add(typeId);
					}

				}
				if (cnkbPref.getSelectedConfidenceType() == null
						|| cnkbPref.getSelectedConfidenceType().shortValue() == 0) {
					// use first one as default value.
					cnkbPref.setSelectedConfidenceType(cnkbPref.getConfidenceTypeList().get(0));
				}
			}
		} /* end of the loop of all markers */

		// null ID for orphan result without parent
		return new CNKBResultSet(hits, cnkbPref, parentId);
	}

	private static CNKBResultSet getInteractions(Map<Serializable, Serializable> params, String userInfo)
			throws UnAuthenticatedException, ConnectException, SocketTimeoutException, IOException {
		return getInteractions(params, userInfo, null);
	}
}
