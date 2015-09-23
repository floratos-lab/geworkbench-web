var $interaction_color_mosaic = {}; /* module namespace */

$interaction_color_mosaic.create = function(id) {
	var div = document.getElementById(id);

	$(div).empty();

	var c = document.createElement('TABLE');
	c.id = 'color-mosaic';
	div.appendChild(c);

	// test content
	var rows = 6, cols = 7;
	for (var i = 0; i < rows; i++) {
		$('#color-mosaic').append('<tr></tr>');
		for (var j = 0; j < cols; j++) {
			// a random color for testing
			var color = '#' + Math.floor(Math.random() * 16777215).toString(16);
			$('#color-mosaic').find('tr').eq(i).append(
					'<td width=30 height=10 bgcolor=' + color + '></td>');
			$('#color-mosaic').find('tr').eq(i).find('td').eq(j).attr(
					'data-row', i).attr('data-col', j);
		}
	}
};
