package org.openmrs.module.patientmatching;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.regenstrief.linkage.MatchFinder;
import org.regenstrief.linkage.analysis.RecordFieldAnalyzer;
import org.regenstrief.linkage.db.RecordDBManager;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class LinkDBConnections {
	private MatchFinder finder;
	private RecordDBManager link_db;
	
	private final static LinkDBConnections INSTANCE = new LinkDBConnections();
	
	private LinkDBConnections(){
		if(!parseConfig(new File(PatientMatchingActivator.CONFIG_FILE))){
			finder = null;
			link_db = null;
		}
	};
	
	public static LinkDBConnections getInstance(){
		return INSTANCE;
	}
	
	public MatchFinder getFinder(){
		return finder;
	}
	
	public RecordDBManager getRecDBManager(){
		return link_db;
	}
	
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
			finder = new MatchFinder(rmc.getLinkDataSource1(), rmc.getMatchingConfigs(), new RecordFieldAnalyzer(),MatchFinder.Scoring.BLOCKING_INCLUSIVE);
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
