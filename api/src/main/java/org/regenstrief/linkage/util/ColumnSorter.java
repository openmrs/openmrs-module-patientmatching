package org.regenstrief.linkage.util;
/*
 * Class sorts character delimted text files with functionality
 * similar to that of the unix 'sort' command.
 * 
 * Currently implemented by running the external sort command.
 * On Windows this is done by using cygwin.  The class determines
 * what OS it is running on and calls the sort command appropriately
 * for the platform.
 * 
 * ColumnSortOption objects are stored in the options Vector and lists
 * the columns that need to be sorted and the parameters for the sort.
 * The order in the vector determines the priority/order of the sort.
 */

import java.util.*;
import java.io.*;

public class ColumnSorter{
	char sep;
	Vector options;
	File input;
	OutputStream os;
	
	// the command to run based on sort arguments and platorm
	String[] cmd;
	
	public ColumnSorter(char s, Vector o, File i, OutputStream outs){
		sep = s;
		options = o;
		input = i;
		os = outs;
		cmd = buildCommand();
		
	}
	
	public void runSort(){
		// creates a CmdLauncher object and gives it the command to sort the file
		CmdLauncher cl = new CmdLauncher(cmd, os);
		cl.runCommand();
	}
	
	private String[] buildCommand(){
		// examines the current values for sep, options, and input
		// builds the needed command
		// assume that the previous building of command left a space at the end
		String os_name = System.getProperties().getProperty("os.name");
		String[] arr_command;
		// first need platform specific prefix to run the command
		if(os_name.indexOf("Windows") != -1){
			arr_command = new String[1 + 1 + 1 + options.size() + 1];
		} else {
			arr_command = new String[1 + 1 + options.size() + 1];
		}
		String built_command = "sort ";
		// windows sort binary has been moved to the win32 directory, so check
		// name of OS and use the correct command name
		
		if(os_name.indexOf("Windows") != -1){
			arr_command[0] = "win32\\sort";
		} else {
			arr_command[0] = "sort";
		}
		
		// sort using alphanumerics and whitespace
		//built_command += "-d ";
		//arr_command[1] = "-d";
		
		// specify seperator
		built_command += "-t" + Character.toString(sep);
		arr_command[1] = "-t" + Character.toString(sep);
		
		// if on windows, specify the tmp directory for sort to use
		int index = 2;
		if(os_name.indexOf("Windows") != -1){
			arr_command[index] = "-Ttmp";
			index++;
		}
		
		// loop through the options and append to the end of built_command
		Iterator it = options.iterator();
		ColumnSortOption cso;
		String sort_option;
		while(it.hasNext()){
			cso = (ColumnSortOption)it.next();
			built_command += "-k" + cso.getIndex() + "," + cso.getIndex();
			sort_option = "-k" + cso.getIndex() + "," + cso.getIndex();
			if(cso.getType() == ColumnSortOption.NUMERIC){
				built_command += "n";
				sort_option += "n";
			}
			if(cso.getOrder() == ColumnSortOption.DESCENDING){
				built_command += "r";
				sort_option += "r";
			}
			built_command += " ";
			arr_command[index] = sort_option;
			index++;
		}
		
		// add file name
		try{
			built_command += "\"" + input.getCanonicalPath() + "\" ";
			arr_command[arr_command.length - 1] = input.getCanonicalPath();
		}
		catch(IOException ioe){
			// getting canonical path failed, use normal getPath()
			built_command += "\"" + input.getPath() + "\" ";
			arr_command[arr_command.length - 1] = input.getPath();
		}
		
		return arr_command;
	}
	
	private static Vector parseOptions(String[] args){
		// parse the column sorting options from the command line
		// and add them to the vector
		
		Vector v = new Vector();
		ColumnSortOption cso;
		int type, order;
		for(int i = 0; i < args.length; i++){
			String s = args[i];
			if(s.indexOf("-k") != -1){
				// this argument has  information we need
				String substr = s.substring(2, s.indexOf(','));
				int column_index = Integer.parseInt(substr);
				if(s.indexOf("n") != -1){
					// numerical column data type
					type = ColumnSortOption.NUMERIC;
				} else {
					type = ColumnSortOption.TEXT;
				}
				
				if(s.indexOf("r") != -1){
					// reverse the sorting order
					order = ColumnSortOption.DESCENDING;
				} else {
					order = ColumnSortOption.ASCENDING;
				}
				
				cso = new ColumnSortOption(column_index, order, type);
				v.add(cso);
			}
		}
		
		return v;
	}
	
	private static char parseSep(String args[]){
		// determine the seperating character give on the command line
		char sep;
		for(int i = 0; i < args.length; i++){
			String s = args[i];
			if(s.equals("-t")){
				sep = args[++i].charAt(0);
				return sep;
			}
		}
		return '\n';
	}
	
	public static void main(String[] argv){
		// static invocation to test sorting directly
		char s = parseSep(argv);
		Vector opts = parseOptions(argv);
		File f = new File(argv[argv.length - 1]);
		
		if(s == '\n' || opts == null || opts.size() == 0 || !f.exists()){
			System.out.println("usage:\njava ColumnSorter -t <sep> -k<column>,<column><type><order> . . . <file>");
		} else {
			// seems valid, create object and perform the sort
			System.out.println("creating column sorter object");
			ColumnSorter cs;
			try{
				FileOutputStream fos = new FileOutputStream("out.tmp");
				cs = new ColumnSorter(s, opts, f, fos);
			}
			catch(FileNotFoundException fnfe){
				cs = new ColumnSorter(s, opts, f, System.out);
			}
			System.out.println("starting sort");
			cs.runSort();
			System.out.println("sort finished");
			
		}
		
		
	}
}
