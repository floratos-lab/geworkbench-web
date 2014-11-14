package org.geworkbenchweb.plugins.marina;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.MraResult;
import org.geworkbenchweb.utils.DataSetOperations;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class MarinaAnalysis {

	private Log log = LogFactory.getLog(MarinaAnalysis.class);
	
	private static final String SGE_CLUSTER_NAME = "hpc";
	private static final String SGE_ROOT = "/opt/gridengine/"+SGE_CLUSTER_NAME;
	
	private static final String USER_HOME = "/ifs/data/c2b2/af_lab/cagrid/";
	private static final String MARINA_RUNS_DIR = USER_HOME+"matlab/marina/runs/";

	private static final String CLASS1_FILENAME = "ix_class1.txt";
	private static final String CLASS2_FILENAME = "ix_class2.txt";
	private static final String FINAL_FILE = "mra_result.txt";
	private static final String LOG_FILE = "matlab.log";
	private static final String SUBMIT_SCRIPT_FILE ="marina_submit.sh";
	private static final String MARINA_MATLAB_FILE ="running_marina.m";
	
	private static final String delimiter = "\t";

	private static final int RESULT_COLUMN_NUMBER = 16;
	private static final long POLL_INTERVAL = 20000; //20 seconds

	/* these parameters are for submitting cluster jobs */
	private static final String JOB_MAX_MEM = "6G";
	private static final String JOB_TIMEOUT = "48::";

	private final Long dataSetId;
	private final MarinaParamBean bean;
	
	public MarinaAnalysis(Long dataSetId, HashMap<Serializable, Serializable> params){
		this.dataSetId = dataSetId;
		this.bean = (MarinaParamBean)params.get("bean");
	}
	
	private static MraResult retrieveExistingResult(String runId)
			throws RemoteException {
		String resultfile = MARINA_RUNS_DIR + runId + "/" + FINAL_FILE;
		String mradir = MARINA_RUNS_DIR + runId + "/";
		if (!new File(resultfile).exists()) {
			String err = null;
			if ((err = runError(mradir)) != null)
				throw new RemoteException("MRA run for " + runId
						+ " got error: \n" + err);
			else
				throw new RemoteException("MRA result for " + runId
						+ " doesn't exist on server.");
		}
		String[][] resultArray = convertResult(resultfile);
		return new MraResult(runId, resultArray);
	}
	
	public MraResult execute() throws Exception{
		String existingRunId = bean.getRetrievePriorResultWithId()
				.toLowerCase();
		if (existingRunId != null && existingRunId.matches("^mra\\d+$")) {
			MraResult result = retrieveExistingResult(existingRunId);
			return result;
		}
		
		/* following is the normal case (submission of a new job) instead of retrieving existing result*/
		boolean paired = false;

		String runid = createRunID();
		String mradir = MARINA_RUNS_DIR + runid + "/";
		if (!new File(mradir).mkdir()) {
			throw new Exception(
					"Failed to create individual MARINA run directory");
		}

		String networkFname = bean.getNetwork();
		if (networkFname.length() == 0)
			throw new RemoteException("Network not loaded");

		/* copy the uploaded (and possibly processed) network file */
		String source = GeworkbenchRoot.getBackendDataDirectory() + "/"
				+ "networks/"+networkFname;
		copyFile(source, mradir+networkFname);

		if (bean.getClass1() == null)
			return null;

		// one sample->split into two paired samples; all in class1->paired; two
		// classes->unpaired
		String[] class1 = bean.getClass1().split(", *");
		String[] class2 = bean.getClass2().split(", *");
		if (bean.getClass2().equals("") && class1.length == 1
				&& !class1[0].equals("")) {
			String[] ixclass1 = new String[2];
			ixclass1[0] = class1[0] + "-sr2";
			ixclass1[1] = class1[0] + "+sr2";
			writeToFile(mradir+CLASS1_FILENAME, ixclass1);
		} else
			writeToFile(mradir+CLASS1_FILENAME, class1);

		if (bean.getClass2().equals(""))
			paired = true;
		writeToFile(mradir+CLASS2_FILENAME, class2);

		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class,
				dataSetId);

		String expFname = dataset.getName();
		if (expFname.length() == 0)
			expFname = "maset.exp";
		else if (!expFname.endsWith(".exp"))
			expFname = expFname + ".exp";
		boolean unique_probeids = exportExp(expFname, mradir, dataset.getDataId());

		String matlabConfig = createMatlabConfig(paired, unique_probeids,
				expFname, networkFname);
		writeToFile(mradir+MARINA_MATLAB_FILE, matlabConfig);
		String submissionScript = createSubmissionScript(runid, mradir);
		writeToFile(mradir+SUBMIT_SCRIPT_FILE, submissionScript);

		int ret = submitJob(mradir + SUBMIT_SCRIPT_FILE);
		log.info("SubmitJob returns: " + ret);

		try {
			Thread.sleep(POLL_INTERVAL * 3); // wait for a minute before polling results
		} catch (InterruptedException e) {
		}

		File resultfile = new File(mradir + FINAL_FILE);
		while (!isJobDone(runid)) {
			try {
				Thread.sleep(POLL_INTERVAL);
			} catch (InterruptedException e) {
			}
		}
		if (!resultfile.exists()) {
			String err = null;
			if ((err = runError(mradir)) != null)
				throw new RemoteException("MRA run for " + runid
						+ " got error:\n" + err);
			else
				throw new RemoteException("MRA run for " + runid
						+ " was killed unexpectedly");
		}
		String[][] resultArray = convertResult(resultfile.getPath());

		return new MraResult(runid, resultArray);
	}

	// TODO there will be easier ways to copy files in Java 7
	static int copyFile(String source, String dest) throws IOException {
		int byteCount = 0;
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
				byteCount += bytesRead;
			}
		} finally {
			if(input!=null) input.close();
			if(output!=null) output.close();
		}
		return byteCount;
	}

	private static String[][] convertResult(String fname){
		ArrayList<String[]> data = new ArrayList<String[]>();
		BufferedReader br = null;
		try{
		    br = new BufferedReader(new FileReader(fname));
		    String line = br.readLine();//skip table header
		    while((line = br.readLine()) != null){
			    data.add(line.split(delimiter, RESULT_COLUMN_NUMBER));
		    }
		}catch(IOException e){
		    e.printStackTrace();
		}finally{
		    try{
		    	if (br!=null) br.close();
		    }catch(IOException e){
		    	e.printStackTrace();
		    }
		}
		String[][] rdata = new String[data.size()][RESULT_COLUMN_NUMBER];
		for (int i = 0; i < data.size(); i++)
		    rdata[i] = data.get(i);

		return rdata;
	}

	private static String runError(String mradir){
		StringBuilder str = new StringBuilder();
		BufferedReader br = null;
		boolean error = false;
		if (!new File(mradir+LOG_FILE).exists()) return null;
		try{
			br = new BufferedReader(new FileReader(mradir+LOG_FILE));
			String line = null; int i = 0;
			while((line = br.readLine())!=null){
				if (error){
				    if ((i = line.indexOf(0x7d)) >-1){
				    	str.append(line.substring(0,i));
				    	break;
				    }
				    else if (!line.contains(">>"))
				    	str.append(line+"\n");
				}else if ((i = line.indexOf("Error "))>-1){
					str.append(line.substring(i)+"\n");
					error = true;
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				if (br!=null) br.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if (error)  return str.toString();
		return null;
	}
	
	private static int submitJob(java.lang.String jobfile)
			throws RemoteException {
		String[] command = {SGE_ROOT+"/bin/lx-amd64/qsub", jobfile};
		System.out.println(command[1]);
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			Map<String, String> env = pb.environment();
			env.put("SGE_ROOT", SGE_ROOT);
			env.put("SGE_CLUSTER_NAME", SGE_CLUSTER_NAME);
			env.put("PATH", SGE_ROOT+"/bin/lx-amd64:$PATH");
			Process p = pb.start();
			StreamGobbler out = new StreamGobbler(p.getInputStream(), "INPUT");
			StreamGobbler err = new StreamGobbler(p.getErrorStream(), "ERROR");
			out.start();
			err.start();
			return p.waitFor();

		} catch (Exception e) {
			throw new RemoteException("MRAImpl.submitJob Exception", e);
		}
	}
	
	private static boolean isJobDone(String runid) throws RemoteException {
		String cmd = SGE_ROOT+"/bin/lx-amd64/qstat";
		BufferedReader brIn = null;
		BufferedReader brErr = null;
		try{
			ProcessBuilder pb = new ProcessBuilder(cmd);
			Map<String, String> env = pb.environment();
			env.put("SGE_ROOT", SGE_ROOT);
			env.put("SGE_CLUSTER_NAME", SGE_CLUSTER_NAME);
			env.put("PATH", SGE_ROOT+"/bin/lx-amd64:$PATH");
			Process p = pb.start();
			brIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
			brErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = brIn.readLine())!=null || (line = brErr.readLine())!=null){
				if(line.startsWith("error")) return false; //cluster scheduler error
				String[] toks = line.trim().split("\\s+");
				if (toks.length > 3 && toks[2].equals(runid))
					return false;
			}
		}catch(Exception e){
			throw new RemoteException("MRAImpl.isJobDone Exception", e);
		}finally {
			try{
				if (brIn!=null)  brIn.close();
				if (brErr!=null) brErr.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return true;
	}

	private static String createRunID() throws IOException {
		File root = new File(MARINA_RUNS_DIR);
		if (!root.exists() && !root.mkdir())
			throw new IOException(
					"Cannot access or create MARINA run top directory");

		String runId = null;
		File dir = null;

		Random random = new Random();
		int i = 0;
		do {
			runId = "mra" + random.nextInt(Short.MAX_VALUE);
			dir = new File(MARINA_RUNS_DIR + runId + "/");
			i++;
		} while (dir.exists() && i < Short.MAX_VALUE);

		if (i >= Short.MAX_VALUE) {
			throw new IOException("Tried too many times to create MARINA run ID");
		}

		return runId;
	}

	/**
	 * Create a .exp file for MARINa computation.
	 * @param expFname
	 * @param mradir
	 * @param dataId
	 * @return true if all the marker labels are unique; false if there are duplicated markers.
	 */
	private boolean exportExp(String expFname, String mradir, Long dataId) throws Exception {
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(
				MicroarrayDataset.class, dataId);
		
		String[] arrayLabels = microarray.getArrayLabels();
		String[] markerLabels = microarray.getMarkerLabels();
		float[][] values = microarray.getExpressionValues();
		Map<String, String> map = DataSetOperations.getAnnotationMap(dataSetId);

		boolean unique_probeids = true;

		String[] class1 = bean.getClass1().split(", *");
		String[] class2 = bean.getClass2().split(", *");
		
		/* Although the specification (mantis 3848) requires that "it is a paired analysis",
		 * it is in fact the only possibility by definition. So we only check the number of columns here. */
		if(arrayLabels.length==1) {
			if(class1.length!=1 || class1[0].trim().length()==0 || class2.length!=1 || class2[0].length()!=0) {
				throw new Exception("Invalid class choice for t-statistic special case");
			}
			return exportExpFileForSpecialTStatsticCase(mradir+expFname, class1[0], markerLabels, map, values);
		}
		
		PrintWriter pw = new PrintWriter(new File(mradir + expFname));
		pw.print("AffyID\tAnnotation");
		for (String arrayLabel : arrayLabels) {
			pw.print(delimiter + arrayLabel);
		}
		pw.println();

		Set<String> markerSet = new HashSet<String>();
		for (int i = 0; i < markerLabels.length; i++) {
			String markerName = markerLabels[i];
			if (unique_probeids) {
				if (!markerSet.contains(markerName)) {
					markerSet.add(markerName);
				} else {
					unique_probeids = false;
				}
			}
			String geneName = map.get(markerName);
			if (geneName == null)
				geneName = markerName;
			pw.print(markerName + delimiter + geneName);
			for (int index = 0; index < arrayLabels.length; index++) {
				float data = values[i][index];
				if (data == (int) data)
					pw.print(delimiter + String.valueOf((int) data));
				else
					pw.print(delimiter + String.valueOf(data));
			}
			pw.println();
		}
		if (pw != null)
			pw.close();

		return unique_probeids;
	}

	/**
	 * Create the .exp file for the special case of 't-statistic'.
	 * @return true if the marker labels are all unique
	 */
	private static boolean exportExpFileForSpecialTStatsticCase(String filename,
			String arrayLabel, String[] markerLabels, Map<String, String> map,
			float[][] values) throws IOException {
		boolean unique_probeids = true;
		PrintWriter pw = new PrintWriter(new File(filename));
		pw.println("AffyID\tAnnotation\t" + arrayLabel + "-r-sr2\t" + arrayLabel
				+ "+r-sr2");

		final float ONE_OVER_SQRT2 = 1.f/(float)Math.sqrt(2.);
		Set<String> markerSet = new HashSet<String>();
		for (int i = 0; i < markerLabels.length; i++) {
			String markerName = markerLabels[i];
			if (unique_probeids) {
				if (!markerSet.contains(markerName)) {
					markerSet.add(markerName);
				} else {
					unique_probeids = false;
				}
			}
			String geneName = map.get(markerName);
			if (geneName == null)
				geneName = markerName;

			float data = values[i][0]; /* there must be only one column. */

			/* the order of the two columns is not important, but this order is preferred. */
			pw.println(markerName + "\t" + geneName + "\t"
					+ (data - ONE_OVER_SQRT2) + "\t" + (data + ONE_OVER_SQRT2));
		}

		if (pw != null)
			pw.close();

		return unique_probeids;
	}

	private static void writeToFile(String filename, String[] strings) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File(filename));
			for (String line : strings) {
				pw.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null)
				pw.close();
		}
	}

	private static void writeToFile(String filename, String content) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File(filename));
			pw.print(content);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null)
				pw.close();
		}
	}

	private static String createSubmissionScript(String runid, String mradir) {
		StringBuilder script = new StringBuilder();
		script.append("#!/bin/bash\n#$ -l mem="+JOB_MAX_MEM+",time="+JOB_TIMEOUT+" -cwd -j y -o ").append(mradir).append(LOG_FILE).append(" -N ").append(runid)
	    .append("\nexport MATLAB_PREFDIR=").append(USER_HOME).append(".matlab/R2012a")
	    .append("\n\nexport HOME=").append(USER_HOME).append("\nexport level=\"$SGE_TASK_ID\"\nexport MATLABROOT=/nfs/apps/matlab/2012a\ncd ").append(mradir)
	    .append("\nmkdir mcr\nexport LD_LIBRARY_PATH=$MATLABROOT/sys/os/glnxa64/:$MATLABROOT/bin/glnxa64/:$MATLABROOT/sys/java/jre/glnxa64/jre/lib/amd64/native_threads/:")
	    .append("$MATLABROOT/sys/java/jre/glnxa64/jre/lib/amd64/server/:$MATLABROOT/sys/java/jre/glnxa64/jre/lib/amd64/:$MATLABROOT/runtime/glnxa64/:$LD_LIBRARY_PATH")
	    .append("\nexport MCR_CACHE_ROOT=$PWD/mcr/$level\n\n/nfs/apps/matlab/2012a/bin/matlab -nodisplay -nodesktop -nosplash < ").append(MARINA_MATLAB_FILE)
	    .append("\nrm -rf grid_submit.sh gsea *.mat network_shadow_mras.txt original_mra*.txt shadow_recovery_gsea2.txt synerg*_pair*txt shadow_runs tmp mcr\n");
	    return script.toString();
	}

	private String createMatlabConfig(boolean paired, boolean unique_probeids, String expFname, String networkFname){
	    StringBuilder matlabConfig = new StringBuilder("clc\nclear\n");
	    matlabConfig.append("src_dir = '").append(USER_HOME).append("matlab/marina/scripts/';\n")
		.append("addpath(src_dir)\nsrc_dir = addTrailingSlash(src_dir);\n")
		.append("final_file = '").append(FINAL_FILE).append("';\n")
		.append("filename_exp = ['").append(expFname).append("'];\n")
		.append("filename_network_5col = ['").append(networkFname).append("'];\n")
		.append("paired = ").append(paired?1:0).append(";\n")
		.append("min_targets = ").append(bean.getMinimumTargetNumber()).append("; % minimum number of targets to run GSEA\n")
		.append("min_samples = ").append(bean.getMinimumSampleNumber()).append("; % minimum number of samples for label suffling\n")
		.append("nperm = ").append(bean.getGseaPermutationNumber()).append(";\n")
		.append("pvalue_gsea = ").append(bean.getGseaPValue()).append(";\n")
		.append("verbose = 1;\n")
		.append("tail = ").append(bean.getGseaTailNumber()).append("; % 2 for GSEA2, 1 for GSEA\n")
		.append("unique_probeids = ").append(unique_probeids?1:0).append(";  % If probe ids are unique else unique_probeids=0\n")
		.append("pv_shadow_threshold = ").append(bean.getShadowPValue()).append(";\n")
		.append("pv_synergy_threshold= ").append(bean.getSynergyPValue()).append(";\n")
		.append("template = [src_dir 'submit-template_2012a.txt'];\n")
		.append("%%\ndisp('Importing dataset')\n")
		.append("[data textdata] = importfile(filename_exp);\n")
		.append("samples_bcell = textdata(1,3:end);\n")
		.append("resistant_samples = textread('").append(CLASS1_FILENAME).append("','%s');\n")
		.append("sensitive_samples = textread('").append(CLASS2_FILENAME).append("','%s');\n")
		.append("[a,b,c] = intersect(samples_bcell,resistant_samples);\n")
		.append("ix_class1 = b;\n")
		.append("[a,b,c] = intersect(samples_bcell,sensitive_samples);\n")
		.append("ix_class2 = b;\n")
		.append("marina_2012a(src_dir,template,final_file,filename_exp,filename_network_5col,paired,min_targets,min_samples,nperm,pvalue_gsea,unique_probeids,pv_shadow_threshold,pv_synergy_threshold,ix_class1,ix_class2,tail,verbose)\n");
	    return matlabConfig.toString();
	}

}
