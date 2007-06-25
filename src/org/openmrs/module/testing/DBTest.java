package org.openmrs.testing;

/*
 * Class written to test the linkage database management code.
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
import org.regenstrief.linkage.db.*;
import org.regenstrief.linkage.io.CharDelimFileReader;
import org.regenstrief.linkage.io.DataBaseReader;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.util.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DBTest {

	
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
			LinkDBManager ldbm = new LinkDBManager(rmc.getLinkDataSource1());
			
			if(ldbm.connect()){
				System.out.println("connected to db link data source");
				
			} else {
				System.out.println("error connecting to db");
			}
			System.out.println("created a config of:\n" + test_mc);
			
			
			// test linkage
			//VectorTable vt = new VectorTable(test_mc);
			//System.out.println(vt);
			
			Hashtable<String, Integer> type_table = new Hashtable<String, Integer>();
			List<DataColumn> dc = rmc.getLinkDataSource1().getDataColumns();
			Iterator<DataColumn> it = dc.iterator();
			while(it.hasNext()){
				DataColumn d = it.next();
				if(d.getIncludePosition() != DataColumn.INCLUDE_NA){
					type_table.put(d.getName(), new Integer(d.getType()));
				}
			}
			DataSourceReader dsr2 = new CharDelimFileReader(rmc.getLinkDataSource2(), test_mc);
			DataSourceReader dsr1 = new DataBaseReader(rmc.getLinkDataSource1(), test_mc);
			org.regenstrief.linkage.io.FormPairs fp = new org.regenstrief.linkage.io.FormPairs(dsr1, dsr2, test_mc, type_table);
			
			System.out.println("form pairs created");
			Record[] pair;
			ScorePair sp = new ScorePair(test_mc);
			int i = 0;
			while((pair = fp.getNextRecordPair()) != null){
				Record r1 = pair[0];
				Record r2 = pair[1];
				double score = sp.scorePair(r1, r2).getScore();
				
				i++;
			}
			System.out.println("found " + i + " record pairs");
			
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
