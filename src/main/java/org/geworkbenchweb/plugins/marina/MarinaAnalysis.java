package org.geworkbenchweb.plugins.marina;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.MraResult;
import org.geworkbenchweb.utils.DataSetOperations;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class MarinaAnalysis {

	private Log log = LogFactory.getLog(MarinaAnalysis.class);
	
	private static final String SGE_CLUSTER_NAME = "hpc";
	private static final String SGE_ROOT = "/opt/gridengine/"+SGE_CLUSTER_NAME;
	
	private final Long dataSetId;
	private MarinaParamBean bean = null;
	private static final String delimiter = "\t";
	private static final String MRAROOT = "/ifs/data/c2b2/af_lab/cagrid/matlab/marina/runs/";
	private static final String MRASRC = "/ifs/data/c2b2/af_lab/cagrid/matlab/marina/scripts/";
	private static final String MRAHOME = "/ifs/data/c2b2/af_lab/cagrid/";
	private static final String class1Fname = "ix_class1.txt";
	private static final String class2Fname = "ix_class2.txt";
	private static final String finalfile = "mra_result.txt";
	private static final String logfile = "matlab.log";
	private static final String submitScriptName="marina_submit.sh";
	private static final String marinaScriptName="running_marina.m";
	private static final Random random = new Random();
	private static final int rstcolnum = 16;
	private static final long POLL_INTERVAL = 20000; //20 seconds
	private static final String maxmem = "6G";
	private static final String timeout = "48::";

	public MarinaAnalysis(Long dataSetId, HashMap<Serializable, Serializable> params){
		this.dataSetId = dataSetId;
		this.bean = (MarinaParamBean)params.get("bean");
	}
	
	private static MraResult retrieveExistingResult(String runId)
			throws RemoteException {
		String resultfile = MRAROOT + runId + "/" + finalfile;
		String mradir = MRAROOT + runId + "/";
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
		String network = bean.getNetworkString();
		if (network == null)
			throw new RemoteException(
					"Please reload a network file in 5-column format, or use a valid AdjacencyMatrix that matches this dataset.");

		boolean paired = false;
		boolean unique_probeids = true;

		String runid = createRunID();
		String mradir = MRAROOT + runid + "/";
		if (!new File(mradir).mkdir()) {
			throw new Exception(
					"Failed to create individual MARINA run directory");
		}

		String networkFname = bean.getNetwork();
		if (networkFname.length() == 0)
			throw new RemoteException("Network not loaded");
		createNetworkFile(networkFname, network, mradir);

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
			writeToFile(class1Fname, ixclass1, mradir);
		} else
			writeToFile(class1Fname, class1, mradir);

		if (bean.getClass2().equals(""))
			paired = true;
		writeToFile(class2Fname, class2, mradir);

		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class,
				dataSetId);
		Long dataId = dataset.getDataId();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(
				MicroarrayDataset.class, dataId);

		String expFname = dataset.getName();
		if (expFname.length() == 0)
			expFname = "maset.exp";
		else if (!expFname.endsWith(".exp"))
			expFname = expFname + ".exp";
		unique_probeids = exportExp(expFname, mradir, microarray);

		StringBuilder matlabjob_template = new StringBuilder();
		String marina_script = prepareMarina(matlabjob_template, paired,
				unique_probeids, expFname, networkFname, runid, mradir);
		writeToFile(marinaScriptName, marina_script, mradir);
		writeToFile(submitScriptName, matlabjob_template.toString(), mradir);

		int ret = submitJob(mradir + submitScriptName);
		log.info("SubmitJob returns: " + ret);

		try {
			Thread.sleep(POLL_INTERVAL * 3); // wait for a minute before polling results
		} catch (InterruptedException e) {
		}

		File resultfile = new File(mradir + finalfile);
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

	private static String[][] convertResult(String fname){
		ArrayList<String[]> data = new ArrayList<String[]>();
		BufferedReader br = null;
		try{
		    br = new BufferedReader(new FileReader(fname));
		    String line = br.readLine();//skip table header
		    while((line = br.readLine()) != null){
			    data.add(line.split(delimiter, rstcolnum));
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
		String[][] rdata = new String[data.size()][rstcolnum];
		for (int i = 0; i < data.size(); i++)
		    rdata[i] = data.get(i);

		return rdata;
	}

	private static String runError(String mradir){
		StringBuilder str = new StringBuilder();
		BufferedReader br = null;
		boolean error = false;
		if (!new File(mradir+logfile).exists()) return null;
		try{
			br = new BufferedReader(new FileReader(mradir+logfile));
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
	
	private int submitJob(java.lang.String jobfile)
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
	
	private boolean isJobDone(String runid) throws RemoteException {
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

	private static String createRunID() throws Exception {
		File root = new File(MRAROOT);
		if (!root.exists() && !root.mkdir())
			throw new Exception(
					"Cannot access or create MARINA run top directory");

		String runId = null;
		File dir = null;

		int i = 0;
		do {
			runId = "mra" + random.nextInt(Short.MAX_VALUE);
			dir = new File(MRAROOT + runId + "/");
			i++;
		} while (dir.exists() && i < Short.MAX_VALUE);

		if (i >= Short.MAX_VALUE) {
			throw new Exception("Tried too many times to create MARINA run ID");
		}

		return runId;
	}

	private boolean exportExp(String expFname, String mradir, MicroarrayDataset microarray){
		String[] arrayLabels = microarray.getArrayLabels();
		String[] markerLabels = microarray.getMarkerLabels();
		float[][] values = microarray.getExpressionValues();
		Map<String, String> map = DataSetOperations.getAnnotationMap(dataSetId);

		boolean unique_probeids = true;

		String[] class1 = bean.getClass1().split(", *");
		String[] class2 = bean.getClass2().split(", *");
		
		BufferedWriter bw = null;
		try{
		    bw = new BufferedWriter(new FileWriter(mradir+expFname));
		    bw.write("AffyID\tAnnotation");
		    for (String arrayLabel : arrayLabels){
				bw.write(delimiter+arrayLabel);
				
			    if ((class2.length == 1 && class2[0].length()==0)
					&& class1.length == 1
					&& class1[0].equals(arrayLabel)) {
			    	bw.write("-sr2\t"+arrayLabel+"+sr2");
			    }
		    }
		    bw.write("\n");
		    bw.flush();

		    int i = 0;
		    HashMap<String, Boolean> hm = new HashMap<String, Boolean>();
		    for (String markerName : markerLabels){
				if (unique_probeids){
				    if (hm.get(markerName) == null)
				    	hm.put(markerName, true);
				    else unique_probeids = false;
				}
				String geneName = map.get( markerName );
				if(geneName==null)geneName = markerName;
				bw.write(markerName+delimiter+geneName);
				for (int index=0; index<arrayLabels.length; index++){
				    bw.write(delimiter);
				    float data = values[i][index];
				    if ((class2.length == 1 && class2[0].length()==0)
						&& class1.length == 1
						&& class1[0].equals(arrayLabels.length)) {
				    	float data1 = data - (float)Math.sqrt(2);
				    	float data2 = data + (float)Math.sqrt(2);
				    	bw.write(data1+delimiter+data2);
				    }else{
				    	if (data==(int)data)
				    		bw.write(String.valueOf((int)data));
				    	else bw.write(String.valueOf(data));
				    }
				}
				bw.write("\n");
				i++;
		    }
		}catch(IOException e){
		    e.printStackTrace();
		}finally{
		    try{
		    	if (bw!=null) bw.close();
		    }catch(IOException e){
		    	e.printStackTrace();
		    }
		}
		return unique_probeids;
	}

	private void writeToFile(String fname, String string, String mradir){
	    String[] strings = new String[1];
	    strings[0] = string;
	    writeToFile(fname, strings, mradir);
	}

	private void writeToFile(String fname, String[] strings, String mradir){
	    BufferedWriter bw = null;
	    try{
			bw = new BufferedWriter(new FileWriter(mradir+fname));
			for (String string : strings){
			    bw.write(string);
			    if (strings.length>1)
			    	bw.write("\n");
			    bw.flush();
			}
	    }catch(IOException e){
	    	e.printStackTrace();
	    }finally{
			try{
			    if (bw!=null) bw.close();
			}catch(IOException e){
			    e.printStackTrace();
			}
	    }
	}

	private void createNetworkFile(String fname, String networkString, String mradir){
	    PrintWriter pw = null;
	    try{
			pw = new PrintWriter(new File(mradir+fname));
			pw.print(networkString);
	    }catch(IOException e){
	    	e.printStackTrace();
	    }finally{
			if (pw != null)	  pw.close();
	    }
	}

	private String prepareMarina(StringBuilder matlabjob_template, boolean paired, boolean unique_probeids, String expFname, String networkFname, String runid, String mradir){
	    matlabjob_template.append("#!/bin/bash\n#$ -l mem="+maxmem+",time="+timeout+" -cwd -j y -o ").append(mradir).append(logfile).append(" -N ").append(runid)
	    .append("\nexport MATLAB_PREFDIR=").append(MRAHOME).append(".matlab/R2012a")
	    .append("\n\nexport HOME=").append(MRAHOME).append("\nexport level=\"$SGE_TASK_ID\"\nexport MATLABROOT=/nfs/apps/matlab/2012a\ncd ").append(mradir)
	    .append("\nmkdir mcr\nexport LD_LIBRARY_PATH=$MATLABROOT/sys/os/glnxa64/:$MATLABROOT/bin/glnxa64/:$MATLABROOT/sys/java/jre/glnxa64/jre/lib/amd64/native_threads/:")
	    .append("$MATLABROOT/sys/java/jre/glnxa64/jre/lib/amd64/server/:$MATLABROOT/sys/java/jre/glnxa64/jre/lib/amd64/:$MATLABROOT/runtime/glnxa64/:$LD_LIBRARY_PATH")
	    .append("\nexport MCR_CACHE_ROOT=$PWD/mcr/$level\n\n/nfs/apps/matlab/2012a/bin/matlab -nodisplay -nodesktop -nosplash < ").append(marinaScriptName)
	    .append("\nrm -rf grid_submit.sh gsea *.mat network_shadow_mras.txt original_mra*.txt shadow_recovery_gsea2.txt synerg*_pair*txt shadow_runs tmp mcr\n");

	    StringBuilder marina_config = new StringBuilder("clc\nclear\n");
	    marina_config.append("src_dir = '").append(MRASRC).append("';\n")
		.append("addpath(src_dir)\nsrc_dir = addTrailingSlash(src_dir);\n")
		.append("final_file = '").append(finalfile).append("';\n")
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
		.append("resistant_samples = textread('").append(class1Fname).append("','%s');\n")
		.append("sensitive_samples = textread('").append(class2Fname).append("','%s');\n")
		.append("[a,b,c] = intersect(samples_bcell,resistant_samples);\n")
		.append("ix_class1 = b;\n")
		.append("[a,b,c] = intersect(samples_bcell,sensitive_samples);\n")
		.append("ix_class2 = b;\n")
		.append("marina_2012a(src_dir,template,final_file,filename_exp,filename_network_5col,paired,min_targets,min_samples,nperm,pvalue_gsea,unique_probeids,pv_shadow_threshold,pv_synergy_threshold,ix_class1,ix_class2,tail,verbose)\n");
	    return marina_config.toString();
	}

}
