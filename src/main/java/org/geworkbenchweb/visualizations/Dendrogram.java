package org.geworkbenchweb.visualizations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.VariableOwner;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the dendrogram widget.
 */
public class Dendrogram extends AbstractComponent {

	private static final long serialVersionUID = -6825142416797042091L;

	final private int[] colors;

	final private int arrayNumber;

	final private int markerNumber;

	final private String arrayCluster, markerCluster;
	final private String[] arrayLabels, markerLabels;

	private int cellWidth = 10, cellHeight = 5;
	private int exportImageCellWidth = 30, exportImageCellHeight = 15;

	// 20.07.2016
	private String arrayPos; // Can be Bottom or Top
	private String markerPos; // Can be Right or Left
	///

	public Dendrogram(int arrayNumber, int markerNumber, String arrayCluster, String markerCluster,
			String[] arrayLabels, String[] markerLabels, int[] colors) {
		this.arrayNumber = arrayNumber;
		this.markerNumber = markerNumber;
		this.arrayCluster = arrayCluster;
		this.markerCluster = markerCluster;
		this.arrayLabels = arrayLabels;
		this.markerLabels = markerLabels;

		/* element value range [-255, 255] */
		this.colors = colors;

		arrayIndex1 = 0;
		arrayIndex2 = arrayNumber - 1;
		markerIndex1 = 0;
		markerIndex2 = markerNumber - 1;

		// 20.07.2016
		arrayPos = "Bottom";
		markerPos = "Right";
		////

		// this is the upper limit because on the client side the space is smaller by
		// excluding microarray dendrogram and microarray labels
		paintableMarkers = Math.min(markerNumber, MAX_HEIGHT / cellHeight);
	}

	final private int MAX_HEIGHT = 10000;

	// FIXME this should be replaced by vaadin 7 communication mechanism
	VariableOwner temporary = null;

	// FIXME this should be replaced by vaadin 7 communication mechanism
	public void paintContent(PaintTarget target) throws PaintException {

		if (requestExportImage) {
			target.addVariable(temporary, "exportImage", true);
			target.addAttribute("exportImageCellWidth", exportImageCellWidth);
			target.addAttribute("exportImageCellHeight", exportImageCellHeight);
			// target.addVariable(this, "firstMarker", firstMarker);
			Integer[] colorSubset = new Integer[markerNumber * arrayNumber];
			int i = 0; // numbers of color values to skip
			int iSubset = 0;
			for (int y = 0; y < markerNumber; y++) {
				for (int x = 0; x < arrayNumber; x++) {
					colorSubset[iSubset++] = colors[i++];
				}
			}
			target.addAttribute("colors", colorSubset);
			requestExportImage = false;
			// return;
		} else {
			if (firstMarker + paintableMarkers > markerNumber) { // handle the last 'page'
				firstMarker = markerNumber - paintableMarkers;
			}
			// target.addVariable(this, "firstMarker", firstMarker);
			Integer[] colorSubset = new Integer[paintableMarkers * arrayNumber];
			int i = firstMarker * arrayNumber; // numbers of color values to skip
			int iSubset = 0;
			for (int y = 0; y < paintableMarkers; y++) {
				for (int x = 0; x < arrayNumber; x++) {
					colorSubset[iSubset++] = colors[i++];
				}
			}

			target.addAttribute("colors", colorSubset);
		}

		// Paint any component specific content by setting attributes
		// These attributes can be read in updateFromUIDL in the widget.
		target.addAttribute("arrayNumber", arrayNumber);
		target.addAttribute("markerNumber", markerNumber);
		if (selectedArrayClusters == null) {
			target.addAttribute("arrayCluster", arrayCluster);
		} else {
			target.addAttribute("arrayCluster", selectedArrayClusters);
		}
		if (selectedMarkerClusters == null) {
			target.addAttribute("markerCluster", markerCluster);
		} else {
			target.addAttribute("markerCluster", selectedMarkerClusters);
		}

		target.addVariable(temporary, "firstMarker", firstMarker);

		target.addAttribute("arrayLabels", arrayLabels);
		target.addAttribute("markerLabels", markerLabels);
		target.addAttribute("cellWidth", cellWidth);
		target.addAttribute("cellHeight", cellHeight);
		target.addVariable(temporary, "arrayIndex1", arrayIndex1);
		target.addVariable(temporary, "arrayIndex2", arrayIndex2);
		target.addVariable(temporary, "markerIndex1", markerIndex1);
		target.addVariable(temporary, "markerIndex2", markerIndex2);

		// 20.07.2016
		target.addVariable(temporary, "arrayPos", arrayPos);
		target.addVariable(temporary, "markerPos", markerPos);
		////
	}

	private int firstMarker = 0;
	private int paintableMarkers;

	private int arrayIndex1, arrayIndex2;
	private int markerIndex1, markerIndex2;

	private String selectedArrayClusters = null;
	private String selectedMarkerClusters = null;

	// FIXME this should be replaced by vaadin 7 communication mechanism
	public void changeVariables(Object source, Map<String, Object> variables) {

		if (variables.containsKey("arrayIndex2")) {
			selectedArrayClusters = (String) variables.get("selectedArrayClusters");
			arrayIndex1 = (Integer) variables.get("arrayIndex1");
			arrayIndex2 = (Integer) variables.get("arrayIndex2");
		} else if (variables.containsKey("markerIndex2")) {
			selectedMarkerClusters = (String) variables.get("selectedMarkerClusters");
			markerIndex1 = (Integer) variables.get("markerIndex1");
			markerIndex2 = (Integer) variables.get("markerIndex2");
		} else if (variables.containsKey("firstMarker")) {
			firstMarker = (Integer) variables.get("firstMarker");
			paintableMarkers = (Integer) variables.get("paintableMarkers");
			requestRepaint();
			// } else if (variables.containsKey("imageUrl")) {
			// String imageUrl = (String) variables.get("imageUrl");
			// // TODO what to do with it?
		}
	}

	public void zoomIn() {
		if (cellHeight >= 50 || cellWidth >= 100)
			return;

		cellWidth += 10;
		cellHeight += 5;
		requestRepaint();
	}

	public void zoomOut() {
		if (cellHeight <= 5 || cellWidth <= 10)
			return;

		cellWidth -= 10;
		cellHeight -= 5;
		paintableMarkers = Math.min(markerNumber, MAX_HEIGHT / cellHeight);
		requestRepaint();
	}

	public void reset() {
		cellWidth = 10;
		cellHeight = 5;
		paintableMarkers = Math.min(markerNumber, MAX_HEIGHT / cellHeight);

		selectedArrayClusters = null;
		selectedMarkerClusters = null;
		arrayIndex1 = 0;
		arrayIndex2 = arrayNumber - 1;
		markerIndex1 = 0;
		markerIndex2 = markerNumber - 1;

		requestRepaint();
	}

	// 20.07.2016 - New function for refreshing dendrogram when user wishes to
	// change display options
	public void refresh(String array_pos, String marker_pos) {
		arrayPos = array_pos;
		markerPos = marker_pos;
		requestRepaint();
	}

	public List<String> getSelectedArrayLabels() {
		List<String> list = new ArrayList<String>();
		for (int i = arrayIndex1; i <= arrayIndex2; i++) {
			list.add(arrayLabels[i]);
		}
		return list;
	}

	public List<String> getSelectedMarkerLabels() {
		List<String> list = new ArrayList<String>();
		for (int i = markerIndex1; i <= markerIndex2; i++) {
			list.add(markerLabels[i]);
		}
		return list;
	}

	private boolean requestExportImage = false;

	public void exportImage() {
		if (cellHeight < 15 && cellWidth < 30) {
			exportImageCellWidth = 30;
			exportImageCellHeight = 15;
		} else {
			exportImageCellWidth = cellWidth;
			exportImageCellHeight = cellHeight;
		}
		requestExportImage = true;
		requestRepaint();
	}
}
