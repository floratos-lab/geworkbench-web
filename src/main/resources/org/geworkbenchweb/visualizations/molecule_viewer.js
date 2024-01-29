window.org_geworkbenchweb_visualizations_MoleculeViewer = function () {
	this.onStateChange = function () {
		$molecule_viewer.set3DRepresentation(this.getState().representation)
		$molecule_viewer.setDisplayAtoms(this.getState().atoms)
		$molecule_viewer.setDisplayBonds(this.getState().bonds)
		$molecule_viewer.setDisplayLabels(this.getState().labels)
		$molecule_viewer.setDisplayRibbon(this.getState().ribbon)
		$molecule_viewer.setDisplayBackbone(this.getState().backbone)
		$molecule_viewer.setDisplayPipe(this.getState().pipe)
		$molecule_viewer.setCartoonize(this.getState().cartoonize)
		$molecule_viewer.setColorByChain(this.getState().colorByChain)

		$molecule_viewer.setColorType(this.getState().colorType)
		$molecule_viewer.setColorByResidue(this.getState().colorByResidue)

		$molecule_viewer.repaint()
	}

	$molecule_viewer.create(this.getElement(), this.getState().pdb_content, this.getState().representation)
}

const $molecule_viewer = {
	repaint: function () {
		this.display3d.repaint();
	}
}

$molecule_viewer.create = function (div, pdb_content, representation) {
	$(div).empty();

	var c = document.createElement('CANVAS');
	c.id = 'display3d';
	var w = $(div).parents('div.v-verticallayout').width();
	var h = $(div).parents('div.v-verticallayout').height();

	div.appendChild(c);

	this.display3d = new ChemDoodle.TransformCanvas3D('display3d', w, h);
	if (this.display3d['gl'] == null) {
		var x = document.createElement('div');
		x.innerHTML = 'Your browser is not fully supported for this molecule viewer. Please try other browsers to enjoy better visualization.';
		div.insertBefore(x, c);

		this.display3d = new ChemDoodle.TransformCanvas('display3d', w, h - 50, true);
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
	this.display3d.specs.macro_displayAtoms = false;
	this.display3d.specs.macro_displayBonds = false;
	this.display3d.specs.proteins_ribbonCartoonize = true;
	var pdbStructure = ChemDoodle.readPDB(pdb_content);
	this.display3d.loadMolecule(pdbStructure);
};

$molecule_viewer.colorType = 'amino';

$molecule_viewer.set3DRepresentation = function (representation) {
	this.display3d.specs.set3DRepresentation(representation);
};

$molecule_viewer.setDisplayAtoms = function (displayAtoms) {
	this.display3d.specs.macro_displayAtoms = displayAtoms;
};

$molecule_viewer.setDisplayBonds = function (displayBonds) {
	this.display3d.specs.macro_displayBonds = displayBonds;
};

$molecule_viewer.setDisplayLabels = function (displayLabels) {
	this.display3d.specs.atoms_displayLabels_3D = displayLabels;
};

$molecule_viewer.setDisplayRibbon = function (displayRibbon) {
	this.display3d.specs.proteins_displayRibbon = displayRibbon;
};

$molecule_viewer.setDisplayBackbone = function (displayBackbone) {
	this.display3d.specs.proteins_displayBackbone = displayBackbone;
};

$molecule_viewer.setDisplayPipe = function (displayPipe) {
	this.display3d.specs.proteins_displayPipePlank = displayPipe;
};

$molecule_viewer.setCartoonize = function (cartoonize) {
	this.display3d.specs.proteins_ribbonCartoonize = cartoonize;
};

$molecule_viewer.setColorByChain = function (colorByChain) {
	this.display3d.specs.macro_colorByChain = colorByChain;
};

$molecule_viewer.setColorByResidue = function (colorByResidue) {
	if (colorByResidue) {
		this.display3d.specs.proteins_residueColor = $molecule_viewer.colorType;
	} else {
		this.display3d.specs.proteins_residueColor = 'none';
	}
};

$molecule_viewer.setColorType = function (colorType) {
	this.display3d.specs.proteins_residueColor = $molecule_viewer.colorType = colorType;
};
