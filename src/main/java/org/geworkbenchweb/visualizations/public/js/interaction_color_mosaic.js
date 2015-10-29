var $interaction_color_mosaic = {}; /* module namespace */

$interaction_color_mosaic.create = function(id, interactome, gene_symbol,
		p_value, color) {
	var div = document.getElementById(id);

	$(div).empty();

	var c = document.createElement('TABLE');
	c.id = 'color-mosaic';
	div.appendChild(c);

	// TODO eventually here will be multiple interactomes
	$('#color-mosaic').append(
			'<tr><th>Gene Symbol</th><th>' + interactome + '</th></tr>');
	for (var i = 0; i < gene_symbol.length; i++) {
		$('#color-mosaic').append('<tr></tr>');
		$('#color-mosaic').find('tr').eq(i + 1).append(
				'<td width=100 height=10>' + gene_symbol[i] + '</td>').append(
				'<td width=100 height=10 bgcolor=' + color[i] + '>'
						+ p_value[i] + '</td>');
	}
};
