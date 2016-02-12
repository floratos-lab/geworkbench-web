/**
 * 
 */
package org.geworkbenchweb.plugins.citrus;

import org.geworkbenchweb.visualizations.CitrusDiagram;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * @author zji
 *
 */
public class GeneBasedQueryAndDataIntegration extends VerticalLayout {

	private static final long serialVersionUID = -713233350568178L;

	@Override
	public void attach() {
		super.attach();

		HorizontalLayout commandPanel = new HorizontalLayout();
		HorizontalLayout diagramPanel = new HorizontalLayout();
		this.setSpacing(true);
		this.addComponent(commandPanel);
		this.addComponent(diagramPanel);

		final CitrusDiagram citrusDiagram = new CitrusDiagram();
		diagramPanel.addComponent(citrusDiagram);

		final ComboBox cancerTypeComboBox = new ComboBox("TCGA cancer type");
		String[] cancerTypes = new String[] { "Bladder carcinoma", "other cancers" };
		for (String s : cancerTypes)
			cancerTypeComboBox.addItem(s);
		cancerTypeComboBox.setNullSelectionAllowed(false);

		final TextField geneSymbolTextField = new TextField("Gene symbol");
		final TextField pValueTextField = new TextField("p-value");
		Button runButton = new Button("Run Citrus");
		runButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 5141684198050379901L;

			@Override
			public void buttonClick(ClickEvent event) {
				getWindow().showNotification("cancer type=" + cancerTypeComboBox.getValue() + "<br>gene="
						+ geneSymbolTextField.getValue() + "<br>p-value=" + pValueTextField.getValue());
			}

		});
		commandPanel.setSpacing(true);
		commandPanel.addComponent(cancerTypeComboBox);
		commandPanel.addComponent(geneSymbolTextField);
		commandPanel.addComponent(pValueTextField);
		commandPanel.addComponent(runButton);
		commandPanel.setComponentAlignment(runButton, Alignment.BOTTOM_CENTER);
	}
}
