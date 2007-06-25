/**
 * Just to see if I can commit code
 * I will be using this class to test by modifications
 * (Temporarily)
 */

package org.openmrs.testing;
import java.io.*;

import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.regenstrief.linkage.analysis.*;
import org.regenstrief.linkage.db.LinkDBManager;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.util.*;
import org.regenstrief.linkage.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import sun.awt.image.ImageWatched.Link;

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
			
			String[] scale_weights = mc_test.getScaleWeightColumns();
			//System.out.println(scale_weights.toString());
			
			
			String ds1_type = rmc.getLinkDataSource1().getType();
			String ds2_type = rmc.getLinkDataSource2().getType();
			
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
			
			DataColumn temp = rmc.getLinkDataSource2().getDataColumn(3);
			
			//LinkDataSource lds = new LinkDataSource("patientmatching_token","DataBase","com.mysql.jdbc.Driver,jdbc:mysql://localhost/patientmatching_datasource_analysis,root,, ");
			
			DataColumn dcc = rmc.getLinkDataSource1().getDataColumn(11);
			Hashtable<String,Integer> deneme1 = analyzer1.getTokenFrequencies(dcc);
			Hashtable<String,Integer> deneme2 = analyzer2.getTokenFrequencies(temp);
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
