package org.geworkbenchweb.parsers;

import java.util.ArrayList;
import java.util.List;

public class ParserFactory {
	
	private List<Parser> parserLiet = new ArrayList<Parser>();

	public ParserFactory() {
		parserLiet.add(new ExpressionFileParser()); 
		parserLiet.add(new PdbFileParser()); 
	}

	public List<Parser> getParserList() {
		return parserLiet;
	}
}
