/**
 * 
 */
package org.geworkbenchweb.layout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.pojos.SubSetContext;
import org.geworkbenchweb.utils.CSVUtil;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.UserDirUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;
import org.vaadin.easyuploads.UploadField.StorageMode;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * @author zji
 *
 */
// this is not a nice design, just an escape from chaos
public class SetViewCommand implements Command {

	private static final long serialVersionUID = -5836083095765179395L;

	private Tree markerTree;

	private Tree arrayTree;

	private Tree markerSetTree;

	private Tree arraySetTree;

	private VerticalLayout contextpane;
	
	private Long selectedSubSetId;
	
	final private UMainLayout mainLayout;
	
	/* constructor */
	SetViewCommand(UMainLayout mainLayout) {
		this.mainLayout = mainLayout;
		
		initOpenSetWindow();
	}

	final private Window openSetWindow = new Window("Open Set");
	private void initOpenSetWindow() {
		openSetWindow.center();
		openSetWindow.setWidth("20%");
		openSetWindow.setHeight("40%");

		VerticalLayout vlayout = (VerticalLayout) openSetWindow.getContent();
        vlayout.setMargin(true);
        vlayout.setSpacing(true);

        final OptionGroup setGroup = new OptionGroup("Please choose a set type");
        setGroup.addItem("Marker Set");
        setGroup.addItem("Array Set");
        setGroup.setValue("Array Set");
        setGroup.setImmediate(true);
        vlayout.addComponent(setGroup);

        final OptionGroup markerGroup = new OptionGroup("Markers are represented by");
		markerGroup.addItem("Marker ID");
		markerGroup.addItem("Gene Symbol");
		markerGroup.setValue("Marker ID");
		vlayout.addComponent(markerGroup);
		markerGroup.setVisible(false);

		setGroup.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 2481194620858021204L;
			public void valueChange(ValueChangeEvent event) {
				if (event.getProperty().getValue().equals("Marker Set"))
					markerGroup.setVisible(true);
				else
					markerGroup.setVisible(false);
			}
        });
		
		UploadField openFile = new UploadField(StorageMode.MEMORY){
			private static final long serialVersionUID = -212174451849906591L;
			protected void updateDisplay(){
				Window pWindow = openSetWindow.getParent();
				if (pWindow!= null)  pWindow.removeWindow(openSetWindow);
        		String filename = getLastFileName();
				byte[] bytes = (byte[])getValue();
				final Long dataSetId = mainLayout.getCurrentDatasetId();
	            if (filename.endsWith(".csv")||filename.endsWith(".CSV")){
	            	if (setGroup.getValue().equals("Array Set")){
	            		CSVUtil.loadArraySet(filename, bytes, dataSetId, arraySetTree);
	            	}else{
	            		CSVUtil.loadMarkerSet(filename, bytes, dataSetId, markerSetTree, (String)markerGroup.getValue());
	            	}
        		}else{
					MessageBox mb = new MessageBox(pWindow,
							"File Format Error", MessageBox.Icon.WARN,
							filename + " is not a CSV file",
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
        		}
			}
		};
		openFile.setButtonCaption("Open File");
        openFile.setFieldType(FieldType.BYTE_ARRAY);
		vlayout.addComponent(openFile);
	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.MenuBar.Command#menuSelected(com.vaadin.ui.MenuBar.MenuItem)
	 */
	@Override
	public void menuSelected(MenuItem selectedItem) {
		
		selectedItem.setEnabled(false);
		mainLayout.switchToSetView();

		// TODO does not look like a good idea to recreate theses trees every time
		markerTree	 	= 	new Tree();
		arrayTree 		=	new Tree();
		markerSetTree 	= 	new Tree();
		arraySetTree 	= 	new Tree();
		 
		final Long dataSetId = mainLayout.getCurrentDatasetId();
		markerTree.addActionHandler(new MarkerTreeActionHandler(dataSetId , markerSetTree));
		markerTree.setImmediate(true);
		markerTree.setSelectable(true);
		markerTree.setMultiSelect(true);
		markerTree.setDescription("Markers");

		HierarchicalContainer markerData 	= 	new HierarchicalContainer();
		List<?> sets 						= 	SubSetOperations.getMarkerSets(dataSetId);
		Item mainItem 						= 	markerData.addItem("MarkerSets");
		
		markerData.addContainerProperty("setName", String.class, null);
		mainItem.getItemProperty("setName").setValue("Marker Sets");
		for (int i=0; i<sets.size(); i++) {
			List<String> markers = ((SubSet) sets.get(i)).getPositions();
			Long subSetId = ((SubSet) sets.get(i)).getId();
			markerData.addItem(subSetId);
			markerData.getContainerProperty(subSetId, "setName").setValue(((SubSet) sets.get(i)).getName() + " [" + markers.size() + "]");
			markerData.setParent(subSetId, "MarkerSets");
			markerData.setChildrenAllowed(subSetId, true);
			for(int j=0; j<markers.size(); j++) {
				markerData.addItem(markers.get(j)+subSetId);
				markerData.getContainerProperty(markers.get(j)+subSetId, "setName").setValue(markers.get(j));
				markerData.setParent(markers.get(j)+subSetId, subSetId);
				markerData.setChildrenAllowed(markers.get(j)+subSetId, false);
			}
		}
		markerSetTree.setImmediate(true);
		markerSetTree.setSelectable(true);
		markerSetTree.setMultiSelect(false);
		markerSetTree.setContainerDataSource(markerData);
		markerSetTree.addListener(new ItemClickEvent.ItemClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void itemClick(ItemClickEvent event) {
				try {
					arraySetTree.select(null);
					if (!(event.getItemId() instanceof Long)) selectedSubSetId = null;
					selectedSubSetId = (Long) event.getItemId();
				}catch (Exception e) {							
				}
			}
		});
		
		HierarchicalContainer arrayData = new HierarchicalContainer();
		List<?> aSets = SubSetOperations.getArraySets(dataSetId);
		arrayData.addContainerProperty("setName", String.class, null);
		Item mainItem1 = arrayData.addItem("arraySets");
		mainItem1.getItemProperty("setName").setValue("Phenotype Sets");
		
		for (int i=0; i<aSets.size(); i++) {
			List<String> arrays = ((SubSet) aSets.get(i)).getPositions();
			Long subSetId = ((SubSet) aSets.get(i)).getId();
			arrayData.addItem(subSetId);
			arrayData.getContainerProperty(subSetId, "setName").setValue(((SubSet) aSets.get(i)).getName() + " [" + arrays.size() + "]");
			arrayData.setParent(subSetId, "arraySets");
			arrayData.setChildrenAllowed(subSetId, true);
			for(int j=0; j<arrays.size(); j++) {
				arrayData.addItem(arrays.get(j)+subSetId);
				arrayData.getContainerProperty(arrays.get(j)+subSetId, "setName").setValue(arrays.get(j));
				arrayData.setParent(arrays.get(j)+subSetId, subSetId);
				arrayData.setChildrenAllowed(arrays.get(j)+subSetId, false);
			}
		}
		arraySetTree.setImmediate(true);
		arraySetTree.setMultiSelect(false);
		arraySetTree.setSelectable(true);
		arraySetTree.setContainerDataSource(arrayData);
		arraySetTree.addListener(new ItemClickEvent.ItemClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void itemClick(ItemClickEvent event) {
				try {
					markerSetTree.select(null);
					if (!(event.getItemId() instanceof Long)) selectedSubSetId = null;
					selectedSubSetId = (Long) event.getItemId();
				}catch (Exception e) {							
				}
			}
		});

	    
		//arrayTree.addActionHandler(arrayTreeActionHandler);
		//arrayTree.addActionHandler(new ArrayTreeActionHandler(dataSetId, arraySetTree, contextSelector));
		arrayTree.setImmediate(true);
		arrayTree.setMultiSelect(true);
		arrayTree.setSelectable(true);
		arrayTree.setDescription("Phenotypes");

		DSMicroarraySet maSet = (DSMicroarraySet) ObjectConversion.toObject(UserDirUtils.getDataSet(dataSetId));

		markerTree.setContainerDataSource(markerTableView(maSet));
		arrayTree.setContainerDataSource(arrayTableView(maSet));

		markerTree.setItemCaptionPropertyId("Labels");
		markerTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

		arrayTree.setItemCaptionPropertyId("Labels");
		arrayTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

		markerSetTree.setItemCaptionPropertyId("setName");
		markerSetTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

		arraySetTree.setItemCaptionPropertyId("setName");
		arraySetTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

		final ComboBox contextSelector = new ComboBox();
		contextSelector.setWidth("160px");
		contextSelector.setImmediate(true);
		contextSelector.setNullSelectionAllowed(false);
		contextSelector.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5667499645414167736L;
			public void valueChange(ValueChangeEvent event) {
				Object val = contextSelector.getValue();
				if (val != null){
					Context context = (Context)val;
					SubSetOperations.setCurrentContext(dataSetId, context);

					HierarchicalContainer arrayData = new HierarchicalContainer();
					List<SubSet> arraysets = SubSetOperations.getArraySetsForContext(context);
					arrayData.addContainerProperty("setName", String.class, null);
					Item mainItem1 = arrayData.addItem("arraySets");
					mainItem1.getItemProperty("setName").setValue("Phenotype Sets");
					arraySetTree.setContainerDataSource(arrayData);

					for (SubSet arrayset : arraysets){
						List<String> arrays = arrayset.getPositions();
						Long id = arrayset.getId();
						arrayData.addItem(id);
						arrayData.getContainerProperty(id, "setName").setValue(arrayset.getName() + " [" + arrays.size() + "]");
						arrayData.setParent(id, "arraySets");
						arrayData.setChildrenAllowed(id, true);
						for(int j=0; j<arrays.size(); j++) {
							arrayData.addItem(arrays.get(j)+id);
							arrayData.getContainerProperty(arrays.get(j)+id, "setName").setValue(arrays.get(j));
							arrayData.setParent(arrays.get(j)+id, id);
							arrayData.setChildrenAllowed(arrays.get(j)+id, false);
						}
					}
				}
			}
		});

		arrayTree.addActionHandler(new ArrayTreeActionHandler(dataSetId, arraySetTree, contextSelector));
		
		List<Context> contexts = SubSetOperations.getAllContexts(dataSetId);
		Context current = SubSetOperations.getCurrentContext(dataSetId);
		for (Context c : contexts){
			contextSelector.addItem(c);
			if (current!=null && c.getId()==current.getId()) contextSelector.setValue(c);
		}

		Button newContextButton = new Button("New");
		newContextButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -5508188056515818970L;
			public void buttonClick(ClickEvent event) {
				final Window nameWindow = new Window();
				nameWindow.setModal(true);
				nameWindow.setClosable(true);
				nameWindow.setWidth("300px");
				nameWindow.setHeight("150px");
				nameWindow.setResizable(false);
				nameWindow.setCaption("Add New ArraySet Context");
				nameWindow.setImmediate(true);

				final TextField contextName = new TextField();
				contextName.setInputPrompt("Please enter arrayset context name");
				contextName.setImmediate(true);
				nameWindow.addComponent(contextName);
				nameWindow.addComponent(new Button("Ok", new Button.ClickListener() {
					private static final long serialVersionUID = 634733324392150366L;
					public void buttonClick(ClickEvent event) {
		                String name = (String)contextName.getValue();
		                for (Context context : SubSetOperations.getAllContexts(dataSetId)){
		                	if (context.getName().equals(name)){
		                		MessageBox mb = new MessageBox(mainLayout.getWindow(), 
										"Warning", MessageBox.Icon.WARN, "Name already exists",  
										new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
								mb.show();
		                		return;
		                	}
		                }
		                Context context = new Context(name, dataSetId);
		                FacadeFactory.getFacade().store(context);
		    			mainLayout.getApplication().getMainWindow().removeWindow(nameWindow);
		                contextSelector.addItem(context);
		                contextSelector.setValue(context);
		            }}));
				mainLayout.getApplication().getMainWindow().addWindow(nameWindow);
			}
		});
		
		contextpane = new VerticalLayout();
		//contextpane.setSpacing(true);
		Label label = new Label("Context for Phenotype Sets");
		contextpane.addComponent(label);
		HorizontalLayout hlayout = new HorizontalLayout();
		hlayout.addComponent(contextSelector);
		hlayout.addComponent(newContextButton);
		contextpane.addComponent(hlayout);
		
		CssLayout leftMainLayout = mainLayout.getLeftMainLayout();
		leftMainLayout.addComponent(markerTree);
		leftMainLayout.addComponent(markerSetTree);
		leftMainLayout.addComponent(contextpane);
		leftMainLayout.addComponent(arrayTree);
		leftMainLayout.addComponent(arraySetTree);
	}

	public void removeSelectedSubset() {
		try {
			if(selectedSubSetId != null) {
				
				/* Deleteing all contexts and then subset */
				Map<String, Object> cParam 		= 	new HashMap<String, Object>();
				cParam.put("subsetid", selectedSubSetId);	
				List<SubSetContext> subcontexts = FacadeFactory.getFacade().list("Select a from SubSetContext a where a.subsetid=:subsetid", cParam);
				if(!subcontexts.isEmpty()) {
					FacadeFactory.getFacade().deleteAll(subcontexts);
				}
				
				Map<String, Object> Param 	= 	new HashMap<String, Object>();
				Param.put("id", selectedSubSetId);	
				List<SubSet> subSets = FacadeFactory.getFacade().list("Select a from SubSet a where a.id=:id", Param);
				
				if(subSets.get(0).getType().equalsIgnoreCase("microarray")) {
					if(arraySetTree.hasChildren(selectedSubSetId)) {
						Collection<?> children = arraySetTree.getChildren(selectedSubSetId);
						LinkedList<String> children2 = new LinkedList<String>();
						for (Iterator<?> i = children.iterator(); i.hasNext();)
							 children2.add((String) i.next());
						 
						// Remove the children of the collapsing node
				        for (Iterator<String> i = children2.iterator(); i.hasNext();) {
				            String child = i.next();
				            arraySetTree.removeItem(child);
				        }
					} 
					arraySetTree.removeItem(selectedSubSetId);
				}else {
					if(markerSetTree.hasChildren(selectedSubSetId)) {
						Collection<?> children = markerSetTree.getChildren(selectedSubSetId);
				        LinkedList<String> children2 = new LinkedList<String>();
						for (Iterator<?> i = children.iterator(); i.hasNext();)
							 children2.add((String) i.next());
						
						 // Remove the children of the collapsing node
				        for (Iterator<String> i = children2.iterator(); i.hasNext();) {
				            String child = i.next();
				            markerSetTree.removeItem(child);
				        }
					}
					markerSetTree.removeItem(selectedSubSetId);
				}
				FacadeFactory.getFacade().deleteAll(subSets);
			}
		} catch(Exception e) {
			e.printStackTrace();
			MessageBox mb = new MessageBox(mainLayout.getWindow(), 
					"Warning", 
					MessageBox.Icon.INFO, 
					"Please select SubSet to delete", 
					new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();	
		}
	}

	/**
	 * Method is used to populate Phenotype Panel
	 * @param maSet
	 * @return - Indexed container with array labels
	 */
	private HierarchicalContainer arrayTableView(DSMicroarraySet maSet) {

		HierarchicalContainer tableData 		= 	new HierarchicalContainer();

		tableData.addContainerProperty("Labels", String.class, null);
		Item mainItem 					= 	tableData.addItem("Phenotypes");
		mainItem.getItemProperty("Labels").setValue("Phenotypes" + " ["+ maSet.size() + "]");

		for(int k=0;k<maSet.size();k++) {
			Item item 					= 	tableData.addItem(k);
			tableData.setChildrenAllowed(k, false);
			item.getItemProperty("Labels").setValue(maSet.get(k).getLabel());
			tableData.setParent(k, "Phenotypes");
		}
		return tableData;
	}

	/**
	 * Method is used to populate Marker Panel
	 * @param maSet
	 * @return - Indexed container with marker labels
	 */
	private HierarchicalContainer markerTableView(DSMicroarraySet maSet) {

		HierarchicalContainer tableData 		= 	new HierarchicalContainer();
		tableData.addContainerProperty("Labels", String.class, null);

		Item mainItem =  tableData.addItem("Markers");
		mainItem.getItemProperty("Labels").setValue("Markers" + " [" + maSet.getMarkers().size()+ "]");

		for(int j=0; j<maSet.getMarkers().size();j++) {

			Item item 					= 	tableData.addItem(j);
			tableData.setChildrenAllowed(j, false);

			for(int k=0;k<=maSet.size();k++) {
				if(k == 0) {
					item.getItemProperty("Labels").setValue(maSet.getMarkers().get(j).getLabel() 
							+ " (" 
							+ maSet.getMarkers().get(j).getGeneName()
							+ ")");
					tableData.setParent(j, "Markers");
				} 
			}
		}
		return tableData;
	}

	public void saveSelectedSet() {
		if(selectedSubSetId == null) return;
		final SubSet subSet = FacadeFactory.getFacade().find(SubSet.class, selectedSubSetId);

		User user = SessionHandler.get();
		String saveSetDir = System.getProperty("user.home") + "/temp/" + user.getUsername() + "/savedSet/";
		if (!new File(saveSetDir).exists())	new File(saveSetDir).mkdirs();
		String savefname = saveSetDir + subSet.getName() + ".csv";
		CSVUtil.saveSetToFile(savefname, subSet);

		FileResource resource = new FileResource(new File(savefname), mainLayout.getApplication()){
			private static final long serialVersionUID = -4237233790958289183L;
			public DownloadStream getStream() {
		        try {
		            final DownloadStream ds = new DownloadStream(new FileInputStream(
		                    getSourceFile()), getMIMEType(), getFilename());
		            ds.setParameter("Content-Disposition", "attachment; filename=\""
		                    + getFilename()+"\"");
		            ds.setCacheTime(0);
		            return ds;
		        } catch (final FileNotFoundException e) {
		            e.printStackTrace();
		            return null;
		        }
		    }
		};
		mainLayout.getApplication().getMainWindow().open(resource);

	}

	public void openOpenSetWindow() {
		if (openSetWindow.getParent() != null) {
			mainLayout.getWindow().showNotification("Window is already open");
        } else {
        	mainLayout.getWindow().addWindow(openSetWindow);
        }
	}

	public void hideSetView() {
		markerTree.setVisible(false);
		contextpane.setVisible(false);
		arrayTree.setVisible(false);
		markerSetTree.setVisible(false);
		arraySetTree.setVisible(false);
	}

}
