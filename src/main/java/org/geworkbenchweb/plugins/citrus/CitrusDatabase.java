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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;

/**
 * @author zji
 *
 */
public class CitrusDatabase {

	private static Log log = LogFactory.getLog(CitrusDatabase.class);
	
	final private String DB_URL = "jdbc:mysql://" + GeworkbenchRoot.getAppProperty("citrus.db.url") + "/"
			+ GeworkbenchRoot.getAppProperty("citrus.db.database");
	final private String USER = GeworkbenchRoot.getAppProperty("citrus.db.username");
	final private String PASS = GeworkbenchRoot.getAppProperty("citrus.db.password");
	
	private Map<String, Integer> cancerIds = new HashMap<String, Integer>();
	private Map<String, String> cancerTypes = new HashMap<String, String>();
	
	public CitrusDatabase() throws Exception {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new Exception("Failure loading MySQL driver");
		}
		
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();

			String sql = "SELECT id, type, name FROM tumortypes";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String tumortype = rs.getString("type");
				String name = rs.getString("name");
				Integer id = rs.getInt("id");
				cancerIds.put(name, id);
				cancerTypes.put(name, tumortype);
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			se.printStackTrace();
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
	}

	public String[] getCancerTypes() {
		return cancerIds.keySet().toArray(new String[cancerIds.size()]);
	}

	public Map<String, Integer> getTF(String cancerType) {
		Map<String, Integer> list = new HashMap<String, Integer>();

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();

			String sql = "SELECT entrez_id, symbol FROM genes JOIN cancergene ON cancergene.gene_id=genes.entrez_id WHERE cancergene.cancer_type="
					+ cancerIds.get(cancerType);
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				list.put(rs.getString("symbol"), rs.getInt("entrez_id"));
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			se.printStackTrace();
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
		return list;
	}
	
	public static class Alteration {
		public String label;
		public int preppi;
		public int cindy;
		public float pvalue;
	}
	
	public Alteration[] getAlterations(String cancerTypeName, int tf, float pvalue) {
		List<Alteration> list = new ArrayList<Alteration>();

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();

			String cancerType = cancerTypes.get(cancerTypeName);
			String tableA = "association_" + cancerType;
			String sql = "SELECT type, " + tableA + ".modulator_id, preppi, cindy, pvalue FROM " + tableA
					+ " JOIN eventtypes on eventtypes.id=" + tableA + ".event_type_id WHERE gene_id=" + tf + " AND "
					+ tableA + ".pvalue<=" + pvalue;
			log.debug(sql);
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Alteration a = new Alteration();
				a.label = rs.getString("type").toUpperCase() + "_" + rs.getString("modulator_id");
				a.preppi = rs.getFloat("preppi")<pvalue?1:0;
				a.cindy = rs.getFloat("cindy")<pvalue?1:0;
				a.pvalue = rs.getFloat("pvalue");
				list.add(a);
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			se.printStackTrace();
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
		return list.toArray(new Alteration[list.size()]);
	}
	
	// TODO all the following are fake test data for now

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

	public String[] getPresence(String cancerType, String geneSymbol, int n) {
		int m = 100;
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

	public String[] getNES(String cancerType, String geneSymbol) {
		int m = 100;
		String[] nes = new String[m];
		for (int i = 0; i < m; i++) {
			nes[i] = String.valueOf(Math.random() * 2. - 1.);
		}
		return nes;
	}
}
