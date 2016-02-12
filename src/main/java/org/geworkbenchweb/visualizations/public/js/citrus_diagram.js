var $citrus_diagram = {}; /* module namespace */

$citrus_diagram.create = function(id, presence, nes) {
	var div = document.getElementById(id);
	$(div).empty();

	var svg = d3.select("div#"+id)
		.style("background-color", "#F2F2F2")
		.append("svg")
		.attr("width", 800)
		.attr("height", 600);

	/* 
	 n: number of rows; m: number of columns.
	 alteration[n], strings like "UMT", "DMT", etc
	 presence[n], each element is a string of m '0's or '1's
	 nes[m], float in string
	 */
	/* create random test data */
	var n = 30, m = 100;
	var alteration = new Array(n);
	for(var i=0; i<n; i++) {
		var colorKeys = Object.keys($citrus_diagram.color_map);
		var randomIndex = Math.floor( Math.random() * colorKeys.length ); 
		alteration[i] = colorKeys[randomIndex];
	}

	var presence = new Array(n);
	for(var i=0; i<n; i++) {
		var p = new Array(m);
		var r = Math.random();
		for(var j=0; j<m; j++) {
			var x = Math.random();
			if(x>r)
				p[j] = '1';
			else
				p[j] = '0';
		}
		presence[i] = p.join('');
	}
	/* end of creating random test data */
	
	var x0 = 100, y0 = 100; // the main reference coordinates for the whole diagram; the top-left corner of the color-bar part
	var dx = 10; // height of each row
	var dy = 20; // height of each row
	
	svg.selectAll("text")
		.data(alteration)
		.enter()
		.append("text")
		.text(function(d) {
			return d;
		})
		.attr( {"x": x0-10,
				"y": function(d, i) { return y0 + dy*i+ 0.7*dy; },
				"text-anchor": "end",
				"font-family": "sans-serif",
				"font-size": "12px"
		});
	
	for(var ii=0; ii<presence.length; ii++) {
		var d = presence[ii].split('');
		svg.selectAll("rect#row")
			.data(d)
			.enter()
			.append("rect")
			.text(function(d){
				return d;
			})
			.attr("x", function(d, i) {
				return x0 + dx*i;
			})
			.attr("y", function(d) {
				return y0 + dy*ii;
			})
			.attr( {"width": dx,
					"height": dy,
					"fill": function(d) {
						var color = "white";
						if(d=='1')
							color = $citrus_diagram.color_map[alteration[ii]]
						return color; 
					} 
			} );
	}

};

$citrus_diagram.color_map = {'UMT':'green', 'DMT':'darkgreen', 'AMP':'red', 'DEL':'blue', 'SNV':'black', 'GFU':'orange'};