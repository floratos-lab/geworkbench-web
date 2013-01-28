package org.geworkbenchweb.plugins.cnkb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.utils.ObjectConversion;
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

	@SuppressWarnings("unchecked")
	@Override
	public String execute(Long resultId, DSDataSet<?> dummy,
			HashMap<Serializable, Serializable> params) {
		Vector<CellularNetWorkElementInformation> hits = null;

		if (params.get(CNKBParameters.NETWORK_ELEMENT_INFO) != null)
			hits = (Vector<CellularNetWorkElementInformation>) params
					.get(CNKBParameters.NETWORK_ELEMENT_INFO);

		AdjacencyMatrixDataSet adjacencyMatrixdataSet = null;
		AdjacencyMatrix matrix = new AdjacencyMatrix(null);

		List<String> selectedTypes = (ArrayList<String>) params
				.get(CNKBParameters.SELECTED_INTERACTION_TYPES);

		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {

			ArrayList<InteractionDetail> arrayList = cellularNetWorkElementInformation
					.getSelectedInteractions(selectedTypes);

			DSGeneMarker marker1 = cellularNetWorkElementInformation
					.getdSGeneMarker();
			AdjacencyMatrix.Node node1 = new AdjacencyMatrix.Node(
					NodeType.GENE_SYMBOL, marker1.getGeneName());

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

		UserDirUtils.saveResultSet(resultId,
				ObjectConversion.convertToByte(adjacencyMatrixdataSet));
		return "Cytoscape";
	}

}
