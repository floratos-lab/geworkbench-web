package org.geworkbenchweb.test;

import java.io.File;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbenchweb.dataset.MicroarraySetConverter;

import junit.framework.TestCase;

public class ExpParserTest extends TestCase {
	
	public void test1() throws Exception {
		MicroarraySetConverter converter = new MicroarraySetConverter();
		DSMicroarraySet mset = converter.parseAsDSMicroarraySet(new File("target/test-classes/Bcell-100.exp"));
		int size = mset.size();
		DSItemList<DSGeneMarker> markers = mset.getMarkers();
		DSGeneMarker marker0 = markers.get(0);
		System.out.println("microarray size "+size);
		System.out.println("markers size "+markers.size());
		System.out.println("marker0 gene name "+marker0.getGeneName());
		DSMicroarray firstArray = mset.get(0);
		System.out.println("first array label "+firstArray.getLabel());
		System.out.println("first array first marker value "+firstArray.getMarkerValue(marker0).getValue());
	}
}
