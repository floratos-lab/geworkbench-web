package org.geworkbenchweb.plugins.citrus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;

public class CitrusDatabase {

	private static Log log = LogFactory.getLog(CitrusDatabase.class);
	
	final private String DB_URL = "jdbc:mysql://" + GeworkbenchRoot.getAppProperty("citrus.db.url") + "/"
			+ GeworkbenchRoot.getAppProperty("citrus.db.database");
	final private String USER = GeworkbenchRoot.getAppProperty("citrus.db.username");
	final private String PASS = GeworkbenchRoot.getAppProperty("citrus.db.password");
	
	private Map<String, Integer> cancerIds = new TreeMap<String, Integer>();
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

	public Set<GeneChoice> getTF(String cancerType) {
		Set<GeneChoice> list = new TreeSet<GeneChoice>();

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();

			String sql = "SELECT count(*), genes.symbol, genes.entrez_id, min(pvalue)"
					+ " FROM associations JOIN genes on genes.entrez_id=associations.gene_id WHERE cancer_type_id="
					+ cancerIds.get(cancerType) + " group by gene_id";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String symbol = rs.getString("symbol");
				int n = rs.getInt("count(*)");
				double p = rs.getDouble("min(pvalue)");
				int id = rs.getInt("entrez_id");
				list.add(new GeneChoice(symbol, n, p, id));
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
		public String eventType;
		public String modulatorSymbol;
		public int eventTypeId;
		public int modulatorId;
		public int preppi;
		public int cindy;
		public float pvalue;
	}

	public static class Viper implements Comparable<Viper> {
		public String sample;
		public int id;
		public float value;

		@Override
		public int compareTo(Viper o) {
			return (int) Math.signum(o.value-value); // sort descending on purpose
		}
	}

	final static double PREPPI_CINDY_THRESHOLD = 0.05;

	public Alteration[] getAlterations(String cancerTypeName, int tf) {
		List<Alteration> list = new ArrayList<Alteration>();

		int cancerTypeId = cancerIds.get(cancerTypeName);
		String sql = "SELECT type, eventtypes.id, genes.symbol, associations.modulator_id, preppi, cindy, pvalue"
				+ " FROM associations JOIN genes on genes.entrez_id=associations.modulator_id"
				+ " JOIN eventtypes on eventtypes.id=associations.event_type_id WHERE cancer_type_id=" + cancerTypeId
				+ " AND gene_id=" + tf;
		log.debug(sql);

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Alteration a = new Alteration();
				a.eventType = rs.getString("type");
				a.modulatorSymbol = rs.getString("genes.symbol");
				a.eventTypeId = rs.getInt("eventtypes.id");
				a.modulatorId = rs.getInt("modulator_id");
				a.preppi = rs.getFloat("preppi") < PREPPI_CINDY_THRESHOLD ? 1 : 0;
				a.cindy = rs.getFloat("cindy") < PREPPI_CINDY_THRESHOLD ? 1 : 0;
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
	
	public Viper[] getViperValues(String cancerTypeName, int tf) {
		List<Viper> list = new ArrayList<Viper>();

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();

			String cancerType = cancerTypes.get(cancerTypeName);
			String tableName = "viper_" + cancerType;
			String sql = "SELECT sample_name, sample.sample_id, " + tableName + ".value FROM " + tableName
					+ " JOIN sample on sample.sample_id=" + tableName + ".sample_id WHERE gene_id=" + tf;
			log.debug(sql);
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Viper v = new Viper();
				v.sample = rs.getString("sample_name");
				v.id = rs.getInt("sample_id");
				v.value = rs.getFloat(tableName + ".value");
				list.add(v);
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
		Collections.sort(list);
		return list.toArray(new Viper[list.size()]);
	}

	// get presence for one genomic event
	private Set<Integer> getPresence(String cancerTypeName, int eventTypeId, int modulatorId) {
		Set<Integer> presence = new HashSet<Integer>();

		int cancerTypeId = cancerIds.get(cancerTypeName);
		String sql = "SELECT sample_id FROM genomic_events WHERE cancer_type_id=" + cancerTypeId + " AND event_type_id="
				+ eventTypeId + " AND modulator_id=" + modulatorId;
		log.debug(sql);

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				presence.add(rs.getInt("sample_id"));
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
		return presence;
	}

	public String[] getPresence(String cancerTypeName, Alteration[] alterations, Viper[] viper) {
		int n = alterations.length;
		int m = viper.length;
		String[] presence = new String[n];
		for (int i = 0; i < n; i++) {
			int eventTypeId = alterations[i].eventTypeId;
			int modulatorId = alterations[i].modulatorId;
			Set<Integer> p = getPresence(cancerTypeName, eventTypeId, modulatorId);
			presence[i] = "";
			for (int j = 0; j < m; j++) {
				int sampleId = viper[j].id;
				if (p.contains(sampleId))
					presence[i] += '1';
				else
					presence[i] += '0';
			}
		}
		return presence;
	}
}
