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
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.steinwedel.messagebox.MessageBox;

public class ArrayTreeActionHandler extends TreeActionHandler {

	private static final long serialVersionUID = 3110032354356936881L;

	private static Log log = LogFactory.getLog(ArrayTreeActionHandler.class);

	private Tree arraySetTree;
	private ComboBox contextSelector;

	public ArrayTreeActionHandler(Long dataSetId, Tree arraySetTree, ComboBox contextSelector) {
		super(dataSetId);
		this.arraySetTree = arraySetTree;
		this.contextSelector = contextSelector;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == ACTION_ADD_SET)
			addPhenoSetAction((Tree) sender);
		else if (action == ACTION_FILTER) {
			filterAction((Tree) sender);
		} else if (action == ACTION_REMOVE_FILTER)
			removeFilterAction((Tree) sender);

	}

	@SuppressWarnings("deprecation")
	private void addPhenoSetAction(final Tree sender) {
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

		Button submit = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					if (setName.getValue() != null) {
						Object contextObj = contextSelector.getValue();
						if (contextObj == null) {
							log.warn("Can't create arrayset: current context is null");
							return;
						}
						Context context = (Context) contextObj;
						String mark = sender.toString();
						final String[] temp = (mark.substring(1, mark.length() - 1)).split(",");
						List<SubSet> arraysets = SubSetOperations.getSubSetsForContext(context);
						for (final SubSet arrayset : arraysets) {
							String name = arrayset.getName();
							if (name.equals(setName.getValue())) {
								final String name1 = name;
								MessageBox.createInfo().withCaption("Warning")
										.withMessage("There is a Phenotype Subset with the name \"" +
												name +
												"\". Click \"Ok\" to add markers to same set." +
												" or Click \"Cancel\" to add set with different name")
										.withOkButton(() -> {
											List<String> arrays = arrayset.getPositions();
											ArrayList<String> newarrays = new ArrayList<String>();
											for (int i = 0; i < temp.length; i++) {
												String array = (String) sender.getItem(Integer.parseInt(temp[i].trim()))
														.getItemProperty("Labels").getValue();
												if (!arrays.contains(array)) {
													arrays.add(array);
													newarrays.add(array);
												}
											}
											if (newarrays.size() > 0) {
												arrayset.setPositions(arrays);
												FacadeFactory.getFacade().store(arrayset);
												arraySetTree.getContainerProperty(arrayset.getId(),
														SetViewLayout.SUBSET_NAME).setValue(name1);
												arraySetTree
														.getContainerProperty(arrayset.getId(),
																SetViewLayout.SET_DISPLAY_NAME)
														.setValue(name1 + " [" + arrays.size() + "]");
												for (int j = 0; j < newarrays.size(); j++) {
													arraySetTree.addItem(newarrays.get(j) + arrayset.getId());
													arraySetTree
															.getContainerProperty(newarrays.get(j) + arrayset.getId(),
																	SetViewLayout.SET_DISPLAY_NAME)
															.setValue(newarrays.get(j));
													arraySetTree.setParent(newarrays.get(j) + arrayset.getId(),
															arrayset.getId());
													arraySetTree.setChildrenAllowed(newarrays.get(j) + arrayset.getId(),
															false);
												}
											}
										}).withCancelButton().open();
								UI.getCurrent().removeWindow(nameWindow);
								return;
							}
						}
						ArrayList<String> arrays = new ArrayList<String>();
						for (int i = 0; i < temp.length; i++) {
							arrays.add((String) sender.getItem(Integer.parseInt(temp[i].trim()))
									.getItemProperty("Labels").getValue());
						}
						String subSetName = (String) setName.getValue();
						Long subSetId = SubSetOperations.storeArraySetInContext(arrays, subSetName, dataSetId, context);
						arraySetTree.addItem(subSetId);
						arraySetTree.getContainerProperty(subSetId, SetViewLayout.SUBSET_NAME).setValue(subSetName);
						arraySetTree.getContainerProperty(subSetId, SetViewLayout.SET_DISPLAY_NAME)
								.setValue(subSetName + " [" + arrays.size() + "]");
						arraySetTree.setParent(subSetId, "arraySets");
						arraySetTree.setChildrenAllowed(subSetId, true);
						for (int j = 0; j < arrays.size(); j++) {
							arraySetTree.addItem(arrays.get(j) + subSetId);
							arraySetTree.getContainerProperty(arrays.get(j) + subSetId, SetViewLayout.SET_DISPLAY_NAME)
									.setValue(arrays.get(j));
							arraySetTree.setParent(arrays.get(j) + subSetId, subSetId);
							arraySetTree.setChildrenAllowed(arrays.get(j) + subSetId, false);
						}
						UI.getCurrent().removeWindow(nameWindow);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		submit.setClickShortcut(KeyCode.ENTER);
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(setName);
		layout.addComponent(submit);
		nameWindow.setContent(layout);
		UI.getCurrent().addWindow(nameWindow);
	}
}
