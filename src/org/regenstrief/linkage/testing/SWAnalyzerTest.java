/**
 * Tests weight scaling functionality
 * Old scheme (B)
 * 
 * @author scentel
 */

package org.regenstrief.linkage.testing;
import java.io.*;

import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.regenstrief.linkage.analysis.*;
import org.regenstrief.linkage.io.CharDelimFileReader;
import org.regenstrief.linkage.io.OrderedCharDelimFileReader;
import org.regenstrief.linkage.util.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SWAnalyzerTest {
	
	public static void main(String[] args) {
		File config = new File(args[0]);
		if(!config.exists()){
			System.out.println("config file does not exist, exiting");
			System.exit(0);
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try{
			// Load the XML configuration file
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			RecMatchConfig rmc = XMLTranslator.createRecMatchConfig(doc);
			MatchingConfig mc_test = rmc.getMatchingConfigs().get(0);
			
			// create a mapping of demographic to type; should move this into another class
			// and not do it explicitly
			Hashtable<String, Integer> type_table = new Hashtable<String, Integer>();
			List<DataColumn> dc = rmc.getLinkDataSource1().getDataColumns();
			Iterator<DataColumn> it = dc.iterator();
			while(it.hasNext()){
				DataColumn d = it.next();
				if(d.getIncludePosition() != DataColumn.INCLUDE_NA){
					type_table.put(d.getName(), new Integer(d.getType()));
				}
			}
			
			//String[] scale_weights = mc_test.getScaleWeightColumnNames();
			//System.out.println(scale_weights.toString());
			
			LinkDataSource lds1 =  rmc.getLinkDataSource1();
			LinkDataSource lds2 = rmc.getLinkDataSource2();
			
			String ds1_type = lds1.getType();
			String ds2_type = lds2.getType();
			CharDelimFileReader cdfr = new OrderedCharDelimFileReader(lds2,mc_test);
			String sw_access_parameter = mc_test.getSw_db_access();
			String sw_token_table = mc_test.getSw_token_table();
			SWAnalyzer analyzer1, analyzer2;
			if(ds1_type.equals("DataBase")) {
				analyzer1 = new DataBaseSWAnalyzer(rmc.getLinkDataSource1(),sw_access_parameter, sw_token_table);

			} else {
				analyzer1 = new CharDelimFileSWAnalyzer(rmc.getLinkDataSource1(),mc_test, sw_access_parameter, sw_token_table);
			}
			
			if(ds2_type.equals("DataBase")) {
				analyzer2 = new DataBaseSWAnalyzer(rmc.getLinkDataSource2(),sw_access_parameter, sw_token_table);

			} else {
				analyzer2 = new CharDelimFileSWAnalyzer(rmc.getLinkDataSource2(),mc_test, sw_access_parameter, sw_token_table);
			}
			
			DataColumn temp = rmc.getLinkDataSource2().getDataColumn(11);
			
			//LinkDataSource lds = new LinkDataSource("patientmatching_token","DataBase","com.mysql.jdbc.Driver,jdbc:mysql://localhost/patientmatching_datasource_analysis,root,, ");
			
			DataColumn dcc = rmc.getLinkDataSource1().getDataColumn(11);
		//	Hashtable<String,Integer> deneme1 = analyzer1.getTokenFrequencies(dcc);
		//	Hashtable<String,Integer> deneme2 = analyzer2.getTokenFrequencies(temp);
			CharDelimFileSWAnalyzer cda = (CharDelimFileSWAnalyzer) analyzer2;
			//analyzer2.analyzeTokenFrequencies(temp, 500);
		//	analyzer2.analyzeTokenFrequencies(temp);
			//boolean res = analyzer2.deleteAnalysis(temp);
			//System.out.println(analyzer1.getDistinctRecordCount(dcc) + "");
			//analyzer1.analyzeTokenFrequencies(dcc, 500);
			//analyzer1.getTokenFrequencies(dcc, ScaleWeightSetting.BottomNPercent, new Float(0.1) );
			//lds2.getScaleWeightColumn(scale_weights, mc_test.getMatchingConfigRowCount());

		}
		
		catch(ParserConfigurationException pce){
			System.out.println("error making XML parser: " + pce.getMessage());
		}
		catch(SAXException spe){
			System.out.println("error parsing config file: " + spe.getMessage());
		}
		catch(IOException ioe){
			System.out.println(ioe.getMessage());
		}
		
	}
	
}
