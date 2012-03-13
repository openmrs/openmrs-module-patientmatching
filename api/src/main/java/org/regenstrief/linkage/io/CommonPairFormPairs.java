package org.regenstrief.linkage.io;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Class is a FormPairs object that takes a list of other FormPairs in its constructor.  It passes through
 * pairs from the multiple FormPairs, but each pair is only passed through once.
 * 
 * The test for equivalency currently depends on which side of the pair an ID appears.  So pair
 * 1,2 and 2,1 will both be passed through even if the pair is actually the same.
 * 
 *
 */

public class CommonPairFormPairs extends FormPairs {

	Stack<FormPairs> fps;
	List<MatchingConfig> mcs;
	FormPairs current_fp;
	MatchingConfig current_mc;
	Hashtable<Long,Hashtable<Long,Boolean>> skip_pair;
	
	int pair_count;
	
	public CommonPairFormPairs(List<FormPairs> formpairs){
		super(null);
		pair_count = 0;
		fps = new Stack<FormPairs>();
		skip_pair = new Hashtable<Long,Hashtable<Long,Boolean>>();
		
		fps.addAll(formpairs);
		mcs = new ArrayList<MatchingConfig>();
		Iterator<FormPairs> it = fps.iterator();
		while(it.hasNext()){
			mcs.add(it.next().getMatchingConfig());
		}
		
		if(fps.size() > 0){
			current_fp = fps.pop();
			current_mc = current_fp.getMatchingConfig();
			mcs.remove(current_mc);
		}
		
	}
	
	@Override
	public Record[] getNextRecordPair() {
		Record[] ret = null;
		boolean skip = false;
		
		do{
			while(ret == null && current_fp != null){
				ret = current_fp.getNextRecordPair();
				if(ret == null && fps.size() > 0){
					current_fp = fps.pop();
					System.out.println("returned " + pair_count + " pairs from completed FP");
					pair_count = 0;
					current_mc = current_fp.getMatchingConfig();
					mcs.remove(current_mc);
				} else if(ret == null){
					System.out.println("returned " + pair_count + " pairs from completed FP");
					current_fp = null;
				}
			}


			// check if record pair needs to be skipped
			skip = false;
			if(ret != null){
				long uid1 = ret[0].getUID();
				long uid2 = ret[1].getUID();
				Hashtable<Long,Boolean> bucket = skip_pair.get(uid1);
				
				if(bucket != null){
					Boolean s = bucket.get(uid2);
					if(s != null && s){
						skip = true;
					}
				}
				// if pair matches on other blocking schemes, then save in skip_pair
				if(fps.size() > 0 && isBlockingEquivalent(ret[0],ret[1])){
					if(bucket == null){
						bucket = new Hashtable<Long,Boolean>();
						skip_pair.put(uid1, bucket);
					}
					bucket.put(uid2, Boolean.TRUE);

				}
			}
			if(skip){
				ret = null;
			}
		} while(skip && current_fp != null);
		
		
		pair_count++;
		return ret;
	}
	
	/**
	 * Method returns whether the two records wiould be paired in any of the MatchingConfig objects in mcs
	 * 
	 * @param r1	the first Record
	 * @param r2	the second Record
	 * @return	true if the Records have equal blocking column values for any MatchingConfig objects in mcs
	 */
	private boolean isBlockingEquivalent(Record r1, Record r2){
		Iterator<MatchingConfig> it = mcs.iterator();
		boolean ret = false;
		while(it.hasNext() && ret == false){
			MatchingConfig mc = it.next();
			boolean equal = true;
			String[] bcols = mc.getBlockingColumns();
			for(int i = 0; i < bcols.length; i++){
				String col = bcols[i];
				String val1 = r1.getDemographic(col);
				String val2 = r2.getDemographic(col);
				equal = equal && val1.equals(val2);
			}
			if(equal){
				ret = true;
			}
		}
		
		return ret;
	}

}
