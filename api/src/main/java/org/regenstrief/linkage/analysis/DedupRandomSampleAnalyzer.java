package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.io.FormPairs;
import org.regenstrief.linkage.io.LookupFormPairs;
import org.regenstrief.linkage.util.MatchingConfig;

public class DedupRandomSampleAnalyzer extends RandomSampleAnalyzer {
	
	Hashtable<Long, Integer> right_uid_index_map;
	
	Hashtable<Long, Integer> left_uid_index_map;
	
	Hashtable<Long, Boolean> uid_list;
	
	public DedupRandomSampleAnalyzer(MatchingConfig mc, FormPairs fp) {
		super(mc, fp);
	}
	
	protected void initAnalyzer() {
		rand = new Random();
		
		left_pair_entry = new Hashtable<Integer, List<Integer>>();
		right_pair_entry = new Hashtable<Integer, List<Integer>>();
		record_pairs = new Hashtable<Integer, Record>();
		demographic_agree_count = new Hashtable<String, Integer>();
		
		// initialize dedup hashtables
		right_uid_index_map = new Hashtable<Long, Integer>();
		left_uid_index_map = new Hashtable<Long, Integer>();
		uid_list = new Hashtable<Long, Boolean>();
		
		if (fp instanceof LookupFormPairs) {
			pair_count = ((LookupFormPairs) fp).size();
		} else {
			pair_count = countRecordPairs();
		}
		
		System.out.println("pair count: " + pair_count);
		
		sampleSize = mc.getRandomSampleSize();
		sample1 = new boolean[pair_count];
		sample2 = new boolean[pair_count];
		
		setIndexPairs(uid_list.size());
		pair_count = 0;
	}
	
	protected int countRecordPairs() {
		int pair_count = 0;
		Record[] pair = fp.getNextRecordPair();
		while (pair != null) {
			Record r1 = pair[0];
			Record r2 = pair[1];
			long uid1 = r1.getUID();
			long uid2 = r2.getUID();
			
			if (left_uid_index_map.get(uid1) == null) {
				left_uid_index_map.put(uid1, pair_count);
			}
			if (right_uid_index_map.get(uid2) == null) {
				right_uid_index_map.put(uid2, pair_count);
			}
			
			uid_list.put(uid1, true);
			uid_list.put(uid2, true);
			
			pair_count++;
			pair = fp.getNextRecordPair();
		}
		
		return pair_count;
	}
	
	protected void setIndexPairs(int max_index) {
		LookupFormPairs lfp = null;
		Hashtable<Integer, List<Integer>> pairs = null;
		if (fp instanceof LookupFormPairs) {
			lfp = (LookupFormPairs) fp;
			pairs = new Hashtable<Integer, List<Integer>>();
		}
		
		Vector<Long> uids = new Vector<Long>();
		uids.addAll(uid_list.keySet());
		
		// need to get two sets of random numbers, one for each data source
		for (int i = 0; i < sampleSize && max_index > 0; i++) {
			
			Integer sampled_left_uid, sampled_right_uid;
			long left_uid, right_uid;
			int left_uid_index, right_uid_index;
			do {
				left_uid_index = rand.nextInt(max_index);
				right_uid_index = rand.nextInt(max_index);
				
				left_uid = uids.get(left_uid_index);
				right_uid = uids.get(right_uid_index);
				
				sampled_left_uid = left_uid_index_map.get(left_uid);
				sampled_right_uid = right_uid_index_map.get(right_uid);
			} while (sampled_left_uid == null || sampled_right_uid == null || left_uid == right_uid);
			int left_index = sampled_left_uid;
			int right_index = sampled_right_uid;
			
			//System.out.println("pair " + left_uid + "," + right_uid + "\tpair index of " + left_index + "," + right_index);
			
			if (lfp != null) {
				// save index pairs
				int first_index;
				if (left_index < right_index) {
					first_index = left_index;
				} else {
					first_index = right_index;
				}
				List<Integer> l = pairs.get(first_index);
				if (l == null) {
					l = new ArrayList<Integer>();
					pairs.put(first_index, l);
				}
			} else {
				// block to set indexes when we don't have a LookupFormPairs
				sample1[left_index] = true;
				sample2[right_index] = true;
				
				List<Integer> left = left_pair_entry.get(left_index);
				if (left == null) {
					left = new ArrayList<Integer>();
					left_pair_entry.put(left_index, left);
				}
				left.add(i);
				
				List<Integer> right = right_pair_entry.get(right_index);
				if (right == null) {
					right = new ArrayList<Integer>();
					right_pair_entry.put(right_index, right);
				}
				right.add(i);
			}
		}
		
		if (lfp != null) {
			// since we have a LookupFormPairs, we can checkSimilarity right now
			Iterator<Integer> it = pairs.keySet().iterator();
			Record r1;
			while (it.hasNext()) {
				int first_index = it.next();
				List<Integer> l = pairs.get(first_index);
				Collections.sort(l);
				r1 = lfp.getRecordPair(first_index)[0];
				Iterator<Integer> it2 = l.iterator();
				int prev_index = -1;
				Record r2 = null;
				while (it2.hasNext()) {
					int second_index = it2.next();
					if (second_index != prev_index) {
						r2 = lfp.getRecordPair(second_index)[1];
					}
					checkSimilarity(r1, r2);
					prev_index = second_index;
				}
			}
		}
	}
}
