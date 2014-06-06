package org.geworkbenchweb.plugins.aracne;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.geworkbench.components.aracne.data.AracneGraphEdge;
import org.geworkbench.components.aracne.data.AracneInput;
import org.geworkbench.components.aracne.data.AracneOutput;
import org.geworkbenchweb.pojos.ConfigResult;
import org.geworkbenchweb.pojos.Network;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;

public class AracneAxisClient {
	private static final String aracneNamespace = "http://www.geworkbench.org/service/aracne";
	private boolean config = false;
	String resultName = null;
	
	public AbstractPojo executeAracne(String serviceAddress, AracneInput input,
			File dataFile, List<String> hubGeneList, Map<String, String> map,
			boolean prune, String[] configstr) throws AxisFault {
		config = input.getMode().equals("Preprocessing");
		OMElement aracneRequest = createAxiomRequestElement(input, dataFile, configstr);
		return doWebServiceCallWithAxis(serviceAddress, aracneRequest, hubGeneList, map, prune);
	}

	private OMElement createAxiomRequestElement(AracneInput input, File dataFile, String[] configstr) {

		OMFactory omFactory = OMAbstractFactory.getSOAP11Factory();
		OMNamespace namespace = omFactory.createOMNamespace(aracneNamespace, null);
		String reqstr = config?"ExecuteAracneConfigRequest":"ExecuteAracneRequest";
		OMElement request = omFactory.createOMElement(reqstr, namespace);
		
		OMText textData = omFactory.createOMText(new DataHandler(new FileDataSource(dataFile)), true);
		omFactory.createOMElement("expFile", namespace, request).addChild(textData);
		omFactory.createOMElement("algorithm", namespace, request).setText(input.getAlgorithm());
		omFactory.createOMElement("dataSetName", namespace, request).setText(input.getDataSetName());
		if(config) return request;
		omFactory.createOMElement("bootstrapNumber", namespace, request).setText(Integer.toString(input.getBootstrapNumber()));
		omFactory.createOMElement("consensusThreshold", namespace, request).setText(Float.toString(input.getConsensusThreshold()));
		omFactory.createOMElement("dataSetIdentifier", namespace, request).setText(input.getDataSetIdentifier());
		omFactory.createOMElement("dPITolerance", namespace, request).setText(Float.toString((float)input.getDPITolerance()));
		omFactory.createOMElement("kernelWidth", namespace, request).setText(Float.toString((float)input.getKernelWidth()));
		omFactory.createOMElement("mode", namespace, request).setText(input.getMode());
		omFactory.createOMElement("threshold", namespace, request).setText(Float.toString(input.getThreshold()));
		omFactory.createOMElement("hubGeneList", namespace, request).setText(arrToStr(input.gethubGeneList()));
		omFactory.createOMElement("targetGeneList", namespace, request).setText(arrToStr(input.getTargetGeneList()));
		omFactory.createOMElement("isDPIToleranceSpecified", namespace, request).setText(Boolean.toString(input.getIsDPIToleranceSpecified()));
		omFactory.createOMElement("isKernelWidthSpecified", namespace, request).setText(Boolean.toString(input.getIsKernelWidthSpecified()));
		omFactory.createOMElement("isThresholdMI", namespace, request).setText(Boolean.toString(input.getIsThresholdMI()));
		omFactory.createOMElement("noCorrection", namespace, request).setText(Boolean.toString(input.getNoCorrection()));
		omFactory.createOMElement("configKernel", namespace, request).setText(configstr[0]);
		omFactory.createOMElement("configThreshold", namespace, request).setText(configstr[1]);

		return request;
	}

	private String arrToStr(String[] list){
		if(list == null) return "";
		StringBuilder sb = new StringBuilder();
		for(String s : list) sb.append(s).append("\n");
		return sb.toString();
	}

	private AbstractPojo doWebServiceCallWithAxis(String serviceAddress, OMElement aracneRequest, List<String> hubGeneList, Map<String, String> map, boolean prune) throws AxisFault {
		org.apache.axis2.client.Options serviceOptions = new org.apache.axis2.client.Options();
    	serviceOptions.setProperty( Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE );
    	serviceOptions.setProperty( Constants.Configuration.ATTACHMENT_TEMP_DIR, System.getProperty("java.io.tmpdir") );
    	serviceOptions.setProperty( Constants.Configuration.CACHE_ATTACHMENTS, Constants.VALUE_TRUE );
    	serviceOptions.setProperty( Constants.Configuration.FILE_SIZE_THRESHOLD, "1024" );
		// 50-hour timeout
		serviceOptions.setTimeOutInMilliSeconds(180000000);

		ServiceClient serviceClient = new ServiceClient();
		serviceClient.setOptions(serviceOptions);
		EndpointReference ref = new EndpointReference();
		ref.setAddress(serviceAddress);
		serviceClient.setTargetEPR(ref);

		OMElement response = serviceClient.sendReceive(aracneRequest);
		
		if(config) return getConfigResult(response);
		return getNetwork(response, hubGeneList, map, prune);	
	}
	
	private Network getNetwork(OMElement response, List<String> hubGeneList, Map<String, String> map, boolean prune){
		OMElement nameElement = (OMElement)response.getFirstChildWithName(new QName(aracneNamespace, "adjName"));
		resultName = nameElement.getText();
		
		OMElement fileElement = (OMElement)response.getFirstChildWithName(new QName(aracneNamespace, "adjFile"));
		DataHandler handler = fileElement==null?null:(DataHandler)((OMText)fileElement.getFirstOMChild()).getDataHandler();

		AracneOutput output = new AracneOutput(getEdges(handler), null);
		return AracneAnalysisWeb.convert(output, hubGeneList, map, prune);
	}
	
	private ConfigResult getConfigResult(OMElement response){
		OMElement nameElement = (OMElement)response.getFirstChildWithName(new QName(aracneNamespace, "name"));
		resultName = nameElement.getText();
		
		ArrayList<Float> kernels = new ArrayList<Float>();
		Iterator<?> elements = response.getChildrenWithName(new QName(aracneNamespace, "kernel"));
		while(elements.hasNext()){
			OMElement elem = (OMElement)elements.next();
			kernels.add(Float.parseFloat(elem.getText()));
		}
		
		ArrayList<Float> thresholds = new ArrayList<Float>();
		elements = response.getChildrenWithName(new QName(aracneNamespace, "threshold"));
		while(elements.hasNext()){
			OMElement elem = (OMElement)elements.next();
			thresholds.add(Float.parseFloat(elem.getText()));
		}

		ConfigResult output = new ConfigResult(kernels.toArray(new Float[0]), thresholds.toArray(new Float[0]));
		return output;
	}

	private AracneGraphEdge[] getEdges(DataHandler handler){
		if(handler == null) return null;
		ArrayList<AracneGraphEdge> edges = new ArrayList<AracneGraphEdge>();
		BufferedReader br = null;
		try{
			br = new BufferedReader(new InputStreamReader(handler.getInputStream()));
			String line = null;
			while((line = br.readLine()) != null){
				if (line.trim().equals("") || line.startsWith(">")) continue;
				String[] toks = line.split("\t");
				String node1 = toks[0];
				int n = toks.length;
				if(n % 2 == 0 || node1.trim().equals("")) continue;
				for(int i = 1; i < n; i += 2){
					String node2 = toks[i];
					if(node2.trim().equals("")) continue;
					float weight = Float.parseFloat(toks[i+1]);
					edges.add(new AracneGraphEdge(node1, node2, weight));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			if(br != null){
				try{ br.close(); }catch(Exception e){ e.printStackTrace(); }
			}
		}		
		return edges.toArray(new AracneGraphEdge[0]);
	}
}
