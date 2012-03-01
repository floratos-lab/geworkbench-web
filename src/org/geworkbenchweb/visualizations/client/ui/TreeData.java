package org.geworkbenchweb.visualizations.client.ui;

import java.util.Stack;

import org.thechiselgroup.choosel.protovis.client.PVDomNode;

import com.google.gwt.core.client.JavaScriptObject;
import com.vaadin.terminal.gwt.client.VConsole;

public final class TreeData {
    
    public static JavaScriptObject data(String treeString) {

		PVDomNode rootObject 		=	PVDomNode.create(new Object(), "root",  0);
		
		PVDomNode parentNode		=	rootObject;  
		Stack<PVDomNode> prevNode 	=	new Stack<PVDomNode>();
		
		for(int i = 0; i<treeString.length(); i++ ) {

			if(treeString.charAt(i) == '(' ) {
				
				PVDomNode childNode  	= 	PVDomNode.create(new Object(), "child"+i, 0);
				parentNode.appendChild(childNode);
				prevNode.push(parentNode);
				parentNode = childNode;
				VConsole.log(parentNode.nodeName());
				
			} else if(treeString.charAt(i) == ')') {
				
				prevNode.pop();
				
				try{
		
					parentNode	=	prevNode.lastElement();	
				
				}catch (Exception e) {
					
					//TODO
					
				}
				
			} else {

				//TODO 
			}
			
		}
		return rootObject;
	}

}
