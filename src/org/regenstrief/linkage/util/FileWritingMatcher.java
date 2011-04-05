package org.regenstrief.linkage.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.RecordLink;
import org.regenstrief.linkage.SameEntityRecordGroup;
import org.regenstrief.linkage.analysis.NullDemographicScoreModifier;
import org.regenstrief.linkage.analysis.SetSimilarityAnalysis;
import org.regenstrief.linkage.analysis.VectorTable;
import org.regenstrief.linkage.db.SavedResultDBConnection;
import org.regenstrief.linkage.io.DedupOrderedDataSourceFormPairs;
import org.regenstrief.linkage.io.FormPairs;
import org.regenstrief.linkage.io.OrderedDataSourceFormPairs;
import org.regenstrief.linkage.io.OrderedDataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.matchresult.DBMatchResultStore;
import org.regenstrief.linkage.matchresult.MatchResultStore;

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
		return writeMatchResults(rmc, new File(OUT_FILE), true, false, false, false);
	}
	
	public static File writeMatchResults(RecMatchConfig rmc, File f, boolean write_xml, boolean write_db, boolean group_analysis, boolean vector_obs){
		
		// set output order based on include position in lds
		LinkDataSource lds = rmc.getLinkDataSource1();
		String[] order = new String[lds.getIncludeCount()];
		for(int i = 0; i < order.length; i++){
			DataColumn dc = lds.getDataColumnByIncludePosition(i);
			order[i] = dc.getName();
		}
		String id_field = lds.getUniqueID();
		
		Date report_time = new Date();
		
		try{
			//BufferedWriter fout = new BufferedWriter(new FileWriter(f));
			
			ReaderProvider rp = ReaderProvider.getInstance();
			
			// if diong a group analysis, then create list for all RecordLink objects from blocking runs
			List<RecordLink> all_links = null;
			if(group_analysis){
				all_links = new ArrayList<RecordLink>();
			}
			
			
			// iterate over each MatchingConfig
			List<MatchingConfig> mcs = rmc.getMatchingConfigs();
			Iterator<MatchingConfig> it = mcs.iterator();
			while(it.hasNext()){
				MatchingConfig mc = it.next();
				List<MatchResult> results = new ArrayList<MatchResult>();
				File f2 = new File(f.getPath() + "_" + mc.getName() + ".txt");
				BufferedWriter fout = new BufferedWriter(new FileWriter(f2));
				Connection db = null;
				
				// write db if needed
				MatchResultStore mrs = null;
				if(write_db){
					File db_file = new File(f.getPath() + ".db");
					db = SavedResultDBConnection.openDBResults(db_file);
					SavedResultDBConnection.createMatchResultTables(db);
					try{
						db.setAutoCommit(false);
					}
					catch(SQLException sqle){
						
					}
					mrs = new DBMatchResultStore(db);
					((DBMatchResultStore)mrs).setDate(report_time);
				}
				
				OrderedDataSourceReader odsr1 = rp.getReader(rmc.getLinkDataSource1(), mc);
				OrderedDataSourceReader odsr2 = rp.getReader(rmc.getLinkDataSource2(), mc);
				/*if(odsr1 != null && odsr2 != null){
					// analyze with EM
					org.regenstrief.linkage.io.FormPairs fp2 = new org.regenstrief.linkage.io.FormPairs(odsr1, odsr2, mc, rmc.getLinkDataSource1().getTypeTable());
					EMAnalyzer ema = new EMAnalyzer(rmc.getLinkDataSource1(), rmc.getLinkDataSource2(), mc);
					ema.analyzeRecordPairs(fp2, mc);
				}
				odsr1.close();
				odsr2.close();
				
				// create form pair object
				odsr1 = rp.getReader(rmc.getLinkDataSource1(), mc);
				odsr2 = rp.getReader(rmc.getLinkDataSource2(), mc);*/
				if(odsr1 != null && odsr2 != null){
				    FormPairs fp = null;
				    if (rmc.isDeduplication()) {
				        fp = new DedupOrderedDataSourceFormPairs(odsr1, mc, rmc.getLinkDataSource1().getTypeTable());
				    } else {
				        fp = new OrderedDataSourceFormPairs(odsr1, odsr2, mc, rmc.getLinkDataSource1().getTypeTable());
				    }
					
					ScorePair sp = new ScorePair(mc);
					
					// check if scoring needs to be modified
					if(mc.isNullScoring()){
						NullDemographicScoreModifier ndsm = new NullDemographicScoreModifier();
						sp.addScoreModifier(ndsm);
					}
					
					Record[] pair;
					int count = 0;
					while((pair = fp.getNextRecordPair()) != null){
						MatchResult mr = sp.scorePair(pair[0], pair[1]);
						//results.add(mr);
						
						// changed to write output line without sorting results first
						fout.write(getOutputLine(mr, order, id_field) + "\n");
						
						// add match result to db, if needed
						if(write_db && mrs != null){
							mrs.addMatchResult(mr, count);
						}
						
						// add to grouping list, if needed
						if(all_links != null){
							if(mr.getScore() >= mc.getScoreThreshold()){
								all_links.add(mr);
							}
						}
						
						count++;
					}
					
					if(mrs instanceof DBMatchResultStore){
						DBMatchResultStore dbmrs = (DBMatchResultStore)mrs;
						dbmrs.addIndexes();
						dbmrs.close();
					}
					
					try{
						if(write_db && db != null){
							db.commit();
							db.close();
						}
					}catch(SQLException sqle){
						
					}
					
					// sort results list, then print to fout
					Collections.sort(results, Collections.reverseOrder());
					Iterator<MatchResult> it2 = results.iterator();
					while(it2.hasNext()){
						MatchResult mr = it2.next();
						fout.write(getOutputLine(mr, order, id_field) + "\n");
					}
					
					// write to an xml file also, to test this new format
					if(write_xml){
						File xml_out = new File(f2.getPath() + ".xml");
						//XMLTranslator.writeXMLDocToFile(MatchResultsXML.resultsToXML(results), xml_out);
						// below method uses SAX to write XML file, so no memory problems like earlier method
						MatchResultsXML.resultsToXML(results, xml_out);
					}
					
					if(vector_obs){
						File vector_out = new File(f2.getPath() + "_vectors.txt");
						BufferedWriter v_out = new BufferedWriter(new FileWriter(vector_out));
						v_out.write("vector|score|true_prob|false_prob|expected|observed\n");
						Hashtable<MatchVector,Long> vectors = sp.getObservedVectors();
						Iterator<MatchVector> mv_it = vectors.keySet().iterator();
						VectorTable vt = new VectorTable(mc);
						while(mv_it.hasNext()){
							MatchVector mv_obs = mv_it.next();
							Long l = vectors.get(mv_obs);
							double score = vt.getScore(mv_obs);
							// expected = (true_probability * p * count) + (false_probability * (1 - p) * count
							double expected_true = vt.getMatchVectorTrueProbability(mv_obs) * mc.getP() * count;
							double expected_false = vt.getMatchVectorFalseProbability(mv_obs) * (1 - mc.getP()) * count;
							double expected = expected_true + expected_false;
							v_out.write("\"" + mv_obs + "\"|" + score + "|" +vt.getMatchVectorTrueProbability(mv_obs) + "|" + vt.getMatchVectorFalseProbability(mv_obs) + "|" + expected + "|" + l + "\n");
						}
						v_out.flush();
						v_out.close();
					}
				}
				
				
				
				fout.flush();
				fout.close();
			}
			
			if(group_analysis){
				// run group analysis and write results to files
				SetSimilarityAnalysis ssa = new SetSimilarityAnalysis();
				List<SameEntityRecordGroup> groups = ssa.getRecordGroups(all_links);
				
				File groups_out = new File(f.getPath() + "_groups.txt");
				BufferedWriter fout = new BufferedWriter(new FileWriter(groups_out));
				
				// iterate over members of groups List
				Iterator<SameEntityRecordGroup> groups_it = groups.iterator();
				while(groups_it.hasNext()){
					SameEntityRecordGroup entity = groups_it.next();
					int group_id = entity.getGroupID();
					
					List<RecordLink> links = entity.getGroupLinks();
					Iterator<RecordLink> group_links_it = links.iterator();
					while(group_links_it.hasNext()){
						RecordLink link = group_links_it.next();
						if(link instanceof MatchResult){
							MatchResult mr = (MatchResult)link;
							String link_output_line = Integer.toString(group_id) + "|" + getOutputLine(mr, order, id_field);
							fout.write(link_output_line + "\n");
						}
					}
				}
				fout.flush();
				fout.close();
				
				// write xml file if xml output is checked
				if(write_xml){
					File groups_xml_out = new File(f.getPath() + "_groups.xml");
					MatchResultsXML.groupsToXML(groups, groups_xml_out);
				}
			}
			
		}
		catch(IOException ioe){
			System.err.println("error writing linkage results: " + ioe.getMessage());
			return null;
		}
		
		return f;
	}
	
	private static String getOutputLine(MatchResult mr, String[] order, String id_field){
		String s = new String();
		s += mr.getScore();
		//Enumeration<String> demographics = mr.getRecord1().getDemographics().keys();
		Record r1 = mr.getRecord1();
		Record r2 = mr.getRecord2();
		s += "|" + r1.getUID() + "|" + r2.getUID();
		//while(demographics.hasMoreElements()){
		for(int i = 0; i < order.length; i++){
			String demographic = order[i];
			MatchingConfigRow mcr = mr.getMatchingConfig().getMatchingConfigRowByName(demographic);
			if(!mcr.getName().equals(id_field)){
				s += "|" + r1.getDemographic(demographic) + "|" + r2.getDemographic(demographic);
			}
			
		}
		return s;
	}
}
