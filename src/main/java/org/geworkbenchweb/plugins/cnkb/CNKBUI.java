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
import org.geworkbench.components.interactions.cellularnetwork.InteractionsConnectionImpl;
import org.geworkbench.components.interactions.cellularnetwork.VersionDescriptor;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.UnAuthenticatedException;
import org.geworkbench.util.network.CellularNetworkPreference;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.AnnotationEntry;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.MarkerSelector;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.UserDirUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * Parameter panel for CNKB
 * 
 * @author Nikhil
 * 
 */
public class CNKBUI extends VerticalLayout implements AnalysisUI {
	private static Log log = LogFactory.getLog(CNKBUI.class);
	
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
	@Override
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
		interactomeBox.addListener(new Property.ValueChangeListener() {
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
								MessageBox mb = new MessageBox(getWindow(),
										"Warning", MessageBox.Icon.WARN,
										warningMesaage,
										new MessageBox.ButtonConfig(
												ButtonType.OK, "Ok"));
								mb.show();
								return;
							}
							params.put(CNKBParameters.MARKER_SET_ID,
									selectedMarkerSets);
							params.put(CNKBParameters.INTERACTOME,
									interactomeBox.getValue().toString());
							params.put(CNKBParameters.VERSION, versionBox
									.getValue().toString());
							submitCnkbEvent(dataSetId);
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

		// this part must be called from front end
		Application app = getApplication();
		if(app==null) { // this should not happens after the code was moved to the front end
			log.error("getApplication() returns null");
			return;
		}
		ApplicationContext cntxt = app.getContext();
		WebApplicationContext wcntxt = (WebApplicationContext)cntxt;
		session = wcntxt.getHttpSession();
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

	private HttpSession session = null;

	private CNKBResultSet getInteractions(Long dataSetId,
			HashMap<Serializable, Serializable> params)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {

		Vector<CellularNetWorkElementInformation> hits = null;
		InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
		String context = ((String) params.get(CNKBParameters.INTERACTOME)).split("\\(")[0].trim();
		String version = (String) params.get(CNKBParameters.VERSION);

		if(session==null) {
			log.error("cannot get session properly");
			return null;
		}
		String userInfo = null;
		if (session.getAttribute(CNKBParameters.CNKB_USERINFO) != null) {
			userInfo = session.getAttribute(CNKBParameters.CNKB_USERINFO)
					.toString();
			log.debug("getting userInfo from session: "+userInfo);
		}
		log.debug("userInfo "+userInfo);
		/* find annotation information */ // TODO review the efficient of this implementation
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("dataSetId", dataSetId);
		DataSetAnnotation dataSetAnnotation = FacadeFactory.getFacade().find(
				"SELECT d FROM DataSetAnnotation AS d WHERE d.datasetid=:dataSetId", parameter);
		Map<String, AnnotationEntry> annotationMap = new HashMap<String, AnnotationEntry>(); // TODO this may be more efficient by using JPA directly
		if(dataSetAnnotation!=null) {
			Long annotationId = dataSetAnnotation.getAnnotationId();
			Annotation annotation = FacadeFactory.getFacade().find(Annotation.class, annotationId);
			for(AnnotationEntry entry : annotation.getAnnotationEntries()) {
				String probeSetId = entry.getProbeSetId();
				annotationMap.put(probeSetId, entry);
			}
		}
		
		String[] selectedMarkerSet = (String[]) params
				.get(CNKBParameters.MARKER_SET_ID);
		List<String> selectedMarkers = new ArrayList<String>();
		hits = new Vector<CellularNetWorkElementInformation>();
		for (int i = 0; i < selectedMarkerSet.length; i++) {
			ArrayList<String> temp = SubSetOperations.getMarkerData(Long.parseLong(selectedMarkerSet[i].trim()));
			for(int m=0; m<temp.size(); m++) {
				String marker = ((temp.get(m)).split("\\s+"))[0].trim();					 
				if (marker != null && !selectedMarkers.contains(marker))
				{
					selectedMarkers.add(marker);
					
					hits.addElement(new CellularNetWorkElementInformation(marker, annotationMap.get(marker).getMolecularFunction(),  annotationMap.get(marker).getBiologicalProcess()));
				}
			}
			 
		}
		log.debug("hist size "+hits.size());

		/* this is the new variation of InteractionsConnectionImpl */
		CNKB cnkb = new CNKB();
		
		CellularNetworkPreference cnkbPref = new CellularNetworkPreference(
				"Throttle Graph(" + context + version + ")");
		cnkbPref.setContext(context);
		cnkbPref.setVersion(version);
		List<String> interactionTypes = interactionsConnection
				.getInteractionTypesByInteractomeVersion(context, version);
		cnkbPref.getDisplaySelectedInteractionTypes().addAll(interactionTypes);

		

		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {

			String label = cellularNetWorkElementInformation.getMarkerLabel();
			if(label==null) {
				log.warn("marker label is null");
				continue;
			}
			
			AnnotationEntry entry = annotationMap.get(label);
			String geneId = entry.getEntrezId();
			String geneSymbol = entry.getGeneSymbol();

			if (geneId!=null && !geneId.trim().equals("---")
					&& cellularNetWorkElementInformation.isDirty()) {				
				List<InteractionDetail> interactionDetails = null;

				if (interaction_flag == 0) {
					interactionDetails = cnkb
							.getInteractionsByEntrezIdOrGeneSymbol_1(geneId, geneSymbol,
									context, version, userInfo);
				} else {
					interactionDetails = cnkb
							.getInteractionsByEntrezIdOrGeneSymbol_2(geneId, geneSymbol,
									context, version, userInfo);
				}

				cellularNetWorkElementInformation.setDirty(false);
				cellularNetWorkElementInformation.setInteractionDetails(
						interactionDetails, cnkbPref);
			 
			}
			 
		}

		return new CNKBResultSet(hits, cnkbPref, dataSetId);
	}

	private void creatAuthenticationDialog(final Long dataSetId) {
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
				HttpSession session = ((WebApplicationContext) getApplication()
						.getContext()).getHttpSession();
				session.setAttribute(CNKBParameters.CNKB_USERINFO, userName
						+ ":" + passwd);
				submitCnkbEvent(dataSetId);
				getApplication().getMainWindow().removeWindow(dialog);

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
		getApplication().getMainWindow().addWindow(dialog);

	}

	private void submitCnkbEvent(Long dataSetId) {
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
				dataSetId, resultSet, params, CNKBUI.this);
		GeworkbenchRoot.getBlackboard().fire(analysisEvent);

	}

	@Override
	public String execute(Long resultId, Long datasetId,
			HashMap<Serializable, Serializable> parameters, Long userId) throws IOException,
			Exception {
		try {
			CNKBResultSet resultSet = getInteractions(
					datasetId, params);
			UserDirUtils.serializeResultSet(resultId, resultSet);
		} catch (UnAuthenticatedException uae) {
			creatAuthenticationDialog(datasetId);
			return "UnAuthenticatedException";
		} catch (NullPointerException e) {
			throw new IOException("null pointer caught in CNKBUI"); // using IOException because of the limitation of the interface AnalysisUI
		}
		return "CNKB";
	}

}
