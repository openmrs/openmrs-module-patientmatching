package org.regenstrief.linkage.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;

/**
 * Class created to read a character delimited file without
 * looking at include column in the LinkDataSource object.  This is
 * useful for the GUI since for working with a file all the columns should be
 * read and known, regardless of order or if columns are hidden.
 * 
 * @author jegg
 *
 */

public class BasicCharDelimFileReader extends CharDelimFileReader {
	
	public BasicCharDelimFileReader(LinkDataSource lds){
		data_source = lds;
		
		File raw_file = new File(lds.getName());
		raw_file_sep = lds.getAccess().charAt(0);
		
		try{
			file_reader = new BufferedReader(new FileReader(raw_file));
			next_record = line2Record(file_reader.readLine());
		}
		catch(IOException ioe){
			file_reader = null;
			next_record = null;
		}
	}
	
	public Record line2Record(String line){
		String[] split_line = line.split(getHexString(raw_file_sep), -1);
		Record ret = new Record(record_count++, data_source.getName());
		List<DataColumn> cols = data_source.getDataColumns();
		while(cols.size() > split_line.length){
			// we have data columns in data source that don't match the actual data
			DataColumn dc = cols.remove(split_line.length);
			if(dc.getName().equals(data_source.getUniqueID())){
				data_source.setUniqueID(null);
			}
		}
		for(int i = 0; i < cols.size(); i++){
			int line_index = Integer.parseInt(cols.get(i).getColumnID());
			ret.addDemographic(cols.get(i).getName(), split_line[line_index]);
			
		}
		
		return ret;
	}
}
