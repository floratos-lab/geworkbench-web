var $molecule_viewer = {}; /* module namespace */

$molecule_viewer.create = function(id, pdb_content) {
	var div = document.getElementById(id);
	
	$(div).empty();
	
	var c = document.createElement('CANVAS');
	c.id = 'display3d';
	var w = $(div).parents('div.v-verticallayout').width();
	var h = $(div).parents('div.v-verticallayout').height();
    c.width = w/2;
	c.height = h/2;

	div.appendChild(c);

	var pdbStructure = ChemDoodle.readPDB(pdb_content);
	var display3d = new ChemDoodle.TransformCanvas3D('display3d', w/2, h/2);
	display3d.specs.set3DRepresentation('van der Waals Spheres');
	var newSpecs = new ChemDoodle.structures.VisualSpecifications();
	newSpecs.set3DRepresentation('Wireframe');
	display3d.residueSpecs = newSpecs;
	display3d.specs.macro_displayAtoms = true;
	display3d.specs.macro_displayBonds = true;
	display3d.loadMolecule(pdbStructure);
};
