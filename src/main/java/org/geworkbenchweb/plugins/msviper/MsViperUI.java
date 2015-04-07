package org.geworkbenchweb.plugins.msviper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSet; 
import org.geworkbenchweb.pojos.Network;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.pojos.MsViperResult;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.SubSetOperations;
 
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
 
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
 
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.data.validator.IntegerValidator; 
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;  
 
import com.vaadin.ui.FormLayout; 
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class MsViperUI extends VerticalLayout implements Upload.SucceededListener,Upload.FailedListener,Upload.Receiver, AnalysisUI {

	private static final long serialVersionUID = 845011602285963638L;
	private Log log = LogFactory.getLog(MsViperUI.class);
 
	protected final String CASE = "case";
	protected final String CONTROL = "control";
	
	private final MicroarrayContext arrayContext;
	private final MicroarraySetSelect caseSelect;
	private final MicroarraySetSelect controlSelect;
	
	private CheckBox limitBox = new CheckBox("Limit gene expression signature to genes in interactome " + QUESTION_MARK);
	private CheckBox bootstrapBox = new CheckBox("Bootstrapping (not normally needed) " + QUESTION_MARK);
	private CheckBox shadowBox = new CheckBox("Shadow Analysis " + QUESTION_MARK);
	//private CheckBox priorBox = new CheckBox("Retrieve Prior Result " + QUESTION_MARK);
	protected Button submitButton = new Button("Submit", new SubmitListener());

	private OptionGroup ogNetwork = null;
	//private OptionGroup ogNetwork = null;
	private OptionGroup ogBootstrap = null;
	private OptionGroup ogShadow  = null;
	private ComboBox networkNodes = null;
	private TextField networkTF = new TextField();
	private TextField minAllowedRegulonTF = new TextField();
	private TextField shadowValue = new TextField();
	
	
	
	protected MsViperParam param = null;	 
	protected HashMap<String, String> arraymap = null;
	protected boolean allpos = true;
	
	 
	private static String QUESTION_MARK = " \uFFFD";
	 
	private static final String analysisName = "MsViper";
	private static final String selectNetworkNode = "Select Network Node";
	private static final String uploadNetworkFile = "Upload Network File";	
	private static final String[] networkOptions = {selectNetworkNode, uploadNetworkFile};
	private static final String average = "Average";
	private static final String median = "Median";
	private static final String mode = "Mode";	 
	private static final String topMasterRegulators = "Top Master Regulators";
	private static final String enrichmentPvalueCutoff = "Enrichment P-value Cutoff";
	 

	protected Long dataSetId = null;
	protected Long userId = null;
	
	public MsViperUI() {
		this(0L);
	}
	
	public MsViperUI(Long dataId){
		this.dataSetId = dataId;		
	    
		FormLayout f1 = new FormLayout();
		FormLayout f2 = new FormLayout();	 
		FormLayout f3 = new FormLayout();	 
		HorizontalLayout h1 = new HorizontalLayout();		 
		
		final Upload upload = new Upload("", this);
		upload.setButtonCaption("Upload");
		upload.setDescription("Available network formats are ARACNe adjacency matrix (.adj), SIF (.sif), and 5-column (.txt)");
		upload.addListener((Upload.SucceededListener)this);
        upload.addListener((Upload.FailedListener)this);
        
		arraymap = new HashMap<String, String>();
		caseSelect = new MicroarraySetSelect(dataSetId,  SessionHandler.get().getId(), "ViperUI", this, "Case", "Select microarray set(s) to use as Case", CASE);
		controlSelect = new MicroarraySetSelect(dataSetId,  SessionHandler.get().getId(), "ViperUI", this, "Control", "Select microarray set(s) to use as Control", CONTROL);
		arrayContext = new MicroarrayContext(dataSetId,  SessionHandler.get().getId(), "ViperUI", this, caseSelect, controlSelect);
	 
		userId = SessionHandler.get().getId();
		
		setDataSetId(dataSetId);	 

		f1.addComponent(arrayContext);
		f1.addComponent(caseSelect);
		f1.addComponent(controlSelect);
		
		ogNetwork = new OptionGroup("Network Source " + QUESTION_MARK, Arrays.asList(networkOptions));
		ogNetwork.setDescription("MARINa requires an interaction network. Choose whether to use an existing network node from the Workspace, or upload the network from a file. ");
		ogNetwork.setImmediate(true);
		ogNetwork.select(uploadNetworkFile);
		ogNetwork.addListener(new ValueChangeListener(){
			private static final long serialVersionUID = -6950089065248041770L;
			public void valueChange(ValueChangeEvent e){
				if(e.getProperty().getValue().equals(selectNetworkNode)){
					networkNodes.setEnabled(true);
					upload.setEnabled(false);
					networkNodes.select(null);
				}else{
					networkNodes.setEnabled(false);
					upload.setEnabled(true);		
					networkNotLoaded(null);
				}
			}
		});
		
	
		f1.addComponent(ogNetwork);		 
		
		//networkNodes = new ComboBox("Select Network Node " + QUESTION_MARK);
		networkNodes = new ComboBox();
		networkNodes.setDescription("Select a network node from those available in the Workspace");
		networkNodes.setImmediate(true);
		networkNodes.setEnabled(false);
		networkNodes.addListener(new ValueChangeListener(){
			private static final long serialVersionUID = -6950089065248041770L;
			public void valueChange(ValueChangeEvent e){
				Long itemId = (Long)e.getProperty().getValue();
				if(itemId == null) { networkNotLoaded(null); return; }
				networkTF.setReadOnly(false);
				networkTF.setValue(networkNodes.getItemCaption(itemId));	
				networkTF.setReadOnly(true); 
				
				ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, itemId);
				Long id = resultSet.getDataId();
				if(id==null) {
					networkNotLoaded("Failed to load the network selected. Result Set Id="+itemId);
					return;
				}
				Network network = FacadeFactory.getFacade().find(Network.class, id);
				if(network==null) {
					networkNotLoaded("Failed to load the network selected. Network Id="+id);
					return;
				} else if (network.getNodeNumber()==0) {
					networkNotLoaded("Zero node in the network. Network Id="+id);
					return;
				}

				NetworkCreator networkCreator = new NetworkCreator(MsViperUI.this);
				try {
					String networkFname = networkTF.getValue().toString().trim().replace(" ", "");
					if (!networkFname.endsWith(".adj"))
						networkFname = networkFname + ".adj";
					networkCreator.createNetworkFile(network, networkFname);
					networkLoaded();				 
				} catch (IOException e1) {
					e1.printStackTrace();
					networkNotLoaded("failed creating network file");
				}
			}
		});
		f1.addComponent(networkNodes);
		
        f1.addComponent(upload);

	
		networkTF.setCaption("Network"+ "  " + QUESTION_MARK);
		networkTF.setDescription("Name of loaded network file.");
		networkTF.setWidth("270px");
		networkTF.setReadOnly(true);
		networkTF.setEnabled(false);
		f1.addComponent(networkTF);
		
		/* this is not a real button, just to show text with tooltip */
		final Button networkRequirements = new Button("Network Requirements");
		networkRequirements.setStyleName(Reindeer.BUTTON_LINK);
 
		String desc = "For 2-tailed GSEA with an adjacency matrix network, the expression node should be the complete dataset from "
				+ "which the network (adjacency matrix) was originally calculated.<p>If it is not, a 5-column-format "
				+ "network file (with regulon correlation values from the original dataset) should be loaded instead.";
		networkRequirements.setDescription(desc);	
		f1.addComponent(networkRequirements);
		
		param = new MsViperParam();		 
		 
		
		
		//minAllowedRegulonTF.setCaption("Minimum allowed regulon size" + "  " + QUESTION_MARK);
		minAllowedRegulonTF.setValue("25");
		
		minAllowedRegulonTF.setNullSettingAllowed(false);		
	 
		minAllowedRegulonTF.addValidator(new PositiveIntValidator("Please enter a positive integer."));
		
		h1.addComponent(minAllowedRegulonTF);
		 
		h1.setCaption("Minimum allowed regulon size" + "  " + QUESTION_MARK);
		h1.setDescription("Minimum allowed regulon size");
		h1.setSpacing(true);		 
		h1.setImmediate(true);		
		f2.addComponent(h1);
		
		
		limitBox.setImmediate(true);
		limitBox.setValue(true);
		limitBox.setDescription("Limit gene expression signature to genes in interactome.");
		limitBox.addListener(new ValueChangeListener() {		  
			private static final long serialVersionUID = 4274487651959112682L;
			public void valueChange(ValueChangeEvent e) {
				if(e.getProperty().getValue().equals(selectNetworkNode)){
					networkNodes.setVisible(true);
					upload.setVisible(false);
					networkNodes.select(null);
				}else{
					networkNodes.setVisible(false);
					upload.setVisible(true);		
					networkNotLoaded(null);
				}
			}
		});
		
		
		bootstrapBox.setImmediate(true);
		bootstrapBox.setDescription("Bootstrapping (not normally needed).");
		bootstrapBox.addListener(new ValueChangeListener() {	 
			private static final long serialVersionUID = 4998585092911587369L;
			public void valueChange(ValueChangeEvent event) {
				 if (bootstrapBox.booleanValue())
					 ogBootstrap.setEnabled(true);
			     else
			    	 ogBootstrap.setEnabled(false);
			}
		});
		ogBootstrap = new OptionGroup();
		ogBootstrap.addItem(average);
		ogBootstrap.addItem(median);
		ogBootstrap.addItem(mode);
		ogBootstrap.setDescription("Choose an option.");
		ogBootstrap.setImmediate(true);
		ogBootstrap.select(average);
		ogBootstrap.setEnabled(false);
		ogBootstrap.addListener(new ValueChangeListener(){		 
			private static final long serialVersionUID = -6431988302489281253L;
			public void valueChange(ValueChangeEvent e){
				 
			}
		});		 
		ogBootstrap.setStyleName("viper");
		
		final Label createReport = new Label("Create Report For:");	
		createReport.setEnabled(false);
		createReport.addStyleName("createreport");		 
		ogShadow = new OptionGroup();
		ogShadow.addItem(topMasterRegulators);
		ogShadow.addItem(enrichmentPvalueCutoff);
		ogShadow.setDescription("Choose an option.");
		ogShadow.setImmediate(true);
		ogShadow.select(topMasterRegulators);
		ogShadow.setEnabled(false);
		ogShadow.addListener(new ValueChangeListener(){				 
			private static final long serialVersionUID = -8023653010092356915L;
			public void valueChange(ValueChangeEvent e){
				shadowValue.removeAllValidators();
				if(e.getProperty().getValue().equals(topMasterRegulators)){					
					shadowValue.setValue(25);
					shadowValue.addValidator(new PositiveIntValidator("Please enter a positive integer."));					
				}else{
					shadowValue.setValue(0.01);
					shadowValue.addValidator(new PvalueValidator("P value must be in the range of 0 to 1"));
				}
			}
		});
		ogShadow.setStyleName("viper");
		shadowValue.setValue(25);
		shadowValue.setEnabled(false);	     
		f3.setImmediate(true);		 
		f3.addComponent(shadowValue);
		f3.addStyleName("f3");
		 
		shadowBox.setImmediate(true);
		shadowBox.setDescription("Shadow Analysis.");
		shadowBox.addListener(new ValueChangeListener() {	 
			private static final long serialVersionUID = 4740368605255326886L;
			public void valueChange(ValueChangeEvent event) {
				 if (shadowBox.booleanValue())
				 {
					 createReport.setEnabled(true);
					 ogShadow.setEnabled(true);
					 shadowValue.setEnabled(true);					 
				 }
			     else
			     {
			    	 createReport.setEnabled(false);
			    	 ogShadow.setEnabled(false);
			    	 shadowValue.setEnabled(false);
			     }
			}
		});
		
		

		addComponent(f1);
		addComponent(limitBox);
		addComponent(f2);			 
		addComponent(bootstrapBox);		
		addComponent(ogBootstrap);	 
		addComponent(shadowBox);			
		addComponent(createReport);	
		addComponent(ogShadow);
		addComponent(f3);
		addComponent(new Label("   "));
		
		/*BarcodeTable myTable = new BarcodeTable();
		  myTable.setImmediate(true);
		  myTable.setSizeFull();
		  addComponent(myTable);  */
		 
		
		submitButton.setEnabled(false);
	 
		addComponent(submitButton);
        
		this.setSpacing(true);
		this.setImmediate(true);
	}
	
	private void addNetworkNodes(Long userId, Long dataId){
		networkNodes.removeAllItems();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("owner", userId);
		params.put("parent", dataId);
		List<AbstractPojo> results = FacadeFactory
				.getFacade()
				.list("Select p from ResultSet as p where p.owner=:owner and p.parent=:parent ORDER by p.timestamp",
						params);

		for (int j = 0; j < results.size(); j++) {
			String nodeName = ((ResultSet) results.get(j)).getName();
			Long   nodeId   = ((ResultSet) results.get(j)).getId();
			String nodeType = ((ResultSet) results.get(j)).getType();

			if (nodeType.equals(Network.class.getName()) && 
					!nodeName.contains("Pending")) {
				networkNodes.addItem(nodeId);
				networkNodes.setItemCaption(nodeId, nodeName);
			}
		}
	}
	 
	 
	
	private ResultSet storePendingResultSet() {

		ResultSet resultSet = new ResultSet();
		java.sql.Timestamp timestamp =	new java.sql.Timestamp(System.currentTimeMillis());
		resultSet.setTimestamp(timestamp);
		String dataSetName = analysisName + " - Pending";
		resultSet.setName(dataSetName);
		resultSet.setType(getResultType().getName());
		resultSet.setParent(dataSetId);
		resultSet.setOwner(SessionHandler.get().getId());
		FacadeFactory.getFacade().store(resultSet);

		generateHistoryString(resultSet.getId());
		
		GeworkbenchRoot app = (GeworkbenchRoot) MsViperUI.this.getApplication();
		app.addNode(resultSet);

		return resultSet;
	}
		
	private void generateHistoryString(Long resultSetId) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Marina(msviper) Parameters : \n");
	  
			builder.append(networkTF.getCaption()+" - " + networkTF.getValue() + "\n");
			builder.append(minAllowedRegulonTF.getCaption()+" - " + minAllowedRegulonTF.getValue() + "\n");
		 
	    int n1 = param.getClassCase().keySet().size();
		int n2 = param.getClassControl().size();
		String casestr = getClassString(param.getClassCase());
		String controlstr = getClassString(param.getClassControl());
		builder.append("\nArrays used (" + (n1+n2) + ") - \n" );
		builder.append("Cases (" + n1 + ") - \n\t");
		if(n1>0) builder.append(casestr);
		builder.append("\nControls (" + n2 + ") - \n\t");
		if(n2>0) builder.append(controlstr);
		
		Long masetId = null;
		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		if(dataset != null) masetId = dataset.getDataId();
		String[] markers = DataSetOperations.getStringLabels("markerLabels", masetId);
		builder.append("\nMarkers used (" + (markers==null?0:markers.length) + ") - \n");
		if(markers != null){
			for(String markerName : markers)
				builder.append("\t").append(markerName).append("\n");
		}
		
		DataHistory his = new DataHistory();
		his.setParent(resultSetId);
		his.setData(builder.toString());
		FacadeFactory.getFacade().store(his);
	}
	
	// Callback method to begin receiving the upload.
	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		/* because the file must be under a directory named by 'run ID' that is only created when execute() is called,
		 * we have to upload first then copy it later. */
		/* temporary file location */
		String dirName = GeworkbenchRoot.getBackendDataDirectory() + File.separator
				+ "networks" + File.separator + "msViper" + File.separator +userId;
		File dir = new File(dirName);
		if (!dir.exists())
			dir.mkdirs();

		FileOutputStream fos = null; // Output stream to write to
		File file = new File(dirName + File.separator + filename);
        try {
            // Open the file for writing.
            fos = new FileOutputStream(file);
        } catch (final java.io.FileNotFoundException e) {
            // Error while opening the file. Not reported here.
            e.printStackTrace();
            return null;
        }

        return fos; // Return the output stream to write to
	}

	// This is called if the upload fails.
	@Override
	public void uploadFailed(FailedEvent event) {
		String fname = event.getFilename();
		log.info("Failed to upload "+fname);
		networkNotLoaded("Network file " + fname + " failed to upload.");
	}

	// This is called if the upload is finished.
	@Override
	public void uploadSucceeded(SucceededEvent event) {
		String networkName = event.getFilename();
		if (!networkName.toLowerCase().endsWith(".adj"))
		{
			networkNotLoaded("The upload network should be adj file.");
			return;
		}
		
		networkTF.setReadOnly(false);
		networkTF.setValue(networkName);
		networkTF.setReadOnly(true);	 
		NetworkDialog dialog = new NetworkDialog(this, networkName);
		dialog.openDialog();
	}

	void networkNotLoaded(String msg){
		networkTF.setReadOnly(false);
		networkTF.setValue("");
		networkTF.setReadOnly(true);
		networkTF.setEnabled(false);		 
		submitButton.setEnabled(false);
		if (msg != null){
			MessageBox mb = new MessageBox(getWindow(), 
					"Network Problem", MessageBox.Icon.ERROR, msg, 
					new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();
		}
	}

	void networkLoaded(){
		networkTF.setEnabled(true);
		if (caseSelect.isTextFieldEnabled() && controlSelect.isTextFieldEnabled())
			submitButton.setEnabled(true);	 
	}
	
	public class PositiveIntValidator extends IntegerValidator {
		private static final long serialVersionUID = -8205632597275359667L;
		private int max = 0;
		public PositiveIntValidator(String message){
			super(message);
		}
		public PositiveIntValidator(String message, int max){
			this(message);
			this.max = max;
		}
		protected boolean isValidString(String value){
			try {
				int n = Integer.parseInt(value);
				if (n <= 0) return false;
				if (max > 0 && n > max) return false;
			} catch (Exception e) {
				return false;
			}
			submitButton.setComponentError(null);
			return true;
		}
	}
	public class PvalueValidator extends DoubleValidator {
		private static final long serialVersionUID = -815490638929041408L;
		public PvalueValidator(String errorMessage) {
			super(errorMessage);
		}
		protected boolean isValidString(String value){
			try {
				double n = Double.parseDouble(value);
				if (n < 0 || n > 1) return false;
			} catch (Exception e) {
				return false;
			}
			submitButton.setComponentError(null);
			return true;
		}
	}
	
	 
	
	// FIXME most of the null checkings should be designed out of the process
	// meaning if they are allowed to be null, it should be very clear when we expect them to be null
	@Override
	public void setDataSetId(Long dataId) {
		this.dataSetId = dataId;
		
		if(dataSetId==null || dataSetId==0) return;

		param.reset();
		arrayContext.setData(dataSetId, SessionHandler.get().getId());
		addNetworkNodes(SessionHandler.get().getId(), dataId);
		ogNetwork.select(uploadNetworkFile);
		 
	 
		arraymap.put(null, "");
	}
	
	private String validInputClassData(String[] selectedClass1Sets, String[] selectedclass2Sets)
	{    
	  
		List<String> microarrayPosList = new ArrayList<String>();
		List<String> caseSetList = new ArrayList<String>();
		/* for each group */
		if (selectedClass1Sets != null)
		{
			for (int i = 0; i < selectedClass1Sets.length; i++) {			
			    caseSetList.add(selectedClass1Sets[i].trim());
			    List<String> arrays = SubSetOperations.getArrayData(Long
						.parseLong(selectedClass1Sets[i].trim()));	 
			 
			    for (int j = 0; j < arrays.size(); j++) {
				   if (microarrayPosList.contains(arrays.get(j)))  				
					 return "Same array (" + arrays.get(j) + ") exists in case array groups.";				 
				   microarrayPosList.add(arrays.get(j));				 
			    }
		    }
		}
		microarrayPosList.clear();
		if (selectedclass2Sets != null)
		{ 
			for (int i = 0; i < selectedclass2Sets.length; i++) {		 
			   if (caseSetList.contains(selectedclass2Sets[i].trim()))
			   {
					SubSet subset = SubSetOperations.getArraySet(Long
							.parseLong(selectedclass2Sets[i].trim()));
				    return "Case and Control groups have same array set " + subset.getName() + ".";
			   }
				List<String> arrays = SubSetOperations.getArrayData(Long
						.parseLong(selectedclass2Sets[i].trim()));	 			 
			   for (int j = 0; j < arrays.size(); j++) {
				  if (microarrayPosList.contains(arrays.get(j)))  				
					 return "Same array (" + arrays.get(j) + ") exists in control array groups.";				 
				   microarrayPosList.add(arrays.get(j));				 
			   }
		    }
		}
		
		return null;
		
	}	
	

	@Override
	public Class<?> getResultType() {
		return org.geworkbenchweb.pojos.MsViperResult.class;
	}

	@Override
	public String execute(Long resultId,
			HashMap<Serializable, Serializable> parameters, Long userId)
			throws IOException, Exception {
		
		MsViperAnalysisClient analyze = new MsViperAnalysisClient(dataSetId, userId, (MsViperParam)parameters.get("bean"));
		MsViperResult msViperResult = analyze.execute();
		
		FacadeFactory.getFacade().store(msViperResult);
		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, resultId);
		resultSet.setDataId(msViperResult.getId());
		FacadeFactory.getFacade().store(resultSet);		
		return  msViperResult.getLabel();  		 
		
	}
	
	/* to keep the earlier behavior. not a clean design. */
	boolean isNetworkNameEnabled() {
		return networkTF.isEnabled();
	} 
	
	
	private class SubmitListener implements ClickListener {

		private static final long serialVersionUID = 1085633263164082701L;

		@Override
		public void buttonClick(ClickEvent event) {			 
		    String warnMsg = validInputClassData(caseSelect.getArraySet(), controlSelect.getArraySet());
			if( warnMsg != null ) 
			{ 
				MessageBox mb = new MessageBox(getWindow(), 
			 
					"Warning", 
					MessageBox.Icon.INFO, 
					warnMsg,
					new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			    mb.show();
			    return;
			}
			
			param.setNetwork(networkTF.getValue().toString().trim());
			param.setGesfilter(limitBox.booleanValue());			 
			param.setMinAllowedRegulonSize(new Integer(minAllowedRegulonTF.getValue().toString().trim()));
			param.setBootstrapping(bootstrapBox.booleanValue());			 
			param.setMethod(ogBootstrap.getValue().toString().toLowerCase());
			if(param.getMethod().equalsIgnoreCase("average"))
				param.setMethod("mean");
			param.setShadow(shadowBox.booleanValue());		 
			param.setShadowValue(new Float(shadowValue.getValue().toString().trim()));
			 
			
			ResultSet resultSet = storePendingResultSet();
			HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>(); 
			params.put("bean", param);

			AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
					resultSet, params, MsViperUI.this);
			GeworkbenchRoot.getBlackboard().fire(analysisEvent);
		}
	}
	
	
	private String getClassString(Map<String, String> classMap)
	{
		StringBuilder builder = new StringBuilder();		 
		for (String key: classMap.keySet())
		{		 
		    builder.append(key + "\n");			 
		}
		return builder.toString();
	}
	
}
