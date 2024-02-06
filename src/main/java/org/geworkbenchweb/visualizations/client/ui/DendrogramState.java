package org.geworkbenchweb.visualizations.client.ui;

import com.vaadin.shared.AbstractComponentState;

public class DendrogramState extends AbstractComponentState {
    public int[] colors;
    public int arrayNumber;
    public int markerNumber;
    public String arrayCluster, markerCluster;
    public String[] arrayLabels, markerLabels;

    public int cellWidth = 10, cellHeight = 5;
    public int exportImageCellWidth = 30, exportImageCellHeight = 15;

    public String arrayPos; // Can be Bottom or Top
    public String markerPos; // Can be Right or Left

    public int firstMarker = 0;
    public int paintableMarkers;

    public int arrayIndex1, arrayIndex2;
    public int markerIndex1, markerIndex2;

    public String selectedArrayClusters = null;
    public String selectedMarkerClusters = null;

    public boolean requestExportImage = false;
}
