package org.geworkbenchweb.plugins;

import java.util.HashMap;
import java.util.Map;

import org.geworkbenchweb.pojos.TTestResult;

/* this class is loosely based on the classes called TTestResultSelectionPanel and CytoscapeUtil in geWorkbench desktop, cytoscape component. */
public class NetworkColorUtil {

	private static String calculateColor(double minTValue, double maxTValue, double tValue) {
		int maxAbs = (int) Math.max(Math.abs(minTValue), Math.abs(maxTValue));

		int r = 0, g = 0, b = 0;

		if (maxAbs != 0) {
			int colorindex = (int) (255 * (tValue) / Math.abs(maxAbs));
			if (colorindex < 0) {
				colorindex = Math.abs(colorindex);
				if (colorindex > 255)
					colorindex = 255;
				r = 255 - colorindex;
				g = 255 - colorindex;
				b = 255;
			} else if (colorindex <= 255) {
				r = 255;
				g = 255 - colorindex;
				b = 255 - colorindex;
			} else { // if (colorindex > 255)
				colorindex = 255;
				r = 255;
				g = 255 - colorindex;
				b = 255 - colorindex;
			}

		}
		return String.format("#%02X%02X%02X", r, g, b);
	}

	// create map from gene symbol to color
	public static Map<String, String> getTTestResultSetColorMap(TTestResult tTestResult, String[] markerLabels) {
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

		Map<String, String> colorMap = new HashMap<String, String>();
		for (String key : tvalueMap.keySet()) {
			double tValue = tvalueMap.get(key);
			String c = NetworkColorUtil.calculateColor(minTValue, maxTValue, tValue);
			colorMap.put(key, c);
		}
		return colorMap;
	}
}
