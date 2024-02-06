package org.geworkbenchweb.visualizations;

import java.util.ArrayList;
import java.util.List;

import org.geworkbenchweb.visualizations.client.ui.DendrogramServerRpc;
import org.geworkbenchweb.visualizations.client.ui.DendrogramState;

import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the dendrogram widget.
 */
public class Dendrogram extends AbstractComponent {

	@Override
	public DendrogramState getState() {
		return (DendrogramState) super.getState();
	}

	public Dendrogram(int arrayNumber, int markerNumber, String arrayCluster, String markerCluster,
			String[] arrayLabels, String[] markerLabels, int[] colors) {
		getState().arrayNumber = arrayNumber;
		getState().markerNumber = markerNumber;
		getState().arrayCluster = arrayCluster;
		getState().markerCluster = markerCluster;
		getState().arrayLabels = arrayLabels;
		getState().markerLabels = markerLabels;

		/* element value range [-255, 255] */
		getState().colors = colors;

		getState().arrayIndex1 = 0;
		getState().arrayIndex2 = arrayNumber - 1;
		getState().markerIndex1 = 0;
		getState().markerIndex2 = markerNumber - 1;

		getState().arrayPos = "Bottom";
		getState().markerPos = "Right";

		// this is the upper limit because on the client side the space is smaller by
		// excluding microarray dendrogram and microarray labels
		getState().paintableMarkers = Math.min(markerNumber, MAX_HEIGHT / getState().cellHeight);

		registerRpc(rpc);
	}

	final private int MAX_HEIGHT = 10000;

	public void zoomIn() {
		if (getState().cellHeight >= 50 || getState().cellWidth >= 100)
			return;

		getState().cellWidth += 10;
		getState().cellHeight += 5;
	}

	public void zoomOut() {
		if (getState().cellHeight <= 5 || getState().cellWidth <= 10)
			return;

		getState().cellWidth -= 10;
		getState().cellHeight -= 5;
		getState().paintableMarkers = Math.min(getState().markerNumber, MAX_HEIGHT / getState().cellHeight);
	}

	public void reset() {
		getState().cellWidth = 10;
		getState().cellHeight = 5;
		getState().paintableMarkers = Math.min(getState().markerNumber, MAX_HEIGHT / getState().cellHeight);

		getState().selectedArrayClusters = null;
		getState().selectedMarkerClusters = null;
		getState().arrayIndex1 = 0;
		getState().arrayIndex2 = getState().arrayNumber - 1;
		getState().markerIndex1 = 0;
		getState().markerIndex2 = getState().markerNumber - 1;
	}

	// refreshing dendrogram when user wishes to
	// change display options
	public void refresh(String array_pos, String marker_pos) {
		getState().arrayPos = array_pos;
		getState().markerPos = marker_pos;
	}

	public List<String> getSelectedArrayLabels() {
		List<String> list = new ArrayList<String>();
		for (int i = getState().arrayIndex1; i <= getState().arrayIndex2; i++) {
			list.add(getState().arrayLabels[i]);
		}
		return list;
	}

	public List<String> getSelectedMarkerLabels() {
		List<String> list = new ArrayList<String>();
		for (int i = getState().markerIndex1; i <= getState().markerIndex2; i++) {
			list.add(getState().markerLabels[i]);
		}
		return list;
	}

	public void exportImage() {
		if (getState().cellHeight < 15 && getState().cellWidth < 30) {
			getState().exportImageCellWidth = 30;
			getState().exportImageCellHeight = 15;
		} else {
			getState().exportImageCellWidth = getState().cellWidth;
			getState().exportImageCellHeight = getState().cellHeight;
		}
		getState().requestExportImage = true;
	}

	private DendrogramServerRpc rpc = new DendrogramServerRpc() {
		@Override
		public void resetExportStatus() {
			getState().requestExportImage = false;
		}
	};
}
