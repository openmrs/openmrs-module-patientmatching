package org.regenstrief.linkage.util;

import java.util.*;
import java.io.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.w3c.dom.*;

/**
 * Class created to store the options for the Record Linkage GUI and 
 * record linkage classes.  It stores two data source objects and a list of 
 * MatchingConfig objects.
 */

public class RecMatchConfig {
	private LinkDataSource lds1, lds2;
	List<MatchingConfig> mc_configs;
	
	public RecMatchConfig(LinkDataSource lds1, LinkDataSource lds2, List<MatchingConfig> mc_configs){
		this.lds1 = lds1;
		this.lds2 = lds2;
		this.mc_configs = mc_configs;
	}
	
	/**
	 * 
	 * @return	the first LinkDataSource
	 */
	public LinkDataSource getLinkDataSource1(){
		return lds1;
	}
	
	/**
	 * 
	 * @return	the second LinkDataSource
	 */
	public LinkDataSource getLinkDataSource2(){
		return lds2;
	}
	
	/**
	 * 
	 * @return	a List of the MatchingConfig objects
	 */
	public List<MatchingConfig> getMatchingConfigs(){
		return mc_configs;
	}
	
	/*
	 * Method added to conveniently go from a list of columns by name,
	 * such as would be returned from a matching config object, and
	 * returns the display position of them.
	 */
	/* Discovered to not be in use
	public int[] getIndexesOfColumnNames(String[] names){
		if(lds1 == null){
			return null;
		}
		int[] ret = new int[names.length];
		for(int i = 0; i < names.length; i++){
			ret[i] = lds1.getDisplayPositionByName(names[i]);
		}
		return ret;
	}*/
	
	/*
	 * Test method to take an XML file and create a RecMatch object from mit
	 */
	public static void main(String[] args){
		if(args.length != 1){
			System.out.println("usage: java RecMatchConfig <XML config file>");
			System.exit(0);
		}
		File config = new File(args[0]);
		if(!config.exists()){
			System.out.println("config file does not exist, exiting");
			System.exit(0);
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try{
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			RecMatchConfig rmc = XMLTranslator.createRecMatchConfig(doc);
			System.out.println("created RecMatchConfig of:\n\t" + rmc);
			XMLTranslator.writeXMLDocToFile(XMLTranslator.toXML(rmc), new File("test.xml"));
		}
		catch(ParserConfigurationException pce){
			System.out.println("error making XML parser: " + pce.getMessage());
		}
		catch(SAXException spe){
			System.out.println("error parsing config file: " + spe.getMessage());
		}
		catch(IOException ioe){
			System.out.println("IO error parsing config file: " + ioe.getMessage());
		}
	}
}
