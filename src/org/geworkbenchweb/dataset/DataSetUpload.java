package org.geworkbenchweb.dataset;

import java.io.File;

import org.geworkbenchweb.GeworkbenchApplication;
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
public class DataSetUpload extends Window {
    
    private static final String[] files		= 	new String[] { "Expression File", "GEO SOFT File", "FASTA File", "PDB File", "MAGE-TAB File"};
    private static final String initialText = 	"Enter description here.";
   
    private VerticalLayout dataLayout; 
    private ComboBox fileCombo; 			
    private TextArea dataArea;			
    private UploadField uploadField; 		
    private Window dataWindow;
    
    public DataSetUpload() {
    	  
    }
    
    public void attach() {
    	
    	super.attach();    	
    	this.setModal(true);
    	this.setStyleName("opaque");
    	this.setCaption("Upload Dataset");
    	this.setWidth("700px");
    	this.setHeight("350px");
    	this.setDraggable(false);
    	this.setResizable(false);

    	dataWindow 			= 	this.getApplication().getMainWindow();
    	dataLayout 			= 	new VerticalLayout();
        fileCombo 			= 	new ComboBox("Please select type of file");
        dataArea 			= 	new TextArea(null, initialText);
        uploadField 		= 	new UploadField();
        
        dataLayout.setSpacing(true);
        
        for (int i = 0; i < files.length; i++) {
        	
            fileCombo.addItem(files[i]);
        
        }

        fileCombo.setFilteringMode(Filtering.FILTERINGMODE_OFF);
        fileCombo.setImmediate(true);
        fileCombo.setRequired(true);
        
        dataArea.setRows(6);
        dataArea.setColumns(40);
        
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
        
        addComponent(dataLayout);       
        Button uploadButton = new Button("Upload");
        dataLayout.addComponent(uploadField);
        dataLayout.addComponent(uploadButton);
        uploadButton.addListener(Button.ClickEvent.class, this, "theButtonClick");
    	
    }

    public void theButtonClick(Button.ClickEvent event) {
    	
    	
    	String fileType 		= 	(String) fileCombo.getValue();
        String dataDescription 	= 	(String) dataArea.getValue();
    	File dataFile 			= 	(File) uploadField.getValue();
    	
    	parseInit(dataFile, fileType, dataDescription);
        dataFile.delete();
    	
    	GeworkbenchApplication app = new GeworkbenchApplication();
    	dataWindow.removeAllComponents();
    	app.initView(dataWindow);
    	
    }
    
	protected void parseInit(File dataFile, String fileType,  String dataDescription) {
		
		new DataSetParser(dataFile, fileType, dataDescription);
		this.close();
		
	}
}
