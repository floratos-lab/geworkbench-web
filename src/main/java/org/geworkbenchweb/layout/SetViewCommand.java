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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.pojos.SubSetContext;
import org.geworkbenchweb.utils.CSVUtil;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.LayoutUtil;
import org.geworkbenchweb.utils.SubSetOperations;
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
import com.vaadin.server.DownloadStream;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.steinwedel.messagebox.ButtonId;
import de.steinwedel.messagebox.Icon;
import de.steinwedel.messagebox.MessageBox;

/**
 * @author zji
 *
 */
// this is not a nice design, just an escape from chaos
public class SetViewCommand implements Command {
	private static Log log = LogFactory.getLog(SetViewCommand.class);
	
	private static final long serialVersionUID = -5836083095765179395L;

	private Tree markerTree;

	private Tree arrayTree;

	private Tree markerSetTree;

	private Tree arraySetTree;

	private VerticalLayout contextpane, mrkcontextpane;
	
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

		VerticalLayout vlayout = new VerticalLayout();
		openSetWindow.setContent(vlayout);
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

		setGroup.addValueChangeListener(new Property.ValueChangeListener(){
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
				UI pWindow = UI.getCurrent();
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
        			 MessageBox.showPlain(Icon.WARN,
							"File Format Error",
							filename + " is not a CSV file",
							ButtonId.OK);
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
		 
		final Long dataSetId = mainLayout.getMicroarraySetId();
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
		markerSetTree.addItemClickListener(new ItemClickEvent.ItemClickListener() {
			
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
		arraySetTree.addItemClickListener(new ItemClickEvent.ItemClickListener() {
			
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

		// TODO this probably can be done more efficiently
		DataSet generic = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		Long id = generic.getDataId();
		if(id==null) {
			log.error("null ID for MicroarrayDataset");
			return;
		}
		String[] markerLabels = DataSetOperations.getStringLabels("markerLabels", id);
		String[] arrayLabels = DataSetOperations.getStringLabels("arrayLabels", id);
		markerTree.setContainerDataSource(markerTableView(markerLabels, dataSetId));
		arrayTree.setContainerDataSource(arrayTableView(arrayLabels));

		markerTree.setItemCaptionPropertyId("Labels");
		markerTree.setItemCaptionMode(ItemCaptionMode.PROPERTY);

		arrayTree.setItemCaptionPropertyId("Labels");
		arrayTree.setItemCaptionMode(ItemCaptionMode.PROPERTY);

		markerSetTree.setItemCaptionPropertyId("setName");
		markerSetTree.setItemCaptionMode(ItemCaptionMode.PROPERTY);

		arraySetTree.setItemCaptionPropertyId("setName");
		arraySetTree.setItemCaptionMode(ItemCaptionMode.PROPERTY);

		//marker context
		final ComboBox mrkcontextSelector = new ComboBox();
		mrkcontextSelector.setWidth("160px");
		mrkcontextSelector.setImmediate(true);
		mrkcontextSelector.setNullSelectionAllowed(false);
		mrkcontextSelector.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5667499645414167736L;
			public void valueChange(ValueChangeEvent event) {
				Object val = mrkcontextSelector.getValue();
				if (val != null){
					Context context = (Context)val;
					SubSetOperations.setCurrentMarkerContext(dataSetId, context);

					HierarchicalContainer markerData = new HierarchicalContainer();
					List<SubSet> markersets = SubSetOperations.getSubSetsForContext(context);
					markerData.addContainerProperty("setName", String.class, null);
					Item mainItem1 = markerData.addItem("MarkerSets");
					mainItem1.getItemProperty("setName").setValue("Marker Sets");
					markerSetTree.setContainerDataSource(markerData);

					for (SubSet markerset : markersets){
						List<String> markers = markerset.getPositions();
						Long id = markerset.getId();
						markerData.addItem(id);
						markerData.getContainerProperty(id, "setName").setValue(markerset.getName() + " [" + markers.size() + "]");
						markerData.setParent(id, "MarkerSets");
						markerData.setChildrenAllowed(id, true);
						for(int j=0; j<markers.size(); j++) {
							markerData.addItem(markers.get(j)+id);
							markerData.getContainerProperty(markers.get(j)+id, "setName").setValue(markers.get(j));
							markerData.setParent(markers.get(j)+id, id);
							markerData.setChildrenAllowed(markers.get(j)+id, false);
						}
					}
				}
			}
		});

		markerTree.addActionHandler(new MarkerTreeActionHandler(dataSetId , markerSetTree, mrkcontextSelector));

		List<Context> mrkcontexts = SubSetOperations.getMarkerContexts(dataSetId);
		Context mrkcurrent = SubSetOperations.getCurrentMarkerContext(dataSetId);
		for (Context c : mrkcontexts){
			mrkcontextSelector.addItem(c);
			if (mrkcurrent!=null && c.getId()==mrkcurrent.getId()) mrkcontextSelector.setValue(c);
		}

		Button mrknewContextButton = new Button("New");
		mrknewContextButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = -5508188056515818970L;
			public void buttonClick(ClickEvent event) {
				final Window nameWindow = new Window();
				nameWindow.setModal(true);
				nameWindow.setClosable(true);
				nameWindow.setWidth("300px");
				nameWindow.setHeight("150px");
				nameWindow.setResizable(false);
				nameWindow.setCaption("Add New MarkerSet Context");
				nameWindow.setImmediate(true);

				final TextField contextName = new TextField();
				contextName.setInputPrompt("Please enter markerset context name");
				contextName.setImmediate(true);
				VerticalLayout layout = LayoutUtil.addComponent(contextName);
				nameWindow.setContent(layout);
				layout.addComponent(new Button("Ok", new Button.ClickListener() {
					private static final long serialVersionUID = 634733324392150366L;
					public void buttonClick(ClickEvent event) {
		                String name = (String)contextName.getValue();
		                for (Context context : SubSetOperations.getMarkerContexts(dataSetId)){
		                	if (context.getName().equals(name)){
		                		MessageBox.showPlain(Icon.WARN, 
										"Warning", "Name already exists",  
										ButtonId.OK);
		                		return;
		                	}
		                }
		                Context context = new Context(name, "marker", dataSetId);
		                FacadeFactory.getFacade().store(context);
		    			UI.getCurrent().removeWindow(nameWindow);
		                mrkcontextSelector.addItem(context);
		                mrkcontextSelector.setValue(context);
		            }}));
				UI.getCurrent().addWindow(nameWindow);
			}
		});
		
		mrkcontextpane = new VerticalLayout();
		Label mrklabel = new Label("Context for Marker Sets");
		mrkcontextpane.addComponent(mrklabel);
		HorizontalLayout mrkhlayout = new HorizontalLayout();
		mrkhlayout.addComponent(mrkcontextSelector);
		mrkhlayout.addComponent(mrknewContextButton);
		mrkcontextpane.addComponent(mrkhlayout);

		//array context
		final ComboBox contextSelector = new ComboBox();
		contextSelector.setWidth("160px");
		contextSelector.setImmediate(true);
		contextSelector.setNullSelectionAllowed(false);
		contextSelector.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5667499645414167736L;
			public void valueChange(ValueChangeEvent event) {
				Object val = contextSelector.getValue();
				if (val != null){
					Context context = (Context)val;
					SubSetOperations.setCurrentArrayContext(dataSetId, context);

					HierarchicalContainer arrayData = new HierarchicalContainer();
					List<SubSet> arraysets = SubSetOperations.getSubSetsForContext(context);
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
		
		List<Context> contexts = SubSetOperations.getArrayContexts(dataSetId);
		Context current = SubSetOperations.getCurrentArrayContext(dataSetId);
		for (Context c : contexts){
			contextSelector.addItem(c);
			if (current!=null && c.getId()==current.getId()) contextSelector.setValue(c);
		}

		Button newContextButton = new Button("New");
		newContextButton.addClickListener(new Button.ClickListener() {
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
				VerticalLayout layout = LayoutUtil.addComponent(contextName);
				nameWindow.setContent(layout);
				layout.addComponent(new Button("Ok", new Button.ClickListener() {
					private static final long serialVersionUID = 634733324392150366L;
					public void buttonClick(ClickEvent event) {
		                String name = (String)contextName.getValue();
		                for (Context context : SubSetOperations.getArrayContexts(dataSetId)){
		                	if (context.getName().equals(name)){
		                		MessageBox.showPlain(Icon.WARN,
										"Warning", "Name already exists",  
										ButtonId.OK);
		                		return;
		                	}
		                }
		                Context context = new Context(name, "microarray", dataSetId);
		                FacadeFactory.getFacade().store(context);
		    			UI.getCurrent().removeWindow(nameWindow);
		                contextSelector.addItem(context);
		                contextSelector.setValue(context);
		            }}));
				UI.getCurrent().addWindow(nameWindow);
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
		leftMainLayout.addComponent(mrkcontextpane);
		leftMainLayout.addComponent(markerTree);
		leftMainLayout.addComponent(markerSetTree);
		leftMainLayout.addComponent(contextpane);
		leftMainLayout.addComponent(arrayTree);
		leftMainLayout.addComponent(arraySetTree);
		markerTree.setWidth("100%");
		arrayTree.setWidth("100%");
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
			MessageBox.showPlain(Icon.INFO, 
					"Warning", 
					"Please select SubSet to delete", 
					ButtonId.OK);
		}
	}

	/**
	 * Method is used to populate Phenotype Panel
	 * @param maSet
	 * @return - Indexed container with array labels
	 */
	private HierarchicalContainer arrayTableView(String[] arrayLabels) {

		HierarchicalContainer tableData 		= 	new HierarchicalContainer();

		tableData.addContainerProperty("Labels", String.class, null);
		Item mainItem 					= 	tableData.addItem("Phenotypes");
		mainItem.getItemProperty("Labels").setValue("Phenotypes" + " ["+ arrayLabels.length + "]");

		for(int k=0;k<arrayLabels.length;k++) {
			Item item 					= 	tableData.addItem(k);
			tableData.setChildrenAllowed(k, false);
			item.getItemProperty("Labels").setValue(arrayLabels[k]);
			tableData.setParent(k, "Phenotypes");
		}
		return tableData;
	}

	/**
	 * Method is used to populate Marker Panel
	 * @param maSet
	 * @return - Indexed container with marker labels
	 */
	private HierarchicalContainer markerTableView(String[] markerLabels, Long dataSetId) {

		HierarchicalContainer tableData 		= 	new HierarchicalContainer();
		tableData.addContainerProperty("Labels", String.class, null);

		Item mainItem =  tableData.addItem("Markers");
		mainItem.getItemProperty("Labels").setValue("Markers" + " [" + markerLabels.length+ "]");

		/* find annotation information */
		Map<String, String> map = DataSetOperations.getAnnotationMap(dataSetId);
		
		for(int j=0; j<markerLabels.length;j++) {

			Item item 					= 	tableData.addItem(j);
			tableData.setChildrenAllowed(j, false);

			String markerLabel = markerLabels[j];
			String geneSymbol = map.get(markerLabel);
			if(geneSymbol!=null) {
				markerLabel += " (" + geneSymbol + ")";
			}
			item.getItemProperty("Labels").setValue( markerLabel );
			tableData.setParent(j, "Markers");
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

		FileResource resource = new FileResource(new File(savefname)){
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
		Page.getCurrent().open(resource, savefname, false);

	}

	public void openOpenSetWindow() {
		if (openSetWindow.getParent() != null) {
			Notification.show("Window is already open");
        } else {
        	UI.getCurrent().addWindow(openSetWindow);
        }
	}

	public void hideSetView() {
		markerTree.setVisible(false);
		mrkcontextpane.setVisible(false);
		contextpane.setVisible(false);
		arrayTree.setVisible(false);
		markerSetTree.setVisible(false);
		arraySetTree.setVisible(false);
	}

}
