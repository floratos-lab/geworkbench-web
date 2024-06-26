package org.geworkbenchweb.layout;

import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.steinwedel.messagebox.MessageBox;

public class NewContextListener implements Button.ClickListener {

	private static final long serialVersionUID = -8562473729337703324L;

	final private Long dataSetId;
	final private ComboBox contextSelector;
	final private String categoryLabel; /* either "maker" or "microarray" */

	NewContextListener(final SetViewLayout setViewLayout, final Long dataSetId,
			final ComboBox contextSelector, final String categoryLabel) {
		this.dataSetId = dataSetId;
		this.contextSelector = contextSelector;
		this.categoryLabel = categoryLabel;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		final UI mainWindow = UI.getCurrent();

		final Window nameWindow = new Window();
		nameWindow.setModal(true);
		nameWindow.setClosable(true);
		nameWindow.setWidth("300px");
		nameWindow.setHeight("150px");
		nameWindow.setResizable(false);
		nameWindow.setCaption("Add New Context");
		nameWindow.setImmediate(true);

		final TextField contextName = new TextField();
		contextName.setInputPrompt("Please enter " + categoryLabel
				+ "set context name");
		contextName.setImmediate(true);
		VerticalLayout layout = new VerticalLayout();
		nameWindow.setContent(layout);
		layout.addComponent(contextName);
		layout.addComponent(new Button("Ok", new Button.ClickListener() {
			private static final long serialVersionUID = 634733324392150366L;

			public void buttonClick(ClickEvent event) {
				String name = (String) contextName.getValue();
				for (Context context : SubSetOperations
						.getMarkerContexts(dataSetId)) {
					if (context.getName().equals(name)) {
						MessageBox.createWarning().withCaption("Warning").withMessage("Name already exists")
								.withOkButton().open();
						return;
					}
				}
				Context context = new Context(name, categoryLabel, dataSetId);
				FacadeFactory.getFacade().store(context);
				mainWindow.removeWindow(nameWindow);
				contextSelector.addItem(context);
				contextSelector.setValue(context);
			}
		}));
		mainWindow.addWindow(nameWindow);
	}

}
