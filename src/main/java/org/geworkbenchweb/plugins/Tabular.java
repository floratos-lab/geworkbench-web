package org.geworkbenchweb.plugins;

/* Shared interface between the two tabular viewers. */
public interface Tabular extends Visualizer{

	void resetDataSource();
	void export();
	
	// TODO all the methods should be reviewed, very likely needs to be refactored
    Long getUserId();    
    void setSearchStr(String search);
    String getSearchStr();
}
