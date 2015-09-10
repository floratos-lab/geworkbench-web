package org.geworkbenchweb.plugins.cnkb;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.TreeMap;

public class ResultSetlUtil {

	public ResultSetlUtil(BufferedReader in) throws IOException {
		this.in = in;
		metaMap = new TreeMap<String, Integer>();

		// metadata
		next();

		processMetadata();
	}

	public boolean next() throws IOException {
		boolean ret = false;
		String decodedString = in.readLine();

		if (decodedString != null && !decodedString.trim().equals("")) {
			row = decodedString.split(REGEX_DEL, SPLIT_ALL);
			ret = true;
		}

		return ret;
	}
	
	public String getString(String colmName) {
		int coluNum = getColumNum(colmName);
		if (coluNum == -1)
			return null;
		return getString(coluNum);
	}
	
	public double getDouble(String colmName) {
		int columNum = getColumNum(colmName);

		double ret = 0;

		String tmp = getString(columNum).trim();

		if (!tmp.equals(NULL_STR)) {
			ret = Double.valueOf(tmp).doubleValue();
		}

		return ret;
	}

	public void close() throws IOException {
		in.close();
	}

	private static final String REGEX_DEL = "\\|";
	private static final int SPLIT_ALL = -2;
	private static final String NULL_STR = "null";

	private TreeMap<String, Integer> metaMap;
	private String[] row;
	private BufferedReader in;

	// reconstruct metadata
	private void processMetadata() {
		if (row == null)
			return;
		for (int i = 0; i < row.length; i++) {
			metaMap.put(row[i], new Integer(i + 1));
		}
		return;
	}
	
	private int getColumNum(String name) {
		Integer ret = metaMap.get(name);
		if (ret != null)
			return ret.intValue();
		else
			return -1;
	}
	
	private String getString(int colmNum) {
		// get from row
		return row[colmNum - 1];
	}
}