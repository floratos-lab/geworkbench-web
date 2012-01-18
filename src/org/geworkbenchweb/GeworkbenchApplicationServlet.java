package org.geworkbenchweb;

import java.io.BufferedWriter;
import java.io.IOException;

import com.vaadin.terminal.gwt.server.ApplicationServlet;

public class GeworkbenchApplicationServlet extends ApplicationServlet {
    private static final long serialVersionUID = 1L;

    protected void writeAjaxPageHtmlHeader(final BufferedWriter page, String title, String themeUri) throws IOException {
        
    	page.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n");
        page.write("<style type=\"text/css\">"
                + "html, body {height:100%;margin:0;}</style>");
        
        // your code here
        page.write("<script type=\"text/javascript\" src=\"WebContent/VAADIN/js/cytoscape/AC_OETags.min.js\"></script>");
        page.write("<script type=\"text/javascript\" src=\"WebContent/VAADIN/js/cytoscape/json2.min.js\"></script>");
        page.write("<script type=\"text/javascript\" src=\"WebContent/VAADIN/js/cytoscape/cytoscapeweb.min.js\"></script>");
        
    }

}