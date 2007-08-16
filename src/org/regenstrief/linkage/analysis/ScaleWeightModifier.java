package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.ScoreVector;
import org.regenstrief.linkage.db.ScaleWeightDBManager;
import org.regenstrief.linkage.db.ScaleWeightDBManager.CountType;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

/**
 * Changes scores of a MatchResult according to weight scaling formula
 * 
 * @author scentel
 * 
 * TODO: Test
 */

public class ScaleWeightModifier implements Modifier {

	private ScaleWeightAnalyzer swa1, swa2;

	// MatchingConfigRows that weight scaling will be used
	private ArrayList<MatchingConfigRow> sw_rows;

	// Tables of <token, frequency> stored in a table indexed by demographic (column label)
	private Hashtable<String, Hashtable<String, Integer>> lds1_frequencies;
	private Hashtable<String, Hashtable<String, Integer>> lds2_frequencies;

	// Table of included DataColumns indexed by demographic
	private Hashtable<String, DataColumn> lds1_inc_cols;
	private Hashtable<String, DataColumn> lds2_inc_cols;

	// DataSource IDs of where records come from
	private int lds1_id;
	private int lds2_id;
	
	// Connection to database where tokens are stored
	private static ScaleWeightDBManager sw_connection;

	public ScaleWeightModifier(ScaleWeightAnalyzer swa1, ScaleWeightAnalyzer swa2) {
		this.swa1 = swa1;
		this.swa2 = swa2;
	}

	public void initializeModifier() {

		MatchingConfig mc = swa1.getMatchingConfig();

		LinkDataSource lds1 = swa1.getLinkDataSource();
		LinkDataSource lds2 = swa2.getLinkDataSource();

		this.lds1_id = lds1.getDataSource_ID(); 
		this.lds2_id = lds2.getDataSource_ID();

		lds1_frequencies = new Hashtable<String, Hashtable<String,Integer>>();
		lds2_frequencies = new Hashtable<String, Hashtable<String,Integer>>();

		// Load previous analysis results for all scale weight columns
		// Note that column labels have to be same in two data sources
		Iterator<MatchingConfigRow> it = mc.getIncludedColumns().iterator();
		lds1_inc_cols = lds1.getIncludedDataColumns();
		lds2_inc_cols = lds2.getIncludedDataColumns();
		
		sw_connection = swa1.getSw_connection();

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

	/**
	 * Internal method used by ScorePair
	 * Collects all information needed for weight scaling 
	 * @param rec
	 * @param inc_cols
	 * @param frequencies
	 * @param datasource_id
	 * @return A hashtable indexed by demographic
	 */
	private Hashtable<String,SWAdjustScore> adjustScore(Record rec, Hashtable<String, DataColumn> inc_cols, Hashtable<String, Hashtable<String, Integer>> frequencies, int datasource_id, ScaleWeightAnalyzer swa) {
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
			int total_tokens = sw_connection.getCount(CountType.NonNull, cur_data_col, datasource_id);
			int unique_tokens = sw_connection.getCount(CountType.Unique, cur_data_col, datasource_id);
			SWAdjustScore adjust = new SWAdjustScore(total_tokens, unique_tokens, frequency);
			// we need this for all columns, so store it in a hashtable indexed by column name
			result.put(comparison_demographic, adjust);
		}

		return result;
	}

	public void modifyMatchResult(MatchResult mr, MatchingConfig mc) {
		// If there is at least one column that requires weight scaling
		if(mc.get_is_scale_weight()) {
			
			// Get individual scores for each column (table indexed by demographic)
			ScoreVector score_vector = mr.getScoreVector();
			
			// Collect all the information to calculate scaling factors
			Hashtable<String, SWAdjustScore> adjust1 = adjustScore(mr.getRecord1(), lds1_inc_cols, lds1_frequencies, lds1_id, swa1);
			Hashtable<String, SWAdjustScore> adjust2 = adjustScore(mr.getRecord2(), lds2_inc_cols, lds2_frequencies, lds2_id, swa2);
		
			Iterator<MatchingConfigRow> sv_iterator = sw_rows.iterator();
			while(sv_iterator.hasNext()) {
				MatchingConfigRow cur_row = sv_iterator.next();
				String cur_demographic = cur_row.getName();
				// If exact matching is used
				if(cur_row.getAlgorithm() == MatchingConfig.EXACT_MATCH) {
					// Calculate scaling factor obtained from two data sources
					SWAdjustScore adjustment = SWAdjustScore.sumTwoScores(adjust1.get(cur_demographic), adjust2.get(cur_demographic));
					// Adjust the score
					score_vector.setScore(cur_demographic, score_vector.getScore(cur_demographic) * adjustment.getScalingFactor());
				} 
			}
			
			mr.setScoreVector(score_vector);
		}
	}

}
