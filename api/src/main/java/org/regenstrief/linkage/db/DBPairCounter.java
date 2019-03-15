package org.regenstrief.linkage.db;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
	
	private final static String PROP_COMBINATIONS = "org.regenstrief.linkage.db.DBPairCounter.combinations";
	
	protected static Connection con = null;
	
	protected static String table = "record";
	protected static String table2 = null;
	
	private static boolean combinationsAndIndividuals = true;
	
	public static void main(final String[] args) throws Exception {
		run(args[0], args[1], (args.length < 3) ? null : args[2]);
	}
	
	private final static void run(final String dbFile, final String conditionLists, final String table2) throws Exception {
		DBPairCounter.table2 = table2;
		final Collection<? extends Collection<JoinCondition>> joinConditionLists = parseConditionLists(conditionLists);
		System.out.println("Preparing to count pairs for:");
		System.out.println();
		for (final Collection<JoinCondition> joinConditions : joinConditionLists) {
			System.out.println(serializeJoinConditionList(joinConditions));
		}
		System.out.println();
		System.out.println("Counts:");
		System.out.println();
		con = SavedResultDBConnection.openDBResults(new File(dbFile));
		try {
			for (final Collection<JoinCondition> joinConditions : joinConditionLists) {
				System.out.println(serializeJoinConditionList(joinConditions) + "|" + countPairs(joinConditions));
			}
		} finally {
			con.close();
		}
		System.out.println();
		System.out.println("Finished");
	}
	
	private final static long countPairs(final Collection<JoinCondition> joinConditions) throws Exception {
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
	
	private final static long countPairsBetweenTwoTables(final Collection<JoinCondition> joinConditions) throws Exception {
		createTempTable(table, joinConditions);
		createTempTable(table2, joinConditions);
		final StringBuilder b = new StringBuilder();
		b.append("select sum(a.value_count * b.value_count)\n");
		b.append("from temp_").append(table).append(" a\n");
		b.append("inner join temp_").append(table2).append(" b\n");
		b.append("on a.row_key=b.row_key");
		return readCount(b);
	}
	
	public final static void createTempTable(final String table, final Collection<JoinCondition> joinConditions) throws Exception {
		final Statement stmt = con.createStatement();
		execute(stmt, "drop table if exists temp_" + table);
		final StringBuilder b = new StringBuilder();
		b.append("create table temp_").append(table).append(" as\n");
		b.append("select (");
		appendSubQueryRowKey(b, joinConditions);
		b.append(") row_key,count(1) value_count\n");
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
		b.append("group by (");
		appendSubQueryRowKey(b, joinConditions);
		b.append(")");
		execute(stmt, b);
		execute(stmt, "create unique index temp_" + table + "_idx on temp_" + table + " (row_key)");
		stmt.close();
	}
	
	public final static void appendSubQueryRowKey(final StringBuilder b, final Collection<JoinCondition> joinConditions) {
		boolean first = true;
		for (final JoinCondition joinCondition : joinConditions) {
			if (first) {
				first = false;
			} else {
				b.append(" || '|' || ");
			}
			b.append(joinCondition.getRecord1Column().getValueExpression(null));
		}
	}
	
	private final static long countPairsAggregated(final Collection<JoinCondition> joinConditions) throws Exception {
		final StringBuilder b = new StringBuilder();
		b.append("select sum((record_count * (record_count - 1)) / 2)\n");
		b.append("from (\n");
		b.append("select ");
		appendColumnNames(b, joinConditions);
		b.append(",count(1) record_count");
		b.append('\n');
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
		b.append("group by ");
		appendColumnNames(b, joinConditions);
		b.append("\n)");
		return readCount(b);
	}
	
	private final static long countPairsJoin(final Collection<JoinCondition> joinConditions) throws Exception {
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
	
	public final static long readCount(final CharSequence sql) throws Exception {
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
	
	public final static void execute(final Statement stmt, final CharSequence sql) throws Exception {
		final String s = sql.toString();
		System.err.println(s);
		System.err.println();
		stmt.execute(s);
	}
	
	private final static void appendColumnNames(final StringBuilder b, final Collection<JoinCondition> joinConditions) {
		boolean first = true;
		for (final JoinCondition joinCondition : joinConditions) {
			if (first) {
				first = false;
			} else {
				b.append(',');
			}
			b.append(joinCondition.getRecord1Column().getValueExpression(null));
		}
	}
	
	public final static Collection<? extends Collection<JoinCondition>> parseConditionLists(final String conditionLists) {
		final String[] tokens = conditionLists.split(",");
		final List<List<JoinCondition>> list = new ArrayList<List<JoinCondition>>(tokens.length);
		for (final String conditionList : tokens) {
			list.add(parseConditionList(conditionList));
		}
		if ("true".equalsIgnoreCase(System.getProperty(PROP_COMBINATIONS))) {
			return getConditionListCombinations(list);
		}
		return list;
	}
	
	private final static Collection<? extends Collection<JoinCondition>> getConditionListCombinations(final List<List<JoinCondition>> lists) {
		final Set<Set<JoinCondition>> sets = new LinkedHashSet<Set<JoinCondition>>(lists.size());
		for (final List<JoinCondition> list : lists) {
			sets.add(new TreeSet<JoinCondition>(list));
		}
		final Set<Set<JoinCondition>> combinations = new LinkedHashSet<Set<JoinCondition>>();
		for (final Set<JoinCondition> set : sets) {
			addConditionListCombinations(combinations, sets, set);
		}
		return combinations;
	}
	
	private final static void addConditionListCombinations(final Set<Set<JoinCondition>> combinations, final Set<Set<JoinCondition>> sets, final Set<JoinCondition> base) {
		for (final Set<JoinCondition> set : sets) {
			final Set<JoinCondition> combination = new TreeSet<JoinCondition>(base);
			final boolean individual = !combination.addAll(set);
			if (individual && !combinationsAndIndividuals) {
				continue;
			} else if (!combinations.add(combination)) {
				continue;
			}
			addConditionListCombinations(combinations, sets, combination);
		}
	}
	
	private final static List<JoinCondition> parseConditionList(final String joinConditionList) {
		final String[] joinConditions = PAT_PLUS.split(joinConditionList);
		final List<JoinCondition> list = new ArrayList<JoinCondition>(joinConditions.length);
		for (final String joinCondition : joinConditions) {
			list.add(new JoinCondition(joinCondition));
		}
		return list;
	}
	
	public final static String serializeJoinConditionList(final Collection<JoinCondition> list) {
		final StringBuilder b = new StringBuilder();
		for (final JoinCondition joinCondition : list) {
			if (b.length() > 0) {
				b.append('+');
			}
			b.append(joinCondition);
		}
		return b.toString();
	}
	
	public static class JoinCondition implements Comparable<JoinCondition> {
		
		private final String joinCondition;
		
		private final JoinColumn record1Column;
		
		private final JoinColumn record2Column;
		
		public JoinCondition(final String joinCondition) {
			this.joinCondition = joinCondition;
			final int d = joinCondition.indexOf(DELIM_SWAP);
			if (d > 0) {
				this.record1Column = new JoinColumn(joinCondition.substring(0, d));
				this.record2Column = new JoinColumn(joinCondition.substring(d + 1));
			} else {
				this.record1Column = this.record2Column = new JoinColumn(joinCondition);
			}
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
		
		@Override
		public final String toString() {
			return this.joinCondition;
		}
		
		@Override
		public final boolean equals(final Object o) {
			if (o == this) {
				return true;
			} else if (o == null) {
				return false;
			} else if (o.getClass() != JoinCondition.class) {
				return false;
			}
			return this.joinCondition.equals(((JoinCondition) o).joinCondition); 
		}
		
		@Override
		public final int hashCode() {
			return this.joinCondition.hashCode();
		}

		@Override
		public final int compareTo(final JoinCondition o) {
			return this.joinCondition.compareTo(o.joinCondition);
		}
	}
	
	public static class JoinColumn implements Comparable<JoinColumn> {
		
		private final String joinColumn;
		
		private final String valueColumn;
		
		private final String function;
		
		private final String blockingColumn;
		
		public JoinColumn(final String joinColumn) {
			this.joinColumn = joinColumn;
			final int d = joinColumn.indexOf(DELIM_BLOCKING);
			final String baseCol, baseBlock;
			if (d > 0) {
				baseCol = joinColumn.substring(0, d);
				baseBlock = joinColumn.substring(d + 1);
			} else {
				baseCol = joinColumn;
				baseBlock = null;
			}
			final int f = baseCol.indexOf(DELIM_FUNCTION);
			if (f > 0) {
				this.valueColumn = baseCol.substring(0, f);
				this.function = baseCol.substring(f + 1);
			} else {
				this.valueColumn = baseCol;
				this.function = null;
			}
			this.blockingColumn = (baseBlock == null) ? this.valueColumn : baseBlock;
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
		
		@Override
		public final String toString() {
			return this.joinColumn;
		}
		
		@Override
		public final boolean equals(final Object o) {
			if (o == this) {
				return true;
			} else if (o == null) {
				return false;
			} else if (o.getClass() != JoinColumn.class) {
				return false;
			}
			return this.joinColumn.equals(((JoinColumn) o).joinColumn); 
		}
		
		@Override
		public final int hashCode() {
			return this.joinColumn.hashCode();
		}

		@Override
		public final int compareTo(final JoinColumn o) {
			return this.joinColumn.compareTo(o.joinColumn);
		}
	}
}
