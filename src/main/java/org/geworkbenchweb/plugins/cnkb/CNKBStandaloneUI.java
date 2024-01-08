package org.geworkbenchweb.plugins.cnkb;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.plugins.cnkb.CNKBUI.InteractomeAndVersion;
import org.geworkbenchweb.pojos.CNKBResultSet;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

/**
 * User interface for 'standalone' CNKB.
 * 
 */
public class CNKBStandaloneUI extends VerticalLayout {
	private static Log log = LogFactory.getLog(CNKBStandaloneUI.class);

	private static final long serialVersionUID = 6992822172488124700L;

	static final String GENE_SYMBOLS = "Gene Symbols";

	final DirectGeneEntry geneEntry = new DirectGeneEntry();

	/*
	 * Initialization in this method instead of constructors is done in the main
	 * GUI thread instead of a background thread.
	 */
	@Override
	public void attach() {

		super.attach();

		/* Create a connection with the server. */
		final CNKBServletClient interactionsConnection = new CNKBServletClient();

		List<InteractomeAndVersion> interactomeList = new ArrayList<InteractomeAndVersion>();
		try {
			List<String> interactonList = interactionsConnection.getDatasetAndInteractioCount();
			for (String interactome : interactonList) {
				if(CNKBUI.isExcludedInteractome(interactome)) continue;
				List<VersionDescriptor> versionList = interactionsConnection
						.getVersionDescriptor(interactome.split(" \\(")[0].trim());
				for (VersionDescriptor v : versionList) {
					interactomeList.add(new InteractomeAndVersion(interactome, v));
				}
			}
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

		final Label interactomeDes = new Label();
		final Label versionDes = new Label();

		interactomeBox = new ListSelect("Select Interactome:");
		interactomeBox.setRows(4);
		interactomeBox.setColumns(25);
		interactomeBox.setImmediate(true);
		interactomeBox.setNullSelectionAllowed(false);
		interactomeBox.setMultiSelect(true);
		for (int j = 0; j < interactomeList.size(); j++) {
			interactomeBox.addItem(interactomeList.get(j));
		}

		interactomeBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {
					Object object = valueChangeEvent.getProperty().getValue();
					if (!(object instanceof InteractomeAndVersion))
						return;
					InteractomeAndVersion a = (InteractomeAndVersion) object;
					interactomeDes.setValue(
							interactionsConnection.getInteractomeDescription(a.interactome.split(" \\(")[0].trim()));

					VersionDescriptor vd = a.version;
					if (vd.getVersionDesc() != null && vd.getVersionDesc().trim().length() > 0
							&& !vd.getVersionDesc().trim().equals("null"))
						versionDes.setValue(vd.getVersionDesc());
					else
						versionDes.setValue("NO DESCRIPTION");
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

		final Button submitButton = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			public void buttonClick(ClickEvent event) {
				String warningMesaage = null;
				String[] selectedMarkers = geneEntry.getItemAsArray();
				if (selectedMarkers == null || selectedMarkers.length == 0)
					warningMesaage = "Please select at least one gene.";
				if (interactomeBox.getValue() == null)
					warningMesaage = "Please select interactome.";
				
				Set<CNKBUI.InteractomeAndVersion> interactomes = null;
				Object obj = interactomeBox.getValue();
				if (obj == null || !(obj instanceof Set<?>) ) { // this never happens for multi-select
					warningMesaage="Please select interactome.";
				} else {
					interactomes = (Set<CNKBUI.InteractomeAndVersion>) obj;
					if(interactomes.size()==0) {
						warningMesaage="Please select interactome.";
					} else if (interactomes.size()>1) {
						for(CNKBUI.InteractomeAndVersion a : interactomes) {
							if(!a.interactome.startsWith("aracne_") && !a.interactome.startsWith("cindy_")) {
								warningMesaage=a.interactome+" cannot be part of the multiple selection of interactomes.";
							}
						}
					}
				}
				
				if (warningMesaage != null) {
					MessageBox mb = new MessageBox(getWindow(), "Warning", MessageBox.Icon.WARN, warningMesaage,
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
					return;
				}

				HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();
				params.put(GENE_SYMBOLS, selectedMarkers);

				String version = null; /* only one version is allowed even if multiple interactome is selected. */
				StringBuilder sb = new StringBuilder();
				int i = 0;
				for(CNKBUI.InteractomeAndVersion a : interactomes) {
					if(i>0) sb.append("|");
					sb.append(a.interactome);
					if(version==null) version = a.version.getVersion();
					i++;
				}
				params.put(CNKBParameters.INTERACTOME, sb.toString());
				params.put(CNKBParameters.VERSION, version);
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
			creatAuthenticationDialog(params);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void creatAuthenticationDialog(final Map<Serializable, Serializable> params) {
		final Application app = getApplication();
		ApplicationContext cntxt = app.getContext();
		WebApplicationContext wcntxt = (WebApplicationContext)cntxt;
		final HttpSession session = wcntxt.getHttpSession();
		
		final Window dialog = new Window("Authentication");

		dialog.setModal(true);
		dialog.setDraggable(false);
		dialog.setResizable(false);
		dialog.setImmediate(true);
		dialog.setWidth("300px");

		FormLayout form = new FormLayout();

		final TextField usertf = new TextField();
		final PasswordField passwordtf = new PasswordField();
		Button submit = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = -6393819962372106745L;

			@Override
			public void buttonClick(ClickEvent event) {
				String userName = usertf.getValue().toString().trim();
				String passwd = passwordtf.getValue().toString().trim();
			 
				session.setAttribute(CNKBParameters.CNKB_USERINFO, userName
						+ ":" + passwd);
				queryCNKB(params);
				app.getMainWindow().removeWindow(dialog);

			}
		});

		usertf.setCaption("User");
		passwordtf.setCaption("Password");

		form.setMargin(true);
		form.setImmediate(true);
		form.setSpacing(true);
		form.addComponent(usertf);
		form.addComponent(passwordtf);
		form.addComponent(submit);

		dialog.addComponent(form);
		app.getMainWindow().addWindow(dialog);
	}
	
	/**
	 * Main function of this class: query the CNKB db for the interactions.
	 * Difference from the original version: no session part; no annotation
	 * part; no entrez ID option
	 */
	static CNKBResultSet getInteractions(Map<Serializable, Serializable> params, final String userInfo, final Long parentId)
			throws UnAuthenticatedException, ConnectException, SocketTimeoutException, IOException {

		String[] allInteractiomesSelected = ((String) params.get(CNKBParameters.INTERACTOME)).split("\\|");
		for(int i=0; i<allInteractiomesSelected.length; i++) {
			allInteractiomesSelected[i] = allInteractiomesSelected[i].split("\\(")[0].trim();	
		}

		String version = (String) params.get(CNKBParameters.VERSION);

		String[] geneSymbols = (String[]) params.get(GENE_SYMBOLS);

		CNKBServletClient cnkb = new CNKBServletClient();

		String interactomeName = "multiple interactomes";
		if(allInteractiomesSelected.length==1) interactomeName = allInteractiomesSelected[0] + version;
		CellularNetworkPreference cnkbPref = new CellularNetworkPreference("Throttle Graph (" + interactomeName + ")");
		List<String> interactionTypes = cnkb.getInteractionTypesByInteractomeVersion(allInteractiomesSelected[0], version);
		cnkbPref.getDisplaySelectedInteractionTypes().addAll(interactionTypes);

		Vector<CellularNetWorkElementInformation> hits = new Vector<CellularNetWorkElementInformation>();
		for (String context : allInteractiomesSelected) {
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
		} /* end of the loop of all interactomes */

		// null ID for orphan result without parent
		return new CNKBResultSet(hits, cnkbPref, parentId);
	}

	private static CNKBResultSet getInteractions(Map<Serializable, Serializable> params, String userInfo)
			throws UnAuthenticatedException, ConnectException, SocketTimeoutException, IOException {
		return getInteractions(params, userInfo, null);
	}
}
