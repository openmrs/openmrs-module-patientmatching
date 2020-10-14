package org.regenstrief.linkage.testing;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.io.DedupOrderedDataSourceFormPairs;
import org.regenstrief.linkage.io.OrderedDataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DedupFormPairsTest {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File config = new File("1.meta.xml");
		if (!config.exists()) {
			System.out.println("config file does not exist, exiting");
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			// Load the XML configuration file
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			RecMatchConfig rmc = XMLTranslator.createRecMatchConfig(doc);
			
			// Retrieve data sources for easier access
			LinkDataSource lds1 = rmc.getLinkDataSource1();
			ReaderProvider rp = ReaderProvider.getInstance();
			
			OrderedDataSourceReader odsr = rp.getReader(lds1, rmc.getMatchingConfigs().get(0));
			
			DedupOrderedDataSourceFormPairs test = new DedupOrderedDataSourceFormPairs(odsr, rmc.getMatchingConfigs().get(0),
			        lds1.getTypeTable());
			
			Record[] pair;
			int total = 0;
			while ((pair = test.getNextRecordPair()) != null) {
				total++;
				Record left = pair[0];
				Record right = pair[1];
				System.out.println(left.getDemographic("sex") + "," + right.getDemographic("sex") + ","
				        + left.getDemographic("zip") + "," + right.getDemographic("zip") + "," + left.getDemographic("ln")
				        + "," + right.getDemographic("ln"));
				
			}
			System.out.println("read " + total + " dedupe pairs");
		}
		catch (ParserConfigurationException pce) {
			System.out.println("error making XML parser: " + pce.getMessage());
		}
		catch (SAXException spe) {
			System.out.println("error parsing config file: " + spe.getMessage());
		}
		catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		
	}
	
}
