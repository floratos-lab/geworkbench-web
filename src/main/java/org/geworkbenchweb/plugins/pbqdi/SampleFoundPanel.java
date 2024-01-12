package org.geworkbenchweb.plugins.pbqdi;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class SampleFoundPanel extends HorizontalLayout {

    private static final long serialVersionUID = 4823126926573145551L;

    final private Label numberLabel = new Label();
    final private VerticalLayout panel = new VerticalLayout();

    public SampleFoundPanel() {
        this.addComponent(new Label("Patient samples found in file:"));
        this.addComponent(numberLabel);
        this.addComponent(panel);

        this.setSpacing(true);
        this.setVisible(false);
    }

    public void setData(String[] list) {
        panel.removeAllComponents();
        for (String s : list)
            panel.addComponent(new Label(s));
        numberLabel.setValue(String.valueOf(list.length));
    }
}
