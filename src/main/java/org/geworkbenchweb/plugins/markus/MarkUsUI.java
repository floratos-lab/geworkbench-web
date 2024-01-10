package org.geworkbenchweb.plugins.markus;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.HashMap;

import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.MarkUsResult;
import org.geworkbenchweb.pojos.PdbFileInfo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.server.UserError;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class MarkUsUI extends VerticalLayout implements AnalysisUI {

	private static final long serialVersionUID = 988711785863720384L;

	private Long dataSetId;
	private PdbFileInfo pdbFileInfo;
	private ComboBox cbxChain = new ComboBox("Chain");
	private TextField email = new TextField("Email (optional)");
	private TextField title = new TextField("Title (optional)");
	private CheckBox cbkey = new CheckBox("Private Key");
	private CheckBox skan = new CheckBox("Skan", true);
	private CheckBox dali = new CheckBox("Dali", false);
	private CheckBox screen = new CheckBox("SCREEN", true);
	private CheckBox vasp = new CheckBox("VASP", true);
	private CheckBox lbias = new CheckBox("LBias", true);
	private CheckBox predus = new CheckBox("PredUs", true);
	private CheckBox delphi = new CheckBox("DelPhi", true);
	private CheckBox psiblast = new CheckBox("PSI-BLAST", true);
	private CheckBox ips = new CheckBox("InterProScan", true);
	private CheckBox consurf = new CheckBox("ConSurf", true);
	private CheckBox consurf3 = new CheckBox("analysis3");
	private CheckBox consurf4 = new CheckBox("analysis4");
	private TextField gridsize = new TextField("Grid size");
	private TextField boxfill = new TextField("Percentage box fill");
	private TextField steps = new TextField("Focusing steps");
	private TextField sc = new TextField("Salt concentration");
	private TextField radius = new TextField("Probe radius");
	private ComboBox ibc = new ComboBox("Initial boundary condition",
			Arrays.asList("Zero", "Debye-Huckel Dipole", "Debye-Huckel Total"));
	private TextField nli = new TextField("Non linear iterations");
	private TextField li = new TextField("Linear iterations");
	private TextField idc = new TextField("Internal dielectric constant");
	private TextField edc = new TextField("External dielectric constant");
	private TextField csftitle3 = new TextField("Title");
	private TextField eval3 = new TextField("PSI-Blast E Value");
	private TextField iter3 = new TextField("Iterations");
	private TextField filter3 = new TextField("Identity Filter Percentage");
	private ComboBox msa3 = new ComboBox("Multiple Sequence Alignment", Arrays.asList("Muscle", "ClustalW"));
	private TextField csftitle4 = new TextField("Title");
	private TextField eval4 = new TextField("PSI-Blast E Value");
	private TextField iter4 = new TextField("Iterations");
	private TextField filter4 = new TextField("Identity Filter Percentage");
	private ComboBox msa4 = new ComboBox("Multiple Sequence Alignment", Arrays.asList("Muscle", "ClustalW"));

	public MarkUsUI(Long dataSetId) {

		setImmediate(true);
		setSpacing(true);

		Accordion tabs = new Accordion();

		tabs.addTab(buildMainPanel(), "Structure Analysis Parameters", null);
		tabs.addTab(buildSequencePanel(), "Sequence Analysis Parameters", null);
		tabs.addTab(buildDelphiPanel(), "DelPhi Parameters", null);
		tabs.addTab(buildConsurf3Panel(), "Add Customized ConSurf analysis 3", null);
		tabs.addTab(buildConsurf4Panel(), "Add Customized ConSurf analysis 4", null);
		tabs.addTab(buildPriorPanel(), "Retrieve Prior Result", null);

		addComponent(tabs);

		final Button submitButton = new Button("Submit", new SubmitListener(this));
		addComponent(submitButton);

		setDataSetId(dataSetId);
	}

	private class SubmitListener implements Button.ClickListener {
		private static final long serialVersionUID = 2697121374034799868L;
		private MarkUsUI form = null;

		public SubmitListener(MarkUsUI paramform) {
			form = paramform;
		}

		@Override
		public void buttonClick(ClickEvent event) {
			MarkusAnalysis analysis = new MarkusAnalysis(pdbFileInfo.getFilename(), form, dataSetId);
			analysis.execute();
		}
	}

	private VerticalLayout buildMainPanel() {
		VerticalLayout vlayout = new VerticalLayout();
		vlayout.addComponent(buildStructurePanel());
		vlayout.addComponent(buildOptionalPanel());
		return vlayout;
	}

	private Panel buildOptionalPanel() {
		cbxChain = new ComboBox("Chain");
		cbxChain.setNullSelectionAllowed(false);
		if (cbxChain.size() > 0) {
			cbxChain.setValue(cbxChain.getItemIds().iterator().next());
		}

		GridLayout layout = new GridLayout(4, 1);
		layout.setSpacing(true);
		layout.setSizeFull();

		layout.addComponent(cbxChain);
		layout.addComponent(email);
		layout.addComponent(title);
		layout.addComponent(cbkey);
		cbxChain.setWidth(50, Unit.PIXELS);

		layout.setComponentAlignment(cbxChain, Alignment.MIDDLE_LEFT);
		layout.setComponentAlignment(email, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(title, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(cbkey, Alignment.MIDDLE_CENTER);

		Panel panel = new Panel();
		panel.setContent(layout);
		return panel;
	}

	private Panel buildStructurePanel() {
		GridLayout grid = new GridLayout(3, 5);
		grid.setSizeFull();
		grid.setSpacing(true);

		Label sh = new Label("Structure Neighbors");
		sh.setDescription(
				"Structure relationships are identified by the structure alignment method Skan. The reference database combines SCOP\ndomains and PDB entries filtered by 60% sequence identity. In addition the Dali structure alignment method can be used.");
		skan.setEnabled(false);
		grid.addComponent(sh);
		grid.addComponent(skan);
		grid.addComponent(dali);
		sh.setWidth(120, Unit.PIXELS);

		sh = new Label("Cavity Analysis");
		sh.setDescription(
				"SCREEN is used to identify protein cavities that are capable of binding chemical compounds. SCREEN will provide an\nassessment of the druggability of each surface cavity based on its properties.\nVASP is a volumetric analysis tool for the comparison of binding sites in aligned protein structures.");
		screen.setEnabled(false);
		vasp.setEnabled(false);
		grid.addComponent(sh);
		grid.addComponent(screen);
		grid.addComponent(vasp);

		sh = new Label("Ligand Analysis");
		sh.setDescription("LBias evaluates binding site similarities of ligands for aligned protein structures.");
		lbias.setEnabled(false);
		grid.addComponent(sh);
		grid.addComponent(lbias, 1, 2, 2, 2);

		sh = new Label("Protein Protein Interactions");
		sh.setDescription(
				"PredUs is a template-based protein interface prediction method. Potential interfacial residues are identified by\niteratively 'mapping' interaction sites of structural neighbors involved in a complex to individual residues in the query protein.");
		predus.setEnabled(false);
		grid.addComponent(sh);
		grid.addComponent(predus, 1, 3, 2, 3);

		sh = new Label("Electrostatic Potential");
		sh.setDescription(
				"The electrostatic potential plays an important role in inferring protein properties like DNA binding regions or the enzymatic\nactivities. To calculate the electrostatic potential DelPhi is used. The default parameters are tuned to suit protein domains in\ngeneral, though adjustment by the user might be necessary. Read the DelPhi manual for a detailed parameter description.");
		grid.addComponent(sh);
		grid.addComponent(delphi, 1, 4, 2, 4);

		Panel panel = new Panel();
		panel.setContent(grid);
		return panel;
	}

	private VerticalLayout buildSequencePanel() {
		GridLayout grid = new GridLayout(3, 3);
		grid.setSizeFull();
		grid.setSpacing(true);

		Label sh = new Label("Sequence Neighbors");
		sh.setDescription(
				"Proteins sharing sequence similarity with the target protein are identified by running three PSI BLAST iterations (E-value\n0.001) against the UniProt reference database. Additionally sequence domain and motif databases are searched using the\nInterProScan service at the EBI.");
		psiblast.setEnabled(false);
		ips.setEnabled(false);
		grid.addComponent(sh);
		grid.addComponent(psiblast);
		grid.addComponent(ips);
		sh.setWidth(140, Unit.PIXELS);

		sh = new Label("Amino Acid Conservation Profile");
		sh.setDescription(
				"Highly conserved amino acids can indicate functionally relevant regions. To identify these amino acids sequences identified\nby BLAST sharing less than 80% identity are aligned using Muscle. For the resulting multiple sequence alignment ConSurf is\nused to estimate the conservation scores. If seeds and full Pfam alignments are available, these are used additionally for\nthe conservation analysis.\nTwo ConSurf analyses can be defined by the User specifying the number of PSI-BLAST iterations, the E-value threshold,\nand the sequence identity cutoff.");
		consurf.setEnabled(false);
		grid.addComponent(sh);
		grid.addComponent(consurf, 1, 1, 2, 1);

		sh = new Label("Add Customized ConSurf");
		grid.addComponent(sh);
		grid.addComponent(consurf3);
		grid.addComponent(consurf4);

		Panel panel = new Panel();
		panel.setContent(grid);
		VerticalLayout vlayout = new VerticalLayout();
		vlayout.addComponent(panel);
		return vlayout;
	}

	private Panel buildDelphiPanel() {
		GridLayout grid = new GridLayout(3, 4);
		grid.setSpacing(true);
		grid.setSizeFull();
		gridsize.setValue(String.valueOf(145));
		grid.addComponent(gridsize);

		boxfill.setValue(String.valueOf(85));
		grid.addComponent(boxfill);

		steps.setValue(String.valueOf(3));
		grid.addComponent(steps);

		sc.setValue(String.valueOf(0.145));
		grid.addComponent(sc);

		radius.setValue(String.valueOf(1.4));
		grid.addComponent(radius);

		ibc.setNullSelectionAllowed(false);
		ibc.setValue("Debye-Huckel Total");
		grid.addComponent(ibc);

		nli.setValue(String.valueOf(1000));
		grid.addComponent(nli);

		li.setValue(String.valueOf(1000));
		grid.addComponent(li, 1, 2, 2, 2);

		idc.setValue(String.valueOf(2));
		grid.addComponent(idc);

		edc.setValue(String.valueOf(80));
		grid.addComponent(edc);

		Panel panel = new Panel();
		panel.setContent(grid);
		return panel;
	}

	private Panel buildConsurf3Panel() {
		GridLayout grid = new GridLayout(2, 3);
		grid.setSpacing(true);
		grid.setSizeFull();
		csftitle3.setValue("analysis 3");
		grid.addComponent(csftitle3, 0, 0, 1, 0);

		eval3.setValue(String.valueOf(0));
		iter3.setValue(String.valueOf(0));
		grid.addComponent(eval3);
		grid.addComponent(iter3);

		filter3.setValue(String.valueOf(0));
		msa3.setNullSelectionAllowed(false);
		msa3.setValue("Muscle");
		grid.addComponent(filter3);
		grid.addComponent(msa3);

		Panel panel = new Panel();
		panel.setContent(grid);
		return panel;
	}

	private Panel buildConsurf4Panel() {
		GridLayout grid = new GridLayout(2, 3);
		grid.setSpacing(true);
		grid.setSizeFull();
		csftitle4.setValue("analysis 4");
		grid.addComponent(csftitle4, 0, 0, 1, 0);

		eval4.setValue(String.valueOf(0));
		iter4.setValue(String.valueOf(0));
		grid.addComponent(eval4);
		grid.addComponent(iter4);

		filter4.setValue(String.valueOf(0));
		msa4.setNullSelectionAllowed(false);
		msa4.setValue("Muscle");
		grid.addComponent(filter4);
		grid.addComponent(msa4);

		Panel panel = new Panel();
		panel.setContent(grid);
		return panel;
	}

	private Panel buildPriorPanel() {
		Form form = new Form();
		form.setDescription("MarkUs results are retained for 90 days on the server.");

		TextField priorTf = new TextField("MarkUs ID");
		form.addField("MarkUs ID", priorTf);

		TextField keyTf = new TextField("Private Key");
		form.addField("Private Key", keyTf);

		Button priorBtn = new Button("Retrieve Prior Result");
		priorBtn.addClickListener(new PriorListener(form, priorTf, keyTf));
		form.getFooter().addComponent(priorBtn);
		form.setValidationVisible(true);

		Panel panel = new Panel();
		panel.setContent(form);
		return panel;
	}

	private class PriorListener implements Button.ClickListener {
		private static final long serialVersionUID = 2697121374034799868L;
		private Form form;
		private TextField priorTf, keyTf;

		public PriorListener(Form f, TextField p, TextField k) {
			form = f;
			priorTf = p;
			keyTf = k;
		}

		@Override
		public void buttonClick(ClickEvent event) {
			form.setComponentError(null);
			String results = priorTf.getValue().toString().toUpperCase();
			if (results.length() == 0)
				priorTf.setComponentError(new UserError("MarkUs ID is missing"));
			else if (!results.matches("^MUS\\d+"))
				priorTf.setComponentError(new UserError("MarkUs ID must be 'MUS' followed by an integer"));
			else {
				String key = keyTf.getValue().toString();
				if (key.length() > 0)
					results = results + "&key=" + key;

				MarkusAnalysis analysis = new MarkusAnalysis(pdbFileInfo.getFilename(), MarkUsUI.this, dataSetId);
				analysis.getResultSet(results);
			}
		}

	}

	// markus parameters 1st part - totally 9
	public boolean getskanValue() {
		return skan.booleanValue();
	}

	public boolean getdaliValue() {
		return dali.booleanValue();
	}

	public boolean getscreenValue() {
		return screen.booleanValue();
	}

	public boolean getdelphiValue() {
		return delphi.booleanValue();
	}

	public boolean getpsiblastValue() {
		return psiblast.booleanValue();
	}

	public boolean getipsValue() {
		return ips.booleanValue();
	}

	public boolean getconsurfValue() {
		return consurf.booleanValue();
	}

	public boolean getconsurf3Value() {
		return consurf3.booleanValue();
	}

	public boolean getconsurf4Value() {
		return consurf4.booleanValue();
	}

	public String getChain() {
		return (String) cbxChain.getValue();
	}

	public boolean getkeyValue() {
		return cbkey.booleanValue();
	}

	public String getEmail(boolean isGrid) {
		String e = email.getValue().toString();
		if (isGrid)
			return escapeCgi(e);
		return e;
	}

	public String getTitle(boolean isGrid) {
		String t = title.getValue().toString();
		if (isGrid)
			return escapeCgi(t);
		return escapeHtml(t);
	}

	// delphi part - totally 10
	public int getgridsizeValue() {
		return Integer.parseInt(gridsize.getValue());
	}

	public int getboxfillValue() {
		return Integer.parseInt(boxfill.getValue());
	}

	public int getstepsValue() {
		return Integer.parseInt(steps.getValue());
	}

	public double getscValue() {
		return Double.parseDouble(sc.getValue());
	}

	public double getradiusValue() {
		return Double.parseDouble(radius.getValue());
	}

	public int getibcValue() {
		String desc = (String) ibc.getValue();
		if (desc.equals("Zero")) {
			return 1;
		} else if (desc.equals("Debye-Huckel Dipole")) {
			return 2;
		} else if (desc.equals("Debye-Huckel Total")) {
			return 4;
		}
		return 0;
	}

	public int getnliValue() {
		return Integer.parseInt(nli.getValue());
	}

	public int getliValue() {
		return Integer.parseInt(li.getValue());
	}

	public int getidcValue() {
		return Integer.parseInt(idc.getValue());
	}

	public int getedcValue() {
		return Integer.parseInt(edc.getValue());
	}

	// analysis 3 & 4
	public String getcsftitle3Value() {
		String val = (String) csftitle3.getValue();
		val = val.replaceAll(" ", "%20");
		return val;
	}

	public String getcsftitle4Value() {
		String val = (String) csftitle4.getValue();
		val = val.replaceAll(" ", "%20");
		return val;
	}

	public double geteval3Value() throws NumberFormatException {
		return Double.parseDouble(eval3.getValue().toString());
	}

	public double geteval4Value() throws NumberFormatException {
		return Double.parseDouble(eval4.getValue().toString());
	}

	public int getiter3Value() throws NumberFormatException {
		return Integer.parseInt(iter3.getValue().toString());
	}

	public int getiter4Value() throws NumberFormatException {
		return Integer.parseInt(iter4.getValue().toString());
	}

	public int getfilter3Value() throws NumberFormatException {
		return Integer.parseInt(filter3.getValue().toString());
	}

	public int getfilter4Value() throws NumberFormatException {
		return Integer.parseInt(filter4.getValue().toString());
	}

	public String getmsa3Value() {
		return (String) msa3.getValue();
	}

	public String getmsa4Value() {
		return (String) msa4.getValue();
	}

	/**
	 * Escape control characters so that they will not be executed by the browser.
	 * Replace the control characters with their escaped equivalents.
	 * 
	 * @param aText
	 * @return escaped string
	 */
	private static String escapeHtml(String aText) {
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(aText);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			if (character == ' ')
				result.append("%20");
			else if (character == '<')
				result.append("&lt;");
			else if (character == '>')
				result.append("&gt;");
			else if (character == '&')
				result.append("&amp;");
			else if (character == '\"')
				result.append("&quot;");
			else if (character == '\t')
				addCharEntity(9, result);
			else if (character == '!')
				addCharEntity(33, result);
			else if (character == '#')
				addCharEntity(35, result);
			else if (character == '$')
				addCharEntity(36, result);
			else if (character == '%')
				addCharEntity(37, result);
			else if (character == '\'')
				addCharEntity(39, result);
			else if (character == '(')
				addCharEntity(40, result);
			else if (character == ')')
				addCharEntity(41, result);
			else if (character == '*')
				addCharEntity(42, result);
			else if (character == '+')
				addCharEntity(43, result);
			else if (character == ',')
				addCharEntity(44, result);
			else if (character == '-')
				addCharEntity(45, result);
			else if (character == '.')
				addCharEntity(46, result);
			else if (character == '/')
				addCharEntity(47, result);
			else if (character == ':')
				addCharEntity(58, result);
			else if (character == ';')
				addCharEntity(59, result);
			else if (character == '=')
				addCharEntity(61, result);
			else if (character == '?')
				addCharEntity(63, result);
			else if (character == '@')
				addCharEntity(64, result);
			else if (character == '[')
				addCharEntity(91, result);
			else if (character == '\\')
				addCharEntity(92, result);
			else if (character == ']')
				addCharEntity(93, result);
			else if (character == '^')
				addCharEntity(94, result);
			else if (character == '_')
				addCharEntity(95, result);
			else if (character == '`')
				addCharEntity(96, result);
			else if (character == '{')
				addCharEntity(123, result);
			else if (character == '|')
				addCharEntity(124, result);
			else if (character == '}')
				addCharEntity(125, result);
			else if (character == '~')
				addCharEntity(126, result);
			// the char is not a special one, add it to the result as is
			else
				result.append(character);
			character = iterator.next();
		}
		return result.toString();
	}

	private static void addCharEntity(Integer aIdx, StringBuilder aBuilder) {
		String padding = "";
		if (aIdx <= 9)
			padding = "00";
		else if (aIdx <= 99)
			padding = "0";

		String number = padding + aIdx.toString();
		aBuilder.append("&#" + number + ";");
	}

	/**
	 * Escape control characters so that they will not be executed by cgi script.
	 * 
	 * @param aText
	 * @return escaped string
	 */
	private static String escapeCgi(String aText) {
		String res = "";
		try {
			res = URLEncoder.encode(aText, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public void setDataSetId(Long dataId) {
		this.dataSetId = dataId;
		if (dataId == 0)
			return;

		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		pdbFileInfo = FacadeFactory.getFacade().find(PdbFileInfo.class, dataset.getDataId());
		cbxChain.removeAllItems();
		for (String itemId : pdbFileInfo.getChains()) {
			cbxChain.addItem(itemId);
		}
		if (cbxChain.size() > 0) {
			cbxChain.setValue(cbxChain.getItemIds().iterator().next());
		}
	}

	@Override
	public Class<?> getResultType() {
		return MarkUsResult.class;
	}

	// TODO this analysis does thing with a different work-flow (not fire
	// AnalysisSubmissionEvent as other analysis plug-ins do)
	// we need reconcile the design if the difference is really necessary
	@Override
	public String execute(Long resultId,
			HashMap<Serializable, Serializable> parameters, Long userId)
			throws IOException, Exception {
		// no-op
		return null;
	}

}