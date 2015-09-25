package org.geworkbenchweb.plugins.cnkb;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class DirectGeneEntry extends VerticalLayout {

	private static final long serialVersionUID = 3292207899128673301L;

	final ListSelect geneEntry = new ListSelect("Direct Gene Entry");

	@Override
	public void attach() {
		geneEntry.setMultiSelect(true);
		geneEntry.setRows(4);
		geneEntry.setColumns(15);
		geneEntry.setImmediate(true);

		setSpacing(true);
		addComponent(geneEntry);
		
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		buttons.addComponent(createAddGeneButton());
		buttons.addComponent(new Button("Delete Gene"));
		buttons.addComponent(new Button("Clear List"));
		buttons.addComponent(new Button("Load Genes from File"));
		addComponent(buttons);
	}

	public String[] getItemAsArray() {
		return getItemAsArray(geneEntry);
	}

	private static String[] getItemAsArray(ListSelect listSelect) {
		String[] a = null;
		String selectStr = listSelect.getValue().toString();
		if (!selectStr.equals("[]")) {
			a = selectStr.substring(1, selectStr.length() - 1).split(",");
		}
		return a;
	}

	private Button createAddGeneButton() {
		final String title = "Add Gene";
		Button b = new Button(title);
		b.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -4929628014095090196L;

			@Override
			public void buttonClick(ClickEvent event) {

				final Window geneDialog = new Window();
				geneDialog.setModal(true);
				geneDialog.setClosable(true);
				((AbstractOrderedLayout) geneDialog.getContent()).setSpacing(true);
				geneDialog.setWidth("300px");
				geneDialog.setHeight("150px");
				geneDialog.setResizable(false);
				geneDialog.setCaption(title);
				geneDialog.setImmediate(true);

				final TextField geneSymbol = new TextField();
				geneSymbol.setInputPrompt("Please enter gene symbol");
				geneSymbol.setImmediate(true);

				Button submit = new Button(title, new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						geneEntry.addItem(geneSymbol.getValue().toString());
					}
				});
				submit.setClickShortcut(KeyCode.ENTER);
				geneDialog.addComponent(geneSymbol);
				geneDialog.addComponent(submit);
				Window mainWindow = DirectGeneEntry.this.getApplication().getMainWindow();
				mainWindow.addWindow(geneDialog);
			}
		});
		return b;
	}
}
