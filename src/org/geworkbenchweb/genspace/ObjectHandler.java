package org.geworkbenchweb.genspace;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.SwingWorker;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.components.genspace.server.stubs.Transaction;
import org.geworkbench.events.AnalysisInvokedEvent;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.utils.UserDirUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/**
 * A handler used to log events.
 * 
 * @author sheths
 * @version $Id: ObjectHandler.java 9557 2012-06-12 21:23:30Z bellj $
 */
public final class ObjectHandler {
	
	private static final String DATASETS 		= 	"data";
	private static final String RESULTSETS		=	"results";
	private static final String ANNOTATION		=	"annotation";
	private static final String DATA_EXTENSION	=	".data";
	private static final String RES_EXTENSION	=	".res";
	private static final String	ANOT_EXTENSION	=	".annot";
	private static final String	SLASH			=	"/";

	private Log log = LogFactory.getLog(ObjectHandler.class);
	private HashMap<String, Long> lastRunTimes = new HashMap<String, Long>();
	private long defaultRunTime = 1000 * 60 * 10; // 10 min
	private GenSpaceLogin_1 login;
	private ObjectLogger objectLogger;
	private HashMap<String, String> lastTransactionId = new HashMap<String, String>();
	private int logStatus = 1; // 0 = log, 1 = log anonymously, 2 = dont
								// log
	private HashMap<String, Long> lastEventCompleteTimes = new HashMap<String, Long>();

	public ObjectHandler(ObjectLogger objectLogger) {
		this.objectLogger = objectLogger;
		objectLogger.setObjectHandler(this);
	}
	
	public void setGenSpaceLogin(GenSpaceLogin_1 login2) {
		this.login = login2;
	}
	
	public GenSpaceLogin_1 getGenSpaceLogin() {
		return this.login;
	}

	public void handleLogging(Object event) {
		if (!event.getClass().equals(AnalysisSubmissionEvent.class)) {
			return;
		}
		
		AnalysisSubmissionEvent ase = (AnalysisSubmissionEvent) event;
		
		System.out.println("DEBUG handleLogging: " + logStatus);
		
		if (logStatus != 2) {
			Method methods[] = ase.getClass().getDeclaredMethods();
			AnalysisUI analysis = null;
			String dataSetName = "";
			long dataSetID = 0;
			runningAnalyses.put(ase, null);

			for (Method m : methods) {
				//System.out.println("Test all method names: " + m.getName());
				try {
					if (m.getName().equals("getAnalaysisUI")) {
						analysis = (AnalysisUI) m.invoke(event);
						//System.out.println("Get analysis UI: " + analysis.getResultType().getName());
					}
					else if (m.getName().equals("getDataSet")) {
						dataSetName = ((DSDataSet)m.invoke(event)).getLabel();
						//dataSetName = ((DSDataSet)m.invoke(event)).getDataSetName();
					}
					else if (m.getName().equals("getDatasetId")) {
						dataSetID = (Long)m.invoke(event);
					}
				}
				catch (Exception e) {
					//e.printStackTrace();
					log.info("Could not call this method");
				}
			}
			
			//Hierarchical Clustering does not provide dataset name anymore. Use dataset id to get name. If fail, dataset id becomes dataset name.
			//Deserialize is too expensive
			if (dataSetName == null || dataSetName.isEmpty()) {
				dataSetName = FacadeFactory.getFacade().find(DataSet.class, dataSetID).getName();
				//DSMicroarraySet dataSet = (DSMicroarraySet) UserDirUtils.deserializeDataSet(dataSetID, DSMicroarraySet.class);;
			}

			incrementTransactionId(dataSetName);
			String analysisName = "";
			analysisName = ase.getResultSet().getName().replace(" - Pending", "");
			@SuppressWarnings("rawtypes")
			Map parameters;
			if (ase != null)
				parameters = ase.getParameters();
			else
				parameters = new HashMap<Serializable, Serializable>();

			if (logStatus == 0) {
				log.debug("genspace - Logging");
				
				objectLogger.log(analysisName, dataSetName,
						lastTransactionId.get(dataSetName), parameters,
						(AnalysisSubmissionEvent) event);
			}
			else if (logStatus == 1) {
				log.debug("genspace - Logging anonymously");
				
				objectLogger.log(analysisName, dataSetName,
						lastTransactionId.get(dataSetName), parameters,
						(AnalysisSubmissionEvent) event);
			}

		}

	}

	private HashMap<AnalysisSubmissionEvent, Transaction> runningAnalyses = new HashMap<AnalysisSubmissionEvent, Transaction>();

	public HashMap<AnalysisSubmissionEvent, Transaction> getRunningAnalyses() {
		return runningAnalyses;
	}

	public void eventCompleted(final AnalysisInvokedEvent invokeEvent) {
		if (invokeEvent != null)
			lastEventCompleteTimes.put(invokeEvent.getDataSetName(),
					System.currentTimeMillis());
		final Transaction tr = runningAnalyses.remove(invokeEvent);

		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() {
				try {
					objectLogger.getGenSpaceLogin().getGenSpaceServerFactory().getUsageOps().analysisEventCompleted(
							tr,
							((AbstractAnalysis) invokeEvent.getAnalysis())
									.getLabel());
				}
				catch (Exception ex) {
					// The sound of silence - this probably just means that the
					// server had never received this event, or it's a
					// continuation, etc.
				}
				return null;
			}
		};
		sw.execute();
	}

	/*
	 * Keep eventAborted and related logic first.
	 * It's possible that we will have aborted event in the future
	 */
	public void eventAborted(final AnalysisInvokedEvent invokeEvent) {
		throw new NotImplementedException();
//		if (invokeEvent != null)
//			lastEventCompleteTimes.put(invokeEvent.getDataSetName(),
//					System.currentTimeMillis());
//		final Transaction tr = runningAnalyses.remove(invokeEvent);
//		SwingWorker<Workflow, Void> sw = new SwingWorker<Workflow, Void>() {
//
//			@Override
//			protected Workflow doInBackground() throws Exception {
//				HashMap<String, Transaction> curTransactions = objectLogger
//						.getCurTransactions();
//				try {
//					curTransactions
//							.put(invokeEvent.getDataSetName(),
//									GenSpaceServerFactory
//											.getUsageOps()
//											.popAnalysisFromTransaction(
//													tr,
//													((AbstractAnalysis) invokeEvent
//															.getAnalysis())
//															.getLabel()));
//				}
//				catch (Exception ex) {
//					// The sound of silence - this probably just means that the
//					// server had never received this event, or it's a
//					// continuation, etc.
//				}
//				return (curTransactions.get(invokeEvent.getDataSetName()) == null ? null
//						: curTransactions.get(invokeEvent.getDataSetName())
//								.getWorkflow());
//			}
//
//			@Override
//			protected void done() {
//				try {
//					Workflow wf = get();
//					if (wf == null) {
//						wf = new Workflow();
//					}
//
//					// if(GenSpace.getInstance() != null &&
//					// GenSpace.getInstance().notebookPanel != null)
//					// GenSpace.getInstance().notebookPanel.updateFormFields();
//					RealTimeWorkflowSuggestion.cwfUpdated(wf);
//				}
//				catch (InterruptedException e) {
//
//				}
//				catch (ExecutionException e) {
//
//				}
//			}
//		};
//		sw.execute();
	}

	/*
	 * This function will update the lastTransactionId, lastRunTime and
	 * lastRunDataSetName if needed
	 * 
	 * @param dataSetName - the name of the data set the analysis was run on
	 */
	private void incrementTransactionId(String dataSetName) {
		long currentTime = Calendar.getInstance().getTimeInMillis();
		long lastRunTime = lastRunTimes.get(dataSetName) == null ? 0
				: lastRunTimes.get(dataSetName);
		long lastEventCompleteTime = lastEventCompleteTimes.get(dataSetName) == null ? 0
				: lastEventCompleteTimes.get(dataSetName);
		if ((currentTime - Math.max(lastRunTime, lastEventCompleteTime)) > defaultRunTime) {
			Random r = new Random();
			Integer j = Integer.valueOf(r.nextInt(Integer.MAX_VALUE));
			lastTransactionId.put(dataSetName, j.toString());
			objectLogger.getCurTransactions().remove(dataSetName); // Will generate a new transaction this way;
		}
		lastRunTimes.put(dataSetName, currentTime);
	}

	public void setLogStatus(int i) {
		logStatus = i;
	}

}
