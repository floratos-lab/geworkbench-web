var $volcano_plot = {}; /* module namespace */

$volcano_plot.create = function(id, title, xtitle, ytitle, x, y, name,
		series_name) {
	var div = document.getElementById(id);
	$(div).empty();

	var color = [];
	for (var i = 0; i < x.length; i++) {
		color.push(Math.abs(x[i] * y[i]));
	}
	var data = [ {
		name : series_name,
		mode : "markers",
		x : x,
		y : y,
		marker : {
			color : color,
			colorscale : [ [ 0, 'rgb(0,0,255)' ], [ 0.66, 'rgb(0,0,205)' ],
					[ 1, 'rgb(255,0,0)' ] ],
		},
		text : name,
	} ];

	var layout = {
		title : title,
		xaxis : {
			title : xtitle,
			showline : true,
			showgrid : false,
			ticks : "inside",
		},
		yaxis : {
			title : ytitle,
			showline : true,
			gridcolor: "#ddd",
		},
		margin : {
			t : 50
		},
		hovermode : 'closest',
		showlegend : true,
		legend : {
			bgcolor : '#E2E2E2',
			orientation : "h",
		},
	};
	Plotly.plot(div, data, layout, {
		displaylogo : false,
		showLink : false, // FIXME this does not work
	});
};
