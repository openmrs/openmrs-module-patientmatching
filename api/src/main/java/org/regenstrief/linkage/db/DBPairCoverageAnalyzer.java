package org.regenstrief.linkage.db;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;

/**
 * DBPairCoverageAnalyzer
 */
public class DBPairCoverageAnalyzer extends DBPairCounter {
	
	public static void main(final String[] args) throws Exception {
		run(args[0], args[1], args[2], args[3]);
	}
	
	private final static void run(final String dbFile, final String conditionLists, final String table, final String table2)
	        throws Exception {
		DBPairCoverageAnalyzer.table = table;
		DBPairCoverageAnalyzer.table2 = table2;
		con = SavedResultDBConnection.openDBResults(new File(dbFile));
		try {
			if ("unlinked".equalsIgnoreCase(conditionLists)) {
				dumpUnlinked();
				return;
			}
			final Collection<? extends Collection<JoinCondition>> joinConditionLists = parseConditionLists(conditionLists);
			System.out.println(
			    "Preparing to find how many rows in " + table + " have are linked to " + table2 + " by these conditions:");
			System.out.println();
			for (final Collection<JoinCondition> joinConditions : joinConditionLists) {
				System.out.println(serializeJoinConditionList(joinConditions));
			}
			System.out.println();
			createCoverageUnionTable();
			for (final Collection<JoinCondition> joinConditions : joinConditionLists) {
				insertCoveredIds(joinConditions);
			}
			final long count = getCoveredIdCount();
			System.out.println(count + " rows in " + table + " are linked to " + table2);
		}
		finally {
			con.close();
		}
	}
	
	private final static void dumpUnlinked() throws Exception {
		System.err.println("Dumping unlinked rows from " + table);
		final Statement stmt = con.createStatement();
		try {
			final String sql = "select uid from " + table
			        + " r where not exists(select 1 from temp_union u where u.uid=r.uid)";
			System.err.println(sql);
			final ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				System.out.println(rs.getString(1));
			}
			rs.close();
		}
		finally {
			stmt.close();
		}
		System.err.println("Finished");
	}
	
	private final static void createCoverageUnionTable() throws Exception {
		final Statement stmt = con.createStatement();
		try {
			execute(stmt, "drop table if exists temp_union");
			final StringBuilder b = new StringBuilder();
			b.append("create table temp_union (\n");
			b.append("uid varchar(4000)");
			b.append(")");
			execute(stmt, b);
			execute(stmt, "create unique index temp_union_idx on temp_union (uid)");
		}
		finally {
			stmt.close();
		}
	}
	
	private final static void insertCoveredIds(final Collection<JoinCondition> joinConditions) throws Exception {
		createCoverageTempTable(table, joinConditions);
		createCoverageTempTable(table2, joinConditions);
		final StringBuilder b = new StringBuilder();
		b.append("insert into temp_union\n");
		b.append("(uid)\n");
		b.append("select a.uid\n");
		b.append("from temp_").append(table).append(" a\n");
		b.append("where exists(select 1 from temp_").append(table2).append(" b where b.row_key=a.row_key)\n");
		b.append("and not exists(select 1 from temp_union u where u.uid=a.uid)");
		final Statement stmt = con.createStatement();
		try {
			execute(stmt, b);
		}
		finally {
			stmt.close();
		}
	}
	
	private final static void createCoverageTempTable(final String table, final Collection<JoinCondition> joinConditions)
	        throws Exception {
		final Statement stmt = con.createStatement();
		try {
			execute(stmt, "drop table if exists temp_" + table);
			final StringBuilder b = new StringBuilder();
			b.append("create table temp_").append(table).append(" as\n");
			b.append("select (");
			appendSubQueryRowKey(b, joinConditions);
			b.append(") row_key,uid\n");
			b.append("from ").append(table).append('\n');
			b.append("where ");
			boolean first = true;
			for (final JoinCondition joinCondition : joinConditions) {
				if (first) {
					first = false;
				} else {
					b.append(" and ");
				}
				b.append(joinCondition.getRecord1Column().getBlockingExpression(null)).append(" is not null");
			}
			b.append('\n');
			execute(stmt, b);
			execute(stmt, "create unique index temp_" + table + "_idx on temp_" + table + " (row_key,uid)");
		}
		finally {
			stmt.close();
		}
	}
	
	private final static long getCoveredIdCount() throws Exception {
		final StringBuilder b = new StringBuilder();
		b.append("select count(1)\n");
		b.append("from temp_union");
		return readCount(b);
	}
}
