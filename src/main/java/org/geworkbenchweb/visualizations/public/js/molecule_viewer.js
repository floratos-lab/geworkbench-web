var $molecule_viewer = {}; /* module namespace */

$molecule_viewer.create = function(id, pdb_content, representation) {
	var div = document.getElementById(id);
	
	$(div).empty();
	
	var c = document.createElement('CANVAS');
	c.id = 'display3d';
	var w = $(div).parents('div.v-verticallayout').width();
	var h = $(div).parents('div.v-verticallayout').height();
    c.width = w/2;
	c.height = h/2;

	div.appendChild(c);

	// set default value when necessary
	representation = typeof representation !== 'undefined' ? representation : 'van der Waals Spheres';
	
	var pdbStructure = ChemDoodle.readPDB(pdb_content);
	var display3d = new ChemDoodle.TransformCanvas3D('display3d', w/2, h/2);
	display3d.specs.set3DRepresentation(representation);
	display3d.specs.macro_displayAtoms = true;
	display3d.specs.macro_displayBonds = true;
	display3d.loadMolecule(pdbStructure);
};
