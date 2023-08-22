package org.geworkbenchweb.test;

import java.io.File;

import org.geworkbenchweb.dataset.GeWorkbenchExpFileParser;
import org.geworkbenchweb.dataset.MicroarraySet;

import junit.framework.TestCase;

public class ExpParserTest extends TestCase {

	public void test1() throws Exception {
		GeWorkbenchExpFileParser parser = new GeWorkbenchExpFileParser(new File("target/test-classes/Bcell-100.exp"));
		MicroarraySet mset = parser.parse();
		int size = mset.arrayNumber;
		String[] markers = mset.markerLabels;
		String marker0 = markers[0];
		System.out.println("microarray size " + size);
		System.out.println("markers size " + markers.length);
		System.out.println("marker0 gene name " + marker0);
		String firstArray = mset.arrayLabels[0];
		System.out.println("first array label " + firstArray);
		System.out.println("first array first marker value " + mset.values[0][0]);
	}
}
