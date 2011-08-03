package org.regenstrief.linkage;

import java.util.ArrayList;
import java.util.List;

import org.regenstrief.linkage.analysis.Modifier;
import org.regenstrief.linkage.io.FormPairs;
import org.regenstrief.linkage.matchresult.MatchResultHandler;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.ScorePair;

/**
 * Class performs matching with the pairs of Records from a 
 * FormPairs object and the matching objects from the given MatchingConfig
 * object.
 * 
 * @author jegg
 *
 */

public class MatchingInstance {
	
	protected List<MatchResultHandler> handlers;
	protected ScorePair sp;
	protected FormPairs fp;
	
	public MatchingInstance(MatchingConfig mc, FormPairs fp){
		handlers = new ArrayList<MatchResultHandler>();
		sp = new ScorePair(mc);
		this.fp = fp;
	}
	
	public void addModifier(Modifier m){
		sp.addScoreModifier(m);
	}
	
	public void addMatchResultHandler(MatchResultHandler mrh){
		handlers.add(mrh);
	}
	
	public List<MatchResultHandler> getMatchResultHandlers(){
		return handlers;
	}
	
	public void Match(){
		Record[] pair;
		
		while((pair = fp.getNextRecordPair()) != null){
			MatchResult mr = sp.scorePair(pair[0], pair[1]);
			for(int i = 0; i < handlers.size(); i++){
				handlers.get(i).acceptMatchResult(mr);
			}
		}
		
		for(int i = 0; i < handlers.size(); i++){
			handlers.get(i).close();
		}
	}
}
