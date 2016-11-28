package org.geworkbenchweb.plugins.pbqdi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.citrus.CitrusDatabase;

import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class PatientBasedQueryAndDataIntegration extends VerticalLayout {

    Log log = LogFactory.getLog(PatientBasedQueryAndDataIntegration.class);

    private static final long serialVersionUID = -713233350568178L;

    final private ComboBox cancerTypeComboBox = new ComboBox("TCGA cancer type");
    final private Upload upload = new Upload();
    final private SampleFoundPanel sampleFound = new SampleFoundPanel();
    final private Button analyzeButton = new Button("Analyze");
    final ProgressIndicator indicator = new ProgressIndicator(new Float(0.0));

    private CitrusDatabase db = null;

    private final String R_PATH = GeworkbenchRoot.getAppProperty("r.path");
    private final String OUTPUT_PATH = GeworkbenchRoot.getAppProperty("pbqdi.output.path");
    private final String WORKING_DIRECTORY = GeworkbenchRoot.getAppProperty("pbqdi.working.directory");
    private final String ERROR_FILE = GeworkbenchRoot.getAppProperty("pbqdi.error.file");
    private final String HTML_LOCATION = GeworkbenchRoot.getAppProperty("html.location");
    private String sampleFile = null;

    private String[] sampleNames;
    private String tumorType = null;

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
                sampleFile = OUTPUT_PATH + filename;
                FileOutputStream fos = null;
                File file = new File(sampleFile);
                try {
                    fos = new FileOutputStream(file);
                } catch (final java.io.FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }

                return fos;
            }

        });
        upload.addListener(new Upload.SucceededListener() {

            private static final long serialVersionUID = 1492448712654675230L;

            @Override
            public void uploadSucceeded(Upload.SucceededEvent event) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(sampleFile));
                    String firstLine = br.readLine();
                    sampleNames = firstLine.split("\t");
                    br.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                sampleFound.setData(sampleNames);
                sampleFound.setVisible(true);
            }
        });
        analyzeButton.addListener(new ClickListener() {

            private static final long serialVersionUID = 3057721104002229089L;

            @Override
            public void buttonClick(ClickEvent event) {
                tumorType = db.getCancerType((String) cancerTypeComboBox.getValue());
                if (tumorType == null || sampleNames == null) {
                    log.error("invalid input");
                    return;
                }

                indicator.setVisible(true);
                final WorkThread thread = new WorkThread();
                thread.start();
                analyzeButton.setEnabled(false);
            }

        });
    }

    private class WorkThread extends Thread {
        @Override
        public void run() {
            String kaplan = "test1.png";

            String reportFilename = sampleFile.substring(0, sampleFile.indexOf(".txt")) + "OncotargetReport.pdf";

            String qualitySection = "";
            String pdaSection = "";
            String investigationalSection = "";

            ResultData result = null;
            String RESULT_OPTION = GeworkbenchRoot.getAppProperty("result.option");
            if (RESULT_OPTION.equalsIgnoreCase("random")) { // // random data in place of result
                result = ResultData.randomTestData();
            } else if (RESULT_OPTION.equalsIgnoreCase("existing")) { // we may want to bypass the R computation for testing
                result = new ResultData(readQualitySection(), readDrugSection("oncology.txt"),
                        readDrugSection("non-oncology.txt"), readDrugSection("investigational.txt"));
            } else { // real R execution

            ProcessBuilder pb1 = new ProcessBuilder(R_PATH + "Rscript", "--vanilla", WORKING_DIRECTORY+"classifySamples.r",
                    tumorType, sampleFile, WORKING_DIRECTORY, ERROR_FILE);
            pb1.directory(new File(WORKING_DIRECTORY));
            try {
                Process process = pb1.start();
                if (log.isDebugEnabled()) {
                    InputStream stream = process.getErrorStream();
                    byte[] b = new byte[1024];
                    int n = -1;
                    while ((n = stream.read(b)) >= 0) {
                        System.out.println(":::" + new String(b, 0, n));
                    }
                }
                int exit = process.waitFor();
                if (exit != 0) {
                    log.error("something went wrong with classification script: exit value "+exit);
                    PatientBasedQueryAndDataIntegration.this.processError("something went wrong with classification script: exit value "+exit);
                    return;
                }
                if (new File(WORKING_DIRECTORY+kaplan).exists()) {
                    log.debug("Kanpan-Merier curve image created");
                } else {
                    log.error("something went wrong creating Kaplan-Meirer curve image");
                    PatientBasedQueryAndDataIntegration.this.processError("something went wrong creating Kaplan-Meirer curve image");
                    return;
                }
            } catch (IOException | InterruptedException e1) {
                e1.printStackTrace();
                PatientBasedQueryAndDataIntegration.this.processError(e1.getMessage());
                return;
            }
            ProcessBuilder pb2 = new ProcessBuilder(R_PATH + "rscript", "--vanilla", WORKING_DIRECTORY+"rununsupervised.r", tumorType,
                    sampleFile, WORKING_DIRECTORY, ERROR_FILE);
            pb2.directory(new File(WORKING_DIRECTORY));

            try {
                Process process = pb2.start();
                if (log.isDebugEnabled()) {
                    InputStream stream = process.getErrorStream();
                    byte[] b = new byte[1024];
                    int n = -1;
                    while ((n = stream.read(b)) >= 0) {
                        System.out.println(":::" + new String(b, 0, n));
                    }
                }
                int exit = process.waitFor();
                if (exit == 0 && new File(reportFilename).exists()) {
                    log.debug("report created");
                } else {
                    log.error("something went wrong with drug report script: exit value "+exit);
                    PatientBasedQueryAndDataIntegration.this.processError("something went wrong with drug report script: exit value "+exit);
                    return;
                }
            } catch (IOException | InterruptedException e1) {
                e1.printStackTrace();
                PatientBasedQueryAndDataIntegration.this.processError(e1.getMessage());
                return;
            }
            result = new ResultData(readQualitySection(), readDrugSection("oncology.txt"),
                        readDrugSection("non-oncology.txt"), readDrugSection("investigational.txt"));

            } // end of real R execution

            qualitySection = createQualitySection(result);
            pdaSection = createFDASection(result);
            investigationalSection = createInvestigationalSection(result);

            if(! new File(WORKING_DIRECTORY + "classAssignments.txt").exists()) { // debug
                log.debug(new File(WORKING_DIRECTORY + "classAssignments.txt").getAbsolutePath()+" does not exists");
            }
            Map<String, Integer> classAssignments = new HashMap<String, Integer>();
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(WORKING_DIRECTORY + "classAssignments.txt"));
                String line = br.readLine();
                while (line != null && line.trim().length() > 0) {
                    String[] f = line.split("\t");
                    classAssignments.put(f[0].trim(), Integer.parseInt(f[1]));
                    line = br.readLine();
                }
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileResource resource =  new FileResource(new File(WORKING_DIRECTORY+kaplan), PatientBasedQueryAndDataIntegration.this.getApplication());
            ResultView v = new ResultView(sampleNames, tumorType, classAssignments, reportFilename, resource, qualitySection, pdaSection, investigationalSection);
            Window mainWindow = PatientBasedQueryAndDataIntegration.this.getApplication().getMainWindow();
            mainWindow.addWindow(v);
            synchronized (getApplication()) {
                indicator.setVisible(false);
                analyzeButton.setEnabled(true);
            }
        }
    }

    private DrugResult readDrugSection(String filename) {
        List<List<String>> images = new ArrayList<List<String>>();
        List<List<IndividualDrugInfo>> drugs = new ArrayList<List<IndividualDrugInfo>>();

        char fieldId = 0; // I for image, N for drug names, D for drug
                          // description, A for accessions
        List<String> img = null;
        List<String> drugNames = null;
        List<String> descriptions = null;
        List<String> accessions = null;
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(WORKING_DIRECTORY + filename));
            String line = br.readLine();
            while (line != null) {
                if (line.equals("images")) {
                    fieldId = 'I';
                    img = new ArrayList<String>();
                } else if (line.equals("drugNames")) {
                    fieldId = 'N';
                    drugNames = new ArrayList<String>();
                } else if (line.equals("drugDescriptions")) {
                    fieldId = 'D';
                    descriptions = new ArrayList<String>();
                } else if (line.equals("drugAccessions")) {
                    fieldId = 'A';
                    accessions = new ArrayList<String>();
                } else if (line.equals("%%")) {
                    // another card
                    fieldId = 0;
                    System.out.println(img+":"+(img==null?line+","+filename:img.size()));
                    images.add(img);
                    List<IndividualDrugInfo> drugsForOneRow = new ArrayList<IndividualDrugInfo>();
                    for (int i = 0; i < drugNames.size(); i++) {
                        drugsForOneRow
                                .add(new IndividualDrugInfo(drugNames.get(i), descriptions.get(i), accessions.get(i)));
                    }
                    drugs.add(drugsForOneRow);
                } else {
                    switch (fieldId) {
                    case 'I':
                        img.add("/cptac/images/" + line.substring(line.lastIndexOf("/") + 1, line.lastIndexOf(".pdf"))
                                + ".png");
                        break;
                    case 'N':
                        drugNames.add(line);
                        break;
                    case 'D':
                        descriptions.add(line);
                        break;
                    case 'A':
                        accessions.add(line);
                        break;
                    }
                }
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new DrugResult(images, drugs);
    }

    private String createFDASection(final ResultData result) {
        DrugResult oncology = result.oncology;
        List<List<String>> images = oncology.images;
        List<List<IndividualDrugInfo>> drugs = oncology.drugs;

        StringBuilder sb = new StringBuilder("<h1>FDA Approved Drugs</h1><h2>Oncology Drugs</h2><table>");
        for (int i = 0; i < images.size(); i++) {
            sb.append("<tr><td>").append(i + 1).append("</td>").append("<td>");
            for (String img : images.get(i)) {
                sb.append("<img src='").append(img).append(" '/>");
            }
            sb.append("</td><td><ul>");
            for (IndividualDrugInfo d : drugs.get(i)) {
                sb.append("<li><a href='http://www.drugbank.ca/drugs/").append(d.accession).append("' target=_blank>").append(d.name).append("</a> ")
                        .append(d.description).append("</li>");
            }
            sb.append("</ul></td></tr>");
        }
        sb.append("</table><h2>Non-oncology Drugs</h2><table>");

        DrugResult nononcology = result.nononcology;
        images = nononcology.images;
        drugs = nononcology.drugs;
        for (int i = 0; i < images.size(); i++) {
            sb.append("<tr><td>").append(i + 1).append("</td>").append("<td>");
            for (String img : images.get(i)) {
                sb.append("<img src='").append(img).append(" '/>");
            }
            sb.append("</td><td><ul>");
            for (IndividualDrugInfo d : drugs.get(i)) {
                sb.append("<li><a href='http://www.drugbank.ca/drugs/").append(d.accession).append("' target=_blank>").append(d.name).append("</a> ")
                        .append(d.description).append("</li>");
            }
            sb.append("</ul></td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private String createInvestigationalSection(final ResultData result) {
        DrugResult investigational = result.investigational;
        List<List<String>> images = investigational.images;
        List<List<IndividualDrugInfo>> drugs = investigational.drugs;

        StringBuilder sb = new StringBuilder("<h1>Investigational drugs</h1><table>");
        for (int i = 0; i < images.size(); i++) {
            sb.append("<tr><td>").append(i + 1).append("</td>").append("<td>");
            for (String img : images.get(i)) {
                sb.append("<img src='").append(img).append("' />");
            }
            sb.append("</td><td><ul>");
            for (IndividualDrugInfo d : drugs.get(i)) {
                sb.append("<li><a href='http://www.drugbank.ca/drugs/").append(d.accession).append("' target=_blank>").append(d.name).append("</a> ")
                        .append(d.description).append("</li>");
            }
            sb.append("</ul></td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private String createQualitySection(final ResultData result) {

        StringBuilder sb = new StringBuilder("<h1>Data Quality</h1>"
                + "<p>The figure below portrays indicators of data quality for the sample:</p>"
                + "<ul><li>Mapped Reads: the total number of mapped reads</li><li>Detected genes: the number of detected genes with at least 1 mapped read</li><li>Expressed genes: the number of expressed genes inferred from the distribution of the digital expression data</li></ul>");

        for (int i = 0; i < result.dataQualityImages.length; i++) {
            sb.append("<img src='")
            .append(result.dataQualityImages[i])
            .append("' />");
        }
        return sb.toString();
    }

    private String[] readQualitySection() {
        List<String> list = new ArrayList<String>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(WORKING_DIRECTORY + "qc.txt"));
            String line = br.readLine();
            while (line != null) {
                list.add("/cptac/images/" + line.substring(line.lastIndexOf("/") + 1, line.lastIndexOf(".pdf"))
                        + ".png");
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list.toArray(new String[list.size()]);
    }

    private void processError(String message) {
        Window mainWindow = getApplication().getMainWindow();
        MessageBox mb = new MessageBox(mainWindow, "Analysis Problem", MessageBox.Icon.ERROR, message,
                new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
        mb.show();
        synchronized (getApplication()) {
            indicator.setVisible(false);
            analyzeButton.setEnabled(true);
        }
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
        this.addComponent(sampleFound);
        this.addComponent(analyzeButton);
        indicator.setIndeterminate(true);
        indicator.setPollingInterval(500);
        indicator.setVisible(false);
        this.addComponent(indicator);

        String[] cancerTypes = db.getCancerTypes();
        for (String s : cancerTypes)
            cancerTypeComboBox.addItem(s);
        cancerTypeComboBox.setNullSelectionAllowed(false);
        cancerTypeComboBox.setImmediate(true);
    }

}
