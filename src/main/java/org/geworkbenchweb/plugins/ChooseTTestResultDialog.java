/**
 * 
 */
package org.geworkbenchweb.plugins;

import java.util.List;

import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.TTestResult;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * @author zji
 *
 */
public class ChooseTTestResultDialog extends Window {

	private static final long serialVersionUID = -4712749272144439069L;

	private NetworkViewer networkViewer;
	final private ListSelect tTestResultSelect = new ListSelect("t-test result node:");

	public void display(NetworkViewer networkViewer) {
		this.networkViewer = networkViewer;
		Window mainWindow = networkViewer.getApplication().getMainWindow();
		if (mainWindow == null) {
			MessageBox mb = new MessageBox(getWindow(), "No main window", MessageBox.Icon.ERROR,
					"Unexpected case of no main window.", new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();
			return;
		}
		mainWindow.addWindow(this);
		
		UMainLayout uMainLayout = (UMainLayout)mainWindow.getContent();
		List<ResultSet> resultSets = uMainLayout.getTtestResult();
		tTestResultSelect.removeAllItems();
		for(ResultSet r : resultSets) {
			tTestResultSelect.addItem(r);
			tTestResultSelect.setItemCaption(r, r.getName());
		}
	}

	public ChooseTTestResultDialog() {

		this.setModal(true);
		this.setClosable(true);
		((AbstractOrderedLayout) this.getContent()).setSpacing(true);
		this.setWidth("250px");
		this.setHeight("200px");
		this.setResizable(true);
		this.setCaption("Select t-test result");
		this.setImmediate(true);
		
		tTestResultSelect.setImmediate(true);
		tTestResultSelect.setNullSelectionAllowed(false);
		tTestResultSelect.setRows(5);
		tTestResultSelect.setWidth("100%");

		Button submit = new Button("Display", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				Window mainWindow = ChooseTTestResultDialog.this.getApplication().getMainWindow();
				mainWindow.removeWindow(ChooseTTestResultDialog.this);
				Object choice = tTestResultSelect.getValue();
				if (networkViewer != null && choice != null) {
					ResultSet resultSet = (ResultSet) choice;
					Long dataId = resultSet.getDataId();
					TTestResult tTestResult = FacadeFactory.getFacade().find(org.geworkbenchweb.pojos.TTestResult.class,
							dataId);
					Long parentId = resultSet
							.getParent(); /* this must be microarray dataset */
					DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, parentId);
					Long id = dataset.getDataId();
					MicroarrayDataset microarray = FacadeFactory.getFacade().find(MicroarrayDataset.class, id);
					String[] markerLabels = microarray.getMarkerLabels();
					List<String> colorMap = NetworkColorUtil.getTTestResultSetColorMap(tTestResult,
							markerLabels);
					networkViewer.displayWithTTestResult(colorMap);
				}
			}
		});
		submit.setClickShortcut(KeyCode.ENTER);
		this.addComponent(tTestResultSelect);
		this.addComponent(submit);
	}
};
