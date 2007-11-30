package org.regenstrief.linkage.testing;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.DataSourceAnalysis;
import org.regenstrief.linkage.analysis.ScaleWeightAnalyzer;
import org.regenstrief.linkage.analysis.ScaleWeightModifier;
import org.regenstrief.linkage.io.DataBaseReader;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.io.OrderedCharDelimFileReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.ScorePair;
import org.regenstrief.linkage.util.XMLTranslator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Tests weight scaling functionality
 * Scheme (A)
 * @author scentel
 */

public class ScaleWeightAnalyzerTest {
	public static void main(String[] args) {
		File config = new File(args[0]);
		if(!config.exists()){
			System.out.println("config file does not exist, exiting");
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try{
			// Load the XML configuration file
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			RecMatchConfig rmc = XMLTranslator.createRecMatchConfig(doc);

			// Retrieve data sources for easier access
			LinkDataSource lds1 = rmc.getLinkDataSource1();
			LinkDataSource lds2 = rmc.getLinkDataSource2();
			ReaderProvider rp = new ReaderProvider();

			for(MatchingConfig mc_test : rmc) {

				Hashtable<String,Integer> type_table = lds1.getTypeTable();

				DataSourceReader dsr1, dsr2;				
				// create readers and a FormPairs object
				if(lds1.getType().equals("DataBase")) {
					dsr1 = (DataSourceReader)rp.getReader(lds1);
				} else {
					dsr1 = new OrderedCharDelimFileReader(lds1, mc_test);
				}

				if(lds2.getType().equals("DataBase")) {
					dsr2 = (DataSourceReader)rp.getReader(lds2);
				} else {
					dsr2 = new OrderedCharDelimFileReader(rmc.getLinkDataSource2(), mc_test);;
				}
								
				ScorePair sp = new ScorePair(mc_test);
					
				if(mc_test.get_is_scale_weight()) {
					DataSourceAnalysis dsa1 = new DataSourceAnalysis(dsr1);
					DataSourceAnalysis dsa2 = new DataSourceAnalysis(dsr2);
					String db_access = rmc.getAnalysis_configs().getInitString("scaleweight");
					ScaleWeightAnalyzer swa1 = new ScaleWeightAnalyzer(lds1, mc_test, db_access);
					ScaleWeightAnalyzer swa2 = new ScaleWeightAnalyzer(lds2, mc_test, db_access);

					dsa1.addAnalyzer(swa1);
					dsa2.addAnalyzer(swa2);

					dsa1.analyzeData();
					dsa2.analyzeData();
					
					ScaleWeightModifier swm = new ScaleWeightModifier(swa1, swa2);
					sp.addScoreModifier(swm);
				}	

				if(!mc_test.getName().equals("default")) {
					// Form pairs should come after analysis, because it modifies next_record of the readers
					org.regenstrief.linkage.io.FormPairs fp = new org.regenstrief.linkage.io.FormPairs(rp.getReader(rmc.getLinkDataSource1(), mc_test), rp.getReader(rmc.getLinkDataSource2(), mc_test), mc_test, type_table);

					// iterate through the Record pairs and print the score
					Record[] pair;
					int i = 0;
					while((pair = fp.getNextRecordPair()) != null) {
						Record r1 = pair[0];
						Record r2 = pair[1];
						double score = sp.scorePair(r1, r2).getScore();
						System.out.println("score: " + score);
						i++;
					}

					System.out.println("found " + i + " records that matched on the blocking field");
				}
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
