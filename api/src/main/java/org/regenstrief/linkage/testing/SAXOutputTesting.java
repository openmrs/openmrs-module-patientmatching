package org.regenstrief.linkage.testing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.io.OrderedDataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.MatchResultsXML;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.ScorePair;
import org.regenstrief.linkage.util.XMLTranslator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SAXOutputTesting {
	
	public static void main(String[] args) throws Exception{
		List<MatchResult> some_results = getMatchResults();
		System.out.println("have " + some_results.size() + " results to write");
		
		// set parser property
		Properties p = System.getProperties();
		
		p.setProperty("org.xml.sax.parser", "javax.xml.parsers.SAXParser");
		
		/*
		MatchResultsSAXSource mrss = new MatchResultsSAXSource(some_results);
		XMLReader reader = (XMLReader)new MatchResultsSAXReader();
		mrss.setXMLReader(reader);
		
		// write SAX streem to system.out
		StreamResult out = new StreamResult(System.out);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer serializer = tf.newTransformer();
		serializer.transform(mrss, out);
		*/
		if(MatchResultsXML.resultsToXML(some_results,new File("sax_test.xml"))){
			System.out.println("file written successfully");
		} else {
			System.out.println("file write failed");
		}
	}
	
	public static List<MatchResult> getMatchResults(){
		ArrayList<MatchResult> ret = new ArrayList<MatchResult>();
		File config = new File("test.xml");
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
			
			// new way of making readers
			ReaderProvider rp = ReaderProvider.getInstance();
			// ugly casting needed until io package object/interface hierarchy is improved or updated
			OrderedDataSourceReader dsr1 = rp.getReader(rmc.getLinkDataSource1(), mc_test);
			OrderedDataSourceReader dsr2 = rp.getReader(rmc.getLinkDataSource1(), mc_test);
			org.regenstrief.linkage.io.OrderedDataSourceFormPairs fp = new org.regenstrief.linkage.io.OrderedDataSourceFormPairs(dsr1, dsr2, mc_test, type_table);
			
			// iterate through the Record pairs and print the score
			Record[] pair;
			ScorePair sp = new ScorePair(mc_test);
			while((pair = fp.getNextRecordPair()) != null){
				Record r1 = pair[0];
				Record r2 = pair[1];
				ret.add(sp.scorePair(r1, r2));
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
		return ret;
	}
}
