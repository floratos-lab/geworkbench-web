package org.geworkbenchweb.plugins.marina;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMasterRegulatorTableResultSet;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.LayoutUtil;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.UserDirUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.steinwedel.messagebox.ButtonId;
import de.steinwedel.messagebox.Icon;
import de.steinwedel.messagebox.MessageBox;

public class MarinaUI extends VerticalLayout implements Upload.SucceededListener,Upload.FailedListener,Upload.Receiver, AnalysisUI {

	private static final long serialVersionUID = 845011602285963638L;
	private Log log = LogFactory.getLog(MarinaUI.class);
	protected DSMicroarraySet dataSet;
	protected Form form = new Form();
	private Upload upload = null;
	private CheckBox priorBox = new CheckBox("Retrieve Prior Result");
	protected Button submitButton = new Button("Submit");
	private ClassSelector classSelector = null;	 
	protected MarinaParamBean bean = null;
	private BeanItem<MarinaParamBean> item = null;
	protected HashMap<String, String> arraymap = null;
	private Window loadDialog;
	private ComboBox formatBox;
	private ComboBox presentBox;
	private boolean allpos = true;
	private int correlationCol = 3;
	private String selectedRepresentedBy = AdjacencyMatrixDataSet.PROBESET_ID;
	private HashMap<String, String> interactionTypeMap = null;
	private boolean isRestrict = true;
	private final ByteArrayOutputStream os = new ByteArrayOutputStream(10240);
	private String selectedFormat = AdjacencyMatrixDataSet.ADJ_FORMART;
	private String marina5colformat = "marina 5-column format";
	public static final String SIF_FORMART = "sif format";
	public static final String ADJ_FORMART = "adj format";
	public static final String GENE_NAME = "gene name";
	public static final String ENTREZ_ID = "entrez id";
	public static final String PROBESET_ID = "probeset id";
	private final String[] order = {"network", "gseaPValue", 
			"minimumTargetNumber", "minimumSampleNumber", "gseaPermutationNumber",
			"gseaTailNumber", "shadowPValue", "synergyPValue", "retrievePriorResultWithId"};
	private static final String analysisName = "MARINa";

	private Long dataSetId = null;
	
	public MarinaUI() {
		this(0L);
	}
	
	public MarinaUI(final Long dataSetId){
		this.dataSetId = dataSetId;
		
		arraymap = new HashMap<String, String>();
		classSelector = new ClassSelector(dataSetId,  SessionHandler.get().getId(), "MarinaUI", this);
	 

		setDataSetId(dataSetId);	 

		form.getLayout().addComponent(classSelector.getArrayContextCB());
		form.getLayout().addComponent(classSelector.getH1());
		form.getLayout().addComponent(classSelector.getH2());
		
		//TODO: allow network to be loaded from adjacency matrix data node
		upload = new Upload("Upload Network File", this);
		upload.setButtonCaption("Upload");
		upload.addSucceededListener((Upload.SucceededListener)this);
        upload.addFailedListener((Upload.FailedListener)this);
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
		priorBox.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -5548846734511323624L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (priorBox.getValue()){
					for (String item : order)
						form.getField(item).setEnabled(false);
					upload.setEnabled(false);
					classSelector.getArrayContextCB().setEnabled(false);
					classSelector.getH1().setEnabled(false);
				    classSelector.getH2().setEnabled(false);
					form.getField("retrievePriorResultWithId").setEnabled(true);
					if (form.isValid()) submitButton.setEnabled(true);
				}else {
					for (String item : order)
						form.getField(item).setEnabled(true);
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
		submitButton.addClickListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1085633263164082701L;

			@Override
			public void buttonClick(ClickEvent event) {			 
			    String warnMsg = validInputClassData(classSelector.getClass1ArraySet(), classSelector.getClass2ArraySet());
				if( warnMsg != null ) 
				{ 
					MessageBox.showPlain( 
						Icon.INFO,
						"Warning", 
						warnMsg,
						ButtonId.OK);
				    return;
				}
					
					
				ResultSet resultSet = storePendingResultSet();
				
				HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>(); 
				params.put("bean", bean);

				AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(dataSet, resultSet, params, MarinaUI.this);
				GeworkbenchRoot.getBlackboard().fire(analysisEvent);
			}
		});
		form.getFooter().addComponent(submitButton);

		addComponent(form);
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
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
		resultSet.setDateField(date);
		String dataSetName = analysisName + " - Pending";
		resultSet.setName(dataSetName);
		resultSet.setType(getResultType().getName());
		resultSet.setParent(dataSetId);
		resultSet.setOwner(SessionHandler.get().getId());
		FacadeFactory.getFacade().store(resultSet);

		NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
		GeworkbenchRoot.getBlackboard().fire(resultEvent);

		return resultSet;
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
		bean.setNetworkBytes(os.toByteArray());
		openDialog();
	}

	private void networkNotLoaded(String msg){
		bean.setNetworkBytes(null);
		item.getItemProperty("network").setValue("");
		form.getField("network").setEnabled(false);
		submitButton.setEnabled(false);
		if (msg != null){
			MessageBox.showPlain(Icon.ERROR,
					"Network Problem", msg, 
					ButtonId.OK);
		}
	}

	private void networkLoaded(byte[] networkBytes){
		bean.setNetworkBytes(networkBytes);
		form.getField("network").setEnabled(true);
		if (classSelector.getTf1().isEnabled())
			submitButton.setEnabled(true);
	}

	private static AdjacencyMatrix.Node token2node(String token,
			final String selectedRepresentedBy, final boolean isRestrict,
			final DSMicroarraySet maSet) {
		DSGeneMarker m = null;
		if (selectedRepresentedBy.equals(PROBESET_ID)
				|| selectedRepresentedBy.equals(GENE_NAME)
				|| selectedRepresentedBy.equals(ENTREZ_ID))
			m = maSet.getMarkers().get(token);

		AdjacencyMatrix.Node node = null;

		if (m == null && isRestrict) {
			// we don't have this gene in our MicroarraySet
			// we skip it
			return null;
		} else if (m == null && !isRestrict) {
			if (selectedRepresentedBy.equals(GENE_NAME))
				node = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL, token);
			else
				node = new AdjacencyMatrix.Node(NodeType.STRING, token);
		} else {
			if (selectedRepresentedBy.equals(PROBESET_ID))
				node = new AdjacencyMatrix.Node(m);
			else {
				String geneName = m.getGeneName();
				String[] geneNameList = m.getShortNames();
				for (int i = 0; i < geneNameList.length; i++) {
					if (geneNameList[i].equals(token)) {
						geneName = token;
						break;
					}

				}
				node = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL, geneName);
			}
		}
		return node;
	}

	public AdjacencyMatrix parseAdjacencyMatrix(byte[] bytes,
			final DSMicroarraySet maSet,
			Map<String, String> interactionTypeSifMap, String format,
			String selectedRepresentedBy, boolean isRestrict)
			throws InputFileFormatException {

		AdjacencyMatrix matrix = new AdjacencyMatrix(bean.getNetwork(), interactionTypeSifMap);
		 
		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));

			String line = null;

			while ((line = br.readLine()) != null) {
				// skip comments
				if (line.trim().equals("") || line.startsWith(">")
						|| line.startsWith("-"))
					continue;

				StringTokenizer tr = new StringTokenizer(line, "\t");

				AdjacencyMatrix.Node node = token2node(tr.nextToken(),
						selectedRepresentedBy, isRestrict, maSet);
				if (node == null)
					continue; // skip it when we don't have it
			 
				String interactionType = null;
				if (format.equals(SIF_FORMART) && tr.hasMoreTokens())
					interactionType = tr.nextToken().toLowerCase();			 
				while (tr.hasMoreTokens()) {

					String strGeneId2 = tr.nextToken();
					AdjacencyMatrix.Node node2 = token2node(strGeneId2,
							selectedRepresentedBy, isRestrict, maSet);
					if (node2 == null)
						continue; // skip it when we don't have it

					float mi = 0.8f;
					if (format.equals(ADJ_FORMART)) {
						if (!tr.hasMoreTokens())
							throw new InputFileFormatException(
									"invalid format around " + strGeneId2);
						mi = Float.parseFloat(tr.nextToken());
					}
				 
					matrix.add(node, node2, mi, interactionType);
				} // end of the token loop for one line			 
			} // end of reading while loop
		} catch (NumberFormatException ex) {
			throw new InputFileFormatException(ex.getMessage());
		} catch (FileNotFoundException ex3) {
			throw new InputFileFormatException(ex3.getMessage());
		} catch (IOException ex) {
			throw new InputFileFormatException(ex.getMessage());
		} catch (Exception e) {
			throw new InputFileFormatException(e.getMessage());
		}

		return matrix;
	}

	private byte[] getNetworkFromAdjMatrix(AdjacencyMatrixDataSet amSet){
		if (amSet==null) return null;
		AdjacencyMatrix matrix  = amSet.getMatrix();
		if (matrix==null) return null;
		boolean goodNetwork = false;
		allpos = true;
		BufferedWriter bw = null;
		
		DSMicroarraySet microarraySet = (DSMicroarraySet) amSet.getParentDataSet();
		try{
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			bw = new BufferedWriter(new OutputStreamWriter(bo));

			for (AdjacencyMatrix.Node node1 : matrix.getNodes()) {
				DSGeneMarker marker1 = getMarkerInNode(node1, matrix, microarraySet);
				if (marker1 != null && marker1.getLabel() != null) {
					StringBuilder builder = new StringBuilder();
					for (AdjacencyMatrix.Edge edge : matrix.getEdges(node1)) {
						DSGeneMarker marker2 = getMarkerInNode(edge.node2, matrix, microarraySet);
						if (marker2 != null && marker2.getLabel() != null) {
							double rho = 1, pvalue = 0;
							double[] v1 = dataSet.getRow(marker1);
							double[] v2 = dataSet.getRow(marker2);
							if (v1 != null && v1.length > 0 && v2 != null && v2.length > 0){
								double[][] arrayData = new double[][]{v1, v2};
								RealMatrix rm = new SpearmansCorrelation().computeCorrelationMatrix(transpose(arrayData));
								if (rm.getColumnDimension() > 1)  rho = rm.getEntry(0, 1);
								if (allpos && rho < 0)  allpos = false;
								try{
									pvalue = new PearsonsCorrelation(rm, v1.length).getCorrelationPValues().getEntry(0, 1);
								}catch(Exception e){
									e.printStackTrace();
								}
							}
							builder.append(marker1.getLabel() + "\t");
							builder.append(marker2.getLabel() + "\t"
									+ edge.info.value +"\t"  // Mutual information
									+ rho+ "\t"   // Spearman's correlation = 1
									+ pvalue +"\n"); // P-value for Spearman's correlation = 0
						}
					}
					if (!goodNetwork && builder.length() > 0) goodNetwork = true;
					bw.write(builder.toString());
				}
			}
			bw.close();
			if (!goodNetwork) return null;
			return bo.toByteArray();
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}finally{
			if (bw!=null) {
				try{
					bw.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	private double[][] transpose(double[][] in){
		if (in==null || in.length==0 || in[0].length==0)
			return null;
		int row = in.length;
		int col = in[0].length;
		double[][] out = new double[col][row];
		for(int i=0; i<row; i++)
			for (int j=0; j<col; j++)
				out[j][i] = in[i][j];
		return out;
	}

	private DSGeneMarker getMarkerInNode(AdjacencyMatrix.Node node, AdjacencyMatrix matrix, DSMicroarraySet microarraySet){
		if (node == null || matrix == null) return null;
		DSGeneMarker marker = null;
		if (node.type == NodeType.MARKER) 
			marker = node.getMarker();
		else 
			marker = microarraySet.getMarkers().get(node.stringId);
		return marker;
	}

	/**
	 * Test if the network is in 5-column format, and if all correlation cols are positive.
	 * @param bytes    network in bytes
	 * @return if the network is in 5-column format
	 */
	private boolean is5colnetwork(byte[] bytes){
		if (bytes == null || bytes.length == 0)
			return false;
		BufferedReader br = null;
		try{
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
			allpos = true;
			String line = null;
			while( (line = br.readLine()) != null) {
				String[] toks = line.split("\t");
				if (toks.length != 5 || !isDouble(toks[2]) 
						|| !isDouble(toks[3]) || !isDouble(toks[4]))
					return false;
				if (allpos && Double.valueOf(toks[correlationCol]) < 0)
					allpos = false;
			}
			return true;
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}finally{
			try{ 
				if (br!=null) br.close(); 
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	private boolean isDouble(String s){
		try{
			Double.parseDouble(s);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}

	private void openDialog(){
		loadDialog = new Window();
		loadDialog.setCaption("Load Interaction Network");

		formatBox = new ComboBox("File Format");
		formatBox.setSizeFull();
		formatBox.setNullSelectionAllowed(false);
		formatBox.addItem(AdjacencyMatrixDataSet.ADJ_FORMART);
		formatBox.addItem(AdjacencyMatrixDataSet.SIF_FORMART);
		formatBox.addItem(marina5colformat);

		presentBox = new ComboBox("Node Represented By");
		presentBox.setSizeFull();
		presentBox.setNullSelectionAllowed(false);
		presentBox.addItem(AdjacencyMatrixDataSet.PROBESET_ID);
		presentBox.addItem(AdjacencyMatrixDataSet.GENE_NAME);
		presentBox.addItem(AdjacencyMatrixDataSet.ENTREZ_ID);
		presentBox.addItem(AdjacencyMatrixDataSet.OTHER);

		Button continueButton = new Button("Continue");
		Button cancelButton = new Button("Cancel");
		formatBox.addValueChangeListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = -7717934520937460169L;
			public void valueChange(ValueChangeEvent event) {
				if (formatBox.getValue().toString().equals(
						AdjacencyMatrixDataSet.ADJ_FORMART)) {
					presentBox.removeAllItems();
					presentBox.addItem(AdjacencyMatrixDataSet.PROBESET_ID);
					presentBox.addItem(AdjacencyMatrixDataSet.GENE_NAME);
					presentBox.addItem(AdjacencyMatrixDataSet.ENTREZ_ID);
					presentBox.addItem(AdjacencyMatrixDataSet.OTHER);
					presentBox.setValue(AdjacencyMatrixDataSet.PROBESET_ID);
				} else if (formatBox.getValue().toString().equals(
						marina5colformat)) {
					presentBox.removeAllItems();
					presentBox.addItem(AdjacencyMatrixDataSet.PROBESET_ID);
					presentBox.setValue(AdjacencyMatrixDataSet.PROBESET_ID);
				} else {
					presentBox.removeAllItems();
					presentBox.addItem(AdjacencyMatrixDataSet.GENE_NAME);
					presentBox.addItem(AdjacencyMatrixDataSet.ENTREZ_ID);
					presentBox.addItem(AdjacencyMatrixDataSet.OTHER);
					presentBox.setValue(AdjacencyMatrixDataSet.GENE_NAME);
				}
			}
		});

		if (bean.getNetwork().toLowerCase().endsWith(".sif"))
			formatBox.setValue(AdjacencyMatrixDataSet.SIF_FORMART);
		else if (bean.getNetwork().toLowerCase().contains("5col"))
			formatBox.setValue(marina5colformat);
		else
			formatBox.setValue(AdjacencyMatrixDataSet.ADJ_FORMART);

		continueButton.addClickListener(new ClickListener(){
			private static final long serialVersionUID = -5207079864397027215L;
			public void buttonClick(ClickEvent event) {
				selectedFormat = formatBox.getValue().toString();
				selectedRepresentedBy = presentBox.getValue().toString();
				UI.getCurrent().removeWindow(loadDialog);

				if ((selectedFormat.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART) && !bean.getNetwork().toLowerCase().endsWith(".sif"))
						|| (bean.getNetwork().toLowerCase().endsWith(".sif") && !selectedFormat
								.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART))
						||(selectedFormat.equals(marina5colformat) && !is5colnetwork(bean.getNetworkBytes()))){
					networkNotLoaded("The network format selected does not match that of the file.");
					return;
				}

				if (selectedFormat.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART)) {
					interactionTypeMap = new org.geworkbench.parsers.AdjacencyMatrixFileFormat().getInteractionTypeMap();
				}
				
				if (allpos && bean.getGseaTailNumber()==2){
					MessageBox.showPlain(
							Icon.WARN,
							"Warning",
							"Since all Spearman's correlation >= 0, gsea will use tail = 1.",
							ButtonId.OK);
					bean.setGseaTailNumber(1);
					item.getItemProperty("gseaTailNumber").setValue(1);
				}

				if (!selectedFormat.equals(marina5colformat)){
					try {
						AdjacencyMatrix matrix = parseAdjacencyMatrix(bean.getNetworkBytes(), dataSet,
								interactionTypeMap, selectedFormat,
								selectedRepresentedBy, isRestrict);

						AdjacencyMatrixDataSet adjMatrix = new AdjacencyMatrixDataSet(matrix, 
								0, bean.getNetwork(), bean.getNetwork(), dataSet);
						
						networkLoaded(getNetworkFromAdjMatrix(adjMatrix));
					} catch (InputFileFormatException e1) {
						log.error(e1.getMessage());
						e1.printStackTrace();
					}
				}else{
					networkLoaded(bean.getNetworkBytes());
				}

			}
		});
		cancelButton.addClickListener(new ClickListener(){
			private static final long serialVersionUID = 1940630593562212467L;
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().removeWindow(loadDialog);
				networkNotLoaded(null);
			}
		});

		HorizontalLayout bar = new HorizontalLayout();
		bar.setSpacing(true);
		bar.addComponent(cancelButton);
		bar.addComponent(continueButton);
		
		Form loadform = new Form();
		loadform.getLayout().addComponent(formatBox);
		loadform.getLayout().addComponent(presentBox);
		loadform.getFooter().addComponent(bar);

		loadDialog.setContent(LayoutUtil.addComponent(loadform));
		loadDialog.setWidth("340px");
		loadDialog.setModal(true);
		loadDialog.setVisible(true);
		UI.getCurrent().addWindow(loadDialog);
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
		
		try {
			dataSet = (DSMicroarraySet) UserDirUtils.deserializeDataSet(dataId, DSMicroarraySet.class);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if(dataSet==null) return;

		((CSMicroarraySet)dataSet).getMarkers().correctMaps();
       
		classSelector.setData(dataSetId, SessionHandler.get().getId());
	 
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
			    ArrayList<String> arrays = SubSetOperations.getArrayData(Long
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
			   {   SubSet subset = (SubSet)SubSetOperations.getArraySet(Long
							.parseLong(selectedclass2Sets[i].trim())).get(0);
				    return "Class1 and class2 groups have same array set " + subset.getName() + ".";
			   }
				 ArrayList<String> arrays = SubSetOperations.getArrayData(Long
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
		return CSMasterRegulatorTableResultSet.class;
	}

	@Override
	public String execute(Long resultId, DSDataSet<?> dataset,
			HashMap<Serializable, Serializable> parameters) throws IOException {
		MarinaAnalysis analyze = new MarinaAnalysis(dataSet, parameters);

		CSMasterRegulatorTableResultSet mraRes = analyze.execute();
		UserDirUtils.serializeResultSet(resultId, mraRes);
		return analysisName + " - " + mraRes.getLabel();
	}

	@Override
	public String execute(Long resultId, Long datasetId,
			HashMap<Serializable, Serializable> parameters, Long userId) throws IOException,
			Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
