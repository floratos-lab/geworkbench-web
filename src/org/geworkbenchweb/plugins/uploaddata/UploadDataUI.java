package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;
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
import org.vaadin.easyuploads.FileFactory;
import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

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
	private UploadField uploadField;
	private UploadField annotUploadField;
	private ComboBox annotChoices;
	private ComboBox annotTypes;

	public UploadDataUI(Long dataSetId) {

		fileCombo = new ComboBox("Please select type of file");
		dataArea = new TextArea(null, initialText);
		uploadField = new UploadField();
		annotUploadField = new UploadField();

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

		uploadField.setFieldType(FieldType.FILE);
		uploadField.setImmediate(false);
		uploadField.setRequired(true);
		uploadField.setFileFactory(new FileFactory() {
			public File createFile(String fileName, String mimeType) {
				File f = new File(System.getProperty("user.home") + "/temp/",
						fileName);
				return f;
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

		annotUploadField.setVisible(false);
		annotUploadField.setFieldType(FieldType.FILE);
		annotUploadField.setImmediate(false);
		annotUploadField.setRequired(true);
		annotUploadField.setFileFactory(new FileFactory() {
			public File createFile(String fileName, String mimeType) {
				String dir = System.getProperty("user.home") + "/temp/"
						+ SessionHandler.get().getUsername();
				if (!new File(dir).exists())
					new File(dir).mkdir();
				File f = new File(dir, fileName);
				return f;
			}
		});

		setSpacing(true);
		annotUploadField.setButtonCaption("Add Annotation File");
		Button uploadButton = new Button("Upload");
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

			Loader loader = (Loader) fileCombo.getValue();
			File dataFile = (File) uploadField.getValue();
			String choice = (String) annotChoices.getValue();
			File annotFile = null;
			User annotOwner = null;
			AnnotationType annotType = null;

			if (dataFile == null) {
				getWindow().showNotification("Data file not loaded", null,
						Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			if (choice == null)
				choice = "";

			// shared default annotation
			if (choice.equals(choices[1])) {
				annotType = AnnotationType.values()[0];
				annotFile = new File(
						System.getProperty("user.home") + "/temp/", choice);
				if (!annotFile.exists()) {
					getWindow().showNotification(
							"Annotation file not found on server", null,
							Notification.TYPE_WARNING_MESSAGE);
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
					annotFile = (File) annotUploadField.getValue();
					if (annotFile == null) {
						getWindow().showNotification(
								"Annotation file not loaded", null,
								Notification.TYPE_WARNING_MESSAGE);
						return;
					}
					if (loadedAnnots.getItemIds().contains(annotFile.getName())) {
						getWindow()
								.showNotification(
										"Annotation file with the same name found on server",
										null, Notification.TYPE_WARNING_MESSAGE);
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
					if (annotFile != null && !annotFile.delete()) {
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
				// e.printStackTrace();
				getWindow().showNotification("Loading problem", e.getMessage(),
						Notification.TYPE_WARNING_MESSAGE);
			}
		}

	}

}
