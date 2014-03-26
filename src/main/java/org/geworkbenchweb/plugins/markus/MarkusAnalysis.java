package org.geworkbenchweb.plugins.markus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.structure.MarkUsResultDataSet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.MarkUsResult;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class MarkusAnalysis {

	private static Log log = LogFactory.getLog(MarkusAnalysis.class);
	private Long sessionId = SessionHandler.get().getId();
	final private String pdbFilename;
	private MarkUsUI mcp = null;
	private Long dataSetId;
	private static final String MARKUS_RESULT_URL = "https://bhapp.c2b2.columbia.edu/MarkUs/cgi-bin/submit.pl";
	private static final String browseUrl = "https://bhapp.c2b2.columbia.edu/MarkUs/cgi-bin/browse.pl?pdb_id=";
	private String req = "--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"submit\"\r\n\r\nUpload\r\n--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"infile\"; filename=\"PDB\"\r\nContent-Type: text/plain\r\n\r\n";

	public MarkusAnalysis(String pdbFilename, MarkUsUI form, Long dataSetId) {
		this.mcp	 = form;
		this.dataSetId = dataSetId;
		this.pdbFilename = pdbFilename;
	}
	
	void execute(){

		final String DATASETS = "data";
		final String SLASH = "/";

		String fullPath = GeworkbenchRoot.getBackendDataDirectory() + SLASH
				+ SessionHandler.get().getUsername() + SLASH + DATASETS + SLASH
				+ pdbFilename;
		File pdbfile = new File(fullPath);

		String tmpfile = null;
		try {
			tmpfile = uploadFile(pdbfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("uploaded file: "+tmpfile);

		String str = generateMarkusInput(pdbFilename, tmpfile);
		String results = MarkusAnalysis.submitJob(str);			

		if(results==null) return;
			
		String impossibleResult = results.toLowerCase();
		if (impossibleResult.contains("error")
				|| results.equals("cancelled") || results.equals("na")) return;

		// start waiting for this job's results
		String url = browseUrl + results;
		UrlStatus urlstat = checkUrlStatus(url);
		log.info("URL status: " + urlstat + " " + url);
		while(urlstat != UrlStatus.FINISHED) {
			try {
				Thread.sleep(30000L);
			} catch (InterruptedException e) {
				// if interrupted while sleeping, do nothing
			}
			urlstat = checkUrlStatus(url);
		}

		getResultSet(results);
	}

	void getResultSet(String results){
		MarkUsResult musresult = new MarkUsResult(results);
		FacadeFactory.getFacade().store(musresult);
		Long dataId = musresult.getId();
		
		ResultSet resultSet = 	new ResultSet();
		resultSet.setDataId(dataId);
		java.sql.Timestamp timestamp =	new java.sql.Timestamp(System.currentTimeMillis());
		resultSet.setTimestamp(timestamp);
		String dataSetName 	=	results;
		resultSet.setName(dataSetName);
		resultSet.setType(MarkUsResultDataSet.class.getName());
		resultSet.setParent(dataSetId);
		resultSet.setOwner(sessionId);	
		FacadeFactory.getFacade().store(resultSet);
		
		GeworkbenchRoot app = (GeworkbenchRoot) mcp.getApplication();
		app.addNode(resultSet);
	}
	
    public static java.lang.String submitJob(java.lang.String string) {
    	HttpURLConnection conn = null;
    	BufferedReader in = null;
        String process_id = "na";
		try {
			URL url = new URL(MARKUS_RESULT_URL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(true);
			conn.setAllowUserInteraction(true);
			conn.setRequestProperty("Content-Type",
					"multipart/form-data, boundary=AaB03x");
			conn.setRequestProperty("Content-Transfer-Encoding", "binary");
			conn.connect();

			OutputStream out = conn.getOutputStream();
			out.write(string.getBytes());
			out.flush();
			out.close();

			InputStream dat = conn.getInputStream();
			String contenttype = conn.getContentType();

			if (contenttype.toLowerCase().startsWith("text")) {
				in = new BufferedReader(new InputStreamReader(
						dat));

				String line = null; int i=-1; int j = -1;
				while ((line = in.readLine()) != null)
				{
					if ((i = line.indexOf("pdb_id=")) > -1 && (j = line.indexOf("\">")) > -1)
				    {
						process_id = line.substring(i+7, j);
				    }
				}
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.disconnect();
			try {
				in.close(); 
			} catch (Exception a) {
				a.printStackTrace();
			}
			conn = null;
		}
        log.info("SubmitJob "+process_id);
        return process_id;
    }

	private java.lang.String uploadFile(File pdbfile) {
		HttpURLConnection conn = null;
		BufferedReader in = null;
		String tmpfile = null;
		try {
			URL url = new URL(MARKUS_RESULT_URL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(true);
			conn.setAllowUserInteraction(true);
			conn.setRequestProperty("Content-Type",
					"multipart/form-data, boundary=AaB03x");
			conn.setRequestProperty("Content-Transfer-Encoding", "binary");
			conn.connect();

			req = req.replaceFirst("PDB", pdbfile.getName());
			OutputStream out = conn.getOutputStream();
			out.write(req.getBytes());
			FileInputStream fis = new FileInputStream(pdbfile);
			byte[] buffer = new byte[4096];
			int bytes_read;
			while ((bytes_read = fis.read(buffer)) != -1) {
				out.write(buffer, 0, bytes_read);
			}
			out.write("\r\n--AaB03x--\r\n".getBytes());
			out.flush();
			out.close();
			fis.close();

			InputStream dat = conn.getInputStream();
			String contenttype = conn.getContentType();

			if (contenttype.toLowerCase().startsWith("text")) {
				in = new BufferedReader(new InputStreamReader(
						dat));
				String line = null; int i=-1, j=-1;
				while ((line = in.readLine()) != null)
				{
				    if ((i = line.indexOf("name=\"tmpfile\" value=\"")) > -1)
				    {
					j = line.indexOf(".pdb");
					tmpfile = line.substring(i+22, j+4);
				    }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.disconnect();
			try {
				in.close(); 
			} catch (Exception a) {
				a.printStackTrace();
			}
			conn = null;
		}
		return tmpfile;
	}
	
    private static enum UrlStatus {FINISHED, PENDING, NO_RECORD}
    /** Check the status of given URL */
    static private UrlStatus checkUrlStatus(String url)
    {
    	BufferedReader br = null;
    	try{
    	    URLConnection uc = new URL(url).openConnection();
    	    if (((HttpURLConnection)uc).getResponseCode() == 404)
    	    	return UrlStatus.NO_RECORD;
    	    
    	    br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    	    String tmp = null;
    	    while((tmp = br.readLine()) != null) {
    	    	if (tmp.indexOf("functional annotation is pending") > -1) {
    	    		br.close();
    	    		return UrlStatus.PENDING;
    	    	}
    	    }
    	}catch (IOException e){
    	    e.printStackTrace();
    	    try {
    			br.close();
    		} catch (IOException e1) { // no action intentionally
    			e1.printStackTrace();
    		}
    	    return UrlStatus.NO_RECORD;
    	}
    	return UrlStatus.FINISHED;
    }

	private String generateMarkusInput(String pdbfilename, String tmpfile)
	{
		StringBuilder cfgcommand = new StringBuilder();

		if (mcp.getdaliValue())
			cfgcommand.append(" Dali=1");
		if (mcp.getdelphiValue())
			cfgcommand.append(" Delphi=1");
		if (mcp.getconsurf3Value()) {
			cfgcommand.append(" C3=BLAST C3T=").append(mcp.getcsftitle3Value())
					.append(" C3E=").append(mcp.geteval3Value())
					.append(" C3I=").append(mcp.getiter3Value())
					.append(" C3P=").append(mcp.getfilter3Value())
					.append(" C3MSA=").append(mcp.getmsa3Value());
		}
		if (mcp.getconsurf4Value()) {
			cfgcommand.append(" C4=BLAST C4T=").append(mcp.getcsftitle4Value())
					.append(" C4E=").append(mcp.geteval4Value())
					.append(" C4I=").append(mcp.getiter4Value())
					.append(" C4P=").append(mcp.getfilter4Value())
					.append(" C4MSA=").append(mcp.getmsa4Value());
		}
		if (mcp.getkeyValue())
			cfgcommand.append(" privateKey=1");
		
		String chain = mcp.getChain();

		cfgcommand
				.append(
						" Skan=1 SkanBox=1 Screen=1 ScreenBox=1 VASP=1 LBias=1 PredUs=1 BlastBox=1 IPSBox=1 CBox=1")
				.append(" D1B=")
				.append(mcp.getibcValue())
				.append(" D1EX=")
				.append(mcp.getedcValue())
				.append(" D1F=")
				.append(mcp.getstepsValue())
				.append(" D1G=")
				.append(mcp.getgridsizeValue())
				.append(" D1IN=")
				.append(mcp.getidcValue())
				.append(" D1L=")
				.append(mcp.getliValue())
				.append(" D1N=")
				.append(mcp.getnliValue())
				.append(" D1P=")
				.append(mcp.getboxfillValue())
				.append(" D1PR=")
				.append(mcp.getradiusValue())
				.append(" D1SC=")
				.append(mcp.getscValue())
				.append(
						" C1=PFAM C1T=Pfam C2=BLAST C2T=e@1.0e-3%20identity%200.8 C2E=0.001 C2I=3 C2P=80 C2MSA=Muscle")
				.append(" chain_ids=").append(chain).append(" chains=").append(
						chain).append(" email=").append(mcp.getEmail(false)).append(
						" infile=").append(pdbfilename).append(
						" submit=Mark%20Us").append(" title=").append(mcp.getTitle(false))
				.append(" tmpfile=").append(tmpfile)
				.append("\r\n--AaB03x--\r\n");
		String cfgstr = cfgcommand.toString();
		cfgstr = cfgstr.replaceAll("=", "\"\r\n\r\n");
		cfgstr = cfgstr.replaceAll(" ", "\r\n--AaB03x\r\n" + "content-disposition: form-data; name=\"");
		cfgstr = cfgstr.replaceFirst("e@1.0e", "e=1.0e");
		cfgstr = cfgstr.replaceAll("%20", " ");
		return cfgstr;
	}
}
