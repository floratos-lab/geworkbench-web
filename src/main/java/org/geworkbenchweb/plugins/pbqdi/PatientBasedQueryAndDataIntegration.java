package org.geworkbenchweb.plugins.pbqdi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.plugins.citrus.CitrusDatabase;
import org.geworkbenchweb.visualizations.KaplanMeier;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.VerticalLayout;

public class PatientBasedQueryAndDataIntegration extends VerticalLayout {
    private static final String COLUMN_SAMPLE_NAME = "Sample Name";
    private static final String COLUMN_SUBTYPE = "Subtype";
    private static final String COLUMN_DRUG_PREDICTION = "Drug Prediction";
    private static final String COLUMN_SAMPLE_PER_SUBTYPE = "samples (click to view)";

    Log log = LogFactory.getLog(PatientBasedQueryAndDataIntegration.class);

    private static final long serialVersionUID = -713233350568178L;

    final private ComboBox cancerTypeComboBox = new ComboBox("TCGA cancer type");
    final private Upload upload = new Upload();
    final private Button analyzeButton = new Button("Analyze");
    final private Table resultTable = new Table();
    final private Table samplePerSubtype = new Table();

    private CitrusDatabase db = null;

    public PatientBasedQueryAndDataIntegration() {
        try {
            db = new CitrusDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        upload.setReceiver(new Receiver() {

            private static final long serialVersionUID = 4691896882408478729L;

            @Override
            public OutputStream receiveUpload(String filename, String mimeType) {
                log.debug("receiveUpload " + filename + " " + mimeType);
                FileOutputStream fos = null;
                File file = new File("C:/tmp/uploads/" + filename);
                try {
                    fos = new FileOutputStream(file);
                } catch (final java.io.FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }

                return fos;
            }

        });
        analyzeButton.addListener(new ClickListener() {

            private static final long serialVersionUID = 3057721104002229089L;

            @Override
            public void buttonClick(ClickEvent event) {
                Container container = new IndexedContainer();
                container.addContainerProperty(COLUMN_SAMPLE_NAME, String.class, null);
                container.addContainerProperty(COLUMN_SUBTYPE, Integer.class, 0);
                container.addContainerProperty(COLUMN_DRUG_PREDICTION, String.class, null);
                // FIXME test data
                String[] sampleNames = { "CUMC-ONC-13457", "CUMC-ONC-12786", "CUMC-ONC-12787" };
                for (String sampleName : sampleNames) {
                    Item item = container.addItem(sampleName);
                    item.getItemProperty(COLUMN_SAMPLE_NAME).setValue(sampleName);
                    item.getItemProperty(COLUMN_SUBTYPE).setValue(new Random().nextInt());
                    item.getItemProperty(COLUMN_DRUG_PREDICTION).setValue(sampleName + " report links");
                }

                resultTable.setContainerDataSource(container);

                Container container2 = new IndexedContainer();
                container2.addContainerProperty(COLUMN_SUBTYPE, Integer.class, 0);
                container2.addContainerProperty(COLUMN_SAMPLE_PER_SUBTYPE, String.class, null);
                // FIXME test data
                for (int subtype = 0; subtype <= 4; subtype++) {
                    Item item = container2.addItem(subtype);
                    item.getItemProperty(COLUMN_SUBTYPE).setValue(subtype);
                    item.getItemProperty(COLUMN_SAMPLE_PER_SUBTYPE).setValue(subtype + " report links");
                }

                samplePerSubtype.setContainerDataSource(container2);
            }

        });
    }

    @Override
    public void attach() {
        super.attach();

        this.setSpacing(true);
        this.addComponent(new Label(
                "This site allows you to upload an RNA-Seq read count file for one or more patient tumor samples of a single type and compare them with existing TCGA samples from the same tumor type. The comparison is performed at the level of the VIPER-predicted activity of regulatory proteins (signaling and transcription factor) and results in assignment of the uploaded samples to computationally derived subtypes of the tumor."));
        this.addComponent(new Label(
                "A drug report is also prepared showing drugs that target the most active regulatory proteins in the sample, as determined using VIPER."));
        this.addComponent(cancerTypeComboBox);
        this.addComponent(upload);
        this.addComponent(analyzeButton);
        this.addComponent(resultTable);
        this.addComponent(KaplanMeier.createInstance());
        this.addComponent(samplePerSubtype);

        String[] cancerTypes = db.getCancerTypes();
        for (String s : cancerTypes)
            cancerTypeComboBox.addItem(s);
        cancerTypeComboBox.setNullSelectionAllowed(false);
        cancerTypeComboBox.setImmediate(true);
    }

}
