package org.regenstrief.linkage.analysis;

import java.util.Hashtable;

import org.regenstrief.linkage.MatchVector;

/**
 * Class stores frequencies of demographics within MatchVectors using
 * a RecordFrequencies object associated with each MatchVector
 * @author jegg
 *
 */

public class MatchVectorRecordFrequencies {

	protected Hashtable<MatchVector,RecordFrequencies> vectors;
	
	public MatchVectorRecordFrequencies(){
		vectors = new Hashtable<MatchVector,RecordFrequencies>();
	}
	
	public RecordFrequencies getFrequencies(MatchVector mv){
		RecordFrequencies rf = vectors.get(mv);
		if(rf == null){
			rf = new RecordFrequencies();
		}
		return rf;
	}
	
	public void setFrequencies(MatchVector mv, RecordFrequencies rf){
		vectors.put(mv, rf);
	}
}
