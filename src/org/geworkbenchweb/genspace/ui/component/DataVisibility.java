package org.geworkbenchweb.genspace.ui.component;

import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbenchweb.genspace.ObjectHandler;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class DataVisibility extends VerticalLayout implements ClickListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7554634436096470168L;
	
	private GenSpaceLogin login;

	static final String PROPERTY_KEY = "genSpace_logging_preferences"; // the
																		// key
																		// in
																		// the
																		// properties
																		// file

	public static final List<String> logOptions = Arrays.asList(new String[] {
			"Log My Analysis Events", "Log My Analysis Events Anonymously",
			"Do Not Log My Analysis Events" });
	public static final List<String> dataVisOptions = Arrays
			.asList(new String[] { "Data Visible to None",
					"Data Visible Within My Networks", "Data Visible To All" });

	private NativeSelect logOptionsSelect;

	private NativeSelect dataVisOptiionsSelect;

	private Button save;

	// private String username = "";
	int preference;

	private ObjectHandler objectHandler;

	public DataVisibility(GenSpaceLogin login) {
		this.login = login;
		// read the preferences from the properties file
		try {
			// PropertiesManager properties = PropertiesManager.getInstance();
			// String pref = properties.getProperty(DataVisibility.class,
			// PROPERTY_KEY, null);

			// username = GenSpaceServerFactory.getUsername();

			User user = login.getGenSpaceServerFactory().getUser();
			int logData = user.getLogData();

			// if (pref == null) {
			if (logData < 0) {
				// if the preferences are not set, then show the pop up window

				// ideally this should also be in the properties file
				String message = "geWorkbench now includes a component called genSpace,\n"
						+ "which will provide social networking capabilities and allow\n"
						+ "you to connect with other geWorkbench users.\n\n"
						+ "In order for it to be effective, genSpace must log which analysis\n"
						+ "tools you use during your geWorkbench session.\n\n"
						+ "Please go to the genSpace Logging Preference window to configure \n"
						+ "your preference. You can later change it at any time.";
				String title = "Please set your genSpace logging preferences.";
				Notification notification = new Notification(title, message,
						Notification.TYPE_HUMANIZED_MESSAGE);
				notification.setDelayMsec(-1);
				getApplication().getMainWindow().showNotification(notification);

				// // set the default to "log anonymously"
				// pref = "1";
				// // write it to the properties file
				// properties
				// .setProperty(DataVisibility.class, PROPERTY_KEY, pref);
				logData = 1;
			}

			// set the logging level
			// ObjectHandler.setLogStatus(Integer.parseInt(pref));
			// preference = Integer.parseInt(pref);
			if (objectHandler != null) {
				objectHandler.setLogStatus(logData);
			}
			preference = logData;
		}
		catch (Exception e) {
		}

		initComponents();
	}

	private void initComponents() {
		// BoxLayout l = new BoxLayout(this, BoxLayout.Y_AXIS);
		//
		// this.setSize(500, 600);

		// l.
		// this.setLayout(l);
		if (login.getGenSpaceServerFactory().isLoggedIn()) {

			removeAllComponents();
			Label blank = new Label(" ");

			logOptionsSelect = new NativeSelect("-- Select Log Preferences --",
					logOptions);
			int preference = login.getGenSpaceServerFactory().getUser().getLogData();
			logOptionsSelect.select(logOptions.get(preference));

			// try {
			// PropertiesManager properties = PropertiesManager.getInstance();
			// properties.setProperty(DataVisibility.class, PROPERTY_KEY, ""
			// + (logPreferences.getSelectedIndex() - 1));
			// } catch (Exception e) {
			// }

			if (objectHandler != null) {
				objectHandler.setLogStatus(preference);
			}

			// c.gridwidth = GridBagConstraints.REMAINDER;
			// gridbag.setConstraints(logPreferences, c);
			addComponent(logOptionsSelect);
			logOptionsSelect.addListener(ItemClickEvent.class, this,
					"logPrefChanged");

			// c.gridwidth = GridBagConstraints.REMAINDER;
			// gridbag.setConstraints(blank, c);
			// add(blank);

			dataVisOptiionsSelect = new NativeSelect(
					"-- Select Data Visibility Options --", dataVisOptions);
			preference = login.getGenSpaceServerFactory().getUser().getDataVisibility();
			dataVisOptiionsSelect.select(dataVisOptions.get(preference));
			addComponent(dataVisOptiionsSelect);
			// add(blank);
			TextArea info = new TextArea(
					"Your selection of data visibility will affect its appearance\n"
							+ "within recommendations of others. It will also affect your\n"
							+ "ability to see recommendations - if you make your data\n"
							+ "completely private, then you will not see any recommendations\n"
							+ "based on other users' data.");
			info.setEnabled(false);
			info.setWordwrap(true);
			// info.setOpaque(false);
			// info.setDisabledTextColor(Color.black);
			// info.setMaximumSize(new Dimension(500, 400));

			addComponent(info);
			addComponent(blank);
		}

		save = new Button("Save");
		addComponent(save);

		// validate();
		save.addListener(this);
	}

	public void logPrefChanged(ItemClickEvent event) {
		// TODO:
	}

	@Override
	public void buttonClick(ClickEvent e) {
		Window mainWindow = getApplication().getMainWindow();
		if (e.getSource() == save) {
			if (logOptionsSelect.getValue() == null) {
				mainWindow.showNotification("Please Select Log Preferences",
						Notification.TYPE_ERROR_MESSAGE);
			}
			else {
				preference = logOptions.indexOf(logOptionsSelect.getValue());
				if (objectHandler != null) {
					objectHandler.setLogStatus(preference);
				}
				
				login.getGenSpaceServerFactory().getUser().setLogData(preference);

				login.getGenSpaceServerFactory().getUser()
						.setDataVisibility(
								dataVisOptions.indexOf(dataVisOptiionsSelect
										.getValue()));

				if (login.getGenSpaceServerFactory().userUpdate()) {
					String msg = "Data Visibility Saved";
					mainWindow.showNotification(msg,
							Notification.TYPE_TRAY_NOTIFICATION);
				}
				else {
					String msg = "Data Visibility update failed";
					mainWindow.showNotification(msg,
							Notification.TYPE_ERROR_MESSAGE);
				}
			}
		}
	}
}
