package org.regenstrief.linkage.analysis;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class SingleFieldSqliteDataSourceFrequency extends SingleFieldBaseDataSourceFrequency {
	
	private final Connection con;
	private final PreparedStatement readFreq;
	private final PreparedStatement insertFreq;
	private final PreparedStatement updateSet;
	private final PreparedStatement updateIncrement;
	private final PreparedStatement deleteFreq;
	private final PreparedStatement readTokens;
	private final int commitThreshold;
	private int writeCount = 0;
	
	public SingleFieldSqliteDataSourceFrequency(final String field) {
		super(field);
		try {
			Class.forName("org.sqlite.JDBC");
			final File f = new File("tmp." + System.currentTimeMillis() + ".db");
			final String path = f.getAbsolutePath();
			System.out.println("Opening frequency database: " + path);
			con = DriverManager.getConnection("jdbc:sqlite:" + path);
			con.setAutoCommit(false);
			createSchema();
			readFreq = con.prepareStatement("select amt from freq where token=?");
			insertFreq = con.prepareStatement("insert into freq (amt,token) values(?,?)");
			updateSet = con.prepareStatement("update freq set amt=? where token=?");
			updateIncrement = con.prepareStatement("update freq set amt=(amt+1) where token=?");
			deleteFreq = con.prepareStatement("delete from freq where token=?");
			readTokens = con.prepareStatement("select token from freq");
			final String commitValue = System.getProperty("org.regenstrief.linkage.analysis.SingleFieldSqliteDataSourceFrequency.commitThreshold");
			commitThreshold = (commitValue == null) ? 5000 : Integer.parseInt(commitValue);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void close() {
		try {
			con.commit();
			closeAll();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		//TODO SHOULD PROBABLY DELETE THE .DB FILES; ONE HAS HAD VALUES REMOVED AND ISN'T VERY USEFUL ANYMORE; DON'T HAVE STRATEGRY FOR DETECTING STALE .DB FILES TO KNOW WHEN ONE CAN REUSED ANYWAY
	}
	
	private void closeAll() throws Exception {
		try {
			closeStatements();
		} finally {
			con.close();
		}
	}
	
	private void closeStatements() {
		close(readFreq);
		close(insertFreq);
		close(updateSet);
		close(updateIncrement);
		close(deleteFreq);
		close(readTokens);
	}
	
	private static void close(final Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void close(final ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void createSchema() throws Exception {
		final Statement stmt = con.createStatement();
		try {
			stmt.execute("create table freq (token varchar(4000) not null, amt integer not null)");
			stmt.execute("create unique index freq_idx on freq(token)");
			con.commit();
		} finally {
			stmt.close();
		}
	}
	
	@Override
	public final int getFrequency(final String field, final String token) {
		try {
			readFreq.setString(1, token);
			final ResultSet rs = readFreq.executeQuery();
			try {
				return rs.next() ? rs.getInt(1) : 0;
			} finally {
				rs.close();
			}
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public final int removeFrequency(final String field, final String token) {
		final int freq = getFrequency(field, token);
		if (freq > 0) {
			try {
				deleteFreq.setString(1, token);
				deleteFreq.executeUpdate();
				commitIfNeeded();
			} catch (final Exception e) {
				throw new IllegalStateException(e);
			}
		}
		return freq;
	}
	
	private final void insert(final String token, final int freq) throws Exception {
		insertFreq.setInt(1, freq);
		insertFreq.setString(2, token);
		insertFreq.executeUpdate();
	}
	
	private final void commitIfNeeded() throws Exception {
		writeCount++;
		if (writeCount >= commitThreshold) {
			con.commit();
			writeCount = 0;
		}
	}
	
	@Override
	public final void setFrequency(final String field, final String token, final int freq) {
		try {
			updateSet.setInt(1, freq);
			updateSet.setString(2, token);
			if (updateSet.executeUpdate() == 0) {
				insert(token, freq);
			}
			commitIfNeeded();
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public final void incrementCount(final String field, final String token) {
		try {
			updateIncrement.setString(1, token);
			if (updateIncrement.executeUpdate() == 0) {
				insert(token, 1);
			}
			commitIfNeeded();
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public final Set<String> getTokens(final String field) {
		try {
			return readTokens();
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private final Set<String> readTokens() throws Exception {
		final ResultSet rs = readTokens.executeQuery();
		try {
			final Set<String> tokens = new HashSet<String>();
			while (rs.next()) {
				tokens.add(rs.getString(1));
			}
			return tokens;
		} finally {
			rs.close();
		}
	}
	
	@Override
	public final Iterator<String> getTokenIterator(final String field) {
		return new TokenIterator();
	}
	
	public final class TokenIterator implements Iterator<String> {
		
		private final ResultSet rs;
		
		private TokenIterator() {
			try {
				rs = readTokens.executeQuery();
			} catch (final Exception e) {
				throw new IllegalStateException(e);
			}
		}
		
		@Override
		public boolean hasNext() {
			try {
				final boolean next = rs.next();
				if (!next) {
					close();
				}
				return next;
			} catch (final Exception e) {
				close();
				throw new IllegalStateException(e);
			}
		}

		@Override
		public String next() {
			try {
				return rs.getString(1);
			} catch (final Exception e) {
				close();
				throw new IllegalStateException(e);
			}
		}
		
		@Override
		protected void finalize() {
			close();
		}
		
		public void close() {
			SingleFieldSqliteDataSourceFrequency.close(rs);
		}
	}
}
