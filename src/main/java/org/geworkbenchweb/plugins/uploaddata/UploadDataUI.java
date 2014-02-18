package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.util.AnnotationInformationManager.AnnotationType;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.dataset.GeWorkbenchLoaderException;
import org.geworkbenchweb.dataset.Loader;
import org.geworkbenchweb.dataset.LoaderFactory;
import org.geworkbenchweb.dataset.LoaderUsingAnnotation;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.plugins.uploaddata.AnnotationUploadLayout.Anno;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class UploadDataUI extends VerticalLayout implements Button.ClickListener {

	private static final long serialVersionUID = 8042523201401300804L;

	private static Log log = LogFactory.getLog(UploadDataUI.class);
			
	final private FileUploadLayout dataUploadLayout = new FileUploadLayout(this, "data");
	final private AnnotationUploadLayout annoLayout = new AnnotationUploadLayout(this);
	final private Button addButton = new Button("Add to workspace");
	
	private Loader selectedLoader = null;

	public UploadDataUI() {

		setImmediate(true);
		
		final ComboBox fileCombo 			= 	new ComboBox("Please select type of file");

		for (Loader loader : new LoaderFactory().getParserList()) {
			fileCombo.addItem(loader);
		}

		fileCombo.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8744518843208040408L;

			public void valueChange(ValueChangeEvent event) {
				Object type = fileCombo.getValue();
				if (type instanceof Loader) {
					selectedLoader = (Loader) type;
					addButton.setEnabled(false);
					if (selectedLoader instanceof LoaderUsingAnnotation) {
						annoLayout.setVisible(true);
					} else {
						annoLayout.setVisible(false);
					}
				}
			}
		});

		fileCombo.setFilteringMode(Filtering.FILTERINGMODE_OFF);
		fileCombo.setImmediate(true);
		fileCombo.setRequired(true);
		fileCombo.setNullSelectionAllowed(false);

		addComponent(fileCombo);

		setSpacing(true);
		addComponent(dataUploadLayout);

		addComponent(new Label("<hr/>", Label.CONTENT_XHTML));
		addComponent(annoLayout);
		addButton.setEnabled(false);
		addButton.addListener(this);
		HorizontalLayout btnLayout = new HorizontalLayout();
		btnLayout.setSpacing(true);
		btnLayout.addComponent(addButton);
		addComponent(btnLayout);
	}
	
	public void cancelUpload(){
		dataUploadLayout.interruptUpload();
		annoLayout.cancelUpload();
	}

	/**
	 * enable or disable components in UMainLayout except pluginView
	 * @param enabled
	 * @return
	 */
	void enableUMainLayout(boolean enabled){
		Iterator<Component> it = getApplication().getMainWindow().getContent().getComponentIterator();
		while(it.hasNext()){
			Component c = it.next();
			if(c instanceof HorizontalSplitPanel){
				HorizontalSplitPanel sp = (HorizontalSplitPanel)c;
				sp.getFirstComponent().setEnabled(enabled);
			}else c.setEnabled(enabled);
		}
		addButton.setEnabled(!enabled);
	}

	/* 'add to workspace button' clicked */
	@Override
	public void buttonClick(ClickEvent event) {
		enableUMainLayout(true);

		Object choice = annoLayout.getAnnotationChoice();
		User annotOwner = SessionHandler.get();
		AnnotationType annotType = annoLayout.getAnnotationType();

		File dataFile = dataUploadLayout.getDataFile();
		if (dataFile == null) {
			MessageBox mb = new MessageBox(getWindow(), "Loading problem",
					MessageBox.Icon.ERROR,
					"Data file not loaded. No valid data file is chosen.",
					new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();
			return;
		}

		if (choice == Anno.DELETE) {
			MessageBox mb = new MessageBox(getWindow(), "To be implemented",
					MessageBox.Icon.ERROR, "Operation not supported yet",
					new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();
			return;
		}

		File annotFile = null;

		if (choice == null) {
			if (selectedLoader instanceof LoaderUsingAnnotation) {
				MessageBox mb = new MessageBox(getWindow(), "Loading problem",
						MessageBox.Icon.ERROR, "Annotation file not selected",
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
				return;
			}
		} else if (!(choice instanceof Anno)) {
			String annotFname = (String) choice;
			Anno parent = annoLayout.getAnnotationChoiceGroup();
			// shared default annotation
			if (parent == Anno.PUBLIC) {
				annotOwner = null;
				annotType = AnnotationType.values()[0];
				annotFile = new File(
						GeworkbenchRoot.getPublicAnnotationDirectory(),
						annotFname);
				if (!annotFile.exists()) {
					MessageBox mb = new MessageBox(getWindow(),
							"Loading problem", MessageBox.Icon.ERROR,
							"Annotation file not found on server",
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
					return;
				}
			}
			// user's loaded annotation
			else if (parent == Anno.PRIVATE) {
				annotType = null;
				String annotDir = GeworkbenchRoot.getBackendDataDirectory()
						+ System.getProperty("file.separator")
						+ annotOwner.getUsername() + "/annotation/";
				annotFile = new File(annotDir, annotFname);
			}
		} else if (choice == Anno.NO) {
			annotFile = null;
			// just loaded
		} else if (choice == Anno.NEW) {
			annotFile = annoLayout.getAnnotationFile();
			if (annotFile == null) {
				MessageBox mb = new MessageBox(getWindow(), "Loading problem",
						MessageBox.Icon.ERROR, "Annotation file not loaded",
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
				log.warn("Annotation file with the same name found on server. It's been overwritten.");
			}
		}

		// store pending dataset. null has been checked earlier
		DataSet dataset = storePendingData(dataFile.getName(), SessionHandler
				.get().getId());
		processFromBackgroundThread(dataFile, dataset, annotOwner, annotType,
				annotFile);

		// add pending dataset node
		NodeAddEvent datasetEvent = new NodeAddEvent(dataset);
		GeworkbenchRoot.getBlackboard().fire(datasetEvent);
	}

	static private DataSet storePendingData(String fileName, Long userId){

		DataSet dataset = new DataSet();
		dataset.setName(fileName + " - Pending");
		dataset.setDescription("pending");
		dataset.setType(DSDataSet.class.getName());
		dataset.setOwner(userId);
		dataset.setWorkspace(WorkspaceUtils.getActiveWorkSpace());
		FacadeFactory.getFacade().store(dataset);
		
		return dataset;
	}
	
	private void rollbackFailedUpload(DataSet dataset) {
		FacadeFactory.getFacade().delete(dataset);
		UMainLayout mainLayout = getMainLayout();
		if(mainLayout!=null) {
			mainLayout.removeItem(dataset.getId());
		}
	}
		
	private void processFromBackgroundThread(final File dataFile2,
			final DataSet dataSet, final User annotOwner,
			final AnnotationType annotType, final File annotFile) {

		final UMainLayout mainLayout = getMainLayout();
		Thread uploadThread = new Thread() {
				
			@Override
			public void run() {
					
				try {
					if (selectedLoader instanceof LoaderUsingAnnotation) {
						LoaderUsingAnnotation expressionFileLoader = (LoaderUsingAnnotation) selectedLoader;
						expressionFileLoader.parseAnnotation(annotFile, annotType,
								annotOwner, dataSet.getId());
					}
					selectedLoader.load(dataFile2, dataSet);
				} catch (GeWorkbenchLoaderException e) {
					MessageBox mb = new MessageBox(getWindow(), 
							"Loading problem", 
							MessageBox.Icon.ERROR, 
							e.getMessage(),  
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
					
					rollbackFailedUpload(dataSet);
					return;
				}

				synchronized(mainLayout.getApplication()) {
						MessageBox mb = new MessageBox(mainLayout.getApplication().getMainWindow(),
								"Upload Completed", 
								MessageBox.Icon.INFO, 
								"Data upload is now completed. ",  
								new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
						mb.show(new MessageBox.EventListener() {
							private static final long serialVersionUID = 1L;
							@Override
							public void buttonClicked(ButtonType buttonType) {    	
								if(buttonType == ButtonType.OK) {
									NodeAddEvent resultEvent = new NodeAddEvent(dataSet);
									GeworkbenchRoot.getBlackboard().fire(resultEvent);
								}
							}
						});
				}
				mainLayout.push();
			}
		};
		// start processing in the background thread
		uploadThread.start();
	}

	// TODO this may not be the best design to get reference to the main layout
	private UMainLayout getMainLayout() {
		Window w = getApplication().getMainWindow();
		ComponentContainer content = w.getContent();
		if(content instanceof UMainLayout) {
			return (UMainLayout)content;
		} else {
			return null;
		}
	}
}
