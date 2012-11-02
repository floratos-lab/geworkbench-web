package org.geworkbenchweb.plugins.tools;

import com.vaadin.ui.HorizontalLayout;

public class ToolsUI extends HorizontalLayout {

	private static final long serialVersionUID = 1L;

	public ToolsUI(Long dataSetId) {
		setStyleName("sample-view");
		setImmediate(true);
		setSpacing(true);
		setSizeFull();
	}
}
