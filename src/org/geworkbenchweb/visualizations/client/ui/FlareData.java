package org.geworkbenchweb.visualizations.client.ui;

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
        
    	String dataString = "\"root\", new Unit(";
    	for(int i = 0; i<treeString.length(); i++ ) {
    		
    		if(treeString.charAt(i) == '(' ) {
    			if(i == 0) {
    				dataString = dataString + "\"" + i +"\"";
    			}else {
    				dataString = dataString + ", new Unit(\"" + i +"\"";
    			}
    		}else {
    			
    			dataString = dataString + ")";
    			
    		}
    	}
    	VConsole.log(dataString);
    	return new Unit("root", new Unit("0", new Unit("1")), new Unit("2"));
    }

}
