function createBarcodeTable(myElementById, columnNames, regulators, barcodeMap, barHeight) {
 
	var columnNameList = JSON.parse(columnNames);
	var regulatorList = JSON.parse(regulators);
	var barcodeMapObj = JSON.parse(barcodeMap);  

	var myTableDiv = document.getElementById(myElementById);

	var table = document.createElement('TABLE');
	var header = table.createTHead();
	for ( var i = 0; i < columnNameList.length; i++) {
		var th = document.createElement('TH');
		th.appendChild(document.createTextNode(columnNameList[i]));
		header.appendChild(th);
	}
	
	$(table).css({
		"width" : "100%"
	});
	var tableBody = document.createElement('TBODY');
	table.appendChild(tableBody);

	for ( var i = 0; i < regulatorList.length; i++) {

		var tr = document.createElement('TR');
		tableBody.appendChild(tr);

		var td1 = document.createElement('TD');
		td1.width = '75';
		td1.appendChild(document.createTextNode(regulatorList[i].gene));
		$(td1).css({"text-align" : "center"});
		tr.appendChild(td1);

		var td2 = document.createElement('TD');
		td2.width = '400';	 
		var newCanvas = document.createElement('CANVAS');
		newCanvas.width = '400';
		var h = barHeight*2 + 6;
		newCanvas.height = h;		 
		
		var ctx = newCanvas.getContext("2d");
        var r = regulatorList[i];
       
		var barcodes = barcodeMapObj[r.gene];
	 
		for ( var j = 0; j < barcodes.length; j++) {

			var colorIndex = 255 - barcodes[j].colorIndex;
			 
			if (barcodes[j].arrayIndex == 0) 
			{
				ctx.fillStyle = 'rgb(255,' + colorIndex + ',' + colorIndex
						+ ')';
				ctx.fillRect(barcodes[j].position, 0, 1, barHeight);
			} else {
				ctx.fillStyle = 'rgb(' + colorIndex + ', ' + colorIndex
						+ ', 255)';
				ctx.fillRect(barcodes[j].position, barHeight, 1, barHeight);
			}

		}
		 
		td2.appendChild(newCanvas);
		$(td2).css({"text-align" : "center"});
		tr.appendChild(td2);
 
		var td3 = document.createElement('TD');
		td3.width = '75';
		td3.appendChild(document.createTextNode(regulatorList[i].pvalue));
		$(td3).css({"text-align" : "center"});
		tr.appendChild(td3);

		var td4 = document.createElement('TD');
		td4.width = '75';
		td4.appendChild(document.createTextNode("  "));
		$(td4).css({
			"background-color" : regulatorList[i].daColor
		});
		tr.appendChild(td4);

		var td5 = document.createElement('TD');
		td5.width = '75';
		td5.appendChild(document.createTextNode("  "));
		$(td5).css({
			"background-color" : regulatorList[i].deColor
		});
		tr.appendChild(td5);

		var td6 = document.createElement('TD');
		td6.width = '75';
		td6.appendChild(document.createTextNode(regulatorList[i].deRank));
		$(td6).css({"padding": "5px", "text-align" : "center"});
		tr.appendChild(td6);
    
	}
	myTableDiv.appendChild(table);

}