var $network_viewer = {}; /* module namespace */

$network_viewer.create = function(id, nodeArray, edgeArray, layout) {
	
	cy = cytoscape({
		container: document.getElementById(id),
  
		style: cytoscape.stylesheet()
		    .selector('node')
		      .css({
		        'font-size': 10,
		        'content': 'data(id)',
		        'border-color': 'black',
		        'border-width': 0.5,
		        'background-color': '#fd0',
		        'min-zoomed-font-size': 8,
		      })
		//    .selector('node[node_type = "interested type of the node"]')
		//      .css({
		//        'background-color': '#666',
		//      })
		    .selector('node:selected')
		      .css({
		        'background-color': 'green',
		      })
		    .selector('edge')
		      .css({
		    	  'width': 1,
		    	  'line-color': '#00a',
		    	  'target-arrow-shape': 'triangle',
		    	  'target-arrow-color': '#00a',
		    	  'target-arrow-fill': 'hollow',
		    	  'opacity': 0.7,
		      })
		//    .selector('edge[edge_type = "interested type of the edge"]')
		//      .css({
		//        'line-color': '#F6C28C'
		//      })
		  .selector('edge:selected')
		    .css({
		    	'line-color': '#a00',
		    }),
	    layout: {
		    'name': layout,
		  } 
	});
	
	cy.on('cxttap', 'node', function(){
		  try { // your browser may block popups
		    window.open( this.data('href') );
		  } catch(e){ // fall back on url change
		    window.location.href = this.data('href'); 
		  } 
		});
	
	var node_array = [];
	for (i = 0; i < nodeArray.length; i++) {
		var tmp = nodeArray[i].split(",");
		var gene_symbol = tmp[1];
		node_array[i] = {
			"group" : "nodes",
			"data" : {
				"id" : gene_symbol,
				"href": "http://www.ncbi.nlm.nih.gov/gene?term="+gene_symbol,
				//"node_type": ...
			}
		};
	}
	cy.add(node_array);
	
	var edge_array = [];
	for(i=0; i<edgeArray.length; i++) {
		var tmp	= edgeArray[i].split(",");
		edge_array[i] = {
				"group" : "edges", 
				"data" : {
					"id": "gwb_ntwk_edge_"+i, /* just try to be unique */ 
					"source" : tmp[0],
					"target" : tmp[1]
				}
		};
	}
	cy.add( edge_array );
	
	console.log('debug');
};

$network_viewer.set_color = function(id, nodeArray, colors) {
	console.log("color map size=" + colors.length);
	var colorMap = {};
	for (var i = 0; i < colors.length; i++) {
		console.log(i+"--"+colors[i]);
		var tmp = colors[i].split(":");
		var gene_symbol = tmp[0];
		colorMap[gene_symbol] = tmp[1];
		console.log(gene_symbol + ":" + tmp[1]);
	}

	cy.nodes().style({
		'background-color' : function(ele) {
			var node_color = colorMap[ele.data('id')];
			if (node_color)
				return node_color;
			else
				return 'white';
		}
	});
}
