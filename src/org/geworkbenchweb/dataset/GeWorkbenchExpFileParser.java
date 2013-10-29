package org.geworkbenchweb.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.parsers.InputFileFormatException;

/**
 * 
 * The parser of the geWorkworkbench's native .exp file.
 * 
 * The ultimate authoritative description of this format is at
 * http://wiki.c2b2.columbia.edu/workbench/index.php/File_Formats#Affymetrix_File_Matrix_Format_.28geWorkbench_.22.exp.22_format.29
 * 
 * @author zji
 * @version $Id$
 */
public class GeWorkbenchExpFileParser {

	private static Log log = LogFactory.getLog(GeWorkbenchExpFileParser.class);
	
	private transient int markerCounter;
	private transient int arrayNumberTemp;
	private transient String[] arrayLabelsTemp;
	private transient List<float[]> confidenceTemp;

	private transient boolean pValueExists = false;
	private transient ArrayList<Float>[] valueColumns;
	private transient ArrayList<String> markerLabelList;

	/* use LinkedHashMap so to maintain the order */
	private transient Map<String, String[]> arrayInfo = new LinkedHashMap<String, String[]>();
	
	private final File expFile;
	
	GeWorkbenchExpFileParser(File expFile) {
		this.expFile = expFile;
	}

	public MicroarraySet parse() throws InputFileFormatException, IOException {

		int arrayNumber;
		String[] arrayLabels, markerLabels;
		float[][] values;
		float[][] confidence;

		markerCounter = 0;
		
		BufferedReader reader = new BufferedReader(new FileReader(expFile));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.trim().length()>0) {
				parseLine(line.trim());
			}
		}
		reader.close();
		
		arrayNumber = arrayNumberTemp;
		arrayLabels = arrayLabelsTemp;
		markerLabels = markerLabelList.toArray(new String[0]);
		
		values = new float[markerCounter][arrayNumber];
		for(int j=0; j<valueColumns.length; j++) {
			List<Float> v = valueColumns[j];
			for(int i=0; i<markerCounter; i++) {
				values[i][j] = v.get(i);
			}
		}
		
		if(pValueExists) {
			confidence = new float[markerCounter][arrayNumber];
			for(int i=0; i<markerCounter; i++) {
				float[] a = confidenceTemp.get(i);
				for(int j=0; j<arrayNumber; j++) {
					confidence[i][j] =a[j];
				}
			}
		} else {
			confidence = null;
		}
		
		/* Reset transient variables so they can be re-used safely. */
		arrayNumberTemp = 0;
		arrayLabelsTemp = null;
		confidenceTemp = null;
		valueColumns = null;
		
		log.info("parsing finished");
		return new MicroarraySet(arrayNumber, markerCounter, arrayLabels,
				markerLabels, values, confidence);
	}
	
	/* This is designed on purpose to be separated from parsing of expression data. */
	public Map<String, String[]> parseSetInformation() throws IOException, InputFileFormatException {
		arrayInfo = new LinkedHashMap<String, String[]>();
		
		BufferedReader reader = new BufferedReader(new FileReader(expFile));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.trim().length()>0) {
				parseSetLine(line.trim()); // FIXME parseSetLine
			}
		}
		reader.close();
		return arrayInfo;
	}

	/* analyze one line. temporary variables that must be maintained: markerCounter, arrayInfo, markerLabelList, valueColumns */
	private void parseLine(String line) throws InputFileFormatException {

		if (line.charAt(0) == '#') { // skip comment
			return;
		}

		int startindx = line.indexOf('\t');
		if (startindx <= 0)
			return;

		String[] fields = line.split( "\t", -1 );

		if (line.substring(0, 6).equalsIgnoreCase("AffyID")) { 
			// this should the first line (probably except for comments; there should be only one
			processHeaderFields(fields);
		} else if (line.substring(0, 11).equalsIgnoreCase("Description")) {
			// definition of sets and contexts of microarrays (phenotypes)
			// set information is parsed separately by pasrseSetInformation();
		} else { // regular data line, each for a marker
			processValueFields(fields);
		}
	}

	@SuppressWarnings("unchecked")
	private void processHeaderFields(String[] fields) {
		List<String> a = new ArrayList<String>();
		for(int i=2; i<fields.length; i++) {
			if(fields[i].trim().length()==0) break;
			a.add(fields[i]);
		}
		arrayNumberTemp = a.size();
		arrayLabelsTemp = a.toArray(new String[0]);
		markerLabelList = new ArrayList<String>();
		valueColumns = new ArrayList[arrayNumberTemp];
		for(int i=0; i<arrayNumberTemp; i++) {
			valueColumns[i] = new ArrayList<Float>();
		}
	}
	
	private void processValueFields(String[] fields) throws InputFileFormatException {
		//String description = fields[1]; // description. ignored on purpose.
		markerLabelList.add(fields[0]); // temporary storage for markerLabels

		// This handles individual gene lines with (value, pvalue) pairs
		// separated by tabs
		if(markerCounter==0) { // only do this for the first line
			if(fields.length-2 == arrayLabelsTemp.length) {
				pValueExists = false;
			} else if(fields.length-2 == arrayLabelsTemp.length*2) {
				pValueExists = true;
				confidenceTemp = new ArrayList<float[]>();
			} else {
				throw new InputFileFormatException("Field number (first line) is incorrect.");
			}
		} else {
			if(fields.length-2 == arrayLabelsTemp.length ) {
				if(pValueExists)
					throw new InputFileFormatException("Value field number is not double the header field number for the case of p-value existing.");
			} else if(fields.length-2 == arrayLabelsTemp.length*2 ) {
				if(!pValueExists)
					throw new InputFileFormatException("Value field number is double the header field number for the case of no p-value.");
			} else {
				throw new InputFileFormatException("Field number is incorrect.");
			}
		}
		int arrayIndex = 0;
		int step = 1;
		if(pValueExists) {
			step = 2;
		}
		float[] confidence = new float[arrayLabelsTemp.length];
		for(int i=2; i<fields.length; i += step) {

			String value = fields[i];
			if (pValueExists) {
				String status = fields[i+1]; // p-value or letter status
				confidence[arrayIndex] = statusToNumeric(status);
			}

			ArrayList<Float> valueArray = valueColumns[arrayIndex];
			valueArray.add(new Float(value));
			arrayIndex++;
		}
		if(pValueExists) {
			confidenceTemp.add(confidence);
		}

		markerCounter++;
	}
	
	/* Parse set information from one line. Ignore other content of the file. */
	private void parseSetLine(String line) throws InputFileFormatException {

		if (line.charAt(0) == '#') { // skip comment
			return;
		}

		int startindx = line.indexOf('\t');
		if (startindx <= 0)
			return;

		if (line.substring(0, 11).equalsIgnoreCase("Description")) {
			// definition of sets and contexts of microarrays (phenotypes)
			String[] f = line.split("\t", -1);
			String[] setNames = new String[(arrayNumberTemp)];
			for (int i = 0; i < Math.min(setNames.length, f.length - 2); i++) {
				setNames[i] = f[i + 2];
			}
			arrayInfo.put(f[1], setNames);
		}
	}
	
	/* Convert the status/p-value/confidence field to the numeric representation of 'confidence'. */
	/* The rules for this is still odd even after I removed the stuff that is unused or incorrect. */
	private static float statusToNumeric(String status) {
		final float MISSING = 1000;
		final float p_threshold = (float) 0.04;
		final float m_threshold = (float) 0.06;

		float confidence = MISSING;
		char c = status.charAt(0);
		if (Character.isLetter(c)) {
			try {
				if (Character.isLowerCase(c)) {
					if (confidence > 0) {
						confidence *= -1.0;
					}
				}
				switch (Character.toUpperCase(c)) {
				case 'P':
					if (confidence < 0.0)
						confidence = -(float) (p_threshold - 0.00005);
					else
						confidence = (float) (p_threshold - 0.00005);
					break;
				case 'A':
					if (confidence < 0.0)
						confidence = -(float) m_threshold;
					else
						confidence = (float) m_threshold;
					break;
				case 'M':
					if (confidence < 0.0)
						confidence = -(float) (p_threshold);
					else
						confidence = (float) (p_threshold);
					break;
				default:
					confidence = MISSING;
					break;
				}
			} catch (NumberFormatException e) {
				confidence = MISSING;
			}
		} else {
			try {
				confidence = Float.parseFloat(status);
			} catch (NumberFormatException e) {
				confidence = MISSING;
			}
		}

		return confidence;
	}

}