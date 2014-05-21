package org.geworkbenchweb.genspace;

/**
 * This class contains methods for reading an XML file from disk, creating the necessary Events,
 * and then using the DatabaseManager to insert those into the database.
 */

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Scanner;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;
import org.geworkbench.components.genspace.server.stubs.AnalysisEventParameter;
import org.geworkbench.components.genspace.server.stubs.Transaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class XMLLoader {

	// the Document object
	private Document dom;

	// the set of objects that need to be inserted into the database
	private ArrayList<AnalysisEvent> events;
	private HashMap<String, Transaction> transactions;
	/**
	 * This is the starting point for any other object that wants to use this one.
	 * Specify the name of the file to read and then load into the database
	 * @param file The full path to the file
	 */
	public ArrayList<AnalysisEvent> readAndLoad(String file) { 

		// reset the list of events
		events = new ArrayList<AnalysisEvent>();
		transactions = new HashMap<String, Transaction>();
		//parse the xml file and get the dom object
		parseXmlFile(file);

		//get each element and create objects
		parseDocument();

		return events;

	}


	/**
	 * Helper method to parse the XML file and create the Document object
	 * @param file
	 */
	private void parseXmlFile(String file)
	{
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try 
		{
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			new FileReader(file);
			String doc = "<measurement>\n";
			Scanner s = new Scanner(new File(file));
			while(s.hasNextLine())
			{
				doc += s.nextLine() + "\n";
			}
			doc += "</measurement>";
			dom = db.parse(new InputSource(new StringReader(doc)));
			// System.out.println("Finished with " + file);


		}
		catch(ParserConfigurationException pce) 
		{
			pce.printStackTrace();
		}
		catch(SAXException se) 
		{
			se.printStackTrace();
		}
		catch(IOException ioe) 
		{
			ioe.printStackTrace();
		}
	}


	/**
	 * This method parses the Document object and returns an ArrayList of objects within.
	 */
	private void parseDocument(){
		//get the root elememt
		Element docEle = dom.getDocumentElement();

		//get a nodelist of <metric> elements
		NodeList nl = docEle.getElementsByTagName("metric");
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				//get the element
				Element el = (Element)nl.item(i);
				// create the objects and put them in the "events" ArrayList
				createObjects(el);
			}
		}

	}

	/**
	 * This method creates objects from the Element and then stores them in the
	 * ArrayList. It's usually not good to have a "side effect" of altering the state of
	 * an argument (in this case, the ArrayList), but... eh? What are ya gonna do?
	 */
	private void createObjects(Element empEl) {

		String hour="0";
		String minutes="0";
		String seconds="0";
		String year="0";
		String month="0";
		String day="0";

		NodeList nl = empEl.getElementsByTagName("time");
		if(nl != null && nl.getLength() > 0) {
			Element e = (Element)nl.item(0);
			year = getTextValue(e, "year");
			month = getTextValue(e, "month");
			day = getTextValue(e, "day");
			hour = getTextValue(e, "hour");
			minutes = getTextValue(e, "minute");
			seconds = getTextValue(e, "second");

		}

		HashMap<String, String> parameters = new HashMap<String, String>();

		NodeList n20 = empEl.getElementsByTagName("parameter");
		if (n20 != null && n20.getLength() > 0) {
			for(int i=0; i<n20.getLength(); i++) {
				Element e = (Element)n20.item(i);
				String key = getTextValue(e, "key");
				String value = getTextValue(e, "value");
				parameters.put(key, value);
			}

		}


		// get the username
		String user = null;
		NodeList nl2 = empEl.getElementsByTagName("user");
		if(nl2 != null && nl2.getLength() > 0) {
			Element e = (Element)nl2.item(0);
			user = e.getAttribute("name");

		}

		// get the host name
		String host = null;
		NodeList nl3 = empEl.getElementsByTagName("host");
		if(nl3 != null && nl3.getLength() > 0) {
			Element e = (Element)nl3.item(0);
			host = e.getAttribute("name");
		}


		// get the analysis name
		String analysis = null;
		NodeList nl4 = empEl.getElementsByTagName("analysis");
		if(nl4 != null && nl4.getLength() > 0) {
			Element e = (Element)nl4.item(0);
			analysis = e.getAttribute("name");
		}

		// get the dataset name
		String dataset = null;
		NodeList nl5 = empEl.getElementsByTagName("dataset");
		if(nl5 != null && nl5.getLength() > 0) {
			Element e = (Element)nl5.item(0);
			dataset = e.getAttribute("name");
		}

		// get the transaction id
		String transaction_id = "";
		NodeList nl6 = empEl.getElementsByTagName("transaction");
		if(nl6 != null && nl6.getLength() > 0) {
			Element e = (Element)nl6.item(0);
			transaction_id = e.getAttribute("id");
		}

		AnalysisEvent event = new AnalysisEvent();
		if(!transactions.containsKey(transaction_id))
		{
			Transaction t = new Transaction();
			t.setClientID(transaction_id);
			t.setDataSetName(dataset);

			Calendar ct = Calendar.getInstance();
			ct.set(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(minutes), Integer.parseInt(seconds));
			try {
				t.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(minutes), Integer.parseInt(seconds))));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DatatypeConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			t.setHostname(host);
			if(user == null)
				t.setUser(null);
			else
				t.setUserName(user);
			transactions.put(transaction_id, t);
		}
		
		event.setTransaction(transactions.get(transaction_id));

			Calendar.getInstance();
			try {
				event.setCreatedAt(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(minutes), Integer.parseInt(seconds))));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DatatypeConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		event.setToolname(analysis);
		
		ArrayList<AnalysisEventParameter> params = new ArrayList<AnalysisEventParameter>();
		for(String key : parameters.keySet())
		{
			AnalysisEventParameter p = new AnalysisEventParameter();
			p.setParameterKey(key);
			p.setParameterValue(parameters.get(key));
			params.add(p);
		}
		event.getParameters().addAll(params);
		
		events.add(event);

	}


	/**
	 * Take a xml element and the tag name, look for the tag and get
	 * the text content
	 */
	private String getTextValue(Element ele, String tagName) {
		String textVal = "";
		try {
			NodeList nl = ele.getElementsByTagName(tagName);
			if(nl != null && nl.getLength() > 0) {
				Element el = (Element)nl.item(0);
				textVal = el.getFirstChild().getNodeValue();
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return textVal;
	}

	/* Only use this as a standalone method. It either reads a single file or, if it's a directory, all files from that directory. */
	public static void main(String[] args)
	{
		// this is for reading one file at a time
		String name = "geworkbench_log.xml"; //null;

		try
		{
			File file = new File(name);

			//create an instance
			XMLLoader loader = new XMLLoader();

			if (file.isDirectory())
			{
				File[] files = file.listFiles();

				for (File f : files)
				{
					// read each file
					String filename = f.getAbsolutePath();
					// only care about xml files, of course
					if (filename.contains(".xml"))
					{
						loader.readAndLoad(filename);
					}
				}
			}
			else
			{
				loader.readAndLoad(name);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}


