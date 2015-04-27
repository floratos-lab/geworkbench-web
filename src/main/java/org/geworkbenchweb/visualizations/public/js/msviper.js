function createBarcodeTable(myElementById, columnNames, regulators, barcodeMap, barHeight) {
 
	var columnNameList = JSON.parse(columnNames);
	var regulatorList = JSON.parse(regulators);
	var barcodeMapObj = JSON.parse(barcodeMap);  

	var myTableDiv = document.getElementById(myElementById);
	$(myTableDiv).empty();

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


function createBarcodeTable_canvas(myElementById, columnNames, regulators, barcodeMap, barHeight, exportImage) {
	 
	var columnNameList = JSON.parse(columnNames);
	var regulatorList = JSON.parse(regulators);
	var barcodeMapObj = JSON.parse(barcodeMap);  

	var myTableDiv = document.getElementById(myElementById);	 
  
	$(myTableDiv).empty();
	
	var tableWidth = 1000;
	var barcodeCellLen = 400;
	var cellWidth = Math.floor((tableWidth-barcodeCellLen)/(columnNameList.length));
	var rowHeight = barHeight*2 + 6;
	var tableHeight = rowHeight * regulatorList.length + 100;
   
	var x = document.createElement("CANVAS");
    x.width = tableWidth;	
	x.height = tableHeight;	
	
	myTableDiv.appendChild(x);
 
	var ctx = x.getContext("2d");		
	ctx.font = "14px  Georgia";
	 
	ctx.fillText(columnNameList[0], 20, 20)
	ctx.fillText(columnNameList[1], 20 + cellWidth + barcodeCellLen/2-4, 20)
	for ( var i = 2; i < columnNameList.length; i++) {
	  	    	
		    ctx.fillText(columnNameList[i], 20+cellWidth*(i-1)+barcodeCellLen+cellWidth/2, 20);
		 
	}
	 
	var firstRowPosition = 36;
	ctx.font = "12px Helvetica, sans-serif";
	for ( var i = 0; i < regulatorList.length; i++) {		 
		
		ctx.fillText(regulatorList[i].gene, 20 ,   barHeight + firstRowPosition + rowHeight*i + 3)
		     
		var barcodes = barcodeMapObj[regulatorList[i].gene];
	 
		for ( var k = 0; k < barcodes.length; k++) {			 
			 
				var colorIndex = 255 - barcodes[k].colorIndex;
				 
				if (barcodes[k].arrayIndex == 0) 
				{
					ctx.fillStyle = 'rgb(255,' + colorIndex + ',' + colorIndex
							+ ')';
					ctx.fillRect(20+cellWidth + barcodes[k].position,  firstRowPosition + rowHeight*i, 1, barHeight);
				} else {
					ctx.fillStyle = 'rgb(' + colorIndex + ', ' + colorIndex
							+ ', 255)';
					ctx.fillRect(20+cellWidth +barcodes[k].position, firstRowPosition + rowHeight*i + barHeight, 1, barHeight);
				}
		}
		
		//p-value
		ctx.fillStyle = "black";
		ctx.fillText(regulatorList[i].pvalue, 20+cellWidth*1+barcodeCellLen+cellWidth/2 , barHeight + firstRowPosition + rowHeight*i+3);
		 
		//act
		ctx.fillStyle = regulatorList[i].daColor;		 
        ctx.fillRect(20+cellWidth*2+barcodeCellLen+ Math.floor(cellWidth/3), firstRowPosition + rowHeight*i, cellWidth-2,  rowHeight-2);
		
        //exp
        ctx.fillStyle = regulatorList[i].deColor;		 
        ctx.fillRect(20+cellWidth*3+barcodeCellLen+ Math.floor(cellWidth/3), firstRowPosition + rowHeight*i, cellWidth-2,  rowHeight-2);
		
        //rank
        ctx.fillStyle = "black";
		ctx.fillText(regulatorList[i].deRank, 20+cellWidth*4+barcodeCellLen+cellWidth/2+2 , barHeight + firstRowPosition + rowHeight*i+3);
	} 
	 
	//var img = x.toDataURL();
	//window.open(img);

}

