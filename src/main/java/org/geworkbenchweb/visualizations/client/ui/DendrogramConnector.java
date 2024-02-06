package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;

@com.vaadin.shared.ui.Connect(org.geworkbenchweb.visualizations.Dendrogram.class)
public class DendrogramConnector extends AbstractComponentConnector {
    @Override
    protected Widget createWidget() {
        return GWT.create(VDendrogram.class);
    }

    @Override
    public VDendrogram getWidget() {
        return (VDendrogram) super.getWidget();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        final DendrogramState state = getState();
        final VDendrogram widget = getWidget();

        if (widget.heatmapCanvas == null) {
            // "Sorry, your browser doesn't support the HTML5 Canvas element";
            return;
        }

        widget.arrayNumber = state.arrayNumber;
        int markerNumber = state.markerNumber;
        String arrayCluster = state.arrayCluster;
        String markerCluster = state.markerCluster;
        widget.colors = state.colors;
        widget.arrayLabels = state.arrayLabels;
        widget.markerLabels = state.markerLabels;

        widget.cellWidth = state.cellWidth;
        widget.cellHeight = state.cellHeight;
        int exportImageCellWidth = state.exportImageCellWidth;
        int exportImageCellHeight = state.exportImageCellHeight;

        widget.firstMarker = state.firstMarker;

        widget.xIndex1 = state.arrayIndex1;
        widget.xIndex2 = state.arrayIndex2;
        widget.yIndex1 = state.markerIndex1;
        widget.yIndex2 = state.markerIndex2;

        widget.arrayPos = state.arrayPos;
        widget.markerPos = state.markerPos;

        if (state.requestExportImage) {
            // this does not show in eclipse workspace browser somehow
            Canvas offline = widget.createOfflineImage(exportImageCellWidth, exportImageCellHeight, markerNumber,
                    arrayCluster, markerCluster);
            // use the fixed window name so the window is reused for this functionality
            String dataUrl = offline.toDataUrl("image/png");

            if (VDendrogram.isIEBrowser())
                widget.openCanvasImage(dataUrl);
            else
                Window.open(dataUrl, "dendrogram_snapshot", "");

            DendrogramServerRpc rpc = getRpcProxy(DendrogramServerRpc.class);
            rpc.resetExportStatus();
        } else
            widget.createOnlineImage(markerNumber, arrayCluster, markerCluster);
    }

    @Override
    public DendrogramState getState() {
        return (DendrogramState) super.getState();
    }
}
