package org.geworkbenchweb.plugins.marina;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
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
import org.geworkbenchweb.pojos.MraResult;
import org.geworkbenchweb.pojos.Network;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class MarinaUI extends VerticalLayout implements Upload.SucceededListener,Upload.FailedListener,Upload.Receiver, AnalysisUI {

	private static final long serialVersionUID = 845011602285963638L;
	private Log log = LogFactory.getLog(MarinaUI.class);

	protected Form form = new Form();
	private Upload upload = null;
	private CheckBox priorBox = new CheckBox("Retrieve Prior Result");
	protected Button submitButton = new Button("Submit", form, "commit");
	private ClassSelector classSelector = null;	 
	private OptionGroup og = null;
	private ComboBox networkNodes = null;
	protected MarinaParamBean bean = null;
	protected BeanItem<MarinaParamBean> item = null;
	protected HashMap<String, String> arraymap = null;
	protected boolean allpos = true;
	private final ByteArrayOutputStream os = new ByteArrayOutputStream(10240);
	private final String[] order = {"network", "gseaPValue", 
			"minimumTargetNumber", "minimumSampleNumber", "gseaPermutationNumber",
			"gseaTailNumber", "shadowPValue", "synergyPValue", "retrievePriorResultWithId"};
	private final String priorStr = order[order.length-1];
	private static final String analysisName = "MARINa";
	private static final String selectNetworkNode = "Select Network Node";
	private static final String uploadNetworkFile = "Upload Network File";
	private static final String[] networkOptions = {selectNetworkNode, uploadNetworkFile};

	protected Long dataSetId = null;
	
	public MarinaUI() {
		this(0L);
	}
	
	public MarinaUI(Long dataId){
		this.dataSetId = dataId;
		
		arraymap = new HashMap<String, String>();
		classSelector = new ClassSelector(dataSetId,  SessionHandler.get().getId(), "MarinaUI", this);
	 

		setDataSetId(dataSetId);	 

		form.getLayout().addComponent(classSelector.getArrayContextCB());
		form.getLayout().addComponent(classSelector.getH1());
		form.getLayout().addComponent(classSelector.getH2());
		
		og = new OptionGroup("Load Network Method", Arrays.asList(networkOptions));
		og.setImmediate(true);
		og.select(uploadNetworkFile);
		og.addListener(new ValueChangeListener(){
			private static final long serialVersionUID = -6950089065248041770L;
			public void valueChange(ValueChangeEvent e){
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
		form.getLayout().addComponent(og);
		
		networkNodes = new ComboBox("Select Network Node");
		networkNodes.setImmediate(true);
		networkNodes.setVisible(false);
		networkNodes.addListener(new ValueChangeListener(){
			private static final long serialVersionUID = -6950089065248041770L;
			public void valueChange(ValueChangeEvent e){
				Long itemId = (Long)e.getProperty().getValue();
				if(itemId == null) { networkNotLoaded(null); return; }
				item.getItemProperty("network").setValue(networkNodes.getItemCaption(itemId));
				
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
				}

				NetworkCreator networkCreator = new NetworkCreator(MarinaUI.this);
				networkLoaded(networkCreator.getNetworkString(network));
				networkCreator.printWarning();
			}
		});
		form.getLayout().addComponent(networkNodes);
		
		upload = new Upload("Upload Network File", this);
		upload.setButtonCaption("Upload");
		upload.addListener((Upload.SucceededListener)this);
        upload.addListener((Upload.FailedListener)this);
		form.getLayout().addComponent(upload);

		bean = new MarinaParamBean();
		DefaultFieldFactory.createCaptionByPropertyId(bean);
		item = new BeanItem<MarinaParamBean>(bean, order);
		form.setImmediate(true);
		form.setFormFieldFactory(new DefaultFieldFactory(){
			private static final long serialVersionUID = 4805200657491765148L;
			public Field createField(Item item, Object propertyId, Component uiContext) {
				Field f = super.createField(item, propertyId, uiContext);
				fieldTitleToUppercase(f, "Gsea");
				fieldTitleToUppercase(f, "Id");
				if (propertyId.equals("minimumTargetNumber") || propertyId.equals("gseaPermutationNumber") ||
	            	propertyId.equals("minimumSampleNumber") || propertyId.equals("gseaTailNumber")) {
					TextField tf = (TextField) f;
					if (propertyId.equals("gseaTailNumber"))
						tf.addValidator(new PositiveIntValidator("Please enter 1 or 2", 2));
					else tf.addValidator(new PositiveIntValidator("Please enter a positive integer"));
				} else if (propertyId.equals("shadowPValue") || propertyId.equals("synergyPValue") ||
	            		propertyId.equals("gseaPValue")) {
					TextField tf = (TextField) f;
					tf.addValidator(new PvalueValidator("P value must be in the range of 0 to 1"));
				} else if (propertyId.equals("retrievePriorResultWithId")) {
					TextField tf = (TextField) f;
					tf.addValidator(new RegexpValidator("^[mM][rR][aA]\\d+$", 
							"MRA Result ID must be 'mra' followed by an integer"));
					}
	            return f;
	        }
		});
		form.setItemDataSource(item);

		form.getField("network").setWidth("270px");
		form.getField("network").setReadOnly(true);
		form.getField("network").setEnabled(false);
		form.getField("retrievePriorResultWithId").setEnabled(false);

		priorBox.setImmediate(true);
		priorBox.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = -5548846734511323624L;
			public void valueChange(ValueChangeEvent event) {
				if (priorBox.booleanValue()){
					for (String item : order)
						form.getField(item).setEnabled(false);
					og.setEnabled(false);
					networkNodes.setEnabled(false);
					upload.setEnabled(false);
					classSelector.getArrayContextCB().setEnabled(false);
					classSelector.getH1().setEnabled(false);
				    classSelector.getH2().setEnabled(false);
					form.getField("retrievePriorResultWithId").setEnabled(true);
					if (form.isValid()) submitButton.setEnabled(true);
				}else {
					for (String item : order)
						form.getField(item).setEnabled(true);
					og.setEnabled(true);
					networkNodes.setEnabled(true);
					upload.setEnabled(true);
					classSelector.getArrayContextCB().setEnabled(true);
					classSelector.getH1().setEnabled(true);
					classSelector.getH2().setEnabled(true);
					form.getField("retrievePriorResultWithId").setEnabled(false);
					if (form.getField("network").getValue().equals("")) form.getField("network").setEnabled(false);
					if (classSelector.getTf1().isEnabled() && form.getField("network").isEnabled())
						submitButton.setEnabled(true);
					else submitButton.setEnabled(false);
				}
			}
		});
		form.getLayout().addComponent(priorBox);

		submitButton.setEnabled(false);
		submitButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1085633263164082701L;

			@Override
			public void buttonClick(ClickEvent event) {			 
			    String warnMsg = validInputClassData(classSelector.getClass1ArraySet(), classSelector.getClass2ArraySet());
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
					
					
				ResultSet resultSet = storePendingResultSet();
				
				HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>(); 
				params.put("bean", bean);

				AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
						resultSet, params, MarinaUI.this);
				GeworkbenchRoot.getBlackboard().fire(analysisEvent);
			}
		});
		form.getFooter().addComponent(submitButton);

		addComponent(form);
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
	/* 
	 * convert abbrev in field title to uppercase
	 */
	private void fieldTitleToUppercase(Field f, String abbrev){
		String caption = f.getCaption();
		if (caption.contains(abbrev))
			f.setCaption(caption.replace(abbrev, abbrev.toUpperCase()));
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
		
		GeworkbenchRoot app = (GeworkbenchRoot) MarinaUI.this.getApplication();
		app.addNode(resultSet);

		return resultSet;
	}
		
	private void generateHistoryString(Long resultSetId) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Marina Parameters : \n");
		if(priorBox.booleanValue()){
			builder.append(form.getField(priorStr).getCaption()+" - " + form.getField(priorStr).getValue() + "\n");
		}else{
			for (String item : order){
				if(!item.equals(priorStr))
					builder.append(form.getField(item).getCaption()+" - " + form.getField(item).getValue() + "\n");
			}
		}
		
		String class1str = bean.getClass1();
		String class2str = bean.getClass2();
		int n1 = class1str.equals("")?0:class1str.split(",").length;
		int n2 = class2str.equals("")?0:class2str.split(",").length;
		builder.append("\nArrays used (" + (n1+n2) + ") - \n" );
		builder.append("Cases (" + n1 + ") - \n\t");
		if(n1>0) builder.append(class1str.replaceAll(",", "\n\t"));
		builder.append("\nControls (" + n2 + ") - \n\t");
		if(n2>0) builder.append(class2str.replaceAll(",", "\n\t"));
		
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
	public OutputStream receiveUpload(String filename, String mimeType) {
		item.getItemProperty("network").setValue(filename);
		os.reset();
		return os;
	}

	// This is called if the upload fails.
	public void uploadFailed(FailedEvent event) {
		String fname = event.getFilename();
		log.info("Failed to upload "+fname);
		networkNotLoaded("Network file " + fname + " failed to upload.");
	}

	// This is called if the upload is finished.
	public void uploadSucceeded(SucceededEvent event) {
		bean.setNetworkString(new String(os.toByteArray(), Charset.defaultCharset()));
		NetworkDialog dialog = new NetworkDialog(this);
		dialog.openDialog();
	}

	protected void networkNotLoaded(String msg){
		bean.setNetworkString(null);
		item.getItemProperty("network").setValue("");
		form.getField("network").setEnabled(false);
		submitButton.setEnabled(false);
		if (msg != null){
			MessageBox mb = new MessageBox(getWindow(), 
					"Network Problem", MessageBox.Icon.ERROR, msg, 
					new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();
		}
	}

	protected void networkLoaded(String networkString){
		bean.setNetworkString(networkString);
		form.getField("network").setEnabled(true);
		if (classSelector.getTf1().isEnabled())
			submitButton.setEnabled(true);
		
		if (allpos && bean.getGseaTailNumber()==2){
			MessageBox mb = new MessageBox(
					getWindow(),
					"Information",
					MessageBox.Icon.INFO,
					"Since all Spearman's correlation >= 0, gsea will use tail = 1.",
					new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();
			bean.setGseaTailNumber(1);
			item.getItemProperty("gseaTailNumber").setValue(1);
		}
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

		bean.reset();
		classSelector.setData(dataSetId, SessionHandler.get().getId());
		addNetworkNodes(SessionHandler.get().getId(), dataId);
		og.select(uploadNetworkFile);
		priorBox.setValue(false);
	 
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
					 return "Same array (" + arrays.get(j) + ") exists in class1 array groups.";				 
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
				    return "Class1 and class2 groups have same array set " + subset.getName() + ".";
			   }
				List<String> arrays = SubSetOperations.getArrayData(Long
						.parseLong(selectedclass2Sets[i].trim()));	 			 
			   for (int j = 0; j < arrays.size(); j++) {
				  if (microarrayPosList.contains(arrays.get(j)))  				
					 return "Same array (" + arrays.get(j) + ") exists in class2 array groups.";				 
				   microarrayPosList.add(arrays.get(j));				 
			   }
		    }
		}
		
		return null;
		
	}	
	

	@Override
	public Class<?> getResultType() {
		return org.geworkbenchweb.pojos.MraResult.class;
	}

	@Override
	public String execute(Long resultId,
			HashMap<Serializable, Serializable> parameters, Long userId)
			throws IOException, Exception {
		MarinaAnalysis analyze = new MarinaAnalysis(dataSetId, parameters);

		MraResult result = analyze.execute();
		FacadeFactory.getFacade().store(result);
		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, resultId);
		resultSet.setDataId(result.getId());
		FacadeFactory.getFacade().store(resultSet);

		return analysisName + " - " + result.getLabel();
	}
}
