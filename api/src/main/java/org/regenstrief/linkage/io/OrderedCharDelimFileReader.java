package org.regenstrief.linkage.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
 * Class extends CharDelimFileReader by taking a MatchingConfig object in the constructor.
 * Information in this object determines the order to sort the file by looking at the blocking
 * variables. This is necessary to provide one-pass Record pair forming. The unix sort command is
 * used to create a new file from the switched file which is then read.
 */
public class OrderedCharDelimFileReader extends CharDelimFileReader implements OrderedDataSourceReader {
	
	private MatchingConfig mc;
	
	private File sorted_file;
	
	/**
	 * Constructs a reader, but returns the Records in order specified by the blocking variables.
	 * 
	 * @param lds the description of the data
	 * @param mc information on the record linkage options, containing blocking variable order (sort
	 *            order)
	 */
	public OrderedCharDelimFileReader(LinkDataSource lds, MatchingConfig mc) {
		//super(lds);
		data_source = lds;
		this.mc = mc;
		raw_file_sep = data_source.getAccess().charAt(0);
		long modified_date = new File(data_source.getName()).lastModified();
		File expected = getExpectedFile(new File(data_source.getName()));
		
		// check if initReader() file exists, and if so, it's modification date isn't newer than modification date of original file
		if (expected.exists()) {
			if (expected.lastModified() > modified_date) {
				try {
					file_reader = new BufferedReader(new FileReader(expected));
					String line = file_reader.readLine();
					if ((line != null) && line.equals(header) && (lds.getFileHeaderLine() || lds.getSkipFirstRow())) {
						line = file_reader.readLine();
					}
					if (line != null) {
						next_record = line2Record(line);
					} else {
						file_reader.close();
						file_reader = null;
						initReader();
					}
				}
				catch (IOException ioe) {
					file_reader = null;
					next_record = null;
				}
			} else {
				initReader();
			}
		} else {
			initReader();
		}
		
	}
	
	private File getExpectedFile(File f) {
		Iterator<DataColumn> it1 = data_source.getDataColumns().iterator();
		boolean same = true;
		int expected = 0;
		while (it1.hasNext()) {
			DataColumn dc = it1.next();
			same = dc.getIncludePosition() == expected && same;
			expected++;
		}
		
		File switched;
		if (same) {
			switched = f;
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				header = br.readLine();
				br.close();
			}
			catch (IOException ioe) {
				
			}
		} else {
			switched = new File(f.getPath() + ".switched");
		}
		String desc = mc.getBlockingHash();
		File sorted = new File(switched.getPath() + desc + ".sorted");
		return sorted;
	}
	
	private void initReader() {
		if (data_source.getUniqueID() == null) {
			addIDColumn(data_source);
			this.switched_file = switchColumns(new File(data_source.getName()), mc, true, data_source.getFileHeaderLine());
		} else {
			this.switched_file = switchColumns(new File(data_source.getName()), mc, false, data_source.getFileHeaderLine());
		}
		
		sorted_file = sortInputFile(switched_file, raw_file_sep);
		try {
			file_reader = new BufferedReader(new FileReader(sorted_file));
			String line = file_reader.readLine();
			if (header != null && header.equals(line)) {
				line = file_reader.readLine();
			}
			next_record = line2Record(line);
		}
		catch (IOException ioe) {
			file_reader = null;
			next_record = null;
		}
	}
	
	protected File switchColumns(File f, MatchingConfig mc, boolean add_id, boolean header_line) {
		List<DataColumn> dcs1 = data_source.getDataColumns();
		int[] order1 = new int[data_source.getIncludeCount()];
		
		// iterate over the  DataColumn list and store the data position value
		// in order arrays at the index given by display_position, as long as
		// display position is not NA
		Iterator<DataColumn> it1 = dcs1.iterator();
		boolean same = true;
		int expected = 0;
		while (it1.hasNext()) {
			DataColumn dc = it1.next();
			if (dc.getIncludePosition() != DataColumn.INCLUDE_NA) {
				order1[dc.getIncludePosition()] = Integer.parseInt(dc.getColumnID());
			}
			same = dc.getIncludePosition() == expected && same;
			expected++;
		}
		
		if (same) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				header = br.readLine();
				br.close();
			}
			catch (IOException ioe) {
				
			}
			return f;
		}
		
		File switched = new File(data_source.getName() + ".switched");
		try {
			ColumnSwitcher cs = new ColumnSwitcher(f, switched, order1, data_source.getAccess().charAt(0));
			cs.setAddIDColumn(add_id);
			cs.setReadHeaderLine(header_line);
			cs.switchColumns();
		}
		catch (IOException ioe) {
			return null;
		}
		
		return switched;
	}
	
	private File sortInputFile(File f, char sep) {
		// sort the data files based on their blocking variables
		// the blocking order determines the sort order
		// if the data file are different files, need to sort each
		// using two ColumnSorter objects with the respective seperating characters
		// method returns true or false depending on success of sorting
		//int[] column_order = data_source.getIndexesOfColumnNames(mc.getBlockingColumns());
		//
		// Cicada Dennis comment - 2016-08-25:
		// This function writes the sorted data into another File.
		// That File object is returned by this method.
		// If there is an error, then null is returned. The above statement that true or false is
		// is returned is not precisely accurate.
		// It appears to me that there may be parts of the code that assume
		// what the name of the sorted file is, rather than depending on the
		// File object that is returned by this method. The assumption is that
		// the filename is created based on the values of the sort blocking variables.
		
		// column IDs for character delimited file should hold line array index
		// of column
		String[] blocking_columns = mc.getBlockingColumns();
		String desc = mc.getBlockingHash();
		File sorted = new File(f.getPath() + desc + ".sorted");
		// if there are blocking columns
		if (blocking_columns != null) {
			int[] column_order = data_source.getIncludeIndexesOfColumnNames(blocking_columns);
			
			int[] column_types = new int[column_order.length];
			for (int i = 0; i < column_order.length; i++) {
				//column_types[i] = data_source.getColumnTypeByName(mc.getRowName(column_order[i]));
				column_types[i] = data_source.getDataColumn(column_order[i]).getType();
			}
			
			// create ColumnSortOption objects for metafile
			Vector<ColumnSortOption> options = new Vector<ColumnSortOption>();
			for (int i = 0; i < column_order.length; i++) {
				// column order is zero based, column options needs to be 1 based
				options.add(new ColumnSortOption(column_order[i] + 1, ColumnSortOption.ASCENDING, column_types[i]));
			}
			// Cicada Dennis comments - 2016-08-23:
			// The ColumnSorter object was being used like a function. It was created solely for the
			// purpose of calling runSort on it, then discarded. As far as I can tell, this is the
			// only place in the code where this object is used. 
			// It made more sense to me to have a single static method that does the sort
			// and just send the File object to it. 
			// Also, the parameter sep is passed in to this method, 
			// but it is not used and instead data_source.getAccess()charAt() is used.
			// I didn't change it, but why is it done this way?
			// 
			// OLD WAY:
			// try{
			// create FileOutputStream for the result of the sort
			// FileOutputStream data1_fos = new FileOutputStream(sorted);
			// ColumnSorter sort_data1 = new ColumnSorter(data_source.getAccess().charAt(0), options, f, data1_fos);
			// sort_data1.runSort();
			// data1_fos.close();
			// }
			// catch(FileNotFoundException fnfe){
			// 	// if can't open the output stream at this stage, return signaling failure
			// 	// since the later steps make no sense without a file from this step
			// 	return null;
			// } catch (IOException e) {
			// 	e.printStackTrace();
			// 	return null;
			// }
			// return sorted;
			//
			// NEW WAY:
			boolean success = ColumnSorter.sortColumns(data_source.getAccess().charAt(0), options, f, sorted);
			if (!success) {
				return null;
			} else {
				return sorted;
			}
		}
		// When there are no blocking columns, write the file as it is
		else {
			// Cicada Dennis comment - 2016-09-16:
			// Would a system call to duplicate the file be more efficient here?
			// And an overall comment, for small files, duplication is not an issue,
			// but for really large files, duplication is probably not the best
			// thing for the program to do. For instance, it could create a link
			// that pointed to the original file. Or the program could be informed
			// that there was no sorting needed, since there were no blocking variables.
			try {
				FileInputStream fis = new FileInputStream(f);
				FileOutputStream fos = new FileOutputStream(sorted);
				byte[] buf = new byte[1024];
				int i = 0;
				while ((i = fis.read(buf)) != -1) {
					fos.write(buf, 0, i);
				}
				fis.close();
				fos.close();
				return sorted;
			}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	/*
	 * Converts a character delimited String into a Record object based on the 
	 * data source information in this object's LinkDataSource.
	 * 
	 * @param line	character-delimited line to convert to a Record object
	 * @return	the Record object with the data from that line
	 */
	/*
	public Record line2Record(String line){
		String[] split_line = line.split(getHexString(raw_file_sep), -1);
		Record ret = new Record();
		List<DataColumn> cols = data_source.getDataColumns();
		for(int i = 0; i < cols.size(); i++){
			DataColumn dc = cols.get(i);
			int line_index = Integer.parseInt(dc.getColumnID());
			int include_index = dc.getIncludePosition();
			if(include_index != -1){
				MatchingConfigRow mcr = mc.getMatchingConfigRowByName(dc.getName());
				int block_order = mcr.getBlockOrder();
				if(include_index != -1 || block_order > 0){
					ret.addDemographic(dc.getName(), split_line[line_index]);
				}
			}
			
			
		}
		
		return ret;
	}*/
	
	/*
	 * Need to override parent class since sorted file needs to be read, not
	 * switched file.
	 * 
	 * @see org.regenstrief.linkage.io.CharDelimFileReader#reset()
	 */
	public boolean reset() {
		try {
			file_reader.close();
			file_reader = new BufferedReader(new FileReader(sorted_file));
			next_record = line2Record(file_reader.readLine());
			return true;
		}
		catch (IOException e1) {
			return false;
		}
		
	}
}
