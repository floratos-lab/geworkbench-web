package org.geworkbenchweb.plugins.cnkb;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.utils.CSVUtil;
import org.geworkbenchweb.utils.UserDirUtils;

import com.vaadin.ui.AbstractOrderedLayout;

// TODO the UI elements are only here to stick to existing interface AnalysisUI. 
// only the computational part is really needed in this case
// TODO input dataset for execute(...) is ignored. that is also due to a quick conversion to the interface
public class NetworkCreation extends AbstractOrderedLayout implements
		AnalysisUI {

	private static final long serialVersionUID = 1L;

	@Override
	public void setDataSetId(Long dataId) {
		// no-op
	}

	@Override
	public Class<?> getResultType() {
		return AdjacencyMatrixDataSet.class;
	}

	 
	/* the method signature is interface defined. this class in fact ignores datasetId and userId*/
	@Override
	public String execute(Long resultId, Long datasetId,
			HashMap<Serializable, Serializable> params, Long userId) throws IOException {
		Vector<CellularNetWorkElementInformation> hits = null;
		CNKBResultSet resultSet = null;
		Short confidentType = null;
		if (params.get(CNKBParameters.CNKB_RESULTSET) != null)
		{	
			resultSet = (CNKBResultSet) params
					.get(CNKBParameters.CNKB_RESULTSET);
		    hits = resultSet.getCellularNetWorkElementInformations();
		    confidentType = resultSet.getCellularNetworkPreference().getSelectedConfidenceType();
		}
		AdjacencyMatrixDataSet adjacencyMatrixdataSet = null;
		AdjacencyMatrix matrix = new AdjacencyMatrix(null);

		List<String> selectedTypes = resultSet.getCellularNetworkPreference().getDisplaySelectedInteractionTypes();
				 
		Map<String, String> map = CSVUtil.getAnnotationMap(datasetId);
		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {

			ArrayList<InteractionDetail> arrayList = cellularNetWorkElementInformation
					.getSelectedInteractions(selectedTypes, confidentType);

			String markerLabel = cellularNetWorkElementInformation.getMarkerLabel();
			String geneSymbol = map.get(markerLabel);
			String geneName = markerLabel;
			if(geneSymbol!=null) geneName = geneSymbol;
			AdjacencyMatrix.Node node1 = new AdjacencyMatrix.Node(
					NodeType.GENE_SYMBOL, geneName);

			for (InteractionDetail interactionDetail : arrayList) {

				String mid2 = interactionDetail.getdSGeneId();
				String mName2 = interactionDetail.getdSGeneName();
				AdjacencyMatrix.Node node2 = null;

				if (mName2 != null && !mName2.trim().equals(""))
					node2 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL,
							mName2);
				else {
					node2 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL, mid2);
				}

				matrix.add(
						node1,
						node2,
						new Float(interactionDetail
								.getConfidenceValue(interactionDetail
										.getConfidenceTypes().get(0))));
			}
		} // end for loop

		adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(matrix, 1,
				"Adjacency Matrix", "CNKB Interactions", null);

		UserDirUtils.serializeResultSet(resultId, adjacencyMatrixdataSet);
		return "Cytoscape";
	}

	@Override
	public String execute(Long resultId, DSDataSet<?> dummy,
			HashMap<Serializable, Serializable> params) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
