package org.regenstrief.linkage.db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.regenstrief.linkage.analysis.DataSourceAnalysis;
import org.regenstrief.linkage.analysis.EMAnalyzer;
import org.regenstrief.linkage.analysis.PairDataSourceAnalysis;
import org.regenstrief.linkage.analysis.ScaleWeightAnalyzer;
import org.regenstrief.linkage.analysis.ScaleWeightModifier;
import org.regenstrief.linkage.analysis.ScaleWeightModifier.ModifySet;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.ScorePair;
import org.regenstrief.linkage.util.XMLTranslator;
import org.regenstrief.linkage.util.MatchingConfigRow.ScaleWeightSetting;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;



/**
 * This class performs database operations of a weight scaling analyzer
 * 
 * @author scentel
 * 
 * TODO: Change DataColumn parameters to String
 * TODO: Time part of analysis date is missing, convert to DateTime
 * TODO: (If necessary) Add parameters for table names to the XML config
 */

public class ScaleWeightDBManager extends DBManager {
	
	// Different types of token counts: 
	// # of null tokens, # of non-null tokens, # of unique tokens
	public enum CountType { Null, NonNull, Unique };
	
	// Constants
	private static final String token_table = "patientmatching_token";
	private static final String analyses_table = "patientmatching_analysis";
	private static final String fields_table = "patientmatching_field";
	
	private static final String UNION_FREQ_QUERY = "select c.token, sum(frequency) from (select token, frequency from " + fields_table + " as a, " + token_table + " as b where a.label = ? and a.column_id = b.column_id group by token, frequency ) as c group by c.token;";
	private static final String UNION_FREQ_THRESHOLD_ABOVE_QUERY = "select token, sum from (select c.token, sum(frequency) from (select token, frequency from " + fields_table + " as a, " + token_table + " as b where a.label = ? and a.column_id = b.column_id group by token, frequency ) as c group by c.token) as d where sum > ?;";
	private static final String UNION_FREQ_THRESHOLD_BELOW_QUERY = "select token, sum from (select c.token, sum(frequency) from (select token, frequency from " + fields_table + " as a, " + token_table + " as b where a.label = ? and a.column_id = b.column_id group by token, frequency ) as c group by c.token) as d where sum < ?;";
	PreparedStatement union_freq_stmt, union_threshold_stmt;
	
	// hashtable stores the frequencies for each demographic when the data sources
	// in the frequency table is unioned.  Eventually, should add support for
	// multiple data sources
	private Hashtable<String,Hashtable<String,Integer>> union_values;
	
	// hashtable stores a list of strings that the UNION_FREQ_THRESHOLD_QUERY returns for
	// a given demographic, percentile value, and ModifySet
	Hashtable<String,Hashtable<Integer,Hashtable<ModifySet,List<String>>>> percentile_tokens;
	
	public ScaleWeightDBManager(String driver, String url, String user, String passwd){
		super(driver, url, user, passwd);
		union_values = new Hashtable<String,Hashtable<String,Integer>>();
		percentile_tokens = new Hashtable<String,Hashtable<Integer,Hashtable<ModifySet,List<String>>>>();
	}

	/**
	 * Checks if the token frequency exists in the database
	 * Updates the frequency or inserts a new record depending on the result
	 * 
	 * @param target_column
	 * @param datasource_id
	 * @param token
	 * @param frequency
	 */
	public void addOrUpdateToken(DataColumn target_column, int datasource_id, String token, Integer frequency) {
		int db_frequency = getTokenFrequencyFromDB(target_column,datasource_id, token);
		// Database and memory are at the same state, we don't need to do anything
		if(db_frequency != frequency) {
			// New record, not in the database
			if(frequency == 1 || db_frequency == 0) {
				insertToken(target_column, datasource_id, token, frequency);
			}
			else {
				updateTokenFrequency(target_column, datasource_id, token, frequency);
			}
		}
	}

	/**
	 * 
	 * @param target_column
	 * @param datasource_id
	 * @return The number of unique tokens in a DataColumn
	 */
	public int getDistinctRecordCount(DataColumn target_column, int datasource_id) {
		String query = "SELECT COUNT(token) FROM " + token_table + " WHERE datasource_id = " + datasource_id + " AND column_id = " + "'" + target_column.getColumnID() +"'";
		return executeQuery(query);
	}

	/**
	 * Empties the token table belonging
	 * @param target_column
	 * @param datasource_id
	 * @return
	 */
	public boolean deleteAnalysis(DataColumn target_column, int datasource_id) {
		String query = "DELETE FROM " + token_table + " WHERE datasource_id = " + datasource_id;
		return executeUpdate(query);
	}

	/**
	 * Inserts a new token with its frequency into where frequencies are stored
	 * 
	 * @param field The DataColumn that this token belongs
	 * @param datasource_id DataSource ID
	 * @param token
	 * @param frequency
	 * @return Whether the insert was successful or not
	 */
	public boolean insertToken(DataColumn field, int datasource_id, String token, int frequency){
		String query = "INSERT INTO " + token_table +  " VALUES (" + datasource_id + "," + "'" + field.getColumnID() + "'" + ",'" + token + "'," + frequency + ")"; 
		return executeUpdate(query);
	}


	/**
	 * Loads precalculated token frequencies from he database
	 * 
	 * @param target_column
	 * @param datasource_id
	 * @param topbottom Determines which values will be loaded into the hash table
	 * @param limit The parameter N, should be between 0.0 and 1.0 for percentages
	 * @return A hashtable containing frequencies, indexed by token
	 */
	public Hashtable<String,Integer> getTokenFrequenciesFromDB(DataColumn target_column, int datasource_id, ScaleWeightSetting topbottom, Float limit) {
		StringBuilder query = new StringBuilder("SELECT token, frequency FROM " + token_table + " WHERE datasource_id = " + datasource_id + " AND column_id = '" + target_column.getColumnID() +"'");
		Integer N = Math.round(limit);
		switch (topbottom) {
		case BottomN:
			query.append(" ORDER BY frequency ASC LIMIT " + N);
			break;
		case TopN:
			query.append(" ORDER BY frequency DESC LIMIT " + N);
			break;
		case TopNPercent:
			// Maybe an exception here?
			if(N <= 1) {
				int tokens = getDistinctRecordCount(target_column, datasource_id);
				int token_limit = Math.round(tokens*limit);
				query.append(" ORDER BY frequency DESC LIMIT " + token_limit);
			}
			else {
				System.out.println("Error: N should be between 0 and 1");
			}
			break;
		case BottomNPercent:
			if(N <= 1) {
				int tokens = getDistinctRecordCount(target_column, datasource_id);
				int token_limit = Math.round(tokens*limit);
				query.append(" ORDER BY frequency ASC LIMIT " + token_limit);
			}
			else {
				System.out.println("Error: N should be between 0 and 1");
			}
			break;
		case AboveN:
			query.append(" AND frequency > " + N);
			break;
		case BelowN:
			query.append(" AND frequency < " + N);
			break;
		}

		ResultSet frequency_rs = getResultSet(query.toString());
		Hashtable<String,Integer> frequencies = new Hashtable<String,Integer>(2*N);
		try {
			while(frequency_rs != null && frequency_rs.next()) {
				String token = frequency_rs.getString(1);
				Integer frequency = frequency_rs.getInt(2);
				frequencies.put(token, frequency);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return frequencies;

	}

	/**
	 * Retrieves a token frequency from the database
	 * @param field The DataColumn that this token belongs
	 * @param id DataSource ID
	 * @param token
	 * @return
	 */
	public int getTokenFrequencyFromDB(DataColumn field, int id, String token) {
		String query = "SELECT frequency FROM " + token_table + " WHERE token = '" + token + "' AND datasource_id = " + id + " AND column_id = " + "'" + field.getColumnID() +"'";
		try{
			Statement stmt = db.createStatement();
			ResultSet rows = stmt.executeQuery(query);
			if(rows.next()) {
				int frequency = rows.getInt(1);
				// Check if more than one row is returned
				if(!rows.next()) {
					// Return frequency if only one row is returned
					return frequency;
				}
				else {
					// If more than one row is returned, it means that there is something wrong
					rows.close();
					return -1;
				}
			}
			// ResultSet is empty, it means that token is not in the database
			else {
				return 0;
			}
		}
		catch (Exception e) {
			return -1;
		}

	}

	/**
	 * Updates the frequency of a token in the database
	 * 
	 * @param field DataColumn that the token belongs
	 * @param id DataSource ID
	 * @param token
	 * @param frequency
	 * @return If the update was successful or not
	 */

	public boolean updateTokenFrequency(DataColumn field, int id, String token, int frequency) {
		String query = "UPDATE " + token_table + " SET frequency = " + frequency + " WHERE datasource_id = " + id + " AND column_id = '" + field.getColumnID() + "' AND token = '" + token + "'";
		return executeUpdate(query);
	}

	/**
	 * Creates an analysis entry for a data column
	 * @param type
	 * @param target_col
	 * @param ds_id
	 * @param count
	 * @return
	 */
	public boolean insertCount(CountType type, DataColumn target_col, int ds_id, int count) {
		Date now = new Date(System.currentTimeMillis());
		PreparedStatement pstmt;
		try {
			pstmt = db.prepareStatement("INSERT INTO " + fields_table + "(column_id, datasource_id, label, unique_count, null_count, entropy, date_changed, non_null_count) VALUES(?,?,?,?,?,?,?,?)");
			pstmt.setString(1, target_col.getColumnID());
			pstmt.setInt(2, ds_id);
			pstmt.setString(3, target_col.getName());
			pstmt.setNull(4, Types.INTEGER);
			pstmt.setNull(5, Types.INTEGER);
			pstmt.setNull(8, Types.INTEGER);
			pstmt.setNull(6, Types.FLOAT);
			pstmt.setDate(7, now);

			if(type == CountType.NonNull) {
				pstmt.setInt(8, count);
			} else if(type == CountType.Unique) {
				pstmt.setInt(4, count);
			} else {
				pstmt.setInt(5, count);
			}

			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Creates or updates a specified token count in db
	 * 
	 * @param type
	 * @param target_col
	 * @param ds_id
	 * @param count
	 * @return
	 */
	public boolean setCount(CountType type, DataColumn target_col, int ds_id, int count) {
		PreparedStatement pstmt;
		int previous_count = getCount(type, target_col, ds_id);
		
		// no previous record
		if(previous_count == -1) {
			return insertCount(type, target_col, ds_id, count);
			// there is a previous record
		} else {
			return updateCount(type, target_col, ds_id, count);
		}
	}
	
	/**
	 * Internal method to update token counts
	 * @param type
	 * @param target_col
	 * @param ds_id
	 * @param count
	 * @return
	 */
	private boolean updateCount(CountType type, DataColumn target_col, int ds_id, int count) {
		PreparedStatement pstmt;
		try {
			if(type == CountType.NonNull) {
				pstmt = db.prepareStatement("UPDATE " + fields_table + " SET non_null_count = ? WHERE datasource_id = ? AND column_id = ?");
			} else if(type == CountType.Unique) {
				pstmt = db.prepareStatement("UPDATE " + fields_table + " SET unique_count = ? WHERE datasource_id = ? AND column_id = ?");
			} else {
				pstmt = db.prepareStatement("UPDATE " + fields_table + " SET null_count = ? WHERE datasource_id = ? AND column_id = ?");
			}

			pstmt.setInt(1, count);
			pstmt.setInt(2, ds_id);
			pstmt.setString(3, target_col.getColumnID());
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Returns the requested token count from db
	 * @param type
	 * @param target_col
	 * @param ds_id
	 * @return
	 */
	public int getCount(CountType type, DataColumn target_col, int ds_id) {
		PreparedStatement pstmt;
		try {
			if(type == CountType.NonNull) {
				pstmt = db.prepareStatement("SELECT non_null_count FROM  " + fields_table + " WHERE datasource_id = ? AND column_id = ?");
			} else if(type == CountType.Unique) {
				pstmt = db.prepareStatement("SELECT unique_count FROM " + fields_table + " WHERE datasource_id = ? AND column_id = ?");
			} else {
				pstmt = db.prepareStatement("SELECT null_count FROM " + fields_table + " WHERE datasource_id = ? AND column_id = ?");
			}

			pstmt.setInt(1, ds_id);
			pstmt.setString(2, target_col.getColumnID());
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			// datasource does not exists
			else {
				return -1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}	
	}

	/**
	 * Returns the number of records in a data source
	 * @param ds_id
	 * @return
	 */
	public int getRecordCount(int ds_id) {
		PreparedStatement pstmt;
		try {
			pstmt = db.prepareStatement("SELECT record_count FROM " + analyses_table + " WHERE datasource_id = ?");
			pstmt.setInt(1, ds_id);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			// datasource does not exists
			else {
				return -1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Internal method to create an entry for a data source in the analysis table
	 * @param ds_id
	 * @param name
	 * @param count
	 * @return
	 */
	private boolean insertRecordCount(int ds_id, String name, int count) {
		PreparedStatement pstmt;
		try {
			pstmt = db.prepareStatement("INSERT INTO " + analyses_table + "(datasource_id, name, record_count) VALUES (?,?,?)");
			pstmt.setInt(1, ds_id);
			pstmt.setString(2, name);
			pstmt.setInt(3, count);
			return pstmt.executeUpdate() == 1;
		} catch(SQLException e) {
			return false;
		}
	}

	/**
	 * Internal method to update record counts
	 * 
	 * @param ds_id
	 * @param count
	 * @return Returns true if one row was affected
	 */
	private boolean updateRecordCount(int ds_id, int count) {
		PreparedStatement pstmt;
		try {
			pstmt = db.prepareStatement("UPDATE " + analyses_table + " SET record_count = ? WHERE datasource_id = ?");
			pstmt.setInt(1, count);
			pstmt.setInt(2, ds_id);
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Stores in db, the number of records in a data source
	 * @param ds_id
	 * @param count
	 * @param ds_name
	 * @return
	 */
	public boolean setRecordCount(int ds_id, int count, String ds_name) {
		int previous_count = getRecordCount(ds_id);
		// does not exists
		if(previous_count == -1) {
			return insertRecordCount(ds_id, ds_name, count);
		} else {
			return updateRecordCount(ds_id, count);
		}
	}
	
	/**
	 * Method returns information on the frequency of unique tokens over combined
	 * data sources.  For example, frequencies of {'a'=3,'b'=2} and {'a'=1,'c'=4}
	 * in two different data sources would have a frequency of {'a'=4,'b'=2,'c'=4}
	 * 
	 * @param demographic	the analyzed demographic of interest
	 * @return	a hashtable indexed on token, pointing to the frequency count of that value
	 */
	public Hashtable<String,Integer> unionUniqueTokens(String demographic){
		Hashtable<String,Integer> ret = union_values.get(demographic);
		
		if(ret == null){
			if(union_freq_stmt == null){
				try{
					union_freq_stmt = db.prepareStatement(UNION_FREQ_QUERY);
				}catch(SQLException sqle){
					return null;
				}
			}
			
			ResultSet rs = null;
			try{
				ret = new Hashtable<String,Integer>();
				union_freq_stmt.setString(1, demographic);
				rs = union_freq_stmt.executeQuery();
				while(rs.next()){
					String dem = rs.getString(1);
					int freq = rs.getInt(2);
					ret.put(dem, freq);
				}
				union_values.put(demographic, ret);
				
			}
			catch(SQLException sqle){
				ret = null;
			}
			
		}
		
		return ret;
	}
	
	public boolean aboveAverageFrequency(String demographic, String value){
		Hashtable<String,Integer> freqs = unionUniqueTokens(demographic);
		Enumeration<String> e = freqs.keys();
		int sum = 0;
		while(e.hasMoreElements()){
			String token = e.nextElement();
			int freq = freqs.get(token);
			sum += freq;
		}
		double avg = sum / freqs.keySet().size();
		
		e = freqs.keys();
		while(e.hasMoreElements()){
			String token = e.nextElement();
			int freq = freqs.get(token);
			if(freq > avg && token.equals(value)){
				return true;
			}
		}
		return false;
	}
	
	public boolean belowAverageFrequency(String demographic, String value){
		Hashtable<String,Integer> freqs = unionUniqueTokens(demographic);
		Enumeration<String> e = freqs.keys();
		int sum = 0;
		while(e.hasMoreElements()){
			String token = e.nextElement();
			int freq = freqs.get(token);
			sum += freq;
		}
		double avg = sum / freqs.keySet().size();
		
		e = freqs.keys();
		while(e.hasMoreElements()){
			String token = e.nextElement();
			int freq = freqs.get(token);
			if(freq < avg && token.equals(value)){
				return true;
			}
		}
		return false;
	}
	
	public boolean inPercentileRange(String demographic, String value, int percentile, ModifySet m){
		Hashtable<String,Integer> demographic_frequencies = unionUniqueTokens(demographic);
		double[] frequencies = new double[demographic_frequencies.size()];
		Enumeration<String> e = demographic_frequencies.keys();
		int i = 0;
		while(e.hasMoreElements()){
			String key = e.nextElement();
			int count = demographic_frequencies.get(key);
			frequencies[i++] = count;
		}
		
		Hashtable<Integer,Hashtable<ModifySet,List<String>>> demographic_table = percentile_tokens.get(demographic);
		if(demographic_table == null){
			demographic_table = new Hashtable<Integer,Hashtable<ModifySet,List<String>>>();
			percentile_tokens.put(demographic, demographic_table);
		}
		
		Hashtable<ModifySet,List<String>> set_table = demographic_table.get(percentile);
		if(set_table == null){
			set_table = new Hashtable<ModifySet,List<String>>();
			demographic_table.put(percentile, set_table);
		}
		
		List<String> tokens = set_table.get(m);
		String query;
		if(tokens == null){
			tokens = new ArrayList<String>();
			set_table.put(m, tokens);
			if(m == ModifySet.ABOVE){
				query = UNION_FREQ_THRESHOLD_ABOVE_QUERY;
			} else {
				query = UNION_FREQ_THRESHOLD_BELOW_QUERY;
			}
			
			try{
				union_threshold_stmt = db.prepareStatement(query);
			}catch(SQLException sqle){
				return false;
			}
			
			
			Percentile p = new Percentile();
			double threshold = p.evaluate(frequencies, percentile);
				
			ResultSet rs = null;
			try{
				union_threshold_stmt.setString(1, demographic);
				union_threshold_stmt.setDouble(2, threshold);
				rs = union_threshold_stmt.executeQuery();
				while(rs.next()){
					String t = rs.getString(1);
					tokens.add(t);
				}
				set_table.put(m, tokens);
			}
			catch(SQLException sqle){
				return false;
			}
		
		}
		
		boolean valid_token = tokens.contains(value);
		return valid_token;
	}
	
	public boolean doesTableExist(String table_name) {
		DatabaseMetaData dbm;
		try {
			dbm = db.getMetaData();
			ResultSet tables = dbm.getTables(null, null	,table_name, null);
			// exists
			if(tables.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/*
	 * test class just to see how frequencies and percentiles work with currently loaded frequencies
	 */
	public static void main(String[] args){
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
			
			MatchingConfig mc_test = rmc.getMatchingConfigs().get(0);
			String db_access = rmc.getAnalysis_configs().getInitString("scaleweight");
			ScaleWeightAnalyzer swa1 = new ScaleWeightAnalyzer(lds1, mc_test, db_access);
			ScaleWeightAnalyzer swa2 = new ScaleWeightAnalyzer(lds2, mc_test, db_access);
			
			String[] init = db_access.split(",");
			ScaleWeightDBManager swdbm = new ScaleWeightDBManager(init[0],init[1],init[2],init[3]);
			swdbm.connect();
			
			String demographic = "ln";
			Hashtable<String,Integer> demographic_frequencies = swdbm.unionUniqueTokens(demographic);
			double[] frequencies = new double[demographic_frequencies.size()];
			Enumeration<String> e = demographic_frequencies.keys();
			int i = 0;
			while(e.hasMoreElements()){
				String key = e.nextElement();
				int count = demographic_frequencies.get(key);
				frequencies[i++] = count;
			}
			
			Percentile p = new Percentile();
			int[] percentiles = {10,20,30,40,50,60,70,80,90,95,98,99,100};
			for(i = 0; i < percentiles.length; i++){
				System.out.println("frequency at " + percentiles[i] + " percentile:\t" + p.evaluate(frequencies, percentiles[i]));
			}
			
			/*
			ScaleWeightModifier swm = new ScaleWeightModifier(swa1, swa2);
			swm.initializeModifier();
			
			// test scaling set inclusiveness
			String[] names = {"SMITH","BAKER","LEE","GONZALES","THOMPSON","MCINTYRE","ACRES","123"};
			String dem = "ln";
			int percent = 99;
			swm.setPercntileRequirement("ln", ModifySet.ABOVE, percent);
			System.out.println("names for " + dem + " above " + percent + " percentile:");
			for(int i = 0; i < names.length; i++){
				boolean b = swm.inScalingSet(dem, names[i]);
				System.out.println(names[i] + ":\t" + b);
			}
			*/
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
