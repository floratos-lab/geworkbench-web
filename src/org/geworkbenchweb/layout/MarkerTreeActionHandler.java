package org.geworkbenchweb.layout;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.LayoutUtil;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.steinwedel.messagebox.ButtonId;
import de.steinwedel.messagebox.Icon;
import de.steinwedel.messagebox.MessageBox;
import de.steinwedel.messagebox.MessageBoxListener;

 


public class MarkerTreeActionHandler extends  TreeActionHandler {
  
	private static final long serialVersionUID = -4045595188160846115L;
	private static Log log = LogFactory.getLog(ArrayTreeActionHandler.class);
	private Tree markerSetTree;
	private ComboBox contextSelector;
	
	public MarkerTreeActionHandler(Long dataSetId, Tree markerSetTree, ComboBox contextSelector)
	{
		super(dataSetId);
		this.markerSetTree = markerSetTree;
		this.contextSelector = contextSelector;
	}
	
 

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == ACTION_ADD_SET)
			  addMarkerSetAction((Tree)sender);
		else if (action == ACTION_FILTER)
		{
			filterAction((Tree)sender);
		}	
		else if (action == ACTION_REMOVE_FILTER)
			removeFilterAction((Tree)sender);
		
	}
	
	 
	@SuppressWarnings("deprecation")
	private void addMarkerSetAction(final Tree sender)
	{
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

		Button submit = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					if(setName.getValue() != null) {
						
						Object contextObj = contextSelector.getValue();
						if (contextObj == null) {
							log.warn("Can't create arrayset: current context is null");
							return;
						}
						Context context = (Context)contextObj;
						String mark 	= 	sender.toString();
						final String[] temp 	= 	(mark.substring(1, mark.length()-1)).split(",");
						List<SubSet> markersets = SubSetOperations.getSubSetsForContext(context);
						for (final SubSet markerset : markersets){
							String name = markerset.getName();
							if (name.equals(setName.getValue())){
								final String name1 = name;
								MessageBox.showPlain(Icon.INFO, 
										"Warning", 
										"There is a Marker Subset with the name \"" +
										name +
										"\". Click \"Ok\" to add markers to same set." +
										" or Click \"Cancel\" to add set with different name",   
										new MessageBoxListener() {
		
											@Override
											public void buttonClicked(ButtonId buttonId) {
												if(buttonId.equals(ButtonId.CANCEL)) {
													return;
												} else {
													ArrayList<String> markers	 = 	markerset.getPositions();
													ArrayList<String> newmarkers = 	new ArrayList<String>();
													for(int i=0; i<temp.length; i++) {
														String data = (String) sender.getItem(Integer.parseInt(temp[i].trim())).getItemProperty("Labels").getValue();
														String[] dataA = data.split("\\s+\\(");
														if (!markers.contains(dataA[0])) {
															markers.add(dataA[0]);
															newmarkers.add(dataA[0]);
														}
													}
													if (newmarkers.size()>0) {
														markerset.setPositions(markers);
														FacadeFactory.getFacade().store(markerset);
														markerSetTree.getContainerProperty(markerset.getId(), "setName").setValue(name1 +" [" + markers.size() + "]");
														for(int j=0; j<newmarkers.size(); j++) {
															markerSetTree.addItem(newmarkers.get(j)+markerset.getId());
															markerSetTree.getContainerProperty(newmarkers.get(j)+markerset.getId(), "setName").setValue(newmarkers.get(j));
															markerSetTree.setParent(newmarkers.get(j)+markerset.getId(), markerset.getId());
															markerSetTree.setChildrenAllowed(newmarkers.get(j)+markerset.getId(), false);
														}
													}
												}
											}
										},
										ButtonId.OK,
										ButtonId.CANCEL);
								UI.getCurrent().removeWindow(nameWindow);
								return;
							}
						}
						ArrayList<String> markers = new ArrayList<String>();
						for(int i=0; i<temp.length; i++) {
							String data = (String) sender.getItem(Integer.parseInt(temp[i].trim())).getItemProperty("Labels").getValue();
							String[] dataA = data.split("\\s+\\(");
							markers.add(dataA[0]);
						}
						String subSetName = (String) setName.getValue();
						Long subSetId = SubSetOperations.storeMarkerSetInContext(markers, subSetName , dataSetId, context);
						markerSetTree.addItem(subSetId);
						markerSetTree.getContainerProperty(subSetId, "setName").setValue(subSetName + " [" + markers.size()+ "]");
						markerSetTree.setParent(subSetId, "MarkerSets");
						markerSetTree.setChildrenAllowed(subSetId, true);
						for(int j=0; j<markers.size(); j++) {
							markerSetTree.addItem(markers.get(j)+subSetId);
							markerSetTree.getContainerProperty(markers.get(j)+subSetId, "setName").setValue(markers.get(j));
							markerSetTree.setParent(markers.get(j)+subSetId, subSetId);
							markerSetTree.setChildrenAllowed(markers.get(j)+subSetId, false);
						}
						UI.getCurrent().removeWindow(nameWindow);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		submit.setClickShortcut(KeyCode.ENTER);
		VerticalLayout layout = LayoutUtil.addComponent(setName);
		layout.addComponent(submit);
		nameWindow.setContent(layout);
		UI.getCurrent().addWindow(nameWindow);
	}
	
	
	
	 

}
