package org.geworkbenchweb.plugins.geneontology;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ontologizer.ByteString;
import ontologizer.OntologizerCore;
import ontologizer.association.AssociationContainer;
import ontologizer.association.Gene2Associations;
import ontologizer.calculation.AbstractGOTermProperties;
import ontologizer.calculation.EnrichedGOTermsResult;
import ontologizer.go.OBOParserException;
import ontologizer.go.Term;
import ontologizer.go.TermID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.GOResult;
import org.geworkbenchweb.pojos.GOResultRow;

public class GOAnalysis {
	
	static Log log = LogFactory.getLog(GOAnalysis.class);

	final private Map<Serializable, Serializable> params;
	
	public GOAnalysis(Long dataSetId, HashMap<Serializable, Serializable> params) {
		this.params = params;
	}

	public GOResult execute() throws Exception {
		String tempPath = GeworkbenchRoot.getBackendDataDirectory() + "/temp/";
		
		final String studySetFileName = "STUDYSET_TEMPORARY";
		final String populationSetFileName = "POPULATIONSET_TEMPORARY";
		String	studySetFilePath = tempPath + studySetFileName;
		String	populationSetFilePath = tempPath + populationSetFileName;

		File studySet = new File(studySetFilePath);
		File populationSet = new File(populationSetFilePath);
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(studySet));
			String[] changedGenesArray = (String[])params.get(GOUI.PARAM_CHANGED_GENE_LIST);
			if (changedGenesArray == null || changedGenesArray.length == 0) {
				pw.close();
				throw new Exception("Invalid 'changed gene list'");
			}
			for (String gene : changedGenesArray) {
				pw.println(gene);
				// FIXME analysisResult.addChangedGenes(gene);
			}
			pw.close();

			pw = new PrintWriter(new FileWriter(populationSet));
			String[] referenceGenesArray = (String[])params.get(GOUI.PARAM_REFERENCE_GENE_LIST);
			if (referenceGenesArray == null || referenceGenesArray.length == 0) {
				throw new Exception("Invalid 'reference gene list'");
			}
			for (String gene : referenceGenesArray) {
				pw.println(gene);
				//FIXME analysisResult.addReferenceGenes(gene);
			}
			pw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new Exception("Failed to create temporary file: " + e1.getMessage());
		}
		
		final OntologizerCore.Arguments arguments = new OntologizerCore.Arguments();
		/*
		 * Ontologizer does not handle the null pointer for this. The name MUST
		 * be set.
		 */
		arguments.goTermsOBOFile = (String)params.get(GOUI.PARAM_OBO_FILE);
		arguments.studySet = studySetFilePath;
		arguments.populationFile = populationSetFilePath;
		arguments.associationFile = (String)params.get(GOUI.PARAM_ASSOCIATION_FILE);

		arguments.calculationName = (String)params.get(GOUI.PARAM_CALCULATION_METHOD);
		arguments.correctionName = (String)params.get(GOUI.PARAM_CORRECTION);
		/* leave other argument as default */
		log.debug(arguments.associationFile);
		
		// step 1: ontologizerCore = new OntologizerCore(arguments);
		/*
		 * OntologizerCore has only one constructor that take the file names. We
		 * cannot directly set the studydset as a StudySetList, which is
		 * actually used in Ontologizer.
		 */
		OntologizerCore ontologizerCore = null;
		try {
			ontologizerCore = new OntologizerCore(arguments);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new Exception("FileNotFoundException from Ontologizer: " + e.getMessage() );
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("IOException from Ontologizer: " + e.getMessage() );
		} catch (OBOParserException e) {
			e.printStackTrace();
			throw new Exception("OBOParserException from Ontologizer: " + e.getMessage() );
		}

		// step 2: while ((studySetResult = ontologizerCore.calculateNextStudy()) != null) {
		Map<Integer, GOResultRow> analysisResult = new HashMap<Integer, GOResultRow>();
		EnrichedGOTermsResult studySetResult = null;
		while ((studySetResult = ontologizerCore.calculateNextStudy()) != null) {
			appendOntologizerResult(analysisResult, studySetResult);

			// this is not needed except for understanding the result structure
			if (log.isDebugEnabled()) {
					if (studySetResult.getSize() > 0) {
						File outFile = new File("ONTOLOGIZER_RESULT");
						studySetResult.writeTable(outFile);
						log.debug("Ontologizer got result.");
					} else {
						log.debug("Ontologizer got empty result. Size="
								+ studySetResult.getSize());
					}
			}
		}
		
		// step 3: 
		HashMap<Integer, Set<String>> term2Gene = new HashMap<Integer, Set<String> >();
		AssociationContainer container = ontologizerCore.getGoAssociations();
		for(ByteString gene : container.getAllAnnotatedGenes()) {
			String geneString = gene.toString();
			Gene2Associations associations = container.get(gene);
			for(TermID termId : associations.getAssociations()) {
				Integer id = termId.id;
				Set<String> set = term2Gene.get(id);
				if(set==null) {
					set = new HashSet<String>();
					term2Gene.put(id, set);
				}
				if(!set.contains(geneString)) {
					set.add(gene.toString());
				}
			}
		}
		
		/* after the analysis, delete the temporary file */
		if (!studySet.delete()) {
			log.error("Error in trying to delete the temporary file "
					+ studySet.getAbsolutePath());
		}
		if (!populationSet.delete()) {
			log.error("Error in trying to delete the temporary file "
					+ populationSet.getAbsolutePath());
		}
		
		return new GOResult(analysisResult);//, term2Gene);
	}

	private static void appendOntologizerResult(Map<Integer, GOResultRow> result, EnrichedGOTermsResult ontologizerResult) {
		Iterator<AbstractGOTermProperties> iter = ontologizerResult.iterator();

		while (iter.hasNext()) {
			AbstractGOTermProperties prop = iter.next();
			Term term = prop.goTerm;
			int popCount = 0, studyCount = 0;
			for (int i = 0; i < prop.getNumberOfProperties(); i++) {
				/*
				 * the index may be fixed, but not 'visible' from the
				 * AbstractGOTermProperties's interface
				 */
				if (prop.getPropertyName(i).equalsIgnoreCase("Pop.term")) {
					popCount = Integer.parseInt(prop.getProperty(i));
				} else if (prop.getPropertyName(i).equalsIgnoreCase(
						"Study.term")) {
					studyCount = Integer.parseInt(prop.getProperty(i));
				} else {
					// log.trace(i+":"+prop.getPropertyName(i)+"="+prop.getProperty(i));
				}
			}
			GOResultRow r = new GOResultRow(term.getName(), term
					.getNamespaceAsAbbrevString(), prop.p, prop.p_adjusted, popCount,
					studyCount);
			result.put(term.getID().id, r);
		}
//
//		int size = ontologizerResult.getSize();
//			GOResultRow row1 = new GOResultRow("A", "B", 1.2, 2.3, 4, 5);
//			GOResultRow row2 = new GOResultRow("C", "DDB", 4.2, 5.3, 14, 25);
//			GOResultRow row3 = new GOResultRow("size="+size, "earth", 4.21, 5.23, 104, 295);
//			result.put(123, row1);
//			result.put(987, row2);
//			result.put(9817, row3);
	}

}
