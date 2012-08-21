package org.geworkbenchweb.dataset;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.pojos.Project;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.easyuploads.FileFactory;
import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;

import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class UDataSetUpload extends Window {
    
    private static final String[] files		= 	new String[] { "Expression File", "PDB File" };
    private static final String initialText = 	"Enter description here.";
   
    private VerticalLayout dataLayout; 
    private ComboBox fileCombo; 			
    private TextArea dataArea;			
    private UploadField uploadField; 		
    private UploadField annotUploadField;
    private ComboBox projectBox;
    
    public UDataSetUpload() {
    	  
    }
    
    public void attach() {
    	
    	super.attach();    	
    	this.setModal(true);
    	this.setStyleName("opaque");
    	this.setCaption("Upload Dataset");
    	this.setWidth("700px");
    	this.setHeight("400px");
    	this.setDraggable(false);
    	this.setResizable(false);

       	dataLayout 			= 	new VerticalLayout();
       	projectBox			=	new ComboBox("Please select project");
        fileCombo 			= 	new ComboBox("Please select type of file");
        dataArea 			= 	new TextArea(null, initialText);
        uploadField 		= 	new UploadField();
        annotUploadField	=	new UploadField();
        
        dataLayout.setSpacing(true);
        
        for (int i = 0; i < files.length; i++) {
            fileCombo.addItem(files[i]);    
        }
     
        projectBox.setFilteringMode(Filtering.FILTERINGMODE_OFF);
        projectBox.setImmediate(true);
        projectBox.setRequired(true);
        projectBox.setNullSelectionAllowed(false);
        
        setProjects();
        
        fileCombo.setFilteringMode(Filtering.FILTERINGMODE_OFF);
        fileCombo.setImmediate(true);
        fileCombo.setRequired(true);
        fileCombo.setNullSelectionAllowed(false);
        
        dataArea.setRows(6);
        dataArea.setColumns(40);
        
        dataLayout.addComponent(projectBox);
        dataLayout.addComponent(fileCombo);
        dataLayout.addComponent(dataArea);
   
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
        
        dataLayout.setSpacing(true);
        annotUploadField.setButtonCaption("Add Annotation File");
        addComponent(dataLayout);       
        Button uploadButton = new Button("Upload");
        dataLayout.addComponent(uploadField);
        dataLayout.addComponent(uploadButton);
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
    	String fileType 		= 	(String) fileCombo.getValue();
        String dataDescription 	= 	(String) dataArea.getValue();
    	File dataFile 			= 	(File) uploadField.getValue();
    	
    	parseInit(dataFile, null, fileType, projectName, dataDescription);
    		
    	dataFile.delete();
    	
    }
    
	protected void parseInit(File dataFile, File annotFile, String fileType,  String projectName, String dataDescription) {

		new DataSetParser(dataFile, annotFile, fileType, projectName, dataDescription);
		this.close();
		
	}
}
