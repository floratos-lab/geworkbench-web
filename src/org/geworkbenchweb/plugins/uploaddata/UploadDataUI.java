package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.AnnotationInformationManager.AnnotationType;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.dataset.GeWorkbenchLoaderException;
import org.geworkbenchweb.dataset.Loader;
import org.geworkbenchweb.dataset.LoaderFactory;
import org.geworkbenchweb.dataset.LoaderUsingAnnotation;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.events.UploadStartedEvent;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.DataSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.SplitPanel;
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

@SuppressWarnings("deprecation")
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

    private CheckBox sleep					=	new CheckBox("Simulate slow upload speed");
	private Label fileUploadStatus 			= 	new Label("Please select a data file to upload");
	private DataFileReceiver fileReceiver 	= 	new DataFileReceiver();
	private Upload uploadField 				= 	new Upload(null, fileReceiver);
    private HorizontalLayout pLayout		=	new HorizontalLayout();
    private ProgressIndicator pIndicator	=	new ProgressIndicator();
	
	private Label annotUploadStatus 			= 	new Label("Please select an annotation file to upload");
	private AnnotFileReceiver annotFileReceiver = 	new AnnotFileReceiver();
	private Upload annotUploadField 			= 	new Upload(null, annotFileReceiver);
    private HorizontalLayout annotPLayout		=	new HorizontalLayout();
    private ProgressIndicator annotPIndicator	=	new ProgressIndicator();

	private Button uploadButton = new Button("Add to workspace");
	
	private File dataFile;
	private File annotFile;
	private static final String tempDir = System.getProperty("user.home") + "/temp/";
	private static final String dataDir = "/data/";
	
	public UploadDataUI() {

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
					uploadButton.setEnabled(false);
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
				enableUMainLayout(false);
				
            	uploadField.setVisible(false);
                fileUploadStatus.setValue("Upload in progress: \"" + event.getFilename()
                        + "\"");
				sleep.setVisible(false);
                pLayout.setVisible(true);
                pIndicator.setValue(0f);
                pIndicator.setPollingInterval(500);
            }
        });

        uploadField.addListener(new Upload.ProgressListener(){	
        	private static final long serialVersionUID = 1L;
            public void updateProgress(final long readBytes,
                    final long contentLength) {
                pIndicator.setValue(new Float(readBytes / (float) contentLength));
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
                float v = 100 * (Float)(pIndicator.getValue());
                fileUploadStatus.setValue("Upload interrupted at " + Math.round(v) + "%");

                if (dataFile != null){ 
                	if(!dataFile.delete())
                		log.warn("problem in deleting " + dataFile);
                	dataFile = null;
				}
            }
        });

        uploadField.addListener(new Upload.FinishedListener() {
			private static final long serialVersionUID = 1L;

			public void uploadFinished(FinishedEvent event) {
                // This method gets called always when the upload finished,
                // either succeeding or failing
				sleep.setVisible(true);
				pLayout.setVisible(false);
                uploadField.setVisible(true);
                uploadField.setCaption("Select different file");
            }
        });
		
        Button cancelUpload = new Button("Cancel");
        cancelUpload.setStyleName("small");
        cancelUpload.addListener(new Button.ClickListener() {
        	private static final long serialVersionUID = 1L;
            public void buttonClick(ClickEvent event) {
                uploadField.interruptUpload();
            }
        });
		
        pLayout.setSpacing(true);
        pLayout.setVisible(false);
        pLayout.addComponent(pIndicator);
        pLayout.addComponent(cancelUpload);

		annotChoices = new Tree("Choose annotation");
		annotChoices.setNullSelectionAllowed(false);
		annotChoices.setWidth(220, 0);
		annotChoices.setVisible(false);
		annotChoices.setImmediate(true);
		annotChoices.addListener(new ItemClickListener(){
			private static final long serialVersionUID = 8744518843208040408L;

			public void itemClick(ItemClickEvent event) {
				if (event.getSource() == annotChoices){
					Object choice = event.getItemId();
					if (choice != null){
						if (choice == Anno.PUBLIC || choice == Anno.PRIVATE) {
							annotChoices.setSelectable(false);
							annotChoices.setValue(null);
						} else {
							annotChoices.setSelectable(true);
							annotChoices.setValue(choice);
						}
						if (choice == Anno.NEW)
							showAnnotUpload(true);
						else
							showAnnotUpload(false);
					}
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
				annotUploadStatus.setValue("Upload in progress: \"" + event.getFilename()
                        + "\"");
				annotPLayout.setVisible(true);
                annotPIndicator.setValue(0f);
                annotPIndicator.setPollingInterval(500);
            }
        });

        annotUploadField.addListener(new Upload.ProgressListener(){	
        	private static final long serialVersionUID = 1L;
            public void updateProgress(final long readBytes,
                    final long contentLength) {
                annotPIndicator.setValue(new Float(readBytes / (float) contentLength));
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
				float v = 100 * (Float)(annotPIndicator.getValue());
                annotUploadStatus.setValue("Upload interrupted at " + Math.round(v) + "%");

                if (annotFile != null){ 
                	if(!annotFile.delete())
                		log.warn("problem in deleting " + annotFile);
                	annotFile = null;
				}
            }
        });

		annotUploadField.addListener(new Upload.FinishedListener() {
			private static final long serialVersionUID = 1L;

			public void uploadFinished(FinishedEvent event) {
                // This method gets called always when the upload finished,
                // either succeeding or failing
				annotPLayout.setVisible(false);
				annotUploadField.setVisible(true);
				annotUploadField.setCaption("Select different file");
            }
        });
		
		Button annotCancelUpload = new Button("Cancel");
		annotCancelUpload.setStyleName("small");
        annotCancelUpload.addListener(new Button.ClickListener() {
        	private static final long serialVersionUID = 1L;
            public void buttonClick(ClickEvent event) {
                annotUploadField.interruptUpload();
            }
        });
		
        annotPLayout.setSpacing(true);
        annotPLayout.setVisible(false);
        annotPLayout.addComponent(annotPIndicator);
        annotPLayout.addComponent(annotCancelUpload);

		setSpacing(true);
		
        sleep.setImmediate(true);
        sleep.setDescription("Simulate slow upload speed to show progress indicator under fast network.");
        sleep.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				fileReceiver.setSlow(event.getButton().booleanValue());
            }
        });

		addComponent(fileUploadStatus);
		addComponent(sleep);
		addComponent(uploadField);
		addComponent(pLayout);
		addComponent(new Label("<hr/>", Label.CONTENT_XHTML));
		HorizontalLayout annoLayout = new HorizontalLayout();
		annoLayout.addComponent(annotChoices);
		VerticalLayout uploadLayout = new VerticalLayout();
		uploadLayout.setSpacing(true);
		uploadLayout.addComponent(annotTypes);
		uploadLayout.addComponent(annotUploadStatus);
		uploadLayout.addComponent(annotUploadField);
		uploadLayout.addComponent(annotPLayout);
		annoLayout.addComponent(uploadLayout);
		addComponent(annoLayout);
		uploadButton.setEnabled(false);
		uploadButton.addListener(new UploadButtonListener());
		HorizontalLayout btnLayout = new HorizontalLayout();
		btnLayout.setSpacing(true);
		btnLayout.addComponent(uploadButton);
		addComponent(btnLayout);
	}
	
	public void cancelUpload(){
		uploadField.interruptUpload();
		annotUploadField.interruptUpload();
	}

	/**
	 * enable or disable components in UMainLayout except pluginView
	 * @param enabled
	 * @return
	 */
	private void enableUMainLayout(boolean enabled){
		Iterator<Component> it = getApplication().getMainWindow().getContent().getComponentIterator();
		while(it.hasNext()){
			Component c = it.next();
			if(c instanceof SplitPanel){
				SplitPanel sp = (SplitPanel)c;
				sp.getFirstComponent().setEnabled(enabled);
			}else c.setEnabled(enabled);
		}
		uploadButton.setEnabled(!enabled);
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
			enableUMainLayout(true);
			
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
			if (choice == Anno.DELETE){
				MessageBox mb = new MessageBox(getWindow(), 
						"To be implemented", 
						MessageBox.Icon.ERROR, 
						"Operation not supported yet",  
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
				return;
			}
			if (choice == null){
				if (loader instanceof LoaderUsingAnnotation){
					MessageBox mb = new MessageBox(getWindow(), 
							"Loading problem", 
							MessageBox.Icon.ERROR, 
							"Annotation file not selected",  
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
					return;
				}
			}
			else if (!(choice instanceof Anno)){
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

			// store pending dataset
			DataSet dataset = loader.storePendingData(dataFile==null?"DataSet":dataFile.getName(), SessionHandler.get().getId());
			
			// send upload start event
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("loader", loader);
			params.put("choice", choice);
			params.put("owner", annotOwner);
			params.put("type", annotType);
			UploadStartedEvent analysisEvent = new UploadStartedEvent(dataset, params, UploadDataUI.this);
			GeworkbenchRoot.getBlackboard().fire(analysisEvent);

			// add pending dataset node
			NodeAddEvent datasetEvent = new NodeAddEvent(dataset);
			GeworkbenchRoot.getBlackboard().fire(datasetEvent);			
		}};
		
		
	public boolean startUpload(DataSet dataSet, HashMap<String, Object> params){
			while(uploadField.isUploading()){
				try{
					Thread.sleep(1000);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
			if (dataFile == null) return false;
			
			Loader loader 				= 	(Loader)params.get("loader");
			Object choice 				= 	params.get("choice");
			User annotOwner 			= 	(User)params.get("owner");
			AnnotationType annotType 	= 	(AnnotationType)params.get("type");
		
			try {
				loader.load(dataFile, dataSet);
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
				return true;
			} catch (GeWorkbenchLoaderException e) {
				MessageBox mb = new MessageBox(getWindow(), 
						"Loading problem", 
						MessageBox.Icon.ERROR, 
						e.getMessage(),  
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();	
				return false;
			}

	}
	
    /**
     * FileOutputStream with 'sleep' flag to toggle slow/normal writing speed
     */
	private class SlowFileOutputStream extends FileOutputStream{
        private boolean sleep;

        /**
         * @param file
         * @param sleep	If set to true, sleep for 100ms/writingBlock to simulate slow speed;
         * 				if set to false, write with normal speed
         * @throws FileNotFoundException
         */
		public SlowFileOutputStream(File file, boolean sleep) throws FileNotFoundException {
			super(file);
			this.sleep = sleep;
		}

		@Override
	    public void write(byte b[], int off, int len) throws IOException {
			super.write(b, off, len);
            if (sleep) {
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                     e.printStackTrace();
                }
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
        private boolean sleep;

        public OutputStream receiveUpload(String filename, String mimetype) {
            fileName = filename;
            mtype = mimetype;
            SlowFileOutputStream fos = null; // Output stream to write to
            String dir = tempDir + SessionHandler.get().getUsername() + dataDir;
			if (!new File(dir).exists())
				new File(dir).mkdirs();
			dataFile = new File(dir, fileName);
            try {
                // Open the file for writing.
                fos = new SlowFileOutputStream(dataFile, sleep);
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
        public void setSlow(final boolean value) {
            sleep = value;
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
