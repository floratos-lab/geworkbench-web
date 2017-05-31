package org.geworkbenchweb.plugins.pbqdi;

import java.io.File;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Window;

public class DrugReport extends Window {
    final static public String RESULT_PATH = "/pbqdi_results/";

    public DrugReport(String tumorType, final int jobId, final String htmlReport) {
        this.setModal(true);
        this.setClosable(true);
        ((AbstractOrderedLayout) this.getContent()).setSpacing(true);
        this.setResizable(true);
        this.setCaption("Columbia/CPTAC Patient Sample Drug Report");
        this.setImmediate(true);

        Embedded e = new Embedded(null, new ExternalResource(RESULT_PATH + jobId + File.separator + htmlReport));
        e.setType(Embedded.TYPE_BROWSER);
        e.setWidth("100%");
        e.setHeight("600px");
        this.addComponent(e);

        this.setWidth("75%");
    }

    private static final long serialVersionUID = -761592910851078204L;

}
