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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.citrus.CitrusDatabase;

import com.vaadin.Application;
import com.vaadin.terminal.FileResource;
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
            String kaplan = "test1.png";
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
                if (new File(WORKING_IDRECTORY+kaplan).exists()) {
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
            String qualitySection = readQualitySection();
            String pdaSection = readPDASection();
            String investigationalSection = readInvestigationalSection();
            Window mainWindow = PatientBasedQueryAndDataIntegration.this.getApplication().getMainWindow();
            Application a = PatientBasedQueryAndDataIntegration.this.getApplication();
            FileResource resource =  new FileResource(new File(WORKING_IDRECTORY+kaplan), a);
            ResultView v = new ResultView(sampleNames, tumorType, classAssignments, drugReports, resource, qualitySection, pdaSection, investigationalSection);
            mainWindow.addWindow(v);
            synchronized (getApplication()) {
                indicator.setVisible(false);
                analyzeButton.setEnabled(true);
            }
        }
    }

    private String readInvestigationalSection() {
        String texFile = sampleFile.substring(0, sampleFile.indexOf(".txt")) + "OncotargetReport.tex"; // this is absolute path
        StringBuilder sb = new StringBuilder();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(texFile));
            String line = br.readLine();
            boolean in = false;
            while(line!=null) {
                if(line.equals("\\subsection*{Investigational drugs}")) {
                    in = true;
                } else if(line.startsWith("\\end")||line.startsWith("\\begin")) {
                } else if(in && line.trim().startsWith("\\input")) {
                    sb.append("[INPUT:"+line+"]");
                } else if(in) {
                    sb.append(line);
                }
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String readPDASection() {
        String texFile = sampleFile.substring(0, sampleFile.indexOf(".txt")) + "OncotargetReport.tex"; // this is absolute path
        StringBuilder sb = new StringBuilder();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(texFile));
            String line = br.readLine();
            boolean in = false;
            while(line!=null) {
                if(line.equals("\\addcontentsline{toc}{subsection}{FDA approved drugs}")) {
                    in = true;
                } else if(line.equals("\\subsection*{Investigational drugs}")) {
                    in = false;
                } else if(line.startsWith("\\end")||line.startsWith("\\begin")) {
                } else if(in && line.trim().startsWith("\\input")) {
                    String tabFile = extractTabFileName(line);
                    String tabFileFullPath = texFile.substring(0, texFile.lastIndexOf("/")+1)+tabFile;
                    sb.append( parseTabFile(tabFileFullPath) );
                } else if(in) {
                    if(line.startsWith("{\\small")) line = line.substring(line.indexOf(" "));
                    if(line.endsWith("}")) line = line.substring(0, line.length()-1);
                    sb.append(line);
                }
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String readQualitySection() {
        String texFile = sampleFile.substring(0, sampleFile.indexOf(".txt")) + "OncotargetReport.tex"; // this is absolute path
        StringBuilder sb = new StringBuilder();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(texFile));
            String line = br.readLine();
            boolean in = false;
            while(line!=null) {
                if(line.equals("\\subsection*{Data Quality}")) {
                    in = true;
                } else if(line.equals("\\section*{Actionable Oncoproteins}")) {
                    in = false;
                } else if(line.startsWith("%")||line.startsWith("\\begin")) {
                } else if(line.trim().startsWith("\\includegraphics")) {
                    for(String s : extractImageNames(line)) {
                        sb.append("["+s+"]");
                    }
                } else if(in) {
                    sb.append(line);
                }
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static String parseIndex(String s) {
        Pattern pattern = Pattern.compile("\\\\scriptsize\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(s);
        if(matcher.find()) return matcher.group(1);
        else return "[]";
    }
    private static String parseImage(String s) {
        Pattern pattern = Pattern.compile("\\\\includegraphics\\[.*?\\]\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(s);
        if(matcher.find()) return matcher.group(1);
        else return "[]";
    }
    private static String parseDescription(String s) {
        Pattern pattern = Pattern.compile("\\\\textbf\\{(.+?)\\}|\\\\scriptsize\\{(.+)\\}");
        Matcher matcher = pattern.matcher(s);
        StringBuilder sb = new StringBuilder();
        while(matcher.find()) {
            String name = matcher.group(1);
            String description = matcher.group(2);
            if(name!=null) sb.append(name);
            if(description!=null) sb.append(description);
        }
        return sb.toString();
    }
    
    private static String parseTabFile(String filename) throws IOException {
        BufferedReader tabReader = new BufferedReader(new FileReader(filename ));
        String tabLine = tabReader.readLine();
        List<String> rows = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        while(tabLine!=null) {
            sb.append(tabLine);
            if(tabLine.endsWith("\\\\")) {
                String[] td = sb.toString().split("&");
                assert td.length==3;
                StringBuilder content = new StringBuilder();
                content.append("<td>").append(parseIndex(td[0])).append("</td>");
                content.append("<td>").append("IMG:"+parseImage(td[1])).append("</td>");
                content.append("<td>").append(parseDescription(td[2])).append("</td>");
                rows.add( content.toString() );
                sb = new StringBuilder();
            }
            tabLine = tabReader.readLine();
        }
        tabReader.close();

        sb = new StringBuilder("<table>");
        for(String s: rows) {
            sb.append("<tr>").append(s).append("</tr>");
        }
        return sb.append("</table>").toString();
    }

    public static void main(String[] args) throws IOException {
        System.out.println(parseTabFile("F:/cptac_project/test1/oncoTarget-FDA-oncology-CUAC1468.tab"));
    }

    private static String extractTabFileName(String line) {
        Pattern pattern = Pattern.compile("\\\\input\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(line);
        if(matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    private static String[] extractImageNames(String line) {
        Pattern pattern = Pattern.compile("\\\\includegraphics\\[.*?\\]\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(line);
        List<String> names = new ArrayList<String>();
        while(matcher.find()) {
            String imageName = matcher.group(1);
            names.add(imageName);
        }
        return names.toArray(new String[0]);
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
