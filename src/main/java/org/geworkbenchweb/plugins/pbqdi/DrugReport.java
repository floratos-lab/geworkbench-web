package org.geworkbenchweb.plugins.pbqdi;

import java.io.File;

import com.vaadin.terminal.FileResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

public class DrugReport extends Window {

    public DrugReport(String tumorType, final String report, final String qualitySection, final String pdaSection, final String investigationalSection) {
        this.setModal(true);
        this.setClosable(true);
        ((AbstractOrderedLayout) this.getContent()).setSpacing(true);
        this.setResizable(true);
        this.setCaption("Columbia/CPTAC Patient Sample Drug Report");
        this.setImmediate(true);

        this.addComponent(new Label("<b>Drug Report using " + tumorType + " data</b>",
                Label.CONTENT_XHTML));
        Button pdfButton = new Button("Download");
        pdfButton.addListener(new ClickListener() {

            private static final long serialVersionUID = -6270828819674891537L;

            @Override
            public void buttonClick(ClickEvent event) {
                DrugReport.this.open(new FileResource(new File(report), DrugReport.this.getApplication()), "_blank");
            }

        });
        HorizontalLayout panel1 = new HorizontalLayout();
        panel1.setSpacing(true);
        this.addComponent(panel1);
        panel1.addComponent(new Label("Download Full Report as PDF"));
        panel1.addComponent(pdfButton);

        final Label reportSection = new Label("", Label.CONTENT_XHTML);
        Button qualityButton = new Button("View");
        Button fdaButton = new Button("View");
        Button investigatinalButton = new Button("View");

        qualityButton.addListener(new ClickListener() {

            private static final long serialVersionUID = 7927755510025754346L;

            @Override
            public void buttonClick(ClickEvent event) {
                reportSection.setValue(qualitySection);
            }
            
        });
        fdaButton.addListener(new ClickListener() {

            private static final long serialVersionUID = -1007067588809145876L;

            @Override
            public void buttonClick(ClickEvent event) {
                reportSection.setValue(pdaSection);
            }
            
        });
        investigatinalButton.addListener(new ClickListener() {

            private static final long serialVersionUID = 675066106800226190L;

            @Override
            public void buttonClick(ClickEvent event) {
                reportSection.setValue(investigationalSection); 
            }
            
        });

        HorizontalLayout panel2 = new HorizontalLayout();
        panel2.setSpacing(true);
        this.addComponent(panel2);
        panel2.addComponent(new Label("Data Quality"));
        panel2.addComponent(qualityButton);

        this.addComponent(new Label("Actionable Oncoproteins"));

        HorizontalLayout panel3 = new HorizontalLayout();
        panel3.setSpacing(true);
        this.addComponent(panel3);
        panel3.addComponent(new Label("FDA approved drugs"));
        panel3.addComponent(fdaButton);

        HorizontalLayout panel4 = new HorizontalLayout();
        panel4.setSpacing(true);
        this.addComponent(panel4);
        panel4.addComponent(new Label("Investigational drugs"));
        panel4.addComponent(investigatinalButton);

        this.addComponent(reportSection);
        this.setSizeUndefined();
        this.setWidth("50%");
    }

    private static final long serialVersionUID = -761592910851078204L;

}
