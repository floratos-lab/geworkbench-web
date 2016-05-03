package org.geworkbenchweb.plugins.citrus;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.plugins.citrus.CitrusDatabase.Alteration;
import org.geworkbenchweb.plugins.citrus.CitrusDatabase.Viper;
import org.geworkbenchweb.visualizations.CitrusDiagram;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

public class GeneBasedQueryAndDataIntegration extends VerticalLayout {

	private static final long serialVersionUID = -713233350568178L;
	private static Log log = LogFactory.getLog(GeneBasedQueryAndDataIntegration.class);
	
	final private ComboBox cancerTypeComboBox = new ComboBox("TCGA cancer type");
	final private ComboBox geneSymbolComboBox = new ComboBox("Gene symbol (N / P_min)");
	final private CitrusDiagram citrusDiagram = new CitrusDiagram();
	final private Button runButton = new Button("Run Citrus");
	
	private Set<GeneChoice> geneSymbols = null;

	private CitrusDatabase db = null;
	
	public GeneBasedQueryAndDataIntegration() {
		try {
			db = new CitrusDatabase();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	final private ClickListener clickListener = new ClickListener() {

		private static final long serialVersionUID = 5141684198050379901L;

		@Override
		public void buttonClick(ClickEvent event) {
			String cancerType = (String) cancerTypeComboBox.getValue();
			GeneChoice geneSymbol = (GeneChoice) geneSymbolComboBox.getValue();
			if(geneSymbol==null) {
				log.debug("null gene ID");
				return;
			}
			int geneId = geneSymbol.id;
			Alteration[] alteration = db.getAlterations(cancerType, geneId);
			Viper[] viper = db.getViperValues(cancerType, geneId);
			String[] presence = db.getPresence(cancerType, alteration, viper);

			int n = alteration.length;
			String[] labels = new String[n];
			Integer[] preppi = new Integer[n];
			Integer[] cindy = new Integer[n];
			String[] pvalue = new String[n];
			for (int i = 0; i < n; i++) {
				labels[i] = alteration[i].eventType.toUpperCase() + "_" + alteration[i].modulatorSymbol;
				preppi[i] = alteration[i].preppi;
				cindy[i] = alteration[i].cindy;
				pvalue[i] = String.valueOf(alteration[i].pvalue);
			}

			int m = viper.length;
			String[] samples = new String[m];
			String[] nes = new String[m];
			for(int i=0; i<m; i++) {
				samples[i] = viper[i].sample;
				nes[i] = String.valueOf( viper[i].value );
			}
			citrusDiagram.setCitrusData(labels, samples, presence, preppi, cindy, pvalue, nes);
		}
	};
	
	private void setTFComboBox(String cancerType) {
		geneSymbolComboBox.removeAllItems();
		geneSymbols = db.getTF(cancerType);
		if(geneSymbols==null) {
			log.debug("null gene symbol list");
			return;
		}
		for (GeneChoice tf : geneSymbols) {
			geneSymbolComboBox.addItem(tf);
		}
		geneSymbolComboBox.select(geneSymbols.iterator().next());
	}

	@Override
	public void attach() {
		super.attach();

		HorizontalLayout commandPanel = new HorizontalLayout();
		this.setSpacing(true);
		this.addComponent(commandPanel);
		this.addComponent(citrusDiagram);

		citrusDiagram.setSizeFull();

		String[] cancerTypes = db.getCancerTypes();
		for (String s : cancerTypes)
			cancerTypeComboBox.addItem(s);
		cancerTypeComboBox.setNullSelectionAllowed(false);
		cancerTypeComboBox.addListener(new ValueChangeListener() {

			private static final long serialVersionUID = 246976645556960310L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				setTFComboBox((String) cancerTypeComboBox.getValue());
			}

		});
		cancerTypeComboBox.setImmediate(true);

		runButton.addListener(clickListener);
		commandPanel.setSpacing(true);
		commandPanel.addComponent(cancerTypeComboBox);
		commandPanel.addComponent(geneSymbolComboBox);
		commandPanel.addComponent(runButton);
		commandPanel.setComponentAlignment(runButton, Alignment.BOTTOM_CENTER);
	}
}
