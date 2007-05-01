package org.regenstrief.linkage.io;

import org.regenstrief.linkage.*;
import org.regenstrief.linkage.util.*;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Class reads lines from a character delimited file until the end of the file is
 * reached.  To fulfill the expectation of returning Records in order sorted on
 * the blocking values, the file is sorted either using UNIX sort command or a
 * Windows port of sort.  The sorted file is created and read from.
 *
 */

public class CharDelimFileReader extends DataSourceReader{
	
	File sorted_file;
	BufferedReader file_reader;
	Record next_record;
	
	/**
	 * The constructor sorts the file according to the blocking variables and opens
	 * a BufferedReader.  If there's an error with sorting the file, the file reader
	 * is set to null.
	 * 
	 * @param lds	the LinkDataSource with information of a character delimited file
	 * @param mc	MatchingConfig object with the blocking variables information
	 */
	public CharDelimFileReader(LinkDataSource lds, MatchingConfig mc){
		super(lds, mc);
		File raw_file = new File(lds.getName());
		char raw_file_sep = lds.getAccess().charAt(0);
		File switched_file = switchColumns(raw_file);
		sorted_file = sortInputFile(switched_file, raw_file_sep);
		try{
			file_reader = new BufferedReader(new FileReader(sorted_file));
			next_record = line2Record(file_reader.readLine());
		}
		catch(IOException ioe){
			file_reader = null;
			next_record = null;
		}
	}
	
	/*
	 * Class switches columns
	 * 
	 * @param f	the file to modify
	 * @return	the resulting file
	 */
	private File switchColumns(File f){
		List<DataColumn> dcs1 = data_source.getDataColumns();
		int[] order1 = new int[data_source.getIncludeCount()];
		
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
		
		File switched = new File(mc.getName() + ".switched");
		try{
			ColumnSwitcher cs = new ColumnSwitcher(f, switched, order1, data_source.getAccess().charAt(0));
			cs.switchColumns();
		}
		catch(IOException ioe){
			return null;
		}
		
		return switched;
	}
	
	private File sortInputFile(File f, char sep){
		// sort the data files based on their blocking variables
		// the blocking order determines the sort order
		// if the data file are different files, need to sort each
		// using two ColumnSorter objects with the respective seperating characters
		// method returns true or false depending on success of sorting
		int[] column_order = data_source.getIndexesOfColumnNames(mc.getBlockingColumns());
		int[] column_types = new int[column_order.length];
		for(int i = 0; i < column_order.length; i++){
			column_types[i] = data_source.getColumnTypeByName(mc.getRowName(column_order[i]));
		}
		
		// create ColumnSortOption objects for metafile
		Vector<ColumnSortOption> options = new Vector<ColumnSortOption>();
		for(int i = 0; i < column_order.length; i++){
			// column order is zero based, column options needs to be 1 based
			options.add(new ColumnSortOption(column_order[i] + 1, ColumnSortOption.ASCENDING, column_types[i]));
		}
		
		// create FileOutputStream for the result of the sort
		File sorted;
		
		sorted = new File(mc.getName() + ".sorted");
		try{
			FileOutputStream data1_fos = new FileOutputStream(sorted);
			ColumnSorter sort_data1 = new ColumnSorter(data_source.getAccess().charAt(0), options, f, data1_fos);
			sort_data1.runSort();
		}
		catch(FileNotFoundException fnfe){
			// if can't open the output stream at the stage, return signaling failure
			// as the later steps make no sense without a file from this step
			return null;
		}
		
		
		return sorted;
	}
	
	/**
	 * Returns whether there are more records to be read from the reader.
	 */
	public boolean hasNextRecord(){
		return next_record != null;
	}
	
	/**
	 * Returns the next Record in the data source.  If there are no more recurds, returns null
	 */
	public Record nextRecord(){
		if(file_reader == null){
			return null;
		}
		Record ret = next_record;
		try{
			String line = file_reader.readLine();
			if(line != null){
				next_record = line2Record(line);
			} else {
				next_record = null;
			}
		}
		catch(IOException ioe){
			next_record = null;
		}
		return ret;
	}
	
	/**
	 * Converts a character delimted String into a Record object based on the 
	 * data source information in this object's LinkDataSource.
	 * 
	 * @param line	character-delimited line to convert to a Record object
	 * @return	the Record object with the data from that line
	 */
	public Record line2Record(String line){
		String[] split_line = line.split("\\|", -1);
		Record ret = new Record();
		List<DataColumn> cols = data_source.getDataColumns();
		for(int i = 0; i < cols.size(); i++){
			int include_index = cols.get(i).getIncludePosition();
			if(include_index != -1){
				ret.addDemographic(cols.get(i).getName(), split_line[include_index]);
			}
			
		}
		
		return ret;
	}
	
	/**
	 * Returns a boolean indicating if the reset of the reader was successful.
	 */
	public boolean reset(){
		return false;
	}
}
