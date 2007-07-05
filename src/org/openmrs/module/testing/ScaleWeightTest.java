/**
 * Used to test weight scaling functionality
 * 
 * @author sarpc
 */

package org.openmrs.module.testing;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.regenstrief.linkage.analysis.CharDelimFileAnalyzer;
import org.regenstrief.linkage.analysis.DataBaseAnalyzer;
import org.regenstrief.linkage.analysis.DataSourceAnalyzer;
import org.regenstrief.linkage.io.CharDelimFileReader;
import org.regenstrief.linkage.io.OrderedCharDelimFileReader;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author sarpc
 *
 */
public class ScaleWeightTest {
	
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
			
			String[] scale_weights = mc_test.getScaleWeightColumnNames();
			//System.out.println(scale_weights.toString());
			
			LinkDataSource lds1 =  rmc.getLinkDataSource1();
			LinkDataSource lds2 = rmc.getLinkDataSource2();
			
			String ds1_type = lds1.getType();
			String ds2_type = lds2.getType();
			CharDelimFileReader cdfr = new OrderedCharDelimFileReader(lds2,mc_test);
			DataSourceAnalyzer analyzer1, analyzer2;
			if(ds1_type.equals("DataBase")) {
				analyzer1 = new DataBaseAnalyzer(rmc.getLinkDataSource1(),mc_test, rmc.getSw_connection());

			} else {
				analyzer1 = new CharDelimFileAnalyzer(rmc.getLinkDataSource1(),mc_test, rmc.getSw_connection());
			}
			
			if(ds2_type.equals("DataBase")) {
				analyzer2 = new DataBaseAnalyzer(rmc.getLinkDataSource2(),mc_test, rmc.getSw_connection());

			} else {
				analyzer2 = new CharDelimFileAnalyzer(rmc.getLinkDataSource2(),mc_test, rmc.getSw_connection());
			}
			
			DataColumn temp = rmc.getLinkDataSource2().getDataColumn(11);
			
			//LinkDataSource lds = new LinkDataSource("patientmatching_token","DataBase","com.mysql.jdbc.Driver,jdbc:mysql://localhost/patientmatching_datasource_analysis,root,, ");
			
			DataColumn dcc = rmc.getLinkDataSource1().getDataColumn(11);
		//	Hashtable<String,Integer> deneme1 = analyzer1.getTokenFrequencies(dcc);
		//	Hashtable<String,Integer> deneme2 = analyzer2.getTokenFrequencies(temp);
			CharDelimFileAnalyzer cda = (CharDelimFileAnalyzer) analyzer2;
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
