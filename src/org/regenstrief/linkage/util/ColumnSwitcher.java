package org.regenstrief.linkage.util;
/*
 * Implements the older program cswitch.pl
 * 
 * The purpose is to switch the columns in a character-delimited text
 * file to a new order.  To reverse a five column-pipe delimited text file, 
 * the arguments to the old cswitch.pl program was:
 * 		cswitch.pl file.txt | 4 3 2 1 0
 * 
 */
import java.io.*;

public class ColumnSwitcher{
	public static final String EXTENSION = ".link";
	
	private File original, results;
	private char sep_char;
	private int[] order;
	
	public ColumnSwitcher(File old_file, File new_file, int[] order, char sep){
		sep_char = sep;
		original = old_file;
		results = new_file;
		this.order = order;
	}
	
	public boolean switchColumns() throws IOException{
		boolean ret = false;
		BufferedReader in = new BufferedReader(new FileReader(original));
		BufferedWriter out = new BufferedWriter(new FileWriter(results));
		String line;
		String[] split_line;
		
		while((line = in.readLine()) != null){
			// split the line using the delimiting character, and write the fields to the new file based
			// on new_order array
			if(sep_char == '|'){
				split_line = line.split("\\|");
			} else {
				split_line = line.split(String.valueOf(sep_char));
			}
			
			for(int i = 0; i < order.length; i++){
				try{
					out.write(split_line[order[i]]);
				}
				catch(ArrayIndexOutOfBoundsException aioobe){
					// this can be thrown here if the line ends with the seperating
					// character and no blank string is returned to keep split_line
					// the same size, ie. a|b|c -> {a,b,c} but a|b| -> {a,b}
					// if the order were 3, 2, 1, then split_line is too short
					// to fix, write a blank string
					out.write("");
				}
				
				if(i < order.length - 1){
					out.write(sep_char);
				}
			}
			out.write("\n");
		}
		out.flush();
		out.close();
		ret = true;
		return ret;
	}
	
	public static void main(String[] argv){
		// added to test class directly, taking same argument as the
		// cswitch.pl script did
		String file = argv[0];
		char sep = argv[1].charAt(0);
		int[] order = new int[argv.length - 2];
		int order_index = 0;
		for(int i = 2; i < argv.length; i++){
			order[order_index++] = Integer.parseInt(argv[i]);
		}
		
		
		
	}

}
