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
	
	// variables different from original Random Sample Analyzer
	Hashtable<Long,Integer> left_uid_index_occurrence;
	Hashtable<Long,Integer> right_uid_index_occurrence;
	Vector<Long> left_uids;
	Vector<Long> right_uids;
	
	public DedupRandomSampleAnalyzer(MatchingConfig mc, FormPairs fp){
		super(mc, fp);
		
	}
	
	protected void initAnalyzer(){
		rand = new Random();
		
		left_pair_entry = new Hashtable<Integer,List<Integer>>();
		right_pair_entry = new Hashtable<Integer,List<Integer>>();
		record_pairs = new Hashtable<Integer,Record>();
		demographic_agree_count = new Hashtable<String,Integer>();
		
		// initialize dedup hashtables
		left_uid_index_occurrence = new Hashtable<Long,Integer>();
		right_uid_index_occurrence = new Hashtable<Long,Integer>();
		left_uids = new Vector<Long>();
		right_uids = new Vector<Long>();
		
		int recordPairCount;
		if(fp instanceof LookupFormPairs){
			recordPairCount = ((LookupFormPairs)fp).size();
		} else {
			recordPairCount = countRecordPairs();
		}
		
		System.out.println("pair count: " + recordPairCount);
		
		sampleSize = mc.getRandomSampleSize();
		sample1 = new boolean[recordPairCount];
		sample2 = new boolean[recordPairCount];
		
		setIndexPairs(recordPairCount);
		
		pair_count = 0;
	}
	
	protected int countRecordPairs(){
		int pair_count = 0;
		Record[] pair = fp.getNextRecordPair();
		while(pair != null){
			Record r1 = pair[0];
			Record r2 = pair[1];
			long uid1 = r1.getUID();
			long uid2 = r2.getUID();
			
			Integer lo = left_uid_index_occurrence.get(uid1);
			if(lo == null){
				left_uid_index_occurrence.put(uid1, pair_count);
			}
			Integer ro = right_uid_index_occurrence.get(uid2);
			if(ro == null){
				right_uid_index_occurrence.put(uid2, pair_count);
			}
			
			if(!left_uids.contains(uid1)){
				left_uids.add(uid1);
			}
			if(!right_uids.contains(uid2)){
				right_uids.add(uid2);
			}
			
			pair_count++;
			pair = fp.getNextRecordPair();
		}
		
		return pair_count;
	}
	
	protected void setIndexPairs(int max_index){
		LookupFormPairs lfp = null;
		Hashtable<Integer,List<Integer>> pairs = null;
		if(fp instanceof LookupFormPairs){
			lfp = (LookupFormPairs)fp;
			pairs = new Hashtable<Integer,List<Integer>>();
		}
		
		// need to get two sets of random numbers, one for each data source
		for(int i = 0; i < sampleSize && max_index > 0; i++){
			
			long sampled_left_uid, sampled_right_uid;
			do{
				int left_uid_index = rand.nextInt(left_uids.size());
				int right_uid_index = rand.nextInt(right_uids.size());
				
				sampled_left_uid = left_uids.get(left_uid_index);
				sampled_right_uid = right_uids.get(right_uid_index);
				
				
			}while(sampled_left_uid == sampled_right_uid);
			int left_index = left_uid_index_occurrence.get(sampled_left_uid);
			int right_index = right_uid_index_occurrence.get(sampled_right_uid);
			
			if(lfp != null){
				// save index pairs
				int first_index;
				if(left_index < right_index){
					first_index = left_index;
				} else {
					first_index = right_index;
				}
				List<Integer> l = pairs.get(first_index);
				if(l == null){
					l = new ArrayList<Integer>();
					pairs.put(first_index, l);
				}
			} else {
				// block to set indexes when we don't have a LookupFormPairs
				sample1[left_index] = true;
				sample2[right_index] = true;
				
				List<Integer> left = left_pair_entry.get(left_index);
				if(left == null){
					left = new ArrayList<Integer>();
					left_pair_entry.put(left_index, left);
				}
				left.add(i);
				
				List<Integer> right = right_pair_entry.get(right_index);
				if(right == null){
					right = new ArrayList<Integer>();
					right_pair_entry.put(right_index, right);
				}
				right.add(i);
			}
		}
		
		if(lfp != null){
			// since we have a LookupFormPairs, we can checkSimilarity right now
			Iterator<Integer> it = pairs.keySet().iterator();
			Record r1;
			while(it.hasNext()){
				int first_index = it.next();
				List<Integer> l = pairs.get(first_index);
				Collections.sort(l);
				r1 = lfp.getRecordPair(first_index)[0];
				Iterator<Integer> it2 = l.iterator();
				int prev_index = -1;
				Record r2 = null;
				while(it2.hasNext()){
					int second_index = it2.next();
					if(second_index != prev_index){
						r2 = lfp.getRecordPair(second_index)[1];
					}
					checkSimilarity(r1, r2);
					prev_index = second_index;
				}
			}
		}
	}
	
	
}
