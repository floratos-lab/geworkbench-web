package org.geworkbenchweb.pojos;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Entity;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
public class Network extends AbstractPojo {

	private static final long serialVersionUID = 2540655095438511032L;
	
	/* these two must have the same length to mimic a map */
	private String[] node1;
	private NetworkEdges[] edges;
	
	public Network() {}
	
	public Network(Map<String, NetworkEdges> networkMap) {
		Set<String> n1s = networkMap.keySet();
		int c = n1s.size();
		node1 = new String[c];
		edges = new NetworkEdges[c];
		int index = 0;
		for(String n1 : n1s) {
			node1[index] = n1;
			edges[index] = networkMap.get(n1);
			index++;
		}
	}

	public String[] getNode1() {
		return node1;
	}
	
	public void setNode1(String[] node1) {
		this.node1 = node1;
	}
	
	public NetworkEdges[] getEdges() {
		return edges;
	}
	
	public void setEdges(NetworkEdges[] edges) {
		this.edges = edges;
	}

	public int getEdgeNumber() {
		int c = 0;
		for(NetworkEdges e : edges) {
			c += e.getCount();
		}
		return c;
	}

	public int getNodeNumber() {
		Set<String> set = new TreeSet<String>();
		set.addAll(Arrays.asList(node1));
		for(NetworkEdges e : edges) {
			set.addAll( Arrays.asList(e.getNode2s()) );
		}
		return set.size();
	}
	
	@Override 
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int index = 0; index < node1.length; index++) {
			String n1 = node1[index];

			sb.append(n1 + "\t");

			NetworkEdges edge = edges[index];
			for (int j = 0; j < edge.getCount(); j++) {
				sb.append(edge.getNode2s()[j] + "\t" + edge.getWeights()[j]
						+ "\t");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public String toSIF() {
		StringBuffer sb = new StringBuffer();
		for (int index = 0; index < node1.length; index++) {

			NetworkEdges edge = edges[index];
			if(edge.getCount()==0) {
				continue;
			}
			
			String n1 = node1[index];
			sb.append(n1);

			String[] types = edge.getInteractionTypes();
			/* We will allow types to be null to support old database that does not have this field. */
			if(types==null) {
				types = new String[edge.getCount()];
				for(int i=0; i<types.length; i++) {
					types[i] = "na";
				}
			}
			String[] node2s = edge.getNode2s();
			String previousType = null;
			for (int j = 0; j < edge.getCount(); j++) {
				String type = types[j];
				String node2 = node2s[j];
				if (previousType == null) {
					sb.append('\t').append(type).append('\t').append(node2);
				} else if (previousType == type) {
					sb.append('\t').append(node2);
				} else { // different type, create new line
					sb.append('\n').append(n1).append('\t').append(type).append('\t').append(node2);
				}
				previousType = types[j];
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/* Map cannot work probably due to an eclipselink bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=364922 */
	/*
	@OneToMany(cascade = CascadeType.PERSIST)
	private Map<String, NetworkEdge> edges;
	*/
	
}
