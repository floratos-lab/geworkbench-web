var $citrus_diagram = {}; /* module namespace */

$citrus_diagram.create = function(id, alteration, samples, presence, preppi, cindy, pvalue, nes) {
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

    svg.attr('width', m*dx+200);
    svg.attr('height', n*dy+150);
	
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
							color = $citrus_diagram.color_map[alteration[ii].substr(0,3)];
						return color; 
					} 
			} );
	}

	svg.selectAll("text#col")
		.data(samples)
		.enter()
		.append("text")
		.text(function(d) {
			return d;
		})
        .style( "writing-mode", "tb")
		.attr( {"x": function(d, i) { return x0 + dx*i+3; },
				"y": y0-5,
				"text-anchor": "end",
				"font-family": "sans-serif",
				"font-size": "10px"
		});

	svg.selectAll("circle#preppi")
		.data(preppi)
		.enter()
		.append("circle")
		.attr( {"cx" : x0 + dx*m + 20,
                "cy" : function(d, i) { return y0+ dy*i + 10; },
                "r" : 5,
                "stroke" : "#005500",
                "fill" : function(d, i) { if(d==0) return "#FFFFFF"; else return "orange"; }
        });
    if(n>0)
    svg.append('text').text('PrePPI').attr({"text-anchor": "end", "x": x0+dx*m+20, "y":y0-5, "font-family": "sans-serif", "font-size": "12px"})
        .style( "writing-mode", "tb");
	svg.selectAll("circle#cindy")
		.data(cindy)
		.enter()
		.append("circle")
		.attr( {"cx" : x0 + dx*m + 40,
                "cy" : function(d, i) { return y0+ dy*i + 10; },
                "r" : 5,
                "stroke" : "#005500",
                "fill" : function(d, i) { if(d==0) return "#FFFFFF"; else return "red"; }
        });
    if(n>0)
    svg.append('text').text('CINDy').attr({"text-anchor": "end", "x": x0+dx*m+40, "y":y0-5, "font-family": "sans-serif", "font-size": "12px"})
        .style( "writing-mode", "tb");

	svg.selectAll("text#value")
		.data(pvalue)
		.enter()
		.append("text")
		.text(function(d) {
			return d.toPrecision(3);
		})
		.attr( {"x" : x0 + dx*m + 60,
                "y" : function(d, i) { return y0+ dy*i + 10; },
				"font-family": "sans-serif",
				"font-size": "12px"
        });
    if(n>0)
    svg.append('text').text('p-value').attr({"text-anchor": "end", "x": x0+dx*m+60, "y":y0-5, "font-family": "sans-serif", "font-size": "12px"})
        .style( "writing-mode", "tb");

    svg.selectAll("rect#nes")
		.data(nes)
		.enter()
		.append("rect")
		.attr( {"width":dx, "height":dy, "x" : function(d, i) { return x0+ dx*i; },
                "y" : y0+ n*dy + 10,
                "fill": function(d) { return $citrus_diagram.colorscale(maxAbsValue, d); }
        });
    if(m>0)
    svg.append('text').text('NES').attr({"text-anchor": "end", "x": x0-20, "y":y0+n*dy+25, "font-family": "sans-serif", "font-size": "12px"});
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
}
