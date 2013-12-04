package org.geworkbenchweb.pojos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="PdbFileInfo")
public class PdbFileInfo extends AbstractPojo {
	
	private static final long serialVersionUID = 533814689112371101L;
	private static Log log = LogFactory.getLog(PdbFileInfo.class);
	
	private String filename;
	private List<String> chains;
	
	public PdbFileInfo(){}

	public PdbFileInfo(File file){
		this.filename = file.getName();
		chains = new ArrayList<String>(readChains(file));
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	// not a very efficient way to do this: read through the file
	static private Set<String> readChains(File pdbfile) {
		Set<String> chains = new HashSet<String>();

		final int chainoffset = 21;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(pdbfile));
			String line = br.readLine();
			while(line!=null) {
				if (line.startsWith("ATOM  ") || line.startsWith("HETATM")){
					chains.add(line.substring(chainoffset, chainoffset+1));
				}
				line = br.readLine();
			}
			br.close();
			log.debug("finish reading chain info");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (chains.contains(" ")) {
			chains.remove(" ");
			chains.add("_");
		}
		return chains;
	}

	public List<String> getChains() {
		return chains;
	}

	public void setChains(List<String> chains) {
		this.chains = chains;
	}

}
