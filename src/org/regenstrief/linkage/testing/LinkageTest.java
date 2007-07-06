package org.regenstrief.linkage.testing;

/*
 * Class written to test objects and methods
 */

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.VectorTable;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.io.OrderedCharDelimFileReader;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.ScorePair;
import org.regenstrief.linkage.util.XMLTranslator;
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
			
			// create a mapping of demographic to type; should move this into another class
			// and not do it explicitly
			Hashtable<String, Integer> type_table = new Hashtable<String, Integer>();
			List<DataColumn> dc = rmc.getLinkDataSource1().getDataColumns();
			Iterator<DataColumn> it = dc.iterator();
			while(it.hasNext()){
				DataColumn d = it.next();
				if(d.getIncludePosition() != DataColumn.INCLUDE_NA){
					type_table.put(d.getName(), new Integer(d.getType()));
				}
			}
			
			// print information about how the scores will be
			VectorTable vt = new VectorTable(mc_test);
			System.out.println(vt);
			
			// create readers and a FormPairs object
			DataSourceReader dsr1 = new OrderedCharDelimFileReader(rmc.getLinkDataSource1(), mc_test);
			DataSourceReader dsr2 = new OrderedCharDelimFileReader(rmc.getLinkDataSource2(), mc_test);
			org.regenstrief.linkage.io.FormPairs fp = new org.regenstrief.linkage.io.FormPairs(dsr1, dsr2, mc_test, type_table);
			
			// iterate through the Record pairs and print the score
			Record[] pair;
			ScorePair sp = new ScorePair(mc_test);
			int i = 0;
			while((pair = fp.getNextRecordPair()) != null){
				Record r1 = pair[0];
				Record r2 = pair[1];
				double score = sp.scorePair(r1, r2).getScore();
				
				System.out.println("score: " + score);
				i++;
			}
			System.out.println("found " + i + " records that matched on the blocking field");
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
