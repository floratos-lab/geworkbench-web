package org.geworkbenchweb.layout;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbenchweb.dataset.UDataSetUpload;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

public class UAccordionPanel extends  Accordion implements Property.ValueChangeListener, Button.ClickListener, Action.Handler {

	private static final long serialVersionUID = 4523693969296820932L;

	private static Tree dataTree;

	private Table arrayTable;

	private Table markerTable;

	final Action ACTION_SUBSET 		= new Action("Create SubSet");

	final Action[] ACTIONS_CREATE 	= new Action[] { ACTION_SUBSET };

	private String setType;

	protected String selectedValues = null;

	public Long dataSetId;

	public DSMicroarraySet maSet;

	User user	=	SessionHandler.get();

	private static Long userId; 
	// Actions for the context menu

	private static final Action ACTION_DELETE	 	= 	new Action("Delete");

	private static final Action ACTION_ANALYZE		= 	new Action("Analyze Data"); 

	private static final Action ACTION_NORMALIZE	= 	new Action("Normalize Data");

	private static final Action ACTION_FILTER		= 	new Action("Filter Data");

	private static final Action ACTION_INTERACTIONS =	new Action("Get Interactions");

	private static final Action[] ACTIONS 			= 	new Action[] { ACTION_ANALYZE, ACTION_INTERACTIONS, ACTION_NORMALIZE, ACTION_FILTER, ACTION_DELETE };

	public UAccordionPanel(boolean closable) {

		super();
		VerticalLayout l 	= 	new VerticalLayout();
		l.setMargin(true);
		Tab t = addTab(l);

		userId = user.getId();

		t.setCaption("DataSets");

		VerticalLayout dataSets = 	new VerticalLayout();
		Button updateDataset 	= 	new Button("Upload DataSet", new ClickListener() {

			private static final long serialVersionUID = 658137872256766310L;

			@Override
			public void buttonClick(ClickEvent event) {

				UDataSetUpload dataWindow = new UDataSetUpload();
				getApplication().getMainWindow().addWindow(dataWindow);

			}

		});

		updateDataset.setIcon(new ThemeResource("../runo/icons/16/document-add.png"));

		dataSets.addComponent(updateDataset);
		dataSets.setComponentAlignment(updateDataset, Alignment.TOP_CENTER);

		dataTree = new Tree();
		dataTree.setImmediate(true);
		dataTree.areChildrenAllowed(true);
		dataTree.setStyleName(Reindeer.TABLE_BORDERLESS);
		dataTree.addContainerProperty("DataSet Name", String.class, "");
		dataTree.setContainerDataSource(getDataContainer());
		dataSets.addComponent(dataTree);

		dataTree.addActionHandler(this);
		dataTree.addListener(this);

		l.addComponent(dataSets);
		l.setHeight("100%");

		t.setIcon(new ThemeResource("../runo/icons/16/folder.png"));

		markerTable = new Table();
		markerTable.setStyleName(Reindeer.TABLE_BORDERLESS);
		markerTable.setSizeFull();
		markerTable.setSelectable(true);
		markerTable.setMultiSelect(true);
		markerTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);

		markerTable.addListener(new Table.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {

				String value = (event.getProperty().getValue()).toString();
				setType = "marker";
				setSelectedValues(value);

			}

		});

		markerTable.addActionHandler(new Action.Handler() {

			private static final long serialVersionUID = 1L;

			public Action[] getActions(Object target, Object sender) {

				return ACTIONS_CREATE;

			}

			public void handleAction(Action action, Object sender, Object target) {

				if(selectedValues == null) {

					getApplication().getMainWindow().showNotification("Please select atleast one marker",  
							Notification.TYPE_ERROR_MESSAGE );

				} else {

					final Window nameWindow = new Window();
					nameWindow.setModal(true);
					nameWindow.setClosable(true);
					nameWindow.setWidth("300px");
					nameWindow.setHeight("150px");
					nameWindow.setResizable(false);
					nameWindow.setCaption("Add Markers to Set");
					nameWindow.setImmediate(true);

					final TextField setName = new TextField();
					setName.setInputPrompt("Please enter set name");
					setName.setImmediate(true);

					Button addSet = new Button("Add Set", new ClickListener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void buttonClick(ClickEvent event) {

							String setN = (String) setName.getValue();
							if(setN != "") {

								if( SubSetOperations.storeData(selectedValues, setType, setN, dataSetId ) == true ) {

									USetsTabSheet.getSetsTabSheetObject().populateTabSheet(maSet);
									getApplication().getMainWindow().removeWindow(nameWindow);

								}
							} else {

								getApplication().getMainWindow().showNotification("Set Name cannot be empty.",
										Notification.TYPE_ERROR_MESSAGE );
								getApplication().getMainWindow().removeWindow(nameWindow);
							}
						}

					});

					nameWindow.addComponent(setName);
					nameWindow.addComponent(addSet);
					getApplication().getMainWindow().addWindow(nameWindow);
					//selectedValues = null;
				}
			}	 
		});

		Tab t1 = addTab(markerTable);
		t1.setCaption("Makers");
		t1.setIcon(new ThemeResource("../runo/icons/16/folder.png"));

		arrayTable = new Table();
		arrayTable.setStyleName(Reindeer.TABLE_BORDERLESS);
		arrayTable.setSizeFull();
		arrayTable.setSelectable(true);
		arrayTable.setMultiSelect(true);
		arrayTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		arrayTable.addListener(new Table.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {

				String value = (event.getProperty().getValue()).toString();
				setType = "Microarray";
				setSelectedValues(value);

			}

		});

		arrayTable.addActionHandler(new Action.Handler() {

			private static final long serialVersionUID = 1L;

			public Action[] getActions(Object target, Object sender) {

				return ACTIONS_CREATE;

			}
			public void handleAction(Action action, Object sender, Object target) {

				if(selectedValues == null) {

					getApplication().getMainWindow().showNotification("Please select atleast one phenotype",  
							Notification.TYPE_ERROR_MESSAGE );

				} else {

					final Window nameWindow = new Window();
					nameWindow.setModal(true);
					nameWindow.setClosable(true);
					nameWindow.setWidth("300px");
					nameWindow.setHeight("150px");
					nameWindow.setResizable(false);
					nameWindow.setCaption("Add Phenotypes to Set");
					nameWindow.setImmediate(true);

					final TextField setName = new TextField();
					setName.setInputPrompt("Please enter set name");
					setName.setImmediate(true);

					Button addSet = new Button("Add Set", new ClickListener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void buttonClick(ClickEvent event) {

							String setN = (String) setName.getValue();
							if(setN != "") {

								if( SubSetOperations.storeData(selectedValues, setType, setN, dataSetId ) == true ) {

									getApplication().getMainWindow().removeWindow(nameWindow);
									USetsTabSheet.getSetsTabSheetObject().populateTabSheet(maSet);

								}
							} else {

								getApplication().getMainWindow().showNotification("Set Name cannot be empty.",
										Notification.TYPE_ERROR_MESSAGE );
								getApplication().getMainWindow().removeWindow(nameWindow);
							}

						}

					});

					nameWindow.addComponent(setName);
					nameWindow.addComponent(addSet);
					getApplication().getMainWindow().addWindow(nameWindow);
					//selectedValues = null;
				}
			}	 
		});
		Tab t2 = addTab(arrayTable);
		t2.setCaption("Phenotypes");
		t2.setIcon(new ThemeResource("../runo/icons/16/folder.png"));

	}


	protected void setSelectedValues(String value) {

		this.selectedValues = value;

	}

	@Override
	public void buttonClick(ClickEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		try {

			if((String) event.getProperty().getValue() != null ) {


				VerticalSplitPanel menuPanel 	= 	new VerticalSplitPanel();
				menuPanel.setStyleName(Reindeer.SPLITPANEL_SMALL);
				menuPanel.setImmediate(true);
				menuPanel.setLocked(true);
				menuPanel.setSplitPosition(23, Sizeable.UNITS_PIXELS);

				UMenuBar toolBar 	= 	UMenuBar.getMenuBarObject();
				toolBar.setImmediate(true);

				menuPanel.setFirstComponent(toolBar);

				String dataPeru					= 	(String) event.getProperty().getValue();
				String query 					= 	"Select p from DataSet as p where p.name=:name and p.owner=:owner";
				Map<String, Object> parameters 	= 	new HashMap<String, Object>();

				parameters.put("name", dataPeru);
				parameters.put("owner", user.getId());

				DataSet dataSet 				= 	FacadeFactory.getFacade().find(query, parameters);

				if(dataSet != null) {
					/*
					 * if this is a dataSet
					 */

					dataSetId 					=	dataSet.getId();
					byte[] dataByte 			= 	dataSet.getData();
					maSet 						= 	(DSMicroarraySet) toObject(dataByte);

					File annotFile = new File((System.getProperty("user.home") + "/temp/HG_U95Av2.na32.annot.csv"));
					AnnotationParser.loadAnnotationFile(maSet, annotFile);

					markerTable.setContainerDataSource(markerTableView(maSet));
					arrayTable.setContainerDataSource(arrayTableView(maSet));

					UVisualPlugin tabSheet 			= 	new UVisualPlugin(maSet, dataSet.getType(), null);

					USetsTabSheet.getSetsTabSheetObject().populateTabSheet(maSet);

					menuPanel.setSecondComponent(tabSheet);
					UMainLayout.setMainPanelSecondComponent(menuPanel);

				} else {

					String querySub 					= 	"Select p from ResultSet as p where p.name=:name and p.owner=:owner";
					Map<String, Object> params 			= 	new HashMap<String, Object>();

					params.put("name", dataPeru);
					params.put("owner", user.getId());

					ResultSet resultSet 				= 	FacadeFactory.getFacade().find(querySub, params);
					if(resultSet != null) {

						byte[] dataByte 					= 	resultSet.getData();
						UVisualPlugin tabSheet = null;

						if(resultSet.getType().equalsIgnoreCase("CNKB")) {

							@SuppressWarnings("unchecked")
							Vector<CellularNetWorkElementInformation> hits 	=	(Vector<CellularNetWorkElementInformation>) toObject(dataByte);
							tabSheet	= 	new UVisualPlugin(hits, resultSet.getType(), null);

						}else if(resultSet.getType().equalsIgnoreCase("Hierarchical Clustering")) {

							CSHierClusterDataSet hierResults 	= 	(CSHierClusterDataSet) toObject(dataByte);
							tabSheet 	= 	new UVisualPlugin(hierResults, resultSet.getType(), null);

						}else if(resultSet.getType().equalsIgnoreCase("ARACne")) {

							AdjacencyMatrixDataSet dSet 	= 	(AdjacencyMatrixDataSet) toObject(dataByte);
							tabSheet 	= 	new UVisualPlugin(dSet, resultSet.getType(), null);

						}

						menuPanel.setSecondComponent(tabSheet);
						UMainLayout.setMainPanelSecondComponent(menuPanel);
					}
				}

				UMainLayout.mainPanelRequestRepaint();		

			}else {
				
				USetsTabSheet.getSetsTabSheetObject().removeData();
				CustomLayout welcome = new CustomLayout("welcome");
				welcome.setSizeFull();
				UMainLayout.setMainPanelSecondComponent(welcome);
				
			}
		}catch(Exception e) {
			
			//USetsTabSheet.getSetsTabSheetObject().removeData();

		}
	}


	/**
	 * Method is used to populate Phenotype Panel
	 * @param maSet
	 * @return - Indexed container with array labels
	 */

	private Container arrayTableView(DSMicroarraySet maSet) {

		IndexedContainer tableData 		= 	new IndexedContainer();

		for(int k=0;k<maSet.size();k++) {

			Item item 					= 	tableData.addItem(k);
			tableData.addContainerProperty("Labels", String.class, null);
			item.getItemProperty("Labels").setValue(maSet.get(k));

		}


		return tableData;
	}

	/**
	 * Method is used to populate Marker Panel
	 * @param maSet
	 * @return - Indexed container with marker labels
	 */

	private IndexedContainer markerTableView(DSMicroarraySet maSet) {

		IndexedContainer tableData 		= 	new IndexedContainer();

		for(int j=0; j<maSet.getMarkers().size();j++) {

			Item item 					= 	tableData.addItem(j);

			for(int k=0;k<=maSet.size();k++) {

				if(k == 0) {

					tableData.addContainerProperty("Labels", String.class, null);
					item.getItemProperty("Labels").setValue(maSet.getMarkers().get(j));

				} 
			}
		}
		return tableData;

	}


	@SuppressWarnings("deprecation")
	public Object toObject(byte[] bytes){ 

		Object object = null; 

		try{ 

			object = new java.io.ObjectInputStream(new 
					java.io.ByteArrayInputStream(bytes)).readObject(); 

		}catch(java.io.IOException ioe){ 

			java.util.logging.Logger.global.log(java.util.logging.Level.SEVERE, 
					ioe.getMessage()); 

		}catch(java.lang.ClassNotFoundException cnfe){ 

			java.util.logging.Logger.global.log(java.util.logging.Level.SEVERE, 
					cnfe.getMessage()); 

		} 

		return object; 

	}

	@Override
	public Action[] getActions(Object target, Object sender) {

		return ACTIONS;

	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {

		if (action == ACTION_DELETE) {

			String dataName 				=	(target.toString());

			String query 					= 	"Select p from DataSet as p where p.name=:name and p.owner=:owner";
			Map<String, Object> parameters 	= 	new HashMap<String, Object>();

			parameters.put("name", dataName);
			parameters.put("owner", user.getId());

			DataSet dataSet 				= 	FacadeFactory.getFacade().find(query, parameters);

			CustomLayout welcome = new CustomLayout("welcome");
			welcome.setSizeFull();

			if(dataSet != null) {

				FacadeFactory.getFacade().delete(dataSet);
				dataTree.removeItem(target);


			} else {

				String querySub 					= 	"Select p from ResultSet as p where p.name=:name and p.owner=:owner";
				Map<String, Object> params 			= 	new HashMap<String, Object>();

				params.put("name", dataName);
				params.put("owner", user.getId());
				ResultSet resultSet 				= 	FacadeFactory.getFacade().find(querySub, params);
				FacadeFactory.getFacade().delete(resultSet);
				dataTree.removeItem(target);


			}

			UMainLayout.setMainPanelSecondComponent(welcome);

		}else if(action == ACTION_ANALYZE || action == ACTION_INTERACTIONS) {

			String dataPeru					= 	(target.toString());
			String query 					= 	"Select p from DataSet as p where p.name=:name and p.owner=:owner";
			Map<String, Object> parameters 	= 	new HashMap<String, Object>();

			parameters.put("name", dataPeru);
			parameters.put("owner", user.getId());

			DataSet dataSet 				= 	FacadeFactory.getFacade().find(query, parameters);

			if(dataSet != null) {
				/*
				 * if this is a dataSet
				 */

				byte[] dataByte 			= 	dataSet.getData();
				DSMicroarraySet maSet 		= 	(DSMicroarraySet) toObject(dataByte);

				if(maSet.getAnnotationFileName() != null){

					File annotFile = new File((System.getProperty("user.home") + "/temp/HG_U95Av2.na32.annot.csv"));
					AnnotationParser.loadAnnotationFile(maSet, annotFile);

				}

				markerTable.setContainerDataSource(markerTableView(maSet));
				arrayTable.setContainerDataSource(arrayTableView(maSet));

				if(action == ACTION_ANALYZE) {
					UVisualPlugin tabSheet = new UVisualPlugin(maSet, dataSet.getType(), "Analyze Data");
					UMainLayout.setMainPanelSecondComponent(tabSheet);
				}else {
					UVisualPlugin tabSheet = new UVisualPlugin(maSet, dataSet.getType(), "Get Interactions");
					UMainLayout.setMainPanelSecondComponent(tabSheet);
				}

			}else {

				getApplication().getMainWindow().showNotification("Please select dataSet node or subset node for analysis",  
						Notification.TYPE_ERROR_MESSAGE );

			}

		}else if(action == ACTION_NORMALIZE) {

			getApplication().getMainWindow().showNotification("No normalizers are implemented yet !!",  
					Notification.TYPE_ERROR_MESSAGE );

		}else if(action == ACTION_FILTER) {

			getApplication().getMainWindow().showNotification("No filters are implemented yet !!",  
					Notification.TYPE_ERROR_MESSAGE );

		}

	}

	public static HierarchicalContainer getDataContainer() {

		HierarchicalContainer dataSets 		= 	new HierarchicalContainer();
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("owner", userId);
		String whereClause = "p.owner = :owner";
		List<?> data = FacadeFactory.getFacade().getFieldValues(DataSet.class, "name", whereClause, parameters);

		for(int i=0; i<data.size(); i++) {

			String id = (String) data.get(i);
			dataSets.addItem(id);

			Map<String, Object> params 	= 	new HashMap<String, Object>();
			params.put("owner", userId);
			params.put("parent", id);
			String wClause = "p.owner = :owner and p.parent = :parent" ;
			List<?> results = FacadeFactory.getFacade().getFieldValues(ResultSet.class, "name", wClause, params);

			for(int j=0; j<results.size(); j++) {

				String subId = (String) results.get(j);
				dataSets.addItem(subId);
				dataSets.setChildrenAllowed(subId, false);
				dataSets.setParent(subId, id);

			}

		}

		return dataSets;

	}

	public static void resetDataContainer() {

		dataTree.removeAllItems();
		dataTree.setContainerDataSource(getDataContainer());

	}

}