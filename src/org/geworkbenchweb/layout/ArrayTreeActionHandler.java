package org.geworkbenchweb.layout;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory; 
import com.vaadin.event.Action; 
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractOrderedLayout; 
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

 


public class ArrayTreeActionHandler extends  TreeActionHandler {
   
	private static final long serialVersionUID = 3110032354356936881L;
	
	private static Log log = LogFactory.getLog(ArrayTreeActionHandler.class);

	private Tree arraySetTree;
	private ComboBox contextSelector;
	
	public ArrayTreeActionHandler(Long dataSetId, Tree arraySetTree, ComboBox contextSelector)
	{
		super(dataSetId);
		this.arraySetTree = arraySetTree;
		this.contextSelector = contextSelector;
	}
	
 

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == ACTION_ADD_SET)
			addPhenoSetAction((Tree)sender);
		else if (action == ACTION_FILTER)
		{
			filterAction((Tree)sender);
		}	
		else if (action == ACTION_REMOVE_FILTER)
			removeFilterAction((Tree)sender);
		
	}	 
	
	@SuppressWarnings("deprecation")
	private void addPhenoSetAction(final Tree sender)
	{
		final Window nameWindow = new Window();
		nameWindow.setModal(true);
		nameWindow.setClosable(true);
		((AbstractOrderedLayout) nameWindow.getLayout()).setSpacing(true);
		nameWindow.setWidth("300px");
		nameWindow.setHeight("150px");
		nameWindow.setResizable(false);
		nameWindow.setCaption("Add Phenotypes to Set");
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
						List<SubSet> arraysets = SubSetOperations.getArraySetsForContext(context);
						for (final SubSet arrayset : arraysets){
							String name = arrayset.getName();
							if (name.equals(setName.getValue())){
								final String name1 = name;
								MessageBox mb = new MessageBox(sender.getApplication().getMainWindow(), 
										"Warning", 
										MessageBox.Icon.INFO, 
										"There is a Phenotype Subset with the name \"" +
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
											ArrayList<String> arrays 		= 	arrayset.getPositions();
											ArrayList<String> newarrays 	= 	new ArrayList<String>();
											for(int i=0; i<temp.length; i++) {
												String array = (String) sender.getItem(Integer.parseInt(temp[i].trim())).getItemProperty("Labels").getValue();
												if (!arrays.contains(array)) {
													arrays.add(array);
													newarrays.add(array);
												}
											}
											if (newarrays.size()>0) {
												arrayset.setPositions(arrays);
												FacadeFactory.getFacade().store(arrayset);
												arraySetTree.getContainerProperty(arrayset.getId(), "setName").setValue(name1 + " [" + arrays.size() + "]");
												for(int j=0; j<newarrays.size(); j++) {
													arraySetTree.addItem(newarrays.get(j)+arrayset.getId());
													arraySetTree.getContainerProperty(newarrays.get(j)+arrayset.getId(), "setName").setValue(newarrays.get(j));
													arraySetTree.setParent(newarrays.get(j)+arrayset.getId(), arrayset.getId());
													arraySetTree.setChildrenAllowed(newarrays.get(j)+arrayset.getId(), false);
												}
											}
										}
									}
								});
								sender.getApplication().getMainWindow().removeWindow(nameWindow);
								return;
							}
						}
						ArrayList<String> arrays = new ArrayList<String>();
						for(int i=0; i<temp.length; i++) {
							arrays.add((String) sender.getItem(Integer.parseInt(temp[i].trim())).getItemProperty("Labels").getValue());
						}
						String subSetName =  (String) setName.getValue();
						Long subSetId = SubSetOperations.storeArraySetInContext(arrays, subSetName, dataSetId, context.getId());
						arraySetTree.addItem(subSetId);
						arraySetTree.getContainerProperty(subSetId, "setName").setValue(subSetName + " [" + arrays.size() + "]");
						arraySetTree.setParent(subSetId, "arraySets");
						arraySetTree.setChildrenAllowed(subSetId, true);
						for(int j=0; j<arrays.size(); j++) {
							arraySetTree.addItem(arrays.get(j)+subSetId);
							arraySetTree.getContainerProperty(arrays.get(j)+subSetId, "setName").setValue(arrays.get(j));
							arraySetTree.setParent(arrays.get(j)+subSetId, subSetId);
							arraySetTree.setChildrenAllowed(arrays.get(j)+subSetId, false);
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
