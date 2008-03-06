package org.regenstrief.linkage.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.ModifiedMatchResult;
import org.regenstrief.linkage.ScoreVector;
import org.regenstrief.linkage.ModifiedMatchResult.Operator;
import org.regenstrief.linkage.analysis.Modifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Class creates an XML document from a list of MatchResult
 * objects containing all the information of how the matching
 * performed.
 * 
 * @author jegg
 *
 */

public class MatchResultsXML {
	
	public static Document resultsToXML(List<MatchResult> results){
		Document ret = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try{
			DocumentBuilder builder = factory.newDocumentBuilder();
			ret = builder.newDocument();
			Element root = ret.createElement("matches");
			ret.appendChild(root);
			
			// iterate over results, creating document nodes for
			Iterator<MatchResult> it = results.iterator();
			while(it.hasNext()){
				MatchResult mr = it.next();
				root.appendChild(toDOMNode(mr, ret));
			}
		}
		catch(ParserConfigurationException pce){
			return null;
		}
		
		return ret;
	}
	
	public static Node toDOMNode(MatchResult mr, Document doc){
		Element ret = doc.createElement("pair");
		
		// add metadata
		Element metadata = doc.createElement("metadata");
		Element score = doc.createElement("score");
		Element sens = doc.createElement("sensitivity");
		Element spec = doc.createElement("specificity");
		score.setTextContent(Double.toString(mr.getScore()));
		sens.setTextContent(Double.toString(mr.getSensitivity()));
		spec.setTextContent(Double.toString(mr.getSpecificity()));
		ret.appendChild(metadata);
		metadata.appendChild(score);
		metadata.appendChild(sens);
		metadata.appendChild(spec);
		
		// add modifier information
		if(mr instanceof ModifiedMatchResult){
			ModifiedMatchResult mmr = (ModifiedMatchResult)mr;
			ret.appendChild(getModifierNodes(mmr, doc));
		}
		
		// iterate over fields
		Iterator<String> it = mr.getDemographics().iterator();
		Element fields = doc.createElement("fields");
		while(it.hasNext()){
			String demographic = it.next();
			Element field = doc.createElement("field");
			field.setAttribute("label", demographic);
			fields.appendChild(field);
			
			Element val_a = doc.createElement("valueA");
			Element val_b = doc.createElement("valueB");
			Element matched = doc.createElement("matched");
			Element score_val = doc.createElement("score_value");
			Element comparator = doc.createElement("comparator");
			
			val_a.setTextContent(mr.getRecord1().getDemographic(demographic));
			val_b.setTextContent(mr.getRecord2().getDemographic(demographic));
			if(mr.matchedOn(demographic)){
				matched.setTextContent("true");
			} else {
				matched.setTextContent("false");
			}
			score_val.setTextContent(Double.toString(mr.getScoreVector().getScore(demographic)));
			comparator.setTextContent(MatchingConfig.ALGORITHMS[mr.getMatchingConfig().getAlgorithm(mr.getMatchingConfig().getRowIndexforName(demographic))]);
			
			field.appendChild(val_a);
			field.appendChild(val_b);
			field.appendChild(matched);
			field.appendChild(score_val);
			field.appendChild(comparator);
		}
		ret.appendChild(fields);
		
		return ret;
	}
	
	private static Node getModifierNodes(ModifiedMatchResult mr, Document doc){
		Element ret = doc.createElement("modifiers");
		
		// write information about base score, before modifications are done
		Element mod_base = doc.createElement("base_values");
		MatchResult base_mr = mr.getBasicMatchResult();
		Iterator<String> demo_it = base_mr.getDemographics().iterator();
		ScoreVector sv = base_mr.getScoreVector();
		while(demo_it.hasNext()){
			String current_field = demo_it.next();
			Element base_field = doc.createElement("base_field");
			base_field.setAttribute("label", current_field);
			Element base_value = doc.createElement("base_value");
			double score = sv.getScore(current_field);
			base_value.setTextContent(Double.toString(score));
			base_field.appendChild(base_value);
			mod_base.appendChild(base_field);
		}
		ret.appendChild(mod_base);
		
		// iterate over modifiers, list should store order of application
		List<Modifier> modifiers = mr.getModifiers();
		Iterator<Modifier> it = modifiers.iterator();
		int order = 1;
		while(it.hasNext()){
			Modifier m = it.next();
			Element mod_element = doc.createElement("modifier");
			mod_element.setAttribute("label", m.getModifierName());
			mod_element.setAttribute("order", Integer.toString(order));
			ret.appendChild(mod_element);
			
			Hashtable<String,Double> mods = mr.getModifications().get(m);
			Iterator<String> it2 = mods.keySet().iterator();
			while(it2.hasNext()){
				String demographic = it2.next();
				Double d = mods.get(demographic);
				Element field_element = doc.createElement("field_modification");
				field_element.setAttribute("field", demographic);
				mod_element.appendChild(field_element);
				Element mod_value = doc.createElement("modification");
				mod_value.setTextContent(d.toString());
				field_element.appendChild(mod_value);
				Element mod_operator = doc.createElement("operator");
				Operator o = mr.getModifierOperators().get(m);
				if(o.equals(ModifiedMatchResult.Operator.MULTIPLY)){
					mod_operator.setTextContent("MULTIPLY");
				} else {
					mod_operator.setTextContent("PLUS");
				}
				field_element.appendChild(mod_operator);
			}
			
			
		}
		
		return ret;
	}
}
