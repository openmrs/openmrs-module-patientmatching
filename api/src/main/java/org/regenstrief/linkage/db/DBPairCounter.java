package org.regenstrief.linkage.db;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * DBPairCounter
 */
public class DBPairCounter {
	
	private final static char DELIM_SWAP = '^';
	
	private final static char DELIM_BLOCKING = '@';
	
	private final static char DELIM_FUNCTION = '.';
	
	private final static String FUNCTION_SUB = "sub";
	
	private final static String FUNCTION_NAME = "name";
	
	private final static String FUNCTION_CONST = "const";
	
	private final static String FUNCTION_NUM = "num";
	
	private final static Pattern PAT_PLUS = Pattern.compile("\\+");
	
	private static Connection con = null;
	
	private static String table = "record";
	
	private static String table2 = null;
	
	public final static void main(final String[] args) throws Exception {
		run(args[0], args[1], (args.length < 3) ? null : args[2]);
	}
	
	private final static void run(final String dbFile, final String conditionLists, final String table2) throws Exception {
		DBPairCounter.table2 = table2;
		con = SavedResultDBConnection.openDBResults(new File(dbFile));
		try {
			for (final String conditionList : conditionLists.split(",")) {
				System.out.println(conditionList + "|" + countPairs(parseConditionList(conditionList)));
			}
		} finally {
			con.close();
		}
	}
	
	private final static long countPairs(final List<JoinCondition> joinConditions) throws Exception {
		if (table2 != null) {
			return countPairsBetweenTwoTables(joinConditions);
		}
		for (final JoinCondition joinCondition : joinConditions) {
			if (joinCondition.isSwapped()) {
				return countPairsJoin(joinConditions);
			}
		}
		return countPairsAggregated(joinConditions);
	}
	
	private final static long countPairsBetweenTwoTables(final List<JoinCondition> joinConditions) throws Exception {
		createTempTable(table, joinConditions);
		createTempTable(table2, joinConditions);
		final StringBuilder b = new StringBuilder();
		b.append("select sum(a.value_count * b.value_count)\n");
		b.append("from temp_").append(table).append(" a\n");
		b.append("inner join temp_").append(table2).append(" b\n");
		b.append("on a.row_key=b.row_key");
		return readCount(b);
	}
	
	private final static void createTempTable(final String table, final List<JoinCondition> joinConditions) throws Exception {
		final Statement stmt = con.createStatement();
		execute(stmt, "drop table if exists temp_" + table);
		final StringBuilder b = new StringBuilder();
		b.append("create table temp_").append(table).append(" as\n");
		b.append("select (");
		appendSubQueryRowKey(b, joinConditions);
		b.append(") row_key,count(1) value_count\n");
		b.append("from ").append(table).append('\n');
		b.append("where ");
		final int size = joinConditions.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				b.append(" and ");
			}
			b.append(joinConditions.get(i).getRecord1Column().getBlockingExpression(null)).append(" is not null");
		}
		b.append('\n');
		b.append("group by (");
		appendSubQueryRowKey(b, joinConditions);
		b.append(")");
		execute(stmt, b);
		execute(stmt, "create unique index temp_" + table + "_idx on temp_" + table + " (row_key)");
		stmt.close();
	}
	
	private final static void appendSubQueryRowKey(final StringBuilder b, final List<JoinCondition> joinConditions) {
		final int size = joinConditions.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				b.append(" || '|' || ");
			}
			b.append(joinConditions.get(i).getRecord1Column().getValueExpression(null));
		}
	}
	
	/*
	private final static long countPairsBetweenTwoTables(final List<JoinCondition> joinConditions) throws Exception {
		final StringBuilder b = new StringBuilder();
		b.append("select sum(a.value_count * b.value_count)\n");
		b.append("from\n");
		appendSubQueryForOneTable(b, table, joinConditions, "a");
		b.append("inner join\n");
		appendSubQueryForOneTable(b, table2, joinConditions, "b");
		b.append("on a.row_key=b.row_key");
		return readCount(b);
	}
	
	private final static void appendSubQueryForOneTable(final StringBuilder b, final String table, final List<JoinCondition> joinConditions, final String alias) {
		b.append("(\n");
		b.append("  select (");
		appendSubQueryRowKey(b, joinConditions);
		b.append(") row_key,count(1) value_count\n");
		b.append("  from ").append(table).append('\n');
		b.append("  where ");
		final int size = joinConditions.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				b.append(" and ");
			}
			b.append(joinConditions.get(i).getRecord1Column().getBlockingExpression(null)).append(" is not null");
		}
		b.append('\n');
		b.append("  group by (");
		appendSubQueryRowKey(b, joinConditions);
		b.append(")\n");
		b.append(") ").append(alias).append('\n');
	}
	
	private final static void appendSubQueryRowKey(final StringBuilder b, final List<JoinCondition> joinConditions) {
		final int size = joinConditions.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				b.append(" || '|' || ");
			}
			b.append(joinConditions.get(i).getRecord1Column().getValueExpression(null));
		}
	}
	*/
	
	private final static long countPairsAggregated(final List<JoinCondition> joinConditions) throws Exception {
		final StringBuilder b = new StringBuilder();
		b.append("select sum((record_count * (record_count - 1)) / 2)\n");
		b.append("from (\n");
		b.append("select ");
		appendColumnNames(b, joinConditions);
		b.append(",count(1) record_count");
		b.append('\n');
		b.append("from ").append(table).append('\n');
		b.append("where ");
		final int size = joinConditions.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				b.append(" and ");
			}
			b.append(joinConditions.get(i).getRecord1Column().getBlockingExpression(null)).append(" is not null");
		}
		b.append('\n');
		b.append("group by ");
		appendColumnNames(b, joinConditions);
		b.append("\n)");
		return readCount(b);
	}
	
	private final static long countPairsJoin(final List<JoinCondition> joinConditions) throws Exception {
		final StringBuilder b = new StringBuilder();
		b.append("select count(1)\n"); // Might not need to do a join; could sort the swappable columns and then join on that.
		b.append("from ").append(table).append(" r1\n"); // That would count where r1.a=r2.b and r1.b=r2.a, but also where r1.a=r2.a and r1.b=r2.b.
		b.append("inner join ").append(table).append(" r2\n"); // So we would also need to do the normal closed form and subtract.
		b.append("on ");
		for (final JoinCondition joinCondition : joinConditions) {
			final JoinColumn col1 = joinCondition.getRecord1Column(), col2 = joinCondition.getRecord2Column();
			b.append(col2.getValueExpression("r2")).append('=').append(col1.getValueExpression("r1")).append(" and ");
			b.append(col2.getBlockingExpression("r2")).append(" is not null and ");
			b.append(col1.getBlockingExpression("r1")).append(" is not null and ");
		}
		b.append("r2.rowid > r1.rowid");
		return readCount(b);
	}
	
	private final static long readCount(final CharSequence sql) throws Exception {
		final String s = sql.toString();
		System.err.println(s);
		System.err.println();
		final PreparedStatement stmt = con.prepareStatement(s);
		try {
			final ResultSet rs = stmt.executeQuery();
			final long pairCount = rs.next() ? rs.getLong(1) : 0;
			rs.close();
			return pairCount;
		} finally {
			stmt.close();
		}
	}
	
	private final static void execute(final Statement stmt, final CharSequence sql) throws Exception {
		final String s = sql.toString();
		System.err.println(s);
		System.err.println();
		stmt.execute(s);
	}
	
	private final static void appendColumnNames(final StringBuilder b, final List<JoinCondition> joinConditions) {
		final int size = joinConditions.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				b.append(',');
			}
			b.append(joinConditions.get(i).getRecord1Column().getValueExpression(null));
		}
	}
	
	private final static List<JoinCondition> parseConditionList(final String joinConditionList) {
		final String[] joinConditions = PAT_PLUS.split(joinConditionList);
		final List<JoinCondition> list = new ArrayList<JoinCondition>(joinConditions.length);
		for (final String joinCondition : joinConditions) {
			list.add(parseCondition(joinCondition));
		}
		return list;
	}
	
	private static JoinCondition parseCondition(final String joinCondition) {
		final int d = joinCondition.indexOf(DELIM_SWAP);
		final JoinColumn col1, col2;
		if (d > 0) {
			col1 = parseColumn(joinCondition.substring(0, d));
			col2 = parseColumn(joinCondition.substring(d + 1));
		} else {
			col1 = col2 = parseColumn(joinCondition);
		}
		return new JoinCondition(col1, col2);
	}
	
	private static JoinColumn parseColumn(final String joinColumn) {
		final int d = joinColumn.indexOf(DELIM_BLOCKING);
		final String baseCol, baseBlock;
		if (d > 0) {
			baseCol = joinColumn.substring(0, d);
			baseBlock = joinColumn.substring(d + 1);
		} else {
			baseCol = joinColumn;
			baseBlock = null;
		}
		final String valueCol, function, blockingCol;
		final int f = baseCol.indexOf(DELIM_FUNCTION);
		if (f > 0) {
			valueCol = baseCol.substring(0, f);
			function = baseCol.substring(f + 1);
		} else {
			valueCol = baseCol;
			function = null;
		}
		blockingCol = (baseBlock == null) ? valueCol : baseBlock;
		return new JoinColumn(valueCol, function, blockingCol);
	}
	
	public static class JoinCondition {
		
		private final JoinColumn record1Column;
		
		private final JoinColumn record2Column;
		
		public JoinCondition(final JoinColumn record1Column, final JoinColumn record2Column) {
			this.record1Column = record1Column;
			this.record2Column = record2Column;
		}
		
		public JoinColumn getRecord1Column() {
			return this.record1Column;
		}
		
		public JoinColumn getRecord2Column() {
			return this.record2Column;
		}
		
		public boolean isSwapped() {
			return record1Column != record2Column;
		}
	}
	
	public static class JoinColumn {
		
		private final String valueColumn;
		
		private final String function;
		
		private final String blockingColumn;
		
		public JoinColumn(final String valueColumn, final String function, final String blockingColumn) {
			this.valueColumn = valueColumn;
			this.function = function;
			this.blockingColumn = blockingColumn;
		}
		
		public String getValueColumn() {
			return this.valueColumn;
		}
		
		public String getFunction() {
			return this.function;
		}
		
		public String getBlockingColumn() {
			return this.blockingColumn;
		}
		
		public String getValueExpression(final String alias) {
			return getExpression(alias, this.valueColumn);
		}
		
		public String getBlockingExpression(final String alias) {
			/*
			An expression doesn't make sense when using LN_NYS@LNB, but it does when using MIB.name.
			Use blockingExpression only if blockingColumn is same as valueColumn.
			*/
			if (this.blockingColumn.equals(this.valueColumn)) {
				return getValueExpression(alias);
			}
			return getAliasedColumn(alias, this.blockingColumn);
		}
		
		public String getExpression(final String alias, final String baseCol) {
			final String col = getAliasedColumn(alias, baseCol);
			if (this.function == null) {
				return col;
			} else if (this.function.startsWith(FUNCTION_SUB)) {
				final String len = this.function.substring(FUNCTION_SUB.length());
				return "substr(" + col + ",1," + len + ")";
			} else if (FUNCTION_NAME.equals(this.function)) {
				return "case when " + col + " is null then null when length(" + col + ") <= 1 then null else " + col + " end";
			} else if (this.function.startsWith(FUNCTION_CONST)) {
				final String val = "'" + this.function.substring(FUNCTION_CONST.length()) + "'";
				return "case when " + col + "=" + val + " then " + val + " else null end";
			} else if (FUNCTION_NUM.equals(this.function)) {
				//return "case when substr(" + col + ",1,1) between '0' and '9' then substr(" + col + ",1,instr(" + col + ",' ')-1) else null end";
				return "case when substr(" + col + ",1,1) between '0' and '9' then substr(" + col + ",1,5) else null end";
			} else {
				throw new IllegalStateException("Unrecognized function: " + this.function);
			}
		}
		
		public static String getAliasedColumn(final String alias, final String baseCol) {
			return (alias == null) ? baseCol : (alias + "." + baseCol);
		}
	}
}
