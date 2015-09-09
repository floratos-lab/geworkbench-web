package org.geworkbenchweb.plugins.cnkb;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.UnAuthenticatedException;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * Parameter panel for 'standalone' CNKB
 * 
 */
public class CNKB2 extends VerticalLayout {

	private static final long serialVersionUID = 6992822172488124700L;

	private static final String CNKB_SERVLET_URL = "http://cagridnode.c2b2.columbia.edu:8080/cknb/InteractionsServlet_new/InteractionsServlet";
	private static final int TIMEOUT = 3000;

	private List<VersionDescriptor> versionList = new ArrayList<VersionDescriptor>();

	/*
	 * Initialization in this method instead of constructors is done in the main
	 * GUI thread instead of a background thread.
	 */
	@Override
	public void attach() {

		super.attach();

		/* Create a connection with the server. */
		ResultSetlUtil.setUrl(CNKB_SERVLET_URL);
		ResultSetlUtil.setTimeout(TIMEOUT);

		final CNKB interactionsConnection = new CNKB();

		List<String> interactomeList = null;
		try {
			interactomeList = interactionsConnection.getDatasetAndInteractioCount();
		} catch (UnAuthenticatedException uae) {
			uae.printStackTrace();
		} catch (ConnectException e1) {
			e1.printStackTrace();
		} catch (SocketTimeoutException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		final ListSelect geneEntry = new ListSelect("Direct Gene Entry");
		geneEntry.setMultiSelect(true);
		geneEntry.setRows(4);
		geneEntry.setColumns(15);
		geneEntry.setImmediate(true);

		final ListSelect interactomeBox;
		final ListSelect versionBox;
		final Label interactomeDes = new Label();
		final Label versionDes = new Label();

		interactomeBox = new ListSelect("Select Interactome:");
		interactomeBox.setRows(4);
		interactomeBox.setColumns(25);
		interactomeBox.setImmediate(true);
		interactomeBox.setNullSelectionAllowed(false);
		for (int j = 0; j < interactomeList.size(); j++) {
			interactomeBox.addItem(interactomeList.get(j));
		}

		versionBox = new ListSelect("Select Version:");

		versionBox.setRows(4);
		versionBox.setColumns(15);
		versionBox.setImmediate(true);
		versionBox.setNullSelectionAllowed(false);
		versionBox.setEnabled(false);
		interactomeBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {
					interactomeDes.setValue(interactionsConnection.getInteractomeDescription(
							valueChangeEvent.getProperty().getValue().toString().split(" \\(")[0].trim()));
					versionBox.setEnabled(true);
					versionBox.removeAllItems();
					versionList = interactionsConnection.getVersionDescriptor(
							valueChangeEvent.getProperty().getValue().toString().split(" \\(")[0].trim());
					for (int k = 0; k < versionList.size(); k++) {
						versionBox.addItem(versionList.get(k).getVersion());
					}

				} catch (ConnectException e) {
					e.printStackTrace();
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (UnAuthenticatedException uae) {
					uae.printStackTrace();
				}
			}
		});

		versionBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				versionDes.setValue("");
				Object version = valueChangeEvent.getProperty().getValue();
				if (version == null || version.toString().trim().length() == 0)
					return;
				String versionValue = version.toString().trim();

				for (VersionDescriptor vd : versionList) {
					if (vd.getVersion().trim().equals(versionValue)) {
						if (vd.getVersionDesc() != null && vd.getVersionDesc().trim().length() > 0
								&& !vd.getVersionDesc().trim().equals("null"))
							versionDes.setValue(vd.getVersionDesc());
						else
							versionDes.setValue("NO DESCRIPTION");
						break;
					}
				}
			}
		});

		final Button submitButton = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				MessageBox mb = new MessageBox(getWindow(), "Under development", MessageBox.Icon.INFO,
						"This feature is not available yet.", new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
			}
		});

		interactomeDes.setStyleName(Reindeer.LABEL_SMALL);
		interactomeDes.setImmediate(true);

		versionDes.setStyleName(Reindeer.LABEL_SMALL);
		versionDes.setImmediate(true);

		setSpacing(true);
		addComponent(geneEntry);
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		buttons.addComponent(new Button("Add Gene"));
		buttons.addComponent(new Button("Delete Gene"));
		buttons.addComponent(new Button("Clear List"));
		buttons.addComponent(new Button("Load Genes from File"));
		addComponent(buttons);
		addComponent(interactomeBox);
		addComponent(interactomeDes);
		addComponent(versionBox);
		addComponent(versionDes);
		addComponent(submitButton);
	}
}
