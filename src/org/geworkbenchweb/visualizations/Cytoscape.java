package org.geworkbenchweb.visualizations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import sun.misc.BASE64Decoder;

import com.vaadin.addon.tableexport.TemporaryFileDownloadResource;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the VCytoscape widget.
 */
@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VCytoscape.class)
public class Cytoscape extends AbstractComponent {

	private static final long serialVersionUID = -6368440900242204532L;

	private String[] nodes;
	private String[] edges;
	private String networkPNG; 
	private String networkSVG;
	private String networkPNGData;
	private String networkSVGData;
	
	private String layoutName = "ForceDirected"; // default
	
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		
		target.addAttribute("layoutName", layoutName);

		target.addVariable(this, "nodes", getNodes());
		target.addVariable(this, "edges", getEdges());
		target.addVariable(this, "networkPNG", getNetwork());
		target.addVariable(this, "networkSVG", getNetworkSVG());
		target.addVariable(this, "networkPNGData", getNetworkPNGData());
		target.addVariable(this, "networkSVGData", getNetworkSVGData());
	}

	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);
		
		if(variables.containsKey("networkPNGData")) {

			networkPNG		= 	(String) variables.get("networkPNG");
			exportPNG((String) variables.get("networkPNGData"));
			
		}
		
		if(variables.containsKey("networkSVGData")) {
			
			networkSVG		= 	(String) variables.get("networkSVG");
			exportSVG((String) variables.get("networkSVGData"));
		
		}
	} 
	
	public void setNetwork(String networkPNG) {
        this.networkPNG = networkPNG;
        requestRepaint();
	}

	public String getNetwork() {
        
		return networkPNG;
	
	}
	
	public void setNetworkSVG(String networkSVG) {
        this.networkSVG = networkSVG;
        requestRepaint();
	}

	public String getNetworkSVG() {
        
		return networkSVG;
	
	}

	public String getNetworkPNGData() {
        
		return networkPNGData;
	
	}
	
	public String getNetworkSVGData() {
        
		return networkSVGData;
	
	}
	
	public void setNodes(String[] nodes) {
        this.nodes = nodes;
        requestRepaint();
	}

	public String[] getNodes() {
        
		return nodes;
	
	}
	
	public void setEdges(String[] edges) {
        this.edges = edges;
        requestRepaint();
	}

	public String[] getEdges() {
        
		return edges;
	
	}
	
	public void exportPNG(String base64) {
		
		FileOutputStream fos = null;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("tmp", ".png");
            fos = new FileOutputStream(tempFile);
           
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] decodedBytes = decoder.decodeBuffer(base64);
            
            fos.write(decodedBytes);
            fos.flush();  
           
        }
        catch (IOException e) {
            e.printStackTrace();
        }
       
        finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String downloadFileName = "CytoNetwork.png";
        String contentType = "image/png";
        try {
            TemporaryFileDownloadResource resource = new TemporaryFileDownloadResource(getApplication(), downloadFileName, contentType, tempFile);
            getWindow().open(resource, "_self");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
		
	}
	
	public void exportSVG(String image) {
		
		BufferedWriter fos = null;
        File tempFile = null;
        try {
            tempFile 	= 	File.createTempFile("tmp", ".svg");
            fos 		= 	new BufferedWriter(new FileWriter(tempFile));
            
            fos.write(image);
            fos.flush();  
           
        }
        catch (IOException e) {
            e.printStackTrace();
        }
       
        finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String downloadFileName = "CytoNetwork.svg";
        String contentType = "image/svg+xml";
        try {
            TemporaryFileDownloadResource resource = new TemporaryFileDownloadResource(getApplication(), downloadFileName, contentType, tempFile);
            getWindow().open(resource, "_self");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
		
	}

	public void setLayout(String layoutName) {
		this.layoutName = layoutName;
	}
	
}
