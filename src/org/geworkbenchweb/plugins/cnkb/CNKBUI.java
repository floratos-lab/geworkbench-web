package org.geworkbenchweb.plugins.cnkb;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.components.interactions.cellularnetwork.InteractionsConnectionImpl;
import org.geworkbench.components.interactions.cellularnetwork.VersionDescriptor;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.UnAuthenticatedException;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.CellularNetworkPreference;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.MarkerSelector;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.UserDirUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.messagebox.ButtonId;
import de.steinwedel.messagebox.Icon;
import de.steinwedel.messagebox.MessageBox;

/**
 * Parameter panel for CNKB
 * 
 * @author Nikhil
 * 
 */
public class CNKBUI extends VerticalLayout implements AnalysisUI {

	private static final long serialVersionUID = -1221913812891134388L;

	private ResultSet resultSet;

	private static final int timeout = 3000;

	private List<String> contextList = new ArrayList<String>();

	private List<VersionDescriptor> versionList = new ArrayList<VersionDescriptor>();

	User user = SessionHandler.get();

	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();

	private long dataSetId;

	private int interaction_flag = 1;

	public CNKBUI() {
		this(0L);
	}

	public CNKBUI(Long dataSetId) {
		this.dataSetId = dataSetId;
		this.setSpacing(true);
		this.setImmediate(true);

	}

	// FIXME why the GUI implementation of this analysis is different from other
	// analysis plug-ins
	public void attach() {

		super.attach();
		loadApplicationProperty();
		final InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();

		try {
			contextList = interactionsConnection.getDatasetAndInteractioCount();
		} catch (UnAuthenticatedException uae) {
			uae.printStackTrace();
		} catch (ConnectException e1) {
			e1.printStackTrace();
		} catch (SocketTimeoutException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		final MarkerSelector markerSelector = new MarkerSelector(dataSetId,
				user.getId(), "CNKBUI");
		final ListSelect interactomeBox;
		final ListSelect versionBox;
		final Label interactomeDes = new Label();

		interactomeBox = new ListSelect("Select Interactome:");
		interactomeBox.setRows(4);
		interactomeBox.setColumns(25);
		interactomeBox.setImmediate(true);
		interactomeBox.setNullSelectionAllowed(false);
		for (int j = 0; j < contextList.size(); j++) {
			interactomeBox.addItem(contextList.get(j));
		}

		versionBox = new ListSelect("Select Version:");

		versionBox.setRows(4);
		versionBox.setColumns(15);
		versionBox.setImmediate(true);
		versionBox.setNullSelectionAllowed(false);
		versionBox.setEnabled(false);
		interactomeBox.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {
					interactomeDes.setValue(interactionsConnection
							.getInteractomeDescription(valueChangeEvent
									.getProperty().getValue().toString()
									.split(" \\(")[0].trim()));
					versionBox.setEnabled(true);
					versionBox.removeAllItems();
					versionList = interactionsConnection
							.getVersionDescriptor(valueChangeEvent
									.getProperty().getValue().toString()
									.split(" \\(")[0].trim());
					for (int k = 0; k < versionList.size(); k++) {
						versionBox.addItem(versionList.get(k).getVersion());
					}

				} catch (ConnectException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SocketTimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnAuthenticatedException uae) {
					// TODO Auto-generated catch block
					uae.printStackTrace();
				}

			}
		});

		final Button submitButton = new Button("Submit",
				new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					public void buttonClick(ClickEvent event) {
						try {
							String warningMesaage = null;
							String[] selectedMarkerSets = markerSelector
									.getSelectedMarkerSet();
							if (selectedMarkerSets == null
									|| selectedMarkerSets.length == 0)
								warningMesaage = "Please select at least one marker set.";
							if (interactomeBox.getValue() == null)
								warningMesaage = "Please select interactome.";
							if (versionBox.getValue() == null)
								warningMesaage = "Please select version.";
							if (warningMesaage != null) {
								MessageBox.showPlain(Icon.WARN,
										"Warning",
										warningMesaage,
										ButtonId.OK);
								return;
							}
							params.put(CNKBParameters.MARKER_SET_ID,
									selectedMarkerSets);
							params.put(CNKBParameters.INTERACTOME,
									interactomeBox.getValue().toString());
							params.put(CNKBParameters.VERSION, versionBox
									.getValue().toString());
							DSMicroarraySet maSet = (DSMicroarraySet) UserDirUtils
									.deserializeDataSet(dataSetId,
											DSMicroarraySet.class);
							UserDirUtils.setAnnotationParser(dataSetId, maSet);
							submitCnkbEvent(maSet);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

		interactomeDes.setStyleName(Reindeer.LABEL_SMALL);
		interactomeDes.setImmediate(true);

		addComponent(markerSelector);
		addComponent(interactomeBox);
		addComponent(interactomeDes);
		addComponent(versionBox);
		addComponent(submitButton);
		markerSelector.setData(dataSetId, user.getId());
	}

	/**
	 * Create a connection with the server.
	 */
	private void loadApplicationProperty() {
		String interactionsServletUrl = "http://cagridnode.c2b2.columbia.edu:8080/cknb/InteractionsServlet_new/InteractionsServlet";
		ResultSetlUtil.setUrl(interactionsServletUrl);
		ResultSetlUtil.setTimeout(timeout);
	}

	private void generateHistoryString() {
		StringBuilder mark = new StringBuilder();

		mark.append("CNKB Parameters : \n");
		mark.append("Interactome - "
				+ (String) params.get(CNKBParameters.INTERACTOME) + "\n");
		mark.append("Interactome Version - "
				+ (String) params.get(CNKBParameters.VERSION) + "\n");
		mark.append("Markers used - \n");
		
		String[] selectedMarkerSet = (String[]) params
				.get(CNKBParameters.MARKER_SET_ID);
		 
		for (int i = 0; i < selectedMarkerSet.length; i++) {
			ArrayList<String> markers = SubSetOperations.getMarkerData(Long.parseLong(selectedMarkerSet[i].trim()));
			
			for(int m=0; m<markers.size(); m++) {
				mark.append("\t" + markers.get(i) + "\n");
			}
			 
		} 
		 

		DataHistory his = new DataHistory();
		his.setParent(resultSet.getId());
		his.setData(mark.toString());
		FacadeFactory.getFacade().store(his);
	}

	@Override
	public void setDataSetId(Long dataId) {
		this.dataSetId = dataId;
		this.removeAllComponents();
	}

	@Override
	public Class<?> getResultType() {
		return CNKBResultSet.class;
	}

	@Override
	public String execute(Long resultId, DSDataSet<?> dataset,
			HashMap<Serializable, Serializable> parameters) {
		try {
			CNKBResultSet resultSet = getInteractions(
					(DSMicroarraySet) dataset, params);
			UserDirUtils.saveResultSet(resultId,
					ObjectConversion.convertToByte(resultSet));
		} catch (UnAuthenticatedException uae) {
			creatAuthenticationDialog((DSMicroarraySet) dataset);
			return "UnAuthenticatedException";
		} catch (Exception ex) {
			return ">>>RemoteException:" + ex.getMessage();
		}
		return "CNKB";

	}

	private CNKBResultSet getInteractions(DSMicroarraySet dataSet,
			HashMap<Serializable, Serializable> params)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException, Exception {

		Vector<CellularNetWorkElementInformation> hits = null;
		InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
		String context = ((String) params.get(CNKBParameters.INTERACTOME)).split("\\(")[0].trim();
		String version = (String) params.get(CNKBParameters.VERSION);
		Object userInfoObj = VaadinSession.getCurrent().getAttribute(
				CNKBParameters.CNKB_USERINFO);
		String userInfo = null;
		if (userInfoObj != null)
			userInfo = userInfoObj.toString();
		String[] selectedMarkerSet = (String[]) params
				.get(CNKBParameters.MARKER_SET_ID);
		DSItemList<DSGeneMarker> selectedMarkers = new CSItemList<DSGeneMarker>();
		hits = new Vector<CellularNetWorkElementInformation>();
		for (int i = 0; i < selectedMarkerSet.length; i++) {
			ArrayList<String> temp = SubSetOperations.getMarkerData(Long.parseLong(selectedMarkerSet[i].trim()));
			for(int m=0; m<temp.size(); m++) {
				String temp1 = ((temp.get(m)).split("\\s+"))[0].trim();					 
				DSGeneMarker marker = dataSet.getMarkers().get(temp1);
				if (marker != null && !selectedMarkers.contains(marker))
				{
					selectedMarkers.add(marker);
					hits.addElement(new CellularNetWorkElementInformation(marker));
				}
			}
			 
		} 	 

		CellularNetworkPreference cnkbPref = new CellularNetworkPreference(
				"Throttle Graph(" + context + version + ")");
		cnkbPref.setContext(context);
		cnkbPref.setVersion(version);
		List<String> interactionTypes = interactionsConnection
				.getInteractionTypesByInteractomeVersion(context, version);
		cnkbPref.getDisplaySelectedInteractionTypes().addAll(interactionTypes);

		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {

			DSGeneMarker marker = cellularNetWorkElementInformation
					.getdSGeneMarker();

			if (marker != null && marker.getGeneId() != 0
					&& cellularNetWorkElementInformation.isDirty()) {

				List<InteractionDetail> interactionDetails = null;

				if (interaction_flag == 0) {
					interactionDetails = interactionsConnection
							.getInteractionsByEntrezIdOrGeneSymbol_1(marker,
									context, version, userInfo);
				} else {
					interactionDetails = interactionsConnection
							.getInteractionsByEntrezIdOrGeneSymbol_2(marker,
									context, version, userInfo);
				}

				cellularNetWorkElementInformation.setDirty(false);
				cellularNetWorkElementInformation.setInteractionDetails(
						interactionDetails, cnkbPref);
			}
		}

		return new CNKBResultSet(hits, cnkbPref);
	}

	private void creatAuthenticationDialog(final DSMicroarraySet maSet) {
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
				try{
					VaadinSession.getCurrent().getLockInstance().lock();
					VaadinSession.getCurrent().setAttribute(
							CNKBParameters.CNKB_USERINFO, userName + ":" + passwd);
				}finally{
					VaadinSession.getCurrent().getLockInstance().unlock();
				}
				submitCnkbEvent(maSet);
				UI.getCurrent().removeWindow(dialog);

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

		dialog.setContent(form);
		UI.getCurrent().addWindow(dialog);

	}

	private void submitCnkbEvent(DSMicroarraySet maSet) {
		resultSet = new ResultSet();
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
		resultSet.setDateField(date);
		String dataSetName = "CNKB - Pending";
		resultSet.setName(dataSetName);
		resultSet.setType(getResultType().getName());
		resultSet.setParent(dataSetId);
		resultSet.setOwner(user.getId());

		FacadeFactory.getFacade().store(resultSet);

		generateHistoryString();

		NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
		GeworkbenchRoot.getBlackboard().fire(resultEvent);

		AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
				maSet, resultSet, params, CNKBUI.this);
		GeworkbenchRoot.getBlackboard().fire(analysisEvent);

	}

	@Override
	public String execute(Long resultId, Long datasetId,
			HashMap<Serializable, Serializable> parameters, Long userId) throws IOException,
			Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
 

}
