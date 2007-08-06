package org.regenstrief.linkage.util;

/**
 * Class created to go from Record Linkage data objects
 * to XML Document nodes and vice versa.
 * 
 * TODO: Make config more flexible  (ex: allow capital letters, spaces etc.)
 * TODO: Add ScaleWeight parameters
 * - A flag indicating whether to use null tokens when scaling agreement weight based on term frequency (default-no)
 * - A flag indicating how to establish agreement among fields when one or both fields are null (eg, apply disagreement weight, apply agreement weight, or apply zero weight) (default-apply zero weight)
 */


import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

import org.xml.sax.*;
import org.regenstrief.linkage.util.MatchingConfigRow.ScaleWeightSetting;
import org.w3c.dom.*;

public class XMLTranslator {
	public static Document getXMLDocFromFile(File f){
		if(!f.exists()){
			return null;
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try{
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(f);
			return doc;
		}
		catch(ParserConfigurationException pce){
			//System.out.println("error making XML parser: " + pce.getMessage());
			return null;
		}
		catch(SAXException spe){
			//System.out.println("error parsing config file: " + spe.getMessage());
			return null;
		}
		catch(IOException ioe){
			//System.out.println("IO error parsing config file: " + ioe.getMessage());
			return null;
		}
	}
	
	public static boolean writeXMLDocToFile(Document d, File f){
		try{
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult(sw);
			DOMSource source = new DOMSource(d);
			trans.transform(source, sr);
			String xmlString = sw.toString();
			FileWriter fw = new FileWriter(f);
			fw.write(xmlString);
			fw.close();
			return true;
		}
		catch(TransformerConfigurationException tce){
			//System.out.println("transformer config error");
			return false;
		}
		catch(TransformerException te){
			//System.out.println("transformer error");
			return false;
		}
		catch(IOException ioe){
			//System.out.println("IO error");
			return false;
		}
		
		
	}
	
	public static Document toXML(RecMatchConfig rmc){
		Document ret = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try{
			DocumentBuilder builder = factory.newDocumentBuilder();
			ret = builder.newDocument();
			Element root = ret.createElement("Session");
			ret.appendChild(root);
			
			Element lds1_node = toDOMNode(rmc.getLinkDataSource1(), ret);
			root.appendChild(lds1_node);
			Node lds2_node = toDOMNode(rmc.getLinkDataSource2(), ret);
			root.appendChild(lds2_node);
			
			Iterator<MatchingConfig> it = rmc.getMatchingConfigs().iterator();
			while(it.hasNext()){
				MatchingConfig mc = it.next();
				Element mc_node = toDOMNode(mc, ret);
				root.appendChild(mc_node);
			}
		}
		catch(ParserConfigurationException pce){
			//System.out.println("error making XML parser: " + pce.getMessage());
			return null;
		}
		
		return ret;
	}
	
	public static Element toDOMNode(LinkDataSource lds, Document doc){
		Element ret = doc.createElement("datasource");
		ret.setAttribute("name", lds.getName());
		ret.setAttribute("type", lds.getType());
		ret.setAttribute("access", lds.getAccess());
		ret.setAttribute("id", "" + lds.getDataSource_ID());
		// add the nodes for the data column objects
		Iterator<DataColumn> it = lds.getDataColumns().iterator();
		while(it.hasNext()){
			DataColumn dc = it.next();
			Node child = toDOMNode(dc, doc);
			ret.appendChild(child);
		}
		
		return ret;
	}
	
	public static Element toDOMNode(DataColumn dc, Document doc){
		Element ret = doc.createElement("column");
		int include_position = dc.getIncludePosition();
		if(include_position == DataColumn.INCLUDE_NA){
			ret.setAttribute("include_position", "NA");
		} else {
			ret.setAttribute("include_position", Integer.toString(include_position));
		}
		
		String column_id = dc.getColumnID();
		ret.setAttribute("column_id", column_id);
		
		ret.setAttribute("label", dc.getName());
		
		if(dc.getType() == DataColumn.NUMERIC_TYPE){
			ret.setAttribute("type", "number");
		} else {
			ret.setAttribute("type", "string");
		}
				
		return ret;
	}
	
	public static Element toDOMNode(MatchingConfig mc, Document doc){
		Element ret = doc.createElement("run");
		if(mc.isEstimated()){
			ret.setAttribute("estimate", "true");
		} else {
			ret.setAttribute("estimate", "false");
		}
		
		Iterator<MatchingConfigRow> it = mc.getMatchingConfigRows().iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			Element child_node = toDOMNode(mcr, doc);
			ret.appendChild(child_node);
		}
		
		return ret;
	}
	
	public static Element toDOMNode(MatchingConfigRow mcr, Document doc){
		Element ret = doc.createElement("row");
		ret.setAttribute("name", mcr.getName());
		
		Element block_order = doc.createElement("BlockOrder");
		block_order.setTextContent(Integer.toString(mcr.getBlockOrder()));
		ret.appendChild(block_order);
		
		Element blck_chars = doc.createElement("BlckChars");
		blck_chars.setTextContent(Integer.toString(mcr.getBlockChars()));
		ret.appendChild(blck_chars);
		
		Element include = doc.createElement("Include");
		if(mcr.isIncluded()){
			include.setTextContent("true");
		} else {
			include.setTextContent("false");
		}
		ret.appendChild(include);
		
		Element t_agreement = doc.createElement("TAgreement");
		t_agreement.setTextContent(Double.toString(mcr.getAgreement()));
		ret.appendChild(t_agreement);
		
		Element n_agreement = doc.createElement("NonAgreement");
		n_agreement.setTextContent(Double.toString(mcr.getNonAgreement()));
		ret.appendChild(n_agreement);
		
		Element scale_weight = doc.createElement("ScaleWeight");
		if(mcr.isScaleWeight()){
			scale_weight.setTextContent("true");
		} else {
			scale_weight.setTextContent("false");
		}
		scale_weight.setAttribute("lookup", mcr.getSw_settings().toString());
		scale_weight.setAttribute("N",mcr.getSw_number().toString());
		ret.appendChild(scale_weight);
		
		Element algorithm = doc.createElement("Algorithm");
		algorithm.setTextContent(MatchingConfig.ALGORITHMS[mcr.getAlgorithm()]);
		ret.appendChild(algorithm);
		
		return ret;
	}
	
	public static RecMatchConfig createRecMatchConfig(Document doc){
		List<MatchingConfig> mc_configs = new ArrayList<MatchingConfig>();
		LinkDataSource lds1, lds2;
		lds1 = null;
		lds2 = null;
		
		Element root_node = doc.getDocumentElement();
		
		// for each matching config
		for(int i = 0; i < root_node.getChildNodes().getLength(); i++) {
			Node n = root_node.getChildNodes().item(i);
			String node_name = n.getNodeName();
			
			if(node_name.equals("datasource")){
				LinkDataSource lds = createDataSource(n);
				if(lds1 == null){
					lds1 = lds;
				} else {
					lds2 = lds;
				}
			} else if(node_name.equals("run")){
				MatchingConfig mc = createMatchingConfig(n);
				mc_configs.add(mc);
			} else if(node_name.equals("analysis")) {
				
				// TODO: STUB
			}
		}
		
		return new RecMatchConfig(lds1, lds2, mc_configs);

	}
	
	/*
	 * Method takes a XML DOM node and returns a LinkDataSource object
	 */
	public static LinkDataSource createDataSource(Node lds){
		String name = lds.getAttributes().getNamedItem("name").getTextContent();
		String type = lds.getAttributes().getNamedItem("type").getTextContent();
		String access = lds.getAttributes().getNamedItem("access").getTextContent();
		try {
			int ds_id = Integer.parseInt(lds.getAttributes().getNamedItem("id").getTextContent());
			LinkDataSource ret = new LinkDataSource(name, type, access, ds_id);
			for(int i = 0; i < lds.getChildNodes().getLength(); i++){
				Node child = lds.getChildNodes().item(i);
				if(child.getNodeName().equals("column")){
					DataColumn dc = createDataColumn(child);
					ret.addDataColumn(dc);
				}
			}
			return ret;
		} catch(IllegalFormatException e) {
			System.out.println("Datasource id must be an integer");
			return null;
		}
	}
	
	public static DataColumn createDataColumn(Node dc){
		// get the attributes of the node, create the data column object
		NamedNodeMap ds_properties = dc.getAttributes();
		String column_id = ds_properties.getNamedItem("column_id").getTextContent();
		String include_pos = ds_properties.getNamedItem("include_position").getTextContent();
		int include_position;
		if(include_pos.equals("NA")){
			include_position = DataColumn.INCLUDE_NA;
		} else {
			include_position = Integer.parseInt(include_pos);
		}
		String name = ds_properties.getNamedItem("label").getTextContent();
		DataColumn ret = new DataColumn(column_id);
		ret.setIncludePosition(include_position);
		ret.setName(name);
		
		String type = ds_properties.getNamedItem("type").getTextContent();
		if(type.equals("number")){
			ret.setType(DataColumn.NUMERIC_TYPE);
		} else {
			ret.setType(DataColumn.STRING_TYPE);
		}
		
		return ret;
	}
	
	/*
	 * Method returns a MatchingConfig object from a 
	 * <run> XML DOM sub-tree
	 */
	public static MatchingConfig createMatchingConfig(Node mc){
		NamedNodeMap attributes = mc.getAttributes();
		String est = attributes.getNamedItem("estimate").getTextContent();
		boolean estimate = false;
		String mc_name = attributes.getNamedItem("name").getTextContent();
		if(est.equals("true")){
			estimate = true;
		}
		// For weight scaling
		Node access_node = attributes.getNamedItem("swaccess");

		boolean is_scaleweight = false;
		String access_value = null;
		// Check if weight scaling nodes exist
		if(access_node != null) {
			access_value = access_node.getTextContent();
			// They may be present, but should not be empty
			if(!access_value.equals("")) {
				is_scaleweight = true;
			}
		}
		
		// For future work:
		//	String use_null_tokens = n.getAttributes().getNamedItem("use_null_tokens").getTextContent();
		//	String null_agreement = n.getAttributes().getNamedItem("null_agreement").getTextContent();
		
		// iterate over the children nodes and create the MatchingConfigRow objects
		ArrayList<MatchingConfigRow> mcrs = new ArrayList<MatchingConfigRow>();
		for(int i = 0; i < mc.getChildNodes().getLength(); i++){
			Node child = mc.getChildNodes().item(i);
			if(child.getNodeName().equals("row")){
				MatchingConfigRow mcr = createMatchingConfigRow(child);
				mcrs.add(mcr);
			}
		}
		
		MatchingConfigRow[] mcr_array = new MatchingConfigRow[mcrs.size()];
		for(int i = 0; i < mcrs.size(); i++){
			mcr_array[i] = mcrs.get(i);
		}
		
		MatchingConfig ret = new MatchingConfig(mc_name, mcr_array);
		ret.setEstimate(estimate);
		
		// Reflect the settings to the MatchingConfig
		if(is_scaleweight) {
		ret.make_scale_weight();
		ret.setSw_db_access(access_value);
		}
		
		return ret;
	}
	
	/*
	 * Method takes a <row> XML DOM sub-tree
	 */
	public static MatchingConfigRow createMatchingConfigRow(Node n){
		String row_name = n.getAttributes().getNamedItem("name").getTextContent();
		MatchingConfigRow ret = new MatchingConfigRow(row_name);
		
		// iterate over the children nodes to get the block order, block chars, etc.
		for(int i = 0; i < n.getChildNodes().getLength(); i++){
			Node child = n.getChildNodes().item(i);
			String child_name = child.getNodeName();
			if(child_name.equals("BlockOrder")){
				//String bo = child.getChildNodes().item(0).getTextContent();
				String bo = child.getTextContent();
				if(!bo.equals("null")){
					ret.setBlockOrder(Integer.parseInt(bo));
				}
			} else if(child_name.equals("BlckChars")){
				String bc = child.getTextContent();
				ret.setBlockChars(Integer.parseInt(bc));
			} else if(child_name.equals("Include")){
				String inc = child.getTextContent();
				if(inc.equals("true")){
					ret.setInclude(true);
				} else if(inc.equals("false")){
					ret.setInclude(false);
				}
			} else if(child_name.equals("TAgreement")){
				String ta = child.getTextContent();
				ret.setAgreement(Double.parseDouble(ta));
			} else if(child_name.equals("NonAgreement")){
				String na = child.getTextContent();
				ret.setNonAgreement(Double.parseDouble(na));
			} else if(child_name.equals("ScaleWeight")){
				String sw = child.getTextContent();
				if(sw.equals("true")){
					ret.setScaleWeight(true);
					NamedNodeMap settings = child.getAttributes();
					if(settings != null) {
						String lookup = settings.getNamedItem("lookup").getTextContent();
						ScaleWeightSetting lookup_setting = null;
						if(lookup.equals(ScaleWeightSetting.AboveN.toString())) {
							lookup_setting = ScaleWeightSetting.AboveN;
						} else if(lookup.equals(ScaleWeightSetting.BelowN.toString())) {
							lookup_setting = ScaleWeightSetting.BelowN;
						} else if(lookup.equals(ScaleWeightSetting.BottomN.toString())) {
							lookup_setting = ScaleWeightSetting.BottomN;
						} else if(lookup.equals(ScaleWeightSetting.BottomNPercent.toString())) {
							lookup_setting = ScaleWeightSetting.BottomNPercent;
						} else 	if(lookup.equals(ScaleWeightSetting.TopN.toString())) {
							lookup_setting = ScaleWeightSetting.TopN;
						} else 	if(lookup.equals(ScaleWeightSetting.TopNPercent.toString())) {
							lookup_setting = ScaleWeightSetting.TopNPercent;
						}
						
						Float N = Float.parseFloat(settings.getNamedItem("N").getTextContent());
						ret.setSw_number(N);
						ret.setSw_settings(lookup_setting);
					}
				}
			} else if(child_name.equals("Algorithm")){
				String alg = child.getTextContent();
				if(alg.equals(MatchingConfig.ALGORITHMS[MatchingConfig.LEV])){
					ret.setAlgorithm(MatchingConfig.LEV);
				} else if(alg.equals(MatchingConfig.ALGORITHMS[MatchingConfig.EXACT_MATCH])){
					ret.setAlgorithm(MatchingConfig.EXACT_MATCH);
				} else if(alg.equals(MatchingConfig.ALGORITHMS[MatchingConfig.JWC])){
					ret.setAlgorithm(MatchingConfig.JWC);
				} else if(alg.equals(MatchingConfig.ALGORITHMS[MatchingConfig.LCS])){
					ret.setAlgorithm(MatchingConfig.LCS);
				}
			}
		}
		
		return ret;
	}
}
