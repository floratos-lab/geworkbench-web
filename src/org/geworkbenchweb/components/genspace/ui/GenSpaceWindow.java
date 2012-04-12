package org.geworkbenchweb.components.genspace.ui;

import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

public class GenSpaceWindow extends Window{
	private static final long serialVersionUID = -4091993515000311665L;

	public GenSpaceWindow()
	{
		setCaption("genSpace");
		addComponent(new Label("This is genspace"));
	}
}
