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
 * Server side component for the VHeatMap widget.
 */
@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VHeatMap.class)
public class HeatMap extends AbstractComponent {

	private static final long serialVersionUID = 1L;
	
	private String[] colors;
	private String[] arrayLabels;
	private String[] markerLabels;
	private int numMarkers;
	private int numArrays;
	private String heatmapSVG;

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		target.addVariable(this, "color", getColors());
		target.addVariable(this, "arrayNumber", getArrayNumber());
		target.addVariable(this, "markerNumber", getMarkerNumber());
		target.addVariable(this, "markerLabels", getMarkerLabels());
		target.addVariable(this, "arrayLabels", getArrayLabels());
		target.addVariable(this, "exportSVG", getExportSVG());
		target.addVariable(this, "svgdata", "");
	}

	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		if(variables.containsKey("svgdata")) {

			heatmapSVG		= 	(String) variables.get("exportSVG");
			exportSVG((String) variables.get("svgdata"));
			
		}
	}
	/**
	 * Doesn't work for now
	 * 
	 */
	private void exportSVG(String image) {
		BufferedWriter fos = null;
        File tempFile = null;
        try {
            tempFile 	= 	File.createTempFile("tmp", ".svg");
            fos 		= 	new BufferedWriter(new FileWriter(tempFile));
            fos.write("<?xml version=\"1.0\" standalone=\"no\"?><!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
            fos.write(image);
            fos.write("</xml>");
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
        String downloadFileName = "HeatMap.svg";
        String contentType = "image/svg+xml";
        try {
            TemporaryFileDownloadResource resource = new TemporaryFileDownloadResource(getApplication(), downloadFileName, contentType, tempFile);
            getWindow().open(resource, "_self");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
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

	public void setExportSVG(String heatmapSVG) {
		this.heatmapSVG = heatmapSVG;
        requestRepaint();
		
	}

	public String getExportSVG() {
		return heatmapSVG;
	}
}
