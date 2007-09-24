package org.regenstrief.linkage.testing;

/*
 * Class written to test the linkage database management code.
 */

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.regenstrief.linkage.MatchFinder;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.RecordFieldAnalyzer;
import org.regenstrief.linkage.analysis.UnMatchableRecordException;
import org.regenstrief.linkage.db.RecordDBManager;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DBTest1 {

	
	public static void main(String[] args) {
		if(args.length == 0){
			System.out.println("usage: java DBTest <config file>");
			System.exit(0);
		}
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
			MatchingConfig test_mc = rmc.getMatchingConfigs().get(0);
			RecordDBManager ldbm = new RecordDBManager(rmc.getLinkDataSource1());
			
			MatchFinder mf = new MatchFinder(rmc.getLinkDataSource1(), rmc.getMatchingConfigs(), new RecordFieldAnalyzer(),MatchFinder.Scoring.BLOCKING_EXCLUSIVE);
			Record test_find = new Record();
			test_find.addDemographic("fn", "test");
			test_find.addDemographic("ln", "patient");
			
			try{
				mf.findBestMatch(test_find);
			}
			catch(UnMatchableRecordException umre){
				
			}
			
			
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
