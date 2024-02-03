window.org_geworkbenchweb_visualizations_CitrusDiagram = function () {
	this.onStateChange = function () {
		if(this.getState().zoom) {
			$citrus_diagram.rescale(this.getState().xzoom, this.getState().yzoom)
		} else {
			$citrus_diagram.create(this.getElement(), this.getState().alteration, this.getState().samples, this.getState().presence,
			 this.getState().preppi, this.getState().cindy, this.getState().pvalue, this.getState().nes)
		}
	}	
}

var $citrus_diagram = {}; /* module namespace */

// external size
$citrus_diagram.width = 900;
$citrus_diagram.height = 600;

$citrus_diagram.x_scale_coef = 0;
$citrus_diagram.x_scale = 1;
$citrus_diagram.y_scale_coef = 0;
$citrus_diagram.y_scale = 1;

$citrus_diagram.create = function(div, alteration, samples, presence, preppi, cindy, pvalue, nes) {
	$(div).empty();

	var svg = d3.select(div)
		.style("background-color", "#F2F2F2")
		.append("svg")
        .attr({"viewBox":"0 0 "+$citrus_diagram.width+" "+$citrus_diagram.height,
         "preserveAspectRatio":"none"});

	/* 
	 n: number of rows; m: number of columns.
	 alteration[n], strings like "UMT", "DMT", etc
	 presence[n], each element is a string of m '0's or '1's
     sample[m], string
     preppi[n], 1 or 0
     cindy[n], 1 or 0
     pvalue[n], float point
	 nes[m], float in string
	 */

	var n = alteration.length;
	var m = samples.length;

	var maxAbsValue = 0;
    for(var i=0; i<m; i++) {
        if( Math.abs(nes[i])>maxAbsValue ) maxAbsValue = Math.abs(nes[i]);
    }
	
	var x0 = 100, y0 = 100; // the main reference coordinates for the whole diagram; the top-left corner of the color-bar part
	var dx = 10; // height of each row
	var dy = 20; // height of each row

    // size of 'presence window'
    var p_width = $citrus_diagram.width - 200;
    var p_height = $citrus_diagram.height - 150;
    
    $citrus_diagram.x_scale_coef = (p_width/(m*dx)-1)*0.01;
    $citrus_diagram.y_scale_coef = (p_height/(n*dy)-1)*0.01;

    var lr_window = svg.append("svg").attr({"y":y0, "height":p_height});
    var lr_group = lr_window.append("g").attr("id", "lr_group");
	var alteration_labels = lr_group.selectAll("text")
		.data(alteration)
		.enter()
        .append("a")
        .attr({"xlink:href": function(d) {
                var gene_symbol = d.substring(d.indexOf('_')+1);
                return "http://www.genecards.org/cgi-bin/carddisp.pl?gene=" +gene_symbol + "&alias=yes";
            }, "target": "_blank"})
		.append("text")
		.text(function(d) {
			return d;
		})
		.attr( {"x": x0-10,
				"y": function(d, i) { return dy*i+ 0.7*dy; },
				"text-anchor": "end",
				"font-family": "sans-serif",
				"font-size": "12px",
				"text-decoration": "underline",
				"fill": "blue"
		})
        .on("mouseover", function() {
            d3.select(this).attr('fill', 'black');
        })
        .on("mouseout", function() {
            d3.select(this).attr('fill', 'blue');
        });

    var p_x = 0, p_y = 0; // presence window translation
	
    var drag = d3.behavior.drag()
        .on("drag", function() {
            var x_scale = $citrus_diagram.x_scale;
            var y_scale = $citrus_diagram.y_scale;
            p_x = Math.min(0, Math.max(p_width-m*dx*x_scale, p_x+d3.event.dx));
            p_y = Math.min(0, Math.max(p_height-n*dy*y_scale, p_y+d3.event.dy));

            container.attr("transform", "translate(" + [p_x, p_y] + ")scale(" + [x_scale, y_scale] + ")");
            hf_group.attr("transform", "translate(" + [p_x, 0] + ")scale(" + [x_scale, 1] + ")");
            lr_group.attr("transform", "translate(" + [0, p_y] + ")scale(" + [1, y_scale] + ")");
        });

    var presence_window = svg.append("svg") // presence window
        .attr({"x":x0, "y":y0, "width":p_width, "height":p_height});
    var container = presence_window.append("g").attr("id", "main_container");

	for(var ii=0; ii<presence.length; ii++) {
		var d = presence[ii].split('');
		container.selectAll("rect#row")
			.data(d)
			.enter()
			.append("rect")
			.attr("x", function(d, i) {
				return dx*i;
			})
			.attr("y", function(d) {
				return dy*ii;
			})
			.attr( {"width": dx,
					"height": dy,
					"fill": function(d) {
						var color = "white";
						if(d=='1')
							color = $citrus_diagram.color_map[alteration[ii].substr(0,3)];
						return color; 
					} 
			} );
	}
    container.call(drag);

    var hf_window = svg.append("svg")
        .attr({"x":x0, "width": p_width});
    var hf_group = hf_window.append("g").attr("id", "hf_group");
	hf_group.selectAll("text#col")
		.data(samples)
		.enter()
		.append("text")
		.text(function(d) {
			return d;
		})
        .style( "writing-mode", "tb")
		.attr( {"x": function(d, i) { return dx*i+3; },
				"y": y0-5,
				"text-anchor": "end",
				"font-family": "sans-serif",
				"font-size": "10px"
		});

	lr_group.selectAll("circle#preppi")
		.data(preppi)
		.enter()
		.append("circle")
		.attr( {"cx" : x0 + p_width + 20,
                "cy" : function(d, i) { return dy*i + 10; },
                "r" : 5,
                "stroke" : "#005500",
                "fill" : function(d, i) { if(d==0) return "#FFFFFF"; else return "orange"; }
        });
    if(n>0)
    svg.append('text').text('PrePPI').attr({"text-anchor": "end", "x": x0+p_width+20, "y":y0-5, "font-family": "sans-serif", "font-size": "12px"})
        .style( "writing-mode", "tb");
	lr_group.selectAll("circle#cindy")
		.data(cindy)
		.enter()
		.append("circle")
		.attr( {"cx" : x0 + p_width + 40,
                "cy" : function(d, i) { return dy*i + 10; },
                "r" : 5,
                "stroke" : "#005500",
                "fill" : function(d, i) { if(d==0) return "#FFFFFF"; else return "red"; }
        });
    if(n>0)
    svg.append('text').text('CINDy').attr({"text-anchor": "end", "x": x0+p_width+40, "y":y0-5, "font-family": "sans-serif", "font-size": "12px"})
        .style( "writing-mode", "tb");

	lr_group.selectAll("text#value")
		.data(pvalue)
		.enter()
		.append("text")
		.text(function(d) {
			return d.toPrecision(2);
		})
		.attr( {"x" : x0 + p_width + 55,
                "y" : function(d, i) { return dy*i + 15; },
				"font-family": "sans-serif",
				"font-size": "12px"
        });
    if(n>0)
    svg.append('text').text('p-value').attr({"text-anchor": "end", "x": x0+p_width+60, "y":y0-5, "font-family": "sans-serif", "font-size": "12px"})
        .style( "writing-mode", "tb");

    hf_group.selectAll("rect#nes")
		.data(nes)
		.enter()
		.append("rect")
		.attr( {"width":dx, "height":dy, "x" : function(d, i) { return dx*i; },
                "y" : y0+ p_height + 10,
                "fill": function(d) { return $citrus_diagram.colorscale(maxAbsValue, d); }
        });
    if(m>0)
    svg.append('text').text('NES').attr({"text-anchor": "end", "x": x0-20, "y":y0+p_height+25, "font-family": "sans-serif", "font-size": "12px"});
    $citrus_diagram.zoom(); // retain the scale even if it is a new query
};

$citrus_diagram.color_map = {'UMT':'green', 'DMT':'darkgreen', 'AMP':'red', 'DEL':'blue', 'SNV':'black', 'GFU':'orange'};

$citrus_diagram.colorscale = function(maxAbsValue, value) {
    var i = 0;
    if(maxAbsValue!=0) i = Math.floor(255*value/maxAbsValue);
    if(i<0) {
        var r = 255 + i;
        var g = 255 + i;
        var b = 255;
    } else {
        var r = 255;
        var g = 255 - i;
        var b = 255 - i;
    }
    return "rgb("+r+","+g+","+b+")";
};

$citrus_diagram.rescale = function(xzoom, yzoom) {
	$citrus_diagram.x_scale = 1 + $citrus_diagram.x_scale_coef*xzoom;
	$citrus_diagram.y_scale = 1 + $citrus_diagram.y_scale_coef*yzoom;
	$citrus_diagram.zoom();
};

$citrus_diagram.zoom = function() {
	var x_scale = $citrus_diagram.x_scale;
	var y_scale = $citrus_diagram.y_scale;
	var ctnr = d3.select("g#main_container");
	ctnr.attr("transform", "translate(" + [0, 0] + ")scale(" + [x_scale, y_scale] + ")");
	var hf = d3.select("g#hf_group");
	hf.attr("transform", "scale(" + [x_scale, 1] + ")");
	var lr = d3.select("g#lr_group");
	lr.attr("transform", "scale(" + [1, y_scale] + ")");
};
