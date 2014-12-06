/*function showWizzard() {

	updateWizzardContentScrollControl();
	
	Richfaces.showModalPanel('wizzardModal', centeredBoxSize());
};

function updateWizzardContentScrollControl() {
	
	var wc  = jQuery(".wizzardContent");
	var wsc = jQuery(".wizzardStepContent");
	
	var heightOffset = 250;
	var widthOffset = 60;

	var boxSize = centeredBoxSize();
	var w = boxSize.width - widthOffset;
	var h = boxSize.height - heightOffset;	 	
	 
	wc.css('width', boxSize.width);
	wsc.css('width', w);
    wsc.css('height', h);
   
}*/

//Devuelve las medidas para colocar un objeto centrado en la vista actual del browser
function centeredBoxSize() {

    e = document.documentElement,
    g = document.getElementsByTagName('body')[0],
    w = window.innerWidth || e.clientWidth || g.clientWidth,
    h = window.innerHeight|| e.clientHeight|| g.clientHeight;

	x=0;
	y=0;
	
	if (w > 110){
		x=25;
		w=w-2*x;
	}
	if (h > 110){
		y=25;
		h=h-2*y;
	}
	
	return {left:x,top:y,width:w,height:h};
}


function dropdownToggle(elemId){
	var query = "#"+elemId;
	var e = jQuery(query);
	var display = e.css('display');

	if (display == "none") {
		e.css('display','block');
	}
	else {
		e.css('display','none');
	}

}

function isDropdownShowing(elemId){
	var query = "#"+elemId;
	var e = jQuery(query);
	var display = e.css('display');

	if (display == "none") {
		return false;
	}
	else {
		return true;
	}

}


/*
 * WIZZARD / CALENDAR / HOURS: HELP FUNCTIONS
 */
function toggleCell(cell, row, col) {
	
	var c = jQuery(cell).parent(); //El td
	if (c.hasClass("wizzardCalendarCellSelected")) {
		c.removeClass("wizzardCalendarCellSelected");
	}
	else {
		c.addClass('wizzardCalendarCellSelected');
	}
}
function toggleRow(cell, row) {

	var c = jQuery(cell).parent(); //El td

	if (c.hasClass("wizzardCalendarRowSelected")) {
		c.removeClass("wizzardCalendarRowSelected");
		c.siblings().removeClass("wizzardCalendarCellSelected");
	}
	else {
		c.addClass('wizzardCalendarRowSelected');
		c.siblings().addClass("wizzardCalendarCellSelected");
	}
}
function toggleCol(cell, col) {

	var tdIndex = col+2;
	
	//cell es el div y esta anidado así: table > thead > tr > th > div > div
	var c = jQuery(cell).parent().parent(); // El th
	var table = c.parent().parent().parent(); // El table 
	var cells = table.find("tr td:nth-child("+tdIndex+")"); //Las (td) celdas de la columna (th) seleccionada
	
	if (c.hasClass("wizzardCalendarColSelected")) {
		c.removeClass("wizzardCalendarColSelected");
		cells.removeClass("wizzardCalendarCellSelected");
	}
	else {
		c.addClass('wizzardCalendarColSelected');
		cells.addClass("wizzardCalendarCellSelected");
	}
}














//matrix el la matriz de horarios (table element), 
//col es el indice zero-based de las columnas de dias: 0..6
// rowFrom <= rowTo son el rango de filas a deselecionar dentro de la columna col
function toggleColRange(matrix, col, rowFrom, rowTo, addOrRemove){

	//Descuento el primer td que muestra las horas y sumo otro pues nth-child es 1-based
	var tdIndex = col+2;
	
	var cells = matrix.find("tr td:nth-child("+tdIndex+")"); //Las celdas (td) de la columna (th) numero col
	
	cells.each(function() {
		var i = cells.index(jQuery(this));
		if (i >= rowFrom && i <= rowTo) {
			jQuery(this).toggleClass("wizzardCalendarCellSelected", addOrRemove);
		}
	});
}

//matrix el la matriz de horarios (table element), 
//row es el indice zero-based de las filas de horarios: 0..n
//colFrom <= colTo son el rango de columnas a deselecionar dentro de la fila row
function toggleRowRange(matrix, row, colFrom, colTo, addOrRemove){

	//Sumo uno pues nth-child es 1-based
	var trIndex = row+1;
	
	var cells = matrix.find("tr:nth-child("+trIndex+") td"); //Las celdas (td) de la fila (tr) numero row
	
	cells.each(function() {
		var i = cells.index(jQuery(this));
		if (i >= colFrom+1 && i <= colTo+1) {
			jQuery(this).toggleClass("wizzardCalendarCellSelected", addOrRemove);
		}
	});
}

function updateSelectedCellsStyle(matrix, rowBegin, colBegin, rowEnd, colEnd, row, col) {

	
	matrix.find("td").toggleClass("wizzardCalendarCellSelected", false);	

	var rowTop 		= Math.min(rowBegin,row);
	var rowBottom 	= Math.max(rowBegin,row);
	var colLeft 	= Math.min(colBegin,col)+1; //El problema de la columna extra que muestra las horas
	var colRight 	= Math.max(colBegin,col)+1; //El problema de la columna extra que muestra las horas

	
	//matrix.find("tr:gt("+(rowTop-1)+"):lt("+(rowBottom-rowTop+1)+") td").toggleClass("wizzardCalendarCellSelected", true);
	matrix.find("tr").slice(rowTop, rowBottom+1).each(function(){
		jQuery(this).find("td").slice(colLeft, colRight+1).toggleClass("wizzardCalendarCellSelected", true);
	});
	
	
	
	//Crece o decrece en columnas 
	
	//decrece
	/*
	if ((colBegin <= col && col < colEnd)
			||
		(colEnd < col && col <= colBegin)) {
		
		if (rowBegin <= rowEnd) {
			toggleColRange(matrix, colEnd, rowBegin, rowEnd, false); //despinto rango de celdas de una columna
		} 
		else {
			toggleColRange(matrix, colEnd, rowEnd, rowBegin, false); //despinto rango de celdas de una columna
		}
	}

	//crece
	if ((colBegin <= colEnd && colEnd < col)
			||
		(col < colEnd && colEnd <= colBegin)) {
		
		if (rowBegin <= rowEnd) {
			toggleColRange(matrix, col, rowBegin, rowEnd, true); //pinto rango de celdas de una columna
		} 
		else {
			toggleColRange(matrix, col, rowEnd, rowBegin, true); //pinto rango de celdas de una columna
		}
	}

	
	
	//Crece o decrece en filas	
	
	//decrece
	if ((rowBegin <= row && row < rowEnd)
			||
		(rowEnd < row && row <= rowBegin)) {
		
		if (colBegin <= colEnd) {
			toggleRowRange(matrix, rowEnd, colBegin, colEnd, false); //despinto rango de celdas de una fila
		} 
		else {
			toggleColRange(matrix, rowEnd, colEnd, colBegin, false); //despinto rango de celdas de una fila
		}
	}

	
	//crece
	if ((rowBegin <= rowEnd && rowEnd < row)
			||
		(row < rowEnd && rowEnd <= rowBegin)) {
		
		if (colBegin <= colEnd) {
			toggleColRange(matrix, row, colBegin, colEnd, true); //pinto rango de celdas de una fila
		} 
		else {
			toggleColRange(matrix, row, colEnd, colBegin, true); //pinto rango de celdas de una fila
		}
	}*/
	
}

var selecting = false;
var colBegin;
var rowBegin;
var colEnd;
var rowEnd;
var matrix;

function cellMouseDown(cell, row, col) {

	rowBegin = row;
	colBegin = col;
	
	rowEnd = rowBegin;
	colEnd = colBegin;

	//cell es el div y esta anidado así: table > tbody > tr > td > div
	matrix = jQuery(cell).parent().parent().parent(); // El tbody

	jQuery(cell).parent().toggleClass('wizzardCalendarCellSelected', true);

	selecting = true;
}


function cellMouseOver(cell, row, col) {

	if (selecting == true) {
		
		
		updateSelectedCellsStyle(matrix, rowBegin, colBegin, rowEnd, colEnd, row, col);

		rowEnd = row;
		colEnd = col;
	}
}

function cellMouseUp(cell, row, col) {

	if (selecting == true) {
		
		var cupos = prompt("Cantidad de puestos de atención para los horarios seleccionados", "0");

		var rowTop 		= Math.min(rowBegin,row);
		var rowBottom 	= Math.max(rowBegin,row);
		var colLeft 	= Math.min(colBegin,col);
		var colRight 	= Math.max(colBegin,col);
		
		changeCuposOnServer(rowTop, rowBottom, colLeft, colRight, cupos);
		
		clearAllCell(matrix);
	}
}

function clearAllCell(matrix) {
	
	matrix.find("td").toggleClass("wizzardCalendarCellSelected", false);	

	selecting = false;
}

function matrixMouseOut() {
	//if (selecting == true) {
	//	selecting = false;
	//}
}

function buttonDisabled(elem,textElem, text) {
	
	elem.disabled = true;
	jQuery(elem).addClass("disabled");
	
	if (jQuery(textElem).attr('value')) {
		textElem.value = text;
	}
	else {
		textElem.nodeValue = text;
	}
}

function buttonEnabled(elem, textElem, text) {
	
	elem.disabled = false;
	jQuery(elem).removeClass("disabled");

	if (jQuery(textElem).attr('value')) {
		textElem.value = text;
	}
	else {
		textElem.nodeValue = text;
	}
}

function highlightRecursoOn(id){
	jQuery("#agendaSelectionForm-recurso-li-"+id).addClass('same-agenda-options');
}

function highlightRecursoOff(id){
	jQuery("#agendaSelectionForm-recurso-li-"+id).removeClass('same-agenda-options');
}


jQuery(document).ready(function () {

	/* Reseto estilos por defecto de Richfaces para que no colisionen con Bootstrap*/
	jQuery(".rich-mpnl-body").removeClass("rich-mpnl-body");
});



