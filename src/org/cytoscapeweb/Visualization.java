package org.cytoscapeweb;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;

/**
 * Wraps a Cytoscape Web Visualization instance.  This is a JSNI overlay type:
 * it's a thin wrapper around the native Javascript object.  It should be
 * instantiated with the <code>create</code> function call, not with a call to
 * the constructor.  All methods (except for <code>embedSWF()</code>) have been
 * overlaid by this class, including all permutations of optional arguments.
 * Note that since Java lacks first-class functions, function arguments have to
 * be provided in the form of interfaces that implement a given function; it is
 * easiest to implement an anonymous class that implements the interface to
 * specify the callback behavior.
 * @author Casey Kenley
 *
 */
public class Visualization extends JavaScriptObject{
	protected Visualization(){}
	
	private static final native JavaScriptObject wrap(ReadyCallback c)/*-{
		var f = function(){
			c.@org.cytoscapeweb.ReadyCallback::invoke()();
		};
		return f;
	}-*/;
	
	private static final native JavaScriptObject wrap(EventCallback c)/*-{
		var f = function(e){
			c.@org.cytoscapeweb.EventCallback::invoke(Lcom/google/gwt/core/client/JavaScriptObject;)(e);
		};
		return f;
	}-*/;
	
	private static final native JavaScriptObject wrap(FilterCallback c)/*-{
		var f = function(e){
			return c.@org.cytoscapeweb.FilterCallback::invoke(Lcom/google/gwt/core/client/JavaScriptObject;)(e);
		};
		return f;
	}-*/;
	
	/**
	 * Initialize Cytoscape Web in the given container.  The resource locations
	 * are set by default based upon the module's base URL.  Other options use
	 * the default values.
	 * @param containerId The id of the HTML element that will be replace by the
	 * flash object.
	 * @return The Visualization instance.
	 */
	public static final native Visualization create(String containerId)/*-{
		var moduleName = @com.google.gwt.core.client.GWT::getModuleBaseURL()();
		var options = new $wnd.Object();
		options.swfPath = moduleName + "/swf/CytoscapeWeb";
		options.flashInstallerPath = moduleName + "/swf/playerProductInstall";
        
        var vis = new $wnd.org.cytoscapeweb.Visualization(containerId, options);
        return vis;
	}-*/;
	
	public static final native Visualization create(String containerId, JavaScriptObject options)/*-{
    	var vis = new $wnd.org.cytoscapeweb.Visualization(containerId, options);
    	return vis;
	}-*/;
	
	public final native void addContextMenuItem(String lbl, String gr, EventCallback callback)/*-{
		var fn = @org.cytoscapeweb.Visualization::wrap(Lorg/cytoscapeweb/EventCallback;)(callback);
		this.addContextMenuItem(lbl, gr, fn);
	}-*/;
	
	public final native void addContextMenuItem(String lbl, EventCallback callback)/*-{
		var fn = @org.cytoscapeweb.Visualization::wrap(Lorg/cytoscapeweb/EventCallback;)(callback);
		this.addContextMenuItem(lbl, fn);
	}-*/;
	
	public final native void addDataField(String gr, JavaScriptObject dataField)/*-{
		this.addDataField(gr, dataField);
	}-*/;
	
	public final native void addDataField(JavaScriptObject dataField)/*-{
		this.addDataField(dataField);
	}-*/;
	
	public final native void addEdge(JavaScriptObject data, boolean updateVisualMappers)/*-{
		this.addEdge(data, updateVisualMappers);
	}-*/;
	
	public final native void addEdge(JavaScriptObject data)/*-{
		this.addEdge(data);
	}-*/;
	
	public final native JavaScriptObject addListener(String evt, String gr, EventCallback callback)/*-{
		var fn = @org.cytoscapeweb.Visualization::wrap(Lorg/cytoscapeweb/EventCallback;)(callback);
		this.addListener(evt, gr, fn);
		return fn;
	}-*/;
	
	public final native JavaScriptObject addListener(String evt, EventCallback callback)/*-{
		var fn = @org.cytoscapeweb.Visualization::wrap(Lorg/cytoscapeweb/EventCallback;)(callback);
		this.addListener(evt, fn);
		return fn;
	}-*/;
	
	public final native void addNode(int x, int y, JavaScriptObject data, boolean updateVisualMappers)/*-{
		this.addNode(x, y, data, updateVisualMappers);
	}-*/;
	
	public final native void addNode(int x, int y, JavaScriptObject data)/*-{
		this.addNode(x, y, data);
	}-*/;
	
	public final native void addNode(int x, int y, boolean updateVisualMappers)/*-{
		this.addNode(x, y, updateVisualMappers);
	}-*/;
	
	public final native void addNode(int x, int y)/*-{
		this.addNode(x, y);
	}-*/;
	
	public final native void customCursorsEnabled(boolean enabled)/*-{
		this.customCursorsEnabled(enabled);
	}-*/;
	
	public final native boolean customCursorsEnabled()/*-{
		return this.customCursorsEnabled();
	}-*/;
	
	public final native JavaScriptObject dataSchema()/*-{
		return this.dataSchema();
	}-*/;
	
	public final native void deselect(String gr, JsArrayMixed items)/*-{
		this.deselect(gr, items);
	}-*/;
	
	public final native void deselect(String gr)/*-{
		this.deselect(gr);
	}-*/;
	
	public final native void deselect(JsArrayMixed items)/*-{
		this.deselect(items);
	}-*/;
	
	public final native void deselect()/*-{
		this.deselect();
	}-*/;
	
	public final native void draw(JavaScriptObject options)/*-{
		this.draw(options);
	}-*/;
	
	public final native void edge(String id)/*-{
		return this.edge(id);
	}-*/;
	
	public final native void edgeLabelsVisible(boolean visible)/*-{
		this.edgeLabelsVisible(visible);
	}-*/;
	
	public final native void edgeLabelsVisible()/*-{
		return this.edgeLabelsVisible();
	}-*/;
	
	public final native JsArrayMixed edges()/*-{
		return this.edges();
	}-*/;
	
	public final native void edgesMerged(boolean merged)/*-{
		this.edgesMerged(merged);
	}-*/;
	
	public final native boolean edgesMerged()/*-{
		return this.edgesMerged();
	}-*/;
	
	public final native void edgeTooltipsEnabled(boolean enabled)/*-{
		this.edgeTooltipsEnabled(enabled);
	}-*/;
	
	public final native boolean edgeTooltipsEnabled()/*-{
		return this.edgeTooltipsEnabled();
	}-*/;
	
	public final native void exportNetwork(String format, String url, JavaScriptObject options)/*-{
		this.exportNetwork(format, url, options);
	}-*/;
	
	public final native void exportNetwork(String format, String url)/*-{
		this.exportNetwork(format, url);
	}-*/;

	public final native void filter(String gr, FilterCallback callback, boolean updateVisualMappers)/*-{
		var fn = @org.cytoscapeweb.Visualization::wrap(Lorg/cytoscapeweb/FilterCallback;)(callback);
		this.filter(gr, fn, updateVisualMappers);
	}-*/;
	
	public final native void filter(String gr, FilterCallback callback)/*-{
		var fn = @org.cytoscapeweb.Visualization::wrap(Lorg/cytoscapeweb/FilterCallback;)(callback);
		this.filter(gr, fn);
	}-*/;
	
	public final native void filter(FilterCallback callback, boolean updateVisualMappers)/*-{
		var fn = @org.cytoscapeweb.Visualization::wrap(Lorg/cytoscapeweb/FilterCallback;)(callback);
		this.filter(fn, updateVisualMappers);
	}-*/;
	
	public final native void filter(FilterCallback callback)/*-{
		var fn = @org.cytoscapeweb.Visualization::wrap(Lorg/cytoscapeweb/FilterCallback;)(callback);
		this.filter(fn);
	}-*/;
	
	public final native JavaScriptObject firstNeighbors(JsArrayMixed nodes, boolean ignoreFilteredOut)/*-{
		return this.firstNeighbors(nodes, ignoreFilteredOut);
	}-*/;
	
	public final native JavaScriptObject firstNeighbors(JsArrayMixed nodes)/*-{
		return this.firstNeighbors(nodes);
	}-*/;
	
	public final native String graphml()/*-{
		return this.graphml();
	}-*/;
	
	public final native boolean hasListener(String evt, String gr)/*-{
		return this.hasListener(evt, gr);
	}-*/;
	
	public final native boolean hasListener(String evt)/*-{
		return this.hasListener(evt);
	}-*/;

	public final native void layout(JavaScriptObject layout)/*-{
		this.layout(layout);
	}-*/;
	
	public final native void layout(String layout)/*-{
		this.layout(layout);
	}-*/;
	
	public final native JavaScriptObject layout()/*-{
		return this.layout();
	}-*/;	
	
	public final native JsArrayMixed mergedEdges()/*-{
		return this.mergedEdges();
	}-*/;
	
	public final native JavaScriptObject networkModel()/*-{
		return this.networkModel();
	}-*/;
	
	public final native JavaScriptObject node(String id)/*-{
		return this.node(id);
	}-*/;
	
	public final native void nodeLabelsVisible(boolean visible)/*-{
		this.nodeLabelsVisible(visible);
	}-*/;
	
	public final native boolean nodeLabelsVisible()/*-{
		return this.nodeLabelsVisible();
	}-*/;

	public final native JsArrayMixed nodes()/*-{
		return this.nodes();
	}-*/;
	
	public final native void nodeTooltipsEnabled(boolean enabled)/*-{
		this.nodeTooltipsEnabled(enabled);
	}-*/;
	
	public final native boolean nodeTooltipsEnabled()/*-{
		return this.nodeTooltipsEnabled();
	}-*/;
	
	public final native void panBy(int amountX, int amountY)/*-{
		this.panBy(amountX, amountY);
	}-*/;
	
	public final native void panEnabled(boolean enabled)/*-{
		this.panEnabled(enabled);
	}-*/;
	
	public final native boolean panEnabled()/*-{
		return this.panEnabled();
	}-*/;
	
	public final native void panToCenter()/*-{
		this.panToCenter();
	}-*/;
	
	public final native void panZoomControlVisible(boolean visible)/*-{
		this.panZoomControlVisible(visible);
	}-*/;
	
	public final native boolean panZoomControlVisible()/*-{
		return this.panZoomControlVisible();
	}-*/;
	
	public final native String pdf(JavaScriptObject options)/*-{
		return this.pdf(options);
	}-*/;
	
	public final native String pdf()/*-{
		return this.pdf();
	}-*/;
	
	public final native String png()/*-{
		return this.png();
	}-*/;
	
	
	public final native void ready(ReadyCallback callback)/*-{
		var fn = @org.cytoscapeweb.Visualization::wrap(Lorg/cytoscapeweb/ReadyCallback;)(callback);
		this.ready(fn);
	}-*/;
	
	public final native void removeAllContextMenuItems()/*-{
		this.removeAllContextMenuItems();
	}-*/;
	
	public final native void removeContextMenuItem(String lbl, String gr)/*-{
		this.removeContextMenuItem(lbl, gr);
	}-*/;
	
	public final native void removeContextMenuItem(String lbl)/*-{
		this.removeContextMenuItem(lbl);
	}-*/;
	
	public final native void removeDataField(String gr, String name)/*-{
		this.removeDataField(gr, name);
	}-*/;
	
	public final native void removeDataField(String name)/*-{
		this.removeDataField(name);
	}-*/;
	
	public final native void removeEdge(JavaScriptObject edge, boolean updateVisualMapper)/*-{
		this.removeEdge(edge, updateVisualMappers);
	}-*/;
	
	public final native void removeEdge(JavaScriptObject edge)/*-{
		this.removeEdge(edge);
	}-*/;
	
	public final native void removeEdge(String edge, boolean updateVisualMapper)/*-{
		this.removeEdge(edge, updateVisualMappers);
	}-*/;
	
	public final native void removeEdge(String edge)/*-{
		this.removeEdge(edge);
	}-*/;
	
	public final native void removeElements(String gr, JsArrayMixed items, boolean updateVisualMappers)/*-{
		this.removeElements(gr, items, updateVisualMappers);
	}-*/;
	
	public final native void removeElements(String gr, JsArrayMixed items)/*-{
		this.removeElements(gr, items);
	}-*/;
	
	public final native void removeElements(String gr, boolean updateVisualMappers)/*-{
		this.removeElements(gr, updateVisualMappers);
	}-*/;
	
	public final native void removeElements(JsArrayMixed items, boolean updateVisualMappers)/*-{
		this.removeElements(items, updateVisualMappers);
	}-*/;
	
	public final native void removeElements(String gr)/*-{
		this.removeElements(gr);
	}-*/;
	
	public final native void removeElements(JsArrayMixed items)/*-{
		this.removeElements(items);
	}-*/;
	
	public final native void removeElements()/*-{
		this.removeElements();
	}-*/;
	
	public final native void removeFilter(String gr, boolean updateVisualMappers)/*-{
		this.removeFilter(gr, updateVisualMappers);
	}-*/;
	
	public final native void removeFilter(String gr)/*-{
		this.removeFilter(gr);
	}-*/;
	
	public final native void removeFilter(boolean updateVisualMappers)/*-{
		this.removeFilter(updateVisualMappers);
	}-*/;
	
	public final native void removeFilter()/*-{
		this.removeFilter();
	}-*/;

	public final native void removeListener(String evt, String gr, JavaScriptObject fn)/*-{
		this.removeListener(evt, gr, fn);
	}-*/;
	
	public final native void removeListener(String evt, String gr)/*-{
		this.removeListener(evt, gr);
	}-*/;
	
	public final native void removeListener(String evt, JavaScriptObject fn)/*-{
		this.removeListener(evt, fn);
	}-*/;
	
	public final native void removeListener(String evt)/*-{
		this.removeListener(evt);
	}-*/;
	
	public final native void removeNode(JavaScriptObject node, boolean updateVisualMappers)/*-{
		this.removeNode(node, updateVisualMappers);
	}-*/;
	
	public final native void removeNode(JavaScriptObject node)/*-{
		this.removeNode(node);
	}-*/;
	
	public final native void removeNode(String node, boolean updateVisualMappers)/*-{
		this.removeNode(node, updateVisualMappers);
	}-*/;
	
	public final native void removeNode(String node)/*-{
		this.removeNode(node);
	}-*/;
	
	public final native void select(String gr, JsArrayMixed items)/*-{
		this.select(gr, items);
	}-*/;
	
	public final native void select(String gr)/*-{
		this.select(gr);
	}-*/;
	
	public final native void select(JsArrayMixed items)/*-{
		this.select(items);
	}-*/;
	
	public final native void select()/*-{
		this.select();
	}-*/;
			
	public final native JsArrayMixed selected(String gr)/*-{
		return this.selected(gr);
	}-*/;
	
	public final native JsArrayMixed selected()/*-{
		return this.selected();
	}-*/;
	
	public final native String sif(String interactionAttr)/*-{
		return this.sif(interactionAttr);
	}-*/;
	
	public final native String sif()/*-{
		return this.sif();
	}-*/;

	public final native String svg(JavaScriptObject options)/*-{
		return this.svg(options);
	}-*/;
	
	public final native String svg()/*-{
		return this.svg();
	}-*/;
	
	public final native JavaScriptObject swf()/*-{
		return this.swf();
	}-*/;
	
	public final native void updateData(String gr, JsArrayMixed items, JavaScriptObject data)/*-{
		this.updateData(gr, items, data);
	}-*/;
	
	public final native void updateData(String gr, JsArrayMixed items)/*-{
		this.updateData(gr, items);
	}-*/;
	
	public final native void updateData(String gr, JavaScriptObject data)/*-{
		this.updateData(gr, data);
	}-*/;
	
	public final native void updateData(JsArrayMixed items, JavaScriptObject data)/*-{
		this.updateData(items, data);
	}-*/;
	
	public final native void updateData(JsArrayMixed items)/*-{
		this.updateData(items);
	}-*/;
	
	public final native void updateData(JavaScriptObject data)/*-{
		this.updateData(data);
	}-*/;
	
	public final native void visualStyle(JavaScriptObject style)/*-{
		this.visualStyle(style);
	}-*/;
	
	public final native JavaScriptObject visualStyle()/*-{
		return this.visualStyle();
	}-*/;
	
	public final native void visualStyleBypass(JavaScriptObject bypass)/*-{
		this.visualStyleBypass(bypass);
	}-*/;
	
	public final native JavaScriptObject visualStyleBypass()/*-{
		return this.visualStyleBypass();
	}-*/;
	
	public final native String xgmml()/*-{
		return this.xgmml();
	}-*/;
	
	public final native void zoom(double scale)/*-{
		this.zoom(scale);
	}-*/;
	
	public final native double zoom()/*-{
		return this.zoom();
	}-*/;
	
	public final native void zoomToFit()/*-{
		this.zoomToFit();
	}-*/;
	
	public final native void ready2()/*-{
		var fn = function() {
					var n3 = {id: "3"};
					var n4 = {id: "4"};
					var n5 = {id: "5"};
					var e34 = {id: "3to4", target: "4", source: "3"};
					var e45 = {id: "4to5", target: "5", source: "4"};
										
					this.addNode(100, 100, n3, false);
					this.addNode(200, 200, n4, false);
					this.addNode(300, 300, n5, false);
					this.addEdge(e34);
					this.addEdge(e45);
					
					this.layout('ForceDirected');
				}
		
		this.ready(fn);
	}-*/;
	
	public final native void draw2()/*-{
		var xml = '\
                <graphml>\
                  <key id="label" for="all" attr.name="label" attr.type="string"/>\
                  <key id="weight" for="node" attr.name="weight" attr.type="double"/>\
                  <graph edgedefault="directed">\
                    <node id="1">\
                        <data key="label">A</data>\
                        <data key="weight">2.0</data>\
                    </node>\
                    <node id="2">\
                        <data key="label">B</data>\
                        <data key="weight">1.5</data>\
                    </node>\
                    <node id="3">\
                        <data key="label">C</data>\
                        <data key="weight">1.0</data>\
                    </node>\
                    <edge source="1" target="2">\
                        <data key="label">A to B</data>\
                    </edge>\
                    <edge source="1" target="3">\
                        <data key="label">A to C</data>\
                    </edge>\
                  </graph>\
                </graphml>\
                ';
		
		var nodes = new $wnd.Array();//[ { id: "1" }, { id: "2" } ];
		var edges = new $wnd.Array();//[ { id: "2to1", target: "1", source: "2" } ];
		nodes[0] = new Object();
		nodes[1] = new Object();
		nodes[0].id = "1";
		nodes[1].id = "2";
		edges[0] = new Object();
		edges[0].id = "2to1";
		edges[0].target = "1";
		edges[0].source = "2";
		
		var network_json = new Object(); 
		
		network_json.data = {
			"nodes": nodes,
			"edges": edges
		};
        
//        this.draw(null);

		var toDraw = new $wnd.Object();
		toDraw.network = network_json;
        this.draw(toDraw);
	}-*/;
	
}
