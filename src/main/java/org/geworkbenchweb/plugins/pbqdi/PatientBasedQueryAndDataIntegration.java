package org.geworkbenchweb.plugins.pbqdi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.citrus.CitrusDatabase;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

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
    private final String WORKING_IDRECTORY = GeworkbenchRoot.getAppProperty("pbqdi.working.directory");
    private final String ERROR_FILE = GeworkbenchRoot.getAppProperty("pbqdi.error.file");
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
            ProcessBuilder pb1 = new ProcessBuilder(R_PATH + "rscript", "--vanilla", WORKING_IDRECTORY+"classifySamples.r",
                    tumorType, sampleFile, WORKING_IDRECTORY, ERROR_FILE);
            pb1.directory(new File(WORKING_IDRECTORY));
            Map<String, Integer> classAssignments = new HashMap<String, Integer>();
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
                if (exit == 0) {
                    BufferedReader br = new BufferedReader(new FileReader(WORKING_IDRECTORY + "classAssignments.txt"));
                    String line = br.readLine();
                    while (line != null && line.trim().length() > 0) {
                        String[] f = line.split("\t");
                        classAssignments.put(f[0].trim(), Integer.parseInt(f[1]));
                        line = br.readLine();
                    }
                    br.close();
                } else {
                    log.error("something went wrong with classification script: exit value "+exit);
                    PatientBasedQueryAndDataIntegration.this.processError("something went wrong with classification script: exit value "+exit);
                    return;
                }
            } catch (IOException | InterruptedException e1) {
                e1.printStackTrace();
                PatientBasedQueryAndDataIntegration.this.processError(e1.getMessage());
                return;
            }
            String command = R_PATH+"rscript --vanilla rununsupervised.r " + tumorType + " " + sampleFile + " "
                    + WORKING_IDRECTORY + " " + ERROR_FILE;
            log.debug("command to run:\n" + command);
            ProcessBuilder pb2 = new ProcessBuilder(R_PATH + "rscript", "--vanilla", WORKING_IDRECTORY+"rununsupervised.r", tumorType,
                    sampleFile, WORKING_IDRECTORY, ERROR_FILE);
            pb2.directory(new File(WORKING_IDRECTORY));
            String reportFilename = sampleFile.substring(0, sampleFile.indexOf(".txt")) + "OncotargetReport.pdf";
            log.debug("expected report name: "+reportFilename);
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
            String[] drugReports = new String[sampleNames.length];
            for (int i = 0; i < sampleNames.length; i++) {
                drugReports[i] = reportFilename; // FIXME it should be multiple reports that match the sample numbers
            }
            Window mainWindow = PatientBasedQueryAndDataIntegration.this.getApplication().getMainWindow();
            ResultView v = new ResultView(sampleNames, tumorType, classAssignments, drugReports, null);
            mainWindow.addWindow(v);
            synchronized (getApplication()) {
                indicator.setVisible(false);
                analyzeButton.setEnabled(true);
            }
        }
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
