package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;
import java.io.IOException;
import org.geworkbenchweb.dataset.DataSetParser;
import org.geworkbenchweb.dataset.geo.GEODataFetch;
import org.vaadin.easyuploads.FileFactory;
import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.AbstractSelect.Filtering;

public class UploadDataUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private static final String[] files		= 	new String[] { "Expression File", "GEO SOFT", "PDB File" };
    private static final String initialText = 	"Enter description here.";
   
    private ComboBox fileCombo; 			
    private TextArea dataArea;			
    private UploadField uploadField; 		
    private UploadField annotUploadField;
    private ComboBox uploadType;
    private TextField geoTextField;
    
	public UploadDataUI(Long dataSetId) {
		
		uploadType			=	new ComboBox("Please select uplaod type");
		fileCombo 			= 	new ComboBox("Please select type of file");
		dataArea 			= 	new TextArea(null, initialText);
		uploadField 		= 	new UploadField();
		annotUploadField	=	new UploadField();
		geoTextField		=	new TextField("Enter GEO ID");
		
		for (int i = 0; i < files.length; i++) {
			fileCombo.addItem(files[i]);    
		}

		uploadType.setFilteringMode(Filtering.FILTERINGMODE_OFF);
		uploadType.setImmediate(true);
		uploadType.setRequired(true);
		uploadType.setNullSelectionAllowed(false);
		uploadType.addItem("Upload from your Desktop");
		uploadType.addItem("Import from NCBI GEO");
		uploadType.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;
			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				try {
					String type = valueChangeEvent.getProperty().getValue().toString();
					if(type == "Upload from your Desktop") {
						geoTextField.setEnabled(false);
					} else {
						fileCombo.setEnabled(false);
						uploadField.setEnabled(false);
					}
				}catch (Exception e) {
				}
			}
		});

		fileCombo.setFilteringMode(Filtering.FILTERINGMODE_OFF);
		fileCombo.setImmediate(true);
		fileCombo.setRequired(true);
		fileCombo.setNullSelectionAllowed(false);
		dataArea.setRows(6);
		dataArea.setColumns(40);

		addComponent(uploadType);
		addComponent(fileCombo);
		addComponent(geoTextField);
		addComponent(dataArea);

		uploadField.setFieldType(FieldType.FILE);
		uploadField.setImmediate(false);
		uploadField.setRequired(true);
		uploadField.setFileFactory(new FileFactory() {

			public File createFile(String fileName, String mimeType) {

				File f = new File(System.getProperty("user.home") + "/temp/", fileName);
				return f;

			}     
		});

		annotUploadField.setFieldType(FieldType.FILE);
		annotUploadField.setImmediate(false);
		annotUploadField.setRequired(true);
		annotUploadField.setFileFactory(new FileFactory() {

			public File createFile(String fileName, String mimeType) {

				File f = new File(System.getProperty("user.home") + "/temp/", fileName);
				return f;

			}     
		});

		setSpacing(true);
		annotUploadField.setButtonCaption("Add Annotation File");
		Button uploadButton = new Button("Upload");
		addComponent(uploadField);
		addComponent(uploadButton);
		uploadButton.addListener(Button.ClickEvent.class, this, "theButtonClick");
	}

	public void theButtonClick(Button.ClickEvent event) {
    	
		String dataDescription 	= 	(String) dataArea.getValue();
		if(uploadType.getValue().toString().equalsIgnoreCase("Upload from your Desktop")) {
    		
    		String fileType 		= 	(String) fileCombo.getValue();
    		File dataFile 			= 	(File) uploadField.getValue();

    		parseInit(dataFile, null, fileType, dataDescription);
    		dataFile.delete();
    	} else {
    		try {
				File dataFile = GEODataFetch.getGDS(geoTextField.getValue().toString());
				parseInit(dataFile, null, "GDS", dataDescription);
				dataFile.delete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
	protected void parseInit(File dataFile, File annotFile, String fileType, String dataDescription) {
		new DataSetParser(dataFile, annotFile, fileType, dataDescription);
	}
}
