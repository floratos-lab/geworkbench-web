package org.geworkbenchweb.plugins.cnkb;

import java.io.Serializable;

public class InteractionParticipant implements Serializable{
 
	private static final long serialVersionUID = 7386897433039268337L;
	
	private final String geneId;    
    private final String geneName;     
   
   
    public InteractionParticipant(String geneId, String geneName) {
        this.geneId = geneId; 
        this.geneName = geneName;        
             
    }
    
    public String getGeneId() {
        return geneId;
    }

    public String getGeneName() {
        return geneName;
    }
    
    @Override
    public int hashCode() {
    	return geneName.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	if(!(obj instanceof InteractionParticipant)) return false;
    	
    	InteractionParticipant ip = (InteractionParticipant)obj;
    	if(ip.geneName.equals(geneName)) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(geneId).append("|").append(geneName);
		return sb.toString();
	}
}
