package org.regenstrief.linkage.testing;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;
import org.w3c.dom.Document;

public class XMLTranslatorTest {
	
	public static void main(String[] args) {
		File config = new File(args[0]);
		if (!config.exists()) {
			System.out.println("config file does not exist, exiting");
			System.exit(0);
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			// Load the XML configuration file
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			RecMatchConfig rmc = XMLTranslator.createRecMatchConfig(doc);
			String fname = config.getAbsolutePath();
			fname = fname.substring(0, fname.length() - 3);
			File newconfig = new File(fname + "test.xml");
			Document d = XMLTranslator.toXML(rmc);
			XMLTranslator.writeXMLDocToFile(d, newconfig);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
