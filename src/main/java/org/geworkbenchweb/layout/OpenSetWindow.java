package org.geworkbenchweb.layout;

import org.geworkbenchweb.utils.CSVUtil;
import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;
import org.vaadin.easyuploads.UploadField.StorageMode;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class OpenSetWindow extends Window {

	private static final long serialVersionUID = 3780041096719367174L;
	
	OpenSetWindow(final Long dataSetId, final SetViewLayout setViewLayout) {
		super("Open Set");

		this.center();
		this.setWidth("20%");
		this.setHeight("40%");

		VerticalLayout vlayout = (VerticalLayout) this.getContent();
		vlayout.setMargin(true);
		vlayout.setSpacing(true);

		final OptionGroup setGroup = new OptionGroup("Please choose a set type");
		setGroup.addItem("Marker Set");
		setGroup.addItem("Array Set");
		setGroup.setValue("Array Set");
		setGroup.setImmediate(true);
		vlayout.addComponent(setGroup);

		final OptionGroup markerGroup = new OptionGroup(
				"Markers are represented by");
		markerGroup.addItem("Marker ID");
		markerGroup.addItem("Gene Symbol");
		markerGroup.setValue("Marker ID");
		vlayout.addComponent(markerGroup);
		markerGroup.setVisible(false);

		setGroup.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 2481194620858021204L;

			public void valueChange(ValueChangeEvent event) {
				if (event.getProperty().getValue().equals("Marker Set"))
					markerGroup.setVisible(true);
				else
					markerGroup.setVisible(false);
			}
		});

		UploadField openFile = new UploadField(StorageMode.MEMORY) {
			private static final long serialVersionUID = -212174451849906591L;

			protected void updateDisplay() {
				Window pWindow = OpenSetWindow.this.getParent();
				if (pWindow != null)
					pWindow.removeWindow(OpenSetWindow.this);
				String filename = getLastFileName();
				byte[] bytes = (byte[]) getValue();

				if (filename.endsWith(".csv") || filename.endsWith(".CSV")) {
					if (setGroup.getValue().equals("Array Set")) {
						CSVUtil.loadArraySet(filename, bytes, dataSetId,
								setViewLayout.getArraySetTree());
					} else {
						CSVUtil.loadMarkerSet(filename, bytes, dataSetId,
								setViewLayout.getMarkerSetTree(), (String) markerGroup.getValue());
					}
				} else {
					MessageBox mb = new MessageBox(pWindow,
							"File Format Error", MessageBox.Icon.WARN, filename
									+ " is not a CSV file",
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
				}
			}
		};
		openFile.setButtonCaption("Open File");
		openFile.setFieldType(FieldType.BYTE_ARRAY);
		vlayout.addComponent(openFile);
	}
}
