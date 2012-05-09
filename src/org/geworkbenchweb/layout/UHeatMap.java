package org.geworkbenchweb.layout;

import java.awt.Color;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSRangeMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbenchweb.visualizations.HeatMap;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.Reindeer;

public class UHeatMap extends HorizontalLayout {
	
	private static final long serialVersionUID = 1L;
	
	private DSMicroarraySet maSet;
    
    private double intensity = 1.0;
    
    private transient Object lock = new Object();
	
	public UHeatMap(DSMicroarraySet dataSet) {
		
		this.maSet = dataSet;
		
		setHeight(((dataSet.getMarkers().size()*10)) + "px");
		setWidth(( (dataSet.size()*30) ) + "px");
		
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySet = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(maSet);
		
		int geneNo 	= 	microarraySet.allMarkers().size();
		int chipNo 	= 	microarraySet.items().size();
		
		String[] markerNames 	= 	new String[geneNo];
		String[] arrayNames		= 	new String[chipNo];
		String[] colors 		= 	new String[chipNo*geneNo];
		int k = 0;
		
		for (int i = 0; i < geneNo; i++) {
			DSGeneMarker stats = null;
			
			stats = microarraySet.markers().get(i);

			markerNames[i] = stats.getLabel();
			
			for (int j = 0; j < chipNo; j++) {
				
				DSMicroarray mArray = microarraySet.get(j);
				
				if(i == 0) {
					
					arrayNames[j] = mArray.getLabel();
					
				}
				
				DSMarkerValue marker = mArray.getMarkerValue(stats);
				
				Color color = getMarkerValueColor(marker, stats, (float) intensity);
				String rgb = Integer.toHexString(color.getRGB());
				rgb = rgb.substring(2, rgb.length());
				colors[k] = rgb;
				k++;
			}
		}
		
		HeatMap heatMap = new HeatMap();
		heatMap.setColors(colors);
		heatMap.setArrayLabels(arrayNames);
		heatMap.setMarkerLabels(markerNames);
		heatMap.setArrayNumber(chipNo);
		heatMap.setMarkerNumber(geneNo);
		heatMap.setSizeFull();
	
		setStyleName(Reindeer.LAYOUT_WHITE);
		addComponent(heatMap);
	}
	
	public Color getMarkerValueColor(DSMarkerValue mv, DSGeneMarker mInfo, float intensity) {

		//      intensity *= 2;
		intensity = 2 / intensity; 
		double value = mv.getValue();
		if (lock == null)
			lock = new Object();
		synchronized (lock) {

			org.geworkbench.bison.util.Range range = ((DSRangeMarker) mInfo).getRange();
			double mean = range.norm.getMean(); //(range.max + range.min) / 2.0;
			double foldChange = (value - mean) / (range.norm.getSigma() + 0.00001); //Math.log(change) / Math.log(2.0);
			if (foldChange < -intensity) {
				foldChange = -intensity;
			}
			if (foldChange > intensity) {
				foldChange = intensity;
			}

			double colVal = foldChange / intensity;
			if (foldChange > 0) {
				return new Color(1.0F, (float) (1 - colVal), (float) (1 - colVal));
			} else {
				return new Color((float) (1 + colVal), (float) (1 + colVal), 1.0F);
			}
		}

	}

}
