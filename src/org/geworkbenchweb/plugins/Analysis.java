/**
 * 
 */
package org.geworkbenchweb.plugins;

/**
 * Analysis.
 * 
 * TODO this could be used to define the exact interface for all analysis plug-ins.
 * At this point, it is simply used as an identifier. Actual analysis is in its *UI class. 
 * 
 * @author zji
 *
 */
public class Analysis {
    
	final private String name, description;
	
	public Analysis(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getName() {return name;}
    public String getDescription() {return description;}

}
