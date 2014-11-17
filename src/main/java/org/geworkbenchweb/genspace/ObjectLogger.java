package org.geworkbenchweb.genspace;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;
import org.geworkbench.components.genspace.server.stubs.AnalysisEventParameter;
import org.geworkbench.components.genspace.server.stubs.Transaction;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.server.stubs.WorkflowTool;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.ui.Window.Notification;


/**
 * The event logger
 * 
 * @author sheths
 */
public class ObjectLogger {
	private static Log log = LogFactory.getLog(ObjectLogger.class);

	private Set<CWFListener> cwfListeners = new LinkedHashSet<CWFListener>();

	private Set<NotebookDataListener> ndListeners = new LinkedHashSet<NotebookDataListener>();

	private HashMap<String, Transaction> curTransactions = new HashMap<String, Transaction>();
	
	protected ObjectHandler objectHandler;
	
	private GenSpaceLogin_1 login;
	private ICEPush pusher = new ICEPush();

	public void addCWFListener(CWFListener listener) {
		this.cwfListeners.add(listener);
	}

	public void addNotebookDataListener(NotebookDataListener ndListener) {
		this.ndListeners.add(ndListener);
	}

	public void setObjectHandler(ObjectHandler objectHandler) {
		this.objectHandler = objectHandler;
	}

	public HashMap<String, Transaction> getCurTransactions() {
		return this.curTransactions;
	}
	
	
	private Workflow curWorkflow = null;
	


	private Transaction prepareTransaction(String analysisName, String dataSetName, String transactionId, Map parameters, AnalysisSubmissionEvent event) {		
		String hostname = "";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			return null;
		}
		
		/*For fixing when user choose not to login genSpace, still need a GenSpaceServerFactory to log information.*/
		GenSpaceServerFactory tmpFactory = null;
		if (!login.isLogin()) {
			String username = this.login.getUsername(); 
			String password = this.login.getPassword();
			this.login.getGenSpaceServerFactory().userLogin(username, password);
		} 
		
		if (!login.isLogin()) {
			log.error("Register Error!");
			tmpFactory = new GenSpaceServerFactory();
		}
		else {
			tmpFactory = login.getGenSpaceServerFactory();
			int logStatus = login.getGenSpaceServerFactory().getUser().getLogData();
			this.login.getGenSpaceLogger().getObjectHandler().setLogStatus(logStatus);
		}
			
		Transaction curTransaction = curTransactions.get(dataSetName);
		if(this.login.getGenSpaceLogger().getObjectHandler().getLogStatus() == 1 ||
				curTransaction == null || !curTransaction.getClientID().equals(tmpFactory.getUsername() + hostname + transactionId))
		{
			curTransaction = new Transaction();
			curTransaction.setDataSetName(dataSetName);
			try {
				curTransaction.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
			} catch (DatatypeConfigurationException e1) {
				e1.printStackTrace();
			} //TODO verify
			if (this.login.getGenSpaceLogger().getObjectHandler().getLogStatus() == 1 || !login.isLogin() ) {
				curTransaction.setClientID(hostname + transactionId);
				curTransaction.setUser(null);
			} else {
				curTransaction.setClientID(login.getGenSpaceServerFactory().getUsername() + hostname + transactionId);
				curTransaction.setHostname(hostname);
				curTransaction.setUser(login.getGenSpaceServerFactory().getUser());
			}
				
		}
		File f = new File(FilePathnameUtils.getUserSettingDirectoryPath()
				+ "geworkbench_log.xml");
		if(f.exists())
		{
			//Try to send up the log file
			XMLLoader loader =new XMLLoader();
			ArrayList<AnalysisEvent> pending = loader.readAndLoad(FilePathnameUtils.getUserSettingDirectoryPath()
					+ "geworkbench_log.xml");
			Transaction done = null;
			try
			{
				done = (Transaction) (tmpFactory.getUsageOps().sendUsageLog(pending));
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				//be silent
			}
			if(done != null)
			{
				for(WorkflowTool t : done.getWorkflow().getTools())
				{
					t.setTool(RuntimeEnvironmentSettings.tools.get(t.getTool().getId()));
				}
				f.delete();
				Calendar recentTime = Calendar.getInstance();
				recentTime.add(Calendar.MINUTE, -20);
				if(pending.get(pending.size()).getCreatedAt().toGregorianCalendar().after(recentTime))
				{
					curTransactions.put(dataSetName, done);
				}
			}
		}
		AnalysisEvent e = new AnalysisEvent();
		e.setToolname(analysisName);

		try {
			e.setCreatedAt(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
		} catch (DatatypeConfigurationException e2) {
			e2.printStackTrace();
			return null;
		} 
		e.setTransaction(curTransaction);
		HashSet<AnalysisEventParameter> params = new HashSet<AnalysisEventParameter>();
		if(parameters != null)
			for(Object key : parameters.keySet())
			{
				if(!key.equals("referenceListSets"))
				{
					AnalysisEventParameter p = new AnalysisEventParameter();
					p.setParameterKey(key.toString());
					
					if (parameters.get(key) == null) {
						p.setParameterValue("");
					} else {
						p.setParameterValue(parameters.get(key).toString());
					}
					
					params.add(p);
				}
			}
		e.getParameters().addAll(params);
		try
		{	
			Transaction retTrans = (tmpFactory.getUsageOps().sendUsageEvent((e))); //try to send the log event
			tmpFactory.clearCache();
			
			//System.out.println("logstatus : " + this.login.getGenSpaceLogger().getObjectHandler().getLogStatus());
			if (this.login.getGenSpaceLogger().getObjectHandler().getLogStatus() == 1) {
				return null;
			}
			
			if(retTrans != null)
			{
				for(WorkflowTool t : retTrans.getWorkflow().getTools())
				{
					int id = t.getTool().getId();
					t.getTool().getRatings().clear();
					t.getTool().setRef(null);
					t.getTool().getComments().clear();
					t.setTool(null);
					t.setTool(RuntimeEnvironmentSettings.tools.get(id));
				}
				objectHandler.getRunningAnalyses().put(event,retTrans);
				for (CWFListener listener : cwfListeners) {
					listener.cwfUpdated(retTrans.getWorkflow());
				}
				setCurWorkflow(retTrans.getWorkflow());
				return retTrans;
			}
			
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
			//be silent on errors... if we get them, we'll just log to the file instead
		}
		
		try {
			
			if(!f.exists())
				f.createNewFile();
			
			FileWriter fw = new FileWriter(f, true);

			// log only the file extension and not the filename
			String[] fileName = dataSetName.split("\\.");
			String fileExtension = fileName[fileName.length - 1];

			fw.write("\t<metric name=\"analysis\">");
			fw.write("\n\t\t<user name=\"" + login.getGenSpaceServerFactory().getUsername() + "\" genspace=\""
					+1+ "\"/>");
									
			fw.write("\n\t\t<host name=\""
					+ InetAddress.getLocalHost().getHostName() + "\"/>");
			fw.write("\n\t\t<analysis name=\"" + analysisName + "\"/>");
			fw.write("\n\t\t<dataset name=\"" + fileExtension + "\"/>");
			fw.write("\n\t\t<transaction id=\"" + login.getGenSpaceServerFactory().getUsername() + hostname +  transactionId + "\"/>");
			fw.write("\n\t\t<time>");

			Calendar c = Calendar.getInstance();

			fw.write("\n\t\t\t<year>" + c.get(Calendar.YEAR) + "</year>");
			fw.write("\n\t\t\t<month>" + (c.get(Calendar.MONTH) )
					+ "</month>");
			fw.write("\n\t\t\t<day>" + c.get(Calendar.DATE) + "</day>");
			fw.write("\n\t\t\t<hour>" + c.get(Calendar.HOUR_OF_DAY) + "</hour>");
			fw.write("\n\t\t\t<minute>" + c.get(Calendar.MINUTE) + "</minute>");
			fw.write("\n\t\t\t<second>" + c.get(Calendar.SECOND) + "</second>");
			fw.write("\n\t\t</time>");

			if(parameters != null)
			{
				// log the parameters
				@SuppressWarnings("rawtypes")
				Set keys = parameters.keySet();

				fw.write("\n\t\t<parameters count=\"" + keys.size() + "\">");
				int count = 0;

				for (Object key : keys) {
					Object value = parameters.get(key);
					fw.write("\n\t\t\t<parameter id=\"" + count + "\">");
					fw.write("\n\t\t\t\t<key>" + key.toString() + "</key>");
					fw.write("\n\t\t\t\t<value>" + value.toString() + "</value>");
					fw.write("\n\t\t\t</parameter>");

					count++;
				}

				fw.write("\n\t\t</parameters>");
			}
			else
			{
				fw.write("\n\t\t<parameters count=\"0\">");

				fw.write("\n\t\t</parameters>");
			}
			fw.write("\n\t</metric>\n");

			fw.close();
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		
		return null;
	}
	
	private boolean completeLoggin(String analysisName, String dataSetName, String transactionId, Map parameters, AnalysisSubmissionEvent event) {
		Transaction ret = null;
		try {
			ret = this.prepareTransaction(analysisName, dataSetName, transactionId, parameters, event);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		if(ret != null)
		{
			curTransactions.put(dataSetName, ret);
			return true;
		}
		
		return false;
	}
	
	private void handleExceptions(Exception e) {
		Notification msg = new Notification("Some error occurs: " + e.getMessage() + "Please try again.",
				Notification.TYPE_ERROR_MESSAGE);
		login.getApplication().getMainWindow().showNotification(msg);
	}
	
	public void log(String analysisName, String dataSetName, String transactionId, @SuppressWarnings("rawtypes") final Map parameters, AnalysisSubmissionEvent event) {
		
		if (this.completeLoggin(analysisName, dataSetName, transactionId, parameters, event) 
														&& this.login != null
														&& this.login.getParent() != null ) {
			ICEPush pusher = this.login.getGenSpaceParent().getPusher();
			this.login.getUIMainWindow().addComponent(pusher);
			pusher.push();
		}
	}
	

	void deleteFile() {

		// find a better way to do this!
		try {
			File f = new File(FilePathnameUtils.getUserSettingDirectoryPath()
					+ "geworkbench_usage_log.xml");
			FileWriter fw = new FileWriter(f, false);
			fw.close();
		} catch (Exception e) {
		}
	}
	
	public void setGenSpaceLogin(GenSpaceLogin_1 login2) {
		this.login = login2;
	}
	
	public GenSpaceLogin_1 getGenSpaceLogin() {
		return this.login;
	}
	
	public Workflow getCurWorkflow() {
		return curWorkflow;
	}

	public void setCurWorkflow(Workflow curWorkflow) {
		this.curWorkflow = curWorkflow;
	}
}
