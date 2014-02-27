package org.geworkbenchweb.plugins.tabularview;

import org.geworkbenchweb.plugins.TableMenuSelector;
import org.geworkbenchweb.plugins.Tabular;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.utils.PreferenceOperations;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

 
public class TabularMenuSelector extends TableMenuSelector {
	
	 
	private static final long serialVersionUID = -2811557010017084202L;

	public TabularMenuSelector(Tabular tabular, String name)
	{
		super(tabular, name);
	}
	
	@Override
	protected void createDisplayPreferenceItems()
	{
	 
		MenuBar.MenuItem geneOrMarkerItem = displayPreferences.addItem(
				"Gene Symbol/Marker ID", new Command() {

					private static final long serialVersionUID = 1L;

					@SuppressWarnings("deprecation")
					@Override
					public void menuSelected(MenuItem selectedItem) {
						final Window displayPrefWindow = new Window();
						displayPrefWindow.setModal(true);
						displayPrefWindow.setClosable(true);
						((AbstractOrderedLayout) displayPrefWindow.getLayout())
								.setSpacing(true);
						displayPrefWindow.setWidth("300px");
						displayPrefWindow.setHeight("200px");
						displayPrefWindow.setResizable(false);
						displayPrefWindow.setCaption("Display Preference");
						displayPrefWindow.setImmediate(true);

						final OptionGroup og;
						og = new OptionGroup();
						og.setImmediate(true);
						og.addItem(Constants.MarkerDisplayControl.marker
								.ordinal());
						og.addItem(Constants.MarkerDisplayControl.gene_symbol
								.ordinal());
						og.addItem(Constants.MarkerDisplayControl.both
								.ordinal());
						og.setItemCaption(
								Constants.MarkerDisplayControl.marker.ordinal(),
								"Marker ID");
						og.setItemCaption(
								Constants.MarkerDisplayControl.gene_symbol
										.ordinal(), "Gene Symbol");
						og.setItemCaption(
								Constants.MarkerDisplayControl.both.ordinal(),
								"Both");

						og.select(((TabularViewUI)parent).getTabViewPreferences().getMarkerDisplayControl());

						final Window mainWindow = getApplication()
								.getMainWindow();

						Button submit = new Button("Submit",
								new Button.ClickListener() {

									private static final long serialVersionUID = -4799561372701936132L;

									@Override
									public void buttonClick(ClickEvent event) {
										try {

											Object value = og.getValue();
											Preference p = PreferenceOperations
													.getData(
															Constants.MARKER_DISPLAY_CONTROL,
															parent.getUserId());
											if (p != null)
												PreferenceOperations.setValue(
														value, p);
											else
												PreferenceOperations.storeData(
														value,
														Integer.class.getName(),
														Constants.MARKER_DISPLAY_CONTROL,
														null, parent.getUserId());

											parent.resetDataSource();				 
													 
											mainWindow
													.removeWindow(displayPrefWindow);
									 
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});
						submit.setClickShortcut(KeyCode.ENTER);
						displayPrefWindow.addComponent(og);
						displayPrefWindow.addComponent(submit);
						mainWindow.addWindow(displayPrefWindow);
					}
				});

		geneOrMarkerItem.setStyleName("plugin");

		MenuBar.MenuItem annotationsItem = displayPreferences.addItem(
				"Annotations", new Command() {

					private static final long serialVersionUID = 1L;

					@SuppressWarnings("deprecation")
					@Override
					public void menuSelected(MenuItem selectedItem) {
						final Window displayPrefWindow = new Window();
						displayPrefWindow.setModal(true);
						displayPrefWindow.setClosable(true);
						((AbstractOrderedLayout) displayPrefWindow.getLayout())
								.setSpacing(true);
						displayPrefWindow.setWidth("300px");
						displayPrefWindow.setHeight("200px");
						displayPrefWindow.setResizable(false);
						displayPrefWindow.setCaption("Display Preference");
						displayPrefWindow.setImmediate(true);

						final OptionGroup og;
						og = new OptionGroup();
						og.setImmediate(true);
						og.addItem(Constants.AnnotationDisplayControl.on
								.ordinal());
						og.addItem(Constants.AnnotationDisplayControl.off
								.ordinal());
						og.setItemCaption(
								Constants.AnnotationDisplayControl.on.ordinal(),
								"On");
						og.setItemCaption(
								Constants.AnnotationDisplayControl.off
										.ordinal(), "Off");

						og.select(((TabularViewUI)parent).getTabViewPreferences()
								.getAnnotationDisplayControl());

						final Window mainWindow = getApplication()
								.getMainWindow();

						Button submit = new Button("Submit",
								new Button.ClickListener() {

									private static final long serialVersionUID = -4799561372701936132L;

									@Override
									public void buttonClick(ClickEvent event) {
										try {

											Object value = og.getValue();
											Preference p = PreferenceOperations
													.getData(
															Constants.ANNOTATION_DISPLAY_CONTROL,
															parent.getUserId());
											if (p != null)
												PreferenceOperations.setValue(
														value, p);
											else
												PreferenceOperations.storeData(
														value,
														Integer.class.getName(),
														Constants.ANNOTATION_DISPLAY_CONTROL,
														null, parent.getUserId());

											parent.resetDataSource();		 
															 
											mainWindow
													.removeWindow(displayPrefWindow);
										    
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});
						submit.setClickShortcut(KeyCode.ENTER);
						displayPrefWindow.addComponent(og);
						displayPrefWindow.addComponent(submit);
						mainWindow.addWindow(displayPrefWindow);
					}
				});

		annotationsItem.setStyleName("plugin");

		MenuBar.MenuItem precisionItem = displayPreferences.addItem(
				"Precision", new Command() {

					private static final long serialVersionUID = 1L;

					@SuppressWarnings("deprecation")
					@Override
					public void menuSelected(MenuItem selectedItem) {
						final Window displayPrefWindow = new Window();
						displayPrefWindow.setModal(true);
						displayPrefWindow.setClosable(true);
						((AbstractOrderedLayout) displayPrefWindow.getLayout())
								.setSpacing(true);
						displayPrefWindow.setWidth("300px");
						displayPrefWindow.setHeight("200px");
						displayPrefWindow.setResizable(false);
						displayPrefWindow.setCaption("Display Preference");
						displayPrefWindow.setImmediate(true);

						final TextField precision;
						precision = new TextField();
						precision.setCaption("Precision");
						precision.setValue(((TabularViewUI)parent).getTabViewPreferences()
								.getNumberPrecisionControl());

						final Window mainWindow = getApplication()
								.getMainWindow();

						Button submit = new Button("Submit",
								new Button.ClickListener() {

									private static final long serialVersionUID = -4799561372701936132L;

									@Override
									public void buttonClick(ClickEvent event) {
										try {

											Object value = precision.getValue();
											Preference p = PreferenceOperations
													.getData(
															Constants.NUMBER_PRECISION_CONTROL,
															parent.getUserId());
											if (p != null)
												PreferenceOperations.setValue(
														new Integer(value
																.toString()
																.trim()), p);
											else
												PreferenceOperations
														.storeData(
																new Integer(
																		value.toString()
																				.trim()),
																Integer.class
																		.getName(),
																Constants.NUMBER_PRECISION_CONTROL,
																null, parent.getUserId());

											((TabularViewUI)parent).setPrecisonNumber( new Integer(value.toString().trim()));											
											parent.resetDataSource();
											((TabularViewUI)parent).getTabViewPreferences().setNumberPrecisionControl( new Integer(value.toString().trim()));
											mainWindow
													.removeWindow(displayPrefWindow);
										 
										} catch (NumberFormatException nfe) {
											MessageBox mb = new MessageBox(
													getWindow(),
													"Warning",
													MessageBox.Icon.WARN,
													"Please enter a number. ",
													new MessageBox.ButtonConfig(
															ButtonType.OK, "Ok"));
											mb.show();

										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});
						submit.setClickShortcut(KeyCode.ENTER);
						displayPrefWindow.addComponent(precision);
						displayPrefWindow.addComponent(submit);
						mainWindow.addWindow(displayPrefWindow);
					}
				});
		precisionItem.setStyleName("plugin");

	 
	}
	
	@Override
	public void createFilterWindow() {
		
		final FilterWindow filterWindow = new FilterWindow(parent);			 
		getApplication().getMainWindow().addWindow(filterWindow);

	} 
	

}
