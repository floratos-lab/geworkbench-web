package org.geworkbenchweb.visualizations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.vaadin.addon.tableexport.TemporaryFileDownloadResource;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the VDendrogram widget.
 * @author Nikhil Reddy
 */
@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VClustergram.class)
public class Clustergram extends AbstractComponent {

	private static final long serialVersionUID = 1L;
	private String[] colors;
	private String[] arrayLabels;
	private String[] markerLabels;
	private String markerCluster;
	private String arrayCluster;
	private int numMarkers;
	private int numArrays;
	private int geneHeight;
	private int geneWidth;
	private String svgFlag;
	private String dendrogramSVGData;

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		target.addVariable(this, "color", getColors());
		target.addVariable(this, "arrayNumber", getArrayNumber());
		target.addVariable(this, "markerNumber", getMarkerNumber());
		target.addVariable(this, "markerLabels", getMarkerLabels());
		target.addVariable(this, "arrayLabels", getArrayLabels());
		target.addVariable(this, "markerCluster", getMarkerCluster());
		target.addVariable(this, "arrayCluster", getArrayCluster());
		target.addVariable(this, "height", getGeneHeight());
		target.addVariable(this, "width", getGeneWidth());
		target.addVariable(this, "svgFlag", getSVGFlag());
		target.addVariable(this, "dendrogramSVGData", getSVGData());
	}


	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		if(variables.containsKey("dendrogramSVGData")) {
			svgFlag		= 	(String) variables.get("svgFlag");
			exportSVG((String) variables.get("dendrogramSVGData"));
		}

		if (variables.containsKey("marker")) {
			markerLabels	= 	(String[]) variables.get("markerLabels");
			markerCluster 	= 	(String) variables.get("marker");
			colors  		= 	(String[]) variables.get("markerColor");
			numMarkers 		= 	(Integer) variables.get("markerNumber");
			requestRepaint();
		}

		if (variables.containsKey("array")) {
			arrayLabels 	= 	(String[]) variables.get("arrayLabels");
			arrayCluster 	= 	(String) variables.get("array");
			colors  		=	(String[]) variables.get("arrayColor");
			numArrays 		= 	(Integer) variables.get("arrayNumber");
			requestRepaint();
		}
	}

	public void setColors(String[] colors) {
		this.colors = colors;
		requestRepaint();
	}

	public String[] getColors() {

		return colors;

	}

	public void setArrayNumber(int numArrays) {
		this.numArrays = numArrays;
		requestRepaint();
	}

	public int getArrayNumber() {
		return numArrays;
	}

	public void setMarkerNumber(int numMarkers) {
		this.numMarkers = numMarkers;
		requestRepaint();
	}

	public int getMarkerNumber() {
		return numMarkers;
	}

	public void setMarkerLabels(String[] markerLabels) {
		this.markerLabels = markerLabels;
		requestRepaint();
	}

	public String[] getMarkerLabels() {
		return markerLabels;
	}

	public void setArrayLabels(String[] arrayLabels) {
		this.arrayLabels = arrayLabels;
		requestRepaint();
	}

	public String[] getArrayLabels() {
		return arrayLabels;
	}

	public void setMarkerCluster(String markerCluster) {
		this.markerCluster = markerCluster;
		requestRepaint();
	}

	public String getMarkerCluster() {
		return markerCluster;
	}

	public void setArrayCluster(String arrayCluster) {
		this.arrayCluster = arrayCluster;
		requestRepaint();
	}

	public String getArrayCluster() {
		return arrayCluster;
	}


	public int getGeneHeight() {
		return geneHeight;
	}


	public void setGeneHeight(int geneHeight) {
		this.geneHeight = geneHeight;
		requestRepaint();
	}


	public int getGeneWidth() {
		return geneWidth;
	}


	public void setGeneWidth(int geneWidth) {
		this.geneWidth = geneWidth;
		requestRepaint();
	}

	private String getSVGData() {
		return dendrogramSVGData;
	}

	public void setSVGFlag(String svgFlag) {
		this.svgFlag = svgFlag;
		requestRepaint();
	}

	public String getSVGFlag() {
		return svgFlag;
	}

	public void exportSVG(String image) {

		BufferedWriter fos = null;
		File tempFile = null;
		try {
			tempFile 	= 	File.createTempFile("tmp", ".html");
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
		String downloadFileName 	= 	"dendrogram.html";
		String contentType 			= 	"XHTML";
		
		try {
			TemporaryFileDownloadResource resource = new TemporaryFileDownloadResource(getApplication(), downloadFileName, contentType, tempFile);
			getWindow().open(resource, "_self");
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
