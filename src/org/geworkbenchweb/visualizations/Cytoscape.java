package org.geworkbenchweb.visualizations;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;

import sun.misc.BASE64Decoder;

import com.vaadin.addon.tableexport.TemporaryFileDownloadResource;
import com.vaadin.terminal.FileResource;
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
	private String networkPNGData;
	
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		target.addVariable(this, "nodes", getNodes());
		target.addVariable(this, "edges", getEdges());
		target.addVariable(this, "networkPNG", getNetwork());
		target.addVariable(this, "networkPNGData", getNetworkPNGData());
		
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
			System.out.println("Nikhil-1");
			export((String) variables.get("networkPNGData"));
			
		}
		networkPNG		= 	(String) variables.get("networkPNG");
		
	} 
	
	public void setNetwork(String networkPNG) {
        this.networkPNG = networkPNG;
        requestRepaint();
	}

	public String getNetwork() {
        
		return networkPNG;
	
	}
	
	public void setNetworkPNGData(String networkPNGData) {
        this.networkPNGData = networkPNGData;
      //  requestRepaint();
	}

	public String getNetworkPNGData() {
        
		return networkPNGData;
	
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
	
	public void export(String base64) {
		System.out.println("Nikhil");
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
        String downloadFileName = System.currentTimeMillis() + "_filename.png";
        String contentType = "image/png";
        try {
            TemporaryFileDownloadResource resource = new TemporaryFileDownloadResource(getApplication(), downloadFileName, contentType, tempFile);
            getWindow().open(resource, "_self");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
		
	}
	
}
