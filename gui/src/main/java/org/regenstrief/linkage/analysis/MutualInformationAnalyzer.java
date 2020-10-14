package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.gui.SaveTextDisplayFrame;
import org.regenstrief.linkage.gui.TextDisplayFrame;
import org.regenstrief.linkage.util.Column;

import org.regenstrief.linkage.util.ColumnPair;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MICalculator;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.Token;

public class MutualInformationAnalyzer extends FrequencyAnalyzer {
	
	private RecMatchConfig rm_conf;
	
	private MICalculator mi_calculator;
	
	private DataSourceFrequency dsf;
	
	public MutualInformationAnalyzer(RecMatchConfig rm_conf, LinkDataSource lds, DataSourceFrequency dsf) {
		super(lds, null, dsf);
		this.dsf = dsf;
		this.rm_conf = rm_conf;
		
		ArrayList<String> columnsToEvaluate = new ArrayList<String>();
		Iterator<MatchingConfig> iterator = rm_conf.getMatchingConfigs().iterator();
		
		while (iterator.hasNext()) {
			MatchingConfig mc = iterator.next();
			System.out.println("Session Name is : " + mc.getName());
			for (String columnName : mc.getBlockingColumns()) {
				columnsToEvaluate.add(columnName);
				System.out.println(columnName);
			}
			for (String columnName : mc.getIncludedColumnsNames()) {
				columnsToEvaluate.add(columnName);
			}
		}
		
		Object[] obj = dsf.getFields().toArray();
		ArrayList<ColumnPair> colum_pair_list = new ArrayList<ColumnPair>();
		
		final ArrayList<String> collected = new ArrayList<String>();
		for (int x = 0; x < columnsToEvaluate.size(); x++) {
			for (int y = 0; y < columnsToEvaluate.size(); y++) {
				ColumnPair columnPair = new ColumnPair();
				if (y == x)
					continue;
				
				if (!columnsToEvaluate.get(x).equals(columnsToEvaluate.get(y))) {
					final String s1 = columnsToEvaluate.get(x) + " " + columnsToEvaluate.get(y);
					final String s2 = columnsToEvaluate.get(y) + " " + columnsToEvaluate.get(x);
					if (!collected.contains(s1) && !collected.contains(s2)) {
						collected.add(s1);
						columnPair.setColumnA(new Column(columnsToEvaluate.get(x), dsf));
						columnPair.setColumnB(new Column(columnsToEvaluate.get(y), dsf));
						colum_pair_list.add(columnPair);
					}
				}
			}
		}
		
		MICalculator mi_calculator = new MICalculator();
		mi_calculator.setColumn_pair_list(colum_pair_list);
		this.mi_calculator = mi_calculator;
		
		for (ColumnPair cp : colum_pair_list) {
			System.out.println("Column A is  :" + cp.getColumnA().getName());
			System.out.println("Column B is  :" + cp.getColumnB().getName());
		}
	}
	
	@Override
	public void finishAnalysis() {
		System.out.println("Entering finishAnalysis method...");
		List<ColumnPair> cp_list = this.mi_calculator.getColumn_pair_list();
		
		DataSourceFrequency dsf1 = dsf;
		List<String> pairList = new ArrayList<String>();
		Map<String, Map<String, Integer>> mp = new HashMap<String, Map<String, Integer>>();
		
		for (ColumnPair cp : cp_list) {
			List<Column> columns = new ArrayList<Column>();
			columns.add(cp.getColumnA());
			columns.add(cp.getColumnB());
			
			for (Column c : columns) {
				if (!pairList.contains(c.getName())) {
					Map<String, Integer> frequency = new HashMap<String, Integer>();
					Set<String> tokens = dsf1.getTokens(c.getName());
					
					for (String token : tokens) {
						System.out.println(token + " occured " + dsf1.getFrequency(c.getName(), token) + " times");
						frequency.put(token, dsf1.getFrequency(c.getName(), token));
					}
					c.setFrequency(frequency);
					pairList.add(c.getName());
					mp.put(c.getName(), frequency);
				} else {
					c.setFrequency((Map<String, Integer>) mp.get(c.getName()));
				}
			}
		}
		
		for (ColumnPair cp : cp_list) {
			System.out.println("Calculating MI scores...");
			List<Column> cols = new ArrayList<Column>();
			cols.add(cp.getColumnA());
			cols.add(cp.getColumnB());
			
			for (Column c : cols) {
				//double sum = 0;
				double result = 0;
				System.out.println(c.getName());
				Map<String, Integer> map = c.getFrequency();
				Iterator entries = map.entrySet().iterator();
				int i = 0;
				while (entries.hasNext()) {
					Entry thisEntry = (Entry) entries.next();
					Object value = thisEntry.getValue();
					i = i + (Integer) value;
				}
				
				Iterator entries2 = map.entrySet().iterator();
				while (entries2.hasNext()) {
					Entry thisEntry = (Entry) entries2.next();
					
					Token t = new Token();
					t.setToken_name((String) thisEntry.getKey());
					t.setFrequency((Integer) thisEntry.getValue());
					
					double probability = (t.getFrequency() / (double) i);
					t.setProbability(probability);
					System.out.println("Probability of " + thisEntry.getKey() + " is :" + t.getProbability());
					result -= probability * (Math.log(probability) / Math.log(2));
				}
				c.setConditionalEntropy(result);
				System.out.println("final entropy value is " + c.getConditionalEntropy() + " bites");
			}
		}
		
		for (ColumnPair cp : mi_calculator.getColumn_pair_list()) {
			HashMap<String, Integer> mapEntries = (HashMap<String, Integer>) cp.getCombinations();
			Integer total = 0;
			
			for (Object key : mapEntries.keySet()) {
				System.out.println("Key: " + (String) key + ", Value: " + mapEntries.get(key));
				total = total + (Integer) mapEntries.get(key);
			}
			
			cp.setPairwiseCombinationCount(total);
			System.out.println("Combination count is " + cp.getPairwiseCombinationCount());
		}
		
		for (ColumnPair cp : mi_calculator.getColumn_pair_list()) {
			HashMap mapEntries1 = (HashMap) cp.getCombinations();
			Iterator iterator = mapEntries1.entrySet().iterator();
			//double sum = 0;
			double result = 0;
			while (iterator.hasNext()) {
				Map.Entry pairs = (Map.Entry) iterator.next();
				
				System.out.println(pairs.getKey() + " occures " + pairs.getValue() + " times");
				int value = (Integer) pairs.getValue();
				double probability = (double) value / cp.getPairwiseCombinationCount();
				System.out.println("Probability of " + pairs.getKey() + " is :" + probability);
				
				result -= probability * (Math.log(probability) / Math.log(2));
				iterator.remove(); // avoids a ConcurrentModificationException
			}
			
			cp.setJointEntropy(result);
			System.out.println(cp.getColumnA().getConditionalEntropy());
			System.out.println(cp.getColumnB().getConditionalEntropy());
			System.out.println(result);
			System.out.println("MI score for columns " + cp.getColumnA().getName() + " " + cp.getColumnB().getName()
			        + " are :" + ((cp.getColumnA().getConditionalEntropy() + cp.getColumnB().getConditionalEntropy())
			                - cp.getJointEntropy()));
			cp.setMutualInformationScore(
			    (cp.getColumnA().getConditionalEntropy() + cp.getColumnB().getConditionalEntropy()) - cp.getJointEntropy());
		}
		writeMIScoresToFile();
	}
	
	private void writeMIScoresToFile() {
		//if(rm_conf.isDeduplication() || (!rm_conf.isDeduplication() && rm_conf.getDataSourceFrequencyf2().equals(dsf))){
		List<ColumnPair> columnPairs = mi_calculator.getColumn_pair_list();
		String s = "";
		for (ColumnPair cp : columnPairs) {
			s += cp.getColumnA().getName() + " " + cp.getColumnB().getName() + " " + cp.getMutualInformationScore() + "\n";
		}
		TextDisplayFrame tdf = new SaveTextDisplayFrame("Save MI Scores", s);
		//}	
	}
	
	@Override
	public void incrementCount(Record rec) {
		super.incrementCount(rec);
		System.out.println("Entering analyzeRecord method...");
		for (ColumnPair cp : mi_calculator.getColumn_pair_list()) {
			boolean flag = false;
			boolean insert = false;
			boolean increment = false;
			String key = null;
			
			String columnA = rec.getDemographic(cp.getColumnA().getName());
			String columnB = rec.getDemographic(cp.getColumnB().getName());
			
			HashMap<String, Integer> mapEntries = (HashMap<String, Integer>) cp.getCombinations();
			
			if (mapEntries.size() == 0) {
				mapEntries.put(columnA + columnB, 1);
				flag = true;
			}
			
			if (flag == false) {
				for (Map.Entry<String, Integer> entry : mapEntries.entrySet()) {
					if (!entry.getKey().equals(columnA + columnB)) {
						insert = true;
						key = columnA + columnB;
					} else {
						increment = true;
						key = columnA + columnB;
					}
				}
			}
			
			if (insert) {
				mapEntries.put(key, 1);
			}
			
			if (increment) {
				Integer i = mapEntries.get(key);
				i = i + 1;
				mapEntries.put(key, i);
			}
		}
	}
	
	@Override
	public boolean isAnalyzedDemographic(MatchingConfigRow mcr) {
		return false;
	}
	
	public RecMatchConfig getRm_conf() {
		return rm_conf;
	}
	
	public void setRm_conf(RecMatchConfig rm_conf) {
		this.rm_conf = rm_conf;
	}
	
	public MICalculator getMi_calculator() {
		return mi_calculator;
	}
	
	public void setMi_calculator(MICalculator mi_calculator) {
		this.mi_calculator = mi_calculator;
	}
	
}
