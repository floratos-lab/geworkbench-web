var $interaction_color_mosaic = {}; /* module namespace */

$interaction_color_mosaic.create = function(id, interactome, gene_symbol,
		p_value, color) {
	var div = document.getElementById(id);

	$(div).empty();

	var c = document.createElement('TABLE');
	c.id = 'color-mosaic';
	div.appendChild(c);

	var header = '<tr><th>Gene Symbol</th>';
	for (var i = 0; i < interactome.length; i++) {
		header += '<th>' + interactome[i] + '</th>';
	}
	header += '</tr>';
	$('#color-mosaic').append(header);
	for (var i = 0; i < gene_symbol.length; i++) {
		$('#color-mosaic').append('<tr></tr>');
		var row = '<td width=100 height=10>' + gene_symbol[i] + '</td>';
		for (var j = 0; j < interactome.length; j++) {
			var index = i*interactome.length + j;
			var p_formatted = '';
			var p = parseFloat(p_value[index]);
			if(!isNaN(p)) p_formatted = p.toFixed(3);
			row += '<td width=100 height=10 bgcolor=' + color[index] + '>' + p_formatted + '</td>';
		}
		$('#color-mosaic').find('tr').eq(i + 1).append(row);
	}
};
