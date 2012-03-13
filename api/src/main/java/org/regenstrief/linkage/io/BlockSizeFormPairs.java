package org.regenstrief.linkage.io;

import java.util.ArrayList;
import java.util.List;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;

public class BlockSizeFormPairs extends FormPairs {

	public static final int DEFAULT_SIZE_LIMIT = 1000;
	
	private FormPairs fp;
	private int size_limit;
	private List<Record[]> pairs;
	private Record[] pattern;
	private Record[] next_block_start;
	
	public BlockSizeFormPairs(MatchingConfig mc, FormPairs fp) {
		super(mc);
		this.fp = fp;
		size_limit = DEFAULT_SIZE_LIMIT;
		pairs = new ArrayList<Record[]>();
	}

	public BlockSizeFormPairs(MatchingConfig mc, FormPairs fp, int limit) {
		super(mc);
		this.fp = fp;
		size_limit = limit;
		pairs = new ArrayList<Record[]>();
	}
	
	public void setSizeLimit(int limit){
		size_limit = limit;
	}
	
	private void fillBuffer(){
		
		boolean spill = false;
		do{
			pattern = next_block_start;
			do{
				next_block_start = fp.getNextRecordPair();

				if(pattern == null){
					pattern = next_block_start;
				}

				if(next_block_start != null && !spill){
					pairs.add(next_block_start);
					if(pairs.size() > size_limit){
						spill = true;
						pairs.clear();
					}
				}
			}while(next_block_start != null && matchesPattern(next_block_start));
		}while(spill);
	}
	
	@Override
	public Record[] getNextRecordPair() {
		Record[] ret = null;
		boolean end_of_pairs = false;
		
		while(!end_of_pairs){
			if(pairs.size() > 0){
				ret = pairs.remove(0);
			} else {
				fillBuffer();
				if(pairs.size() == 0){
					end_of_pairs = true;
				}
			}
		}
		
		return ret;
	}
	
	private boolean matchesPattern(Record[] pair){
		String[] b_cols = mc.getBlockingColumns();
		boolean matches = true;
		for(int i = 0; i < b_cols.length; i++){
			String demographic = b_cols[i];
			String dem1 = pair[0].getDemographic(demographic);
			String dem2 = pair[0].getDemographic(demographic);
			matches = matches && compareString(dem1, dem2, MatchingConfig.STRING_TYPE, mc.getMatchingConfigRowByName(demographic).getBlockChars());
		}
		return false;
	}
	
	private boolean compareString(String str1, String str2, int type, int n){
		boolean ret = false;
		double d1, d2;
		
		if(str1.equals("") && str2.equals("")){
			// setting two null values as unequal prevents forming pairs of records
			// on empty blocking columns
			ret = false;
		} else if(type == MatchingConfig.NUMERIC_TYPE){
			try{
				d1 = Double.parseDouble(str1);
			}
			catch(NumberFormatException nfe){
				//throw new ComparisonException("Number format exception");
				return false;
			}
			try{
				d2 = Double.parseDouble(str2);
			}
			catch(NumberFormatException nfe){
				//throw new ComparisonException("Number format exception");
				return false;
			}
			if(d1 < d2){
				ret = false;
			} else if(d1 == d2){
				ret = true;
			} else {
				ret = false;
			}
		} else if(type == MatchingConfig.STRING_TYPE){
			// if strings are longer than n, then need to get substrings
			if(str1.length() > n){
				str1 = str1.substring(0, n);
			}
			if(str2.length() > n){
				str2 = str2.substring(0, n);
			}
			int comp = str1.compareToIgnoreCase(str2);
			if(comp < 0){
				ret = false;
			} else if(comp == 0){
				ret = true;
			} else {
				ret = false;
			}
		}
		
		return ret;
	}

}
