package org.geworkbenchweb.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.pojos.TTestResult;

/* this class is loosely based on the classes called TTestResultSelectionPanel and CytoscapeUtil in geWorkbench desktop, cytoscape component. */
public class NetworkColorUtil {

	private static String calculateColor(double minTValue, double maxTValue, double tValue) {
		int maxAbs = (int) Math.max(Math.abs(minTValue), Math.abs(maxTValue));

		final String colorFormat = "#%02X%02X%02X";
		if (maxAbs == 0) {
			return String.format(colorFormat, 0, 0, 0);
		}

		int colorindex = (int) (255 * (tValue) / Math.abs(maxAbs));
		if (colorindex < 0) {
			colorindex = Math.abs(colorindex);
			if (colorindex > 255)
				colorindex = 255;
			return String.format(colorFormat, 255 - colorindex, 255 - colorindex, 255);
		} else if (colorindex <= 255) {
			return String.format(colorFormat, 255, 255 - colorindex, 255 - colorindex);
		} else { // if (colorindex > 255)
			return String.format(colorFormat, 255, 0, 0);
		}
	}

	// create map from gene symbol to color
	public static List<String> getTTestResultSetColorMap(TTestResult tTestResult, String[] markerLabels) {
		double minTValue = Double.POSITIVE_INFINITY;
		double maxTValue = Double.NEGATIVE_INFINITY;

		int[] significantIndex = tTestResult.getSignificantIndex();
		double[] tValues = tTestResult.gettValue();

		Map<String, Double> tvalueMap = new HashMap<String, Double>();
		for (int m : significantIndex) {
			String name = markerLabels[m].trim();
			double tValue = tValues[m];
			if (minTValue > tValue)
				minTValue = tValue;
			if (maxTValue < tValue)
				maxTValue = tValue;

			if (tvalueMap.containsKey(name) && tvalueMap.get(name) >= tValue)
				continue;
			tvalueMap.put(name, tValue);
		}

		List<String> colorMap = new ArrayList<String>();
		for (String key : tvalueMap.keySet()) {
			double tValue = tvalueMap.get(key);
			String c = NetworkColorUtil.calculateColor(minTValue, maxTValue, tValue);
			colorMap.add(key + ":" + c);
		}
		return colorMap;
	}
}
