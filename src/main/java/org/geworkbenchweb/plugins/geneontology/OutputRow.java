package org.geworkbenchweb.plugins.geneontology;

import com.vaadin.data.Item;

public class OutputRow implements Comparable<OutputRow> {

	private String id;
	private String name;
	private String namespace;
	private double p;
	private double pAdjusted;
	private int popCount;
	private int studyCount;

	public static OutputRow getInstance(Item item) {
		String goId = (String) (item.getItemProperty(GOResultUI.HEADER_ID)
				.getValue());
		String name = (String) (item.getItemProperty(GOResultUI.HEADER_NAME)
				.getValue());
		String namespace = (String) (item
				.getItemProperty(GOResultUI.HEADER_NAMESPACE).getValue());
		double p = (Double) (item.getItemProperty(GOResultUI.HEADER_P_VALUE)
				.getValue());
		double pAdjusted = (Double) (item
				.getItemProperty(GOResultUI.HEADER_ADJUSTED_P_VALUE).getValue());
		int popCount = (Integer) (item
				.getItemProperty(GOResultUI.HEADER_POPULATION_COUNT).getValue());
		int studyCount = (Integer) (item
				.getItemProperty(GOResultUI.HEADER_STUDY_COUNT).getValue());
		return new OutputRow(goId, name, namespace, p, pAdjusted, popCount,
				studyCount);
	}

	private OutputRow(String id, String name, String namespace, double p,
			double pAdjusted, int popCount, int studyCount) {
		this.id = id;
		this.name = name;
		this.namespace = namespace;
		this.p = p;
		this.pAdjusted = pAdjusted;
		this.popCount = popCount;
		this.studyCount = studyCount;
	}

	@Override
	public int compareTo(OutputRow o) {
		return (int) Math.signum(pAdjusted - o.pAdjusted);
	}

	@Override
	public String toString() {
		name = name.replaceAll("\"", "'");
		return id + ",\"" + name + "\"," + namespace + "," + p + ","
				+ pAdjusted + "," + popCount + "," + studyCount;
	}
}
