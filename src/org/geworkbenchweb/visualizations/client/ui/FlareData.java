package org.geworkbenchweb.visualizations.client.ui;

import java.util.Stack;

import org.thechiselgroup.choosel.protovis.client.PVDomAdapter;

import com.vaadin.terminal.gwt.client.VConsole;

public final class FlareData {

    public static class Unit {

        public Unit[] children;

        public int value;

        public String name;

        public Unit(String name) {
            this.name = name;
        }

        public Unit(String name, Unit... children) {
            this.children = children;
            this.name = name;
        }
        
    }

    public static class UnitDomAdapter implements PVDomAdapter<Unit> {

        public Unit[] getChildren(Unit t) {
            return t.children == null ? new Unit[0] : t.children;
        }

        public String getNodeName(Unit t) {
            return t.name;
        }

		@Override
		public double getNodeValue(Unit t) {
			return 0;
		}
    }

   
    
    public static Unit data(String treeString) {
    	
    	Unit parent 			= 	null;   
    	Stack<Unit> prevParent 	= 	new Stack<Unit>();
    	Unit newRoot 			= 	null;
    	Unit root 				= 	new Unit("root");
    	
    	parent = root;
    	VConsole.log(treeString);
    	
    	for(int i = 0; i<treeString.length(); i++ ) {
    		
    		if(treeString.charAt(i) == '(' ) {
    			
    			newRoot = new Unit( "node"+i, parent);
    			prevParent.push(parent);
    			parent = newRoot;
    			
    		} else if(treeString.charAt(i) == ')') {
    			
    			prevParent.pop();
    			
    			try {
    				parent = prevParent.firstElement();
    				
    			}catch(Exception e) {
    				
    				VConsole.log("Exception here = " + i);
    			}
    			
    		} else {
    			
    			VConsole.log("other characters");
    			
    		}
    	}
    	return newRoot;
    }

}
