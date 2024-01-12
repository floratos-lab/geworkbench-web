package org.geworkbenchweb.layout;

import com.vaadin.data.Property;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class ULinkOutWindow extends Window {

	private static final long serialVersionUID = 1L;

	private String gene;

	public ULinkOutWindow(String geneName) {

		this.gene = geneName;

		setCaption("LinkOut Marker");
		setModal(true);
		setClosable(true);
		setHeight("200px");
		setWidth("300px");
		setDraggable(false);
		// setScrollable(false);
		this.setResizable(false);

		ComboBox dataBase = new ComboBox();
		dataBase.setCaption("Select Database");
		dataBase.setImmediate(true);
		dataBase.addItem("NCBI - Gene");
		dataBase.addItem("NCBI - Homologene");
		dataBase.setInputPrompt("Select one");
		dataBase.setNullSelectionAllowed(false);

		final VerticalLayout lay = new VerticalLayout();
		lay.setImmediate(true);
		lay.setStyleName(Reindeer.LAYOUT_WHITE);

		dataBase.addValueChangeListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				lay.removeAllComponents();

				if (valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("NCBI - Gene")) {

					String URI = "http://www.ncbi.nlm.nih.gov/gene?term=" + gene;
					Link l = new Link("Submit",
							new ExternalResource(URI));

					l.setImmediate(true);
					l.setTargetName("_blank");
					lay.addComponent(l);

				} else {

					String URI = "http://www.ncbi.nlm.nih.gov/homologene?term=" + gene;
					Link l = new Link("Submit",
							new ExternalResource(URI));

					l.setImmediate(true);
					l.setTargetName("_blank");
					lay.addComponent(l);

				}

			}
		});

		VerticalLayout layout = new VerticalLayout();
		setContent(layout);
		layout.addComponent(dataBase);
		layout.addComponent(lay);

	}
}
