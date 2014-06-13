package org.geworkbenchweb.plugins.cnkb;

public class InteractionParticipant {

	private final String dSGeneId;    
    private final String dSGeneName;     
    private final String dbSource;   
   
    public InteractionParticipant(String dSGeneMarker, String dSGeneName, String dbSource) {
        this.dSGeneId = dSGeneMarker; 
        this.dSGeneName = dSGeneName;        
        this.dbSource = dbSource;       
    }
    
    public String getdSGeneId() {
        return dSGeneId;
    }

    public String getdSGeneName() {
        return dSGeneName;
    }
    public String getDbSource() {
        return this.dbSource;
    }
}
