package org.geworkbenchweb.pojos;

import javax.persistence.Entity;

import org.geworkbench.components.hierarchicalclustering.computation.HNode;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
public class HierarchicalClusteringResult extends AbstractPojo {

	private HNode markerCluster;
	private HNode arrayCluster;
	private int[] selectedMarkers;
	private int[] selectedArrays;

	public HierarchicalClusteringResult() {}
	
	public HierarchicalClusteringResult(HNode markerCluster,
			HNode arrayCluster, int[] selectedMarkers, int[] selectedArrays) {
		this.markerCluster = markerCluster;
		this.arrayCluster = arrayCluster;
		this.selectedMarkers = selectedMarkers;
		this.selectedArrays = selectedArrays;
	}

	public HNode getMarkerCluster() {
		return markerCluster;
	}

	public void setMarkerCluster(HNode markerCluster) {
		this.markerCluster = markerCluster;
	}

	public HNode getArrayCluster() {
		return arrayCluster;
	}

	public void setArrayCluster(HNode arrayCluster) {
		this.arrayCluster = arrayCluster;
	}

	public int[] getSelectedMarkers() {
		return selectedMarkers;
	}

	public void setSelectedMarkers(int[] selectedMarkers) {
		this.selectedMarkers = selectedMarkers;
	}

	public int[] getSelectedArrays() {
		return selectedArrays;
	}

	public void setSelectedArrays(int[] selectedArrays) {
		this.selectedArrays = selectedArrays;
	}

	private static final long serialVersionUID = 3122902016623638363L;

}
