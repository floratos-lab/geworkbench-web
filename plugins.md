# status of plug-ins

The plug-ins provide various features to geWorkbench. In principle, they are not required for the platform itself.
There are two main groups: the first group is tightly tied to the general workflow and data types of geWorkbench,
which in turn includes two categories: analysis and visualization;
the second group, called 'standalone', is only presented via the geWorkbench user interface.

## list of analysis plug-ins
1. anova: on
2. aracne: on
3. cnkb: on (There is a standalone version of this plug-in as well.)
4. t test: on
5. gene ontology: on
6. hierarchical clustering: on
7. marina: on
8. markus: not fully functioning

## list of visualiztion plug-ins
1. anova result: on
2. aracne result: on
3. cnkb result: on
4. cytoscape: on
5. dendrogram: on
6. GO result: on
7. marina result: on
8. markus result: not fully functioning
9. molecular structure viewer: on
10. PDB file viewer: on
11. t test result: on
12. tabular microarray viewer: on

## list of 'standalone' plug-ins
1. cnkb: on (Note that there is a regular analysis version as well.)
2. lincs: not fully functioning
3. pbqdi: not fully functioning
4. TCGA Driver-Gene Inference: on

## customized widgets

1. barcode table
2. citrus diagram
3. cytoscape
4. dendrogram
5. molecular viewer

### barcode table
This is part of MsViper result viewer. MsViper is a synonym of marina.

### citrus diagram
Part of GeneBasedQueryAndDataIntegration (TCGA Driver-Gene Inference).

### cytoscape
Part of Networkviwer.

### dendrogram
Used in hierarchical clustering result viewer. References from HierarchicalClusteringResultUI, DisplayOptionCommand, and SubsetCommand.

### molecular viewer
Part of PDBViewer (molecular structure viewer).