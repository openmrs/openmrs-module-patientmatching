package org.regenstrief.linkage.analysis;

import java.util.Hashtable;

import org.regenstrief.linkage.MatchVector;

/**
 * Class stores frequencies of demographics within MatchVectors using a RecordFrequencies object
 * associated with each MatchVector
 * 
 * @author jegg
 */

public class MatchVectorRecordFrequencies {
	
	public enum Side {
		LEFT,
		RIGHT
	};
	
	protected Hashtable<MatchVector, RecordFrequencies> right_vectors;
	
	protected Hashtable<MatchVector, RecordFrequencies> left_vectors;
	
	public MatchVectorRecordFrequencies() {
		left_vectors = new Hashtable<MatchVector, RecordFrequencies>();
		right_vectors = new Hashtable<MatchVector, RecordFrequencies>();
	}
	
	public RecordFrequencies getFrequencies(Side s, MatchVector mv) {
		Hashtable<MatchVector, RecordFrequencies> vectors;
		if (s == Side.LEFT) {
			vectors = left_vectors;
		} else {
			vectors = right_vectors;
		}
		RecordFrequencies rf = vectors.get(mv);
		return rf;
	}
	
	public void setFrequencies(Side s, MatchVector mv, RecordFrequencies rf) {
		Hashtable<MatchVector, RecordFrequencies> vectors;
		if (s == Side.LEFT) {
			vectors = left_vectors;
		} else {
			vectors = right_vectors;
		}
		vectors.put(mv, rf);
	}
}
