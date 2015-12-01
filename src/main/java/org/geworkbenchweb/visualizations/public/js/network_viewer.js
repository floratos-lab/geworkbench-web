var $network_viewer = {}; /* module namespace */

$network_viewer.create = function(id, nodeArray, edgeArray, layout) {

	cy = cytoscape({
		container : document.getElementById(id),

		style : cytoscape.stylesheet().selector('node').css({
			'font-size' : 10,
			'content' : 'data(id)',
			'border-color' : 'black',
			'border-width' : 0.5,
			'background-color' : '#fd0',
			'min-zoomed-font-size' : 8,
		}).selector('node:selected').css({
			'background-color' : 'green',
		}).selector('edge').css({
			'width' : 1,
			'line-color' : '#00a',
			'target-arrow-shape' : 'triangle',
			'target-arrow-color' : '#00a',
			'target-arrow-fill' : 'hollow',
			'opacity' : 0.7,
		}).selector('edge:selected').css({
			'line-color' : '#a00',
		}),
		layout : {
			'name' : layout,
		}
	});

	cy.on('cxttap', 'node', function() {
		$.contextMenu( 'destroy', '#'+id );
		var sym = this.data('id');
		$.contextMenu({
			selector: '#'+id,
			callback: function(key, options) {
				switch(key) {
	            case 'gene': 
	          	  linkUrl = "http://www.ncbi.nlm.nih.gov/gene?cmd=Search&term="+sym;                                    	  
	          	  break;
	            case 'protein':
	          	  linkUrl = "http://www.ncbi.nlm.nih.gov/protein?cmd=Search&term=" + sym + "&doptcmdl=GenPept";                                    	  
	          	  break;
	            case 'pubmed':
	          	  linkUrl = "http://www.ncbi.nlm.nih.gov/pubmed?cmd=Search&term=" + sym + "&doptcmdl=Abstract";                                    	  
	          	  break;
	            case 'nucleotide':
	          	  linkUrl = "http://www.ncbi.nlm.nih.gov/nucleotide?cmd=Search&term=" + sym + "&doptcmdl=GenBank";                                    	  
	          	  break;
	            case 'alldatabases':
	          	  linkUrl = "http://www.ncbi.nlm.nih.gov/gquery/?term="+sym;                                    	  
	          	  break;
	            case 'structure':
	          	  linkUrl = "http://www.ncbi.nlm.nih.gov/structure?cmd=Search&term=" + sym + "&doptcmdl=Brief";                                    	  
	          	  break;
	            case 'omim':
	          	  linkUrl = "http://www.ncbi.nlm.nih.gov/omim?cmd=Search&term=" + sym + "&doptcmdl=Synopsis";                                    	  
	          	  break;
	            case 'genecards':
	          	  linkUrl = "http://www.genecards.org/cgi-bin/carddisp.pl?gene=" + sym + "&alias=yes";                                    	  
	          	  break;
				}
				window.open(linkUrl);
			},
			items: {
				"entrez": {
                    "name": "Entrez", 
                    "items": {
                        "gene": {"name": "Gene"},
                        "protein": {"name": "Protein"},
                        "pubmed": {"name": "PubMed"},
                        "nucleotide": {"name": "Nucleotide"},
                        "alldatabases": {"name": "All Databases"},
                        "structure": {"name": "Structure"},
                        "omim": {"name": "OMIM"}
                    }
                },
                "genecards": {name: "GeneCards"},
			}
		});
	});

	var node_array = [];
	for (i = 0; i < nodeArray.length; i++) {
		var tmp = nodeArray[i].split(",");
		var gene_symbol = tmp[1];
		node_array[i] = {
			"group" : "nodes",
			"data" : {
				"id" : gene_symbol,
				"href" : "http://www.ncbi.nlm.nih.gov/gene?term=" + gene_symbol,
			}
		};
	}
	cy.add(node_array);

	var edge_array = [];
	for (i = 0; i < edgeArray.length; i++) {
		var tmp = edgeArray[i].split(",");
		edge_array[i] = {
			"group" : "edges",
			"data" : {
				"id" : "gwb_ntwk_edge_" + i, /* just try to be unique */
				"source" : tmp[0],
				"target" : tmp[1]
			}
		};
	}
	cy.add(edge_array);
};

$network_viewer.set_color = function(colors) {
	var colorMap = {};
	for (var i = 0; i < colors.length; i++) {
		var tmp = colors[i].split(":");
		var gene_symbol = tmp[0];
		colorMap[gene_symbol] = tmp[1];
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
