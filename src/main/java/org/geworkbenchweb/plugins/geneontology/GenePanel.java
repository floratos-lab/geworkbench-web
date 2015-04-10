/**
 * 
 */
package org.geworkbenchweb.plugins.geneontology;

import java.util.Arrays;

import org.geworkbenchweb.pojos.GOResult;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * @author zji
 *
 */
public class GenePanel extends VerticalLayout {

	private static final long serialVersionUID = -2473273908829758933L;
	
	private static final String[] GENE_FOR_OPTIONS = {"Term", "Term and its descendants"};
	static final String[] GENE_FROM_OPTIONS = {"Changed gene list", "Rerefence list"};

	private final OptionGroup geneForSelect = new OptionGroup("Show Genes For", Arrays.asList(GENE_FOR_OPTIONS));
	private final OptionGroup geneFromSelect = new OptionGroup("Show Genes From", Arrays.asList(GENE_FROM_OPTIONS));
	private final GeneTable geneTable;
	
	GenePanel(final Table goResultTable, final GOResult result, final Long parenrId) {
		geneTable = new GeneTable(result, parenrId);
		
		geneTable.setSizeFull();

		geneForSelect.setValue(GENE_FOR_OPTIONS[0]);
		geneForSelect.addStyleName("horizontal");
		geneForSelect.setImmediate(true);
		geneForSelect.addListener(new Table.ValueChangeListener() {

			private static final long serialVersionUID = -7615528381968321116L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Integer goId = (Integer) goResultTable.getValue();
				if(goId==null) return;
				String f = (String)event.getProperty().getValue();
				boolean d = f.equals(GENE_FOR_OPTIONS[1]);
				geneTable.updateData(goId , d, (String)geneFromSelect.getValue());
			}
			
		});
		geneFromSelect.setValue(GENE_FROM_OPTIONS[0]);
		geneFromSelect.addStyleName("horizontal");
		geneFromSelect.setImmediate(true);
		geneFromSelect.addListener(new Table.ValueChangeListener() {

			private static final long serialVersionUID = -7615528381968321116L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				String newFrom = (String)event.getProperty().getValue();
				Integer goId = (Integer) goResultTable.getValue();
				if(goId==null) return;
				String f = (String)geneForSelect.getValue();
				boolean d = f.equals(GENE_FOR_OPTIONS[1]);
				geneTable.updateData(goId, d, newFrom);
			}
			
		});
		
		setSpacing(true);
		addComponent( geneForSelect );
		addComponent( geneFromSelect );
		addComponent(geneTable);
	}

	public void update(int goId) {
		String f = (String)geneForSelect.getValue();
		boolean d = f.equals(GENE_FOR_OPTIONS[1]);
		geneTable.updateData(goId, d, (String)geneFromSelect.getValue());
	}
}
