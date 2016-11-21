package org.geworkbenchweb.plugins.pbqdi;

import java.io.File;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

public class DrugReport extends Window {

    Embedded e = new Embedded("", new ExternalResource("/cptac/1/dataquality.html"));
    Embedded e2 = new Embedded("", new ExternalResource("/cptac/1/drugreport.html"));
    Embedded e3 = new Embedded("", new ExternalResource("/cptac/1/investigational.html"));
    Panel panel = new Panel();

    public DrugReport(String tumorType, final String report, final String qualitySection, final String pdaSection, final String investigationalSection) {
        this.setModal(true);
        this.setClosable(true);
        ((AbstractOrderedLayout) this.getContent()).setSpacing(true);
        this.setResizable(true);
        this.setCaption("Columbia/CPTAC Patient Sample Drug Report");
        this.setImmediate(true);

        this.addComponent(new Label("<b>Drug Report using " + tumorType + " data</b>",
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
        HorizontalLayout panel1 = new HorizontalLayout();
        panel1.setSpacing(true);
        this.addComponent(panel1);
        panel1.addComponent(pdfButton);

        final Label reportSection = new Label("", Label.CONTENT_XHTML);
        reportSection.setValue(qualitySection+pdaSection+investigationalSection);

        Button qualityButton = new Button("Data Quality");
        qualityButton.setStyleName(BaseTheme.BUTTON_LINK);
        Button fdaButton = new Button("FDA approved drugs");
        fdaButton.setStyleName(BaseTheme.BUTTON_LINK);
        Button investigatinalButton = new Button("Investigational drugs");
        investigatinalButton.setStyleName(BaseTheme.BUTTON_LINK);

        qualityButton.addListener(new ClickListener() {

            private static final long serialVersionUID = 7927755510025754346L;

            @Override
            public void buttonClick(ClickEvent event) {
                panel.setScrollTop(0);
            }
            
        });
        fdaButton.addListener(new ClickListener() {

            private static final long serialVersionUID = -1007067588809145876L;

            @Override
            public void buttonClick(ClickEvent event) {
                panel.setScrollTop(100); // TODO temporary arbitrary offset
            }
            
        });
        investigatinalButton.addListener(new ClickListener() {

            private static final long serialVersionUID = 675066106800226190L;

            @Override
            public void buttonClick(ClickEvent event) {
                panel.setScrollTop(300); // TODO temporary arbitrary offset
            }
            
        });

        HorizontalLayout panel2 = new HorizontalLayout();
        panel2.setSpacing(true);
        this.addComponent(panel2);
        panel2.addComponent(qualityButton);

        this.addComponent(new Label("Actionable Oncoproteins"));

        HorizontalLayout panel3 = new HorizontalLayout();
        panel3.setSpacing(true);
        this.addComponent(panel3);
        panel3.addComponent(fdaButton);

        HorizontalLayout panel4 = new HorizontalLayout();
        panel4.setSpacing(true);
        this.addComponent(panel4);
        panel4.addComponent(investigatinalButton);

        /*
        e.setType(Embedded.TYPE_OBJECT);
        e.setSizeFull();
        e2.setType(Embedded.TYPE_BROWSER);
        e2.setSizeFull();
        e3.setType(Embedded.TYPE_OBJECT);
        e3.setSizeFull();

        panel.setWidth("100%");
        panel.setHeight("600px");
        panel.setScrollable(true);
        VerticalLayout layout = new VerticalLayout(); 
        layout.addComponent(e);
        layout.addComponent(e2);
        layout.addComponent(e3);
        layout.setSizeFull();
        panel.setContent(layout);
        this.addComponent(panel);*/
        panel.setWidth("100%");
        panel.setHeight("600px");
        panel.setScrollable(true);
        VerticalLayout layout = new VerticalLayout(); 
        layout.addComponent(reportSection);
        panel.setContent(layout);
        this.addComponent(panel);

        this.setSizeUndefined();
        this.setWidth("50%");
    }

    private static final long serialVersionUID = -761592910851078204L;

}
