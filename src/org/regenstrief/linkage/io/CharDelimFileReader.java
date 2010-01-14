package org.regenstrief.linkage.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.ColumnSwitcher;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;

/**
 * Class reads lines from a character delimited file until the end of the file is
 * reached.  The input file is first re-written to create a file of only the columns
 * of interest.  This file is then read and Records created using column
 * information in the LinkDataSource object.
 *
 */

public class CharDelimFileReader implements DataSourceReader{
	protected LinkDataSource data_source;
	
	protected File switched_file;
	protected BufferedReader file_reader;
	protected Record next_record;
	protected char raw_file_sep;
	protected int record_count;
	
	public static final String UNIQUE_ID = "uniq_id";
	
	/**
	 * The constructor sorts the file according to the blocking variables and opens
	 * a BufferedReader.  If there's an error with sorting the file, the file reader
	 * is set to null.
	 * 
	 * @param lds	the LinkDataSource with information of a character delimited file
	 */
	public CharDelimFileReader(LinkDataSource lds){
		data_source = lds;
		record_count = 0;
		
		File raw_file = new File(lds.getName());
		
		// determine if unique ID column needs to be added
		if(lds.getUniqueID() == null){
			addIDColumn(lds);
			switched_file = switchColumns(raw_file, true, (lds.getFileHeaderLine() || lds.getSkipFirstRow()));
		} else {
			switched_file = switchColumns(raw_file, false, (lds.getFileHeaderLine() || lds.getSkipFirstRow()));
		}
		
		raw_file_sep = lds.getAccess().charAt(0);
		
		try{
			file_reader = new BufferedReader(new FileReader(switched_file));
			next_record = line2Record(file_reader.readLine());
		}
		catch(IOException ioe){
			file_reader = null;
			next_record = null;
		}
	}
	
	/*
	 * Default constructor protected so subclasses don't need to call super class constructor
	 */
	protected CharDelimFileReader(){
		data_source = null;
	}
	
	
	protected void addIDColumn(LinkDataSource lds){
		int id_index = lds.getDataColumns().size();
		int id_include = lds.getIncludeCount();
		DataColumn dc = new DataColumn(Integer.toString(id_index));
		dc.setName(UNIQUE_ID);
		dc.setType(DataColumn.NUMERIC_TYPE);
		dc.setIncludePosition(id_include);
		lds.addDataColumn(dc);
		lds.setUniqueID(UNIQUE_ID);
	}
	
	public int getRecordSize(){
		return data_source.getIncludeCount();
	}
	
	/*
	 * Method switches columns.  Gets information from LinkDataSource object.
	 * 
	 * @param f	the file to modify
	 * @return	the resulting file
	 */
	protected File switchColumns(File f, boolean add_id, boolean header_line){
		List<DataColumn> dcs1 = data_source.getDataColumns();
		int[] order1 = new int[data_source.getIncludeCount()];
		
		// iterate over the  DataColumn list and store the data position value
		// in order arrays at the index given by display_position, as long as
		// display position is not NA
		Iterator<DataColumn> it1 = dcs1.iterator();
		while(it1.hasNext()){
			DataColumn dc = it1.next();
			if(dc.getIncludePosition() != DataColumn.INCLUDE_NA){
				order1[dc.getIncludePosition()] = Integer.parseInt(dc.getColumnID());
			}
		}
		
		File switched = new File(data_source.getName() + ".switched");
		try{
			ColumnSwitcher cs = new ColumnSwitcher(f, switched, order1, data_source.getAccess().charAt(0));
			cs.setAddIDColumn(add_id);
			cs.setReadHeaderLine(header_line);
			cs.switchColumns();
		}
		catch(IOException ioe){
			return null;
		}
		
		return switched;
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
	 * Converts a character delimited String into a Record object based on the 
	 * data source information in this object's LinkDataSource.
	 * 
	 * @param line	character-delimited line to convert to a Record object
	 * @return	the Record object with the data from that line
	 */
	public Record line2Record(String line){
		String[] split_line = line.split(getHexString(raw_file_sep), -1);
		
		DataColumn id_column = data_source.getIncludedDataColumns().get(data_source.getUniqueID());
		int id = Integer.parseInt(split_line[id_column.getIncludePosition()]);
		
		Record ret = new Record(id, data_source.getName());
		List<DataColumn> cols = data_source.getDataColumns();
		for(int i = 0; i < cols.size(); i++){
			//int line_index = Integer.parseInt(cols.get(i).getColumnID());
			DataColumn col = cols.get(i);
			int include_index = col.getIncludePosition();
			if(include_index != -1 && !col.getName().equals(data_source.getUniqueID())){
				ret.addDemographic(cols.get(i).getName(), split_line[include_index]);
			}
			
		}
		
		return ret;
	}
	
	protected String getHexString(char c){
		int i = Integer.valueOf(c);
		String hex = Integer.toHexString(i);
		while(hex.length() < 4){
			hex = "0" + hex;
		}
		hex = "\\u" + hex;
		return hex;
	}
	
	/**
	 * Returns a boolean indicating if the reset of the reader was successful.
	 */
	public boolean reset(){
		try {
			file_reader.close();
			file_reader = new BufferedReader(new FileReader(switched_file));
			next_record = line2Record(file_reader.readLine());			
			return true;
		} catch (IOException e1) {
			return false;
		}
		
	}
	
	public boolean close(){
		return true;
	}
}