package org.geworkbenchweb.interactions.CNKB; 
 

/**
 * @author Min You
 * @version $Id$
 */
 
public class Constants  {
	 
	public static final String DEL = "|";
	
	public static final String REGEX_DEL = "\\|";	
	
	public static final String GeneCards_PREFIX = "http://www.genecards.org/cgi-bin/carddisp.pl?gene=";

	public static final String Entrez_PREFIX = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=Retrieve&dopt=full_report&list_uids=";

	public static final String PROPERTIES_FILE = "conf/application.properties";

	public static final String CNKB_HITS = "CNKB_HITS";

	public static final String INTERACTIONS_SERVLET_URL = "interactions_servlet_url";
	
	public static final String INTERACTIONS_SERVLET_CONNECTION_TIMEOUT = "interactions_servlet_connection_timeout";
	
	public static final String MAX_INTERACTIONS_NUMBER = "max_interaction_number";
	
	public static final String INTERACTIONS_FLAG = "interaction_flag";
	
	public static final String GOTERMCOLUMN = "GO Annotation";

	public static final String ACTIVETABLE = "ACTIVETABLE";

	public static final String DETAILTABLE = "DETAILTABLE";

	public static final String MARKERLABEL = "Marker";

	public static final String GENELABEL = "Gene";

	public static final String GENETYPELABEL = "Gene Type";

	public static final String PROTEIN_DNA = "protein-dna";

	public static final String PROTEIN_PROTEIN = "protein-protein";

	public static final String COLUMNLABELPOSTFIX = " #";

	public static final String CNKB_SELECTION = "cnkb selection";

	public static final String CNKB_SELECTION_INDEX = "CNKB_SELECTION_INDEX";
	
	public static final String CNKB_PREFERENCE = "CNKB_PREFERENCE";

	public static final String SELECTCONTEXT = "Select Context";

	public static final String SELECTVERSION = "Select Version";

	public static final String DISPLAYSELECTEDINTERACTIONTYPE = "displaySelectedInteractionTypes";

	public static final String NETWORKSELECTEDINTERACTIONTYPE = "networkSelectedInteractionTypes";

	public static final String ENTREZ_GENE = "Entrez Gene";
  
	public static final String UNIPORT = "Uniprot";
	
	public static final String  TF= "TF";
	
	public static final String  TRANSCRIPTION_FACTOR= "Transcription Factor";

    public static final String  K= "K";
	
	public static final String  KINASE= "Kinase";
	
    public static final String  P= "P";
	
	public static final String  PHOSPHATASE= "Phosphatase";

	public static final String SIF_FORMAT = "sif format";
	
	public static final String ADJ_FORMAT = "adj format";	 
	
	public static final String GENE_SYMBOL_ONLY = "Gene Symbol Only";
	
	public static final String GENE_SYMBOL_PREFERRED = "Gene Symbol Preferred";
	
	public static final String ENTREZ_ID_ONLY = "Entrez ID Only";
	
	public static final String ENTREZ_ID_PREFERRED = "Entrez ID Preferred";
	
	public static final String PROJECT = "project ";
	
	public static final String FILE = "file";
	
}
