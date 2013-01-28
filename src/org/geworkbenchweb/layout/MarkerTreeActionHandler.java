package org.geworkbenchweb.layout;

import java.util.ArrayList;
import java.util.List;

 
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory; 
import com.vaadin.event.Action; 
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractOrderedLayout; 
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

 


public class MarkerTreeActionHandler extends  TreeActionHandler {
  
	private static final long serialVersionUID = -4045595188160846115L;
	private Tree markerSetTree;
	
	public MarkerTreeActionHandler(Long dataSetId, Tree markerSetTree)
	{
		super(dataSetId);
		this.markerSetTree = markerSetTree;
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
		((AbstractOrderedLayout) nameWindow.getLayout()).setSpacing(true);
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
						
						String mark 	= 	sender.toString();
						final String[] temp 	= 	(mark.substring(1, mark.length()-1)).split(",");
						List<?> sets = SubSetOperations.getMarkerSets(dataSetId);;
						for (Object set : sets){
							final SubSet markerset = (SubSet)set;
							String name = markerset.getName();
							if (name.equals(setName.getValue())){
								final String name1 = name;
								MessageBox mb = new MessageBox(sender.getApplication().getMainWindow(), 
										"Warning", 
										MessageBox.Icon.INFO, 
										"There is a Marker Subset with the name \"" +
										name +
										"\". Click \"Ok\" to add markers to same set." +
										" or Click \"Cancel\" to add set with different name",   
										new MessageBox.ButtonConfig(ButtonType.OK, "Ok"),
										new MessageBox.ButtonConfig(ButtonType.CANCEL, "Cancel"));
								mb.show(new MessageBox.EventListener() {
									private static final long serialVersionUID = 1L;

									@Override
									public void buttonClicked(ButtonType buttonType) {
										if(buttonType.equals(ButtonType.CANCEL)) {
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
								});
								sender.getApplication().getMainWindow().removeWindow(nameWindow);
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
						Long subSetId = SubSetOperations.storeMarkerSet(markers, subSetName , dataSetId);
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
						sender.getApplication().getMainWindow().removeWindow(nameWindow);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		submit.setClickShortcut(KeyCode.ENTER);
		nameWindow.addComponent(setName);
		nameWindow.addComponent(submit);
		sender.getApplication().getMainWindow().addWindow(nameWindow);
	}
	
	
	
	 

}
