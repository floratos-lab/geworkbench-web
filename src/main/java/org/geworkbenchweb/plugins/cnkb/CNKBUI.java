package org.geworkbenchweb.plugins.cnkb;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.AnnotationEntry;
import org.geworkbenchweb.pojos.CNKBResultSet;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.MarkerSelector;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.OptionGroup;
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
 */
public class CNKBUI extends VerticalLayout implements AnalysisUI {
	private static Log log = LogFactory.getLog(CNKBUI.class);
	
	private static final long serialVersionUID = -1221913812891134388L;
	private static boolean multiSelectAllowed;
	private static List<String> excludedInteractomes;
	private static String[] excludedPrefixes;

	private ResultSet resultSet;

	User user = null;
	Application app = null;

	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();

	private long dataSetId;
	private Long dataSetAnnotationId = null;

	public CNKBUI(Long dataSetId) {
		this.dataSetId = dataSetId;
		this.setSpacing(true);
		this.setImmediate(true);

	}

	static class InteractomeAndVersion {
		String interactome;
		VersionDescriptor version;

		public InteractomeAndVersion(String interactome, VersionDescriptor version) {
			this.interactome = interactome;
			this.version = version;
		}
		
		@Override
		public String toString() {
			return interactome + " version " + version.getVersion();
		}
	}

	static boolean isExcludedInteractome(String interactomeAndCount) {
		String interactome = interactomeAndCount.split(" \\(")[0].trim();
		if(excludedInteractomes.contains(interactome)) return true;
		for(String prefix : excludedPrefixes) {
			if(interactome.startsWith(prefix)) return true;
		}
		return false;
	}

	/*
	 * Initialization in this method instead of constructors is done in the main
	 * GUI thread instead of a background thread.
	 */
	@Override
	public void attach() {

		super.attach();

		/* Create a connection with the server. */
		final CNKBServletClient interactionsConnection = new CNKBServletClient();

		List<InteractomeAndVersion> contextList = new ArrayList<InteractomeAndVersion>();

		try {
			List<String> interactonList = interactionsConnection.getDatasetAndInteractioCount();
			for (String interactome : interactonList) {
				if(isExcludedInteractome(interactome)) continue;
				List<VersionDescriptor> versionList = interactionsConnection
						.getVersionDescriptor(interactome.split(" \\(")[0].trim());
				for (VersionDescriptor v : versionList) {
					contextList.add(new InteractomeAndVersion(interactome, v));
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

		user = SessionHandler.get();
		final MarkerSelector markerSelector = new MarkerSelector(dataSetId,
				user.getId(), "CNKBUI");
		final ListSelect interactomeBox;

		final Label interactomeDes = new Label();
		final Label versionDes = new Label();
		 
		interactomeBox = new ListSelect("Select Interactome:");
		interactomeBox.setRows(8);
		interactomeBox.setColumns(25);
		interactomeBox.setImmediate(true);
		interactomeBox.setNullSelectionAllowed(false);
		interactomeBox.setMultiSelect(multiSelectAllowed);
		for (int j = 0; j < contextList.size(); j++) {
			InteractomeAndVersion a = contextList.get(j);
			interactomeBox.addItem(a);
		}

		interactomeBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {
					Object object = valueChangeEvent.getProperty().getValue();
					InteractomeAndVersion a = null;
					if (object instanceof InteractomeAndVersion) {
						a = (InteractomeAndVersion) object;
					} else if(object instanceof Set && ((Set<?>)object).size()>0) {
						Object obj = ((Set<?>)object).iterator().next();
						if(obj instanceof InteractomeAndVersion) a = (InteractomeAndVersion)obj;
					} else { // FIXME for debug only, remove later
						System.out.println("CLASS NAME:"+object.getClass().getName());
					}
					if(a==null) return;
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

		final DirectGeneEntry directEntry = new DirectGeneEntry();

		final Button submitButton = new Button("Submit",
				new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@SuppressWarnings("unchecked")
					public void buttonClick(ClickEvent event) {
						try {
							// find annotation information and if it is null, cannot process query 
							Map<String, Object> parameter = new HashMap<String, Object>();
							parameter.put("dataSetId", dataSetId);
							DataSetAnnotation dataSetAnnotation = FacadeFactory.getFacade()
									.find("SELECT d FROM DataSetAnnotation AS d WHERE d.datasetid=:dataSetId", parameter);
							if (dataSetAnnotation == null) {
								MessageBox mb = new MessageBox(getWindow(), "Warning", MessageBox.Icon.WARN,
										"No annotation file was loaded, cannot query CNKB.",
										new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
								mb.show();
								return;
							}
							dataSetAnnotationId = dataSetAnnotation.getAnnotationId();
							
							//If annotation info is not null then proceed to verify other things
							//Define an array of 4 Warning messages based on priorities for each situation below
							String[] priorityWarningMessages={null,null,null};
							//index 0 represents warning message for not choosing markerSet
							//index 1 represents warning message for not choosing directEntry
							//index 2 represents warning message for not choosing interactome
							
							String warningMesaage = null;
							if (markerSelector.isEnabled()) {
								String[] selectedMarkerSets = markerSelector.getSelectedMarkerSet();
								params.put(CNKBParameters.MARKER_SET_ID, selectedMarkerSets);
								if (selectedMarkerSets == null || selectedMarkerSets.length == 0)
									priorityWarningMessages[0]="Please select at least one marker set.";
							} else if (directEntry.isEnabled()) {
								String[] selectedMarkers = directEntry.getItemAsArray();
								params.put(CNKBStandaloneUI.GENE_SYMBOLS, selectedMarkers);
								if (selectedMarkers == null || selectedMarkers.length == 0)
									priorityWarningMessages[1]="Please select at least one gene.";
							} else {
								return;
							}
							Set<InteractomeAndVersion> interactomes = null;
							Object obj = interactomeBox.getValue();
							if (obj == null || !(obj instanceof Set<?>) ) { // this never happens for multi-select
								priorityWarningMessages[2]="Please select interactome.";
							} else {
								interactomes = (Set<InteractomeAndVersion>) obj;
								if(interactomes.size()==0) {
									priorityWarningMessages[2]="Please select interactome.";
								} else if (interactomes.size()>1) {
									for(InteractomeAndVersion a : interactomes) {
										if(!a.interactome.startsWith("aracne_") && !a.interactome.startsWith("cindy_")) {
											priorityWarningMessages[2]=a.interactome+" cannot be part of the multiple selection of interactomes.";
										}
									}
								}
							}
							
							//Now assign a value to warningMesaage based on priority
							for(int i=0;i<3;i++){
								if(priorityWarningMessages[i]!=null){
									warningMesaage=priorityWarningMessages[i];
									break;
								}
							}
							
							if (warningMesaage != null) {
								MessageBox mb = new MessageBox(getWindow(),
										"Warning", MessageBox.Icon.WARN,
										warningMesaage,
										new MessageBox.ButtonConfig(
												ButtonType.OK, "Ok"));
								mb.show();
								return;
							}
							String version = null; /* only one version is allowed even if multiple interactome is selected. */
							StringBuilder sb = new StringBuilder();
							int i = 0;
							for(InteractomeAndVersion a : interactomes) {
								if(i>0) sb.append("|");
								sb.append(a.interactome);
								if(version==null) version = a.version.getVersion();
								i++;
							}
							params.put(CNKBParameters.INTERACTOME, sb.toString());
							params.put(CNKBParameters.VERSION, version);
							submitCnkbEvent(dataSetId);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

		interactomeDes.setStyleName(Reindeer.LABEL_SMALL);
		interactomeDes.setImmediate(true);
		
		versionDes.setStyleName(Reindeer.LABEL_SMALL);
		versionDes.setImmediate(true);

		HorizontalLayout geneSelector = new HorizontalLayout();
		geneSelector.setSpacing(true);
		geneSelector.addComponent(markerSelector);
		geneSelector.setComponentAlignment(markerSelector, Alignment.BOTTOM_CENTER);
		final String USE_MARKER_SETS = "Use Marker Sets";
		final String USE_DIRECT_ENTRY = "User Direct_Entry";
		List<String> geneSource = Arrays.asList( new String[]{USE_MARKER_SETS, USE_DIRECT_ENTRY} );
		OptionGroup sourceSelector = new OptionGroup("", geneSource);
		sourceSelector.setImmediate(true);
		geneSelector.addComponent(sourceSelector);
		geneSelector.setComponentAlignment(sourceSelector, Alignment.BOTTOM_CENTER);
		geneSelector.addComponent(directEntry);
		geneSelector.setComponentAlignment(directEntry, Alignment.BOTTOM_CENTER);
		directEntry.setEnabled(false);

		sourceSelector.select(USE_MARKER_SETS);
		sourceSelector.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = -7598659974346923939L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Property p = event.getProperty();
				Object v = p.getValue();
				if(USE_MARKER_SETS.equals(v)) {
					markerSelector.setEnabled(true);
					directEntry.setEnabled(false);
				} else if(USE_DIRECT_ENTRY.equals(v)) {
					markerSelector.setEnabled(false);
					directEntry.setEnabled(true);
				}
			}
		});

		addComponent(geneSelector);
		addComponent(interactomeBox);
		addComponent(interactomeDes);
		addComponent(versionDes);
		addComponent(submitButton);
		markerSelector.setData(dataSetId, user.getId());

		// this part must be called from front end
		app = getApplication();
		if(app==null) { // this should not happens after the code was moved to the front end
			log.error("getApplication() returns null");
			return;
		}
		ApplicationContext cntxt = app.getContext();
		WebApplicationContext wcntxt = (WebApplicationContext)cntxt;
		session = wcntxt.getHttpSession();
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
		
		if(selectedMarkerSet==null) {
			String[] selectedGenes = (String[])params.get(CNKBStandaloneUI.GENE_SYMBOLS);
			for(String gene : selectedGenes) {
				mark.append(gene).append(", ");
			}
			mark.append('\n');
		} else {
			for (int i = 0; i < selectedMarkerSet.length; i++) {
				List<String> markers = SubSetOperations.getMarkerData(Long.parseLong(selectedMarkerSet[i].trim()));
				
				for(int m=0; m<markers.size(); m++) {
					mark.append("\t" + markers.get(i) + "\n");
				}
				 
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

	/**
	 * Main function of this class: query the CNKB db for the interactions.
	 */
	private CNKBResultSet getInteractions(Long dataSetId,
			HashMap<Serializable, Serializable> params)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {

		String[] allInteractiomesSelected = ((String) params.get(CNKBParameters.INTERACTOME)).split("\\|");
		for(int i=0; i<allInteractiomesSelected.length; i++) {
			allInteractiomesSelected[i] = allInteractiomesSelected[i].split("\\(")[0].trim();	
		}
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
		if(dataSetAnnotationId==null) { /* this should never happen. */
			log.error("null annotation ID");
			return null;
		}
		Map<String, AnnotationEntry> annotationMap = new HashMap<String, AnnotationEntry>(); // TODO this may be more efficient by using JPA directly
		Annotation annotation = FacadeFactory.getFacade().find(Annotation.class, dataSetAnnotationId);
		for (AnnotationEntry entry : annotation.getAnnotationEntries()) {
			String probeSetId = entry.getProbeSetId();
			annotationMap.put(probeSetId, entry);
		}
		
		String[] selectedMarkerSet = (String[]) params
				.get(CNKBParameters.MARKER_SET_ID);
		if(selectedMarkerSet==null) {
			return CNKBStandaloneUI.getInteractions(params, userInfo, dataSetId);
		}
		List<String> selectedMarkers = new ArrayList<String>();
		for (int i = 0; i < selectedMarkerSet.length; i++) {
			List<String> temp = SubSetOperations.getMarkerData(Long.parseLong(selectedMarkerSet[i].trim()));
			for(int m=0; m<temp.size(); m++) {
				String marker = ((temp.get(m)).split("\\s+"))[0].trim();					 
				if (marker != null && !selectedMarkers.contains(marker))
				{
					selectedMarkers.add(marker);
				}
			}

		}
		log.debug("hist size " + selectedMarkers.size());

		CNKBServletClient cnkb = new CNKBServletClient();

		String interactomeName = "multiple interactomes";
		if(allInteractiomesSelected.length==1) interactomeName = allInteractiomesSelected[0] + version;
		CellularNetworkPreference cnkbPref = new CellularNetworkPreference(
				"Throttle Graph (" + interactomeName + ")");
		List<String> interactionTypes = cnkb
				.getInteractionTypesByInteractomeVersion(allInteractiomesSelected[0], version);
		cnkbPref.getDisplaySelectedInteractionTypes().addAll(interactionTypes);

		Vector<CellularNetWorkElementInformation> hits = new Vector<CellularNetWorkElementInformation>(); 
		for (String context : allInteractiomesSelected) {
		for(String marker: selectedMarkers) {
	
			int[] mf = new int[0];
			int[] bp = new int[0];
			AnnotationEntry a = annotationMap.get(marker);
			if (a != null) {
				mf = a.getMolecularFunction();
				bp = a.getBiologicalProcess();
			}
			
			/* get detail */
			AnnotationEntry entry = annotationMap.get(marker);
			String geneId = "";
			String geneSymbol =  "";
			if(entry!=null) {
				geneId = entry.getEntrezId().trim();
				geneSymbol = entry.getGeneSymbol().trim();
			}

			List<InteractionDetail> interactionDetails = null;
			if (geneId != null && !geneId.trim().equals("---")) {
				/* the earlier codes says to do this only when it is 'dirty'. it does not appear correct. */
				interactionDetails = cnkb
						.getInteractionsByEntrezIdOrGeneSymbol_2(geneId,
								geneSymbol, context, version, userInfo);
			}
			
			CellularNetWorkElementInformation element = new CellularNetWorkElementInformation(
					marker, context+" "+version, mf, bp, interactionDetails);
			hits.addElement(element);
			
			/* FIXME update preference. this is inherited from the earlier code. I don't think it does the correct thing
			 * considering this is repeated in this loop of all markers.*/
			if (interactionDetails != null && interactionDetails.size() > 0) {
				
				for(InteractionDetail detail : interactionDetails)
				{
					List<Short> typeIdList = detail.getConfidenceTypes();
					for (int j=0; j<typeIdList.size(); j++)
					{  
						Short typeId = typeIdList.get(j);
						Double maxConfidenceValue = cnkbPref.getMaxConfidenceValue(typeId);
						double confidenceValue = detail.getConfidenceValue(typeId);
						if (maxConfidenceValue == null )
							cnkbPref.getMaxConfidenceValueMap().put(typeId, new Double(confidenceValue));
						else
						{
							if (maxConfidenceValue < confidenceValue)
							{	 
								cnkbPref.getMaxConfidenceValueMap().put(typeId, new Double(confidenceValue));
							}
						}
						if (!cnkbPref.getConfidenceTypeList().contains(typeId))
							cnkbPref.getConfidenceTypeList().add(typeId);
					}
					 
				}
				if (cnkbPref.getSelectedConfidenceType() == null || cnkbPref.getSelectedConfidenceType().shortValue() == 0)
					cnkbPref.setSelectedConfidenceType(cnkbPref.getConfidenceTypeList().get(0)); //use first one as default value.
			}
		} /* end of the loop of all markers */
		} /* end of the loop of all interactomes */

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
			 
				session.setAttribute(CNKBParameters.CNKB_USERINFO, userName
						+ ":" + passwd);
				submitCnkbEvent(dataSetId);
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

	private void submitCnkbEvent(Long dataSetId) {
		resultSet = new ResultSet();
		java.sql.Timestamp timestamp =	new java.sql.Timestamp(System.currentTimeMillis());
		resultSet.setTimestamp(timestamp);
		String dataSetName = "CNKB - Pending";
		resultSet.setName(dataSetName);
		resultSet.setType(getResultType().getName());
		resultSet.setParent(dataSetId);
		resultSet.setOwner(user.getId());

		FacadeFactory.getFacade().store(resultSet);

		generateHistoryString();
	 
	    ((GeworkbenchRoot)app).addNode(resultSet);

		AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
				resultSet, params, CNKBUI.this);
		GeworkbenchRoot.getBlackboard().fire(analysisEvent);

	}

	@Override
	public String execute(Long resultId,
			HashMap<Serializable, Serializable> parameters, Long userId)
			throws IOException, Exception {
		try {
			CNKBResultSet result = getInteractions(
					dataSetId, params);
			FacadeFactory.getFacade().store(result);

			ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, resultId);
			resultSet.setDataId(result.getId());
			FacadeFactory.getFacade().store(resultSet);
		} catch (UnAuthenticatedException uae) {
			creatAuthenticationDialog(dataSetId);
			return "UnAuthenticatedException";
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new IOException("null pointer caught in CNKBUI"+e); // using IOException because of the limitation of the interface AnalysisUI
		}
		return "CNKB";
	}

	private static final String PROPERTIES_FILE = "application.properties";
	static { // this class should not depend on class GeworkbenchRoot being loaded first.
		try {
			Properties prop = new Properties();
			prop.load(CNKBUI.class.getClassLoader().getResourceAsStream(
					"/" + PROPERTIES_FILE));
			multiSelectAllowed = prop.getProperty("cnkb.multi.select").equalsIgnoreCase("yes");
			excludedInteractomes = Arrays.asList(prop.getProperty("cnkb.interactome.excluded").split(","));
			excludedPrefixes = prop.getProperty("cnkb.interactome.prefix.excluded").split(",");
		} catch (IOException e) {
			log.error("failed to read application properties file " + PROPERTIES_FILE);
			e.printStackTrace();
		}
	}
}
