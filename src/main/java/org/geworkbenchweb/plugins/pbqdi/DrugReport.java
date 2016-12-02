package org.geworkbenchweb.plugins.pbqdi;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Window;

public class DrugReport extends Window {

    public DrugReport(String tumorType, final String report, final String htmlReport) {
        this.setModal(true);
        this.setClosable(true);
        ((AbstractOrderedLayout) this.getContent()).setSpacing(true);
        this.setResizable(true);
        this.setCaption("Columbia/CPTAC Patient Sample Drug Report");
        this.setImmediate(true);

        Embedded e = new Embedded(null, new ExternalResource("/cptac/reports/" + htmlReport));
        e.setType(Embedded.TYPE_BROWSER);
        e.setWidth("100%");
        e.setHeight("400px");
        this.addComponent(e);

        this.setSizeUndefined();
        this.setWidth("75%");
    }

    private static final long serialVersionUID = -761592910851078204L;

}
