var $molecule_viewer = {}; /* module namespace */

$molecule_viewer.create = function(id, pdb_content, representation) {
	var div = document.getElementById(id);
	
	$(div).empty();
	
	var c = document.createElement('CANVAS');
	c.id = 'display3d';
	var w = $(div).parents('div.v-verticallayout').width();
	var h = $(div).parents('div.v-verticallayout').height();

	div.appendChild(c);

	var pdbStructure = ChemDoodle.readPDB(pdb_content);
	this.display3d = new ChemDoodle.TransformCanvas3D('display3d', w, h);
	this.display3d.specs.set3DRepresentation(representation);
	this.display3d.specs.macro_displayAtoms = true;
	this.display3d.specs.macro_displayBonds = true;
	this.display3d.loadMolecule(pdbStructure);
};

$molecule_viewer.set3DRepresentation = function(representation) {
	this.display3d.specs.set3DRepresentation(representation);
	this.display3d.repaint();
};

$molecule_viewer.setDisplayAtoms = function(displayAtoms) {
	this.display3d.specs.macro_displayAtoms = displayAtoms;
	this.display3d.repaint();
};

$molecule_viewer.setDisplayBonds = function(displayBonds) {
	this.display3d.specs.macro_displayBonds = displayBonds;
	this.display3d.repaint();
};

$molecule_viewer.setDisplayRibbon = function(displayRibbon) {
	this.display3d.specs.proteins_displayRibbon = displayRibbon;
	this.display3d.repaint();
};
