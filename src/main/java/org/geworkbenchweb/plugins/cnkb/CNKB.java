package org.geworkbenchweb.plugins.cnkb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.geworkbench.components.interactions.cellularnetwork.InteractionParticipant;
import org.geworkbench.components.interactions.cellularnetwork.VersionDescriptor;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.UnAuthenticatedException;

/* This is based on InteractionsConnectionImpl in CNKB (interactions) component
 * to fix the issue of dependency on 'current dataset' */
/**
 * The class to query CNKB database via servlet.
 * 
 */
public class CNKB {

	private static final Log logger = LogFactory
			.getLog(CNKB.class);

	private static final String ENTREZ_GENE = "Entrez Gene";
	private static class Constants {
		static String DEL = "|";
	};
	
	/**
	 * This is similar to getInteractionsByEntrezIdOrGeneSymbol_2 and currently
	 * NOT used. The difference is that this version only retains the 'edges'
	 * that involve the queried marker, not those that belong to an interaction
	 * that includes the queried marker but does not connect directly.
	 */
	public List<InteractionDetail> getInteractionsByEntrezIdOrGeneSymbol_1(
			String geneId, String geneSymbol, String context, String version, String userInfo)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {

		List<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();

		String msid1 = geneId;
		String geneName1 = geneSymbol;
		String methodAndParams = "getInteractionsByEntrezIdOrGeneSymbol"
				+ Constants.DEL + msid1 + Constants.DEL + geneName1
				+ Constants.DEL + context + Constants.DEL + version;

		ResultSetlUtil rs = ResultSetlUtil.executeQueryWithUserInfo(methodAndParams,
				ResultSetlUtil.getUrl(), userInfo);
		String previousInteractionId = null;
		boolean firstHit = true;
		while (rs.next()) {
			try {
				String msid2 = rs.getString("primary_accession");
				String geneName2 = rs.getString("gene_symbol");
				String db2_xref = rs.getString("accession_db");
				String interactionId = rs.getString("interaction_id");
				if (previousInteractionId == null
						|| !previousInteractionId.equals(interactionId)) {
					previousInteractionId = interactionId;
					firstHit = true;
				}
				if ((db2_xref.equals(ENTREZ_GENE) && msid1.equals(msid2))
						|| (geneName2.equalsIgnoreCase(geneName1))) {
					if (firstHit == true) {
						firstHit = false;
						continue;
					} else {
						msid2 = msid1;
						db2_xref = ENTREZ_GENE;
					}
				}

				String interactionType = rs.getString("interaction_type")
						.trim();
				Short evidenceId = 0;
				if (rs.getString("evidence_id") != null
						&& !rs.getString("evidence_id").trim().equals("null")) {
					evidenceId = new Short(rs.getString("evidence_id"));
				}

				InteractionDetail interactionDetail = new InteractionDetail(
						msid2, geneName2, db2_xref, interactionType,
						interactionId, evidenceId);

				double confidenceValue = rs.getDouble("confidence_value");
				Short confidenceType = new Short(rs
						.getString("confidence_type").trim());
				interactionDetail
						.addConfidence(confidenceValue, confidenceType);
				String otherConfidenceValues = rs
						.getString("other_confidence_values");
				String otherConfidenceTypes = rs
						.getString("other_confidence_types");
				if (!otherConfidenceValues.equals("null")) {
					String[] values = otherConfidenceValues.split(";");
					String[] types = otherConfidenceTypes.split(";");

					for (int i = 0; i < values.length; i++)
						interactionDetail.addConfidence(new Double(values[i]),
								new Short(types[i]));

				}

				arrayList.add(interactionDetail);

			} catch (NullPointerException npe) {
				logger.error("db row is dropped because a NullPointerException");
			}
		}
		rs.close();

		return arrayList;
	}

	/**
	 * This is similar to getInteractionsByEntrezIdOrGeneSymbol_1 and currently
	 * used. The difference is that this version retains all the 'edges' of the
	 * interactions that include the queried marker even if they are not
	 * connected directly.
	 */
	public List<InteractionDetail> getInteractionsByEntrezIdOrGeneSymbol_2(
			String geneId, String geneSymbol, String context, String version, String userInfo)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {

		List<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();

		String marker_msid = geneId;
		String marker_geneName = geneSymbol;

		String methodAndParams = "getInteractionsByEntrezIdOrGeneSymbol"
				+ Constants.DEL + marker_msid + Constants.DEL + marker_geneName
				+ Constants.DEL + context + Constants.DEL + version;

		ResultSetlUtil rs = ResultSetlUtil.executeQueryWithUserInfo(methodAndParams,
				ResultSetlUtil.getUrl(), userInfo);

		String previousInteractionId = null;
		List<InteractionParticipant> participantList = new ArrayList<InteractionParticipant>();
		while (rs.next()) {
			try {
				String msid = rs.getString("primary_accession");
				String geneName = rs.getString("gene_symbol");

				String db_xref = rs.getString("accession_db");
				String interactionType = rs.getString("interaction_type")
						.trim();
				String interactionId = rs.getString("interaction_id");

				Short evidenceId = 0;
				if (rs.getString("evidence_id") != null
						&& !rs.getString("evidence_id").trim().equals("null")) {
					evidenceId = new Short(rs.getString("evidence_id"));
				}
				if (!db_xref.equalsIgnoreCase(ENTREZ_GENE)
						&& geneName.equals(marker_geneName)) {
					msid = marker_msid;
					db_xref = ENTREZ_GENE;
				}

				if (previousInteractionId == null
						|| !previousInteractionId.equals(interactionId)) {
					previousInteractionId = interactionId;
					participantList.clear();
				} else {
					for (InteractionParticipant p : participantList) {
						InteractionDetail interactionDetail = null;

						if ((p.getdSGeneName() != null && p.getdSGeneName()
								.equalsIgnoreCase(marker_geneName))
								|| (p.getdSGeneId() != null && p.getdSGeneId()
										.equals(marker_msid)))
							interactionDetail = new InteractionDetail(msid,
									geneName, db_xref, interactionType,
									interactionId, evidenceId);
						else
							interactionDetail = new InteractionDetail(
									p.getdSGeneId(), p.getdSGeneName(),
									p.getDbSource(), interactionType,
									interactionId, evidenceId);

						double confidenceValue = 1.0;
						try
						{
						   confidenceValue = rs.getDouble("confidence_value");
						}catch(NumberFormatException nfe) {
				           logger.info("there is no confidence value for this row. Default it to 1.");
			            } 
						short confidenceType = 0;
						try {
						    confidenceType = new Short(rs.getString(
								"confidence_type").trim());
						}catch(NumberFormatException nfe) {
				           logger.info("there is no confidence value for this row. Default it to 0.");
			            } 
						interactionDetail.addConfidence(confidenceValue,
								confidenceType);
						String otherConfidenceValues = rs
								.getString("other_confidence_values");
						String otherConfidenceTypes = rs
								.getString("other_confidence_types");
						if (!otherConfidenceValues.equals("null")) {
							String[] values = otherConfidenceValues.split(";");
							String[] types = otherConfidenceTypes.split(";");

							for (int i = 0; i < values.length; i++)
								interactionDetail.addConfidence(new Double(
										values[i]), new Short(types[i]));

						}

						arrayList.add(interactionDetail);

					}
				}

				participantList.add(new InteractionParticipant(msid, geneName,
						db_xref));

			} catch (NullPointerException npe) {
				logger.error("db row is dropped because a NullPointerException");

			} catch (NumberFormatException nfe) {
				logger.error("db row is dropped because a NumberFormatExceptio");
			}
		}
		rs.close();

		return arrayList;
	}

	public List<String> getInteractionsSifFormat(String context,
			String version, String interactionType, String presentBy)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {

		List<String> arrayList = new ArrayList<String>();

		String methodAndParams = "getInteractionsSifFormat" + Constants.DEL
				+ context + Constants.DEL + version + Constants.DEL
				+ interactionType + Constants.DEL + presentBy;
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		String sifLine = null;
		while (rs.next()) {
			try {
				sifLine = rs.getString("sif format data");
				arrayList.add(sifLine);
			} catch (NullPointerException npe) {
				if (logger.isErrorEnabled()) {
					logger.error("db row is dropped because a NullPointerException");
				}
			}
		}
		rs.close();

		return arrayList;
	}

	public List<String> getInteractionsAdjFormat(String context,
			String version, String interactionType, String presentBy)
			throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {

		List<String> arrayList = new ArrayList<String>();

		String methodAndParams = "getInteractionsAdjFormat" + Constants.DEL
				+ context + Constants.DEL + version + Constants.DEL
				+ interactionType + Constants.DEL + presentBy;
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		String adjLine = null;
		while (rs.next()) {
			try {
				adjLine = rs.getString("adj format data");
				arrayList.add(adjLine);
			} catch (NullPointerException npe) {
				if (logger.isErrorEnabled()) {
					logger.error("db row is dropped because a NullPointerException");
				}
			}
		}
		rs.close();

		return arrayList;
	}

	public HashMap<String, String> getInteractionTypeMap()
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {
		HashMap<String, String> map = new HashMap<String, String>();

		String methodAndParams = "getInteractionTypes";
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		while (rs.next()) {

			String interactionType = rs.getString("interaction_type").trim();
			String short_name = rs.getString("short_name").trim();

			map.put(interactionType, short_name);
			map.put(short_name, interactionType);
		}
		rs.close();

		return map;
	}

	public HashMap<String, String> getInteractionEvidenceMap()
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {
		HashMap<String, String> map = new HashMap<String, String>();

		String methodAndParams = "getInteractionEvidences";
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		while (rs.next()) {

			String evidenceDesc = rs.getString("description");
			String evidenceId = rs.getString("id");

			map.put(evidenceId, evidenceDesc);
			map.put(evidenceDesc, evidenceId);
		}
		rs.close();

		return map;
	}

	public HashMap<String, String> getConfidenceTypeMap()
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {
		HashMap<String, String> map = new HashMap<String, String>();

		String methodAndParams = "getConfidenceTypes";
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		while (rs.next()) {

			String confidenceType = rs.getString("name").trim();
			String id = rs.getString("id").trim();

			map.put(confidenceType, id);
			map.put(id, confidenceType);
		}
		rs.close();

		return map;
	}

	public List<String> getInteractionTypes() throws ConnectException,
			SocketTimeoutException, IOException, UnAuthenticatedException {
		List<String> arrayList = new ArrayList<String>();

		String methodAndParams = "getInteractionTypes";
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		while (rs.next()) {

			String interactionType = rs.getString("interaction_type").trim();

			arrayList.add(interactionType);
		}
		rs.close();

		return arrayList;
	}

	public List<String> getInteractionTypesByInteractomeVersion(String context,
			String version) throws ConnectException, SocketTimeoutException,
			IOException, UnAuthenticatedException {
		List<String> arrayList = new ArrayList<String>();

		String methodAndParams = "getInteractionTypesByInteractomeVersion"
				+ Constants.DEL + context + Constants.DEL + version;
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		while (rs.next()) {

			String interactionType = rs.getString("interaction_type").trim();

			arrayList.add(interactionType);
		}
		rs.close();

		return arrayList;
	}

	 

	public String getInteractomeDescription(String interactomeName)
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {

		String interactomeDesc = null;

		String methodAndParams = "getInteractomeDescription" + Constants.DEL
				+ interactomeName;
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());
		while (rs.next()) {
			interactomeDesc = rs.getString("description").trim();
			break;
		}
		rs.close();

		return interactomeDesc;
	}

	public ArrayList<String> getDatasetAndInteractioCount()
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {
		ArrayList<String> arrayList = new ArrayList<String>();

		String datasetName = null;
		int interactionCount = 0;

		String methodAndParams = "getDatasetNames";
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());

		while (rs.next()) {

			datasetName = rs.getString("name").trim();
			interactionCount = (int) rs.getDouble("interaction_count");
			arrayList.add(datasetName + " (" + interactionCount
					+ " interactions)");
		}
		rs.close();

		return arrayList;
	}

	public List<VersionDescriptor> getVersionDescriptor(String interactomeName)
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {
		List<VersionDescriptor> arrayList = new ArrayList<VersionDescriptor>();

		String methodAndParams = "getVersionDescriptor" + Constants.DEL
				+ interactomeName;
		ResultSetlUtil rs = ResultSetlUtil.executeQuery(methodAndParams,
				ResultSetlUtil.getUrl());
		while (rs.next()) {
			String version = rs.getString("version").trim();
			if (version.equalsIgnoreCase("DEL"))
				continue;
			String value = rs.getString("authentication_yn").trim();
			boolean needAuthentication = false;
			if (value.equalsIgnoreCase("Y")) {
				needAuthentication = true;
			}
			String versionDesc = rs.getString("description").trim();
			VersionDescriptor vd = new VersionDescriptor(version,
					needAuthentication, versionDesc);
			arrayList.add(vd);
		}
		rs.close();

		return arrayList;
	}

	/**
	 * Test the connection. The actual query result is ignored.
	 */
	public static boolean isValidUrl(String urlStr) {

		try {
			ResultSetlUtil rs = ResultSetlUtil.executeQuery("getDatasetNames",
					urlStr);
			rs.close();
			return true;
		} catch (java.net.ConnectException ce) {
			logger.error(ce.getMessage());
			return false;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}

	}

}
