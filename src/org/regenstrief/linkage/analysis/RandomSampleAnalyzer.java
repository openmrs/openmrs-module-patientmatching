package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.io.FormPairs;
import org.regenstrief.linkage.io.OrderedDataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
import org.regenstrief.linkage.util.StringMatch;


/**
 * Class analyzes two record sources to calculate new u values 
 * to use in matching.  To do this, it creates record pairs based on the
 * blocking columns.  It then randomly pairs records from both
 * data sources and compares the demographics, calculating the new u values
 * based on what percent the random pairings match.
 * 
 * 
 */

public class RandomSampleAnalyzer {
	public static final int SAMPLE_SIZE = 100000;
	
	LinkDataSource lds1, lds2;
	ReaderProvider rp;
	Random rand;
	
	List<int[]> index_pairs;
	
	public RandomSampleAnalyzer(LinkDataSource lds1, LinkDataSource lds2){
		this.lds1 = lds1;
		this.lds2 = lds2;
		rand = new Random();
		
		rp = new ReaderProvider();
		
		index_pairs = new ArrayList<int[]>();
	}
	
	/**
	 * Modify the given MatchingConfig u values with numbers
	 * determine by random sampling possible pairs from a
	 * FormPairs object a statistically significant number of times.
	 * 
	 * @param mc	the MatchingConfig object to modify using this analysis
	 */
	public void setUValues(MatchingConfig mc){
		int pair_count = countRecordPairs(mc);
		setIndexPairs(pair_count);
		
		Record[] recs1 = new Record[SAMPLE_SIZE];
		Record[] recs2 = new Record[SAMPLE_SIZE];
		
		// first need to create ordered readers and a FormPairs object to 
		// get the record pairs
		OrderedDataSourceReader reader1 = rp.getReader(lds1, mc);
		OrderedDataSourceReader reader2 = rp.getReader(lds2, mc);
		FormPairs fp = new FormPairs(reader1, reader2, mc, lds1.getTypeTable());
		
		Record[] pair;
		int count = 0;
		while((pair = fp.getNextRecordPair()) != null){
			for(int i = 0; i < SAMPLE_SIZE; i++){
				int[] set = index_pairs.get(i);
				if(set[0] == count){
					recs1[i] = pair[0];
				}
				if(set[1] == count){
					recs2[i] = pair[1];
				}
			}
			
			count++;
		}
		
		// initialize values to calculate u
		Hashtable<String,Integer> demographic_agree_count = new Hashtable<String,Integer>();
		
		for(int i = 0; i < SAMPLE_SIZE; i++){
			Record r1 = recs1[i];
			Record r2 = recs2[i];
			
			// compare the two records to modify u values
			HashSet<String> demographics = new HashSet<String>();
			demographics.addAll(r1.getDemographics().keySet());
			demographics.addAll(r2.getDemographics().keySet());
			Iterator<String> it = demographics.iterator();
			while(it.hasNext()){
				String demographic = it.next();
				Integer bucket = demographic_agree_count.get(demographic);
				if(bucket == null){
					// not keeping stats for this column yet
					demographic_agree_count.put(demographic, new Integer(0));
				}
				
				if(matchesOnDemographic(r1, r2, demographic, mc)){
					Integer non_match_count = demographic_agree_count.get(demographic);
					demographic_agree_count.put(demographic, new Integer(non_match_count.intValue() + 1));
				}
				
			}
		}
		
		// review totals and calculate u values
		// modify the matching config object to reflect calculated values
		Iterator<String> it = demographic_agree_count.keySet().iterator();
		while(it.hasNext()){
			String demographic = it.next();
			int agree_count = demographic_agree_count.get(demographic);
			double u_val = (double)agree_count/(double)SAMPLE_SIZE;
			if(u_val < EMAnalyzer.EM_ZERO){
				u_val = EMAnalyzer.EM_ZERO;
			} else if(u_val > EMAnalyzer.EM_ONE){
				u_val = EMAnalyzer.EM_ONE;
			}
			
			mc.getMatchingConfigRowByName(demographic).setNonAgreement(u_val);
		}
	}
	
	private boolean matchesOnDemographic(Record r1, Record r2, String demographic, MatchingConfig mc){
		String val1 = r1.getDemographic(demographic);
		String val2 = r2.getDemographic(demographic);
		
		MatchingConfigRow mcr = mc.getMatchingConfigRowByName(demographic);
		
		boolean match = false;
		if(val1 != null && val2 != null) {
			switch(mcr.getAlgorithm()){
			case(MatchingConfig.EXACT_MATCH):
				match = StringMatch.exactMatch(val1, val2);
			break;
			case(MatchingConfig.JWC):
				match = StringMatch.JWCMatch(val1, val2);
			break;
			case(MatchingConfig.LCS):
				match = StringMatch.LCSMatch(val1, val2);
			break;
			case(MatchingConfig.LEV):
				match = StringMatch.LEVMatch(val1, val2);
			break;
			}
		}
		
		return match;
	}
	
	private int countRecordPairs(MatchingConfig mc){
		OrderedDataSourceReader reader1 = rp.getReader(lds1, mc);
		OrderedDataSourceReader reader2 = rp.getReader(lds2, mc);
		FormPairs fp = new FormPairs(reader1, reader2, mc, lds1.getTypeTable());
		int pair_count = 0;
		while(fp.getNextRecordPair() != null){
			pair_count++;
		}
		reader1.close();
		reader2.close();
		
		return pair_count;
	}
	
	private void setIndexPairs(int max_index){
		index_pairs.clear();
		
		// need to get two sets of random numbers, one for each data source
		for(int i = 0; i < SAMPLE_SIZE; i++){
			int[] pair = new int[2];
			pair[0] = rand.nextInt(max_index);
			pair[1] = rand.nextInt(max_index);
			index_pairs.add(pair);
		}
	}
}
