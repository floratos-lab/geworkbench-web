package org.geworkbenchweb.plugins.aracne;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

public class AracneAxisClient {
	private static final String aracneNamespace = "http://www.geworkbench.org/service/aracne";
	
	public AracneOutput executeAracne(String serviceAddress, AracneInput input, File dataFile) throws AxisFault {
		OMElement aracneRequest = createAxiomRequestElement(input, dataFile);
		return doWebServiceCallWithAxis(serviceAddress, aracneRequest);
	}

	private OMElement createAxiomRequestElement(AracneInput input, File dataFile) {

		OMFactory omFactory = OMAbstractFactory.getSOAP11Factory();
		OMNamespace namespace = omFactory.createOMNamespace(aracneNamespace, null);
		OMElement request = omFactory.createOMElement("ExecuteAracneRequest", namespace);
		
		OMText textData = omFactory.createOMText(new DataHandler(new FileDataSource(dataFile)), true);
		omFactory.createOMElement("expFile", namespace, request).addChild(textData);
		omFactory.createOMElement("algorithm", namespace, request).setText(input.getAlgorithm());
		omFactory.createOMElement("bootstrapNumber", namespace, request).setText(Integer.toString(input.getBootstrapNumber()));
		omFactory.createOMElement("consensusThreshold", namespace, request).setText(Float.toString(input.getConsensusThreshold()));
		omFactory.createOMElement("dataSetIdentifier", namespace, request).setText(input.getDataSetIdentifier());
		omFactory.createOMElement("dataSetName", namespace, request).setText(input.getDataSetName());
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

		return request;
	}

	private String arrToStr(String[] list){
		if(list == null) return "";
		StringBuilder sb = new StringBuilder();
		for(String s : list) sb.append(s).append("\n");
		return sb.toString();
	}

	private AracneOutput doWebServiceCallWithAxis(String serviceAddress, OMElement aracneRequest) throws AxisFault {
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
		
		OMElement nameElement = (OMElement)response.getFirstChildWithName(new QName(aracneNamespace, "adjName"));
		String adjName = nameElement.getText();
		
		OMElement fileElement = (OMElement)response.getFirstChildWithName(new QName(aracneNamespace, "adjFile"));
		DataHandler handler = (DataHandler)((OMText)fileElement.getFirstOMChild()).getDataHandler();

		AracneOutput output = new AracneOutput(getEdges(handler), adjName);
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
