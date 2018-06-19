package org.regenstrief.linkage.util;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
/**
 * Designed to transform old XML config files to the new format
 *
 * 
 * @author scentel
 *
 */
public class XMLUpdater {

	/**
	 * Converts all XML files in subdirectories to new format
	 * 
	 * @param args Parent directory
	 */
	public static void main(String [] args) {
		
		File arg = new File(args[0]);
		if(!arg.exists()){
			System.out.println("directory does not exist, exiting");
		} else {
			File [] dirs = arg.listFiles();
			// for all subdirectories
			for(File directory : dirs) {
				if(directory.isDirectory()) {
					File [] dir_files = directory.listFiles();
					// for all XML files in it
					for(File myfile : dir_files) {
						if(myfile.isFile() && myfile.getName().endsWith(".xml")) {
							convertToNewFormat(myfile);
						}
					}
				} else {
					if(directory.isFile() && directory.getName().endsWith(".xml")) {
						convertToNewFormat(directory);
					}
				}
			}
		}
	}
	
	public static void convertToNewFormat(File old_file) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(old_file);
			RecMatchConfig rmc = createRecMatchConfig(doc);
			String fname = old_file.getAbsolutePath();
			fname = fname.substring(0, fname.length()-3);
			File newconfig = new File(fname + "new.xml");
			Document d = toXML(rmc, true);
			writeXMLDocToFile(d, newconfig);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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

	public static Document toXML(RecMatchConfig rmc, boolean scale_weight){
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
			
			if(scale_weight) {
				Element analysis = ret.createElement("analysis");
				analysis.setAttribute("type", "scaleweight");
				
				Element init = ret.createElement("init");
				init.setTextContent(" ");
				
				analysis.appendChild(init);
				root.appendChild(analysis);
			}

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
		ret.setAttribute("id", lds.getDataSource_ID() + "");

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
		if(mcr.DEFAULT_BLOCK_ORDER == mcr.getBlockOrder()) {
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

		Element t_agreement = doc.createElement("TAgreement");

		//Locale locale = Locale.US;
		
		//DecimalFormat df = new DecimalFormat("0.#####");
		//df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(locale));
			
		t_agreement.setTextContent(Double.toString(mcr.getAgreement()));
		ret.appendChild(t_agreement);

		Element n_agreement = doc.createElement("NonAgreement");
		n_agreement.setTextContent(Double.toString(mcr.getNonAgreement()));
		ret.appendChild(n_agreement);
		
		Element scale_weight = doc.createElement("ScaleWeight"); 
		if(mcr.isScaleWeight()){ 
		   scale_weight.setTextContent("true"); 
		} else { 
		  scale_weight.setTextContent("null"); 
		} 
		ret.appendChild(scale_weight); 


		Element algorithm = doc.createElement("Algorithm");
		algorithm.setTextContent(MatchingConfig.ALGORITHMS[mcr.getAlgorithm()]);
		ret.appendChild(algorithm);
		
		Element Interchangable_ID = doc.createElement("SetID");
		if((mcr.getSetID()).equals("0")){
			//if(mcr.isSet()){
				Interchangable_ID.setTextContent("1");
			} else {
				Interchangable_ID.setTextContent(MatchingConfigRow.NO_SET_ID);
			}
			  ret.appendChild(Interchangable_ID);
				
				return ret;
			}
	public static RecMatchConfig createRecMatchConfig(Document doc){
		List<MatchingConfig> mc_configs = new ArrayList<MatchingConfig>();
		LinkDataSource lds1, lds2;
		lds1 = null;
		lds2 = null;

		Element root_node = doc.getDocumentElement();
		for(int i = 0; i < root_node.getChildNodes().getLength(); i++){
			Node n = root_node.getChildNodes().item(i);
			if(n.getNodeName().equals("datasource")){
				LinkDataSource lds = createDataSource(n);
				if(lds1 == null){
					lds1 = lds;
				} else {
					lds2 = lds;
				}
			} else if(n.getNodeName().equals("run")){
				MatchingConfig mc = createMatchingConfig(n);
				mc_configs.add(mc);
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
		Random r = new Random();
	
		LinkDataSource ret = new LinkDataSource(name, type, access,	r.nextInt(50) + 1);
		for(int i = 0; i < lds.getChildNodes().getLength(); i++){
			Node child = lds.getChildNodes().item(i);
			if(child.getNodeName().equals("column")){
				DataColumn dc = createDataColumn(child);
				ret.addDataColumn(dc);
			}
		}
		return ret;
	}

	public static DataColumn createDataColumn(Node dc){
		// get the attributes of the node, create the data column object
		String column_id = dc.getAttributes().getNamedItem("column_id").getTextContent();
		String include_pos = dc.getAttributes().getNamedItem("include_position").getTextContent();
		int include_position;
		if(include_pos.equals("NA")){
			include_position = DataColumn.INCLUDE_NA;
		} else {
			include_position = Integer.parseInt(include_pos);
		}
		String name = dc.getAttributes().getNamedItem("label").getTextContent();
		DataColumn ret = new DataColumn(column_id);
		ret.setIncludePosition(include_position);
		ret.setName(name);

		String type = dc.getAttributes().getNamedItem("type").getTextContent();
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
		String est = mc.getAttributes().getNamedItem("estimate").getTextContent();
		boolean estimate = false;
		String mc_name = mc.getAttributes().getNamedItem("name").getTextContent();
		if(est.equals("true")){
			estimate = true;
		}
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
			} else if(child_name.equals("BlockChars")){
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
				if(sw.equals("True")){
					ret.setScaleWeight(true);
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
			} else if (child_name.equals("Threshold")) {
				ret.setThreshold(Double.parseDouble(child.getTextContent()));
			} else if (child_name.equals("SetID")) {	
				String set_id=child.getTextContent();
				if(set_id.equals("1"))
					ret.setSetID("1");
				else
					ret.setSetID(MatchingConfigRow.NO_SET_ID);
				
			}
		}

		return ret;
	}
}
