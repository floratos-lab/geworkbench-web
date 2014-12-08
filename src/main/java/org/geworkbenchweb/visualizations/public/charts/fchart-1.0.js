/* Copyright by Freecode.net.ua, author: Volodymyr Dobryvechir (dobrivecher@yahoo.com)     */ if (!window.getComputedStyle){window.getComputedStyle = function(el, pseudo){this.el = el;this.getPropertyValue = function(prop){var re = /(\-([a-z]){1})/g;if (prop == 'float')prop = 'styleFloat';if (re.test(prop)){prop
= prop.replace(re, function (){return arguments[2].toUpperCase();});}
return el.currentStyle[prop]? el.currentStyle[prop]: null;}
return this;}}
function chart(parentref){var width = 720,widthUser=0, height = 400,heightUser=0, widthPerAxisLabelNumeric=50, heightPerAxisLabelNumeric = 50, widthPerAxisLabelString=70, heightPerAxisLabelString = 70,
heightPerGeneralChartCaption=24, widthAxisCaption = 19, heightAxisCaption = 15, minTopMargin=8, minLeftMargin=8, minRightMargin=8, minBottomMargin=8, generalChartTitlePosition="xc0,y17",generalChartTitleAlignment="middle",
heightPerLegendItem = 15, widthPerLegendItem = 115, legendColumnAmount = 1, legendImageHeight = 0, legendImageWidth = 0, legendWidthClearance = 15, legendHeightClearance = 15, legendShape=-1,legendSize=-1,legendPlace=0,legendCustomPosition=null,
isLeftAxis = 0,isRightAxis = 0x10, isBottomAxis = 0x20,isTopAxis = 0x30,isTopAxisBitwise=0x10, axisOrientationMask = 0x30,axisNumberMask = 0xf, topAxisSign = 0x20,axisMaxNumber = 0x3f,axisDirMask=0x1f,axisDirShift=5,
chartCommonOptionsDisableValuesOnPoints=1, chartCommonOptionsDisableLegend=2, chartCommonOptionsDisableWholeChartExceptTitle=4, chartCommonOptionsDisableGeneralChartHeader=8, chartCommonOptionsDisablePopUpValueIndicator=16,
chartCommonOptions = chartCommonOptionsDisableValuesOnPoints, chartPointShapeMask=255, chartBaseViewMask = 15, chartBaseViewAsLine = 0, chartBaseViewAsSinglePoints = 1, chartBaseViewAsArea = 2, chartBaseViewAsHBars
= 3, chartBaseViewAsVBars = 4, chartBaseViewAsSpline=5, chartBaseViewAsSplineArea=6, chartBaseViewAsPie = 7, chartBaseViewAsDoublePie = 8, chartBaseViewShift=8, chartCurveThicknessShift=16, chartCurveThicknessMask=65535,
popUpValueIndicatorRadiusSquare=15000, popUpValueIndicatorRadiusXWeight=2, legendPlaceRight=0,legendPlaceLeft=1, legendPlaceTop=2,legendPlaceBottom=3, legendPlaceOverlap=4, popUpAlignment_Default=0, popUpAlignment_Left=1,
popUpAlignment_Middle=2, popUpAlignment_Right=3;var chartViewValueDefault=(chartBaseViewAsLine<<chartBaseViewShift)| 1 | (0 << chartCurveThicknessShift);var curveView=new Array(),valueAxisMax = new Array(),
valueAxisMin = new Array(), valueAxisDiscrete=new Array(),valueAxisAngle=new Array(), valueYData=new Array(),valueAxisTitle = new Array(), sizeAxisLabel = new Array(), sizeAxisCaption = new Array(), theScale
= new Array(),theAxis = new Array(),axisLabelStart = new Array(), axisCaptionStart = new Array(), axisCaptionLength = new Array(),discretePositions = new Array(), extraMessages = new Array(),extraJS=new
Array(),chartPointShapeCustom=new Array(), widthLeft,widthRight,heightTop,heightBottom,generalChartCaption, limitXAxisNumber=0,dataGraphBoxLow,dataGraphBoxMedium,dataGraphBoxHigh, widthInside,heightInside,
popUpValueShowX=null,popUpValueShowY=null,popUpValueShowIndex, popUpValueShowPointPool=new Array(),popUpFormat="%l%n%X: %Y", popUpAlignment=0, legendData=null,legendDataAmount=0,legendPanelX,legendPanelY,
legendPanelWidth,legendPanelHeight,legendInfoX,legendInfoY, legendPool=new Array();var parent = typeof parentref=="object"?parentref: document.getElementById(parentref);function dvchart(){alert("width="+width);
};function calculateAxisPointsInTheMiddle(axiswidth,pointnmb){var r = new Array();var n = pointnmb + 1;var step = axiswidth / n;for(var i=0;i<pointnmb;i++)r[i]=Math.floor(step * (i+1));return r;}
function getAxisNumberInArray(names,n){var r = 0;switch(names[n]){case 'bottom': r = isBottomAxis;break;case 'left': r = isLeftAxis;break;case 'right': r = isRightAxis;break;case 'top': r = isTopAxis;break;default:
r = 0+names[n];switch(names[n-1]){case 'bottom': r |= isBottomAxis;break;case 'left': r |= isLeftAxis;break;case 'right': r |= isRightAxis;break;case 'top': r |= isTopAxis;break;}}
return r;}
function getAxisNumber(names){return getAxisNumberInArray(names,names.length-1);}
function getSquareAxisNumber(names){var n = names.length-1,o=0;var v = getAxisNumberInArray(names,n);var sn = names[n];if ((sn=="right")||(sn=="left")){n--;}
else if ((sn=="bottom")||(sn=="top")){n--;o=1;}
else {sn=names[--n];if ((sn=="bottom")||(sn=="top"))o=1;n--;}
var h=getAxisNumberInArray(names,n);if (o!=0){var r1 = h;h=v;v=r1;}
r = ((v & axisDirMask)<<axisDirShift)| (h & axisDirMask);return r;}
function setParameterC(names,value){switch(names[0]){case 'chartCommonOptions': chartCommonOptions=typeof value=="string"? parseInt(value):value;break;case 'chartPointShapeCustom': chartPointShapeCustom[names[1]]=value;break;case
'chartViewValueDefault': chartViewValueDefault=typeof value=="string"? parseInt(value):value;break;case 'curveView': if (value=="null")value=null;else if (typeof(value)=="string")value = eval(value);curveView[getSquareAxisNumber(names)]=value;break;}
}
function setParameterE(names,value){switch(names[0]){case 'extraJSAdd': extraJS.push({"id":names[1], "text":value});break;case 'extraJSClearAll': extraJS = new Array();break;case 'extraJSDelete': {var
id = names[1];var n = extraJS.length,i=0;while(i<n){if (typeof extraJS[i]=="object"&& extraJS[i].id==id){extraJS.splice(i,1);n--;}
else i++;}}
break;case 'extraMessageAdd': extraMessages.push({"id":names[1], "text":value,"position":names[2], "className":names[3],"alignment":names[4]});break;case 'extraMessageClearAll': extraMessages = new Array();
break;case 'extraMessageDelete': {var id = names[1];var n = extraMessages.length,i=0;while(i<n){if (typeof extraMessages[i]=="object"&& extraMessages[i].id==id){extraMessage.splice(i,1);n--;}
else i++;}}
break;}}
function setParameterG(names,value){switch(names[0]){case 'generalChartCaption': generalChartCaption=value;break;case 'generalChartTitlePosition': generalChartTitlePosition=value;break;case 'generalChartTitleAlignment':
generalChartTitleAlignment=value;break;}}
function setParameterH(names,value){switch(names[0]){case 'height': heightUser=typeof value=="string"?parseFloat(value):value;break;case 'heightAxisCaption': heightAxisCaption = typeof value=="string"?
parseFloat(value):value;break;case 'heightPerAxisLabelNumeric': heightPerAxisLabelNumeric=typeof value=="string"? parseFloat(value):value;break;case 'heightPerAxisLabelString': heightPerAxisLabelString=typeof
value=="string"? parseFloat(value):value;break;case 'heightPerGeneralChartCaption': heightPerGeneralChartCaption=typeof value=="string"? parseFloat(value):value;break;case 'heightPerLegendItem': heightPerLegendItem=typeof
value=="string"? parseFloat(value):value;break;}}
function setParameterL(names,value){switch(names[0]){case 'legendColumnAmount': legendColumnAmount=typeof value=="string"? parseInt(value):value;break;case 'legendData': if (value=="null")value=null;else
if (typeof(value)=="string")value = eval(value);legendData=(typeof value=="object")&& (value.length>0)?value:null;break;case 'legendHeightClearance': legendHeightClearance=typeof value=="string"? parseFloat(value):value;break;case
'legendImageHeight': legendImageHeight=typeof value=="string"? parseFloat(value):value;break;case 'legendImageWidth': legendImageWidth=typeof value=="string"? parseFloat(value):value;break;case 'legendPlace':
if (typeof value=="string"&& value!=null && value.length>0){var c=value[0];if (c>='0'&& c<='4'){legendPlace = parseInt(c);}
if (value.length>4 && value[1]=='_'){legendCustomPosition = value.substr(2);}}
break;case 'legendShape': legendShape=typeof value=="string"? parseInt(value):value;break;case 'legendSize': legendSize = typeof value=="string"? parseInt(value):value;break;case 'legendWidthClearance':
legendWidthClearance=typeof value=="string"? parseFloat(value):value;break;}}
function setParameterM(names,value){switch(names[0]){case 'minTopMargin': minTopMargin=typeof value=="string"? parseFloat(value):value;break;case 'minLeftMargin': minLeftMargin = typeof value=="string"?
parseFloat(value):value;break;case 'minBottomMargin': minBottomMargin=typeof value=="string"? parseFloat(value):value;break;case 'minRightMargin': minRightMargin=typeof value=="string"? parseFloat(value):value;break;}
}
function setParameterP(names,value){switch(names[0]){case 'popUpAlignment': popUpAlignment=value;break;case 'popUpFormat': popUpFormat=value;break;case 'popUpValueIndicatorRadiusXWeight': popUpValueIndicatorRadiusXWeight=typeof
value=="string"? parseInt(value):value;break;case 'popUpValueIndicatorRadiusSquare': popUpValueIndicatorRadiusSquare=typeof value=="string"? parseInt(value):value;break;}}
function setParameterS(names,value){switch(names[0]){case 'sizeAxisLabel': sizeAxisLabel[getAxisNumber(names)]=typeof value=="string"? parseFloat(value):value;break;case 'sizeAxisCaption': sizeAxisCaption[getAxisNumber(names)]=typeof
value=="string"? parseFloat(value):value;break;}}
function setParameterV(names,value){switch(names[0]){case 'valueAxisMax': valueAxisMax[getAxisNumber(names)]=typeof value=="string"? parseFloat(value):value;break;case 'valueAxisMin': valueAxisMin[getAxisNumber(names)]=typeof
value=="string"? parseFloat(value):value;break;case 'valueAxisDiscrete': if (value=="null")value=null;else if (typeof(value)=="string")value = eval(value);valueAxisDiscrete[getAxisNumber(names)]=value;break;case
'valueAxisAngle': valueAxisAngle[getAxisNumber(names)]=typeof value=="string"? parseFloat(value):value;break;case 'valueAxisTitle': valueAxisTitle[getAxisNumber(names)]=value;break;case 'valueYData':
if (value=="null")value=null;else if (typeof(value)=="string")value = eval(value);valueYData[getSquareAxisNumber(names)]=value;break;}}
function setParameterW(names,value){switch(names[0]){case 'width': widthUser = typeof value=="string"? parseFloat(value):value;break;case 'widthAxisCaption': widthAxisCaption = typeof value=="string"?
parseFloat(value):value;break;case 'widthPerAxisLabelNumeric': widthPerAxisLabelNumeric=typeof value=="string"? parseFloat(value):value;break;case 'widthPerAxisLabelString': widthPerAxisLabelString=typeof
value=="string"? parseFloat(value):value;break;case 'widthPerLegendItem': widthPerLegendItem=typeof value=="string"? parseFloat(value):value;break;}}
function getChartPositionX(position){if (typeof(position)!="string"|| position==null)return 0;var n = position.length,i=0;while (i<n && position[i]!="x"&& position[i]!="X")i++;if (++i>=n)return 0;var
base=0,pos=0,sn=1;var tp=position[i];if (tp>="A")i++;while (i<n){var c=position[i++];if (c=='-')sn=-1;else if (c=='+');else if (c>="0"&& c<="9")pos = pos * 10 + (c & 0xf);else if (c=="%"){pos=Math.floor(pos
* width/100);break;}
else if (c=="!"){pos=Math.floor(pos * widthInside/100);break;}
else break;}
if (sn<0)pos = -pos;if (tp=="L"|| tp=="l"){base = widthLeft;}
else if (tp=="R"|| tp=="r"){base = width - widthRight;}
else if (tp=="C"|| tp=="c"){base = Math.floor(widthLeft+widthInside/2);}
else if (tp=="M"|| tp=="m"){base =Math.floor(width/2);}
else if (pos<0){base = width;}
return base + pos;}
function getChartPositionY(position){if (typeof(position)!="string"|| position==null)return 0;var n = position.length,i=0;while (i<n && position[i]!="y"&& position[i]!="Y")i++;if (++i>=n)return 0;var
base=0,pos=0,sn=1;var tp=position[i];if (tp>="A")i++;while (i<n){var c=position[i++];if (c=='-')sn=-1;else if (c=='+');else if (c>="0"&& c<="9")pos = pos * 10 + (c & 0xf);else if (c=="%"){pos=Math.floor(pos
* height/100);break;}
else if (c=="!"){pos=Math.floor(pos * heightInside/100);break;}
else break;}
if (sn<0)pos = -pos;if (tp=="T"|| tp=="t"){base = heightTop;}
else if (tp=="B"|| tp=="b"){base = height - heightBottom;}
else if (tp=="C"|| tp=="c"){base = Math.floor(heightTop+heightInside/2);}
else if (tp=="M"|| tp=="m"){base =Math.floor(height/2);}
else if (pos<0){base = height;}
return base + pos;}
function printChartMessage(messageText,messageClass,messagePosition,messageAlignment){if (typeof(messageText)=="undefined"|| messageText==null)return;var x=getChartPositionX(messagePosition),y=getChartPositionY(messagePosition);
if (typeof(messageClass)=="undefined"|| messageClass==null)messageClass="";if (typeof messageAlignment=="undefined"|| messageAlignment==null || messageAlignment=="")messageAlignment="middle";dvchart.svg.append("svg:g").attr("class","chart-extra-message-group chart-extra-group-"+messageClass).
append("svg:text").text(messageText).attr("x",x).attr("y",y). attr("text-anchor",messageAlignment). attr("class","chart-extra-message-text "+messageClass);}
function getAxisOrientationByNumber(axisNr){switch(axisNr & axisOrientationMask){case isBottomAxis: return "bottom";case isLeftAxis: return "left";case isRightAxis: return "right";case isTopAxis: return
"top";}
return undefined;}
dvchart.setParameter=function(name,value){var names = name.split('_');var ctrlname = names[0];var ctrlchar = names[0].charAt(0);switch(ctrlchar){case 'c': setParameterC(names,value);break;case 'e': setParameterE(names,value);
break;case 'g': setParameterG(names,value);break;case 'h': setParameterH(names,value);break;case 'l': setParameterL(names,value);break;case 'm': setParameterM(names,value);break;case 'p': setParameterP(names,value);
break;case 's': setParameterS(names,value);break;case 'v': setParameterV(names,value);break;case 'w': setParameterW(names,value);break;}
return dvchart;}
dvchart.svg = d3.select(parent).append("svg").attr("class","chart-svg-root");dvchart.chartPointShapeNone=0;dvchart.chartPointShapeCircle=1;dvchart.chartPointShapeSquare=2;dvchart.chartPointShapeTriangleUp=3;dvchart.chartPointShapeRhombus=4;dvchart.chartPointShapeRing=5;dvchart.chartPointShapeTriangleDown=6;dvchart.chartPointShapeTriangleLeft=7;dvchart.chartPointShapeTriangleRight=8;dvchart.chartPointShapeRectangle=15;dvchart.chartBaseViewAsLine=chartBaseViewAsLine;dvchart.chartBaseViewAsSinglePoints=chartBaseViewAsSinglePoints;dvchart.chartBaseViewAsArea=chartBaseViewAsArea;dvchart.chartBaseViewAsHBars=chartBaseViewAsHBars;dvchart.chartBaseViewAsVBars=chartBaseViewAsVBars;dvchart.chartBaseViewAsSpline=chartBaseViewAsSpline;dvchart.chartBaseViewAsSplineArea=chartBaseViewAsSplineArea;dvchart.chartBaseViewAsPie=chartBaseViewAsPie;dvchart.chartBaseViewAsDoublePie=chartBaseViewAsDoublePie;function
printHorizontalAxis(axisNr,posX,posY,axisWidth,meshHeight){var orienta = getAxisOrientationByNumber(axisNr);var specNumber = axisNr & axisNumberMask;var specName = orienta+"-axis"+specNumber;var oldAxis
= dvchart.svg.select("."+specName);if (!oldAxis.empty()){oldAxis.remove();}
if (typeof(valueAxisDiscrete[axisNr])==="undefined"|| valueAxisDiscrete[axisNr]==null || ((chartCommonOptions & chartCommonOptionsDisableWholeChartExceptTitle)!=0)){return;}
var axisTit = valueAxisTitle[axisNr];if (!axisTit){}
else {var titx =0;}
var dataDiscrete = valueAxisDiscrete[axisNr];var ndataDiscrete = dataDiscrete.length;var therange = calculateAxisPointsInTheMiddle(axisWidth,ndataDiscrete);discretePositions[axisNr]=therange;theScale[axisNr]=
d3.scale.ordinal().domain(dataDiscrete).range(therange);theAxis[axisNr]= d3.svg.axis().tickSize(5,5,2).scale(theScale[axisNr]).orient(orienta);dvchart.svg.append("svg:g").attr("class","chart-axis "+specName).attr("transform","translate("+posX+","+posY+")").
call(theAxis[axisNr]);theAngle = valueAxisAngle[axisNr];if (typeof(theAngle)!="undefined"){dvchart.svg.select("."+specName).selectAll("g").select("text").attr("transform", "rotate("+theAngle+")").attr("text-anchor","start").attr("x","0").
attr("style","text-anchor:start");}
if (meshHeight!=0){dvchart.svg.select("."+specName).selectAll("g").attr("class","chart-mesh-item-"+orienta). append("svg:line").attr("class","chart-mesh-line mesh-"+specName). attr("x2",0).attr("y2",meshHeight);
}
var axisTitle = valueAxisTitle[axisNr],axisTitlePosStart,axisTitlePosEnd,axisTitleLength=axisCaptionLength[axisNr];if (typeof(axisTitle)!="undefined"&& axisTitle!=null && axisTitle!==""){if (orienta=="bottom"){axisTitlePosEnd
= axisCaptionStart[axisNr];axisTitlePosStart = axisTitlePosEnd + axisTitleLength;}
else {axisTitlePosEnd = axisCaptionStart[axisNr];axisTitlePosStart = -axisTitlePosEnd - axisTitleLength;}
dvchart.svg.select("."+specName).append("svg:text").text(axisTitle).attr("x",axisWidth/2).attr("y",axisTitlePosEnd). attr("text-anchor","middle").attr("class","axis-title "+specName+"-axis-title");}}
function messageDecode(mes){var nmes="",n = nmes.length,b=0,k=0;for(var i=0;i<n;i++){var c=mes[i];var j = c & 0xf;if (c>='A'&& c<='Z')j = c - 55;else if (c>='a'&& c<='z')j = c - 61;else if (c=='!')j=62;else
if (c=='%')j=63;b+=6;k |= (j<<b);if (b>=16){var d =(k & 0xffff);k>>=16;b-=16;nmes+=d;}}
return nmes;}
function messageEncode(mes){var nmes="",n = nmes.length,b=0,k=0;for(var i=0;i<n;i++){var c=mes[i];var j = c & 0xf;if (c>='A'&& c<='Z')j = c - 55;else if (c>='a'&& c<='z')j = c - 61;else if (c=='!')j=62;else
if (c=='%')j=63;b+=6;k |= (j<<b);if (b>=16){var d =(k & 0xffff);k>>=16;b-=16;nmes+=d;}}
return nmes;}
function printRoundedPanelEx(selection,zIndex,cssClass,cssStyle,sx,sy,dx,dy,bgcolor, borderColorOut,borderColorIn,borderStroke,borderOpacity,isShadow){var gg = selection.append("svg:g").attr("transform","translate("+
sx+","+sy+")");if (typeof cssClass!="undefined"&& cssClass!=null && cssClass!=""){gg.attr("class",cssClass+"-outer");}
if (typeof cssStyle!="undefined"&& cssStyle!=null && cssStyle!=""){gg.attr("style",cssStyle);}
if (typeof zIndex!="undefined"&& zIndex>0){gg.attr("zIndex",zIndex);}
gg.append("svg:rect").attr("x",0.5).attr("y",0.5).attr("width",dx). attr("height",dy).attr("rx",2).attr("ry",2).attr("fill","none"). attr("stroke",borderColorOut).attr("stroke-width",5+borderStroke).
attr("isShadow",isShadow).attr("stroke-opacity",borderOpacity). attr("transform","translate(1,1)");gg.append("svg:rect").attr("x",0.5).attr("y",0.5).attr("width",dx). attr("height",dy).attr("rx",2).attr("ry",2).attr("fill","none").
attr("stroke",borderColorOut).attr("stroke-width",3+borderStroke). attr("isShadow",isShadow).attr("stroke-opacity",borderOpacity*2). attr("transform","translate(1,1)").attr("visibility","visible");gg.append("svg:rect").attr("x",0.5).attr("y",0.5).attr("width",dx).
attr("height",dy).attr("rx",2).attr("ry",2).attr("fill","none"). attr("stroke",borderColorOut).attr("stroke-width",1+borderStroke). attr("isShadow",isShadow).attr("stroke-opacity",borderOpacity*3). attr("transform","translate(1,1)");
gg.append("svg:rect").attr("x",0.5).attr("y",0.5).attr("width",dx). attr("height",dy).attr("rx",2).attr("ry",2).attr("fill",bgcolor). attr("stroke",borderColorIn).attr("stroke-width",1). attr("isShadow",isShadow).attr("opacity","1");
return gg;}
function printBarLikeOnChart(selection,sx,sy,dx,dy,w,h,className,scolor){var gg = selection.append("svg:g").attr("transform","translate("+ sx+","+sy+")");gg.append("svg:rect").attr("x",dx).attr("y",dy).attr("width",w).
attr("height",h).attr("stroke","black").attr("transform","translate(1,1)"). attr("rx",0).attr("ry",0).attr("isShadow","true"). attr("stroke-opacity",0.05).attr("stroke-width",5).attr("fill","none");gg.append("svg:rect").attr("x",dx).attr("y",dy).attr("width",w).
attr("height",h).attr("stroke","black").attr("transform","translate(1,1)"). attr("rx",0).attr("ry",0).attr("isShadow","true"). attr("stroke-opacity",0.1).attr("stroke-width",3).attr("fill","none");gg.append("svg:rect").attr("x",dx).attr("y",dy).attr("width",w).
attr("height",h).attr("stroke","black").attr("transform","translate(1,1)"). attr("rx",0).attr("ry",0).attr("isShadow","true"). attr("stroke-opacity",0.15).attr("stroke-width",1).attr("fill","none");gg.append("svg:rect").attr("x",dx).attr("y",dy).attr("width",w).
attr("height",h).attr("stroke",scolor).attr("transform","translate(1,1)"). attr("rx",0).attr("ry",0).attr("isShadow","true"). attr("stroke-width",1).attr("class",className);return gg;}
function showBarOnChart(dicto){var w = dicto["t"];var x = dicto["x"]-1+parseInt(dicto["b"]);return printBarLikeOnChart(dataGraphBoxMedium,x,dicto["y"], 0,0,w,heightInside-dicto["y"],dicto["c"],"#FFFFFF");
}
function printRoundedPanel1(selection,zIndex,cssClass,cssStyle,sx,sy,dx,dy,bgcolor){return printRoundedPanelEx(selection,zIndex,cssClass,cssStyle,sx,sy,dx,dy,bgcolor, "#000000","#C0C0C0",0,0.2,"true");
}
function printRoundedPanel2(selection,zIndex,cssClass,cssStyle,sx,sy,dx,dy,bgcolor){return printRoundedPanelEx(selection,zIndex,cssClass,cssStyle,sx,sy,dx,dy,bgcolor, "#000000","#0000ff",0,0.2,"false");
}
function getPercentDictionaryFormattedMessage(src,chartDictionary){var n = src.length-1;for(var i=0;i<n;i++)if (src[i]=='%'){var c = src[i+1];var d=c;if (typeof chartDictionary[c]=="string"){d=chartDictionary[c];
}
var dn=d.length;src= src.substr(0,i)+d+src.substr(i+2);n+=dn-2;i+=dn-1;}
return src;}
function preshowLegendCalculations(){legendDataAmount=0;legendPool.splice(0,legendPool.length);if ((chartCommonOptions & (chartCommonOptionsDisableLegend | chartCommonOptionsDisableWholeChartExceptTitle))!=0)return;if
(legendData!=null)for(var i=0;i<legendData.length;i++){if (typeof legendData[i]=="string"&& legendData[i]!=null && legendData[i]!="")legendDataAmount++;}
if (legendDataAmount==0)return;if (legendColumnAmount<=0)legendColumnAmount=1;var legendTotalRows = Math.ceil(legendDataAmount /legendColumnAmount);var legendTotalCols = legendDataAmount>legendColumnAmount?
legendColumnAmount : legendDataAmount;var legendTotalWidth = legendTotalCols * widthPerLegendItem + legendWidthClearance;var legendTotalHeight = legendTotalRows * heightPerLegendItem + legendHeightClearance;legendPanelWidth
= legendTotalWidth - (legendWidthClearance>>1);legendPanelHeight = legendTotalHeight - (legendHeightClearance>>1);legendPanelX=-20;legendPanelY=-20;switch(legendPlace){case legendPlaceRight: legendPanelX
= width - widthRight - legendTotalWidth + (legendWidthClearance>>2);widthRight += legendTotalWidth;if (legendCustomPosition==null)legendCustomPosition="xl0,yt0";break;case legendPlaceLeft: legendPanelX
= (legendWidthClearance>>2)+widthLeft;widthLeft += legendTotalWidth;if (legendCustomPosition==null)legendCustomPosition="xl0,yt0";break;case legendPlaceTop: legendPanelY = heightTop+(legendHeightClearance>>2);
heightTop += legendTotalHeight;if (legendCustomPosition==null)legendCustomPosition="xl0,yt0";break;case legendPlaceBottom: legendPanelY = height - heightBottom- legendTotalHeight + (legendHeightClearance>>2);
heightBottom += legendTotalHeight;if (legendCustomPosition==null)legendCustomPosition="xl0,yt0";break;default: if (legendCustomPosition==null)legendCustomPosition="xl20,yt20";}}
function reShowLegend(){var selection = dvchart.svg;selection.select(".chart-legend-outer").remove();if (legendDataAmount==0){var n = legendPool.length;for(var i=0;i<n;i++)if (typeof legendData[i]=="string"&&
legendData[i]!=null && legendData[i]!=""){var dicti = legendPool[i];var mes = legendData[i];dicti["l"]=mes;}
else {var dicti = legendPool[i];dicti["l"]="";}
return;}
if (legendPanelX<0){if (legendCustomPosition==null)legendPanelX = - legendPanelX;else legendPanelX = getChartPositionX(legendCustomPosition);}
if (legendPanelY<0){if (legendCustomPosition==null)legendPanelY = - legendPanelY;else legendPanelY = getChartPositionY(legendCustomPosition);}
legendInfoX = (legendWidthClearance>>2)+1;legendInfoY = (legendHeightClearance>>2)+2;printRoundedPanel1(selection,10,"chart-legend",null,legendPanelX, legendPanelY,legendPanelWidth,legendPanelHeight,"#ffffff");
var n = legendPool.length,c=0,r=0;selection = selection.select(".chart-legend-outer");for(var i=0;i<n;i++)if (typeof legendData[i]=="string"&& legendData[i]!=null && legendData[i]!=""){var dicti = legendPool[i];
var mes = legendData[i];dicti["l"]=mes;var e = selection.append("svg:g").attr("transform","translate("+ (legendInfoX+c * widthPerLegendItem)+","+(legendInfoY+r * heightPerLegendItem )+")"). attr("class","chart-legend-cell chart-legend-cell"+i);
var textx=37,texty=12;e.append("svg:path").attr("d","M 3 1 l 10 0 l 0 12 l -10 0 l -2 -2 l 0 -7 Z"). attr("fill",dicti["f"]);e.append("svg:path").attr("d","M 23 1 l -10 0 l 0 12 l 10 0 l 2 -2 l 0 -7 Z").
attr("fill",dicti["g"]);e.append("svg:text").text(mes).attr("x",textx).attr("y",texty). attr("text-anchor","left").attr("class","chart-legend-text chart-legend-text"+i);if ((++c)>=legendColumnAmount){c=0;r++;}
}
else {var dicti = legendPool[i];dicti["l"]="";}}
function calculateSpecificBoundariesMinMax(offs,tot,isvert,minMargin,beyondAxisSize){var r = beyondAxisSize?beyondAxisSize:0;var numdef = isvert? widthPerAxisLabelNumeric:heightPerAxisLabelNumeric;var
strdef = isvert? widthPerAxisLabelString:heightPerAxisLabelString;var capdef = isvert? widthAxisCaption : heightAxisCaption;for(;offs<=tot;offs++){var dr = 0;var mi = valueAxisMin[offs],ma = valueAxisMax[offs];
if (valueAxisDiscrete[offs])dr += sizeAxisLabel[offs]? sizeAxisLabel[offs]: strdef;else {if (typeof(ma)==="undefined"|| ma==null)ma = 0;if (typeof(mi)==="undefined"|| mi==null)mi = 0;if (ma>mi){dr +=
sizeAxisLabel[offs]? sizeAxisLabel[offs]: numdef;valueAxisMax[offs]= ma;valueAxisMin[offs]= mi;}}
if (dr>0 && ((chartCommonOptions & chartCommonOptionsDisableWholeChartExceptTitle)==0)){axisLabelStart[offs]= r;var b = valueAxisTitle[offs];if (b || b==='0'){axisCaptionStart[offs]= r + dr;axisCaptionLength[offs]=
sizeAxisCaption[offs]? sizeAxisCaption[offs]: capdef;dr += axisCaptionLength[offs];}}
else dr=0;r += dr;}
if (r<minMargin)r=minMargin;return r;}
function calculateBoundaries(){heightTop=0;heightBottom=0;widthLeft=0;widthRight=0;if (typeof(generalChartCaption)=="string"&& generalChartCaption!=null && generalChartCaption!=""&& heightPerGeneralChartCaption>0
&& (chartCommonOptions & chartCommonOptionsDisableGeneralChartHeader)==0){heightTop+=heightPerGeneralChartCaption;}
preshowLegendCalculations();popUpValueShowPointPool.splice(0,popUpValueShowPointPool.length);popUpValueShowIndex=-1;widthLeft=calculateSpecificBoundariesMinMax(isLeftAxis,isLeftAxis | axisNumberMask,true,minLeftMargin,widthLeft);
widthRight=calculateSpecificBoundariesMinMax(isRightAxis,isRightAxis | axisNumberMask,true,minRightMargin,widthRight);heightTop=calculateSpecificBoundariesMinMax(isTopAxis,isTopAxis | axisNumberMask,false,minTopMargin,heightTop);
heightBottom=calculateSpecificBoundariesMinMax(isBottomAxis,isBottomAxis | axisNumberMask,false,minBottomMargin,heightBottom);}
function printVerticalAxis(axisNr,posX,posY,h,meshWidth){var axisMax=0,axisMin=0;if (typeof(valueAxisMin[axisNr])!="undefined"){axisMin = valueAxisMin[axisNr];}
if (typeof(valueAxisMax[axisNr])!="undefined"){axisMax = valueAxisMax[axisNr];}
var dif = axisMax - axisMin;var orienta = getAxisOrientationByNumber(axisNr);var specNumber = axisNr & axisNumberMask;var specName = orienta+"-axis"+specNumber;var oldAxis = dvchart.svg.select("."+specName);
if (!oldAxis.empty()){oldAxis.remove();}
if (dif<=0 || ((chartCommonOptions & chartCommonOptionsDisableWholeChartExceptTitle)!=0)){return;}
theScale[axisNr]= d3.scale.linear().range([h,0]).domain([axisMin,axisMax]);theAxis[axisNr]= d3.svg.axis().tickSize(5,5,2).scale(theScale[axisNr]).orient(orienta);dvchart.svg.append("svg:g").attr("class","chart-axis "+specName).attr("transform","translate("+posX+","+posY+")").
call(theAxis[axisNr]);if (meshWidth!=0){dvchart.svg.select("."+specName).selectAll("g").attr("class","chart-mesh-item-"+orienta). append("svg:line").attr("class","chart-mesh-line mesh-"+specName). attr("x2",meshWidth).attr("y2",0);
}
var axisTitle = valueAxisTitle[axisNr],axisTitlePosStart,axisTitlePosEnd,axisTitleLength=axisCaptionLength[axisNr];if (typeof(axisTitle)!="undefined"&& axisTitle!=null && axisTitle!==""){if (orienta=="left"){axisTitlePosEnd
= - axisCaptionStart[axisNr];axisTitlePosStart = axisTitlePosEnd - axisTitleLength;axisTitlePosAngle = 270;}
else {axisTitlePosEnd = posX + axisCaptionStart[axisNr];axisTitlePosStart = axisTitlePosEnd - axisTitleLength;axisTitlePosAngle = 90;}
var centerposx = Math.floor((axisTitlePosStart+axisTitlePosEnd)/2+0.45);var centerposy = Math.floor(h /2+0.45);var fontsizehalf = 4;var gaxisbox = dvchart.svg.select("."+specName).append("g");gaxisbox.append("svg:text").text(axisTitle).attr("x",centerposx).attr("y",centerposy+fontsizehalf).attr("transform",
"rotate("+axisTitlePosAngle+" "+centerposx+" "+centerposy+")").attr("text-anchor","middle").attr("class","axis-title "+specName+"-axis-title");}}
function showPieceLineOnChart(lineClassName,styleName,x1,y1,x2,y2,baseForm){if (baseForm==chartBaseViewAsArea){var d = "M "+x1+" "+y1+" L "+x2+" "+y2+" L "+x2+" "+heightInside+ " L "+x1+" "+heightInside+" Z";
dataGraphBoxLow.append("svg:path"). attr("class","chart-area-fill-opacity area-"+lineClassName). attr("d",d);}
dataGraphBoxLow.append("svg:line"). attr("class",lineClassName).attr("style",styleName). attr("x1",x1).attr("y1",y1).attr("x2",x2).attr("y2",y2);}
function showDataOnChartContinuous(axisX,axisY,dataarray){}
function getWidthOfSingleText(selection,message,className){var w=0,n=message.length;if (n!=0){var t = selection.append("svg:text").text(message). attr("text-anchor","start"). attr("visibility","hidden");
if (typeof className=="string"&& className!=null && className!="")t.attr("class",className);var t1=t[0][0];w=Math.ceil(t1.getEndPositionOfChar(t1.getNumberOfChars()-1).x- t1.getStartPositionOfChar(0).x);
t.remove();}
return w;}
function getSizeOfComplexText(selection,message,className){var h=15,w=0,n=message.length,i=0,r=1;var s=new Array();if (n!=0){var t = selection.append("svg:text").text(message). attr("text-anchor","start").
attr("visibility","hidden");var t1=t[0][0];while(i<n){var b = i;while (i<n && message[i]!=="\n")i++;var e=i-1;if (i<n)r++;if (e>=b){var mes = message.substring(b,e+1);var w1=getWidthOfSingleText(selection,mes,className);
s.push(mes);if (w1>w)w=w1;}
i++;}}
return {h:h,w:w,r:r,s:s};}
function showClosestValue(x,y){var dmin = popUpValueIndicatorRadiusSquare,pi=-1,dx,dy,dsrad,i, n=popUpValueShowPointPool.length;for(i=0;i<n;i++){dx=popUpValueShowPointPool[i].x-x;dy=popUpValueShowPointPool[i].y-y;dsrad=popUpValueIndicatorRadiusXWeight*dx*dx+dy*dy;if
(dsrad<dmin){dmin=dsrad;pi=i;}}
if (pi==popUpValueShowIndex)return;popUpValueShowIndex=pi;dataGraphBoxHigh.select("g.chart-popup-show-value-outer").remove();if (pi<0 || (chartCommonOptions & chartCommonOptionsDisablePopUpValueIndicator)!=0){popUpValueShowX=null;return;}
dx=popUpValueShowPointPool[pi].x;dy=popUpValueShowPointPool[pi].y;var dicti = popUpValueShowPointPool[pi].d;var xx=popUpValueShowPointPool[pi].xv;var axisX = parseInt(dicti["j"]);var valx = valueAxisDiscrete[axisX
| isBottomAxis];dicti["X"]=valx[xx]+"";dicti["Y"]=popUpValueShowPointPool[pi].yv+"";dicti["x"]=dx+"";dicti["y"]=dy+"";var message = getPercentDictionaryFormattedMessage(popUpFormat,dicti);var selection
= dataGraphBoxHigh.append("svg:g").attr("class", "chart-popup-show-value-outer").attr("transform","translate("+ +dx+","+dy+")");var popUpShowValueBoxClearanceX=3,popUpShowValueBoxClearanceY=1;var z =
getSizeOfComplexText(selection,message,"chart-popup-show-value-inner");var outh = (z.h * z.r)+ (2 * popUpShowValueBoxClearanceY);var outw = z.w + (2 * popUpShowValueBoxClearanceX);dx -= outw / 2;dy -=
outh+4;if (popUpValueShowX==null){popUpValueShowX = dx;popUpValueShowY = dy;}
selection.attr("transform","translate("+ +popUpValueShowX+","+popUpValueShowY+")");var f=popUpValueShowPointPool[pi].f;if (typeof f!="string"|| f==null || f=="")f="#0000ff";var selecti = printRoundedPanelEx(selection,50,"chart-popup-show-value-inner",null,0,0,outw,outh,"#ffffff",
"#000000",f,0,0.2,"false");n = z.r;var b = popUpAlignment;for(i=0;i<n;i++){var d = b & 3;b>>=2;var x = popUpShowValueBoxClearanceX;var y = popUpShowValueBoxClearanceY+z.h * i+z.h-2;var ta="start";if (d==popUpAlignment_Middle
|| d==popUpAlignment_Default){ta="middle";x+=z.w/2;}
else if (d==popUpAlignment_Right){ta="end";x+=z.w;}
selecti.append("text").text(z.s[i]).attr("x",x). attr("y",y).attr("text-anchor",ta);}
if (popUpValueShowX != dx || popUpValueShowY != dy){popUpValueShowX = dx;popUpValueShowY = dy;selection.transition().attr("transform","translate("+ +popUpValueShowX+","+popUpValueShowY+")");}}
function mediumDataBoxMouseMove(d,i){var x = d3.mouse(this)[0]- widthLeft;var y = d3.mouse(this)[1]- heightTop;showClosestValue(x,y);}
function mediumDataBoxMouseOut(d,i){var x=-6000;var y=-6000;showClosestValue(x,y);}
function mediumDataBoxMouseOver(d,i){}
function getComputedFillColor(className,defColor){var d=dataGraphBoxMedium.append("svg:line").attr("class",className). attr("visibility","hidden");var e=window.getComputedStyle(d[0][0], null);var f=e==null?null:e.getPropertyValue("fill");
d.remove();if (typeof f!="string"|| f==null || f=="")return defColor;return f;}
function showDataOnChart(axisX,axisY,dataarray,curveViewInfo){var valx = valueAxisDiscrete[axisX | isBottomAxis];if (!valx){showDataOnChartContinuous(axisX,axisY,dataarray,curveViewInfo);return;}
var diramount = dataarray.length;var discPos = discretePositions[axisX | isBottomAxis];if (!discPos)return;var LeftOrRight = ((axisY & isRightAxis)!=0?"right":"left");var TopOrBottom = ((axisX & isTopAxisBitwise)!=0?"top":"bottom");
var HorizontalAxis = (axisX & axisNumberMask);var VerticalAxis = (axisY & axisNumberMask);var prevval = new Array();var prevpos = new Array();var chartBaseName = "chart-"+TopOrBottom+HorizontalAxis+"-"+
LeftOrRight+VerticalAxis+"-line";var xamount = limitXAxisNumber;if (xamount==0 || xamount>valx.length)xamount = valx.length;var valYMax=0,valYMin=0;if (typeof(valueAxisMin[axisY])!="undefined"){valYMin
= valueAxisMin[axisY];}
if (typeof(valueAxisMax[axisY])!="undefined"){valYMax = valueAxisMax[axisY];}
var difY = valYMax - valYMin;if (difY<=0)return;var chartCurveThicknessValue=new Array(),chartBaseValue=new Array(), chartPointMark=new Array(),chartLineName=new Array(), styleForLine=new Array(),pathForMark=new
Array(), styleForMark=new Array(),isLineJoinable=new Array();pathDictionary=new Array(),barOffset=new Array(), barTotalCount=0,barTotalWidth=0;for(var i=0;i<diramount;i++){prevval[i]="null";prevpos[i]=0;barOffset[i]=0;var
chartViewValue = chartViewValueDefault;if (typeof curveViewInfo=="object"&& typeof curveViewInfo[i]!="undefined")chartViewValue = curveViewInfo[i];chartBaseValue[i]= (chartViewValue >> chartBaseViewShift)&
chartBaseViewMask;chartCurveThicknessValue[i]= ((chartViewValue >> chartCurveThicknessShift)& chartCurveThicknessMask)+1;chartPointMark[i]= chartViewValue & chartPointShapeMask;chartLineName[i]= chartBaseName+i;styleForLine[i]="stroke-width:"+chartCurveThicknessValue[i];
styleForMark[i]="stroke-width:0";var markSize = (chartPointMark[i]==1)?(3 + Math.floor(chartCurveThicknessValue[i]*0.6)): (5 + Math.floor(chartCurveThicknessValue[i]*0.8));var halfMarkSize = (markSize>>1)+(markSize
& 1);var markSizeEven = halfMarkSize<<1;pathForMark[i]="";var c = chartBaseValue[i];var dicti = new Array();dicti["n"]="\n";dicti["m"]=markSize+"";dicti["h"]=halfMarkSize+"";dicti["w"]=markSizeEven+"";
dicti["t"]=chartCurveThicknessValue[i]+"";dicti["b"]=0;dicti["d"]=chartLineName[i];dicti["i"]=i+"";dicti["j"]=axisX+"";dicti["k"]=axisY+"";dicti["f"]=getComputedFillColor(chartLineName[i]+" "+chartLineName[i]+"-asc","blue");
dicti["g"]=getComputedFillColor(chartLineName[i]+" "+chartLineName[i]+"-desc","blue");pathDictionary[i]= dicti;legendPool.push(dicti);isLineJoinable[i]= c==chartBaseViewAsLine || c==chartBaseViewAsArea
|| c==chartBaseViewAsSpline || c==chartBaseViewAsSplineArea;if (isLineJoinable[i]|| c==chartBaseViewAsSinglePoints){if (chartPointMark[i]<=15){switch(chartPointMark[i]){case 1: pathForMark[i]="m 0 -"+markSize+" a"+markSize+
","+markSize+" 0 1,0 1,0";break;case 2: pathForMark[i]="m 0 -"+halfMarkSize+" l "+halfMarkSize+ " 0 l 0 "+markSizeEven+" l -"+markSizeEven+ " 0 l 0 -"+markSizeEven+" Z";break;case 3: pathForMark[i]="m 0 -"+halfMarkSize+" l "+halfMarkSize+
" "+markSizeEven+" l -"+markSizeEven+" 0 Z";break;case 4: pathForMark[i]="m 0 -"+halfMarkSize+" l "+halfMarkSize+ " "+halfMarkSize+" l -"+halfMarkSize+" "+ halfMarkSize+" l -"+halfMarkSize+" -"+halfMarkSize+" Z";
break;case 5: var holeMarkSize = Math.floor(markSize * 0.6);pathForMark[i]="m 0 -"+markSize+" a"+markSize+ ","+markSize+" 0 1,0 1,0$$$m 0 -"+holeMarkSize+" a"+ holeMarkSize+ ","+holeMarkSize+" 0 1,0 1,0";
break;case 6: pathForMark[i]="m 0 "+halfMarkSize+" l -"+halfMarkSize+ " -"+markSizeEven+" l "+markSizeEven+" 0 Z";break;case 7: pathForMark[i]="m -"+halfMarkSize+" 0 l "+markSizeEven+ " "+halfMarkSize+" l 0 -"+markSizeEven+" Z";
break;case 8: pathForMark[i]="m "+halfMarkSize+" 0 l -"+markSizeEven+ " -"+halfMarkSize+" l 0 "+markSizeEven+" Z";break;case 15: var halfMarkSizeHor=Math.floor(halfMarkSize*0.8);var markSizeEvenHor =
halfMarkSizeHor<<1;pathForMark[i]="m 0 -"+halfMarkSize+" l "+halfMarkSizeHor+ " 0 l 0 "+markSizeEven+" l -"+markSizeEvenHor+ " 0 l 0 -"+markSizeEven+" Z";break;}}
else if (typeof chartPointShapeCustom[chartPointMark[i]]!="undefined"){pathForMark[i]=chartPointShapeCustom[chartPointMark[i]];}}
else if (c==chartBaseViewAsVBars){pathForMark[i]= showBarOnChart;if (chartCurveThicknessValue[i]<=1)chartCurveThicknessValue[i]=9;dicti["t"]=chartCurveThicknessValue[i]+"";barTotalCount++;barTotalWidth+=chartCurveThicknessValue[i];
barOffset[i]=1;}
else if (c==chartBaseViewAsPie){}}
if (barTotalCount>0){var barTotalStart=-((barTotalWidth+(barTotalCount-1)*4)>>1)-1;for(var i=0;i<diramount;i++)if (barOffset[i]!=0){var w = chartCurveThicknessValue[i];barOffset[i]= barTotalStart;barTotalStart
+= w+4;dicti = pathDictionary[i];dicti["b"]=barOffset[i]+"";barOffset[i]+= (w>>1);}}
for(var x=0;x<xamount;x++){var xpos = discPos[x];var xprevpos = discPos[x==0?x:x-1];for(var i=0;i<diramount;i++){var yval = dataarray[i][x];var ypos = -1;if (yval==null)yval="null";var yprevval = prevval[i];
var yprevpos = prevpos[i];var lineClassName1 = "chart-data-line "+chartLineName[i]+" "+chartLineName[i];var lineClassName2 = " "+chartLineName[i]+"-id"+x;if (yval!="null"){ypos= Math.floor(heightInside*(yval
- valYMin)/difY + 0.5);var yposr = heightInside-ypos;if (yval<valYMin){yval="bottom";switch(yprevval){case "bottom":case "null": break;case "top": var direc = "-desc";var xx1 =Math.floor(xpos + (heightInside-ypos)*(xprevpos-xpos)/(yprevpos-ypos)+0.5);
var xx2 =Math.floor(xpos - ypos*(xprevpos-xpos)/(yprevpos-ypos)+0.5);var lineClassName = lineClassName1+direc+lineClassName2;if (isLineJoinable[i]){showPieceLineOnChart(lineClassName,styleForLine[i],
xx1,0,xx2,heightInside,chartBaseValue[i]);}
break;default: var direc = "-desc";var lineClassName = lineClassName1+direc+lineClassName2;var xx2 =Math.floor(xpos - ypos*(xprevpos-xpos)/(yprevpos-ypos)+0.5);if (isLineJoinable[i]){showPieceLineOnChart(lineClassName,styleForLine[i],
xprevpos,heightInside-yprevpos,xx2,heightInside,chartBaseValue[i]);}}}
else if (yval>valYMax){yval="top";switch(yprevval){case "top":case "null": break;case "bottom": var direc = "-asc";var xx2 =Math.floor(xpos + (heightInside-ypos)*(xprevpos-xpos)/(yprevpos-ypos)+0.5);var
lineClassName = lineClassName1+direc+lineClassName2;var xx1 =Math.floor(xpos - ypos*(xprevpos-xpos)/(yprevpos-ypos)+0.5);if (isLineJoinable[i]){showPieceLineOnChart(lineClassName,styleForLine[i], xx1,heightInside,xx2,0,chartBaseValue[i]);
}
break;default: var direc = "-asc";var xx2 =Math.floor(xpos + (heightInside-ypos)*(xprevpos-xpos)/(yprevpos-ypos)+0.5);var lineClassName = lineClassName1+direc+lineClassName2;if (isLineJoinable[i]){showPieceLineOnChart(lineClassName,styleForLine[i],
xprevpos,heightInside-yprevpos,xx2,0,chartBaseValue[i]);}}}
else {var direc = "-asc";switch(yprevval){case "top": {direc = "-desc";var xx1 =Math.floor(xpos + (heightInside-ypos)*(xprevpos-xpos)/(yprevpos-ypos)+0.5);var lineClassName = lineClassName1+direc+lineClassName2;if
(isLineJoinable[i]){showPieceLineOnChart(lineClassName,styleForLine[i], xx1,0,xpos,heightInside-ypos,chartBaseValue[i]);}}
break;case "bottom": {var xx1 =Math.floor(xpos - ypos*(xprevpos-xpos)/(yprevpos-ypos)+0.5);var lineClassName = lineClassName1+direc+lineClassName2;if (isLineJoinable[i]){showPieceLineOnChart(lineClassName,styleForLine[i],
xx1,heightInside,xpos,heightInside-ypos,chartBaseValue[i]);}}
break;case "null": {}
break;default: {var direc = yval>=yprevval?"-asc":"-desc";var lineClassName = lineClassName1+direc+lineClassName2;if (isLineJoinable[i]){showPieceLineOnChart(lineClassName,styleForLine[i], xprevpos,heightInside-yprevpos,xpos,heightInside-ypos,chartBaseValue[i]);
}}}
var pathd = pathForMark[i];var dicti = pathDictionary[i];dicti["x"]=xpos+"";dicti["y"]=(heightInside-ypos)+"";var className = "chart-data-mark "+chartLineName[i]+" "+chartLineName[i]+direc+" "+chartLineName[i]+"-id"+x;dicti["c"]=className;if
(typeof pathd=="string"&& pathd!=null && pathd!=""){var pathdprefix="M "+xpos+" "+(heightInside-ypos)+" ";var npathdtriple=pathd.indexOf("$$$"),npathdwhite="";if (npathdtriple>=0){npathdwhite=getPercentDictionaryFormattedMessage(pathdprefix+pathd.substring(npathdtriple+3),dicti);
pathd = pathd.substring(0,npathdtriple);}
pathd=getPercentDictionaryFormattedMessage(pathdprefix+pathd,dicti);dataGraphBoxMedium.append("svg:path").attr("d",pathd). attr("class",className). attr("style",styleForMark[i]);if (npathdwhite!=""){dataGraphBoxMedium.append("svg:path").attr("d",npathdwhite).
attr("class","chart-data-white-mark "+chartLineName[i]+"-white "+chartLineName[i]+direc+"-white "+chartLineName[i]+"-mark-white-id"+x);}}
else if (typeof pathd=="function"){pathd(dicti);}
var yy=heightInside-ypos,xx=xpos+barOffset[i];var f=getComputedFillColor(className,"blue");popUpValueShowPointPool.push({x:xx,y:yy,d:dicti,yv:yval,xv:x,f:f});if ((chartCommonOptions & chartCommonOptionsDisableValuesOnPoints)==0){if
(yy<12){yy+=12;}
else {yy-=chartBaseValue[i]==chartBaseViewAsVBars?2:2+parseInt(pathDictionary[i]["h"]);}
dataGraphBoxMedium.append("svg:text"). attr("x",xx).attr("y",yy). text(yval+"").attr("text-anchor","middle"). attr("class","chart-data-values");}}}
prevval[i]=yval;prevpos[i]=ypos;}}}
dvchart.render = function(){var a = getComputedStyle(parent,null);if (widthUser>2)width = widthUser;else {width=700;if (a!=null && a.getPropertyValue("width")){var b = a.getPropertyValue("width");var
c = b.lastIndexOf("px");if (c>0){var k =Math.floor(parseFloat(b.substring(0,c)));if (k>0)width = k;}}}
if (heightUser>2)height = heightUser;else {height=500;if (a!=null && a.getPropertyValue("height")){var b = a.getPropertyValue("height");var c = b.lastIndexOf("px");if (c>0){var k =Math.floor(parseFloat(b.substring(0,c)));
if (k>0)height = k;}}}
calculateBoundaries();widthInside = width-widthLeft-widthRight;heightInside = height - heightTop - heightBottom;if (widthInside<0)widthInside = 0;if (heightInside<0)heightInside = 0;dvchart.svg.attr("width",width).attr("height",height).attr("class","chart-whole-area");
dvchart.svg.select(".chart-data-rectangle").remove();if ((chartCommonOptions & chartCommonOptionsDisableWholeChartExceptTitle)==0){dvchart.svg.append("svg:rect").attr("width",widthInside).attr("height",heightInside).
attr("x",widthLeft).attr("y",heightTop).attr("class","chart-data-rectangle");}
printHorizontalAxis(isBottomAxis,widthLeft,height-heightBottom,widthInside,-heightInside);printHorizontalAxis(isTopAxis,widthLeft,heightTop,widthInside,heightInside);printVerticalAxis(isLeftAxis,widthLeft,heightTop,heightInside,widthInside);
printVerticalAxis(isRightAxis,widthLeft+widthInside,heightTop,heightInside,-widthInside);dvchart.svg.select(".chart-data-box-low").remove();dataGraphBoxLow=dvchart.svg.append("svg:g").attr("class","chart-data-box-low").attr("transform","translate("+widthLeft+","+heightTop+")");
dvchart.svg.select(".chart-data-box-medium").remove();dataGraphBoxMedium=dvchart.svg.append("svg:g").attr("class","chart-data-box-medium"). attr("transform","translate("+widthLeft+","+heightTop+")");dvchart.svg.select(".chart-data-box-high").remove();
dataGraphBoxHigh=dvchart.svg.append("svg:g").attr("class","chart-data-box-high").attr("transform","translate("+widthLeft+","+heightTop+")");if ((chartCommonOptions & chartCommonOptionsDisableWholeChartExceptTitle)==0)for(var
ydata in valueYData)if (typeof(valueYData[ydata])=="object"&& valueYData[ydata]!=null)showDataOnChart(ydata & axisDirMask,ydata >>axisDirShift, valueYData[ydata],curveView[ydata]);dvchart.svg.select("g.chart-extra-message-group").remove();
if (typeof(generalChartCaption)=="string"&& generalChartCaption!=null && generalChartCaption!=""&& (chartCommonOptions & chartCommonOptionsDisableGeneralChartHeader)==0){printChartMessage(generalChartCaption,"general-chart-title-text",
generalChartTitlePosition,generalChartTitleAlignment);}
reShowLegend();var n = extraMessages.length;for(var i=0;i<n;i++){var o = extraMessages[i];printChartMessage(o.text,o.className,o.position,o.alignment);if (o.id == null || o.id.length==0 || o.id[0]=='#'){extraMessages.splice(i,1);
n--;i--;}}
n = extraJS.length;for(var i=0;i<n;i++){var o = extraJS[i];var t=o.text;var f=eval(t);if (typeof f == "function")f();if (o.id == null || o.id.length==0 || o.id[0]=='#'){extraJS.splice(i,1);n--;i--;}}
dvchart.svg.select(".chart-data-event-rectangle").remove();dvchart.svg.append("svg:rect").attr("width",widthInside).attr("height",heightInside). attr("x",widthLeft).attr("y",heightTop).attr("class","chart-data-event-rectangle").
attr("stroke","white").attr("fill","white").attr("zIndex","100"). attr("opacity","0.05"). on("mousemove",mediumDataBoxMouseMove). on("mouseout",mediumDataBoxMouseOut). on("mouseover",mediumDataBoxMouseOver);
};dvchart.getCurveView=function(baseView,thickness,shape){return ((baseView & 255)<<chartBaseViewShift)| (thickness<<chartCurveThicknessShift)| shape;};return dvchart;};
