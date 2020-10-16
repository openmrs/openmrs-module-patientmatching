package org.regenstrief.linkage;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.regenstrief.linkage.analysis.Modifier;

/**
 * Class used by analysis modifiers for when a basic matching score is modified. This takes the
 * basic MatchResult object, but stores the
 * 
 * @author jegg
 */

public class ModifiedMatchResult extends MatchResult {
	
	public static enum Operator {
		PLUS,
		MULTIPLY
	};
	
	protected MatchResult base;
	
	// modifications to the demographic score values in order that they are to be applied
	protected Vector<Modifier> demographic_scalars_order;
	
	protected Hashtable<Modifier, Hashtable<String, Double>> demographic_modifications;
	
	// association of modifier to what operation (add or multiply)
	protected Hashtable<Modifier, Operator> demographic_scalar_operations;
	
	public ModifiedMatchResult(MatchResult mr) {
		super(mr.getScore(), mr.getInclusiveScore(), mr.getTrueProbability(), mr.getFalseProbability(), mr.getSensitivity(),
		        mr.getSpecificity(), mr.getMatchVector(), mr.getScoreVector(), mr.getRecord1(), mr.getRecord2(),
		        mr.getMatchingConfig());
		base = mr;
		demographic_scalars_order = new Vector<Modifier>();
		demographic_modifications = new Hashtable<Modifier, Hashtable<String, Double>>();
		demographic_scalar_operations = new Hashtable<Modifier, Operator>();
	}
	
	public ModifiedMatchResult(ModifiedMatchResult mr) {
		super(mr.getScore(), mr.getInclusiveScore(), mr.getTrueProbability(), mr.getFalseProbability(), mr.getSensitivity(),
		        mr.getSpecificity(), mr.getMatchVector(), mr.getScoreVector(), mr.getRecord1(), mr.getRecord2(),
		        mr.getMatchingConfig());
		base = mr.getBasicMatchResult();
		demographic_scalars_order = new Vector<Modifier>(mr.getModifiers());
		demographic_modifications = mr.getModifications();
		demographic_scalar_operations = mr.getModifierOperators();
	}
	
	public Hashtable<Modifier, Hashtable<String, Double>> getModifications() {
		return demographic_modifications;
	}
	
	public Hashtable<Modifier, Operator> getModifierOperators() {
		return demographic_scalar_operations;
	}
	
	public List<Modifier> getModifiers() {
		return demographic_scalars_order;
	}
	
	public void addDemographicScalarModifier(Modifier m, String demographic, double d, Operator o) {
		// set operator for this modifier
		demographic_scalar_operations.put(m, o);
		
		// if this modifier isn't in the list yet, add to end
		if (!demographic_scalars_order.contains(m)) {
			demographic_scalars_order.add(m);
		}
		
		// need to find the hashtable in demographic 
		Hashtable<String, Double> entry = demographic_modifications.get(m);
		if (entry == null) {
			entry = new Hashtable<String, Double>();
			demographic_modifications.put(m, entry);
		}
		
		entry.put(demographic, new Double(d));
	}
	
	public MatchResult getBasicMatchResult() {
		return base;
	}
	
	public double getBaseScore() {
		return super.getScore();
	}
	
	public double getScore() {
		ScoreVector sv = getScoreVector();
		return sv.getTotalScore();
	}
	
	public ScoreVector getScoreVector() {
		ScoreVector ret = new ScoreVector();
		ScoreVector base = super.getScoreVector();
		
		// set base values in ret
		List<String> demographics = base.getDemographics();
		Iterator<String> it = demographics.iterator();
		while (it.hasNext()) {
			String demographic = it.next();
			ret.setScore(demographic, base.getScore(demographic));
		}
		
		// iterate over demographic_scalars_order, and use the modifications to change
		// the scores stored in ret
		Iterator<Modifier> it2 = demographic_scalars_order.iterator();
		while (it2.hasNext()) {
			Modifier m = it2.next();
			Operator o = demographic_scalar_operations.get(m);
			Hashtable<String, Double> modifications = demographic_modifications.get(m);
			if (modifications != null) {
				Iterator<String> it3 = modifications.keySet().iterator();
				while (it3.hasNext()) {
					String demographic = it3.next();
					double scalar = modifications.get(demographic);
					double current_score = ret.getScore(demographic);
					if (o == Operator.PLUS) {
						ret.setScore(demographic, current_score + scalar);
					} else if (o == Operator.MULTIPLY) {
						ret.setScore(demographic, current_score * scalar);
					}
				}
			}
		}
		
		return ret;
	}
}
