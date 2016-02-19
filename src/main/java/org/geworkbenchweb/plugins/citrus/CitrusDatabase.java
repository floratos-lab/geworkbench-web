/**
 * 
 */
package org.geworkbenchweb.plugins.citrus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.geworkbenchweb.GeworkbenchRoot;

/**
 * @author zji
 *
 */
public class CitrusDatabase {

	final private String DB_URL = "jdbc:mysql://" + GeworkbenchRoot.getAppProperty("citrus.db.url") + "/"
			+ GeworkbenchRoot.getAppProperty("citrus.db.database");
	final private String USER = GeworkbenchRoot.getAppProperty("citrus.db.username");
	final private String PASS = GeworkbenchRoot.getAppProperty("citrus.db.password");

	public String[] getCancerTypes() {
		List<String> list = new ArrayList<String>();

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();

			String sql = "SELECT type FROM tumortypes";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String tumortype = rs.getString("type");
				list.add(tumortype);
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // no-op
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
			} // no-op
		}
		return list.toArray(new String[list.size()]);
	}

	// TODO all the following are fake test data for now

	public String[] getTF(String cancerType) {
		String[] tf = new String[1800];
		for (int i = 0; i < tf.length; i++) {
			tf[i] = "";
			for (int j = 0; j < 3; j++) {
				char c = (char) ((Math.random() * 26) + 'A');
				tf[i] += c;
			}
			tf[i] += cancerType;
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

	public String[] getSamples(String cancerType) {
		int m = 100;
		String[] samples = new String[m];
		for (int j = 0; j < m; j++) {
			samples[j] = "";
			for (int k = 0; k < 10; k++) {
				char c = (char) ('A' + Math.random() * 26);
				samples[j] += c;
			}
		}
		return samples;
	}

	public String[] getPresence(String cancerType, String geneSymbol) {
		int n = 30, m = 100;
		String[] presence = new String[n];
		for (int i = 0; i < n; i++) {
			presence[i] = "";
			double r = Math.random();
			for (int j = 0; j < m; j++) {
				char p = '_';
				double x = Math.random();
				if (x > r)
					p = '1';
				else
					p = '0';
				presence[i] += p;
			}
		}
		return presence;
	}

	public Integer[] getPrePPI(String cancerType, String geneSymbol) {
		int n = 30;
		Integer[] preppi = new Integer[n];
		for (int i = 0; i < n; i++) {
			preppi[i] = (int) (Math.random() * 2);
		}
		return preppi;
	}

	public Integer[] getCINDy(String cancerType, String geneSymbol) {
		int n = 30;
		Integer[] cindy = new Integer[n];
		for (int i = 0; i < n; i++) {
			cindy[i] = (int) (Math.random() * 2);
		}
		return cindy;
	}

	public String[] getPValue(String cancerType, String geneSymbol) {
		int n = 30;
		String[] pvalue = new String[n];
		for (int i = 0; i < n; i++) {
			pvalue[i] = String.valueOf(Math.random());
		}
		return pvalue;
	}

	public String[] getNES(String cancerType, String geneSymbol) {
		int m = 100;
		String[] nes = new String[m];
		for (int i = 0; i < m; i++) {
			nes[i] = String.valueOf(Math.random() * 2. - 1.);
		}
		return nes;
	}
}
