/**
 * 
 */
package org.geworkbenchweb.plugins;

/**
 * Plug-in entry.
 * 
 * @author zji
 *
 */
public class PluginEntry {
    
	final private String name, description;
	
	public PluginEntry(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getName() {return name;}
    public String getDescription() {return description;}

}
