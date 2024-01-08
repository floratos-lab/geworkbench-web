package org.geworkbenchweb.plugins.msviper;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.BaseTheme;

/* This is intended to be display as a question mark, not like an actual button. */
public class ParameterDescriptionButton extends Button {

	private static final long serialVersionUID = 2310789551446997712L;
	
	static private final ThemeResource infoIcon = new ThemeResource(
			"../custom/icons/icon_info.gif");

	ParameterDescriptionButton(String description) {
		setDescription(description);
		setStyleName(BaseTheme.BUTTON_LINK);
		setIcon(infoIcon);
	}

}
