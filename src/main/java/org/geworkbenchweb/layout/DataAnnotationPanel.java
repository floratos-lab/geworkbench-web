/**
 * 
 */
package org.geworkbenchweb.layout;

import org.vaadin.alump.fancylayouts.FancyCssLayout;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

/**
 * @author zji
 * @version $Id$
 *
 */
/* ATTENTION: please be aware of our over-use of the word 'annotation'!
 * 'annotation' here refers to the additional information of the dataset, e.g. user comment, history, experimental info, etc.,
 * NOT the annotation for the microaaray dataset, e.g. the AffyMatrix annotation*/
public class DataAnnotationPanel extends FancyCssLayout {

	private static final long serialVersionUID = 825425207503421000L;

	final private MenuItem upArrow, downArrow;
	final MenuBar menuBar = new MenuBar();
	
	private Long datasetId;
	
	DataAnnotationPanel() {
		
		setSlideEnabled(true);
		
		//setMargin(true);
		setHeight("250px");
		setWidth("100%");
		setImmediate(true);
		
		setVisible(false);
		
		menuBar.setWidth("100%");
		upArrow = menuBar.addItem("", new ThemeResource(
				"../runo/icons/16/arrow-up.png"), new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				expand();
			}
		});
		upArrow.setDescription("View Annotation");
		downArrow = menuBar.addItem("", new ThemeResource(
				"../runo/icons/16/arrow-down.png"), new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				collapse();
			}
		});
		downArrow.setDescription("Close Annotation");
		downArrow.setVisible(false);

	}
	
	void setDatasetId(Long datasetId) { // datasetId should not be null
		if(datasetId!=null && datasetId!=this.datasetId) {
			this.removeAllComponents();
			this.addComponent(new AnnotationTabSheet(datasetId));
			this.datasetId = datasetId;
			this.collapse();
		}
	}

	private void collapse() {
		menuBar.setVisible(true);
		downArrow.setVisible(false);
		upArrow.setVisible(true);
		
		this.setVisible(false);
	}
	
	void expand() {
		menuBar.setVisible(true);
		downArrow.setVisible(true);
		upArrow.setVisible(false);
		
		this.setVisible(true);
	}
	
	void hide() {
		menuBar.setVisible(false);
		this.setVisible(false);
	}
}
