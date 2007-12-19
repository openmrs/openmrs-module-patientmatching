package org.openmrs.module.patientmatching;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.regenstrief.linkage.MatchFinder;
import org.regenstrief.linkage.analysis.RecordFieldAnalyzer;
import org.regenstrief.linkage.db.RecordDBManager;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class provides a singleton for database access to where the
 * link table.
 * 
 * @author jegg
 *
 */

public class LinkDBConnections {
	private MatchFinder finder;
	private RecordDBManager link_db;
	private ReaderProvider rp;
	
	private final static LinkDBConnections INSTANCE = new LinkDBConnections();
	
	private LinkDBConnections(){
		if(!parseConfig(new File(PatientMatchingActivator.CONFIG_FILE))){
			finder = null;
			link_db = null;
			rp = null;
		}
	};
	
	/**
	 * Method returns an object holding a MatchFinder object and
	 * a RecordDBManager object.  The first is used to find matches, 
	 * the second is used to add, update, or delete the records
	 * used for matching.
	 * 
	 * @return	an instance of the class with database connections
	 */
	public static LinkDBConnections getInstance(){
		return INSTANCE;
	}
	
	/**
	 * 
	 * @return	the object used to find patients in the database
	 */
	public MatchFinder getFinder(){
		return finder;
	}
	
	/**
	 * 
	 * @return	the object used to add, update, or delete records used in matching
	 */
	public RecordDBManager getRecDBManager(){
		return link_db;
	}
	
	/**
	 * 
	 * @return	the object used to get DataSourceReader objects for a LinkDataSource
	 */
	public ReaderProvider getReaderProvider(){
		return rp;
	}
	
	/**
	 * Method parses a configuration file.  Program assumes that the information under
	 * the first datasource tag in the file is the connection information for the 
	 * record linkage table.  The columns to be used for matching and the string comparators
	 * are also listed in this file.
	 * 
	 * @param config	the configuration file with connection and linkage information
	 * @return	true if there were no errors while parsing the file, false if there was an 
	 * exception
	 */
	private boolean parseConfig(File config){
		try{
			//log.debug("parsing config file " + config);
			//file_log.debug("parsing config file " + config);
			if(!config.exists()){
				//log.warn("cannot find config file in " + config.getPath());
				//file_log.warn("cannot find config file in " + config.getPath());
				return false;
			}
			// Load the XML configuration file
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			RecMatchConfig rmc = XMLTranslator.createRecMatchConfig(doc);
			rp = new ReaderProvider();
			finder = new MatchFinder(rmc.getLinkDataSource1(), rp, rmc.getMatchingConfigs(), new RecordFieldAnalyzer(),MatchFinder.Scoring.BLOCKING_EXCLUSIVE);
			link_db = new RecordDBManager(rmc.getLinkDataSource1());
		}
		catch(ParserConfigurationException pce){
			//log.warn("XML parser error with config file: " + pce.getMessage());
			//file_log.warn("XML parser error with config file: " + pce.getMessage());
			return false;
		}
		catch(SAXException spe){
			//log.warn("XML parser error with config file: " + spe.getMessage());
			//file_log.warn("XML parser error with config file: " + spe.getMessage());
			return false;
		}
		catch(IOException ioe){
			//log.warn("IOException with config file: " + ioe.getMessage());
			//file_log.warn("IOException with config file: " + ioe.getMessage());
			return false;
		}
		//log.debug("file parsed");
		return link_db.connect();
		
	}
}
