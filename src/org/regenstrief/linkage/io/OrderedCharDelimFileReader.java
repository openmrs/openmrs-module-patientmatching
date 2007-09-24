package org.regenstrief.linkage.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.regenstrief.linkage.util.ColumnSortOption;
import org.regenstrief.linkage.util.ColumnSorter;
import org.regenstrief.linkage.util.ColumnSwitcher;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
/**
 * Class extends CharDelimFileReader by taking a MatchingConfig object in
 * the constructor.  Information in this object determines the order to sort
 * the file by looking at the blocking variables.  This is necessary to
 * provide one-pass Record pair forming.
 * 
 * The unix sort command is used to create a new file from the switched file
 * which is then read.
 *
 */
public class OrderedCharDelimFileReader extends CharDelimFileReader {
	
	private MatchingConfig mc;
	private File sorted_file;
	
	/**
	 * Constructs a reader, but returns the Records in order specified by the
	 * blocking variables.
	 * 
	 * @param lds	the description of the data
	 * @param mc	information on the record linkage options, containing blocking variable order (sort order)
	 */
	public OrderedCharDelimFileReader(LinkDataSource lds, MatchingConfig mc){
		super(lds);
		this.mc = mc;
		this.switched_file = switchColumns(new File(lds.getName()), mc);
		char raw_file_sep = lds.getAccess().charAt(0);
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
	
	protected File switchColumns(File f, MatchingConfig mc){
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
		
		File switched = new File(data_source.getName() + this.mc.getName() + ".switched");
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
		//int[] column_order = data_source.getIndexesOfColumnNames(mc.getBlockingColumns());
		
		// column IDs for character delimited file should hold line array index
		// of column
		String [] blocking_columns = mc.getBlockingColumns();
		File sorted = new File(f.getPath() + ".sorted");
		// if there are blocking columns
		if(blocking_columns != null) {
			int[] column_order = data_source.getIncludeIndexesOfColumnNames(blocking_columns);
			
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

			try{
				FileOutputStream data1_fos = new FileOutputStream(sorted);
				ColumnSorter sort_data1 = new ColumnSorter(data_source.getAccess().charAt(0), options, f, data1_fos);
				sort_data1.runSort();
				data1_fos.close();
			}
			catch(FileNotFoundException fnfe){
				// if can't open the output stream at the stage, return signaling failure
				// as the later steps make no sense without a file from this step
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			return sorted;
		}
		// When there are no blocking columns, write the file as it is
		else {
			try {
				FileInputStream fis = new FileInputStream(f);
				FileOutputStream fos = new FileOutputStream(sorted);
			    byte[] buf = new byte[1024];
			    int i = 0;
			    while((i=fis.read(buf))!=-1) {
			      fos.write(buf, 0, i);
			    }
			    fis.close();
			    fos.close();
			    return sorted;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	/*
	 * Need to override parent class since sorted file needs to be read, not
	 * switched file.
	 * 
	 * @see org.regenstrief.linkage.io.CharDelimFileReader#reset()
	 */
	public boolean reset(){
		try {
			file_reader.close();
			file_reader = new BufferedReader(new FileReader(sorted_file));
			next_record = line2Record(file_reader.readLine());			
			return true;
		} catch (IOException e1) {
			return false;
		}
		
	}
}
