package org.regenstrief.linkage.matchresult;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.ModifiedMatchResult;
import org.regenstrief.linkage.ScoreVector;
import org.regenstrief.linkage.ModifiedMatchResult.Operator;
import org.regenstrief.linkage.analysis.Modifier;
import org.regenstrief.linkage.util.MatchingConfig;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Code uses SAX to emit XML representation of MatchResults to a Writer
 * @author jegg
 *
 */

public class XMLMatchResultWriter extends MatchResultWriter {
	
	protected TransformerHandler hd;
	protected AttributesImpl atts;
	
	public XMLMatchResultWriter(Writer w){
		super(w);
		
		// setup SAX
		StreamResult streamResult = new StreamResult(output);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		// SAX2.0 ContentHandler
		try{
			hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			//serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
			serializer.setOutputProperty(OutputKeys.INDENT,"yes");
			hd.setResult(streamResult);
			
			hd.startDocument();
			atts = new AttributesImpl();
			hd.startElement("","","matches",atts);
			
		}
		catch(TransformerConfigurationException tce){
			output = null;
		}
		catch(SAXException se){
			output = null;
		}
		
	}
	
	public void acceptMatchResult(MatchResult mr) {
		// call SAX events for MatchResult object
		if(output != null){
			try{
				atts.clear();
				hd.startElement("","","pair",atts);
				
				// add metadata
				hd.startElement("", "", "metadata", atts);
				char[] score_chars = Double.toString(mr.getScore()).toCharArray();
				char[] sens_chars = Double.toString(mr.getSensitivity()).toCharArray();
				char[] spec_chars = Double.toString(mr.getSpecificity()).toCharArray();
				
				hd.startElement("", "", "score", atts);
				hd.characters(score_chars, 0, score_chars.length);
				hd.endElement("","", "score");
				hd.startElement("", "", "sensitivity", atts);
				hd.characters(sens_chars, 0, sens_chars.length);
				hd.endElement("","", "sensitivity");
				hd.startElement("", "", "specificity", atts);
				hd.characters(spec_chars, 0, spec_chars.length);
				hd.endElement("","", "specificity");
				
				hd.endElement("","", "metadata");
				
				// if it's a modified match result, write modifier information
				if(mr instanceof ModifiedMatchResult){
					ModifiedMatchResult mmr = (ModifiedMatchResult)mr;
					atts.clear();
					hd.startElement("", "", "modifiers", atts);
					
					MatchResult base_mr = mmr.getBasicMatchResult();
					Iterator<String> demo_it = base_mr.getDemographics().iterator();
					ScoreVector sv = base_mr.getScoreVector();
					hd.startElement("", "", "base_values", atts);
					while(demo_it.hasNext()){
						String current_field = demo_it.next();
						atts.addAttribute("", "", "label", "CDATA", current_field);
						hd.startElement("", "", "base_field", atts);
						atts.clear();
						hd.startElement("", "", "base_value", atts);
						String base_val = Double.toString(mmr.getBasicMatchResult().getScoreVector().getScore(current_field));
						hd.characters(base_val.toCharArray(), 0, base_val.length());
						hd.endElement("", "", "base_value");
						hd.endElement("", "", "base_field");
					}
					hd.endElement("", "", "base_values");
					
					List<Modifier> modifiers = mmr.getModifiers();
					Iterator<Modifier> it2 = modifiers.iterator();
					int order = 1;
					while(it2.hasNext()){
						Modifier m = it2.next();
						atts.clear();
						atts.addAttribute("", "", "label", "CDATA", m.getModifierName());
						atts.addAttribute("", "", "order", "CDATA", Integer.toString(order));
						hd.startElement("", "", "modifier", atts);
						
						Hashtable<String,Double> mods = mmr.getModifications().get(m);
						Iterator<String> it3 = mods.keySet().iterator();
						while(it3.hasNext()){
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
							if(o.equals(ModifiedMatchResult.Operator.MULTIPLY)){
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
				while(it2.hasNext()){
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
					if(mr.matchedOn(demographic)){
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
					String comp = MatchingConfig.ALGORITHMS[mr.getMatchingConfig().getAlgorithm(mr.getMatchingConfig().getRowIndexforName(demographic))];
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
				for(int i = 0; i < bd.length; i++){
					blocking_demographics.add(bd[i]);
				}
				while(it3.hasNext()){
					String dem = it3.next();
					if(!matched_demographics.contains(dem)){
						atts.clear();
						atts.addAttribute("", "", "label", "CDATA", dem);
						if(blocking_demographics.contains(dem)){
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
				hd.endElement("","","pair");
			}
			catch(SAXException se){
				
			}
			
		}
	}

	public void close() {
		open = false;
		if(output != null){
			try{
				hd.endElement("","","matches");
				hd.endDocument();
			}
			catch(SAXException se){
				
			}
		}
		// I think hd.endDocument calls what super.close() does, namelyl
		// flushes and closes the output stream
		// super.close();
	}

}
