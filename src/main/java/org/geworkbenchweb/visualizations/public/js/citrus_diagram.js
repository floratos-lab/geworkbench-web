var $citrus_diagram = {}; /* module namespace */

$citrus_diagram.create = function(id, presence, nes) {
	var div = document.getElementById(id);
	$(div).empty();

	var svg = d3.select("div#"+id)
		.style("background-color", "#AAEEFF")
		.append("svg")
		.attr("width", 800)
		.attr("height", 600);

	/* 
	 n: number of rows; m: number of columns.
	 alteration[n], strings like "UMT", "DMT", etc
	 presence[n], each element is a string of m '0's or '1's
	 nes[m], float in string
	 */
	var alteration = ["UMT", "DMT", "UMT", "UMT"];
	var dh = 20; // height of each row
	svg.selectAll("text")
		.data(alteration)
		.enter()
		.append("text")
		.text(function(d) {
			return d;
		})
		.attr("y", function(d, i) {
			return 20 + dh*i;
		});
	
	presence = ["11011", "11111", "01111", "01011"];
	for(var ii=0; ii<presence.length; ii++) {
		var d = presence[ii].split('');
		svg.selectAll("text")
			.data(d)
			.enter()
			.append("text")
			.text(function(d){
				return d;
			})
			.attr("x", function(d, i) {
				return 100 + 20*i;
			})
			.attr("y", function(d) {
				return 200 + 10*ii;
			});
	}

};
