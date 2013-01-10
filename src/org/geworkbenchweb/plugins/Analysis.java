/**
 * 
 */
package org.geworkbenchweb.plugins;

/**
 * Analysis.
 * 
 * TODO this eventually should define the exact interface for all analysis plug-ins.
 * At this point, let us use as a simple marker interface. 
 * 
 * @author zji
 *
 */
public interface Analysis {
    
	String getName();
    String getDescription();

}
