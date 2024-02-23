package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;
import java.util.HashMap;
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
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.plugins.uploaddata.AnnotationUploadLayout.Anno;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.UserActivityLog;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.messagebox.MessageBox;

public class UploadDataUI extends VerticalLayout implements Button.ClickListener {

	private static final long serialVersionUID = 8042523201401300804L;

	private static Log log = LogFactory.getLog(UploadDataUI.class);

	final private FileUploadLayout dataUploadLayout = new FileUploadLayout(this, "data");
	final private AnnotationUploadLayout annoLayout = new AnnotationUploadLayout(this);
	final private Button addButton = new Button("Add to workspace");
	final private Button cancelButton = new Button("Cancel");

	private Loader selectedLoader = null;

	public UploadDataUI() {

		setImmediate(true);

		final ComboBox fileCombo = new ComboBox("Please select type of file");

		for (Loader loader : new LoaderFactory().getParserList()) {
			fileCombo.addItem(loader);
		}

		fileCombo.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8744518843208040408L;

			public void valueChange(ValueChangeEvent event) {
				Object type = fileCombo.getValue();
				if (type instanceof Loader) {
					selectedLoader = (Loader) type;
					addButton.setEnabled(true);
					if (selectedLoader instanceof LoaderUsingAnnotation) {
						annoLayout.setVisible(true);
					} else {
						annoLayout.setVisible(false);
					}
				}
			}
		});

		cancelButton.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -2844610389998064829L;

			@Override
			public void buttonClick(ClickEvent event) {
				addButton.setEnabled(false);
				cancelUpload();
				fileCombo.select(null);
				getMainLayout().unlockGuiForUpload();
			}

		});

		fileCombo.setFilteringMode(FilteringMode.OFF);
		fileCombo.setImmediate(true);
		fileCombo.setRequired(true);
		fileCombo.setNullSelectionAllowed(false);

		addComponent(fileCombo);

		setSpacing(true);
		setMargin(true);
		addComponent(dataUploadLayout);

		addComponent(new Label("<hr/>", ContentMode.HTML));
		addComponent(annoLayout);
		addButton.setImmediate(true);
		addButton.setEnabled(false);
		addButton.addClickListener(this);
		HorizontalLayout btnLayout = new HorizontalLayout();
		btnLayout.setSpacing(true);
		btnLayout.addComponent(addButton);
		btnLayout.addComponent(cancelButton);
		addComponent(btnLayout);
	}

	public void cancelUpload() {
		dataUploadLayout.interruptUpload();
		annoLayout.cancelUpload();
	}

	/* 'add to workspace button' clicked */
	@Override
	public void buttonClick(ClickEvent event) {
		getMainLayout().unlockGuiForUpload();
		addButton.setEnabled(false);

		Object choice = annoLayout.getAnnotationChoice();
		User annotOwner = SessionHandler.get();
		AnnotationType annotType = annoLayout.getAnnotationType();
		Long annoId = null;

		File dataFile = dataUploadLayout.getDataFile();
		if (dataFile == null) {
			MessageBox.createError().withCaption("Loading problem")
					.withMessage("Data file not loaded. No valid data file is chosen.").withOkButton().open();
			addButton.setEnabled(true);
			return;
		}

		if (choice == Anno.DELETE) {
			MessageBox.createError().withCaption("To be implemented").withMessage("Operation not supported yet")
					.withOkButton().open();
			addButton.setEnabled(true);
			return;
		}

		File annotFile = null;

		if (choice == null) {
			if (selectedLoader instanceof LoaderUsingAnnotation) {
				MessageBox.createError().withCaption("Loading problem").withMessage("Annotation file not selected")
						.withOkButton().open();
				addButton.setEnabled(true);
				return;
			}
		} else if (!(choice instanceof Anno)) {
			Anno parent = annoLayout.getAnnotationChoiceGroup();
			// shared default annotation
			if (parent == Anno.PUBLIC) {
				annotOwner = null;
				annoId = (Long) choice;

			}
			// user's loaded annotation
			else if (parent == Anno.PRIVATE) {
				annoId = (Long) choice;
			}
		} else if (choice == Anno.NO) {
			annotFile = null;
			// just loaded
		} else if (choice == Anno.NEW) {
			annotFile = annoLayout.getAnnotationFile();
			if (annotFile == null) {
				MessageBox.createError().withCaption("Loading problem").withMessage("Annotation file not selected")
						.withOkButton().open();
				addButton.setEnabled(true);
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
				log.warn("Annotation file with the same name found on server. It's been overwritten.");
			}
		}

		// store pending dataset. null has been checked earlier
		DataSet dataset = storePendingData(dataFile.getName(), SessionHandler
				.get().getId());
		processFromBackgroundThread(dataFile, dataset, annotOwner, annotType,
				annotFile, annoId);

		// add pending dataset node
		GeworkbenchRoot app = (GeworkbenchRoot) UI.getCurrent();
		app.addNode(dataset);
	}

	static private DataSet storePendingData(String fileName, Long userId) {

		DataSet dataset = new DataSet();
		dataset.setName(fileName + " - Pending");
		dataset.setDescription("pending");
		// dataset.setType(className); /* leave type as null for pending node */
		dataset.setOwner(userId);
		dataset.setWorkspace(WorkspaceUtils.getActiveWorkSpace());
		FacadeFactory.getFacade().store(dataset);

		return dataset;
	}

	private void rollbackFailedUpload(DataSet dataset) {
		FacadeFactory.getFacade().delete(dataset);
		UMainLayout mainLayout = getMainLayout();
		if (mainLayout != null) {
			mainLayout.removeItem(dataset.getId());
		}
	}

	private void processFromBackgroundThread(final File dataFile2,
			final DataSet dataSet, final User annotOwner,
			final AnnotationType annotType, final File annotFile, final Long annoId) {

		final UMainLayout mainLayout = getMainLayout();
		Thread uploadThread = new Thread() {

			@Override
			public void run() {

				try {
					if (selectedLoader instanceof LoaderUsingAnnotation) {
						LoaderUsingAnnotation expressionFileLoader = (LoaderUsingAnnotation) selectedLoader;
						expressionFileLoader.parseAnnotation(annotFile, annotType,
								annotOwner, dataSet.getId(), annoId);
					}
					selectedLoader.load(dataFile2, dataSet);
				} catch (GeWorkbenchLoaderException e) {
					MessageBox.createError().withCaption("Loading problem").withMessage(e.getMessage()).withOkButton()
							.open();

					rollbackFailedUpload(dataSet);
					addButton.setEnabled(true);
					return;
				}

				synchronized (UI.getCurrent()) {
					MessageBox.createInfo().withCaption("Upload Completed").withMessage("Data upload is now completed.")
							.withOkButton(() -> {
								mainLayout.addNode(dataSet);
							}).open();
					addButton.setEnabled(true);
				}
				GeworkbenchRoot ui = (GeworkbenchRoot) UI.getCurrent();
				ui.push();
			}
		};
		// start processing in the background thread
		uploadThread.start();
		User user = SessionHandler.get();
		UserActivityLog ual = new UserActivityLog(user.getUsername(),
				UserActivityLog.ACTIVITY_TYPE.LOAD_DATA.toString(), dataFile2.getName());
		FacadeFactory.getFacade().store(ual);
	}

	// TODO this may not be the best design to get reference to the main layout
	public UMainLayout getMainLayout() {
		Component content = UI.getCurrent().getContent();
		if (content instanceof UMainLayout) {
			return (UMainLayout) content;
		} else {
			return null;
		}
	}
}
