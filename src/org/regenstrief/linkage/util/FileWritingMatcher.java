package org.regenstrief.linkage.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.EMAnalyzer;
import org.regenstrief.linkage.io.OrderedDataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;

/**
 * Purpose of class is to find matches between two data sources using all the
 * given MatchingConfigs and write a simple output file
 * 
 * This class is a temp measure until better output can be created
 * @author jegg
 *
 */

public class FileWritingMatcher {
	
	public static final String OUT_FILE = "linkage.out";
	
	public static File writeMatchResults(RecMatchConfig rmc){
		return writeMatchResults(rmc, new File(OUT_FILE));
	}
	
	public static File writeMatchResults(RecMatchConfig rmc, File f){
		
		try{
			BufferedWriter fout = new BufferedWriter(new FileWriter(f));
			ReaderProvider rp = new ReaderProvider();
			
			// iterate over each MatchingConfig
			List<MatchingConfig> mcs = rmc.getMatchingConfigs();
			Iterator<MatchingConfig> it = mcs.iterator();
			while(it.hasNext()){
				MatchingConfig mc = it.next();
				List<MatchResult> results = new ArrayList<MatchResult>();
				
				OrderedDataSourceReader odsr1 = rp.getReader(rmc.getLinkDataSource1(), mc);
				OrderedDataSourceReader odsr2 = rp.getReader(rmc.getLinkDataSource2(), mc);
				if(odsr1 != null && odsr2 != null){
					// analyze with EM
					org.regenstrief.linkage.io.FormPairs fp2 = new org.regenstrief.linkage.io.FormPairs(odsr1, odsr2, mc, rmc.getLinkDataSource1().getTypeTable());
					EMAnalyzer ema = new EMAnalyzer();
					ema.analyzeRecordPairs(fp2, mc);
				}
				odsr1.close();
				odsr2.close();
				
				// create form pair object
				odsr1 = rp.getReader(rmc.getLinkDataSource1(), mc);
				odsr2 = rp.getReader(rmc.getLinkDataSource2(), mc);
				if(odsr1 != null && odsr2 != null){
					org.regenstrief.linkage.io.FormPairs fp = new org.regenstrief.linkage.io.FormPairs(odsr1, odsr2, mc, rmc.getLinkDataSource1().getTypeTable());
					
					ScorePair sp = new ScorePair(mc);
					Record[] pair;
					while((pair = fp.getNextRecordPair()) != null){
						MatchResult mr = sp.scorePair(pair[0], pair[1]);
						results.add(mr);
					}
					
					// sort results list, then print to fout
					Collections.sort(results, Collections.reverseOrder());
					Iterator<MatchResult> it2 = results.iterator();
					while(it2.hasNext()){
						MatchResult mr = it2.next();
						fout.write(getOutputLine(mr) + "\n");
					}
					
					// write to an xml file also, to test this new format
					File xml_out = new File(f.getPath() + ".xml");
					XMLTranslator.writeXMLDocToFile(MatchResultsXML.resultsToXML(results), xml_out);
				}
			}
			fout.flush();
			fout.close();
		}
		catch(IOException ioe){
			System.err.println("error writing linkage results: " + ioe.getMessage());
			return null;
		}
		
		return f;
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
			//if(mcr.isIncluded() || mcr.getBlockOrder() > 0){
				s += "|" + r1.getDemographic(demographic) + "|" + r2.getDemographic(demographic);
			//}
		}
		return s;
	}
}
