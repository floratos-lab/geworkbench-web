package org.geworkbenchweb.plugins.tabularview;

import java.util.HashMap;
import java.util.Map;

import org.geworkbenchweb.plugins.TableMenuSelector;
import org.geworkbenchweb.plugins.Tabular;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.utils.PreferenceOperations;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.steinwedel.messagebox.MessageBox;

public class TabularMenuSelector extends TableMenuSelector {

	public TabularMenuSelector(Tabular tabular, String tabularName) {
		super(tabular, tabularName);
	}

	private static final long serialVersionUID = -2811557010017084202L;

	@Override
	protected Map<String, Command> createDisplayPreferenceItems() {
		Map<String, Command> map = new HashMap<String, Command>();
		map.put("Gene Symbol/Marker ID", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window displayPrefWindow = new Window();
				displayPrefWindow.setModal(true);
				displayPrefWindow.setClosable(true);
				displayPrefWindow.setWidth("300px");
				displayPrefWindow.setHeight("200px");
				displayPrefWindow.setResizable(false);
				displayPrefWindow.setCaption("Display Preference");
				displayPrefWindow.setImmediate(true);

				final OptionGroup og;
				og = new OptionGroup();
				og.setImmediate(true);
				og.addItem(Constants.MarkerDisplayControl.marker.ordinal());
				og.addItem(Constants.MarkerDisplayControl.gene_symbol.ordinal());
				og.addItem(Constants.MarkerDisplayControl.both.ordinal());
				og.setItemCaption(Constants.MarkerDisplayControl.marker.ordinal(), "Marker ID");
				og.setItemCaption(Constants.MarkerDisplayControl.gene_symbol.ordinal(), "Gene Symbol");
				og.setItemCaption(Constants.MarkerDisplayControl.both.ordinal(), "Both");
				og.select(((TabularViewUI) parent).getTabViewPreferences().getMarkerDisplayControl());

				final UI mainWindow = UI.getCurrent();

				Button submit = new Button("Submit", new Button.ClickListener() {

					private static final long serialVersionUID = -4799561372701936132L;

					@Override
					public void buttonClick(ClickEvent event) {
						try {

							Object value = og.getValue();
							Preference p = PreferenceOperations.getData(Constants.MARKER_DISPLAY_CONTROL,
									parent.getUserId());
							if (p != null)
								PreferenceOperations.setValue(value, p);
							else
								PreferenceOperations.storeData(value, Integer.class.getName(),
										Constants.MARKER_DISPLAY_CONTROL, null, parent.getUserId());

							parent.resetDataSource();

							mainWindow.removeWindow(displayPrefWindow);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				submit.setClickShortcut(KeyCode.ENTER);
				VerticalLayout layout = new VerticalLayout();
				displayPrefWindow.setContent(layout);
				layout.addComponent(og);
				layout.addComponent(submit);
				mainWindow.addWindow(displayPrefWindow);
			}
		});

		map.put("Annotations", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window displayPrefWindow = new Window();
				displayPrefWindow.setModal(true);
				displayPrefWindow.setClosable(true);
				displayPrefWindow.setWidth("300px");
				displayPrefWindow.setHeight("150px");
				displayPrefWindow.setResizable(false);
				displayPrefWindow.setCaption("Display Preference");
				displayPrefWindow.setImmediate(true);

				final OptionGroup og;
				og = new OptionGroup();
				og.setImmediate(true);
				og.addItem(Constants.AnnotationDisplayControl.on.ordinal());
				og.addItem(Constants.AnnotationDisplayControl.off.ordinal());
				og.setItemCaption(Constants.AnnotationDisplayControl.on.ordinal(), "Show annotations");
				og.setItemCaption(Constants.AnnotationDisplayControl.off.ordinal(), "Hide annotations");

				og.select(((TabularViewUI) parent).getTabViewPreferences().getAnnotationDisplayControl());

				final UI mainWindow = UI.getCurrent();

				Button submit = new Button("Submit", new Button.ClickListener() {

					private static final long serialVersionUID = -4799561372701936132L;

					@Override
					public void buttonClick(ClickEvent event) {
						try {
							Object value = og.getValue();
							Preference p = PreferenceOperations.getData(Constants.ANNOTATION_DISPLAY_CONTROL,
									parent.getUserId());
							if (p != null)
								PreferenceOperations.setValue(value, p);
							else
								PreferenceOperations.storeData(value, Integer.class.getName(),
										Constants.ANNOTATION_DISPLAY_CONTROL, null, parent.getUserId());

							parent.resetDataSource();

							mainWindow.removeWindow(displayPrefWindow);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				submit.setClickShortcut(KeyCode.ENTER);
				VerticalLayout layout = new VerticalLayout();
				displayPrefWindow.setContent(layout);
				layout.addComponent(og);
				layout.addComponent(submit);
				mainWindow.addWindow(displayPrefWindow);
			}
		});

		map.put("Precision", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window displayPrefWindow = new Window();
				displayPrefWindow.setModal(true);
				displayPrefWindow.setClosable(true);
				displayPrefWindow.setWidth("300px");
				displayPrefWindow.setHeight("200px");
				displayPrefWindow.setResizable(false);
				displayPrefWindow.setCaption("Display Preference");
				displayPrefWindow.setImmediate(true);

				final TextField precision;
				precision = new TextField();
				precision.setCaption("Precision");
				precision.setValue(
						String.valueOf(((TabularViewUI) parent).getTabViewPreferences().getNumberPrecisionControl()));

				final UI mainWindow = UI.getCurrent();

				Button submit = new Button("Submit",
						new Button.ClickListener() {

							private static final long serialVersionUID = -4799561372701936132L;

							@Override
							public void buttonClick(ClickEvent event) {
								try {
									Object value = precision.getValue();
									Preference p = PreferenceOperations.getData(Constants.NUMBER_PRECISION_CONTROL,
											parent.getUserId());
									if (p != null)
										PreferenceOperations.setValue(Integer.valueOf(value.toString().trim()), p);
									else
										PreferenceOperations.storeData(Integer.valueOf(value.toString().trim()),
												Integer.class.getName(), Constants.NUMBER_PRECISION_CONTROL, null,
												parent.getUserId());

									((TabularViewUI) parent)
											.setPrecisionNumber(Integer.valueOf(value.toString().trim()));
									parent.resetDataSource();
									((TabularViewUI) parent).getTabViewPreferences()
											.setNumberPrecisionControl(Integer.valueOf(value.toString().trim()));
									mainWindow
											.removeWindow(displayPrefWindow);

								} catch (NumberFormatException nfe) {
									MessageBox.createWarning().withCaption("Warning")
											.withMessage("Please enter a number.").withOkButton().open();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
				submit.setClickShortcut(KeyCode.ENTER);
				VerticalLayout layout = new VerticalLayout();
				displayPrefWindow.setContent(layout);
				layout.addComponent(precision);
				layout.addComponent(submit);
				mainWindow.addWindow(displayPrefWindow);
			}
		});
		return map;
	}

	@Override
	protected Window createFilterWindow() {
		return new FilterWindow(parent);
	}
}
