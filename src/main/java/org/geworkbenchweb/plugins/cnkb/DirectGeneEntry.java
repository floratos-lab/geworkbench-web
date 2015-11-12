package org.geworkbenchweb.plugins.cnkb;

import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;
import org.vaadin.easyuploads.UploadField.StorageMode;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

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
		buttons.addComponent(createDeleteGeneButton());
		buttons.addComponent(createClearListButton());
		buttons.addComponent(createLoadFileButton());
		addComponent(buttons);
	}

	public String[] getItemAsArray() {
		return geneEntry.getItemIds().toArray(new String[0]);
	}

	private static String[] getItemAsArray(ListSelect listSelect) {
		String[] a = new String[0];
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

				final Window mainWindow = DirectGeneEntry.this.getApplication().getMainWindow();
				Button submit = new Button(title, new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						mainWindow.removeWindow(geneDialog);
						String[] genes = geneSymbol.getValue().toString().split(",");
						for (String g : genes) {
							geneEntry.addItem(g.trim());
						}
					}
				});
				submit.setClickShortcut(KeyCode.ENTER);
				geneDialog.addComponent(geneSymbol);
				geneDialog.addComponent(submit);
				mainWindow.addWindow(geneDialog);
			}
		});
		return b;
	}

	private Button createDeleteGeneButton() {
		final String title = "Delete Gene";
		Button b = new Button(title);
		b.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -4929628014095090196L;

			@Override
			public void buttonClick(ClickEvent event) {
				String[] genes = getItemAsArray(geneEntry);
				for (String g : genes) {
					geneEntry.removeItem(g.trim());
				}
			}
		});
		return b;
	}

	private Button createClearListButton() {
		final String title = "Clear List";
		Button b = new Button(title);
		b.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -4929628014095090196L;

			@Override
			public void buttonClick(ClickEvent event) {
				geneEntry.removeAllItems();
			}
		});
		return b;
	}

	private UploadField createLoadFileButton() {
		UploadField b = new UploadField(StorageMode.MEMORY) {
			private static final long serialVersionUID = -212174451849906591L;

			@Override
			protected void updateDisplay() {
				String value = (String) this.getValue();
				String[] genes = value.split("\\s");
				for (String g : genes) {
					if(g.length()>0) {
						geneEntry.addItem(g);
					}
				}
			}
		};
		b.setButtonCaption("Load Genes from File");
		b.setImmediate(true);
		b.setFieldType(FieldType.UTF8_STRING);

		return b;
	}
}
