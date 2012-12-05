package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
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
	private static final String[] choices = { "No annotation",
			"HG_U95Av2.na32.annot.csv", "Use your own annotation file" };
	private static final String loadOption = "Load annotation file now";

	private ComboBox loadedAnnots = new ComboBox(
			"Choose available annotation or load new one");
	private ComboBox fileCombo;
	private TextArea dataArea;
	
	private ComboBox annotChoices;
	private ComboBox annotTypes;

	private Label fileUploadStatus 			= 	new Label("Please select a data file to upload");
	private DataFileReceiver fileReceiver 	= 	new DataFileReceiver();
	private Upload uploadField 				= 	new Upload(null, fileReceiver);
	
	private Label annotUploadStatus 			= 	new Label("Please select a annotation file to upload");
	private AnnotFileReceiver annotFileReceiver = 	new AnnotFileReceiver();
	private Upload annotUploadField 			= 	new Upload(null, annotFileReceiver);
	
	private File dataFile;
	private File annotFile;
	
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
						annotChoices.setValue(choices[0]);
						annotChoices.setVisible(true);
					} else {
						annotChoices.setValue(null);
						annotChoices.setVisible(false);
						loadedAnnots.setVisible(false);
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
		
		annotChoices = new ComboBox("Choose annotation", Arrays.asList(choices));
		annotChoices.setNullSelectionAllowed(false);
		annotChoices.setWidth(200, 0);
		annotChoices.setVisible(false);
		annotChoices.setImmediate(true);
		annotChoices.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8744518843208040408L;

			public void valueChange(ValueChangeEvent event) {
				Object choice = annotChoices.getValue();
				if (choice != null) {
					loadedAnnots.setVisible(false);
					showAnnotUpload(false);
					if (choice.equals(choices[2])) {
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("owner", SessionHandler.get().getId());
						List<Annotation> annots = FacadeFactory
								.getFacade()
								.list("Select a from Annotation as a where a.owner=:owner",
										params);
						if (!annots.isEmpty()) {
							loadedAnnots.removeAllItems();
							for (Annotation a : annots)
								loadedAnnots.addItem(a.getName());
							loadedAnnots.addItem(loadOption);
							loadedAnnots.setValue(annots.get(0).getName());
							loadedAnnots.setVisible(true);
						} else
							showAnnotUpload(true);
					}
				}
			}
		});

		loadedAnnots.setNullSelectionAllowed(false);
		loadedAnnots.setWidth(200, 0);
		loadedAnnots.setVisible(false);
		loadedAnnots.setImmediate(true);
		loadedAnnots.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -6160532471996111349L;

			public void valueChange(ValueChangeEvent evt) {
				Object o = loadedAnnots.getValue();
				if (o != null) {
					if (o.equals(loadOption))
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
		addComponent(annotChoices);
		addComponent(loadedAnnots);
		addComponent(annotTypes);
		addComponent(annotUploadField);
		addComponent(uploadButton);
		uploadButton.addListener(new UploadButtonListener());
	}

	private void showAnnotUpload(boolean visible) {
		annotTypes.setVisible(visible);
		annotUploadField.setVisible(visible);
	}

	private class UploadButtonListener implements Button.ClickListener {

		private static final long serialVersionUID = -2592257781106708221L;

		@Override
		public void buttonClick(ClickEvent event) {
			
			Loader loader 				= 	(Loader) fileCombo.getValue();
			String choice 				= 	(String) annotChoices.getValue();
			User annotOwner 			= 	null;
			AnnotationType annotType 	= 	null;
			
			if (dataFile == null) {
				MessageBox mb = new MessageBox(getWindow(), 
						"Loading problem", 
						MessageBox.Icon.ERROR, 
						"Data file not loaded. No valid data file is chosen.",  
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
				return;
			}
			if (choice == null){
				choice = "";
			}
			// shared default annotation
			if (choice.equals(choices[1])) {
				annotType = AnnotationType.values()[0];
				annotFile = new File(
						System.getProperty("user.home") + "/temp/", choice);
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
			else if (choice.equals(choices[2])) {
				annotOwner = SessionHandler.get();
				// previously loaded
				if (loadedAnnots.isVisible()
						&& !loadedAnnots.getValue().equals(loadOption))
					annotFile = new File(System.getProperty("user.home")
							+ "/temp/" + annotOwner.getUsername(),
							(String) loadedAnnots.getValue());
				// just loaded
				else {
					annotType = (AnnotationType) annotTypes.getValue();
					if (annotFile == null) {
						MessageBox mb = new MessageBox(getWindow(), 
								"Loading problem", 
								MessageBox.Icon.ERROR, 
								"Annotation file not loaded",  
								new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
						mb.show();	
						return;
					}
					if (loadedAnnots.getItemIds().contains(annotFile.getName())) {
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
			}

			try {
				loader.load(dataFile);
				if (loader instanceof LoaderUsingAnnotation) {
					LoaderUsingAnnotation expressionFileLoader = (LoaderUsingAnnotation) loader;
					expressionFileLoader.parseAnnotation(annotFile, annotType,
							annotOwner);
					if (annotFile != null && choice.equals(choices[2]) && !annotFile.delete()) {
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
	
	public class DataFileReceiver implements Receiver {

		private static final long serialVersionUID = 1L;

		private String fileName;
        private String mtype;

        public OutputStream receiveUpload(String filename, String mimetype) {
            fileName = filename;
            mtype = mimetype;
            FileOutputStream fos = null; // Output stream to write to
            dataFile = new File(System.getProperty("user.home") + "/temp/" + filename);
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

	public class AnnotFileReceiver implements Receiver {

		private static final long serialVersionUID = 1L;

		private String fileName;
        private String mtype;

        public OutputStream receiveUpload(String filename, String mimetype) {
            fileName = filename;
            mtype = mimetype;
            FileOutputStream fos = null; // Output stream to write to
            String dir = System.getProperty("user.home") + "/temp/"
					+ SessionHandler.get().getUsername();
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
