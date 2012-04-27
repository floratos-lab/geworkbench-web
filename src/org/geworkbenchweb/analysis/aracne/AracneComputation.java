package org.geworkbenchweb.analysis.aracne;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.components.aracne.HardenedAracne;

import wb.data.Marker;
import wb.data.MarkerSet;
import wb.data.Microarray;
import wb.data.MicroarraySet;
import wb.plugins.aracne.WeightedGraph;
import edu.columbia.c2b2.aracne.Parameter;

/**
 * ARACNe computation.
 */
class AracneComputation {

	private static Log log = LogFactory.getLog(AracneComputation.class);

	private final DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView;
	private final Parameter p;

	private final int bootstrapNumber;
	private final double pThreshold;
	
	private HardenedAracne hardenedAracne = new HardenedAracne();

	public AracneComputation(final DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSet,
			final Parameter p, int bootstrapNumber, final double pThreshold) {
		this.mSetView = mSet;
		this.p = p;

		this.bootstrapNumber = bootstrapNumber;
		this.pThreshold = pThreshold;
	}

	void cancel() {
		hardenedAracne.cancelled = true;
	}
	
	public WeightedGraph execute() {
		hardenedAracne.cancelled = false;

		p.setSuppressFileWriting(true);
		WeightedGraph weightedGraph;
		try {
			weightedGraph = hardenedAracne.run(convert(mSetView), p,
					bootstrapNumber, pThreshold);
		} catch (Exception e) {
			log.warn("Exception caught in ARACNe run: " + e.toString());
			return null;
		}

		/* done if in PREPROCESSING mode */
		if (this.p.getMode().equals(Parameter.MODE.PREPROCESSING)) {
			return null;
		}

		return weightedGraph;
	}

	private static MicroarraySet convert(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> inSet) {
		MarkerSet markers = new MarkerSet();
		for (DSGeneMarker marker : inSet.markers()) {
			markers.addMarker(new Marker(marker.getLabel()));
		}
		MicroarraySet returnSet = new MicroarraySet(inSet.getDataSet()
				.getDataSetName(), inSet.getDataSet().getID(), "Unknown",
				markers);
		DSItemList<DSMicroarray> arrays = inSet.items();
		for (DSMicroarray microarray : arrays) {
			float[] markerData = new float[markers.size()];
			int i = 0;
			for (DSGeneMarker marker : inSet.markers()) {
				markerData[i++] = (float) microarray.getMarkerValue(marker)
						.getValue();
			}
			returnSet.addMicroarray(new Microarray(microarray.getLabel(),
					markerData));
		}
		return returnSet;
	}

}