var $kaplan_meier = {}; /* module namespace */

$kaplan_meier.create = function(id, title, xtitle, ytitle, subtypes, p,
        series_name, series_count) {
    var div = document.getElementById(id);
    $(div).empty();

    var index = 0; // index of p, which are all the x and y coordinate values
    var data = [];
    for (var i = 0; i < subtypes; i++) {
        var x = [];
        var y = [];
        for (var j = 0; j < series_count[i]; j++) {
            x.push(p[index++]);
            y.push(p[index++]);
        }
        var trace = {
            'name' : "Subtype "+series_name[i],
            'x' : x,
            'y' : y,
        };
        data.push(trace);
    }

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
            gridcolor : "#ddd",
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
