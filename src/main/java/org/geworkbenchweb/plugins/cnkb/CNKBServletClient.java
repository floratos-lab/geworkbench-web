package org.geworkbenchweb.plugins.cnkb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import sun.misc.BASE64Encoder;

/* This is based on InteractionsConnectionImpl in CNKB (interactions) component
 * to fix the issue of dependency on 'current dataset' */
/**
 * The class to query CNKB database via servlet.
 * 
 */
public class CNKBServletClient {
	private static Log log = LogFactory.getLog(CNKBServletClient.class);
	
	private static final Log logger = LogFactory.getLog(CNKBServletClient.class);

	private static final String ENTREZ_GENE = "Entrez Gene";

	private static class Constants {
		static String DEL = "|";
	};
	
	private static final String CNKB_SERVLET_URL = "http://cagridnode.c2b2.columbia.edu:8080/cknb/InteractionsServlet_new/InteractionsServlet";
	private static final int TIMEOUT = 3000;
	
	private final String cnkbServletUrl;

	public CNKBServletClient() {
		cnkbServletUrl = CNKB_SERVLET_URL;
	}
	
	/**
	 * This was originally developed to retain all the 'edges' of the
	 * interactions that include the queried marker even if the edges are not
	 * connected to the queried marker. That behavior is no longer supported in
	 * the current implementation.
	 */
	public List<InteractionDetail> getInteractionsByEntrezIdOrGeneSymbol_2(
			String geneId, String geneSymbol, String context, String version,
			String userInfo) throws UnAuthenticatedException, ConnectException,
			SocketTimeoutException, IOException {

		List<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();

		String marker_msid = geneId;
		String marker_geneName = geneSymbol;

		String methodAndParams = "getInteractionsByEntrezIdOrGeneSymbol"
				+ Constants.DEL + marker_msid + Constants.DEL + marker_geneName
				+ Constants.DEL + context + Constants.DEL + version;

		ResultSetlUtil rs = executeQuery(methodAndParams, cnkbServletUrl, userInfo);

		String previousInteractionId = null;
		boolean firstHitOnQueryGene = true;
		InteractionDetail interactionDetail = null;
		while (rs.next()) {
			try {
				String msid2 = rs.getString("primary_accession");
				String geneName2 = rs.getString("gene_symbol");

				String db2_xref = rs.getString("accession_db");
				String interactionType = rs.getString("interaction_type")
						.trim();
				String interactionId = rs.getString("interaction_id");              
				Short evidenceId = 0;
				if (rs.getString("evidence_id") != null
						&& !rs.getString("evidence_id").trim().equals("null")) {
					evidenceId = new Short(rs.getString("evidence_id"));
				}
				if (!db2_xref.equalsIgnoreCase(ENTREZ_GENE)
						&& marker_geneName.equals(geneName2)) {
					msid2 = marker_msid;
				}

				if (previousInteractionId == null
						|| !previousInteractionId.equals(interactionId)) {
					if (interactionDetail != null) {
						arrayList.add(interactionDetail);
						interactionDetail = null;
					}
					previousInteractionId = interactionId;
					firstHitOnQueryGene = true;

				}
				if ((db2_xref.equals(ENTREZ_GENE) && marker_msid.equals(msid2))
						|| (geneName2 != null && marker_geneName
								.equalsIgnoreCase(geneName2))) {
					if (firstHitOnQueryGene == true) {
						firstHitOnQueryGene = false;
						continue;
					}
				}

				if (interactionDetail == null) {
					interactionDetail = new InteractionDetail(
							new InteractionParticipant(msid2, geneName2),
							interactionType, evidenceId);
					double confidenceValue = 1.0;
					try {
						confidenceValue = rs.getDouble("confidence_value");
					} catch (NumberFormatException nfe) {
						logger.info("there is no confidence value for this row. Default it to 1.");
					}
					short confidenceType = 0;
					try {
						confidenceType = new Short(rs.getString(
								"confidence_type").trim());
					} catch (NumberFormatException nfe) {
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
				} else {
					interactionDetail
							.addParticipant(new InteractionParticipant(msid2,
									geneName2));
				}

			} catch (NullPointerException npe) {
				logger.error("db row is dropped because a NullPointerException");

			} catch (NumberFormatException nfe) {
				logger.error("db row is dropped because a NumberFormatExceptio");
			}
		}
		
		if (interactionDetail != null) {
			arrayList.add(interactionDetail);
			interactionDetail = null;
		}
		rs.close();

		return arrayList;
	}
	
	public List<InteractionDetail> getInteractionsByGeneSymbol(String geneSymbol, String context, String version,
			String userInfo) throws UnAuthenticatedException, ConnectException, SocketTimeoutException, IOException {

		List<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();

		String marker_geneName = geneSymbol;

		String methodAndParams = "getInteractionsByGeneSymbol" + Constants.DEL + marker_geneName + Constants.DEL
				+ context + Constants.DEL + version;

		ResultSetlUtil rs = executeQuery(methodAndParams, cnkbServletUrl, userInfo);

		String previousInteractionId = null;
		boolean firstHitOnQueryGene = true;
		InteractionDetail interactionDetail = null;
		while (rs.next()) {
			try {
				String msid2 = rs.getString("primary_accession");
				String geneName2 = rs.getString("gene_symbol");

				String interactionType = rs.getString("interaction_type").trim();
				String interactionId = rs.getString("interaction_id");
				Short evidenceId = 0;
				if (rs.getString("evidence_id") != null && !rs.getString("evidence_id").trim().equals("null")) {
					evidenceId = new Short(rs.getString("evidence_id"));
				}

				if (previousInteractionId == null || !previousInteractionId.equals(interactionId)) {
					if (interactionDetail != null) {
						arrayList.add(interactionDetail);
						interactionDetail = null;
					}
					previousInteractionId = interactionId;
					firstHitOnQueryGene = true;

				}
				if ((geneName2 != null && marker_geneName.equalsIgnoreCase(geneName2))) {
					if (firstHitOnQueryGene == true) {
						firstHitOnQueryGene = false;
						continue;
					}
				}

				if (interactionDetail == null) {
					interactionDetail = new InteractionDetail(new InteractionParticipant(msid2, geneName2),
							interactionType, evidenceId);
					double confidenceValue = 1.0;
					try {
						confidenceValue = rs.getDouble("confidence_value");
					} catch (NumberFormatException nfe) {
						logger.info("there is no confidence value for this row. Default it to 1.");
					}
					short confidenceType = 0;
					try {
						confidenceType = new Short(rs.getString("confidence_type").trim());
					} catch (NumberFormatException nfe) {
						logger.info("there is no confidence value for this row. Default it to 0.");
					}
					interactionDetail.addConfidence(confidenceValue, confidenceType);
					String otherConfidenceValues = rs.getString("other_confidence_values");
					String otherConfidenceTypes = rs.getString("other_confidence_types");
					if (!otherConfidenceValues.equals("null")) {
						String[] values = otherConfidenceValues.split(";");
						String[] types = otherConfidenceTypes.split(";");

						for (int i = 0; i < values.length; i++)
							interactionDetail.addConfidence(new Double(values[i]), new Short(types[i]));

					}
				} else {
					interactionDetail.addParticipant(new InteractionParticipant(msid2, geneName2));
				}

			} catch (NullPointerException npe) {
				logger.error("db row is dropped because a NullPointerException");

			} catch (NumberFormatException nfe) {
				logger.error("db row is dropped because a NumberFormatExceptio");
			}
		}

		if (interactionDetail != null) {
			arrayList.add(interactionDetail);
			interactionDetail = null;
		}
		rs.close();

		return arrayList;
	}

	public HashMap<String, String> getConfidenceTypeMap()
			throws ConnectException, SocketTimeoutException, IOException,
			UnAuthenticatedException {
		HashMap<String, String> map = new HashMap<String, String>();

		String methodAndParams = "getConfidenceTypes";
		ResultSetlUtil rs = executeQuery(methodAndParams, cnkbServletUrl);

		while (rs.next()) {

			String confidenceType = rs.getString("name").trim();
			String id = rs.getString("id").trim();

			map.put(confidenceType, id);
			map.put(id, confidenceType);
		}
		rs.close();

		return map;
	}

	public List<String> getInteractionTypesByInteractomeVersion(String context,
			String version) throws ConnectException, SocketTimeoutException,
			IOException, UnAuthenticatedException {
		List<String> arrayList = new ArrayList<String>();

		String methodAndParams = "getInteractionTypesByInteractomeVersion"
				+ Constants.DEL + context + Constants.DEL + version;
		ResultSetlUtil rs = executeQuery(methodAndParams, cnkbServletUrl);

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
		ResultSetlUtil rs = executeQuery(methodAndParams, cnkbServletUrl);
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
		ResultSetlUtil rs = executeQuery(methodAndParams, cnkbServletUrl);

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
		ResultSetlUtil rs = executeQuery(methodAndParams, cnkbServletUrl);
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

	/** Get the map from the long name of interaction type to the short name. */
	public Map<String, String> getInteractionTypeMap()
			throws ConnectException, SocketTimeoutException, IOException, UnAuthenticatedException {
		Map<String, String> map = new HashMap<String, String>();

		String methodAndParams = "getInteractionTypes";
		ResultSetlUtil rs = executeQuery(methodAndParams, cnkbServletUrl);

		while (rs.next()) {

			String interactionType = rs.getString("interaction_type").trim();
			String short_name = rs.getString("short_name").trim();

			map.put(interactionType, short_name);
		}
		rs.close();

		return map;
	}

	private static ResultSetlUtil executeQuery(String methodAndParams,
			String urlStr) throws IOException, UnAuthenticatedException {
	    return executeQuery(methodAndParams, urlStr, null);
	}

	private static ResultSetlUtil executeQuery(String methodAndParams,
			String urlStr, String userInfo) throws IOException, UnAuthenticatedException {
		log.debug(methodAndParams);
		log.debug(urlStr);
		log.debug(userInfo);
		
		URL aURL = new URL(urlStr);
		HttpURLConnection aConnection = (HttpURLConnection) (aURL
				.openConnection());
		aConnection.setDoOutput(true);
		aConnection.setConnectTimeout(TIMEOUT);

		if (userInfo != null && userInfo.trim().length() != 0) {
			BASE64Encoder encoder = new BASE64Encoder();
			aConnection.setRequestProperty("Authorization", "Basic " + encoder.encode(userInfo.getBytes()));
		}
		OutputStreamWriter out = new OutputStreamWriter(aConnection.getOutputStream());

		out.write(methodAndParams);
		out.close();

		int respCode = aConnection.getResponseCode();

		if (respCode == HttpURLConnection.HTTP_UNAUTHORIZED)
			throw new UnAuthenticatedException("server response code = " + respCode);

		if ((respCode == HttpURLConnection.HTTP_BAD_REQUEST) || (respCode == HttpURLConnection.HTTP_INTERNAL_ERROR)) {
			throw new IOException("server response code = " + respCode + ", see server logs");
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(aConnection.getInputStream()));

		ResultSetlUtil rs = new ResultSetlUtil(in);

		return rs;
	}
}
