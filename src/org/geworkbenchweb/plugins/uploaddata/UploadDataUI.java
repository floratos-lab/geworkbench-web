package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.AnnotationInformationManager.AnnotationType;
import org.geworkbenchweb.dataset.GeWorkbenchLoaderException;
import org.geworkbenchweb.dataset.Loader;
import org.geworkbenchweb.dataset.LoaderFactory;
import org.geworkbenchweb.dataset.LoaderUsingAnnotation;
import org.geworkbenchweb.pojos.Annotation;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class UploadDataUI extends VerticalLayout {

	private static Log log = LogFactory.getLog(UploadDataUI.class);
			
	private static final long serialVersionUID = 1L;

	private static final String initialText = "Enter description here.";
	public static enum Anno {
		NO("No annotation"), NEW("Load new annotation"), PUBLIC("Public annotation files"),
		PRIVATE("Private annotations files"), DELETE("Delete private annotation files");
		private String value;
		Anno(String v) { value = v; }
		public String toString() { return value; }
	};

	private ComboBox fileCombo;
	private TextArea dataArea;
	
	private Tree annotChoices;
	private ComboBox annotTypes;

	private Label fileUploadStatus 			= 	new Label("Please select a data file to upload");
	private DataFileReceiver fileReceiver 	= 	new DataFileReceiver();
	private Upload uploadField 				= 	new Upload(null, fileReceiver);
	
	private Label annotUploadStatus 			= 	new Label("Please select an annotation file to upload");
	private AnnotFileReceiver annotFileReceiver = 	new AnnotFileReceiver();
	private Upload annotUploadField 			= 	new Upload(null, annotFileReceiver);
	
	private File dataFile;
	private File annotFile;
	private static final String tempDir = System.getProperty("user.home") + "/temp/";
	
	public UploadDataUI(Long dataSetId) {

		setImmediate(true);
		
		fileCombo 			= 	new ComboBox("Please select type of file");
		dataArea 			= 	new TextArea(null, initialText);

		for (Loader loader : new LoaderFactory().getParserList()) {
			fileCombo.addItem(loader);
		}

		fileCombo.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8744518843208040408L;

			public void valueChange(ValueChangeEvent event) {
				Object type = fileCombo.getValue();
				if (type != null) {
					Loader loader = (Loader) type;
					if (loader instanceof LoaderUsingAnnotation) {
						getAnnotChoices();
						annotChoices.setValue(Anno.NO);
						annotChoices.setVisible(true);
					} else {
						annotChoices.setValue(null);
						annotChoices.setVisible(false);
						showAnnotUpload(false);
					}
				}
			}
		});

		fileCombo.setFilteringMode(Filtering.FILTERINGMODE_OFF);
		fileCombo.setImmediate(true);
		fileCombo.setRequired(true);
		fileCombo.setNullSelectionAllowed(false);
		dataArea.setRows(6);
		dataArea.setColumns(40);

		addComponent(fileCombo);
		uploadField.setImmediate(true);
		uploadField.setButtonCaption("Select data file");
        uploadField.addListener(new Upload.StartedListener() {
			private static final long serialVersionUID = 1L;

			public void uploadStarted(StartedEvent event) {
                // This method gets called immediatedly after upload is started
            	uploadField.setVisible(false);
                fileUploadStatus.setValue("Uploading file \"" + event.getFilename()
                        + "\"");
            }
        });

        uploadField.addListener(new Upload.SucceededListener() {
			private static final long serialVersionUID = 1L;

			public void uploadSucceeded(SucceededEvent event) {
                // This method gets called when the upload finished successfully
                fileUploadStatus.setValue(" The data file \"" + event.getFilename()
                        + "\" is selected");
            }
        });

        uploadField.addListener(new Upload.FailedListener() {
			private static final long serialVersionUID = 1L;

			public void uploadFailed(FailedEvent event) {
                // This method gets called when the upload failed
                fileUploadStatus.setValue("Uploading interrupted");
            }
        });

        uploadField.addListener(new Upload.FinishedListener() {
			private static final long serialVersionUID = 1L;

			public void uploadFinished(FinishedEvent event) {
                // This method gets called always when the upload finished,
                // either succeeding or failing
                uploadField.setVisible(true);
                uploadField.setCaption("Select different file");
            }
        });
		
		annotChoices = new Tree("Choose annotation");
		annotChoices.setNullSelectionAllowed(false);
		annotChoices.setWidth(220, 0);
		annotChoices.setVisible(false);
		annotChoices.setImmediate(true);
		annotChoices.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8744518843208040408L;

			public void valueChange(ValueChangeEvent event) {
				Object choice = annotChoices.getValue();
				if (choice != null) {
					if (choice == Anno.NEW)
						showAnnotUpload(true);
					else
						showAnnotUpload(false);
				}
			}
		});

		ArrayList<AnnotationType> atypes = new ArrayList<AnnotationType>();
		atypes.add(AnnotationType.values()[0]);
		atypes.add(AnnotationType.values()[1]);
		annotTypes = new ComboBox("Choose annotation file type", atypes);
		annotTypes.setNullSelectionAllowed(false);
		annotTypes.setWidth(200, 0);
		annotTypes.setValue(AnnotationType.values()[0]);
		annotTypes.setVisible(false);

		annotUploadField.setImmediate(true);
		annotUploadField.setVisible(false);
		annotUploadStatus.setVisible(false);
		annotUploadField.setButtonCaption("Select Annotation file");
		annotUploadField.addListener(new Upload.StartedListener() {
			private static final long serialVersionUID = 1L;

			public void uploadStarted(StartedEvent event) {
                // This method gets called immediatedly after upload is started
				annotUploadField.setVisible(false);
				annotUploadStatus.setValue("Uploading file \"" + event.getFilename()
                        + "\"");
            }
        });

		annotUploadField.addListener(new Upload.SucceededListener() {
			private static final long serialVersionUID = 1L;

			public void uploadSucceeded(SucceededEvent event) {
                // This method gets called when the upload finished successfully
				annotUploadStatus.setValue(" The Annotation file \"" + event.getFilename()
                        + "\" is selected");
            }
        });

		annotUploadField.addListener(new Upload.FailedListener() {
			private static final long serialVersionUID = 1L;

			public void uploadFailed(FailedEvent event) {
                // This method gets called when the upload failed
				annotUploadStatus.setValue("Uploading interrupted");
            }
        });

		annotUploadField.addListener(new Upload.FinishedListener() {
			private static final long serialVersionUID = 1L;

			public void uploadFinished(FinishedEvent event) {
                // This method gets called always when the upload finished,
                // either succeeding or failing
				annotUploadField.setVisible(true);
				annotUploadField.setCaption("Select different file");
            }
        });
		

		setSpacing(true);
		annotUploadField.setButtonCaption("Add Annotation File");
		Button uploadButton = new Button("Upload");
		addComponent(fileUploadStatus);
		addComponent(uploadField);
		addComponent(new Label("<hr/>", Label.CONTENT_XHTML));
		HorizontalLayout annoLayout = new HorizontalLayout();
		annoLayout.addComponent(annotChoices);
		VerticalLayout uploadLayout = new VerticalLayout();
		uploadLayout.setSpacing(true);
		uploadLayout.addComponent(annotTypes);
		uploadLayout.addComponent(annotUploadStatus);
		uploadLayout.addComponent(annotUploadField);
		annoLayout.addComponent(uploadLayout);
		addComponent(annoLayout);
		addComponent(uploadButton);
		uploadButton.addListener(new UploadButtonListener());
	}

	private void getAnnotChoices(){
		annotChoices.removeAllItems();
		for (Anno anno : Anno.values()){
			annotChoices.addItem(anno);
			if (anno == Anno.NO || anno == Anno.NEW || anno == Anno.DELETE){
				annotChoices.setChildrenAllowed(anno, false);
			}else if (anno == Anno.PUBLIC){
				int cnt = 0;
				for (File f : new File(tempDir).listFiles()){
					if (f.isFile() && f.getName().endsWith(".csv")){
						String fname = f.getName();
						annotChoices.addItem(fname);
						annotChoices.setParent(fname, anno);
						annotChoices.setChildrenAllowed(fname, false);
						cnt++;
					}
				}
				if (cnt == 0) annotChoices.setChildrenAllowed(anno, false);
			}else if (anno == Anno.PRIVATE){
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("owner", SessionHandler.get().getId());
				List<Annotation> annots = FacadeFactory
						.getFacade()
						.list("Select a from Annotation as a where a.owner=:owner order by a.name",
								params);
				for (Annotation a : annots){
					String aname = a.getName();
					annotChoices.addItem(aname);
					annotChoices.setParent(aname, anno);
					annotChoices.setChildrenAllowed(aname, false);
				}
				if (annots.isEmpty()) annotChoices.setChildrenAllowed(anno, false);
			}
		}
	}

	private void showAnnotUpload(boolean visible) {
		annotTypes.setVisible(visible);
		annotUploadField.setVisible(visible);
		annotUploadStatus.setVisible(visible);
	}

	private class UploadButtonListener implements Button.ClickListener {

		private static final long serialVersionUID = -2592257781106708221L;

		@Override
		public void buttonClick(ClickEvent event) {
			
			Loader loader 				= 	(Loader) fileCombo.getValue();
			Object choice 				= 	annotChoices.getValue();
			User annotOwner 			= 	SessionHandler.get();
			AnnotationType annotType 	= 	(AnnotationType)annotTypes.getValue();
			
			if (dataFile == null) {
				MessageBox mb = new MessageBox(getWindow(), 
						"Loading problem", 
						MessageBox.Icon.ERROR, 
						"Data file not loaded. No valid data file is chosen.",  
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
				return;
			}
			if (choice == null) return;
			if (choice == Anno.DELETE){
				MessageBox mb = new MessageBox(getWindow(), 
						"To be implemented", 
						MessageBox.Icon.ERROR, 
						"Operation not supported yet",  
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
				return;
			}
			if (choice == Anno.PUBLIC || choice == Anno.PRIVATE){
				MessageBox mb = new MessageBox(getWindow(), 
						"Loading problem", 
						MessageBox.Icon.ERROR, 
						"Annotation file not selected",  
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
				return;
			}
			if (!(choice instanceof Anno)){
				String annotFname = (String)choice;
				Object parent = annotChoices.getParent(choice);
				// shared default annotation
				if (parent == Anno.PUBLIC){
					annotOwner = null;
					annotType = AnnotationType.values()[0];
					annotFile = new File(tempDir, annotFname);
					if (!annotFile.exists()) {
						MessageBox mb = new MessageBox(getWindow(), 
								"Loading problem", 
								MessageBox.Icon.ERROR, 
								"Annotation file not found on server",  
								new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
						mb.show();
						return;
					}
				}
				// user's loaded annotation
				else if (parent == Anno.PRIVATE){
					annotType = null;
					annotFile = new File(tempDir + annotOwner.getUsername(), annotFname);
				}
			}
			else if (choice == Anno.NO)
				annotFile = null;
			// just loaded
			else if (choice == Anno.NEW){
				if (annotFile == null) {
					MessageBox mb = new MessageBox(getWindow(), 
							"Loading problem", 
							MessageBox.Icon.ERROR, 
							"Annotation file not loaded",  
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();	
					return;
				}
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("owner", annotOwner.getId());
				params.put("name", annotFile.getName());
				List<Annotation> annots = FacadeFactory
						.getFacade()
						.list("Select a from Annotation as a where a.owner=:owner and a.name=:name",
								params);
				if (!annots.isEmpty()) {
					MessageBox mb = new MessageBox(getWindow(), 
							"Loading problem", 
							MessageBox.Icon.ERROR, 
							"Annotation file with the same name found on server",  
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();	
					// if (annotFile.exists()) annotFile.delete();
					// return;
				}
			}

			try {
				loader.load(dataFile);
				if (loader instanceof LoaderUsingAnnotation) {
					LoaderUsingAnnotation expressionFileLoader = (LoaderUsingAnnotation) loader;
					expressionFileLoader.parseAnnotation(annotFile, annotType,
							annotOwner);
					if (annotFile != null && choice == Anno.NEW && !annotFile.delete()) {
						log.warn("problem in deleting " + annotFile);
					}
				}
				/*
				 * FIXME delete is correct behavior, but the current code,
				 * particularly CSProteinStructure, depends on the retaining of
				 * the temporary file.
				 */
				// if(!dataFile.delete()) {
				// Log.warn("problem in deleting "+dataFile);
				// }
			} catch (GeWorkbenchLoaderException e) {
				MessageBox mb = new MessageBox(getWindow(), 
						"Loading problem", 
						MessageBox.Icon.ERROR, 
						e.getMessage(),  
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();	
			}
		}

	}
	
	/**
	 * Data File receiver writes file to the temp directory on the server
	 * @author Nikhil
	 */
	public class DataFileReceiver implements Receiver {

		private static final long serialVersionUID = 1L;

		private String fileName;
        private String mtype;

        public OutputStream receiveUpload(String filename, String mimetype) {
            fileName = filename;
            mtype = mimetype;
            FileOutputStream fos = null; // Output stream to write to
            dataFile = new File(tempDir + filename);
            try {
                // Open the file for writing.
                fos = new FileOutputStream(dataFile);
            } catch (final java.io.FileNotFoundException e) {
                // Error while opening the file. Not reported here.
                e.printStackTrace();
                return null;
            }
            return fos;
        }

        public String getFileName() {
            return fileName;
        }

        public String getMimeType() {
            return mtype;
        }
    }
	
	/**
	 * Annotation File receiver writes file to the temp directory on the server
	 * @author Nikhil
	 */
	public class AnnotFileReceiver implements Receiver {

		private static final long serialVersionUID = 1L;

		private String fileName;
        private String mtype;

        public OutputStream receiveUpload(String filename, String mimetype) {
            fileName = filename;
            mtype = mimetype;
            FileOutputStream fos = null; // Output stream to write to
            String dir = tempDir + SessionHandler.get().getUsername();
			if (!new File(dir).exists())
				new File(dir).mkdir();
			annotFile = new File(dir, fileName);
            try {
                // Open the file for writing.
                fos = new FileOutputStream(annotFile);
            } catch (final java.io.FileNotFoundException e) {
                // Error while opening the file. Not reported here.
                e.printStackTrace();
                return null;
            }
            return fos;
        }

        public String getFileName() {
            return fileName;
        }

        public String getMimeType() {
            return mtype;
        }
    }
}
