package org.geworkbenchweb.plugins.pbqdi;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import org.geworkbenchweb.pojos.PbqdiResult;

public class ResultView extends Window {

    private static final long serialVersionUID = -273679922042872864L;

    private static final String COLUMN_SAMPLE_NAME = "Sample Name";
    private static final String COLUMN_SUBTYPE = "Subtype";
    private static final String COLUMN_SAMPLE_PER_SUBTYPE = "samples";

    private Table resultTable = new Table();
    private Embedded image = new Embedded();
    private Table samplePerSubtype = new Table();

    public ResultView(final PbqdiResult result) throws IOException {
        this.setModal(true);
        this.setClosable(true);
        ((AbstractOrderedLayout) this.getContent()).setSpacing(true);
        this.setResizable(true);
        this.setCaption("Columbia/CPTAC Patient Tumor Subtype Results");
        this.setImmediate(true);

        final String tumorType = result.getTumorType();
        final Map<String, Integer> subtypes = result.getSubtypes();
        final String htmlReport = result.getSampleFileName() + ".html";
        final int jobId = result.getJobId();

        Container container = new IndexedContainer();
        container.addContainerProperty(COLUMN_SAMPLE_NAME, String.class, null);
        container.addContainerProperty(COLUMN_SUBTYPE, Integer.class, 0);
        Map<Integer, List<String>> summary = new HashMap<Integer, List<String>>();
        for (String sampleName : subtypes.keySet()) {
            Integer subtype = subtypes.get(sampleName);
            if(subtype==null) throw new IOException("Null subtype for sample name "+sampleName);
            Item item = container.addItem(sampleName);
            item.getItemProperty(COLUMN_SAMPLE_NAME).setValue(sampleName);
            item.getItemProperty(COLUMN_SUBTYPE).setValue(subtype);

            List<String> s = summary.get(subtype);
            if (s == null) {
                s = new ArrayList<String>();
                summary.put(subtype, s);
            }
            s.add(sampleName);
        }
        resultTable.setContainerDataSource(container);
        resultTable.setPageLength(subtypes.size());
        resultTable.setSizeFull();

        Button reportButton = new Button("Drug Prediction Report");
        reportButton.addListener(new ClickListener() {

            private static final long serialVersionUID = 345938285589568581L;

            @Override
            public void buttonClick(ClickEvent event) {
                Window mainWindow = ResultView.this.getApplication().getMainWindow();
                DrugReport v = new DrugReport(tumorType, jobId, htmlReport);
                mainWindow.addWindow(v);
            }

        });

        Container container2 = new IndexedContainer();
        container2.addContainerProperty(COLUMN_SUBTYPE, Integer.class, 0);
        container2.addContainerProperty(COLUMN_SAMPLE_PER_SUBTYPE, Integer.class, null);
        for (Integer subtype : summary.keySet()) {
            Item item = container2.addItem(subtype);
            item.getItemProperty(COLUMN_SUBTYPE).setValue(subtype);
            item.getItemProperty(COLUMN_SAMPLE_PER_SUBTYPE).setValue(summary.get(subtype).size());
        }
        samplePerSubtype.setContainerDataSource(container2);
        samplePerSubtype.setPageLength(summary.size());
        samplePerSubtype.setSizeFull();

        image = new Embedded(null, new ExternalResource("/kaplan_images/"+tumorType+"_km.png"));
        image.setSizeFull();

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setSizeFull();
        VerticalLayout leftSide = new VerticalLayout();
        leftSide.setSpacing(true);
        VerticalLayout rightSide = new VerticalLayout();

        VerticalLayout top = new VerticalLayout();
        top.setSpacing(true);
        top.setMargin(true);
        VerticalLayout bottom = new VerticalLayout();
        bottom.setSpacing(true);
        bottom.setMargin(true);
        leftSide.addComponent(top);
        leftSide.addComponent(bottom);

        top.addComponent(new Label("<b>Subtypes</b>", Label.CONTENT_XHTML));
        top.addComponent(resultTable);
        bottom.addComponent(new Label("<b>Summary of TCGA Samples per Subtype</b>", Label.CONTENT_XHTML));
        bottom.addComponent(samplePerSubtype);
        bottom.addComponent(new Label("<br/><br/><br/><br/><br/>", Label.CONTENT_XHTML)); // add some artificial space
        bottom.addComponent(reportButton);

        rightSide.addComponent(new Label("<b>Survival Curves per Subtype</b>", Label.CONTENT_XHTML));
        rightSide.addComponent(image);
        layout.addComponent(leftSide);
        layout.addComponent(rightSide);
        this.addComponent(layout);

        this.setWidth("75%");
    }

}
