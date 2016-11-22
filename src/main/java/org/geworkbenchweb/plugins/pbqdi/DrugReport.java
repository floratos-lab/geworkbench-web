package org.geworkbenchweb.plugins.pbqdi;

import java.io.File;

import com.vaadin.terminal.FileResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

public class DrugReport extends Window {

    Panel panel = new Panel();

    public DrugReport(String tumorType, final String report, final String qualitySection, final String pdaSection, final String investigationalSection) {
        this.setModal(true);
        this.setClosable(true);
        ((AbstractOrderedLayout) this.getContent()).setSpacing(true);
        this.setResizable(true);
        this.setCaption("Columbia/CPTAC Patient Sample Drug Report");
        this.setImmediate(true);

        this.addComponent(new Label("<b>Drug Prediction Report</b>",
                Label.CONTENT_XHTML));
        Button pdfButton = new Button("Download Full Report as PDF");
        pdfButton.setStyleName(BaseTheme.BUTTON_LINK);
        pdfButton.addListener(new ClickListener() {

            private static final long serialVersionUID = -6270828819674891537L;

            @Override
            public void buttonClick(ClickEvent event) {
                DrugReport.this.open(new FileResource(new File(report), DrugReport.this.getApplication()), "_blank");
            }

        });

        Button qualityButton = new Button("Data Quality");
        qualityButton.setStyleName(BaseTheme.BUTTON_LINK);
        Button oncologyButton = new Button("Oncology drugs");
        oncologyButton.setStyleName(BaseTheme.BUTTON_LINK);
        Button nononcologyButton = new Button("Nononcology drugs");
        nononcologyButton.setStyleName(BaseTheme.BUTTON_LINK);
        Button investigatinalButton = new Button("Investigational drugs");
        investigatinalButton.setStyleName(BaseTheme.BUTTON_LINK);

        HorizontalLayout panel1 = new HorizontalLayout();
        panel1.setSpacing(true);
        this.addComponent(panel1);
        panel1.addComponent(pdfButton);
        panel1.addComponent(qualityButton);
        panel1.addComponent(oncologyButton);
        panel1.addComponent(nononcologyButton);
        panel1.addComponent(investigatinalButton);

        final Label reportSection = new Label("", Label.CONTENT_XHTML);
        reportSection.setValue(qualitySection+pdaSection+investigationalSection);


        qualityButton.addListener(new ClickListener() {

            private static final long serialVersionUID = 7927755510025754346L;

            @Override
            public void buttonClick(ClickEvent event) {
                panel.setScrollTop(0);
            }
            
        });
        oncologyButton.addListener(new ClickListener() {

            private static final long serialVersionUID = -1007067588809145876L;

            @Override
            public void buttonClick(ClickEvent event) {
                panel.setScrollTop(100); // TODO temporary arbitrary offset
            }
            
        });
        nononcologyButton.addListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                panel.setScrollTop(200); // TODO temporary arbitrary offset
            }
            
        });
        investigatinalButton.addListener(new ClickListener() {

            private static final long serialVersionUID = 675066106800226190L;

            @Override
            public void buttonClick(ClickEvent event) {
                panel.setScrollTop(300); // TODO temporary arbitrary offset
            }
            
        });

        panel.setWidth("100%");
        panel.setHeight("600px");
        panel.setScrollable(true);
        VerticalLayout layout = new VerticalLayout(); 
        layout.addComponent(reportSection);
        panel.setContent(layout);
        this.addComponent(panel);

        this.setSizeUndefined();
        this.setWidth("75%");
    }

    private static final long serialVersionUID = -761592910851078204L;

}
