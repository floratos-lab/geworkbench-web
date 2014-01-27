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
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.messagebox.ButtonId;
import de.steinwedel.messagebox.Icon;
import de.steinwedel.messagebox.MessageBox;
import de.steinwedel.messagebox.MessageBoxListener;

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

	private Label fileUploadStatus 			= 	new Label("Please select a data file to upload");
	private FileUpload uploadField;
    private HorizontalLayout pLayout		=	new HorizontalLayout();
    private ProgressIndicator pIndicator	=	new ProgressIndicator();
	
	private Label annotUploadStatus 			= 	new Label("Please select an annotation file to upload");
	private FileUpload annotUploadField;
    private HorizontalLayout annotPLayout		=	new HorizontalLayout();
    private ProgressIndicator annotPIndicator	=	new ProgressIndicator();

	private Button uploadButton = new Button("Add to workspace");

	public UploadDataUI() {

		setImmediate(true);
		
		fileCombo 			= 	new ComboBox("Please select type of file");
		dataArea 			= 	new TextArea(null, initialText);

		for (Loader loader : new LoaderFactory().getParserList()) {
			fileCombo.addItem(loader);
		}

		fileCombo.addValueChangeListener(new Property.ValueChangeListener() {
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

		fileCombo.setFilteringMode(FilteringMode.OFF);
		fileCombo.setImmediate(true);
		fileCombo.setRequired(true);
		fileCombo.setNullSelectionAllowed(false);
		dataArea.setRows(6);
		dataArea.setColumns(40);

		addComponent(fileCombo);

        uploadField = new FileUpload(this, fileUploadStatus, pLayout, pIndicator, "data");
        
        Button cancelUpload = new Button("Cancel");
        cancelUpload.setStyleName("small");
        cancelUpload.addClickListener(new Button.ClickListener() {
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
		annotChoices.setWidth(220, Unit.PIXELS);
		annotChoices.setVisible(false);
		annotChoices.setImmediate(true);
		annotChoices.addItemClickListener(new ItemClickListener(){
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
		annotTypes.setWidth(200, Unit.PIXELS);
		annotTypes.setValue(AnnotationType.values()[0]);
		annotTypes.setVisible(false);

		annotUploadField = new FileUpload(this, annotUploadStatus, annotPLayout, annotPIndicator, "annotation");
		annotUploadField.setVisible(false);
		annotUploadStatus.setVisible(false);

		Button annotCancelUpload = new Button("Cancel");
		annotCancelUpload.setStyleName("small");
        annotCancelUpload.addClickListener(new Button.ClickListener() {
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
		
		addComponent(fileUploadStatus);
		addComponent(uploadField);
		addComponent(pLayout);
		addComponent(new Label("<hr/>", ContentMode.HTML));
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
		uploadButton.addClickListener(new UploadButtonListener());
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
	void enableUMainLayout(boolean enabled){
		Component maincomp = UI.getCurrent().getContent();
		if(maincomp instanceof UMainLayout){
			Iterator<Component> it = ((UMainLayout) maincomp).iterator();
			while(it.hasNext()){
				Component c = it.next();
				if(c instanceof HorizontalSplitPanel){
					HorizontalSplitPanel sp = (HorizontalSplitPanel)c;
					sp.getFirstComponent().setEnabled(enabled);
				}else c.setEnabled(enabled);
			}
			uploadButton.setEnabled(!enabled);
		}	
	}
	
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

	private void showAnnotUpload(boolean visible) {
		annotTypes.setVisible(visible);
		annotUploadField.setVisible(visible);
		annotUploadStatus.setVisible(visible);
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
			
			File dataFile = uploadField.getDataFile();
			if (dataFile == null) {
				MessageBox.showPlain(Icon.ERROR, 
						"Loading problem", 
						"Data file not loaded. No valid data file is chosen.",  
						ButtonId.OK);
				return;
			}
			
			if (choice == Anno.DELETE){
				MessageBox.showPlain(Icon.ERROR,
						"To be implemented", 
						"Operation not supported yet",  
						ButtonId.OK);
				return;
			}
			
			File annotFile = null;

			if (choice == null){
				if (loader instanceof LoaderUsingAnnotation){
					MessageBox.showPlain(Icon.ERROR,
							"Loading problem", 
							"Annotation file not selected",  
							ButtonId.OK);
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
						MessageBox.showPlain(Icon.ERROR,
								"Loading problem", 
								"Annotation file not found on server",  
								ButtonId.OK);
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
				annotFile = annotUploadField.getDataFile();
				if (annotFile == null) {
					MessageBox.showPlain(Icon.ERROR,
							"Loading problem", 
							"Annotation file not loaded",  
							ButtonId.OK);	
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
					MessageBox.showPlain( 
							Icon.ERROR,
							"Loading problem", 
							e.getMessage(),  
							ButtonId.OK);

					rollbackFailedUpload(dataSet);
					return;
				}

				MessageBox.showPlain(Icon.INFO, 
								"Upload Completed", 
								"Data upload is now completed. ",  
								new MessageBoxListener() {
									@Override
									public void buttonClicked(ButtonId buttonId) {
										if(buttonId == ButtonId.OK) {
											NodeAddEvent resultEvent = new NodeAddEvent(dataSet);
											GeworkbenchRoot.getBlackboard().fire(resultEvent);
										}
									}
								},
								ButtonId.OK);
			}
		};
		// start processing in the background thread
		uploadThread.start();
	}

	// TODO this may not be the best design to get reference to the main layout
	private UMainLayout getMainLayout() {
		Component content = UI.getCurrent().getContent();
		if(content instanceof UMainLayout) {
			return (UMainLayout)content;
		} else {
			return null;
		}
	}

	public void setFileUploadStatus(String string) {
		fileUploadStatus.setValue(string);
	}

}
