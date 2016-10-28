package org.geworkbenchweb.plugins.pbqdi;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

public class DrugReport extends Window {

    public DrugReport(String sampleName, String tumorType, final String report) {
        this.setModal(true);
        this.setClosable(true);
        ((AbstractOrderedLayout) this.getContent()).setSpacing(true);
        this.setResizable(true);
        this.setCaption("Columbia/CPTAC Patient Sample Drug Report");
        this.setImmediate(true);

        this.addComponent(new Label("<b>Drug Report for " + sampleName + " using " + tumorType + " data</b>",
                Label.CONTENT_XHTML));
        Button pdfButton = new Button("Download Full Report as PDF");
        pdfButton.addListener(new ClickListener() {

            private static final long serialVersionUID = -6270828819674891537L;

            @Override
            public void buttonClick(ClickEvent event) {
                System.out.println("... open PDF report " + report);
            }

        });
        this.addComponent(pdfButton);
        this.addComponent(new Label("Data Quality"));
        this.addComponent(new Button("View"));

    }

    private static final long serialVersionUID = -761592910851078204L;

}
