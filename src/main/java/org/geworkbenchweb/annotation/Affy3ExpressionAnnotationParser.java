/**
 * 
 */
package org.geworkbenchweb.annotation;

import org.geworkbench.util.AnnotationInformationManager.AnnotationType; 
 
public class Affy3ExpressionAnnotationParser extends AffyAnnotationParser{

	// at this time, the field names in AnnotationParser are those actually used
	// the field names here are to be used in the future
	private static final String PROBE_SET_ID = "Probe Set ID";
	private static final String SCIENTIFIC_NAME = "Species Scientific Name";
	private static final String UNIGENE_CLUSTER = "Archival UniGene Cluster";
	private static final String GENOME_VERSION = "Genome Version";
	private static final String ALIGNMENT = "Alignments";
	private static final String PATHWAY = "Pathway"; // pathway
	private static final String TRANSCRIPT = "Transcript Assignments";
	
	private static final String GENE_SYMBOL = "Gene Symbol";
	private static final String UNIGENE = "UniGene ID"; // Unigene
	private static final String DESCRIPTION = "Gene Title"; // (full name)
	
	private static final String GENE_ONTOLOGY_BIOLOGICAL_PROCESS = "Gene Ontology Biological Process";
	private static final String GENE_ONTOLOGY_CELLULAR_COMPONENT = "Gene Ontology Cellular Component";
	private static final String GENE_ONTOLOGY_MOLECULAR_FUNCTION = "Gene Ontology Molecular Function";

	private static final String LOCUSLINK = "Entrez Gene"; // LocusLink
	private static final String SWISSPROT = "SwissProt"; // swissprot
	public static final String REFSEQ = "RefSeq Transcript ID"; // RefSeq

	// columns read into geWorkbench
	// probe id must be first column read in, and the rest of the columns must
	// follow the same order
	// as the columns in the annotation file.
	private static final String[] labels = {
			PROBE_SET_ID, // probe id must be the first item in this list
			SCIENTIFIC_NAME, UNIGENE_CLUSTER, UNIGENE,
			GENOME_VERSION, ALIGNMENT, DESCRIPTION,
			GENE_SYMBOL, LOCUSLINK,
			SWISSPROT, REFSEQ,
			GENE_ONTOLOGY_BIOLOGICAL_PROCESS,
			GENE_ONTOLOGY_CELLULAR_COMPONENT,
			GENE_ONTOLOGY_MOLECULAR_FUNCTION, PATHWAY,
			TRANSCRIPT };

	@Override
	 AnnotationFields parseOneLine() {
		affyId = parser.getValueByLabel(labels[0]);
		if(affyId==null)
			return null;
		affyId = affyId.trim();
		AnnotationFields fields = new AnnotationFields();
		for (int i = 1; i < labels.length; i++) {
			String label = labels[i];
			String val = parser.getValueByLabel(label);
		 
			if (label.equals(GENE_SYMBOL))
				fields.setGeneSymbol(val);
			else if (label.equals(LOCUSLINK))
				fields.setLocusLink(val);
			else if (label.equals(SWISSPROT))
				fields.setSwissProt(val);
			else if (label.equals(DESCRIPTION))
				fields.setDescription(val);
			else if (label.equals(GENE_ONTOLOGY_MOLECULAR_FUNCTION))
				fields.setMolecularFunction(val);
			else if (label.equals(GENE_ONTOLOGY_CELLULAR_COMPONENT))
				fields.setCellularComponent(val);
			else if (label.equals(GENE_ONTOLOGY_BIOLOGICAL_PROCESS))
				fields.setBiologicalProcess(val);
			else if (label.equals(UNIGENE))
				fields.setUniGene(val);
			else if (label.equals(REFSEQ))
				fields.setRefSeq(val);
		}

		return fields;
	}
	
	public AnnotationType getAnnotationType()
	{
		return AnnotationType.AFFYMETRIX_3_EXPRESSION;
	}
	
	 
}
