var $kaplan_meier = {}; /* module namespace */

$kaplan_meier.create = function(id, title, xtitle, ytitle, subtypes, months, y,
        series_name) {
    var div = document.getElementById(id);
    $(div).empty();

    var x = [];
    for (var i = 0; i < months; i++) {
        x.push(i);
    }
    var index = 0; // index of y
    var data = [];
    for (var i = 0; i < subtypes; i++) {
        var s = [];
        for (var j = 0; j < months; j++) {
            s.push(y[index++]);
        }
        var trace = {
            'name' : series_name[i],
            'x' : x,
            'y' : s,
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
