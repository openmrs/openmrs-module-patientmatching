package org.regenstrief.linkage.analysis;

/**
 * Class takes a list of DemographicValueAction objects
 * and uses them to inspect Record objects.  An example
 * of how this would be used is with medical patient data
 * that has generic information that would cause it to be
 * filtered out from any analysis.  Patients with last
 * names of "NONAME" or "DOE" or "XXXX" should be found and treated 
 * differently from normal Records. 
 *
 */

import org.regenstrief.linkage.*;
import java.util.*;

public class RecordFieldAnalyzer {
	
	// integer constants for actions
	public static final int DISCARD = 1;
	
	public static final int DO_NOTHING = 2;
	
	public static final int NULLIFY = 3;
	
	List<List<DemographicValueAction>> rules;
	
	public RecordFieldAnalyzer() {
		// initialize with default rules
		rules = getDefaultRules();
	}
	
	public RecordFieldAnalyzer(List<List<DemographicValueAction>> rules) {
		this.rules = rules;
	}
	
	public List<List<DemographicValueAction>> getDefaultRules() {
		ArrayList<List<DemographicValueAction>> defaults = new ArrayList<List<DemographicValueAction>>();
		DemographicValueAction rule;
		ArrayList<DemographicValueAction> rule_set;
		
		// last name is blank and first name is blank
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		rule = new DemographicValueAction("FN", "", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// last name = 'AAA' and first name contains 'DUPL'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "AAA", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		rule = new DemographicValueAction("FN", "DUPL", DISCARD, DemographicValueAction.CONTAINS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// lastname = 'CHECK-NAME'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "CHECK-NAME", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// lastname = 'DONOTUSE'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "DONOTUSE", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// last name = 'NONAME'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "NONAME", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// last name = 'REUSE'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "REUSE", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// last name begins with 'X-'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "X\\-", DISCARD, DemographicValueAction.MATCHES);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// last name has more than 1 digit
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "[0-9]*[0-9]", DISCARD, DemographicValueAction.MATCHES);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// first name has more than 1 digit
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("FN", "[0-9]*[0-9]", DISCARD, DemographicValueAction.MATCHES);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// last name = 'BUSINESS' and first name is blank
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "BUSINESS", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		rule = new DemographicValueAction("FN", "", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// last name = 'UNIDENTIFIED' and first name is blank
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "UNIDENTIFIED", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		rule = new DemographicValueAction("FN", "", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// last name starts with 'UNK' and firstname is blank
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "UNK", DISCARD, DemographicValueAction.MATCHES);
		rule_set.add(rule);
		rule = new DemographicValueAction("FN", "", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// last name = 'DOE' and first name starts with 'J'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "DOE", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		rule = new DemographicValueAction("FN", "^J", DISCARD, DemographicValueAction.MATCHES);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// last name starts with 'J' and first name = 'DOE'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("FN", "DOE", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		rule = new DemographicValueAction("LN", "^J", DISCARD, DemographicValueAction.MATCHES);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// last name = 'BUS' and first name = 'OFF'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "BUS", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		rule = new DemographicValueAction("FN", "OFF", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// last name = 'BOO' and first name = 'BOO'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("LN", "BOO", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		rule = new DemographicValueAction("FN", "BOO", DISCARD, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// modify these rules by having those fields cleared
		// first name = 'BABY'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("FN", "BABY", NULLIFY, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// first name = 'INFANT'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("FN", "INFANT", NULLIFY, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// first name = 'BOY'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("FN", "BOY", NULLIFY, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// first name = 'GIRL'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("FN", "GIRL", NULLIFY, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// first name = 'BABYBOY'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("FN", "BABYBOY", NULLIFY, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// first name = 'BABYGIRL'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("FN", "BABYGIRL", NULLIFY, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// DOB = '1/1/1900'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("MB", "1", NULLIFY, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		rule = new DemographicValueAction("DB", "1", NULLIFY, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		rule = new DemographicValueAction("YB", "1900", NULLIFY, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// telephone = '99999'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("FN", "99999", NULLIFY, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// telephone = '00000'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("FN", "BOO", NULLIFY, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// ZIP = '00000'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("ZIP", "00000", NULLIFY, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		// ZIP = '99999'
		rule_set = new ArrayList<DemographicValueAction>();
		rule = new DemographicValueAction("ZIP", "99999", NULLIFY, DemographicValueAction.EQUALS);
		rule_set.add(rule);
		defaults.add(rule_set);
		
		return defaults;
	}
	
	public int analyzeRecordFields(Record r) {
		int ret = DO_NOTHING;
		Iterator<List<DemographicValueAction>> it = rules.iterator();
		while (it.hasNext()) {
			List<DemographicValueAction> rule_set = it.next();
			boolean set_true = true;
			int action = DO_NOTHING;
			Iterator<DemographicValueAction> it2 = rule_set.iterator();
			while (it2.hasNext()) {
				DemographicValueAction rule = it2.next();
				action = rule.getAction();
				set_true = set_true && rule.recordMatches(r);
			}
			if (set_true) {
				// need to inspect the action value to see what to do
				if (action == DISCARD) {
					return action;
				} else if (action == NULLIFY) {
					// need to change the demographic to an empty string
					// set ret equal to NULLIFY to notify caller values changed
					// do not return here so that if further demographics need to be cleared,
					// they will be modified
					it2 = rule_set.iterator();
					while (it2.hasNext()) {
						DemographicValueAction rule = it2.next();
						String dem = rule.getDemographic();
						r.addDemographic(dem, "");
					}
					ret = NULLIFY;
				}
			}
			
		}
		return ret;
	}
	
}
