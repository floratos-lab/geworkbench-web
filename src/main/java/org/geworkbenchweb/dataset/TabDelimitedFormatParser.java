package org.geworkbenchweb.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.parsers.InputFileFormatException;

public class TabDelimitedFormatParser {

	private static Log log = LogFactory.getLog(TabDelimitedFormatParser.class);

	private final File file;

	private transient int arrayNumber = -1;
	private transient String[] arrayLabels;
	private transient List<String> markerLabelTmp;
	private transient List<float[]> valueTmp;

	public TabDelimitedFormatParser(File file) {
		this.file = file;
	}

	public MicroarraySet parse() throws InputFileFormatException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		arrayNumber = -1;
		arrayLabels = null;
		markerLabelTmp = new ArrayList<String>();
		valueTmp = new ArrayList<float[]>();
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() > 0) {
				parseLine(line.trim());
			}
		}
		reader.close();

		log.info("parsing finished");

		int markerNumber = valueTmp.size();
		String[] markerLabels = markerLabelTmp.toArray(new String[0]);
		float[][] values = new float[markerNumber][arrayNumber];
		for (int i = 0; i < markerNumber; i++) {
			for (int j = 0; j < arrayNumber; j++) {
				values[i][j] = valueTmp.get(i)[j];
			}
		}
		return new MicroarraySet(arrayNumber, markerNumber, arrayLabels,
				markerLabels, values, null);
	}

	private void parseLine(final String line) throws InputFileFormatException {
		if (line.startsWith("#") || line.startsWith("!"))
			return;

		if (arrayNumber <= 0) { // parse header
			String[] f = line.split("\t");
			arrayNumber = f.length - 1;
			if (arrayNumber <= 0) {
				throw new InputFileFormatException(
						"invalid microarray number in the header line "
								+ arrayNumber);
			}
			arrayLabels = new String[arrayNumber];
			for (int i = 0; i < arrayNumber; i++) {
				arrayLabels[i] = f[i + 1];
			}
			return;
		}

		String[] f = line.split("\t");
		float[] oneRow = new float[arrayNumber];
		markerLabelTmp.add(f[0]);
		for (int i = 0; i < arrayNumber; i++) {
			oneRow[i] = Float.parseFloat(f[i + 1]);
		}
		valueTmp.add(oneRow);
	}
}
