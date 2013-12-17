/**
 * 
 */
package org.geworkbenchweb.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSRangeMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.parsers.InputFileFormatException;

/**
 * A converter to support compatibility to bison type.
 * 
 * @author zji
 * @version $Id$
 * 
 */
public class MicroarraySetConverter {
	private static Log log = LogFactory.getLog(MicroarraySetConverter.class);

	/*
	 * It is recommended to user GeWorkbenchExpFileParser.parse() to create a
	 * clean MicroarraSet. This method is provided to support compatibility.
	 */
	public DSMicroarraySet parseAsDSMicroarraySet(final File expFile)
			throws InputFileFormatException, IOException {
		GeWorkbenchExpFileParser parser = new GeWorkbenchExpFileParser(expFile);
		MicroarraySet cleanMicroaraySet = parser.parse();
		Map<String, String[]> setInformation = parser.parseSetInformation(cleanMicroaraySet.arrayNumber);
		DSMicroarraySet mset = convertToDSMicroarraySet(cleanMicroaraySet,
				setInformation);
		return mset;
	}

	/* Utility function to convert the clean microarray set to the bison type. */
	public DSMicroarraySet convertToDSMicroarraySet(
			MicroarraySet cleanMicroaraySet,
			Map<String, String[]> setInformation)
			throws InputFileFormatException {
		DSMicroarraySet microarraySet = new CSMicroarraySet();

		// TODO when and even whether we should use these fields
		microarraySet.setCompatibilityLabel("COMPATIBILITY LABEL NOT SET");
		microarraySet.setFile(null);
		microarraySet.setLabel("LABEL NOT SET");

		List<DSGeneMarker> markers = new ArrayList<DSGeneMarker>();
		List<DSMarkerValue[]> markerValues = new ArrayList<DSMarkerValue[]>();

		int markerNumber = cleanMicroaraySet.markerNumber;
		int arrayNumber = cleanMicroaraySet.arrayNumber;
		String[] markerLabels = cleanMicroaraySet.markerLabels;
		float[][] values = cleanMicroaraySet.values;
		float[][] confidence = cleanMicroaraySet.confidence;

		for (int i = 0; i < markerNumber; i++) {
			CSExpressionMarker marker = new CSExpressionMarker(i);
			marker.setLabel(markerLabels[i]);
			// marker.setDescription(description); // TODO ?
			marker.getUnigene().set(markerLabels[i]);

			/*
			 * this is correct because CSExpressionMarker equal is based on
			 * label only
			 */
			if (markers.contains(marker)) {
				throw new InputFileFormatException("duplicate probeset names");
			}

			String[] entrezIds = AnnotationParser.getInfo(markerLabels[i],
					AnnotationParser.LOCUSLINK);
			if ((entrezIds != null) && (!entrezIds[0].trim().equals(""))) {
				try {
					marker.setGeneId(Integer.parseInt(entrezIds[0].trim()));
				} catch (NumberFormatException e) {
					log.debug("Invalid locus link for gene " + i);
				}
			}

			String[] geneNames = AnnotationParser.getInfo(markerLabels[i],
					AnnotationParser.ABREV);
			if (geneNames != null) {
				marker.setGeneName(geneNames[0].trim());
			}

			String[] annotations = AnnotationParser.getInfo(markerLabels[i],
					AnnotationParser.DESCRIPTION);
			if (annotations != null) {
				marker.setAnnotation(annotations[0].trim());
			}

			markers.add(marker);

			DSMarkerValue[] valuesPerMarker = new DSMarkerValue[arrayNumber];
			for (int j = 0; j < arrayNumber; j++) {

				CSMarkerValue markerValue = new CSExpressionMarkerValue(
						values[i][j]);
				if (confidence != null) {
					markerValue.setConfidence(confidence[i][j]);
				}
				// markerValue does not really need to be mutable. it is
				// DSRangeMarker's mistake
				((DSRangeMarker) marker).updateRange(markerValue);
				valuesPerMarker[j] = markerValue;
			}

			markerValues.add(valuesPerMarker);
		}

		populateDataset(microarraySet, markers, markerValues,
				cleanMicroaraySet, setInformation);

		return microarraySet;
	}

	private void populateDataset(DSMicroarraySet microarraySet,
			List<DSGeneMarker> markers, List<DSMarkerValue[]> markerValues,
			MicroarraySet cleanMicroaraySet,
			Map<String, String[]> setInformation) {
		// just to make it is clear it is final here
		final int markerCount = cleanMicroaraySet.markerNumber;
		microarraySet.initializeMarkerVector(markerCount); // only way to set
															// marker
		// count
		for (int i = 0; i < cleanMicroaraySet.arrayNumber; i++) {
			microarraySet.add(i, (DSMicroarray) new CSMicroarray(i,
					markerCount, cleanMicroaraySet.arrayLabels[i],
					DSMicroarraySet.expPvalueType));
		}

		setSetContext(setInformation, microarraySet);

		for (int markerIndex = 0; markerIndex < markerCount; markerIndex++) {
			microarraySet.getMarkers().set(markerIndex,
					markers.get(markerIndex));
		}
		if (microarraySet instanceof CSMicroarraySet) {
			((CSMicroarraySet) microarraySet).getMarkers().correctMaps();
		}
		microarraySet.sortMarkers(markerCount);

		int markerIndex = 0;
		for (DSMarkerValue[] markerValue : markerValues) {
			for (int arrayIndex = 0; arrayIndex < cleanMicroaraySet.arrayNumber; arrayIndex++) {
				// missing data should not be set as null
				if (markerValue[arrayIndex] == null)
					continue;

				microarraySet.get(arrayIndex).setMarkerValue(
						microarraySet.getNewMarkerOrder()[markerIndex],
						markerValue[arrayIndex]);
			}
			markerIndex++;
		}
	}

	private void setSetContext(Map<String, String[]> arrayInfo,
			DSMicroarraySet microarraySet) {
		CSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		for (String phLabel : arrayInfo.keySet()) {
			DSAnnotationContext<DSMicroarray> context = manager.getContext(
					microarraySet, phLabel);
			CSAnnotationContext.initializePhenotypeContext(context);
			String[] labels = arrayInfo.get(phLabel);
			for (int arrayIndex = 0; arrayIndex < labels.length; arrayIndex++) {
				if (labels[arrayIndex] == null
						|| labels[arrayIndex].length() == 0)
					continue;

				if (labels[arrayIndex].indexOf("|") > -1) {
					for (String tok : labels[arrayIndex].split("\\|")) {
						context.labelItem(microarraySet.get(arrayIndex), tok);
					}
				} else {
					context.labelItem(microarraySet.get(arrayIndex),
							labels[arrayIndex]);
				}
			}
		}
	}
}
