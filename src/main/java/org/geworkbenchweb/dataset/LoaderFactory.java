package org.geworkbenchweb.dataset;

import java.util.ArrayList;
import java.util.List;

public class LoaderFactory {
	
	private List<Loader> parserLiet = new ArrayList<Loader>();

	public LoaderFactory() {
		parserLiet.add(new ExpressionFileLoader());
		parserLiet.add(new TabDelimitedFormatLoader()); 
		parserLiet.add(new PdbFileLoader()); 
	}

	public List<Loader> getParserList() {
		return parserLiet;
	}
}
