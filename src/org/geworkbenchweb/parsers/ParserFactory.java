package org.geworkbenchweb.parsers;

import java.util.HashMap;
import java.util.Map;

public class ParserFactory {

	// because the parser choice is a string from GUI, let's control all the
	// mess in this class only
	public static Map<String, Parser> map = new HashMap<String, Parser>();
	// assume the same parser can be re-used. otherwise, we would same the class here
	static {
		map.put("Expression File", new ExpressionFileParser()); 
		map.put("PDB File", new PdbFileParser()); 
	}

//	Parser createParser(String parserName) throws GeWorkbenchParserException {
//		Parser parser = map.get(parserName);
//		if (parser == null) {
//			throw new GeWorkbenchParserException("unknown parser name");
//		} else {
//			return parser;
//		}
//	}
}
