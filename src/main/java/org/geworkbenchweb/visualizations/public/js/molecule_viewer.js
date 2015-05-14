var $molecule_viewer = {}; /* module namespace */

$molecule_viewer.create = function(id, path) {
	var div = document.getElementById(id);
	
	$(div).empty();
	
	var c = document.createElement('CANVAS');
	var w = $(div).parents('div.v-verticallayout').width();
	var h = $(div).parents('div.v-verticallayout').height();
    c.width = w/2;
	c.height = h/4	
	var ctx = c.getContext("2d");
	ctx.fillStyle = '#A9F5BC';
	ctx.fillRect(50, 50, c.width-50, c.height-50);
	ctx.font = "12px Arial";
	ctx.fillStyle = 'black';
	ctx.fillText("Placeholder: "+id+" "+path, 60, 80);
	
	div.appendChild(c);
};
