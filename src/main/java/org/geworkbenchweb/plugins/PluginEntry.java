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
public class PluginEntry implements Comparable<PluginEntry> {
    
	final private String name, description;
	
	public PluginEntry(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getName() {return name;}
    public String getDescription() {return description;}

	@Override
	public int compareTo(PluginEntry o) {
		return name.compareTo(o.name);
	}

}
