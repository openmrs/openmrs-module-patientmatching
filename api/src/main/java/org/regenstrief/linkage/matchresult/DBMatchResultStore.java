package org.regenstrief.linkage.matchresult;

import java.sql.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Class implements a MatchResultStore that has the MatchResult information stored within a database
 *
 */
public class DBMatchResultStore implements MatchResultStore {
	
	public static final int LEFT_UID = 1;
	public static final int RIGHT_UID = 2;
	
	public static final String RECORD_INSERT = "insert into record" +
			"(uid)" +
			" values (?)";
	public static final String MATCH_RESULT_INSERT = "insert into matchresult" +
			"(ID,mc,score,true_prob,false_prob,spec,sens,status,certainty,uid1,uid2,note,report_date)" +
			" values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String DEMOGRAPHIC_INSERT = "insert into demographic" +
			"(uid,field,value)" +
			" values (?,?,?)";
	public static final String FIELD_AGREEMENT_INSERT = "insert into field_agreement" +
			"(ID,field,algorithm,agreement)" +
			" values (?,?,?,?)";
	public static final String COUNT_QUERY = "select count(*) from matchresult where report_date = ?";
	public static final String MATCH_RESULT_QUERY = "select * from matchresult where ID = ? and report_date = ?";
	public static final String DEMOGRAPHIC_QUERY = "select * from demographic where uid = ?";
	public static final String RECORD_COUNT_QUERY = "select count(*) from record where uid = ?";
	public static final String FIELD_AGREEMENT_QUERY = "select * from field_agreement where ID = ?";
	public static final String DELETE_MATCH_RESULT_QUERY = "delete from matchresult where ID = ? and report_date = ?";
	public static final String DELETE_FIELD_AGREEMENT_QUERY = "delete from field_agreement where ID = ?";
	public static final String UPDATE_MATCH_RESULT_QUERY = "update matchresult set status = ?, certainty = ?, note = ? where ID = ? and report_date = ?";
	public static final String MIN_UNKNOWN_QUERY = "select min(ID) from matchresult where status = " + MatchResult.UNKNOWN + " and report_date = ?";
	public static final String DATES_QUERY = "select distinct report_date from report_dates";
	public static final String DATES_INSERT = "insert into report_dates(report_date) values (?)";
	
	private final HashSet<Long> imported_uids = new HashSet<Long>();
	
	protected Connection db;
	protected PreparedStatement rec_insert;
	protected PreparedStatement mr_insert;
	protected PreparedStatement dem_insert;
	protected PreparedStatement fa_insert;
	protected PreparedStatement mr_count;
	protected PreparedStatement mr_query, dem_query, rec_count_query, fa_query;
	protected PreparedStatement mr_delete, fa_delete;
	protected PreparedStatement mr_update;
	protected PreparedStatement min_query;
	protected PreparedStatement date_query;
	protected PreparedStatement date_insert;
	
	protected Date set_date;
	
	private int addCount = 0;
	
	public DBMatchResultStore(final Connection db) {
		this.db = db;
		try {
			rec_insert = db.prepareStatement(RECORD_INSERT);
			mr_insert = db.prepareStatement(MATCH_RESULT_INSERT);
			dem_insert = db.prepareStatement(DEMOGRAPHIC_INSERT);
			fa_insert = db.prepareStatement(FIELD_AGREEMENT_INSERT);
			mr_count = db.prepareStatement(COUNT_QUERY);
			mr_query = db.prepareStatement(MATCH_RESULT_QUERY);
			dem_query = db.prepareStatement(DEMOGRAPHIC_QUERY);
			rec_count_query = db.prepareStatement(RECORD_COUNT_QUERY);
			fa_query = db.prepareStatement(FIELD_AGREEMENT_QUERY);
			mr_delete = db.prepareStatement(DELETE_MATCH_RESULT_QUERY);
			fa_delete = db.prepareStatement(DELETE_FIELD_AGREEMENT_QUERY);
			mr_update = db.prepareStatement(UPDATE_MATCH_RESULT_QUERY);
			min_query = db.prepareStatement(MIN_UNKNOWN_QUERY);
			date_query = db.prepareStatement(DATES_QUERY);
			date_insert = db.prepareStatement(DATES_INSERT);
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Connection getDb() {
		return db;
	}
	
	public void setDate(final java.util.Date d) {
		set_date = new Date(d.getTime());
		if (!getDates().contains(set_date)) {
			try {
				date_insert.setDate(1, set_date);
				date_insert.executeUpdate();
			} catch (final SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public Date getDate() {
		return set_date;
	}
	
	public int getAddCount() {
		return addCount;
	}
	
	@Override
	public MatchResult getMatchResult(final int index) {
		ResultSet rs = null;
		try {
			mr_query.setInt(1, index);
			mr_query.setDate(2, set_date);
			rs = mr_query.executeQuery();
			rs.next();
			final String mc_name = rs.getString("mc");
			final double score = rs.getDouble("score");
			final double true_prob = rs.getDouble("true_prob"), false_prob = rs.getDouble("false_prob");
			final double sens = rs.getDouble("sens"), spec = rs.getDouble("spec");
			final int status = rs.getInt("status");
			final double certainty = rs.getDouble("certainty");
			final long uid1 = rs.getLong("uid1"), uid2 = rs.getLong("uid2");
			final String note = rs.getString("note");
			rs.close();
			
			// get demographics for uid1 and uid2 and make Record objects
			final Record r1 = readRecord(uid1), r2 = readRecord(uid2);
			
			// get agreement information and make MatchVector
			fa_query.setInt(1, index);
			rs = fa_query.executeQuery();
			final MatchVector mv = new MatchVector();
			while (rs.next()) {
				mv.setMatch(rs.getString("field"), rs.getInt("agreement") == 1);
			}
			rs.close();
			final MatchingConfig mc = new MatchingConfig(mc_name, new String[0]);
			
			// make MatchResult object to return
			final MatchResult ret = new MatchResult(score, 0, true_prob, false_prob, sens, spec, mv, null, r1, r2, mc);
			ret.setNote(note);
			ret.setCertainty(certainty);
			ret.setMatch_status(status);
			return ret;
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(rs);
		}
	}
	
	private final Record readRecord(final long uid) throws SQLException {
		dem_query.setLong(1, uid);
		final Record r = new Record(uid, "resultdb");
		final ResultSet rs = dem_query.executeQuery();
		try {
			while (rs.next()) {
				r.addDemographic(rs.getString("field"), rs.getString("value"));
			}
		} finally {
			rs.close();
		}
		return r;
	}

	@Override
	public void addMatchResult(final MatchResult mr, final int id) {
		final MatchingConfig mc = mr.getMatchingConfig();
		final Record record1 = mr.getRecord1(), record2 = mr.getRecord2();
		final long uid1 = record1.getUID(), uid2 = record2.getUID();
		boolean add_r1 = !imported_uids.contains(Long.valueOf(uid1)), add_r2 = !imported_uids.contains(Long.valueOf(uid2));
		
		try {
			mr_insert.setInt(1, id);
			mr_insert.setString(2, mc.getName());
			mr_insert.setDouble(3, mr.getScore());
			mr_insert.setDouble(4, mr.getTrueProbability());
			mr_insert.setDouble(5, mr.getFalseProbability());
			mr_insert.setDouble(6, mr.getSpecificity());
			mr_insert.setDouble(7, mr.getSensitivity());
			mr_insert.setInt(8, mr.getMatch_status());
			mr_insert.setDouble(9, mr.getCertainty());
			mr_insert.setLong(10, uid1);
			mr_insert.setLong(11, uid2);
			mr_insert.setString(12, mr.getNote());
			mr_insert.setDate(13, set_date);
			executeAtLeastOneUpdate(mr_insert);
			
			add_r1 = addRecordIfNeeded(add_r1, uid1);
			add_r2 = addRecordIfNeeded(add_r2, uid2);
		
			for (final String dem : mr.getDemographics()) {
				final String val1 = record1.getDemographic(dem), val2 = record2.getDemographic(dem);
				final int agree = mr.matchedOn(dem) ? 1 : 0;
				final String alg = MatchingConfig.ALGORITHMS[mc.getAlgorithm(mc.getRowIndexforName(dem))];
				
				// set demographic and agreement table values
				add(add_r1, uid1, dem, val1);
				add(add_r2, uid2, dem, val2);
				
				fa_insert.setInt(1, id);
				fa_insert.setString(2, dem);
				fa_insert.setString(3, alg);
				fa_insert.setInt(4, agree);
				executeAtLeastOneUpdate(fa_insert);
			}
			addCount++;
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void addMatchResultToEachStore(final DBMatchResultStore other, final MatchResult mr, final int id) {
		addMatchResult(mr, id);
		other.addMatchResult(mr, id);
	}
	
	private boolean addRecordIfNeeded(final boolean add, final long uid) throws SQLException {
		if (!add) {
			return false;
		}
		rec_count_query.setLong(1, uid);
		final ResultSet rs = rec_count_query.executeQuery();
		try {
			if (rs.next() && (rs.getInt(1) > 0)) {
				return false;
			}
		} finally {
			rs.close();
		}
		rec_insert.setLong(1, uid);
		executeAtLeastOneUpdate(rec_insert);
		imported_uids.add(Long.valueOf(uid));
		return true;
	}
	
	private void add(final boolean add, final long uid, final String dem, final String val) throws SQLException {
		if (add) {
			dem_insert.setLong(1, uid);
			dem_insert.setString(2, dem);
			dem_insert.setString(3, val);
			executeAtLeastOneUpdate(dem_insert);
		}
	}
	
	public List<java.util.Date> getDates() {
		List<java.util.Date> ret = new ArrayList<java.util.Date>();
		ResultSet rs = null;
		try {
			rs = date_query.executeQuery();
			while (rs.next()) {
				ret.add(rs.getDate(1));
			}
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(rs);
		}
		
		return ret;
	}
	
	public int getMinUnknownID() {
		ResultSet rs = null;
		try {
			min_query.setDate(1, set_date);
			rs = min_query.executeQuery();
			return rs.next() ? rs.getInt(1) : 0;
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(rs);
		}
	}
	
	public void addIndexes() {
		try {
			final Statement st = db.createStatement();
			st.execute("create index mr_idx on matchresult(ID, report_date)");
			st.execute("create index dem_idx on demographic(uid)");
			st.execute("create index fa_idx on field_agreement(ID)");
			st.close();
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getSize() {
		ResultSet rs = null;
		try {
			mr_count.setDate(1, set_date);
			rs = mr_count.executeQuery();
			return rs.next() ? rs.getInt(1) : 0;
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(rs);
		}
	}

	@Override
	public void removeMatchResult(final int id) {
		if (!removeMatchResultIfPresent(id)) {
			throw new IllegalStateException("Attempted to remove match result " + id + ", but it was not found");
		}
	}
	
	private boolean removeMatchResultIfPresent(final int id) {
		// need to remove row in matchresult and field_agreement tables
		try {
			fa_delete.setInt(1, id);
			if (fa_delete.executeUpdate() <= 0) {
				return false;
			}
			mr_delete.setInt(1, id);
			mr_delete.setDate(2, set_date);
			executeAtLeastOneUpdate(mr_delete);
			return true;
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private final static void executeAtLeastOneUpdate(final PreparedStatement stmt) throws SQLException {
		final int count = stmt.executeUpdate();
		if (count > 0) {
			return;
		}
		throw new IllegalStateException("Expected SQL to impact at least one row, but none were impacted");
	}
	
	public void updateMatchResult(final int id, final String note, final int status, final double certainty) {
		try {
			mr_update.setInt(1, status);
			mr_update.setDouble(2, certainty);
			mr_update.setString(3, note);
			mr_update.setInt(4, id);
			mr_update.setDate(5, set_date);
			mr_update.executeUpdate();
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void close() {
		try {
			rec_insert.close();
			mr_insert.close();
			dem_insert.close();
			fa_insert.close();
			mr_count.close();
			mr_query.close();
			dem_query.close();
			rec_count_query.close();
			fa_query.close();
			mr_delete.close();
			fa_delete.close();
			mr_update.close();
			min_query.close();
			date_query.close();
			date_insert.close();
		} catch (final SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	private final static void close(final ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (final SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
