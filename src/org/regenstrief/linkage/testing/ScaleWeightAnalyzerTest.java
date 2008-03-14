package org.regenstrief.linkage.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.ModifiedMatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.DataSourceAnalysis;
import org.regenstrief.linkage.analysis.EMAnalyzer;
import org.regenstrief.linkage.analysis.PairDataSourceAnalysis;
import org.regenstrief.linkage.analysis.ScaleWeightAnalyzer;
import org.regenstrief.linkage.analysis.ScaleWeightModifier;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.io.OrderedCharDelimFileReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
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
				// create output file for this MatchingConfig
				File out_file = new File("output_" + mc_test.getName() + ".txt");
				BufferedWriter fout = new BufferedWriter(new FileWriter(out_file));
				
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
								
				org.regenstrief.linkage.io.FormPairs fp2 = new org.regenstrief.linkage.io.FormPairs(rp.getReader(rmc.getLinkDataSource1(), mc_test), rp.getReader(rmc.getLinkDataSource2(), mc_test), mc_test, type_table);
				EMAnalyzer ema = new EMAnalyzer(lds1, lds2, mc_test);
				PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(fp2);
				pdsa.addAnalyzer(ema);
				pdsa.analyzeData();
				
				ScorePair sp = new ScorePair(mc_test);
					
				if(mc_test.get_is_scale_weight()) {
					DataSourceAnalysis dsa1 = new DataSourceAnalysis(dsr1);
					DataSourceAnalysis dsa2 = new DataSourceAnalysis(dsr2);
					String db_access = rmc.getAnalysis_configs().getInitString("scaleweight");
					ScaleWeightAnalyzer swa1 = new ScaleWeightAnalyzer(lds1, mc_test, db_access);
					ScaleWeightAnalyzer swa2 = new ScaleWeightAnalyzer(lds2, mc_test, db_access);

					dsa1.addAnalyzer(swa1);
					dsa2.addAnalyzer(swa2);
					
					System.out.println(new java.util.Date() + ":  starting to analyze first data source");
					//dsa1.analyzeData();
					System.out.println(new java.util.Date() + ":  starting to analyze second data source");
					//dsa2.analyzeData();
					
					ScaleWeightModifier swm = new ScaleWeightModifier(swa1, swa2);
					swm.initializeModifier();
					sp.addScoreModifier(swm);
				}	
				
				System.out.println(new java.util.Date() + ":  starting to get record pairs");
				ArrayList<MatchResult> results = new ArrayList<MatchResult>();
				//if(mc_test.getName().equals("2")) {
					// Form pairs should come after analysis, because it modifies next_record of the readers
					org.regenstrief.linkage.io.FormPairs fp = new org.regenstrief.linkage.io.FormPairs(rp.getReader(rmc.getLinkDataSource1(), mc_test), rp.getReader(rmc.getLinkDataSource2(), mc_test), mc_test, type_table);
					
					
					
					// iterate through the Record pairs and print the score
					Record[] pair;
					int i = 0;
					while((pair = fp.getNextRecordPair()) != null) {
						Record r1 = pair[0];
						Record r2 = pair[1];
						MatchResult mr = sp.scorePair(r1, r2);
						results.add(mr);
						String match_details;
						if(mr instanceof ModifiedMatchResult){
							match_details = getOutputLine((ModifiedMatchResult)mr);
						} else {
							match_details = getOutputLine(mr);
						}
						
						//System.out.println(match_details);
						fout.write(match_details + "\n");
						i++;
						if(i % 10000 == 0){
							System.out.println(new Date() + ": read pair " + i);
						}
					}

					System.out.println("found " + i + " records that matched on the blocking field");
				//}
				
				//Document d = MatchResultsXML.resultsToXML(results);
				//XMLTranslator.writeXMLDocToFile(d, new File("test output.xml"));
				//System.out.println(new java.util.Date() + ":  finsihed");
				fout.flush();
				fout.close();
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
	
	private static String getOutputLine(MatchResult mr){
		String s = new String();
		s += mr.getScore();
		Enumeration<String> demographics = mr.getRecord1().getDemographics().keys();
		Record r1 = mr.getRecord1();
		Record r2 = mr.getRecord2();
		while(demographics.hasMoreElements()){
			String demographic = demographics.nextElement();
			MatchingConfigRow mcr = mr.getMatchingConfig().getMatchingConfigRowByName(demographic);
			if(mcr.isIncluded() || mcr.getBlockOrder() > 0){
				s += "|" + r1.getDemographic(demographic) + "|" + r2.getDemographic(demographic);
			}
		}
		return s;
	}
	
	private static String getOutputLine(ModifiedMatchResult mr){
		String s = new String();
		s += "records";
		Enumeration<String> demographics = mr.getRecord1().getDemographics().keys();
		Record r1 = mr.getRecord1();
		Record r2 = mr.getRecord2();
		while(demographics.hasMoreElements()){
			String demographic = demographics.nextElement();
			MatchingConfigRow mcr = mr.getMatchingConfig().getMatchingConfigRowByName(demographic);
			//if(mcr.isIncluded() || mcr.getBlockOrder() > 0){
				s += "|" + r1.getDemographic(demographic) + "|" + r2.getDemographic(demographic);
			//}
		}
		s += "\n";
		s += mr.getBaseScore() + "|" + mr.getBasicMatchResult().getScoreVector();
		s+= "\n";
		s += mr.getScore() + "|" + mr.getScoreVector();
		return s;
	}
}
