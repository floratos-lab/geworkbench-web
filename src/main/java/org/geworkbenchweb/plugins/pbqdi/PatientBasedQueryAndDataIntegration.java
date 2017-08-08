package org.geworkbenchweb.plugins.pbqdi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.citrus.CitrusDatabase;

import org.geworkbench.service.pbqdi.schema.PbqdiRequest;
import org.geworkbench.service.pbqdi.schema.PbqdiResponse;
import org.geworkbench.service.PbqdiEndpoint;

import org.geworkbenchweb.pojos.PbqdiResult;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Select;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.appfoundation.authentication.SessionHandler;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class PatientBasedQueryAndDataIntegration extends VerticalLayout {

    private static Log log = LogFactory.getLog(PatientBasedQueryAndDataIntegration.class);

    private static final long serialVersionUID = -713233350568178L;

    final private ComboBox cancerTypeComboBox = new ComboBox("TCGA cancer type");
    final private Upload upload = new Upload();
    final private SampleFoundPanel sampleFound = new SampleFoundPanel();
    final private Button analyzeButton = new Button("Analyze");
    final ProgressIndicator indicator = new ProgressIndicator(new Float(0.0));

    private CitrusDatabase db = null;

    private final String SERVICE_URL = GeworkbenchRoot.getAppProperty("pbdqi.service.url");
    private final String OUTPUT_PATH = GeworkbenchRoot.getAppProperty("pbqdi.output.path");
    private final String BASE_WORKING_DIRECTORY = GeworkbenchRoot.getAppProperty("pbqdi.working.directory");
    private final String HTML_LOCATION = GeworkbenchRoot.getAppProperty("html.location");
    private String sampleFile = null;

    private String[] sampleNames;
    private String tumorType = null;

    private final Long owner;

    public PatientBasedQueryAndDataIntegration() {
        try {
            db = new CitrusDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        owner = SessionHandler.get().getId();

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

            ResultView v = null;

            int jobId = new java.util.Random().nextInt(Integer.MAX_VALUE);

            String RESULT_OPTION = GeworkbenchRoot.getAppProperty("result.option");
            if ("random".equalsIgnoreCase(RESULT_OPTION)) { // // random data in place of result
                log.warn("'random result' test feature disabled");
            } else if ("existing".equalsIgnoreCase(RESULT_OPTION)) { // we may want to bypass the R computation for testing
                log.warn("'existing result' test feature disabled");
            } else { // submit PBQDI web service request
                PbqdiRequest request = new PbqdiRequest();
                request.setTumorType(tumorType);
                request.setSampleFile(new java.io.File(sampleFile).getName());

                try {
                    BufferedReader br = new BufferedReader(new FileReader(sampleFile));
                    StringBuffer sb = new StringBuffer();
                    String line = br.readLine();
                    while(line!=null) {
                        sb.append(line).append('\n');
                        line = br.readLine();
                    }
                    br.close();
                    request.setFileContent(sb.toString());
                } catch(IOException e) {
                    e.printStackTrace();
                    PatientBasedQueryAndDataIntegration.this.processError(e.getMessage());
                    return;
                }

                QName qname = new QName(PbqdiEndpoint.NAMESPACE_URI, PbqdiEndpoint.REQUEST_LOCAL_NAME);
                JAXBElement<PbqdiRequest> requestElement = new JAXBElement<PbqdiRequest>(qname, PbqdiRequest.class, request);

                Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
                marshaller.setContextPath("org.geworkbench.service.pbqdi.schema");
                marshaller.setMtomEnabled(true);
                try {
                    MessageFactory mf = MessageFactory.newInstance();
                    WebServiceTemplate template = new WebServiceTemplate(new SaajSoapMessageFactory(mf));
                    template.setMarshaller(marshaller);
                    template.setUnmarshaller(marshaller);

                    PbqdiResponse response = (PbqdiResponse)template.marshalSendAndReceive(SERVICE_URL, requestElement);
                    String classAssignmentsResult = response.getClassAssignment();
                    if(classAssignmentsResult==null) {
                        PatientBasedQueryAndDataIntegration.this.processError("PBQDI web service failed: classAssignmentsResult==null");
                        return;
                    }
                    Map<String, Integer> classAssignments = parseClassAssignments(classAssignmentsResult);

                    String resultPath = HTML_LOCATION + DrugReport.RESULT_PATH + jobId +"/";
                    Files.createDirectories(FileSystems.getDefault().getPath(resultPath));

                    DataHandler resultPackage = response.getResultPackage();
                    ZipEntry entry;
                    byte[] buffer = new byte[8 * 1024];

                    InputStream inputStream = resultPackage.getInputStream();
                    ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                    while( (entry = zipInputStream.getNextEntry())!=null ) {
                        String s = String.format("Entry: %s len %d added %TD",
                                    entry.getName(), entry.getSize(),
                                    new java.util.Date(entry.getTime()));
                        System.out.println(s);

                        String outpath = resultPath + entry.getName();
                        FileOutputStream outputFileStream = new FileOutputStream(outpath);
                        int len = 0;
                        while ((len = zipInputStream.read(buffer)) > 0)
                        {
                            outputFileStream.write(buffer, 0, len);
                        }
                        outputFileStream.close();
                    }
                    zipInputStream.close();
                    inputStream.close();

                    String sampleFileName = sampleFile.substring(sampleFile.lastIndexOf("/")+1, sampleFile.lastIndexOf(".txt"));
                    PbqdiResult result = new PbqdiResult(owner, tumorType, sampleFileName, jobId, classAssignments);
                    FacadeFactory.getFacade().store(result);
                    storedResults.addItem(result);
                    storedResults.setItemCaption(result, result.getSampleFileName()+": Job ID "+result.getJobId());

                    v = new ResultView(result);
                } catch(SOAPException e) {
                    e.printStackTrace();
                    PatientBasedQueryAndDataIntegration.this.processError(e.getMessage());
                    return;
                } catch(IOException e) {
                    e.printStackTrace();
                    PatientBasedQueryAndDataIntegration.this.processError(e.getMessage());
                    return;
                }
            } // end of web servive request

            Window mainWindow = PatientBasedQueryAndDataIntegration.this.getApplication().getMainWindow();
            mainWindow.addWindow(v);
            synchronized (getApplication()) {
                indicator.setVisible(false);
                analyzeButton.setEnabled(true);
            }
        }
    }

    private static Map<String, Integer> parseClassAssignments(String classAssignmentsResult) {
        Map<String, Integer> classAssignments = new HashMap<String, Integer>();
        String[] s = classAssignmentsResult.split("\n");
        for(String x : s) {
            String[] f = x.split("\t");
            classAssignments.put(f[0].trim(), Integer.parseInt(f[1]));
        }
        return classAssignments;
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

    final private ListSelect storedResults = new ListSelect();

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

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("owner", owner);
        List<PbqdiResult> results = FacadeFactory.getFacade()
            .list("Select p from PbqdiResult as p where p.owner=:owner", param);
        //if(results.size()<=0) return;

        storedResults.setItemCaptionMode(Select.ITEM_CAPTION_MODE_EXPLICIT);
        storedResults.removeAllItems();
        for(PbqdiResult x : results) {
            storedResults.addItem(x);
            storedResults.setItemCaption(x, x.getSampleFileName()+": Job ID "+x.getJobId());
        }
        storedResults.setRows(7);
        storedResults.setNullSelectionAllowed(false);
        storedResults.setImmediate(true);
        storedResults.addListener(new Property.ValueChangeListener() {
            static final long serialVersionUID = 1L;
            public void valueChange(ValueChangeEvent event) {
                PbqdiResult selection = (PbqdiResult)event.getProperty().getValue();
                Window mainWindow = PatientBasedQueryAndDataIntegration.this.getApplication().getMainWindow();
                try {
                    mainWindow.addWindow(new ResultView(selection));
                    synchronized (getApplication()) {
                        indicator.setVisible(false);
                        analyzeButton.setEnabled(true);
                    }
                } catch(IOException e) {
                    PatientBasedQueryAndDataIntegration.this.processError(e.getMessage());
                }
            }
        });

        HorizontalLayout titlePanel = new HorizontalLayout();
        titlePanel.setWidth("100%");
        titlePanel.setStyleName("feature-controls");
        Label title = new Label("<span>Results of previous runs</span>", Label.CONTENT_XHTML);
        title.setStyleName("title");
        titlePanel.addComponent(title);
        titlePanel.setExpandRatio(title, 1);
        this.addComponent(titlePanel);
        this.addComponent(storedResults);
    }

}
