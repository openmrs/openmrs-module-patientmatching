package org.regenstrief.linkage.matchresult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.ScoreVector;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Class implements a MatchResultStore that has the MatchResult information stored within a database
 * 
 *
 */

public class DBMatchResultStore implements MatchResultStore {
	
	public static final int LEFT_UID = 1;
	public static final int RIGHT_UID = 2;
	
	public static final String MATCH_RESULT_INSERT = "insert into matchresult" +
			"(ID,mc,score,true_prob,false_prob,spec,sens,status,certainty,uid1,uid2,note,report_date)" +
			" values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String DEMOGRAPHIC_INSERT = "insert into demographic" +
			"(uid,side,field,value)" +
			" values (?,?,?,?)";
	public static final String FIELD_AGREEMENT_INSERT = "insert into field_agreement" +
			"(ID,field,algorithm,agreement)" +
			" values (?,?,?,?)";
	public static final String COUNT_QUERY = "select count(*) from matchresult where report_date = ?";
	public static final String MATCH_RESULT_QUERY = "select * from matchresult where ID = ? and report_date = ?";
	public static final String DEMOGRAPHIC_QUERY = "select * from demographic where uid = ? and side = ?";
	public static final String DEMOGRAPHIC_COUNT_QUERY = "select count(*) from demographic where uid = ?";
	public static final String FIELD_AGREEMENT_QUERY = "select * from field_agreement where ID = ?";
	public static final String DELETE_MATCH_RESULT_QUERY = "delete from matchresult where ID = ? and report_date = ?";
	public static final String DELETE_FIELD_AGREEMENT_QUERY = "delete from field_agreement where ID = ?";
	public static final String UPDATE_MATCH_RESULT_QUERY = "update matchresult set status = ?, certainty = ?, note = ? where ID = ? and report_date = ?";
	public static final String MIN_UNKNOWN_QUERY = "select min(ID) from matchresult where status = " + MatchResult.UNKNOWN + " and report_date = ?";
	public static final String DATES_QUERY = "select distinct report_date from report_dates";
	public static final String DATES_INSERT = "insert into report_dates(report_date) values (?)";
	
	private Hashtable<Long,Boolean> imported_uids;
	
	protected Connection db;
	protected PreparedStatement mr_insert;
	protected PreparedStatement dem_insert;
	protected PreparedStatement fa_insert;
	protected PreparedStatement mr_count;
	protected PreparedStatement mr_query, dem_query, dem_count_query, fa_query;
	protected PreparedStatement mr_delete, fa_delete;
	protected PreparedStatement mr_update;
	protected PreparedStatement min_query;
	protected PreparedStatement date_query;
	protected PreparedStatement date_insert;
	
	protected Date set_date;
	
	public DBMatchResultStore(Connection db){
		this.db = db;
		imported_uids = new Hashtable<Long,Boolean>();
		try{
			mr_insert = db.prepareStatement(MATCH_RESULT_INSERT);
			dem_insert = db.prepareStatement(DEMOGRAPHIC_INSERT);
			fa_insert = db.prepareStatement(FIELD_AGREEMENT_INSERT);
			mr_count = db.prepareStatement(COUNT_QUERY);
			mr_query = db.prepareStatement(MATCH_RESULT_QUERY);
			dem_query = db.prepareStatement(DEMOGRAPHIC_QUERY);
			dem_count_query = db.prepareStatement(DEMOGRAPHIC_COUNT_QUERY);
			fa_query = db.prepareStatement(FIELD_AGREEMENT_QUERY);
			mr_delete = db.prepareStatement(DELETE_MATCH_RESULT_QUERY);
			fa_delete = db.prepareStatement(DELETE_FIELD_AGREEMENT_QUERY);
			mr_update = db.prepareStatement(UPDATE_MATCH_RESULT_QUERY);
			min_query = db.prepareStatement(MIN_UNKNOWN_QUERY);
			date_query = db.prepareStatement(DATES_QUERY);
			date_insert = db.prepareStatement(DATES_INSERT);
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
		}
		
	}
	
	public void setDate(Date d){
		List<Date> dates = getDates();
		if(!dates.contains(d)){
			try{
				date_insert.setDate(1, new java.sql.Date(d.getTime()));
				date_insert.execute();
			}
			catch(SQLException sqle){
				System.err.println(sqle.getMessage());
			}
		}
		set_date = d;
	}
	
	public Date getDate(){
		return set_date;
	}
	
	public MatchResult getMatchResult(int index) {
		MatchResult ret = null;
		ResultSet rs;
		try{
			mr_query.setInt(1, index);
			mr_query.setDate(2, new java.sql.Date(set_date.getTime()));
			rs = mr_query.executeQuery();
			rs.next();
			String mc_name = rs.getString("mc");
			double score = rs.getDouble("score");
			double incl_score = 0;
			double true_prob = rs.getDouble("true_prob");
			double false_prob = rs.getDouble("false_prob");
			double sens = rs.getDouble("sens");
			double spec = rs.getDouble("spec");
			int status = rs.getInt("status");
			double certainty = rs.getDouble("certainty");
			long uid1 = rs.getLong("uid1");
			long uid2 = rs.getLong("uid2");
			String note = rs.getString("note");
			rs.close();
			
			// get demographics for uid1 and uid2 and make Record objects
			dem_query.setLong(1, uid1);
			dem_query.setInt(2, LEFT_UID);
			rs = dem_query.executeQuery();
			Record r1 = new Record(uid1, "resultdb");
			while(rs.next()){
				String field = rs.getString("field");
				String value = rs.getString("value");
				r1.addDemographic(field, value);
			}
			rs.close();
			
			dem_query.setLong(1, uid2);
			dem_query.setInt(2, RIGHT_UID);
			rs = dem_query.executeQuery();
			Record r2 = new Record(uid2, "resultdb");
			while(rs.next()){
				String field = rs.getString("field");
				String value = rs.getString("value");
				r2.addDemographic(field, value);
			}
			rs.close();
			
			// get agreement information and make MatchVector
			fa_query.setInt(1, index);
			rs = fa_query.executeQuery();
			MatchVector mv = new MatchVector();
			while(rs.next()){
				String field = rs.getString("field");
				int agree = rs.getInt("agreement");
				if(agree == 1){
					mv.setMatch(field, true);
				} else {
					mv.setMatch(field, false);
				}
			}
			rs.close();
			
			ScoreVector sv = null;
			MatchingConfig mc = null;
			String[] mcrs = new String[0];
			mc = new MatchingConfig(mc_name, mcrs);
			
			// make MatchResult object to return
			ret = new MatchResult(score, incl_score, true_prob, false_prob, sens, spec, mv, sv, r1, r2, mc);
			ret.setNote(note);
			ret.setCertainty(certainty);
			ret.setMatch_status(status);
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
		}
		
		return ret;
	}

	public void addMatchResult(MatchResult mr, int id) {
		long uid1 = mr.getRecord1().getUID();
		long uid2 = mr.getRecord2().getUID();
		String mc = mr.getMatchingConfig().getName();
		double score = mr.getScore();
		double true_prob = mr.getTrueProbability();
		double false_prob = mr.getFalseProbability();
		double spec = mr.getSpecificity();
		double sens = mr.getSensitivity();
		int status = mr.getMatch_status();
		double certainty = mr.getCertainty();
		String note = mr.getNote();
		
		boolean add_r1 = true;
		boolean add_r2 = true;
		
		if(imported_uids.get(uid1) != null){
			add_r1 = false;
		}
		if(imported_uids.get(uid2) != null){
			add_r2 = false;
		}
		
		try{
			mr_insert.setInt(1, id);
			mr_insert.setString(2, mc);
			mr_insert.setDouble(3, score);
			mr_insert.setDouble(4, true_prob);
			mr_insert.setDouble(5, false_prob);
			mr_insert.setDouble(6, spec);
			mr_insert.setDouble(7, sens);
			mr_insert.setInt(8, status);
			mr_insert.setDouble(9, certainty);
			mr_insert.setLong(10, uid1);
			mr_insert.setLong(11, uid2);
			mr_insert.setString(12, note);
			mr_insert.setDate(13, new java.sql.Date(set_date.getTime()));
			mr_insert.execute();
			
			if(add_r1){
				dem_count_query.setLong(1, uid1);
				ResultSet rs = dem_count_query.executeQuery();
				rs.next();
				int n = rs.getInt(1);
				if(n > 0){
					add_r1 = false;
				}
				rs.close();
			}
			
			if(add_r2){
				dem_count_query.setLong(1, uid2);
				ResultSet rs = dem_count_query.executeQuery();
				rs.next();
				int n = rs.getInt(1);
				if(n > 0){
					add_r2 = false;
				}
				rs.close();
			}
			
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
		}
		
		Iterator<String> it = mr.getDemographics().iterator();
		while(it.hasNext()){
			String dem = it.next();
			String val1 = mr.getRecord1().getDemographic(dem);
			String val2 = mr.getRecord2().getDemographic(dem);
			
			int agree = 0;
			if(mr.matchedOn(dem)){
				agree = 1;
			}
			String alg = MatchingConfig.ALGORITHMS[mr.getMatchingConfig().getAlgorithm(mr.getMatchingConfig().getRowIndexforName(dem))];
			
			// set demographic and agreement table values
			try{
				
				if(add_r1){
					dem_insert.setLong(1, uid1);
					dem_insert.setInt(2, LEFT_UID);
					dem_insert.setString(3, dem);
					dem_insert.setString(4, val1);
					dem_insert.execute();
					imported_uids.put(uid1, Boolean.TRUE);
				}
				
				if(add_r2){
					dem_insert.setLong(1, uid2);
					dem_insert.setInt(2, RIGHT_UID);
					dem_insert.setString(3, dem);
					dem_insert.setString(4, val2);
					dem_insert.execute();
					imported_uids.put(uid2, Boolean.TRUE);
				}
				
				fa_insert.setInt(1, id);
				fa_insert.setString(2, dem);
				fa_insert.setString(3, alg);
				fa_insert.setInt(4, agree);
				fa_insert.execute();
			}
			catch(SQLException sqle){
				System.err.println(sqle.getMessage());
			}
			
		}
	}
	
	public List<Date> getDates(){
		List<Date> ret = new ArrayList<Date>();
		try{
			ResultSet rs = date_query.executeQuery();
			while(rs.next()){
				ret.add(rs.getDate(1));
			}
			rs.close();
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
		}
		
		return ret;
	}
	
	public int getMinUnknownID(){
		int id = 0;
		try{
			min_query.setDate(1, new java.sql.Date(set_date.getTime()));
			ResultSet rs = min_query.executeQuery();
			rs.next();
			id = rs.getInt(1);
			rs.close();
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
		}
		return id;
	}
	
	public void addIndexes(){
		try{
			Statement st = db.createStatement();
			st.execute("create index mr_idx on matchresult(ID, report_date)");
			st.execute("create index dem_idx on demographic(uid,side)");
			st.execute("create index fa_idx on field_agreement(ID)");
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
		}
		
	}

	public int getSize() {
		int size = 0;
		try{
			mr_count.setDate(1, new java.sql.Date(set_date.getTime()));
			ResultSet rs = mr_count.executeQuery();
			rs.next();
			size = rs.getInt(1);
			rs.close();
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
		}
		return size;
	}

	public void removeMatchResult(int id) {
		// need to remove row in matchresult and field_agreement tables
		try{
			fa_delete.setInt(1, id);
			fa_delete.execute();
			mr_delete.setInt(1, id);
			mr_delete.setDate(2, new java.sql.Date(set_date.getTime()));
			mr_delete.execute();
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
		}
	}
	
	public void updateMatchResult(int id, String note, int status, double certainty){
		try{
			mr_update.setInt(1, status);
			mr_update.setDouble(2, certainty);
			mr_update.setString(3, note);
			mr_update.setInt(4, id);
			mr_update.setDate(5, new java.sql.Date(set_date.getTime()));
			mr_update.executeUpdate();
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
		}
	}
	
	public void close(){
		try{
			mr_insert.close();
			dem_insert.close();
			fa_insert.close();
			mr_count.close();
			mr_query.close();
			dem_query.close();
			dem_count_query.close();
			fa_query.close();
			mr_delete.close();
			fa_delete.close();
			mr_update.close();
			min_query.close();
			date_query.close();
			date_insert.close();
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
		}
	}

}
