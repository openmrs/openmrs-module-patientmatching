package org.regenstrief.linkage.deprecated;
/* 
 * *************************************************************
 * This class was first a class that called the C binaries, then it was
 * modified to call the new Java classes that replaced the C
 * binaries, and then it was unused when the program was
 * restructured.
 * *************************************************************
 * 
 * Class takes the data needed to run the C record linkage
 * software and create the results.  The static arguments
 * for the class's main method match the constructor in the
 * required files needed for it to run and estimate flag.
 * These files are:
 * 	1.	Meta-file - has the scheme of the matching run
 * 	2.	Matched files - the re-ordered data files where the 
 * 		columns of each file are of the same data
 * 	3.	
 * 
 * The process used to link the two data files are:
 * 	1.	Sort data files on their blocking variables
 * 		This currently uses the unix or cygwin sort command
 * 	2.	Generate pair file using form_pairs.exe
 * 			form_pairs.exe <fileA> <fileB> <meta_file> > <pairs_file>
 * 	3.	If estimate is selected, run the estimation program
 * 			EM <pairs_file> <meta_file> <iterations>
 * 	4.	Score record pairs using score_pairs.exe
 * 			score_pairs.exe <pairs_file> <meta_file> > <score_file>
 * 	5.	Score records in descending order using record pair score
 * 		Te score will be the first column of the score pairs file
 * 
 * The C implementations of for_pairs and score_pairs send the output to 
 * STDOUT, requiring the redirection of output to a file.
 * 
 * For testing, the CLI command to run the program is:
 * 	java RecordLinker <fileA> <fileB> <meta_file> <sep> [true|false]
 * So a sample invocation would be:
 * 	java RecordLinker file1.txt.srt file2.txt.srt sample.meta "|" true
 */

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.regenstrief.linkage.util.ColumnSortOption;
import org.regenstrief.linkage.util.ColumnSorter;
import org.regenstrief.linkage.util.ColumnSwitcher;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.FormPairs;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class RecordLinker extends Linker{
	// original files
	File data1, data2, output_dir;
	
	// temporary files created during linkage process
	File data1_link, data2_link, data1step1, data2step1, pairsstep2, scorestep4, sorted_score_step5;
	
	boolean est, same_file;
	
	int[] column_types;
	
	// file extensions for the different steps and other constants
	public final static String STEP0_EXT = ".link";
	public final static String STEP1_EXT = ".sort";
	public final static String STEP2_EXT = ".pairs";
	public final static String STEP4_EXT = ".tmp";
	public final static String STEP5_EXT = ".score";
	// needed when running shell scripts, not needed when
	// running programs directly
	// public static final String CMD_PREFIX = CmdLauncher.getPrefix();
	public static final String WIN_DIR = "win32";
	public static final String LIN_DIR = "linux";
	
	
	public final static int EM_ITERATIONS = 15;
	
	public RecordLinker(LinkDataSource lds1, LinkDataSource lds2, MatchingConfig mc, File output_dir){
		// constructor implementation using File objects instead of String representation of paths
		super(lds1, lds2, mc, true);
		data1 = new File(lds1.getName());
		data2 = new File(lds2.getName());
		this.output_dir = output_dir;
	}
	
	public boolean runLinkage(){
		// run the steps to perform the linking of the two files stored in class variables
		// success variable will keep track of each step succeeding
		boolean success;
		System.out.println("starting linkage process");
		
		// re-write files in the correct order given by the link data source
		System.out.println("re-writing input files");
		success = switchColumns();
		
		// sort data files based on blocking variables
		System.out.println("sorting data files");
		if(success){
			success = sortDataFiles();
		}
		
		// call Linker's runLinkage() method to perform formPairs, EM, and scorePairs
		if(success){
			success = super.runLinkage();
		}
		
		// sort records
		if(success){
			success = sortScoreFile();
		}
		
		if(success){
			System.out.println("linkage completed successfully");
		} else {
			System.out.println("error running linkage process");
		}
		return success;
	}
	
	private boolean switchColumns(){
		List<DataColumn> dcs1 = lds1.getDataColumns();
		List<DataColumn> dcs2 = lds2.getDataColumns();
		int[] order1 = new int[lds1.getIncludeCount()];
		int[] order2 = new int[lds2.getIncludeCount()];
		
		// iterate over the  DataColumn list and store the data position value
		// in order arrays at the index given by display_position, as long as
		// display positioin is not NA
		Iterator<DataColumn> it1 = dcs1.iterator();
		while(it1.hasNext()){
			DataColumn dc = it1.next();
			if(dc.getIncludePosition() != DataColumn.INCLUDE_NA){
				order1[dc.getIncludePosition()] = Integer.parseInt(dc.getColumnID());
			}
		}
		
		Iterator<DataColumn> it2 = dcs2.iterator();
		while(it2.hasNext()){
			DataColumn dc = it2.next();
			if(dc.getIncludePosition() != DataColumn.INCLUDE_NA){
				order2[dc.getIncludePosition()] = Integer.parseInt(dc.getColumnID());
			}
		}
		
		data1_link = new File(output_dir.getPath() + File.separator + mc.getName() + "1" + STEP0_EXT);
		data2_link = new File(output_dir.getPath() + File.separator + mc.getName() + "2" + STEP0_EXT);
		try{
			ColumnSwitcher cs = new ColumnSwitcher(data1, data1_link, order1, lds1.getAccess().charAt(0));
			cs.switchColumns();
			cs = new ColumnSwitcher(data2, data2_link, order1, lds2.getAccess().charAt(0));
			cs.switchColumns();
		}
		catch(IOException ioe){
			return false;
		}
		
		return true;
	}
	
	private boolean sortDataFiles(){
		// sort the data files based on their blocking variables
		// the blocking order determines the sort order
		// if the data file are different files, need to sort each
		// using two ColumnSorter objects with the respective seperating characters
		// method returns true or false depending on success of sorting
		int[] column_order = lds1.getIncludeIndexesOfColumnNames(mc.getBlockingColumns());
		column_types = new int[column_order.length];
		for(int i = 0; i < column_order.length; i++){
			column_types[i] = lds1.getColumnTypeByName(mc.getRowName(column_order[i]));
		}
		
		// create ColumnSortOption objects for metafile
		Vector<ColumnSortOption> options = new Vector<ColumnSortOption>();
		for(int i = 0; i < column_order.length; i++){
			// column order is zero based, column options needs to be 1 based
			options.add(new ColumnSortOption(column_order[i] + 1, ColumnSortOption.ASCENDING, column_types[i]));
		}
		
		// create FileOutputStream for the result of the sort
		try{
			data1step1 = new File(output_dir.getCanonicalPath() + File.separator + mc.getName() + "sort1" + STEP1_EXT);
		}
		catch(IOException ioe){
			// canonical path failed, attempt just using normal getPath
			data1step1 = new File(output_dir.getPath() + File.separator + mc.getName() + "sort1" + STEP1_EXT);
		}
		try{
			FileOutputStream data1_fos = new FileOutputStream(data1step1);
			ColumnSorter sort_data1 = new ColumnSorter(lds1.getAccess().charAt(0), options, data1_link, data1_fos);
			sort_data1.runSort();
		}
		catch(FileNotFoundException fnfe){
			// if can't open the output stream at the stage, return signaling failure
			// as the later steps make no sense without a file from this step
			return false;
		}
		
		// if second file not the same as the first, create columnsorter object for second file file
		if(same_file){
			data2step1 = data1step1;
		} else {
			try{
				data2step1 = new File(output_dir.getCanonicalPath() + File.separator + mc.getName() + "sort2" + STEP1_EXT);
			}
			catch(IOException ioe){
				// canonical path failed, attempt just using normal getPath
				data2step1 = new File(output_dir.getPath() + File.separator + mc.getName() + "sort2" + STEP1_EXT);
			}
			try{
				FileOutputStream data2_fos = new FileOutputStream(data2step1);
				ColumnSorter sort_data2 = new ColumnSorter(lds2.getAccess().charAt(0), options, data2_link, data2_fos);
				sort_data2.runSort();
			}
			catch(FileNotFoundException fnfe){
				// if can't open the output stream at the stage, return signaling failure
				// as the later steps make no sense without a file from this step
				return false;
			}
		}
		
		return true;
	}
	
	public boolean formPairs(){
		pairsstep2 = new File(output_dir.getPath() + File.separator + mc.getName() + STEP2_EXT);
		FormPairs fp = new FormPairs(data1step1, data2step1, pairsstep2, lds1, lds2, mc);
		fp.createPairFile();
		
		// if the output file is initialized, then it is assumed to have succeeded
		return(pairsstep2 != null);
	}
	
	/*
	 * This method modified on September 22nd, 2006 to change from external C code of
	 * EM to using the EM Java class
	 */
	public boolean estimateValues(){
		
		EM em = new EM(pairsstep2, mc, EM_ITERATIONS, lds1);
		try{
			em.estimate();
		}
		catch(IOException ioe){
			return false;
		}
		return true;
	}
	
	public boolean scorePairs(){
		
		ScorePairs sp = new ScorePairs(pairsstep2, mc, lds1);
		try{
			sp.createScoreFile();
		}catch(IOException ioe){
			return false;
		}
		scorestep4 = sp.getOutputFile();
		return scorestep4 != null;
	}
	
	private boolean sortScoreFile(){
		// need to sort the resulting file from the score pairs step on column 1 in descending order
		// give the resulting sorted file a name the same as the meta file, but a different extension
		// ie., simple.meta -> simple.score
		Vector<ColumnSortOption> option = new Vector<ColumnSortOption>();
		option.add(new ColumnSortOption(1, ColumnSortOption.DESCENDING, ColumnSortOption.NUMERIC));
		
		sorted_score_step5 = new File(output_dir.getPath() + File.separator + mc.getName() + STEP5_EXT);
		try{
			FileOutputStream score_fos = new FileOutputStream(sorted_score_step5);
			ColumnSorter sort_score = new ColumnSorter('|', option, scorestep4, score_fos);
			sort_score.runSort();
		}
		catch(FileNotFoundException fnfe){
			// if can't open the output stream at the stage, return signaling failure
			// as the later steps make no sense without a file from this step
			return false;
		}
		return true;
	}
	
	public static void main(String[] argv){
		// instantiate the class and run the steps needed to perform record linkage
		// not currently worried about error specificity
		if(argv.length != 1){
			System.out.println("usage:\njava RecordLinker <xml config file>");
			System.exit(0);
		}
		File xml_config = new File(argv[0]);
		if(!xml_config.exists()){
			System.out.println("xml config file does not exist");
			System.exit(0);
		}
		
		// create config object from xml file and use it to create new
		// RecordLinker object
		File output_dir = xml_config.getParentFile();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try{
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(xml_config);
			RecMatchConfig rmc = XMLTranslator.createRecMatchConfig(doc);
			RecordLinker rl = new RecordLinker(rmc.getLinkDataSource1(), rmc.getLinkDataSource2(), rmc.getMatchingConfigs().get(0), output_dir);
			rl.runLinkage();
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
}
