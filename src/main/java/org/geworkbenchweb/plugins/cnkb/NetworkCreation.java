package org.geworkbenchweb.plugins.cnkb;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.Network;
import org.geworkbenchweb.pojos.NetworkEdges;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.CNKBResultSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.AbstractOrderedLayout;

// TODO the UI elements are only here to stick to existing interface AnalysisUI. 
// only the computational part is really needed in this case
// TODO input dataset for execute(...) is ignored. that is also due to a quick conversion to the interface
public class NetworkCreation extends AbstractOrderedLayout implements
		AnalysisUI {

	private static final long serialVersionUID = 1L;
	private final Long datasetId;

	public NetworkCreation(Long parentId) {
		datasetId = parentId;
	}

	@Override
	public void setDataSetId(Long dataId) {
		// no-op
	}

	@Override
	public Class<?> getResultType() {
		return Network.class;
	}

	/*
	 * the method signature is interface defined. this class in fact ignores
	 * datasetId and userId
	 */
	@Override
	public String execute(Long resultId,
			HashMap<Serializable, Serializable> params, Long userId)
			throws IOException {
		Vector<CellularNetWorkElementInformation> hits = null;
		CNKBResultSet resultSet = null;
		Short confidentType = null;
		if (params.get(CNKBParameters.CNKB_RESULTSET) != null) {
			resultSet = (CNKBResultSet) params
					.get(CNKBParameters.CNKB_RESULTSET);
			hits = resultSet.getCellularNetWorkElementInformations();
			confidentType = resultSet.getCellularNetworkPreference()
					.getSelectedConfidenceType();
		}
		Map<String, NetworkEdges> networkMap = new HashMap<String, NetworkEdges>();

		List<String> selectedTypes = resultSet.getCellularNetworkPreference()
				.getDisplaySelectedInteractionTypes();

		Map<String, String> map = DataSetOperations.getAnnotationMap(datasetId);
		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {

			ArrayList<InteractionDetail> arrayList = cellularNetWorkElementInformation
					.getSelectedInteractions(selectedTypes, confidentType);

			String markerLabel = cellularNetWorkElementInformation
					.getMarkerLabel();
			String geneSymbol = map.get(markerLabel);
			String geneName = markerLabel;
			if (geneSymbol != null)
				geneName = geneSymbol;

			List<String> node2s = new ArrayList<String>();
			List<Double> weights = new ArrayList<Double>();

			for (InteractionDetail interactionDetail : arrayList) {
				List<InteractionParticipant> participants = interactionDetail
						.getParticipantList();
				for (InteractionParticipant p : participants) {
					String mid2 = p.getGeneId();
					String mName2 = p.getGeneName();
					String node2 = null;

					if (mName2 != null && !mName2.trim().equals("")
							&& !mName2.equals("null"))
						node2 = mName2;
					else {
						node2 = mid2;
					}

					double weight = interactionDetail
							.getConfidenceValue(interactionDetail
									.getConfidenceTypes().get(0));
					node2s.add(node2);
					weights.add(weight);
				}				
				
			}
			networkMap.put(geneName, new NetworkEdges(node2s, weights));			
			
			
		} // end for loop

		Network network = new Network(networkMap);
		FacadeFactory.getFacade().store(network);
		ResultSet networkResult = FacadeFactory.getFacade().find(
				ResultSet.class, resultId);
		networkResult.setDataId(network.getId());
		FacadeFactory.getFacade().store(networkResult);

		return "Cytoscape";
	}
}
