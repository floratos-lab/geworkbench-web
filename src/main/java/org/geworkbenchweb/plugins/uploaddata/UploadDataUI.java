package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;
import java.util.ArrayList;
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
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.utils.WorkspaceUtils;
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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

@SuppressWarnings("deprecation")
public class UploadDataUI extends VerticalLayout {

	private static final long serialVersionUID = 8042523201401300804L;

	private static Log log = LogFactory.getLog(UploadDataUI.class);
			
	public static enum Anno {
		NO("No annotation"), NEW("Load new annotation"), PUBLIC("Public annotation files"),
		PRIVATE("Private annotations files"), DELETE("Delete private annotation files");
		private String value;
		Anno(String v) { value = v; }
		public String toString() { return value; }
	};

	final private ComboBox fileCombo;
	
	final private Tree annotChoices;
	final private ComboBox annotTypes;

	final private FileUploadLayout dataUploadLayout, annotUploadLayout;
	
	final private Button uploadButton = new Button("Add to workspace");

	public UploadDataUI() {

		setImmediate(true);
		
		fileCombo 			= 	new ComboBox("Please select type of file");

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
						annotUploadLayout.setVisible(false);
					}
				}
			}
		});

		fileCombo.setFilteringMode(Filtering.FILTERINGMODE_OFF);
		fileCombo.setImmediate(true);
		fileCombo.setRequired(true);
		fileCombo.setNullSelectionAllowed(false);

		addComponent(fileCombo);

		dataUploadLayout = new FileUploadLayout(this, "data");
		
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
							annotUploadLayout.setVisible(true);
						else
							annotUploadLayout.setVisible(false);
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

		annotUploadLayout = new FileUploadLayout(this, "annotation");
		annotUploadLayout.setVisible(false);

		setSpacing(true);
		addComponent(dataUploadLayout);

		addComponent(new Label("<hr/>", Label.CONTENT_XHTML));
		HorizontalLayout annoLayout = new HorizontalLayout();
		annoLayout.addComponent(annotChoices);
		VerticalLayout rightSideLayout = new VerticalLayout();
		rightSideLayout.setSpacing(true);
		rightSideLayout.addComponent(annotTypes);
		rightSideLayout.addComponent(annotUploadLayout);
		annoLayout.addComponent(rightSideLayout);
		addComponent(annoLayout);
		uploadButton.setEnabled(false);
		uploadButton.addListener(new UploadButtonListener());
		HorizontalLayout btnLayout = new HorizontalLayout();
		btnLayout.setSpacing(true);
		btnLayout.addComponent(uploadButton);
		addComponent(btnLayout);
	}
	
	public void cancelUpload(){
		dataUploadLayout.interruptUpload();
		annotUploadLayout.interruptUpload();
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
			if(c instanceof SplitPanel){
				SplitPanel sp = (SplitPanel)c;
				sp.getFirstComponent().setEnabled(enabled);
			}else c.setEnabled(enabled);
		}
		uploadButton.setEnabled(!enabled);
	}
	
	/* FIXME why does this need to be reset every time?? */
	private void getAnnotChoices(){
		annotChoices.removeAllItems();
		for (Anno anno : Anno.values()){
			annotChoices.addItem(anno);
			if (anno == Anno.NO || anno == Anno.NEW || anno == Anno.DELETE){
				annotChoices.setChildrenAllowed(anno, false);
			}else if (anno == Anno.PUBLIC){
				int cnt = 0;
				File dir = new File(GeworkbenchRoot.getPublicAnnotationDirectory());
				if(!dir.exists() || !dir.isDirectory()) {
					continue; // TODO document when it happens. It did happen and cause a lot of trouble during release
				}
				for (File f : dir.listFiles()){
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

	/* this name is totally misleading - this is AFTER uploading is done, during background 'adding to the workspace' process */
	private class UploadButtonListener implements Button.ClickListener {

		private static final long serialVersionUID = -2592257781106708221L;

		@Override
		public void buttonClick(ClickEvent event) {
			enableUMainLayout(true);
			
			Loader loader 				= 	(Loader) fileCombo.getValue();
			Object choice 				= 	annotChoices.getValue();
			User annotOwner 			= 	SessionHandler.get();
			AnnotationType annotType 	= 	(AnnotationType)annotTypes.getValue();
			
			File dataFile = dataUploadLayout.getDataFile();
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
			
			File annotFile = null;

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
			} else if (!(choice instanceof Anno)){
				String annotFname = (String)choice;
				Object parent = annotChoices.getParent(choice);
				// shared default annotation
				if (parent == Anno.PUBLIC){
					annotOwner = null;
					annotType = AnnotationType.values()[0];
					annotFile = new File(GeworkbenchRoot.getPublicAnnotationDirectory(), annotFname);
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
					String annotDir = GeworkbenchRoot.getBackendDataDirectory()
							+ System.getProperty("file.separator")
							+ annotOwner.getUsername() + "/annotation/";
					annotFile = new File(annotDir, annotFname);
				}
			} else if (choice == Anno.NO) {
				annotFile = null;
				// just loaded
			} else if (choice == Anno.NEW){
				annotFile = annotUploadLayout.getDataFile();
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
					log.warn("Annotation file with the same name found on server. It's been overwritten.");
				}
			}

			// store pending dataset. null has been checked earlier
			DataSet dataset = storePendingData(dataFile.getName(), SessionHandler.get().getId());
			processFromBackgroundThread(dataFile, dataset, loader, choice, annotOwner, annotType, annotFile);

			// add pending dataset node
			NodeAddEvent datasetEvent = new NodeAddEvent(dataset);
			GeworkbenchRoot.getBlackboard().fire(datasetEvent);			
		}
	};

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
		
	private void processFromBackgroundThread(final File dataFile2, final DataSet dataSet,
			final Loader loader, final Object choice, final User annotOwner,
			final AnnotationType annotType, final File annotFile) {

		final UMainLayout mainLayout = getMainLayout();
		Thread uploadThread = new Thread() {
				
			@Override
			public void run() {
					
				try {
					if (loader instanceof LoaderUsingAnnotation) {
						LoaderUsingAnnotation expressionFileLoader = (LoaderUsingAnnotation) loader;
						expressionFileLoader.parseAnnotation(annotFile, annotType,
								annotOwner, dataSet.getId());
					}
					loader.load(dataFile2, dataSet);
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
