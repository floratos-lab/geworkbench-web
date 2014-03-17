/**
 * 
 */
package org.geworkbenchweb.layout;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.DataTypeMenuPage;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Tree;

/**
 * 
 * Main navigation tree of the workspace.
 * 
 * @author zji
 * 
 */
public class NavigationTree extends Tree {

	private static Log log = LogFactory.getLog(NavigationTree.class);

	private static final long serialVersionUID = -1721033415879095081L;

	private Long dataSetId;

	/* this is only for the functionality of set view */
	private Long microarraySetId = null;

	Long geDataSetId() {
		return dataSetId;
	}

	Long getMicroarraySetId() {
		return microarraySetId;
	}

	NavigationTree(final Button annotButton, final Button removeButton,
			final DataAnnotationPanel annotationPanel, final MenuBar toolBar,
			final MenuItem setViewMeuItem, final VisualPluginView pluginView,
			final Long userId, final ThemeResource pendingIcon) {

		this.setImmediate(true);
		this.setStyleName("menu");
		this.setContainerDataSource(getDataContainer(userId, pendingIcon));
		this.setSelectable(true);
		this.setMultiSelect(false);
		this.addListener(new Tree.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(
					com.vaadin.data.Property.ValueChangeEvent event) {

				Object itemId = event.getProperty().getValue();
				if(itemId==null && dataSetId!=null) {
					// being selected before unselecting, force it selected. mantis issue 3702					 
					ResultSet resultSet =  FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
				    DataSet dataSet =  FacadeFactory.getFacade().find(DataSet.class, dataSetId);					
					if (resultSet != null || dataSet != null)
					    NavigationTree.this.select(dataSetId);
					return;
				}
				if (!(itemId instanceof Long)) {
					log.error("wrong type for dataSetId " + itemId);
					return;
				}

				annotButton.setEnabled(true);
				removeButton.setEnabled(true);
				Item selectedItem = NavigationTree.this.getItem(itemId);
				String className = (String) selectedItem
						.getItemProperty("Type").getValue();
				Object parentId = NavigationTree.this.getParent(itemId);
				String parentItemClassName = null;
				Item parentItem = NavigationTree.this.getItem(parentId);
				if (parentItem != null) {
					parentItemClassName = (String) parentItem.getItemProperty(
							"Type").getValue();
				}

				/* this is the only place that dataset ID may change */
				dataSetId = (Long) itemId;
				annotationPanel.setDatasetId(dataSetId);

				final String specialClassName = "org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet";
				if (specialClassName.equals(className)) {
					microarraySetId = dataSetId;
					toolBar.setEnabled(true);
					setViewMeuItem.setEnabled(true);
				} else if (specialClassName.equals(parentItemClassName)) {
					microarraySetId = (Long) parentId;
					toolBar.setEnabled(true);
					setViewMeuItem.setEnabled(true);
				} else {
					// leave microarraySetId unchanged intentionally
					toolBar.setEnabled(false);
				}

				ClassLoader classLoader = this.getClass().getClassLoader();
				boolean success = false;
				try {
					Class<?> aClass = classLoader.loadClass(className);
					Class<? extends DataTypeMenuPage> uiComponentClass = GeworkbenchRoot
							.getPluginRegistry().getDataUI(aClass);
					Class<? extends Component> resultUiClass = GeworkbenchRoot
							.getPluginRegistry().getResultUI(aClass);
					if (uiComponentClass != null) {
						/*
						 * "not result" - menu page. For now, we only expect
						 * CSMcrioarraySet and CSProteinStructure
						 */
						DataTypeMenuPage dataUI = uiComponentClass
								.getDeclaredConstructor(Long.class)
								.newInstance(dataSetId);
						dataUI.setVisualPluginView(pluginView);
						pluginView.setContent(dataUI, dataUI.getTitle(),
								dataUI.getDescription());
					} else if (resultUiClass != null) {
						/*
						 * "is result" - visualizer
						 */
						pluginView.setContentUsingCache(resultUiClass,
								dataSetId);
					}
					success = true;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
				if (!success) {
					pluginView.showToolList();
					annotationPanel.hide();
				}
			}
		});

		this.setItemDescriptionGenerator(new ItemDescriptionGenerator() {

			private static final long serialVersionUID = -3576690826530527342L;

			@Override
			public String generateDescription(Component source, Object itemId,
					Object propertyId) {
				Item item = NavigationTree.this.getItem(itemId);
				return (String) item.getItemProperty("description").getValue();
			}
		});

		new RenameHandler(this); /* returned value can be ignored */
	}

	/**
	 * Supplies the container for the dataset and result tree to display.
	 * 
	 * @param pendingIcon
	 * 
	 * @return dataset and resultset container
	 */
	private HierarchicalContainer getDataContainer(final Long userId,
			final ThemeResource pendingIcon) {

		HierarchicalContainer dataSets = new HierarchicalContainer();
		dataSets.addContainerProperty("Name", String.class, null);
		dataSets.addContainerProperty("Type", String.class, null);
		dataSets.addContainerProperty("Icon", Resource.class, null);
		dataSets.addContainerProperty("description", String.class, "");

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("owner", userId);
		param.put("workspace", WorkspaceUtils.getActiveWorkSpace());

		List<AbstractPojo> data = FacadeFactory
				.getFacade()
				.list("Select p from DataSet as p where p.owner=:owner and p.workspace =:workspace",
						param);

		for (int i = 0; i < data.size(); i++) {

			String id = ((DataSet) data.get(i)).getName();
			Long dataId = ((DataSet) data.get(i)).getId();
			String description = ((DataSet) data.get(i)).getDescription();

			Item subItem = dataSets.addItem(dataId);
			subItem.getItemProperty("Name").setValue(id);
			String className = ((DataSet) data.get(i)).getType();
			subItem.getItemProperty("Type").setValue(className);
			try {
				ThemeResource icon = GeworkbenchRoot.getPluginRegistry()
						.getIcon(Class.forName(className));
				subItem.getItemProperty("Icon").setValue(icon);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				// subItem.getItemProperty("Icon").setValue(icon);
			}
			subItem.getItemProperty("description").setValue(description);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("owner", userId);
			params.put("parent", dataId);
			List<AbstractPojo> results = FacadeFactory
					.getFacade()
					.list("Select p from ResultSet as p where p.owner=:owner and p.parent=:parent ORDER by p.timestamp",
							params);

			if (results.size() == 0) {
				dataSets.setChildrenAllowed(dataId, false);
			}

			for (int j = 0; j < results.size(); j++) {
				String subId = ((ResultSet) results.get(j)).getName();
				Long subSetId = ((ResultSet) results.get(j)).getId();
				String type = ((ResultSet) results.get(j)).getType();

				Item res = dataSets.addItem(subSetId);
				res.getItemProperty("Name").setValue(subId);
				res.getItemProperty("Type").setValue(type);
				if (subId.contains("Pending")) {
					res.getItemProperty("Icon").setValue(pendingIcon);
				} else {
					try {
						ThemeResource icon = GeworkbenchRoot
								.getPluginRegistry().getResultIcon(
										Class.forName(type));
						res.getItemProperty("Icon").setValue(icon);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
				dataSets.setChildrenAllowed(subSetId, false);
				dataSets.setParent(subSetId, dataId);
			}
		}
		return dataSets;
	}
}
