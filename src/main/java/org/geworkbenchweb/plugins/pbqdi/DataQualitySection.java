package org.geworkbenchweb.plugins.pbqdi;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class DataQualitySection extends VerticalLayout {

    private static final long serialVersionUID = 3709762091245469430L;

    private Label text = new Label();

    public DataQualitySection() {
        text.setContentMode(Label.CONTENT_XHTML);
        this.addComponent(text);
    }

    public void setResultData(ResultData x) {
        text.setValue("<h1>Data Quality</h1>"
                + "<p>The figure below portrays indicators of data quality for the sample:</p>"
                + "<ul><li>Mapped Reads: the total number of mapped reads</li><li>Detected genes: the number of detected genes with at least 1 mapped read</li><li>Expressed genes: the number of expressed genes inferred from the distribution of the digital expression data</li><ul>");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < x.dataQualityImages.length; i++) {
            sb.append("<img src='").append(x.dataQualityImages[i]).append("' />");
        }

        this.addComponent(text);
        this.addComponent(new Label(sb.toString(), Label.CONTENT_XHTML));
    }

    public DataQualitySection(final ResultData x) {

        text = new Label(
                "<h1>Data Quality</h1>" + "<p>The figure below portrays indicators of data quality for the sample:</p>"
                        + "<ul><li>Mapped Reads: the total number of mapped reads</li><li>Detected genes: the number of detected genes with at least 1 mapped read</li><li>Expressed genes: the number of expressed genes inferred from the distribution of the digital expression data</li><ul>",
                Label.CONTENT_XHTML);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < x.dataQualityImages.length; i++) {
            sb.append("<img src='").append(x.dataQualityImages[i]).append("' />");
        }

        this.addComponent(text);
        this.addComponent(new Label(sb.toString(), Label.CONTENT_XHTML));

    }
}
