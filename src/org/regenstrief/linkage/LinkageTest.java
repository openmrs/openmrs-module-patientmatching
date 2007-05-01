package org.regenstrief.linkage;

/*
 * Class written to test objects and methods
 */

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.regenstrief.linkage.io.*;
import org.regenstrief.linkage.util.*;
import org.regenstrief.linkage.analysis.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class LinkageTest {

	
	public static void main(String[] args) {
		File config = new File(args[0]);
		if(!config.exists()){
			System.out.println("config file does not exist, exiting");
			System.exit(0);
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try{
			// Load the XML configuration file
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			RecMatchConfig rmc = XMLTranslator.createRecMatchConfig(doc);
			MatchingConfig mc_test = rmc.getMatchingConfigs().get(0);
			
			VectorTable vt = new VectorTable(mc_test);
			System.out.println(vt);
		}
		catch(ParserConfigurationException pce){
			System.out.println("error making XML parser: " + pce.getMessage());
		}
		catch(SAXException spe){
			System.out.println("error parsing config file: " + spe.getMessage());
		}
		catch(IOException ioe){
			System.out.println(ioe.getMessage());
		}
		
	}

}
