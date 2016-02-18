/**
 * 
 */
package org.geworkbenchweb.plugins.citrus;

/**
 * @author zji
 *
 */
public class CitrusDatabase {
	// TODO all fake test data for now

	public String[] getCancerTypes() {
		String[] ct = new String[20];
		for (int i = 0; i < ct.length; i++) {
			ct[i] = "";
			for (int j = 0; j < 4; j++) {
				char c = (char) ((Math.random() * 26) + 'a');
				ct[i] += c;
			}
		}
		return ct;
	}

	public String[] getTF(String cancerType) {
		String[] tf = new String[1800];
		for (int i = 0; i < tf.length; i++) {
			tf[i] = "";
			for (int j = 0; j < 3; j++) {
				char c = (char) ((Math.random() * 26) + 'A');
				tf[i] += c;
			}
			tf[i] += cancerType.charAt(3);
		}
		return tf;
	}

	public String[] getAlterations(String cancerType, String tf) {
		int n = 30;
		String[] colorKeys = { "UMT", "DMT", "AMP", "DEL", "SNV", "GFU" };
		String[] alteration = new String[n];
		for (int i = 0; i < n; i++) {
			int randomIndex = (int) (Math.random() * colorKeys.length);
			alteration[i] = colorKeys[randomIndex] + "_m_" + tf;
		}
		return alteration;
	}
}
