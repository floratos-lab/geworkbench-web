package org.geworkbenchweb.plugins.msviper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter; 
import java.util.HashSet;  
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbenchweb.GeworkbenchRoot; 
import org.geworkbenchweb.pojos.Network;
import org.geworkbenchweb.pojos.NetworkEdges;

import com.vaadin.ui.ProgressIndicator;

public class NetworkCreator {
	private Log log = LogFactory.getLog(NetworkCreator.class);

	private final MsViperUI ui;
 
	public static final String SIF_FORMART = "sif format";
	public static final String ADJ_FORMART = "adj format";
	public static final String GENE_NAME = "gene name";
	public static final String ENTREZ_ID = "entrez id";
	public static final String PROBESET_ID = "probeset id";

	public NetworkCreator(MsViperUI ui) {
		this.ui = ui;
		 
		this.indicator = null;
	}

	public NetworkCreator(MsViperUI ui, ProgressIndicator indicator) {
		this.ui = ui;
		 
		this.indicator = indicator;
	}

	private static AdjacencyMatrix.Node token2node(String token,
			final String selectedRepresentedBy) {
		 
		    AdjacencyMatrix.Node node = null;
 
			if (selectedRepresentedBy.equals(GENE_NAME))
				node = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL, token);
			else if (selectedRepresentedBy.equals(PROBESET_ID))
			    node = new AdjacencyMatrix.Node(NodeType.PROBESET_ID, token);
			else  if (selectedRepresentedBy.equals(AdjacencyMatrixDataSet.ENTREZ_ID))
				node = new AdjacencyMatrix.Node(NodeType.NUMERIC, token);
		   else  
				node = new AdjacencyMatrix.Node(NodeType.OTHER, token);
		 
		return node;
	}

	

	public AdjacencyMatrix parseAdjacencyMatrix(String networkFile,
			Map<String, String> interactionTypeSifMap, String format,
			String selectedRepresentedBy)
			throws InputFileFormatException {

		AdjacencyMatrix matrix = new AdjacencyMatrix(ui.param.getNetwork(),
				interactionTypeSifMap);

		//Map<String, String> map = getAnnotationMap(selectedRepresentedBy, dataSetId);
		
		Set<String> hubSet = new HashSet<String>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(networkFile));
			String line = br.readLine();

			while (line != null) {
				// skip comments
				if (line.trim().equals("") || line.startsWith(">")
						|| line.startsWith("-")) {
					line = br.readLine();
					continue;
				}

				StringTokenizer tr = new StringTokenizer(line, "\t");
                String hub = tr.nextToken().trim();
                hubSet.add(hub);
				AdjacencyMatrix.Node node = token2node(hub,
						selectedRepresentedBy);
				if (node == null) {
					line = br.readLine();
					continue; // skip it when we don't have it
				}

				String interactionType = null;
				if (format.equals(SIF_FORMART) && tr.hasMoreTokens())
					interactionType = tr.nextToken().toLowerCase();
				while (tr.hasMoreTokens()) {

					String strGeneId2 = tr.nextToken();
					AdjacencyMatrix.Node node2 = token2node(strGeneId2,
							selectedRepresentedBy);
					if (node2 == null)
						continue; // skip it when we don't have it

					float mi = 0.8f;
					if (format.equals(ADJ_FORMART)) {
						if (!tr.hasMoreTokens())
							throw new InputFileFormatException(
									"invalid format around " + strGeneId2);
						mi = Float.parseFloat(tr.nextToken());
					}

					matrix.add(node, node2, mi, interactionType);
				} // end of the token loop for one line

				line = br.readLine();
			} // end of reading while loop
			
			if (hubSet.size() <2)
				throw new InputFileFormatException("There is only one hub in the network, MsViper can not process it.");
			
		} catch (NumberFormatException ex) {
			throw new InputFileFormatException(ex.getMessage());
		} catch (Exception e) {
			throw new InputFileFormatException(e.getMessage());
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return matrix;
	}

	private final ProgressIndicator indicator;
	private int progress = 0;

	public void updateProgress(int np) {
		progress = np;
		log.info("progress=" + progress + "%");
		if (indicator != null) { /*
								 * For now, it is allowed to have null
								 * ProgressIndicator.
								 */
			indicator.setValue(new Float(progress * 0.01));
		}
	}

	 
 
	public void createNetworkFile(Network network, String networkName)
			throws IOException {
		String dirName = GeworkbenchRoot.getBackendDataDirectory()
				+ File.separator + "networks" + File.separator + "msViper"
				+ File.separator + ui.userId;
		File dir = new File(dirName);
		if (!dir.exists())
			dir.mkdirs();

		PrintWriter pw = new PrintWriter(new FileWriter(dirName
				+ File.separator + networkName));
 
		String[] node1s = network.getNode1();
		NetworkEdges[] allEdges = network.getEdges();		 
		for (int index = 0; index < node1s.length; index++) {
			String marker1 = node1s[index];
			NetworkEdges edges = allEdges[index];
			String[] node2s = edges.getNode2s();
			if (node2s == null || node2s.length == 0)
				continue;			 
			pw.print(marker1);			
			double[] weights = edges.getWeights();
			for (int index2 = 0; index2 < node2s.length; index2++) {
				String marker2 = node2s[index2];
				pw.print("\t" + marker2 + "\t" + weights[index2]); // Mutual
																	// information
			}
			pw.print("\n");

		}		
		pw.close();
		return;
	}

	 
}
