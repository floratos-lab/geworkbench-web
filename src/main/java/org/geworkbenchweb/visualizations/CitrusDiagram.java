package org.geworkbenchweb.visualizations;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VCitrusDiagram.class)
public class CitrusDiagram extends AbstractComponent {

	private static final long serialVersionUID = 2073659992756579843L;
	private String[] alteration, samples, presence;
	final private Integer[] preppi, cindy;
	final private String[] pvalue;
	final private String[] nes;

	public CitrusDiagram() {
    	/* create random test data */
		int n = 30, m = 100;
		String[] colorKeys = {"UMT", "DMT", "AMP", "DEL", "SNV", "GFU"};
		alteration = new String[n];
		for(int i=0; i<n; i++) {
    		int randomIndex = (int)( Math.random() * colorKeys.length );
            String geneSymbol = "";
            for(int j=0; j<4; j++) {
                char c = (char)((Math.random()*26)+'A');
                geneSymbol += c;
            } 
    		alteration[i] = colorKeys[randomIndex]+"_"+geneSymbol;
		};

        samples = new String[m];
        for(int j=0; j<m; j++) {
            samples[j] = "";
            for(int k=0; k<10; k++) {
                char c = (char)('A' + Math.random()*26 );
                samples[j] += c;
            } 
        }

        presence = new String[n];
    	for(int i=0; i<n; i++) {
    		presence[i] = "";
    		double r = Math.random();
    		for(int j=0; j<m; j++) {
    			char p = '_';
    			double x = Math.random();
    			if(x>r)
    				p = '1';
    			else
    				p = '0';
    			presence[i] += p;
    		}
    	}

    	preppi = new Integer[n];
        for(int i=0; i<n; i++) {
            preppi[i] = (int)(Math.random()*2);
        }

        cindy = new Integer[n];
        for(int i=0; i<n; i++) {
            cindy[i] = (int)(Math.random()*2);
        }
        
        pvalue = new String[n];
        for(int i=0; i<n; i++) {
            pvalue[i] = String.valueOf( Math.random() );
        }

		nes = new String[m];
		for (int i = 0; i < m; i++) {
			nes[i] = String.valueOf( Math.random()*2.-1. );
		}
		/* end of creating random test data */
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		target.addAttribute("alteration", alteration);
		target.addAttribute("samples", samples);
		target.addAttribute("presence", presence);
		target.addAttribute("preppi", preppi);
		target.addAttribute("cindy", cindy);
		target.addAttribute("pvalue", pvalue);
		target.addAttribute("nes", nes);
	}

	public void setCitrusData(String[] alteration) {
		this.alteration = alteration;
		requestRepaint();
	}
}
