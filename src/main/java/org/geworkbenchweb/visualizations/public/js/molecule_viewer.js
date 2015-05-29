var $molecule_viewer = {}; /* module namespace */

$molecule_viewer.create = function(id, pdb_content, representation) {
	var div = document.getElementById(id);
	
	$(div).empty();
	
	var c = document.createElement('CANVAS');
	c.id = 'display3d';
	var w = $(div).parents('div.v-verticallayout').width();
	var h = $(div).parents('div.v-verticallayout').height();

	div.appendChild(c);

	this.display3d = new ChemDoodle.TransformCanvas3D('display3d', w, h);
	if(this.display3d['gl']==null) {
		var x = document.createElement('div');
		x.innerHTML = 'Your browser is not fully supported for this molecule viewer. Please try other browsers to enjoy better visualization.';
		div.appendChild(x);
		
		this.display3d = new ChemDoodle.TransformCanvas('display3d', w, h-50, true);
		this.display3d.specs.atoms_circles_2D = true;
		this.display3d.specs.atoms_useJMOLColors = true;
		this.display3d.specs.bonds_useJMOLColors = true;
		this.display3d.specs.bonds_width_2D = 3;
		this.display3d.specs.bonds_clearOverlaps_2D = true;
		var pdbStructure = ChemDoodle.readPDB(pdb_content, 10);
		this.display3d.loadMolecule(pdbStructure);
		return;
	}

	this.display3d.specs.set3DRepresentation(representation);
	this.display3d.specs.macro_displayAtoms = true;
	this.display3d.specs.macro_displayBonds = true;
	var pdbStructure = ChemDoodle.readPDB(pdb_content);
	this.display3d.loadMolecule(pdbStructure);
};

$molecule_viewer.colorType = 'amino';

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

$molecule_viewer.setDisplayBackbone = function(displayBackbone) {
	this.display3d.specs.proteins_displayBackbone = displayBackbone;
	this.display3d.repaint();
};

$molecule_viewer.setDisplayPipe = function(displayPipe) {
	this.display3d.specs.proteins_displayPipePlank = displayPipe;
	this.display3d.repaint();
};

$molecule_viewer.setCartoonize = function(cartoonize) {
	this.display3d.specs.proteins_ribbonCartoonize = cartoonize;
	this.display3d.repaint();
};

$molecule_viewer.setColorByChain = function(colorByChain) {
	this.display3d.specs.macro_colorByChain = colorByChain;
	this.display3d.repaint();
};

$molecule_viewer.setColorByResidue = function(colorByResidue) {
	if(colorByResidue) {
		this.display3d.specs.proteins_residueColor = $molecule_viewer.colorType;
	} else {
		this.display3d.specs.proteins_residueColor = 'none';
	}
	this.display3d.repaint();
};
