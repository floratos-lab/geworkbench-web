package org.geworkbenchweb.plugins.pbqdi;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class FDAApprovedSection extends VerticalLayout {

    private static final long serialVersionUID = 3709762091245469430L;

    public FDAApprovedSection() {

        Label richText = new Label("<h1>Rich text example</h1>"
                + "<p>The <b>quick</b> brown fox jumps <sup>over</sup> the <b>lazy</b> dog.</p>"
                + "<p>This text can be edited with the <i>Edit</i> -button</p>"
                + "<ul><li>Mapped Reads: the total number of mapped reads</li><ul>");
        richText.setContentMode(Label.CONTENT_XHTML);

        this.addComponent(new Label("Data Quality"));
        this.addComponent(new Label("The figure below portrays indicators of data quality for the sample:"));

    }
}
