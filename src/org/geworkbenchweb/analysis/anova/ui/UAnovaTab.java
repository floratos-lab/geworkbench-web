package org.geworkbenchweb.analysis.anova.ui;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet; 

import org.geworkbenchweb.utils.TableView;

 
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox; 
import com.vaadin.ui.GridLayout; 
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.HorizontalLayout; 
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
 

/**
 * This class displays Anova results in a Table 
 * @author Min You
 */
 
public class UAnovaTab extends VerticalLayout {   
	 
	private static final long serialVersionUID = 3115606230292029231L;
	
	private TableView dataTable;
	// preferences
	private static boolean fStat = true;
	private static boolean pVal = true;
	private static boolean mean = true;
	private static boolean std = true;
	
	private CheckBox bF; 
	private CheckBox bP; 
	private CheckBox bM; 
	private CheckBox bS;  
	
	private CSAnovaResultSet<DSGeneMarker> anovaResultSet = null;
		 
	Button submitButton;
	
	
	public UAnovaTab(CSAnovaResultSet<DSGeneMarker>  anovaResultSet) {
	
	     this.anovaResultSet = anovaResultSet;
	     
		 setSpacing(true);
		 setImmediate(true);	 
		 
		 HorizontalLayout horizontalLayout = new HorizontalLayout();
		 horizontalLayout.setSpacing(true);			 
	 
		 horizontalLayout.setImmediate(true);
		/* Results Table Code */
		 
		dataTable  = new TableView();		 ;		 
		dataTable.setContainerDataSource(getIndexedContainer());	
		
	 
		
		Button preferenceButton;		
		preferenceButton = new Button("Display Preference", new PreferenceListener());
		
		Button exportButton;		
		exportButton = new Button("Export", new ExportListener());
	 
		horizontalLayout.addComponent(preferenceButton, 0);
		horizontalLayout.addComponent(exportButton, 1);
		
			 
		addComponent(horizontalLayout);
		addComponent(dataTable);	
	 
	}
	
	private IndexedContainer getIndexedContainer() {

		String[] header;
		IndexedContainer dataIn = new IndexedContainer();
		
		int groupNum = anovaResultSet.getLabels(0).length;
		int meanStdStartAtIndex = 1 + (fStat ? 1 : 0) + (pVal ? 1 : 0);
		header = new String[meanStdStartAtIndex + groupNum
				* ((mean ? 1 : 0) + (std ? 1 : 0))];
		int fieldIndex = 0;
		header[fieldIndex++] = "Marker Name";
		if (pVal) {
			header[fieldIndex++] = "P-Value";
		}
		if (fStat) {
			header[fieldIndex++] = "F-statistic";
		}
		for (int cx = 0; cx < groupNum; cx++) {
			if (mean) {
				header[meanStdStartAtIndex + cx
						* ((mean ? 1 : 0) + (std ? 1 : 0)) + 0] = anovaResultSet
						.getLabels(0)[cx] + "_Mean";
			}
			if (std) {
				header[meanStdStartAtIndex + cx
						* ((mean ? 1 : 0) + (std ? 1 : 0)) + (mean ? 1 : 0)] = anovaResultSet
						.getLabels(0)[cx] + "_Std";
			}
		}
		
		for (String p : header) 
		{ 
			if (p.equals("Marker Name"))
		       dataIn.addContainerProperty(p, String.class, "");   
			else
				dataIn.addContainerProperty(p, Float.class, "");  
			  
		}
		
		
		double[][] result2DArray = anovaResultSet.getResult2DArray();
		int significantMarkerNumbers = anovaResultSet.getSignificantMarkers()
				.size();
		for (int cx = 0; cx < significantMarkerNumbers; cx++) {
			Object id = dataIn.addItem();
			fieldIndex = 0;
			dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(((DSGeneMarker) anovaResultSet
					.getSignificantMarkers().get(cx)).getShortName());
			if (pVal) {
				dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(convertDouble(result2DArray[0][cx]));
			}
			if (fStat) {
				dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(convertDouble(result2DArray[2][cx]));
			}
			for (int gc = 0; gc < groupNum; gc++) {
				if (mean) {
					dataIn.getContainerProperty(id, header[meanStdStartAtIndex + gc
					           							* ((mean ? 1 : 0) + (std ? 1 : 0)) + 0]).setValue(convertDouble(result2DArray[3 + gc * 2][cx]));
				}
				if (std) {
					dataIn.getContainerProperty(id, header[meanStdStartAtIndex + gc
							* ((mean ? 1 : 0) + (std ? 1 : 0)) + (mean ? 1 : 0)]).setValue(convertDouble(result2DArray[4 + gc * 2][cx]));
				}
			}
			 
		}
		
		
		return dataIn;
	}
	
	 
	
	private Object convertDouble(Object value) {
		     
		if ((value != null) && (value instanceof Number)) {
			if (((Number) value).doubleValue() < 0.1)
				value = String.format("%.2E", value);
			else
				value = String.format("%.2f", value);
		}
			
			return value;
			
	}
		 
	
	 
	private class PreferenceListener implements ClickListener {

		private static final long serialVersionUID = 831124091338570481L;
	 
		public PreferenceListener (){};
		 
		@Override
		public void buttonClick(ClickEvent event) {
			 
			Window dispPref  = new Window("Display Preferences");
		 
			GridLayout  gridLayout	= 	new GridLayout(2, 2);
			
			dispPref.setModal(true);
			dispPref.setClosable(true);
			dispPref.setDraggable(false);
			dispPref.setResizable(false);
			dispPref.setWidth("350px");
			
			bF = new CheckBox("F-Statistic", fStat);
		    bP = new CheckBox("P-Value", pVal);
			bM = new CheckBox("Mean", mean);
		    bS = new CheckBox("Std", std);
		    
		    bF.addListener(new CheckBoxListener());
		    bP.addListener(new CheckBoxListener());
		    bM.addListener(new CheckBoxListener());
		    bS.addListener(new CheckBoxListener());
			
		    gridLayout.setMargin(true);
		    gridLayout.setImmediate(true);
		    gridLayout.setSpacing(true);
			
		    gridLayout.addComponent(bF, 0, 0);
		    gridLayout.addComponent(bP, 1, 0);
		    gridLayout.addComponent(bM, 0, 1);
		    gridLayout.addComponent(bS, 1, 1);
			 
			dispPref.addComponent(gridLayout);
			getApplication().getMainWindow().addWindow(dispPref);
		}
		 
	}
	
	private class ExportListener implements ClickListener {

		private static final long serialVersionUID = 831124091338570481L;
	 
	 
		public ExportListener ( ){};
	 
		@Override
		public void buttonClick(ClickEvent event) {
			
			 dataTable.csvExport("anovaTable.csv");			 
			
         } 
	}
	
	
	private class CheckBoxListener implements ClickListener {

		private static final long serialVersionUID = 831124091338570481L;
	 
	 
		public CheckBoxListener(){};
	 
		@Override
		public void buttonClick(ClickEvent e) {
			  
			Object source = e.getSource();
 
	        if (source == bF) {
	        	fStat = bF.booleanValue();
	        } else if (source == bP) {
	        	pVal = bP.booleanValue();
	        } else if (source == bM) {
	        	mean = bM.booleanValue();
	        } else if (source == bS) {
	        	std = bS.booleanValue();
	        }
	        
	        dataTable.setContainerDataSource(getIndexedContainer());	
			
			
          } 
	}
	
	 
	 
}
