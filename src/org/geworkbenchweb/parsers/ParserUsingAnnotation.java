package org.geworkbenchweb.parsers;

import java.io.File;

import org.geworkbench.util.AnnotationInformationManager.AnnotationType;
import org.vaadin.appfoundation.authentication.data.User;

public abstract class ParserUsingAnnotation extends Parser {

	public abstract void parseAnnotation(File annotFile,
			AnnotationType annotType, User annotOwner)
			throws GeWorkbenchParserException;

}
