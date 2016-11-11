package org.geworkbenchweb.plugins.pbqdi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

public class ResultView extends Window {

    private static final long serialVersionUID = -273679922042872864L;

    private static final String COLUMN_SAMPLE_NAME = "Sample Name";
    private static final String COLUMN_SUBTYPE = "Subtype";
    private static final String COLUMN_DRUG_PREDICTION = "Drug Prediction";
    private static final String COLUMN_SAMPLE_PER_SUBTYPE = "samples (click to view)";

    public ResultView(String[] sampleNames, final String tumorType, Map<String, Integer> subtypes, final String[] drugReports,
            FileResource kaplanImage, final String qualitySection, final String pdaSection, final String investigationalSection) {
        this.setModal(true);
        this.setClosable(true);
        ((AbstractOrderedLayout) this.getContent()).setSpacing(true);
        this.setResizable(true);
        this.setCaption("Columbia/CPTAC Patient Tumor Subtype Results");
        this.setImmediate(true);

        Table resultTable = new Table();
        Container container = new IndexedContainer();
        container.addContainerProperty(COLUMN_SAMPLE_NAME, String.class, null);
        container.addContainerProperty(COLUMN_SUBTYPE, Integer.class, 0);
        container.addContainerProperty(COLUMN_DRUG_PREDICTION, Button.class, null);
        Map<Integer, List<String>> summary = new HashMap<Integer, List<String>>();
        for (int i = 0; i < sampleNames.length; i++) {
            final String sampleName = sampleNames[i];
            Integer subtype = subtypes.get(sampleName);
            Item item = container.addItem(sampleName);
            item.getItemProperty(COLUMN_SAMPLE_NAME).setValue(sampleName);
            item.getItemProperty(COLUMN_SUBTYPE).setValue(subtype);
            Button b = new Button("View Report");
            b.setStyleName(BaseTheme.BUTTON_LINK);
            final String report = drugReports[i];
            b.addListener(new ClickListener() {

                private static final long serialVersionUID = 345938285589568581L;

                @Override
                public void buttonClick(ClickEvent event) {
                    Window mainWindow = ResultView.this.getApplication().getMainWindow();
                    DrugReport v = new DrugReport(sampleName, tumorType, report, qualitySection, pdaSection, investigationalSection);
                    mainWindow.addWindow(v);
                }

            });
            item.getItemProperty(COLUMN_DRUG_PREDICTION).setValue(b);

            List<String> s = summary.get(subtype);
            if (s == null) {
                s = new ArrayList<String>();
                summary.put(subtype, s);
            }
            s.add(sampleName);
        }
        resultTable.setContainerDataSource(container);
        resultTable.setPageLength(sampleNames.length);
        resultTable.setSizeFull();

        Table samplePerSubtype = new Table();
        Container container2 = new IndexedContainer();
        container2.addContainerProperty(COLUMN_SUBTYPE, Integer.class, 0);
        container2.addContainerProperty(COLUMN_SAMPLE_PER_SUBTYPE, Button.class, null);
        for (Integer subtype : summary.keySet()) {
            Item item = container2.addItem(subtype);
            item.getItemProperty(COLUMN_SUBTYPE).setValue(subtype);
            Button b = new Button("" + summary.get(subtype).size());
            b.setStyleName(BaseTheme.BUTTON_LINK);
            item.getItemProperty(COLUMN_SAMPLE_PER_SUBTYPE).setValue(b);
        }
        samplePerSubtype.setContainerDataSource(container2);
        samplePerSubtype.setPageLength(summary.size());
        samplePerSubtype.setSizeFull();

        Embedded image = new Embedded(null, kaplanImage);

        this.addComponent(new Label("<b>Subtypes</b>", Label.CONTENT_XHTML));
        this.addComponent(resultTable);
        this.addComponent(new Label("<b>Survival Curves per Subtype</b>", Label.CONTENT_XHTML));
        this.addComponent(image);
        this.addComponent(new Label("<b>Summary of TCGA Samples per Subtype</b>", Label.CONTENT_XHTML));
        this.addComponent(samplePerSubtype);

        this.setSizeUndefined();
        this.setWidth("75%");
    }

}
