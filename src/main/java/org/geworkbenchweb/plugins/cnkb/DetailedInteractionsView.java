/**
 * 
 */
package org.geworkbenchweb.plugins.cnkb;

import java.util.Arrays;
import java.util.List;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * @author zji
 *
 */
public class DetailedInteractionsView extends Window {

	private static final long serialVersionUID = -4712749272144439069L;

	private CNKBResultsUI parent;

	public void display(CNKBResultsUI parent) {
		this.parent = parent;
		this.setWidth("50%");;
		this.setHeight("50%");;
		Window mainWindow = parent.getApplication().getMainWindow();
		if (mainWindow == null) {
			MessageBox mb = new MessageBox(getWindow(), "No main window", MessageBox.Icon.ERROR,
					"Unexpected case of no main window.", new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();
			return;
		}
		mainWindow.addWindow(this);
	}

	private static final String TABLE_VIEW = "Table View";
	private static final String COLOR_MOSAIC_VIEW = "Color Mosaic View";
			
	public DetailedInteractionsView() {

		this.setModal(true);
		this.setClosable(true);
		((AbstractOrderedLayout) this.getContent()).setSpacing(true);
		this.setWidth("250px");
		this.setHeight("200px");
		this.setResizable(false);
		this.setCaption("Interaction Details");
		this.setImmediate(true);

		this.addComponent(new Label("Query Gene Symbol: [to be implemented]"));
		this.addComponent(new Label("Description: [to be implemented]"));
		this.addComponent(new Label("Entrez Gene: linkout   Gene Cards: linkout"));

		List<String> views = Arrays.asList(new String[] { TABLE_VIEW, COLOR_MOSAIC_VIEW });

		OptionGroup viewSelect = new OptionGroup("Views", views);
		viewSelect.setImmediate(true);
		this.addComponent(viewSelect);
		final Component tableview = new InteractionDetailTableView();
		final Component colormosaicview = new Label("Color Mosaic View to be implemented");
		final VerticalLayout detailedView = new VerticalLayout();
		this.addComponent(detailedView);
		
		detailedView.addComponent(tableview);
		viewSelect.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 3816899447811925504L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Property property = event.getProperty();
				Object value = property.getValue();
				if(value.equals(TABLE_VIEW)) {
					detailedView.removeAllComponents();
					detailedView.addComponent(tableview);
				} else if(value.equals(COLOR_MOSAIC_VIEW)) {
					detailedView.removeAllComponents();
					detailedView.addComponent(colormosaicview);
				}
			}
			
		});
	}
};
