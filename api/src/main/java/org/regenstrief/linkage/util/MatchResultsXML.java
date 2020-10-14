package org.regenstrief.linkage.util;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.ModifiedMatchResult;
import org.regenstrief.linkage.RecordLink;
import org.regenstrief.linkage.SameEntityRecordGroup;
import org.regenstrief.linkage.ScoreVector;
import org.regenstrief.linkage.ModifiedMatchResult.Operator;
import org.regenstrief.linkage.analysis.Modifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Class creates an XML document from a list of MatchResult objects containing all the information
 * of how the matching performed.
 * 
 * @author jegg
 */

public class MatchResultsXML {
	
	public static Document resultsToXML(List<MatchResult> results) {
		Document ret = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			ret = builder.newDocument();
			Element root = ret.createElement("matches");
			ret.appendChild(root);
			
			// iterate over results, creating document nodes for
			Iterator<MatchResult> it = results.iterator();
			while (it.hasNext()) {
				MatchResult mr = it.next();
				root.appendChild(toDOMNode(mr, ret));
			}
		}
		catch (ParserConfigurationException pce) {
			return null;
		}
		
		return ret;
	}
	
	public static boolean groupsToXML(List<SameEntityRecordGroup> groups, File f) {
		try {
			FileWriter out = new FileWriter(f);
			StreamResult streamResult = new StreamResult(out);
			SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
			// SAX2.0 ContentHandler.
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			//serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			hd.setResult(streamResult);
			
			hd.startDocument();
			AttributesImpl atts = new AttributesImpl();
			
			hd.startElement("", "", "groups", atts);
			Iterator<SameEntityRecordGroup> it = groups.iterator();
			while (it.hasNext()) {
				SameEntityRecordGroup group = it.next();
				atts.addAttribute("", "", "group_ID", "CDATA", Integer.toString(group.getGroupID()));
				hd.startElement("", "", "group", atts);
				
				// for each group, iterate over match results as well
				List<RecordLink> links = group.getGroupLinks();
				Iterator<RecordLink> links_it = links.iterator();
				while (links_it.hasNext()) {
					RecordLink rl = links_it.next();
					if (rl instanceof MatchResult) {
						MatchResult mr = (MatchResult) rl;
						atts.clear();
						hd.startElement("", "", "matchresult", atts);
						
						// add metadata
						hd.startElement("", "", "metadata", atts);
						char[] score_chars = Double.toString(mr.getScore()).toCharArray();
						char[] sens_chars = Double.toString(mr.getSensitivity()).toCharArray();
						char[] spec_chars = Double.toString(mr.getSpecificity()).toCharArray();
						char[] rec1_id = Long.toString(mr.getRecord1().getUID()).toCharArray();
						char[] rec2_id = Long.toString(mr.getRecord2().getUID()).toCharArray();
						
						hd.startElement("", "", "score", atts);
						hd.characters(score_chars, 0, score_chars.length);
						hd.endElement("", "", "score");
						hd.startElement("", "", "sensitivity", atts);
						hd.characters(sens_chars, 0, sens_chars.length);
						hd.endElement("", "", "sensitivity");
						hd.startElement("", "", "specificity", atts);
						hd.characters(spec_chars, 0, spec_chars.length);
						hd.endElement("", "", "specificity");
						hd.startElement("", "", "rec1_ID", atts);
						hd.characters(rec1_id, 0, rec1_id.length);
						hd.endElement("", "", "rec1_ID");
						hd.startElement("", "", "rec2_ID", atts);
						hd.characters(rec2_id, 0, rec2_id.length);
						hd.endElement("", "", "rec2_ID");
						
						hd.endElement("", "", "metadata");
						
						// if it's a modified match result, write modifier information
						if (mr instanceof ModifiedMatchResult) {
							ModifiedMatchResult mmr = (ModifiedMatchResult) mr;
							atts.clear();
							hd.startElement("", "", "modifiers", atts);
							
							MatchResult base_mr = mmr.getBasicMatchResult();
							Iterator<String> demo_it = base_mr.getDemographics().iterator();
							ScoreVector sv = base_mr.getScoreVector();
							hd.startElement("", "", "base_values", atts);
							while (demo_it.hasNext()) {
								String current_field = demo_it.next();
								atts.addAttribute("", "", "label", "CDATA", current_field);
								hd.startElement("", "", "base_field", atts);
								atts.clear();
								hd.startElement("", "", "base_value", atts);
								String base_val = Double
								        .toString(mmr.getBasicMatchResult().getScoreVector().getScore(current_field));
								hd.characters(base_val.toCharArray(), 0, base_val.length());
								hd.endElement("", "", "base_value");
								hd.endElement("", "", "base_field");
							}
							hd.endElement("", "", "base_values");
							
							List<Modifier> modifiers = mmr.getModifiers();
							Iterator<Modifier> it2 = modifiers.iterator();
							int order = 1;
							while (it2.hasNext()) {
								Modifier m = it2.next();
								atts.clear();
								atts.addAttribute("", "", "label", "CDATA", m.getModifierName());
								atts.addAttribute("", "", "order", "CDATA", Integer.toString(order));
								hd.startElement("", "", "modifier", atts);
								
								Hashtable<String, Double> mods = mmr.getModifications().get(m);
								Iterator<String> it3 = mods.keySet().iterator();
								while (it3.hasNext()) {
									String demographic = it3.next();
									Double d = mods.get(demographic);
									atts.clear();
									atts.addAttribute("", "", "field", "CDATA", demographic);
									hd.startElement("", "", "field_modification", atts);
									atts.clear();
									hd.startElement("", "", "modification", atts);
									String mod = Double.toString(d);
									hd.characters(mod.toCharArray(), 0, mod.length());
									hd.endElement("", "", "modification");
									hd.startElement("", "", "operator", atts);
									Operator o = mmr.getModifierOperators().get(m);
									if (o.equals(ModifiedMatchResult.Operator.MULTIPLY)) {
										hd.characters("MULTIPLEY".toCharArray(), 0, "MULTIPLY".length());
									} else {
										hd.characters("PLUS".toCharArray(), 0, "PLUS".length());
									}
									hd.endElement("", "", "operator");
									
									hd.endElement("", "", "field_modification");
								}
								
								hd.endElement("", "", "modifier");
								order++;
							}
							
							hd.endElement("", "", "modifiers");
						}
						
						// iterate over fields
						hd.startElement("", "", "fields", atts);
						Iterator<String> it2 = mr.getDemographics().iterator();
						while (it2.hasNext()) {
							String demographic = it2.next();
							// set field attributes
							atts.addAttribute("", "", "label", "CDATA", demographic);
							atts.addAttribute("", "", "type", "CDATA", "match");
							hd.startElement("", "", "field", atts);
							atts.clear();
							
							hd.startElement("", "", "valueA", atts);
							String val_a = mr.getRecord1().getDemographic(demographic);
							hd.characters(val_a.toCharArray(), 0, val_a.length());
							hd.endElement("", "", "valueA");
							hd.startElement("", "", "valueB", atts);
							String val_b = mr.getRecord2().getDemographic(demographic);
							hd.characters(val_b.toCharArray(), 0, val_b.length());
							hd.endElement("", "", "valueB");
							hd.startElement("", "", "matched", atts);
							if (mr.matchedOn(demographic)) {
								hd.characters("true".toCharArray(), 0, "true".length());
							} else {
								hd.characters("false".toCharArray(), 0, "false".length());
							}
							hd.endElement("", "", "matched");
							hd.startElement("", "", "score_value", atts);
							String score = Double.toString(mr.getScoreVector().getScore(demographic));
							hd.characters(score.toCharArray(), 0, score.length());
							hd.endElement("", "", "score_value");
							hd.startElement("", "", "comparator", atts);
							String comp = MatchingConfig.ALGORITHMS[mr.getMatchingConfig()
							        .getAlgorithm(mr.getMatchingConfig().getRowIndexforName(demographic))];
							hd.characters(comp.toCharArray(), 0, comp.length());
							hd.endElement("", "", "comparator");
							hd.startElement("", "", "similarity", atts);
							String sim = Double.toString(mr.getSimilarityScore(demographic));
							hd.characters(sim.toCharArray(), 0, sim.length());
							hd.endElement("", "", "similarity");
							
							hd.endElement("", "", "field");
						}
						// iterate over demographics not used in the matching, but present in the Record objects
						Collection<String> demographics = new ArrayList<String>();
						demographics.addAll(mr.getRecord1().getDemographics().keySet());
						//demographics.addAll(mr.getRecord2().getDemographics().keySet());
						
						Iterator<String> it3 = demographics.iterator();
						List<String> matched_demographics = mr.getMatchVector().getDemographics();
						String[] bd = mr.getMatchingConfig().getBlockingColumns();
						List<String> blocking_demographics = new ArrayList<String>();
						for (int i = 0; i < bd.length; i++) {
							blocking_demographics.add(bd[i]);
						}
						while (it3.hasNext()) {
							String dem = it3.next();
							if (!matched_demographics.contains(dem)) {
								atts.clear();
								atts.addAttribute("", "", "label", "CDATA", dem);
								if (blocking_demographics.contains(dem)) {
									atts.addAttribute("", "", "type", "CDATA", "block");
								} else {
									atts.addAttribute("", "", "type", "CDATA", "display");
								}
								hd.startElement("", "", "field", atts);
								
								atts.clear();
								hd.startElement("", "", "valueA", atts);
								String val_a = mr.getRecord1().getDemographic(dem);
								hd.characters(val_a.toCharArray(), 0, val_a.length());
								hd.endElement("", "", "valueA");
								hd.startElement("", "", "valueB", atts);
								String val_b = mr.getRecord2().getDemographic(dem);
								hd.characters(val_b.toCharArray(), 0, val_b.length());
								hd.endElement("", "", "valueB");
								
								hd.endElement("", "", "field");
							}
						}
						
						//atts.addAttribute("","","ID","CDATA",id[i]);
						//atts.addAttribute("","","TYPE","CDATA",type[i]);
						
						//hd.characters(desc[i].toCharArray(),0,desc[i].length());
						hd.endElement("", "", "fields");
						hd.endElement("", "", "matchresult");
					}
					
				}
				hd.endElement("", "", "group");
			}
			hd.endElement("", "", "groups");
			hd.endDocument();
		}
		catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	public static boolean resultsToXML(List<MatchResult> results, File f) {
		try {
			FileWriter out = new FileWriter(f);
			StreamResult streamResult = new StreamResult(out);
			SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
			// SAX2.0 ContentHandler.
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			//serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			hd.setResult(streamResult);
			
			hd.startDocument();
			AttributesImpl atts = new AttributesImpl();
			hd.startElement("", "", "matches", atts);
			
			Iterator<MatchResult> it = results.iterator();
			while (it.hasNext()) {
				MatchResult mr = it.next();
				atts.clear();
				hd.startElement("", "", "pair", atts);
				
				// add metadata
				hd.startElement("", "", "metadata", atts);
				char[] score_chars = Double.toString(mr.getScore()).toCharArray();
				char[] sens_chars = Double.toString(mr.getSensitivity()).toCharArray();
				char[] spec_chars = Double.toString(mr.getSpecificity()).toCharArray();
				char[] rec1_id = Long.toString(mr.getRecord1().getUID()).toCharArray();
				char[] rec2_id = Long.toString(mr.getRecord2().getUID()).toCharArray();
				
				hd.startElement("", "", "score", atts);
				hd.characters(score_chars, 0, score_chars.length);
				hd.endElement("", "", "score");
				hd.startElement("", "", "sensitivity", atts);
				hd.characters(sens_chars, 0, sens_chars.length);
				hd.endElement("", "", "sensitivity");
				hd.startElement("", "", "specificity", atts);
				hd.characters(spec_chars, 0, spec_chars.length);
				hd.endElement("", "", "specificity");
				hd.startElement("", "", "rec1_ID", atts);
				hd.characters(rec1_id, 0, rec1_id.length);
				hd.endElement("", "", "rec1_ID");
				hd.startElement("", "", "rec2_ID", atts);
				hd.characters(rec2_id, 0, rec2_id.length);
				hd.endElement("", "", "rec2_ID");
				
				hd.endElement("", "", "metadata");
				
				// if it's a modified match result, write modifier information
				if (mr instanceof ModifiedMatchResult) {
					ModifiedMatchResult mmr = (ModifiedMatchResult) mr;
					atts.clear();
					hd.startElement("", "", "modifiers", atts);
					
					MatchResult base_mr = mmr.getBasicMatchResult();
					Iterator<String> demo_it = base_mr.getDemographics().iterator();
					ScoreVector sv = base_mr.getScoreVector();
					hd.startElement("", "", "base_values", atts);
					while (demo_it.hasNext()) {
						String current_field = demo_it.next();
						atts.addAttribute("", "", "label", "CDATA", current_field);
						hd.startElement("", "", "base_field", atts);
						atts.clear();
						hd.startElement("", "", "base_value", atts);
						String base_val = Double
						        .toString(mmr.getBasicMatchResult().getScoreVector().getScore(current_field));
						hd.characters(base_val.toCharArray(), 0, base_val.length());
						hd.endElement("", "", "base_value");
						hd.endElement("", "", "base_field");
					}
					hd.endElement("", "", "base_values");
					
					List<Modifier> modifiers = mmr.getModifiers();
					Iterator<Modifier> it2 = modifiers.iterator();
					int order = 1;
					while (it2.hasNext()) {
						Modifier m = it2.next();
						atts.clear();
						atts.addAttribute("", "", "label", "CDATA", m.getModifierName());
						atts.addAttribute("", "", "order", "CDATA", Integer.toString(order));
						hd.startElement("", "", "modifier", atts);
						
						Hashtable<String, Double> mods = mmr.getModifications().get(m);
						Iterator<String> it3 = mods.keySet().iterator();
						while (it3.hasNext()) {
							String demographic = it3.next();
							Double d = mods.get(demographic);
							atts.clear();
							atts.addAttribute("", "", "field", "CDATA", demographic);
							hd.startElement("", "", "field_modification", atts);
							atts.clear();
							hd.startElement("", "", "modification", atts);
							String mod = Double.toString(d);
							hd.characters(mod.toCharArray(), 0, mod.length());
							hd.endElement("", "", "modification");
							hd.startElement("", "", "operator", atts);
							Operator o = mmr.getModifierOperators().get(m);
							if (o.equals(ModifiedMatchResult.Operator.MULTIPLY)) {
								hd.characters("MULTIPLEY".toCharArray(), 0, "MULTIPLY".length());
							} else {
								hd.characters("PLUS".toCharArray(), 0, "PLUS".length());
							}
							hd.endElement("", "", "operator");
							
							hd.endElement("", "", "field_modification");
						}
						
						hd.endElement("", "", "modifier");
						order++;
					}
					
					hd.endElement("", "", "modifiers");
				}
				
				// iterate over fields
				hd.startElement("", "", "fields", atts);
				Iterator<String> it2 = mr.getDemographics().iterator();
				while (it2.hasNext()) {
					String demographic = it2.next();
					// set field attributes
					atts.addAttribute("", "", "label", "CDATA", demographic);
					atts.addAttribute("", "", "type", "CDATA", "match");
					hd.startElement("", "", "field", atts);
					atts.clear();
					
					hd.startElement("", "", "valueA", atts);
					String val_a = mr.getRecord1().getDemographic(demographic);
					hd.characters(val_a.toCharArray(), 0, val_a.length());
					hd.endElement("", "", "valueA");
					hd.startElement("", "", "valueB", atts);
					String val_b = mr.getRecord2().getDemographic(demographic);
					hd.characters(val_b.toCharArray(), 0, val_b.length());
					hd.endElement("", "", "valueB");
					hd.startElement("", "", "matched", atts);
					if (mr.matchedOn(demographic)) {
						hd.characters("true".toCharArray(), 0, "true".length());
					} else {
						hd.characters("false".toCharArray(), 0, "false".length());
					}
					hd.endElement("", "", "matched");
					hd.startElement("", "", "score_value", atts);
					String score = Double.toString(mr.getScoreVector().getScore(demographic));
					hd.characters(score.toCharArray(), 0, score.length());
					hd.endElement("", "", "score_value");
					hd.startElement("", "", "comparator", atts);
					String comp = MatchingConfig.ALGORITHMS[mr.getMatchingConfig()
					        .getAlgorithm(mr.getMatchingConfig().getRowIndexforName(demographic))];
					hd.characters(comp.toCharArray(), 0, comp.length());
					hd.endElement("", "", "comparator");
					hd.startElement("", "", "similarity", atts);
					String sim = Double.toString(mr.getSimilarityScore(demographic));
					hd.characters(sim.toCharArray(), 0, sim.length());
					hd.endElement("", "", "similarity");
					
					hd.endElement("", "", "field");
				}
				// iterate over demographics not used in the matching, but present in the Record objects
				Collection<String> demographics = new ArrayList<String>();
				demographics.addAll(mr.getRecord1().getDemographics().keySet());
				//demographics.addAll(mr.getRecord2().getDemographics().keySet());
				
				Iterator<String> it3 = demographics.iterator();
				List<String> matched_demographics = mr.getMatchVector().getDemographics();
				String[] bd = mr.getMatchingConfig().getBlockingColumns();
				List<String> blocking_demographics = new ArrayList<String>();
				for (int i = 0; i < bd.length; i++) {
					blocking_demographics.add(bd[i]);
				}
				while (it3.hasNext()) {
					String dem = it3.next();
					if (!matched_demographics.contains(dem)) {
						atts.clear();
						atts.addAttribute("", "", "label", "CDATA", dem);
						if (blocking_demographics.contains(dem)) {
							atts.addAttribute("", "", "type", "CDATA", "block");
						} else {
							atts.addAttribute("", "", "type", "CDATA", "display");
						}
						hd.startElement("", "", "field", atts);
						
						atts.clear();
						hd.startElement("", "", "valueA", atts);
						String val_a = mr.getRecord1().getDemographic(dem);
						hd.characters(val_a.toCharArray(), 0, val_a.length());
						hd.endElement("", "", "valueA");
						hd.startElement("", "", "valueB", atts);
						String val_b = mr.getRecord2().getDemographic(dem);
						hd.characters(val_b.toCharArray(), 0, val_b.length());
						hd.endElement("", "", "valueB");
						
						hd.endElement("", "", "field");
					}
				}
				
				//atts.addAttribute("","","ID","CDATA",id[i]);
				//atts.addAttribute("","","TYPE","CDATA",type[i]);
				
				//hd.characters(desc[i].toCharArray(),0,desc[i].length());
				hd.endElement("", "", "fields");
				hd.endElement("", "", "pair");
			}
			hd.endElement("", "", "matches");
			hd.endDocument();
		}
		catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	public static Node toDOMNode(MatchResult mr, Document doc) {
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
		if (mr instanceof ModifiedMatchResult) {
			ModifiedMatchResult mmr = (ModifiedMatchResult) mr;
			ret.appendChild(getModifierNodes(mmr, doc));
		}
		
		// iterate over fields
		Iterator<String> it = mr.getDemographics().iterator();
		Element fields = doc.createElement("fields");
		while (it.hasNext()) {
			String demographic = it.next();
			Element field = doc.createElement("field");
			field.setAttribute("label", demographic);
			field.setAttribute("type", "match");
			fields.appendChild(field);
			
			Element val_a = doc.createElement("valueA");
			Element val_b = doc.createElement("valueB");
			Element matched = doc.createElement("matched");
			Element score_val = doc.createElement("score_value");
			Element comparator = doc.createElement("comparator");
			Element similarity = doc.createElement("similarity");
			
			val_a.setTextContent(mr.getRecord1().getDemographic(demographic));
			val_b.setTextContent(mr.getRecord2().getDemographic(demographic));
			if (mr.matchedOn(demographic)) {
				matched.setTextContent("true");
			} else {
				matched.setTextContent("false");
			}
			score_val.setTextContent(Double.toString(mr.getScoreVector().getScore(demographic)));
			comparator.setTextContent(MatchingConfig.ALGORITHMS[mr.getMatchingConfig()
			        .getAlgorithm(mr.getMatchingConfig().getRowIndexforName(demographic))]);
			similarity.setTextContent(Double.toString(mr.getSimilarityScore(demographic)));
			
			field.appendChild(val_a);
			field.appendChild(val_b);
			field.appendChild(matched);
			field.appendChild(score_val);
			field.appendChild(comparator);
			field.appendChild(similarity);
		}
		
		// iterate over demographics not used in the matching, but present in the Record objects
		Collection<String> demographics = new ArrayList<String>();
		demographics.addAll(mr.getRecord1().getDemographics().keySet());
		//demographics.addAll(mr.getRecord2().getDemographics().keySet());
		
		Iterator<String> it2 = demographics.iterator();
		List<String> matched_demographics = mr.getMatchVector().getDemographics();
		String[] bd = mr.getMatchingConfig().getBlockingColumns();
		List<String> blocking_demographics = new ArrayList<String>();
		for (int i = 0; i < bd.length; i++) {
			blocking_demographics.add(bd[i]);
		}
		while (it2.hasNext()) {
			String dem = it2.next();
			if (!matched_demographics.contains(dem)) {
				// demographic wasn't used when calculating the score, but is present and needs to be added
				Element field = doc.createElement("field");
				field.setAttribute("label", dem);
				fields.appendChild(field);
				if (blocking_demographics.contains(dem)) {
					field.setAttribute("type", "block");
				} else {
					field.setAttribute("type", "display");
				}
				Element val_a = doc.createElement("valueA");
				Element val_b = doc.createElement("valueB");
				val_a.setTextContent(mr.getRecord1().getDemographic(dem));
				val_b.setTextContent(mr.getRecord2().getDemographic(dem));
				field.appendChild(val_a);
				field.appendChild(val_b);
			}
		}
		
		ret.appendChild(fields);
		
		return ret;
	}
	
	private static Node getModifierNodes(ModifiedMatchResult mr, Document doc) {
		Element ret = doc.createElement("modifiers");
		
		// write information about base score, before modifications are done
		Element mod_base = doc.createElement("base_values");
		MatchResult base_mr = mr.getBasicMatchResult();
		Iterator<String> demo_it = base_mr.getDemographics().iterator();
		ScoreVector sv = base_mr.getScoreVector();
		while (demo_it.hasNext()) {
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
		while (it.hasNext()) {
			Modifier m = it.next();
			Element mod_element = doc.createElement("modifier");
			mod_element.setAttribute("label", m.getModifierName());
			mod_element.setAttribute("order", Integer.toString(order));
			ret.appendChild(mod_element);
			
			Hashtable<String, Double> mods = mr.getModifications().get(m);
			Iterator<String> it2 = mods.keySet().iterator();
			while (it2.hasNext()) {
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
				if (o.equals(ModifiedMatchResult.Operator.MULTIPLY)) {
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
