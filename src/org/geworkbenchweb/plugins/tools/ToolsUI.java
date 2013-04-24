package org.geworkbenchweb.plugins.tools;

import org.geworkbenchweb.plugins.DataTypeMenuPage;
import org.geworkbenchweb.plugins.DataTypeUI;

/**
 * List of all plug-ins regardless of data type. 
*/
public class ToolsUI extends DataTypeMenuPage implements DataTypeUI {

	private static final long serialVersionUID = 1L;
	
	public ToolsUI() {
		super("The list of all the available tools.", "Tools", null, null);
	}
}
