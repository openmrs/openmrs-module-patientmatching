package org.regenstrief.linkage.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.regenstrief.linkage.analysis.DataSourceFrequency;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class created to store the options for the Record Linkage GUI and 
 * record linkage classes.  It stores two data source objects and a list of 
 * MatchingConfig objects.
 */

public class RecMatchConfig implements Iterable<MatchingConfig> {
	private LinkDataSource lds1, lds2;	
	private AnalysisConfig analysis_configs;
	private List<MatchingConfig> mc_configs;
	private DataSourceFrequency dsf1, dsf2;
	
	private boolean deduplication;
	
	public RecMatchConfig(){
		lds1 = null;
		lds2 = null;
		mc_configs = new ArrayList<MatchingConfig>();
	}
	
	public RecMatchConfig(LinkDataSource lds1, LinkDataSource lds2, List<MatchingConfig> mc_configs){
		this.lds1 = lds1;
		this.lds2 = lds2;
		this.mc_configs = mc_configs;
	}
	
	public RecMatchConfig(LinkDataSource lds1, LinkDataSource lds2, List<MatchingConfig> mc_configs, AnalysisConfig analysis_settings){
		this.lds1 = lds1;
		this.lds2 = lds2;
		this.mc_configs = mc_configs;
		this.analysis_configs = analysis_settings;
	}
	
	public Iterator<MatchingConfig> iterator() {
		return mc_configs.iterator();
	}
	
	
	
	public DataSourceFrequency getDataSourceFrequency1() {
		return dsf1;
	}

	public void setDataSourceFrequency1(DataSourceFrequency dsf1) {
		this.dsf1 = dsf1;
	}

	public DataSourceFrequency getDataSourceFrequencyf2() {
		return dsf2;
	}

	public void setDataSourceFrequency2(DataSourceFrequency dsf2) {
		this.dsf2 = dsf2;
	}

	/**
	 * 
	 * @param lds	the new first LinkDataSource
	 */
	public void setLinkDataSource1(LinkDataSource lds){
		lds1 = lds;
	}
	
	/**
	 * 
	 * @return	the first LinkDataSource
	 */
	public LinkDataSource getLinkDataSource1(){
		return lds1;
	}
	
	/**
	 * 
	 * @param lds	the new second LinkDataSource
	 */
	public void setLinkDataSource2(LinkDataSource lds){
		lds2 = lds;
	}
	
	/**
	 * 
	 * @return	the second LinkDataSource
	 */
	public LinkDataSource getLinkDataSource2(){
		return lds2;
	}
	
	/**
	 * 
	 * @return	a List of the MatchingConfig objects
	 */
	public List<MatchingConfig> getMatchingConfigs(){
		return mc_configs;
	}
	
	/*
	 * Method added to conveniently go from a list of columns by name,
	 * such as would be returned from a matching config object, and
	 * returns the display position of them.
	 */
	/* Discovered to not be in use
	public int[] getIndexesOfColumnNames(String[] names){
		if(lds1 == null){
			return null;
		}
		int[] ret = new int[names.length];
		for(int i = 0; i < names.length; i++){
			ret[i] = lds1.getDisplayPositionByName(names[i]);
		}
		return ret;
	}*/
	
	/*
	 * Test method to take an XML file and create a RecMatch object from mit
	 */
	public static void main(String[] args){
		if(args.length != 1){
			System.out.println("usage: java RecMatchConfig <XML config file>");
			System.exit(0);
		}
		File config = new File(args[0]);
		if(!config.exists()){
			System.out.println("config file does not exist, exiting");
			System.exit(0);
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try{
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			RecMatchConfig rmc = XMLTranslator.createRecMatchConfig(doc);
			System.out.println("created RecMatchConfig of:\n\t" + rmc);
			XMLTranslator.writeXMLDocToFile(XMLTranslator.toXML(rmc), new File("test.xml"));
		}
		catch(ParserConfigurationException pce){
			System.out.println("error making XML parser: " + pce.getMessage());
		}
		catch(SAXException spe){
			System.out.println("error parsing config file: " + spe.getMessage());
		}
		catch(IOException ioe){
			System.out.println("IO error parsing config file: " + ioe.getMessage());
		}
	}

	public AnalysisConfig getAnalysis_configs() {
		return analysis_configs;
	}

    /**
     * @return the deduplication
     */
    public boolean isDeduplication() {
        return deduplication;
    }

    /**
     * @param deduplication the deduplication to set
     */
    public void setDeduplication(boolean deduplication) {
        this.deduplication = deduplication;
    }


}
