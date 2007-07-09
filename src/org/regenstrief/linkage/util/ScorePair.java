package org.regenstrief.linkage.util;

/**
 * Takes a record and calculates a score between them using the options in the
 * given MatchingConfig object
 * 
 * TODO: Add size parameter to lds1_frequencies & lds2_frequencies
 */

import org.regenstrief.linkage.*;
import org.regenstrief.linkage.analysis.*;
import org.regenstrief.linkage.db.ScaleWeightDBManager;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import java.util.*;

public class ScorePair {
	private VectorTable vt;
	private MatchingConfig mc;
	
	// Tables of <token, frequency> stored in a table indexed by demographic (column label)
	private Hashtable<String, Hashtable<String, Integer>> lds1_frequencies;
	private Hashtable<String, Hashtable<String, Integer>> lds2_frequencies;
	
	// Table of included DataColumns indexed by demographic
	private Hashtable<String, DataColumn> lds1_inc_cols;
	private Hashtable<String, DataColumn> lds2_inc_cols;
	
	// DataSource IDs of where records come from
	private String lds1_id;
	private String lds2_id;
	
	// MatchingConfigRows that weight scaling will be used
	private ArrayList<MatchingConfigRow> sw_rows;

	// Database connection to retrieve token frequencies
	private ScaleWeightDBManager sw_connection;
	
	/**
	 * Constructor when weight scaling is NOT used
	 * @param mc
	 */
	public ScorePair(MatchingConfig mc){
		this.mc = mc;
		vt = new VectorTable(mc);
	}
	
	/**
	 * Constructor when weight scaling is used
	 * @param rmc
	 * @param sw_connection Connection should already be open using connect()
	 */
	public ScorePair(MatchingConfig mc, RecMatchConfig rmc, ScaleWeightDBManager sw_connection){
		this.mc = mc;
		vt = new VectorTable(mc);
		this.sw_connection = sw_connection;
		
		this.lds1_id = rmc.getLinkDataSource1().getDataSource_ID() + ""; 
		this.lds2_id = rmc.getLinkDataSource2().getDataSource_ID() + "";
		
		lds1_frequencies = new Hashtable<String, Hashtable<String,Integer>>();
		lds2_frequencies = new Hashtable<String, Hashtable<String,Integer>>();
		
		// Load previous analysis results for all scale weight columns
		// Note that column labels have to be same in two data sources
		Iterator<MatchingConfigRow> it = mc.getIncludedColumns().iterator();
		lds1_inc_cols =  rmc.getLinkDataSource1().getIncludedDataColumns();
		lds2_inc_cols = rmc.getLinkDataSource2().getIncludedDataColumns();
		
		// In the worst case, all included row are scale weight
		sw_rows = new ArrayList<MatchingConfigRow>(lds1_inc_cols.size());
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			// For starters, only do weight scaling if it uses exact matching
			if(mcr.isScaleWeight() && mcr.getAlgorithm() == MatchingConfig.EXACT_MATCH) {
				sw_rows.add(mcr);
				String col_label = mcr.getName();
				// Retrieve previous token frequency analysis results
				Hashtable<String, Integer> table1 = sw_connection.getTokenFrequenciesFromDB(lds1_inc_cols.get(col_label), lds1_id, mcr.getSw_settings(), mcr.getSw_number());
				Hashtable<String, Integer> table2 = sw_connection.getTokenFrequenciesFromDB(lds2_inc_cols.get(col_label), lds2_id, mcr.getSw_settings(), mcr.getSw_number());
				// Store them in hash tables indexed by demographic
				lds1_frequencies.put(col_label, table1);
				lds2_frequencies.put(col_label, table2);
			}
		}
	}
	
	public MatchResult scorePair(Record rec1, Record rec2){
		MatchVector mv = new MatchVector();
		List<MatchingConfigRow> config_rows = mc.getIncludedColumns();
		Iterator<MatchingConfigRow> it = config_rows.iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			String comparison_demographic = mcr.getName();
			String data1 = rec1.getDemographic(comparison_demographic);
			String data2 = rec2.getDemographic(comparison_demographic);
			
			boolean match = false;
			if(data1 != null && data2 != null) {
				switch(mcr.getAlgorithm()){
				case(MatchingConfig.EXACT_MATCH):
					match = StringMatch.exactMatch(data1, data2);
					break;
				case(MatchingConfig.JWC):
					match = StringMatch.JWCMatch(data1, data2);
					break;
				case(MatchingConfig.LCS):
					match = StringMatch.LCSMatch(data1, data2);
					break;
				case(MatchingConfig.LEV):
					match = StringMatch.LEVMatch(data1, data2);
					break;
				}
			}
			
			mv.setMatch(comparison_demographic, match);
		}
		MatchResult mr;
		
		// If there is at least one column that requires weight scaling
		if(mc.get_is_scale_weight()) {
			// Get individual scores for each column (table indexed by demographic)
			Hashtable<String, Double> score_vector = vt.getScoreVector(mv);
			// Collect all the information to calculate scaling factors
			Hashtable<String, SWAdjustScore> adjust1 = adjustScore(rec1, lds1_inc_cols, lds1_frequencies, lds1_id);
			Hashtable<String, SWAdjustScore> adjust2 = adjustScore(rec2, lds2_inc_cols, lds2_frequencies, lds2_id);

			// Sum up the scores in score vector
			double total_score = 0;
			Iterator<MatchingConfigRow> sv_iterator = config_rows.iterator();
			while(sv_iterator.hasNext()) {
				MatchingConfigRow cur_row = sv_iterator.next();
				String cur_demographic = cur_row.getName();
				// If this is a weight scaling column
				if(cur_row.isScaleWeight() && cur_row.getAlgorithm() == MatchingConfig.EXACT_MATCH) {
					// Calculate scaling factor obtained from two data sources
					SWAdjustScore adjustment = SWAdjustScore.sumTwoScores(adjust1.get(cur_demographic), adjust2.get(cur_demographic));
					// Adjust the score
					total_score += score_vector.get(cur_demographic) * adjustment.getScalingFactor();
				} else {
					// Use the old score for non weight scaling columns
					total_score += score_vector.get(cur_demographic);
				}
			}
			mr = new MatchResult(total_score,vt.getMatchVectorTrueProbability(mv),vt.getMatchVectorFalseProbability(mv),vt.getSensitivity(mv),vt.getSpecificity(mv),mv,rec1,rec2);
		}
		else {
			mr = new MatchResult(vt.getScore(mv),vt.getMatchVectorTrueProbability(mv),vt.getMatchVectorFalseProbability(mv),vt.getSensitivity(mv),vt.getSpecificity(mv),mv,rec1,rec2);
		}

		return mr;
	}
	
	/**
	 * Internal method used by ScorePair
	 * Collects all information needed for weight scaling 
	 * @param rec
	 * @param inc_cols
	 * @param frequencies
	 * @param datasource_id
	 * @return A hashtable indexed by demographic
	 */
	private Hashtable<String,SWAdjustScore> adjustScore(Record rec, Hashtable<String, DataColumn> inc_cols, Hashtable<String, Hashtable<String, Integer>> frequencies, String datasource_id) {
		Hashtable<String, SWAdjustScore> result = new Hashtable<String, SWAdjustScore>(2 * sw_rows.size());
		// For each weight scaling column
		Iterator<MatchingConfigRow> sw_it = sw_rows.iterator();
		while(sw_it.hasNext()) {
			MatchingConfigRow mcr = sw_it.next();
			String comparison_demographic = mcr.getName();
			DataColumn cur_data_col = inc_cols.get(comparison_demographic); 
			String token = rec.getDemographic(comparison_demographic);
			// frequencies for the current column
			Hashtable<String, Integer> token_frequencies = frequencies.get(comparison_demographic);
			// find the frequency of our token
			int frequency;
			try {
				 frequency = token_frequencies.get(token).intValue();
			} catch(NullPointerException e) {
				// It is not in the lookup table, have to check the database
				frequency = sw_connection.getTokenFrequencyFromDB(cur_data_col,datasource_id, token);
			}
			// other information needed for weight scaling
			int total_tokens = cur_data_col.getNonNullCount();
			int unique_tokens = cur_data_col.getUnique_non_null();
			SWAdjustScore adjust = new SWAdjustScore(total_tokens, unique_tokens, frequency);
			// we need this for all columns, so store it in a hashtable indexed by column name
			result.put(comparison_demographic, adjust);
		}
		
		return result;
	}
}