window.org_geworkbenchweb_visualizations_BarcodeTable = function () {
	this.onStateChange = function () {
		createBarcodeTable_canvas(this.getElement(), this.getState().columnNames, this.getState().regulators, this.getState().barcodeMap, this.getState().barHeight)
	}
}

function createBarcodeTable_canvas(myTableDiv, columnNameList, regulatorList, barcodeMapObj, barHeight) {
	myTableDiv.textContent = ""

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

