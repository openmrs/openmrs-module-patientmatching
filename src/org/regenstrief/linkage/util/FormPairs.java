package org.regenstrief.linkage.util;
import java.io.*;
import java.util.*;

/*
 * Implements the C code to create FormPairs
 * Constructor takes a delimiter argument, but
 * pipe character is hard coded in the split method
 * call since String.split(Character.toString('|')) does
 * not work, it needs to be String.split("\\|");
 * 
 * I have coded a way around it, but have not found the earlier
 * code
 */

public class FormPairs {
	
	File f1, f2, output_file;
	String errf;
	LinkDataSource lds1, lds2;
	int[] blocking_indexes;
	int[] blocking_types;
	boolean[] ignore_nulls;
	BufferedReader f1_reader, f2_reader;
	BufferedWriter out_file, err_file;
	
	public static final String DEFAULT_OUTPUT_FILE = "formed";
	public static final String EXT = ".pairs";
	public static final String ERR_EXT = ".err";
	public static final int LESS_THAN = -1;
	public static final int EQUAL = 0;
	public static final int GREATER_THAN = 1;
	
	
	public FormPairs(File f1, File f2, File output_file, LinkDataSource lds1, LinkDataSource lds2, MatchingConfig mc){
		this.f1 = f1;
		this.f2 = f2;
		this.output_file = output_file;
		this.lds1 = lds1;
		this.lds2 = lds2;
		determineBlockingIndexesAndTypes(mc);
		errf = output_file.getParent() + File.separator + mc.getName() + ERR_EXT;
	}
	
	/*
	 * Blocking index is populated in showing the index of the blocking
	 * columns in the order of blocking, indexed on zero.
	 * 
	 * For example, returning an array with values {3, 2} would
	 * indicate that the fourth column is the first blocking column,
	 * and that the third column is the second blocking column
	 * 
	 * Need to populate the following arrays:
	 * 	blocking_indexes - holds the column indexes that are blocking columns
	 * 	blocking_types - the type values for the blocking_indexes columns
	 * 	ignore_nulls - the ignore null flag for blocking_indexes columns
	 * 
	 */
	private void determineBlockingIndexesAndTypes(MatchingConfig mc){
		blocking_indexes = lds1.getIndexesOfColumnNames(mc.getBlockingColumns());
		int block_col_count = blocking_indexes.length;
		
		// initialize blocking_indexes or correct length, and populate them with the order
		blocking_types = new int[block_col_count];
		ignore_nulls = new boolean[block_col_count];
		for(int i = 0; i < block_col_count; i++){
			blocking_types[i] = lds1.getColumnTypeByName(mc.getRowName(blocking_indexes[i]));
			ignore_nulls[i] = mc.ignoreNullValues(blocking_indexes[i]);
			
		}
		
	}
	
	public void createPairFile(){
		String[] f1_line, f2_line;
		LinkedList<String[]> b_buffer = new LinkedList<String[]>();
		int total_errors = 0;
		try{
			
			f1_reader = new BufferedReader(new FileReader(f1));
			f2_reader = new BufferedReader(new FileReader(f2));
			out_file = new BufferedWriter(new FileWriter(output_file));
			err_file = new BufferedWriter(new FileWriter(errf));
			
			
			f1_line = file1ReadLine();
			f2_line = file2ReadLine();
			if(f1_line == null || f2_line == null){
				return;
			}
			do{
				
				try{
					if(!b_buffer.isEmpty() && compareLineArray(f1_line, b_buffer.getFirst()) == EQUAL){
						createPairs(f1_line, b_buffer);
						f1_line = file1ReadLine();
					}else {
						if(f1_line == null || f2_line == null){
							break;
						}
						int comp = compareLineArray(f1_line, f2_line);
						b_buffer.clear();
						if(comp == EQUAL){
							// the two values are equal, need to read lines from f2 as long as
							// the two are equal and then print
							
							// buffer is empty, need to get values that are equal
							// add the current one before we loop through the file
							b_buffer.add(f2_line);
							while((f2_line = file2ReadLine()) != null && compareLineArray(f1_line, f2_line) == EQUAL){
								b_buffer.add(f2_line);
							}
							// all the equal blocking column lines are read, print to file
							createPairs(f1_line, b_buffer);
							f1_line = file1ReadLine();
						}else if(comp == LESS_THAN){
							// need to advance in file1, and clear buffer
							f1_line = file1ReadLine();
							b_buffer.clear();
						}else if(comp == GREATER_THAN){
							// need to advance in file2, and clear buffer
							f2_line = file2ReadLine();
							b_buffer.clear();
						}
					}
					
				}
				catch(ComparisonException ce){
					total_errors++;
					
					// depending on the file that caused the error, increment the current line
					// print the error information to the error file
					if(ce.getReader() == f1_reader){
						err_file.write("error in " + f1.getName() + ": ");
						f1_line = file1ReadLine();
					} else if(ce.getReader() == f2_reader){
						err_file.write("error in " + f2.getName() + ": ");
						f2_line = file2ReadLine();
					}
					err_file.write(ce.getMessage() + "\n");
				}
			
			}while((f1_line != null && f2_line != null) || (f1_line != null && !b_buffer.isEmpty()));
		
			// flush output
			out_file.flush();
			out_file.close();
			err_file.flush();
			err_file.close();
			
			if(total_errors > 0){
				System.err.println(total_errors + " error(s) encountered");
			}
		}
		catch(IOException ioe){
			System.err.println("error forming pair file: " + ioe.getMessage());
			// clear the variable for the output file, since it's incomplete
			output_file = null;
		}
		
		
	}
	
	/*
	 * Method prints the pairs created from the str1 and the string arrays
	 * in str2s.  The variable str1 has equal blocking column values as
	 * all String arrays in str2s, and so each pairing needs to be printed
	 * to the output file
	 */
	private void createPairs(String[] str1, LinkedList<String[]> str2s) throws IOException{
		Iterator<String[]> it = str2s.iterator();
		while(it.hasNext()){
			String[] str2 = it.next();
			writePair(str1, str2);
		}
	}
	
	/*
	 * Writes the values of the string arrays to the output file,
	 * inserting | characters as delimiters
	 */
	private void writePair(String[] str1, String[] str2) throws IOException{
		String out = new String();
		out += buildLine(str1);
		out += "|";
		out += buildLine(str2);
		out += "\n";
		out_file.write(out);
	}
	
	/*
	 * Compares the two lines based on the values in the blocking columns
	 * and returns either LESS_THAN, EQUAL, or GREATER_THAN
	 */
	private int compareLineArray(String[] line1, String[] line2) throws ComparisonException{
		String[] l1_val = new String[blocking_indexes.length];
		String[] l2_val = new String[blocking_indexes.length];
		int ret = EQUAL;
		for(int i = 0; i < blocking_indexes.length; i++){
			int index = blocking_indexes[i];
			try{
				l1_val[i] = line1[index];
			}
			catch(ArrayIndexOutOfBoundsException aioobe){
				throw new ComparisonException("Invalid column index in line " + buildLine(line1), f1_reader);
			}
			try{
				l2_val[i] = line2[index];
			}
			catch(ArrayIndexOutOfBoundsException aioobe){
				throw new ComparisonException("Invalid column index in line " + buildLine(line2), f2_reader);
			}
			try{
				// check if values are null or empty, and the column is set to ignore null values
				// in comparisons
				if((l1_val[i] == null || l1_val[i].equals("")) && ignore_nulls[i]){
					throw new ComparisonException("null value in blocking column", f1_reader);
				}
				if((l2_val[i] == null || l2_val[i].equals("")) && ignore_nulls[i]){
					throw new ComparisonException("null value in blocking column", f2_reader);
				}
				ret = compareString(l1_val[i], l2_val[i], blocking_types[i]);
			}
			catch(ComparisonException ce){
				if(ce.getReader() == f1_reader){
					throw new ComparisonException(ce.getMessage() + " at line " + buildLine(line1), f1_reader);
				} else if(ce.getReader() == f2_reader){
					throw new ComparisonException(ce.getMessage() + " at line " + buildLine(line2), f2_reader);
				} else {
					throw ce;
				}
				
			}
			if(ret == LESS_THAN || ret == GREATER_THAN){
				break;
			}
		}
		return ret;
	}
	
	/*
	 * Method compares two string values, converting to a numeric type
	 * if needed based on the type value
	 */
	private int compareString(String str1, String str2, int type) throws ComparisonException{
		int ret;
		double d1, d2;
		
		if(type == MatchingConfig.NUMERIC_TYPE){
			try{
				d1 = Double.parseDouble(str1);
			}
			catch(NumberFormatException nfe){
				throw new ComparisonException("Number format exception with " + str1, f1_reader);
			}
			try{
				d2 = Double.parseDouble(str2);
			}
			catch(NumberFormatException nfe){
				throw new ComparisonException("Number format exception with " + str2, f1_reader);
			}
			if(d1 < d2){
				ret = LESS_THAN;
			} else if(d1 == d2){
				ret = EQUAL;
			} else {
				ret = GREATER_THAN;
			}
		} else if(type == MatchingConfig.STRING_TYPE){
			int comp = str1.compareTo(str2);
			if(comp < 0){
				ret = LESS_THAN;
			} else if(comp == 0){
				ret = EQUAL;
			} else {
				ret = GREATER_THAN;
			}
		} else {
			throw new ComparisonException("unknown type given when comparing Strings " + str1 + ", " + str2);
		}
		
		return ret;
	}
	/*
	 * The variable output_file is initialized after createPairFile method is called, so it's assumed to
	 * reperesent the newly created file.  If the method does not return null, then the form pairs 
	 * process has been run
	 */
	public File getOutputFile(){
		return output_file;
	}
	
	private String[] file1ReadLine() throws IOException{
		String str = f1_reader.readLine();
		if(str == null){
			return null;
		}
		String[] ret = str.split("\\|", -1);
		return ret;
	}
	
	private String[] file2ReadLine() throws IOException{
		String str = f2_reader.readLine();
		if(str == null){
			return null;
		}
		String[] ret = str.split("\\|", -1);
		return ret;
	}
	
	private String buildLine(String[] line){
		String ret = new String();
		for(int i = 0; i < line.length - 1; i++){
			ret += line[i] + "|";
		}
		ret += line[line.length - 1];
		return ret;
	}
	
	/**
	 * @param args
	 */
	/*
	 * Method created to mimic the command line behaviour of the C program
	 * to use in porting and testing
	 */
	public static void main(String[] args) {
		if(args.length != 3){
			// match error output with C version
			System.out.println();
			System.out.println("form_pairs error:");
			System.out.println("Incorrct number of arguments. Enter 3 command-line parameters:");
			System.out.println("\t1) Smaller (sorted) file");
			System.out.println("\t2) Larger sorted file");
			System.out.println("\t3) Matrix file");
			System.exit(0);
		}
		
	}

}
