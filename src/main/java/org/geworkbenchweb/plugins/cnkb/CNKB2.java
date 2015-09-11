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
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * Parameter panel for 'standalone' CNKB
 * 
 */
public class CNKB2 extends VerticalLayout {
	private static Log log = LogFactory.getLog(CNKB2.class);

	private static final long serialVersionUID = 6992822172488124700L;

	private static final String MARKER_NAMES = "Marker Names";

	final ListSelect geneEntry = new ListSelect("Direct Gene Entry");
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

		geneEntry.setMultiSelect(true);
		geneEntry.setRows(4);
		geneEntry.setColumns(15);
		geneEntry.setImmediate(true);

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
				String[] selectedMarkers = getItemAsArray( geneEntry );
				if (selectedMarkers == null || selectedMarkers.length == 0)
					warningMesaage = "Please select at least one marker set.";
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
				params.put(MARKER_NAMES, selectedMarkers);
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
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		buttons.addComponent(createAddGeneButton());
		buttons.addComponent(new Button("Delete Gene"));
		buttons.addComponent(new Button("Clear List"));
		buttons.addComponent(new Button("Load Genes from File"));
		addComponent(buttons);
		addComponent(interactomeBox);
		addComponent(interactomeDes);
		addComponent(versionBox);
		addComponent(versionDes);
		addComponent(submitButton);
	}
	
	private Button createAddGeneButton() {
		final String title = "Add Gene";
		Button b =  new Button(title);
		b.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = -4929628014095090196L;

			@Override
			public void buttonClick(ClickEvent event) {

				final Window geneDialog = new Window();
				geneDialog.setModal(true);
				geneDialog.setClosable(true);
				((AbstractOrderedLayout) geneDialog.getContent()).setSpacing(true);
				geneDialog.setWidth("300px");
				geneDialog.setHeight("150px");
				geneDialog.setResizable(false);
				geneDialog.setCaption(title);
				geneDialog.setImmediate(true);

				final TextField geneSymbol = new TextField();
				geneSymbol.setInputPrompt("Please enter gene symbol");
				geneSymbol.setImmediate(true);

				Button submit = new Button(title, new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						geneEntry.addItem( geneSymbol.getValue().toString() );
					}
				});
				submit.setClickShortcut(KeyCode.ENTER);
				geneDialog.addComponent(geneSymbol);
				geneDialog.addComponent(submit);
				Window mainWindow = CNKB2.this.getApplication().getMainWindow();
				mainWindow.addWindow(geneDialog);
			}
		});
		return b;
	}
	
	private static String[] getItemAsArray(ListSelect listSelect) {
		String[] a = null;
		String selectStr = listSelect.getValue().toString();
		if (!selectStr.equals("[]")) {
			a = selectStr.substring(1, selectStr.length() - 1).split(",");
		}
		return a;
	}
	
	// TODO review both input and output
	private void queryCNKB(final Map<Serializable, Serializable> params) {
		// this part about session must be called from front end
		Application app = getApplication();
		if (app == null) { // this should not happens after the code was moved
							// to the front end
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
			log.debug("getting userInfo from session: " + userInfo);
		}
		log.debug("userInfo " + userInfo);
				
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
	 * Difference from the original version: no session part; no annotation part; no entrez ID option
	 */
	private static CNKBResultSet getInteractions(Map<Serializable, Serializable> params, final String userInfo)
			throws UnAuthenticatedException, ConnectException, SocketTimeoutException, IOException {

		String context = ((String) params.get(CNKBParameters.INTERACTOME)).split("\\(")[0].trim();
		String version = (String) params.get(CNKBParameters.VERSION);

		String[] selectedMarkers = (String[]) params.get(MARKER_NAMES);

		CNKBServletClient cnkb = new CNKBServletClient();

		CellularNetworkPreference cnkbPref = new CellularNetworkPreference("Throttle Graph(" + context + version + ")");
		List<String> interactionTypes = cnkb.getInteractionTypesByInteractomeVersion(context, version);
		cnkbPref.getDisplaySelectedInteractionTypes().addAll(interactionTypes);

		Vector<CellularNetWorkElementInformation> hits = new Vector<CellularNetWorkElementInformation>();
		for (String geneSymbol : selectedMarkers) {

			// GO stuff
			int[] mf = new int[0];
			int[] bp = new int[0];

			List<InteractionDetail> interactionDetails = cnkb.getInteractionsByGeneSymbol(geneSymbol, context, version,
					userInfo);
			CellularNetWorkElementInformation element = new CellularNetWorkElementInformation(geneSymbol, mf, bp,
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
		return new CNKBResultSet(hits, cnkbPref, null);
	}
}
