package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.dataset.DataSetParser;
import org.geworkbenchweb.dataset.geo.GEODataFetch;
import org.geworkbenchweb.pojos.Project;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.easyuploads.FileFactory;
import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;
import org.vaadin.jonatan.contexthelp.ContextHelp;

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
    private ComboBox projectBox;
    private ComboBox uploadType;
    private TextField geoTextField;
    
	public UploadDataUI(Long dataSetId) {
		
		ContextHelp help = new ContextHelp();
		help.setFollowFocus(true);
		addComponent(help);
		
		uploadType			=	new ComboBox("Please select uplaod type");
		projectBox			=	new ComboBox("Please select project");
		fileCombo 			= 	new ComboBox("Please select type of file");
		dataArea 			= 	new TextArea(null, initialText);
		uploadField 		= 	new UploadField();
		annotUploadField	=	new UploadField();
		geoTextField		=	new TextField("Enter GEO ID");

		help.addHelpForComponent(projectBox, "Project for the data to be uploaded");
		help.addHelpForComponent(fileCombo, "Select the type of file");
		help.addHelpForComponent(dataArea, "Some help so that it can help user");
		
		for (int i = 0; i < files.length; i++) {
			fileCombo.addItem(files[i]);    
		}

		projectBox.setFilteringMode(Filtering.FILTERINGMODE_OFF);
		projectBox.setImmediate(true);
		projectBox.setRequired(true);
		projectBox.setNullSelectionAllowed(false);

		setProjects();
		
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

		addComponent(projectBox);
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
	
	private void setProjects() {
    	Map<String, Object> param 		= 	new HashMap<String, Object>();
		param.put("owner", SessionHandler.get().getId());
		param.put("workspace", WorkspaceUtils.getActiveWorkSpace());
		
		List<?> projects =  FacadeFactory.getFacade().list("Select p from Project as p where p.owner=:owner and p.workspace =:workspace", param);
		for(int i=0; i<projects.size(); i++) {
			
			projectBox.addItem(((Project) projects.get(i)).getName());
		}
    }

	public void theButtonClick(Button.ClickEvent event) {
    	
		String projectName		=  	(String) projectBox.getValue();
		String dataDescription 	= 	(String) dataArea.getValue();
		if(uploadType.getValue().toString().equalsIgnoreCase("Upload from your Desktop")) {
    		
    		String fileType 		= 	(String) fileCombo.getValue();
    		File dataFile 			= 	(File) uploadField.getValue();

    		parseInit(dataFile, null, fileType, projectName, dataDescription);
    		dataFile.delete();
    	} else {
    		try {
				File dataFile = GEODataFetch.getGDS(geoTextField.getValue().toString());
				parseInit(dataFile, null, "GDS", projectName, dataDescription);
				dataFile.delete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
	protected void parseInit(File dataFile, File annotFile, String fileType,  String projectName, String dataDescription) {
		new DataSetParser(dataFile, annotFile, fileType, projectName, dataDescription);
	}
}
