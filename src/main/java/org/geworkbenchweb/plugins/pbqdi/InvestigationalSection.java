package org.geworkbenchweb.plugins.pbqdi;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class InvestigationalSection extends VerticalLayout {

    private static final long serialVersionUID = 3709762091245469430L;

    public InvestigationalSection() {

        Label richText = new Label();
        richText.setContentMode(Label.CONTENT_XHTML);

        List<List<String>> imageFiles = new ArrayList<List<String>>();
        for (int i = 0; i < 5; i++) {
            List<String> x = new ArrayList<String>();
            x.add("some image files");
            imageFiles.add(x);
        }
        List<List<String>> drugs = new ArrayList<List<String>>();
        for (int i = 0; i < 5; i++) {
            List<String> x = new ArrayList<String>();
            x.add("drug name and description");
            drugs.add(x);
        }

        int numberOfCards = imageFiles.size();

        StringBuilder sb = new StringBuilder("<h1>Investigational drugs</h1><table>");
        for (int i = 0; i < numberOfCards; i++) {
            sb.append("<tr><td>").append(i + 1).append("</td>").append("<td>");
            for (int j = 0; j < imageFiles.get(i).size(); j++) {
                sb.append("<img src='").append(imageFiles.get(i).get(j)).append("'/>");
            }
            sb.append("</td><td>");
            for (int j = 0; j < drugs.get(i).size(); j++) {
                sb.append(drugs.get(i).get(j));
            }
            sb.append("</td></tr>");
        }
        sb.append("</table>");

        this.addComponent(richText);
        this.addComponent(new Label(sb.toString(), Label.CONTENT_XHTML));

    }
}
