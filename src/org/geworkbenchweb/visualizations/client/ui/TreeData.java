package org.geworkbenchweb.visualizations.client.ui;

import java.util.Stack;

import org.thechiselgroup.choosel.protovis.client.PVDomNode;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * This class converts cluster strings into object strucure so that it can 
 * be used by protovis
 * 
 * @author Nikhil Reddy
 */

public final class TreeData {
    
    public static JavaScriptObject data(String treeString) {

    	Stack<PVDomNode> prevNode 	=	new Stack<PVDomNode>();
		PVDomNode rootObject = null; 
		PVDomNode parentNode = null;
		
		for(int i = 0; i<treeString.length(); i++ ) {

			if(i == 0) {

				rootObject 		=	PVDomNode.create(new Object(), i + "",  0);
				parentNode		=	rootObject; 

			} else {

				if(treeString.charAt(i) == '(' ) {

					PVDomNode childNode  	= 	PVDomNode.create(new Object(), i + "", 0);
					parentNode.appendChild(childNode);
					prevNode.push(parentNode);
					parentNode = childNode;

				} else if(treeString.charAt(i) == ')') {

					try {
						
						parentNode	=	prevNode.lastElement();	
						prevNode.pop();
					
					} catch (Exception e) {
						
						//TODO	Should rewrite this loop 
						
					}
				}
			}
		}
		return rootObject;
	}

}
