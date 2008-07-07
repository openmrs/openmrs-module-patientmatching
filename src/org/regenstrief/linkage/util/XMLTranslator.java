package org.regenstrief.linkage.util;

/**
 * Class created to go from Record Linkage data objects
 * to XML Document nodes and vice versa.
 * 
 * TODO: Make config more flexible  (ex: allow capital letters, spaces etc.)
 * TODO: Add ScaleWeight parameters
 * - A flag indicating whether to use null tokens when scaling agreement weight based on term frequency (default-no)
 * - A flag indicating how to establish agreement among fields when one or both fields are null (eg, apply disagreement weight, apply agreement weight, or apply zero weight) (default-apply zero weight)
 * TODO: Analyze all columns if no matching runs are present
 */


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.regenstrief.linkage.util.MatchingConfigRow.ScaleWeightSetting;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
			
			FileWriter fw = new FileWriter(f);
			
			StreamResult sr = new StreamResult(fw);
			DOMSource source = new DOMSource(d);
			trans.transform(source, sr);
			
			fw.close();
			
			/* alternate method
			Source source = new DOMSource(d);
		    
            // Prepare the output file
            Result result = new StreamResult(f);
    
            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
			*/
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
			
			// DataSources
			if(rmc.getLinkDataSource1() != null){
				Element lds1_node = toDOMNode(rmc.getLinkDataSource1(), ret);
				root.appendChild(lds1_node);
			}
			if(rmc.getLinkDataSource2() != null){
				Node lds2_node = toDOMNode(rmc.getLinkDataSource2(), ret);
				root.appendChild(lds2_node);
			}
			
			// Analysis part
			AnalysisConfig ac = rmc.getAnalysis_configs();
			if(ac != null){
				HashMap<String, String> config_table = ac.getSettings();
				for(String type : config_table.keySet()) {
					String init = config_table.get(type);
					Element analysis = toDOMNode(type, init, ret);
					root.appendChild(analysis);
				}
			}
			
			// MatchingConfigRows
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
	
	public static Element toDOMNode(String analyzer_type, String analyzer_init, Document doc) {
		Element ret = doc.createElement("analysis");
		ret.setAttribute("type", analyzer_type);
		Element child = doc.createElement("init");
		child.setTextContent(analyzer_init);
		ret.appendChild(child);
		return ret;
	}
	
	public static Element toDOMNode(LinkDataSource lds, Document doc){
		Element ret = doc.createElement("datasource");
		ret.setAttribute("name", lds.getName());
		ret.setAttribute("type", lds.getType());
		ret.setAttribute("access", lds.getAccess());
		ret.setAttribute("id", "" + lds.getDataSource_ID());
		
		if(lds.getUniqueID() != null){
			ret.setAttribute("id_field", lds.getUniqueID());
		}
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
		
		ret.setAttribute("name", mc.getName());
		
		// if the user want to use random sampling, then add attributes in
		// the "run" element of the configuration file to persist the random
		// sampling parameters
		if(mc.isUsingRandomSampling()) {
			ret.setAttribute("random-sample", "true");
			ret.setAttribute("sample-size", String.valueOf(mc.getRandomSampleSize()));
		} else {
			ret.setAttribute("random-sample", "false");
		}
		
		if(mc.getScoreThreshold() != MatchingConfig.DEFAULT_SCORE_THRESHOLD){
			ret.setAttribute("threshold", Double.toString(mc.getScoreThreshold()));
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
		if(mcr.getBlockOrder() == mcr.DEFAULT_BLOCK_ORDER) {
			block_order.setTextContent("null");
		} else {
			block_order.setTextContent(Integer.toString(mcr.getBlockOrder()));
		}

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
		
		Locale new_locale = Locale.US;
		DecimalFormat df = new DecimalFormat("0.#####");
		//df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(new_locale));
		
		Element t_agreement = doc.createElement("TAgreement");
		t_agreement.setTextContent(df.format(mcr.getAgreement()));
		ret.appendChild(t_agreement);
		
		Element n_agreement = doc.createElement("NonAgreement");
		n_agreement.setTextContent(df.format(mcr.getNonAgreement()));
		ret.appendChild(n_agreement);
		
		Element scale_weight = doc.createElement("ScaleWeight");
		if(mcr.isScaleWeight()){
			scale_weight.setTextContent("true");
			scale_weight.setAttribute("lookup", mcr.getSw_settings().toString());
			scale_weight.setAttribute("N",mcr.getSw_number().toString());
			scale_weight.setAttribute("buffer", "" + mcr.getBuffer_size());
		} else {
			scale_weight.setTextContent("null");
		}

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
		AnalysisConfig analysis_config = new AnalysisConfig();
		
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
				boolean is_scale_weight = false;
				Iterator<MatchingConfigRow> mcit = mc.getMatchingConfigRows().iterator();
				while(mcit.hasNext() && !is_scale_weight) {
					MatchingConfigRow current = mcit.next();
					if(current.isScaleWeight()) {
						is_scale_weight = true;
						mc.make_scale_weight();
					}
				}
				mc_configs.add(mc);
			} else if(node_name.equals("analysis")) {
				String type = n.getAttributes().getNamedItem("type").getTextContent();
				NodeList settings = n.getChildNodes();
				String init = "";
				if(settings != null) {
					init = settings.item(1).getTextContent();
				}
				analysis_config.addInitString(type, init);
			}
		}
		
		if(mc_configs.size() == 0) {
			mc_configs = analyzeAllColumns(lds1);
		}

		return new RecMatchConfig(lds1, lds2, mc_configs, analysis_config);
	}
	/**
	 * Method used when there are no matching configurations specified
	 * Default behavior is to analyze all rows
	 * @param lds
	 * @return
	 */
	private static List<MatchingConfig> analyzeAllColumns(LinkDataSource lds) {
		List<MatchingConfig> mcs = new ArrayList<MatchingConfig>();
		
		ArrayList<MatchingConfigRow> rows = new ArrayList<MatchingConfigRow>();
		for(DataColumn dc : lds.getDataColumns()) {
			MatchingConfigRow mcr = new MatchingConfigRow(dc.getName());
			mcr.setInclude(true);
			mcr.setScaleWeight(true);
			rows.add(mcr);
		}
		
		Object[] objectArray = rows.toArray();    
		MatchingConfigRow [] mcr_array = (MatchingConfigRow []) rows.toArray(new MatchingConfigRow[objectArray.length]);
		
		MatchingConfig mc = new MatchingConfig("default",mcr_array);
		mc.make_scale_weight();
		mcs.add(mc);
		return mcs;
	}

	/*
	 * Method takes a XML DOM node and returns a LinkDataSource object
	 */
	public static LinkDataSource createDataSource(Node lds){
		String name = lds.getAttributes().getNamedItem("name").getTextContent();
		String type = lds.getAttributes().getNamedItem("type").getTextContent();
		String access = lds.getAttributes().getNamedItem("access").getTextContent();
		String id_field = null;
		if(lds.getAttributes().getNamedItem("id_field") != null){
			 id_field = lds.getAttributes().getNamedItem("id_field").getTextContent();
		}
		try {
			int ds_id = Integer.parseInt(lds.getAttributes().getNamedItem("id").getTextContent());
			LinkDataSource ret = new LinkDataSource(name, type, access, ds_id);
			ret.setUniqueID(id_field);
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
		Node score_threshold = attributes.getNamedItem("threshold");
		BlockingExclusionList bel = null;
		
		// get the random sampling params
		Node node = attributes.getNamedItem("random-sample");
		// check for compatibility with older xml
		boolean randomSampling = false;
		int sampleSize = 0;
		if(node != null) {
			// check whether to use random sampling or not
			String useRandomSampling = node.getTextContent();
			if(useRandomSampling.equals("true")) {
				randomSampling = true;
				// if random sampling is used, then there will be a sample size attribute
				String randomSampleSize = attributes.getNamedItem("sample-size").getTextContent();
				sampleSize = Integer.parseInt(randomSampleSize);
			}
		}
		
		// iterate over the children nodes and create the MatchingConfigRow objects
		ArrayList<MatchingConfigRow> mcrs = new ArrayList<MatchingConfigRow>();
		for(int i = 0; i < mc.getChildNodes().getLength(); i++){
			Node child = mc.getChildNodes().item(i);
			if(child.getNodeName().equals("row")){
				MatchingConfigRow mcr = createMatchingConfigRow(child);
				mcrs.add(mcr);
			} else if(child.getNodeName().equals("BlockingExclusion")){
				// create blocking exclusion list object
				bel = createBlockingExclusionList(child);
			}
		}
		
		MatchingConfigRow[] mcr_array = new MatchingConfigRow[mcrs.size()];
		for(int i = 0; i < mcrs.size(); i++){
			mcr_array[i] = mcrs.get(i);
		}
		
		MatchingConfig ret = new MatchingConfig(mc_name, mcr_array);
		ret.setBlockingExclusionList(bel);
		ret.setEstimate(estimate);
		ret.setUsingRandomSampling(randomSampling);
		ret.setRandomSampleSize(sampleSize);
		if(score_threshold != null){
			try{
				double threshold = Double.parseDouble(score_threshold.getTextContent());
				ret.setScoreThreshold(threshold);
			}
			catch(NumberFormatException nfe){
				// bad value for score threshold, default value will be used
			}
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
	
	public static BlockingExclusionList createBlockingExclusionList(Node n){
		BlockingExclusionList ret = new BlockingExclusionList();
		
		for(int i = 0; i < n.getChildNodes().getLength(); i++){
			Node child = n.getChildNodes().item(i);
			String child_name = child.getNodeName();
			if(child_name.equals("exclusion")){
				String demographic = child.getAttributes().getNamedItem("demographic").getTextContent();
				String regex = child.getAttributes().getNamedItem("regex").getTextContent();
				ret.addExclusion(demographic, regex);
			}
		}
		
		return ret;
	}
}
