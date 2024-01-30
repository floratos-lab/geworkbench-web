package org.geworkbenchweb.visualizations;

import com.vaadin.shared.ui.JavaScriptComponentState;

public class CytoscapeState extends JavaScriptComponentState {
    public String[] nodes;
    public String[] edges;
    public String layoutName = "concentric"; // default
    public String[] colors = null;
}
