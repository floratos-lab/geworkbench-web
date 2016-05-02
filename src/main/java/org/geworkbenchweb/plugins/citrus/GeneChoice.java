package org.geworkbenchweb.plugins.citrus;

import java.text.DecimalFormat;
import java.text.NumberFormat;

class GeneChoice implements Comparable<GeneChoice> {
	final String symbol;
	final int count;
	final double minP;
	final int id;

	private static NumberFormat decimalFormat = new DecimalFormat("0.#####");
	private static NumberFormat scientificFormat = new DecimalFormat("0.###E0");

	GeneChoice(String symbol, int count, double minP, int id) {
		this.symbol = symbol;
		this.count = count;
		this.minP = minP;
		this.id = id;
	}

	@Override
	public int compareTo(GeneChoice o) {
		if (minP < o.minP)
			return -1;
		else if (minP > o.minP)
			return 1;
		else
			return 0;
	}

	@Override
	public String toString() {
		String p = "";
		if (minP > 0.0001)
			p = decimalFormat.format(minP);
		else
			p = scientificFormat.format(minP);
		return symbol + "(" + count + "/" + p + ")";
	}
}
