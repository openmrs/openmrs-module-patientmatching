package org.regenstrief.linkage.util;
/*
 * Class sorts character delimited text files with functionality
 * similar to that of the unix 'sort' command.
 *
 * Cicada Dennis, Indiana University, August, 2016
 * Modifies to include parallel sorting, when available
 * and to otherwise use an internal parallel sort if  
 * the jvm has enough virtual memory to do it.
 * 
 * 
 * Currently implemented by running the external sort command.
 * On Windows this is done by using cygwin.  The class determines
 * what OS it is running on and calls the sort command appropriately
 * for the platform.
 * Cicada Dennis comment, it does not seem like the cygwin sort is being
 * used anymore, but rather the win32\\sort is being called, but that would
 * only work on machines where it was installed...
 *
 * 
 * ColumnSortOption objects are stored in the options Vector and lists
 * the columns that need to be sorted and the parameters for the sort.
 * The order in the vector determines the priority/order of the sort.
 */

import java.lang.Runtime;
import java.lang.String;
import java.lang.System;
import java.util.Vector;
import java.util.Iterator;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

// Cicada Dennis Note:
//import org.regenstrief.linkage.util.PSRSSort;
// The PSRSSort object has not been added to the package.
// The intent was to incorporate an internal parallel sorting algorithm
// that could be used if there was no external parallel sort available.
// The other advantage could be if the program is changed to allow sorting
// of data internally, rather than reading from and writing to files.
// That could be considerably faster for larger data sets.

public class ColumnSorter{
    /*
       When on Windows, we need Unix sort functionality. 
       This exists in sort programs from the following packages
           Cygwin: https://sourceforge.net/projects/unxutils
           UnxUtils: https://sourceforge.net/projects/unxutils
           GnuWin32 coreutils: http://www.gnu.org/software/coreutils
       Following are the locations we look for sort binaries on Windows:
           relative to current directory: 
               cygwin \ bin \ sort.exe
               cygwin64 \ bin \ sort.exe
	       UnxUtils \ usr \ local \ wbin \ sort.exe
               GnuWin32 \ bin \ sort.exe
               win32 \ bin \ sort.exe
               win32 \ sort.exe
           absolute paths:
               \ cygwin \ bin \ sort.exe
               \ cygwin64 \ bin \ sort.exe
	       \ UnxUtils \ usr \ local \ wbin \ sort.exe
               \ Program Files (x86) \ GnuWin32 \ bin \ sort.exe
        We don't use the standard search path, because on Windows, that will most likely
        lead to the Windows sort program, which does not have the functionality we need.
    */
    static String[] possibleWindowsLocations = {
            "cygwin\\bin\\sort.exe",
            "cygwin64\\bin\\sort.exe",
            "UnxUtils\\usr\\local\\wbin\\sort.exe",
            "GnuWin32\\bin\\sort.exe",
            "win32\\bin\\sort.exe",
            "win32\\sort.exe",
            "C:\\cygwin\\bin\\sort.exe",
            "C:\\cygwin64\\bin\\sort.exe",
            "C:\\UnxUtils\\usr\\local\\wbin\\sort.exe",
            "C:\\Program Files (x86)\\GnuWin32\\bin\\sort.exe"
    };
    // Regarding the use of two slashes in the file paths.
    // We need two because otherwise the complier is interpreting the slashes as escape codes.
    // The previous code used the following assignment, assuming there would be a sort program
    // in that location.
    // sort_command = "win32\\sort";

    public static boolean sortColumns(
            char separator, 
            Vector<ColumnSortOption> options, 
            File input_file, 
            File output_file){
        // examines the current values for separator, options, and input_file
        // and sorts the data from the input_file, placing the sorted data into the output_file.
        
        boolean success = false;
        int num_processors = Runtime.getRuntime().availableProcessors();
        // No point in sorting parallel if there is only one processor.
        boolean sort_parallel_if_possible = (num_processors > 1);
        boolean can_sort_parallel = false;
        boolean enough_mem_for_internal_sort = false;
        boolean os_is_windows = false;
        long memory_remaining = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory())
                               + Runtime.getRuntime().freeMemory();
        // The command will be created based on sort arguments and platform
        Vector<String> found_sort_programs = new Vector<String>();
        Vector<String> programs_that_sort_parallel = new Vector<String>();
        String[] arr_command = null;
        String copy_command = null;
        String sort_command = null;
        
        // Windows will require different commands than Unix based systems.
        String os_name = System.getProperties().getProperty("os.name");
        if(os_name.indexOf("Windows") != -1){
            os_is_windows = true;
        }
        
        // If there are no sort options, just copy the file.
        if (options.isEmpty()){
            // Create a copy command appropriate for the system.
            if (os_is_windows){
                copy_command = "copy " + input_file.getPath() + " " + output_file.getPath();
            }
            else{
                copy_command = "cp " + input_file.getPath() + " " + output_file.getPath();
            }
            try{
                Process cp_process = new ProcessBuilder(copy_command).start();
                InputStream error_stream = cp_process.getErrorStream();
                cp_process.waitFor();
                //System.out.println("copy exit val: " + cp_process.exitValue());
                success = true;
            }
            catch(IOException ioe){
                System.err.println("ERROR ColumnSorter.sortColumns() - with no blocking columns - "
                    + "\n\tUnable to copy to file: " 
                    + output_file.getPath() + "\n\t" + ioe.getMessage());
            }
            catch(InterruptedException ioe){
                System.err.println("ERROR ColumnSorter.sortColumns() - with no blocking columns - "
                    + "\n\tProcess interrupted before finished: " 
                    + output_file.getPath() + "\n\t" + ioe.getMessage());
            }
            return success;
        }
        // Find/Construct the system sort command. 
        // The sort needs to be able to be sorted independently on different fields.
        // Currently this method only support sort programs which use Unix's sort api.
        if (os_is_windows){
            // Is there a Unix sort program at any of the locations where we hope to find one?
            for (int i=0; i<possibleWindowsLocations.length; i++){
                File possible_sort_program = new File(possibleWindowsLocations[i]);
                if (possible_sort_program.exists())
                    found_sort_programs.add(possibleWindowsLocations[i]);
            }
        }
        else{
            // For now, assume that a Unix type of sort exists on the search path.
            // TODO -- Add a test to see if it actually exists?
            found_sort_programs.add("sort");
        }
        for (int i=0; i<found_sort_programs.size(); i++){
            // Test each found_sort_programs for --parallel sort option.
            // This is done by invoking the sort with the --help option.
            // If the result includes documentation of --parallel, then the
            // sort is added to the list of programs that sort parallel.
            boolean parallel_found = false;
            ProcessBuilder sort_test_process_builder = null;
            Process sort_test_process = null;
            InputStream sort_output_stream = null;
            String sort_program = found_sort_programs.elementAt(i);
            sort_test_process_builder = new ProcessBuilder(sort_program,"--help");
            sort_test_process_builder.redirectErrorStream(true);
            System.out.println("\nChecking for --parallel in " + sort_program);
            try{
                // Call the sort command with --help,
                // System.out.println("Starting sort --help command.");
                sort_test_process = sort_test_process_builder.start();
                sort_output_stream = sort_test_process.getInputStream();
            }
            catch(IOException ioe){
                System.err.println("ERROR ColumnSorter.sortColumns()\n"
                    + "\twhile testing sort program: " + sort_command 
                    + "\n\t" + ioe.getMessage());
            }
            if (sort_output_stream != null){
                // parse the output of the call to sort --help
                // to see if the sort supports the --parallel option
                InputStreamReader reader = new InputStreamReader(sort_output_stream);
                BufferedReader sort_help_output_reader = new BufferedReader(reader);
                String help_message_line = null;
                // System.out.println("Message from sort --help command:\n\t" + sort_program);
                try{
                    while ((help_message_line = sort_help_output_reader.readLine()) != null) {
                        // We go ahead and read all of the output from the call, so we can get
                        // the exit value of the process after it is done running.
                        // System.out.println(help_message_line);
                        if (help_message_line.contains("--parallel")){
                            programs_that_sort_parallel.add(sort_program);
                            parallel_found = true;
                        }
                    }
                }
                catch(IOException ioe){
                    System.err.println("WARNING ColumnSorter.sortColumns() - "
                        + "\n\tUnable to read output from call to sort command:\n\t\t" 
                        + sort_program + " --help\n" + ioe.getMessage());
                }
            }
            try{
                sort_test_process.waitFor();
            }
            catch(InterruptedException ioe){
                System.err.println("ERROR ColumnSorter.sortColumns()\n"
                    + "\twhile testing sort program: " + sort_command 
                    + "\n\tProcess interrupted before finished: " 
                    + "\n\t" + ioe.getMessage());
            }
            if (parallel_found == true){
                System.out.println("This sort can sort parallel.");
            }
            else{
                System.out.println("This sort can not sort parallel.");
            }
            // System.out.println("sort --help exit val: " + sort_test_process.exitValue());
            // At start of each loop sort_output_stream should be null.
            sort_output_stream = null;
        }
        // The above loop allocates quite a few objects that now could be freed.
        System.gc();
        if (memory_remaining > (2.2 * input_file.length())){
            // I use 2.2 as a multiple in order to give some head room for the sorting process and
            // for other objects which may be created. Not sure what would be the best value to use,
            // but need to make it big enough so that we don't think there is enough memory, but then
            // the process runs out of memory.
            // TODO - This flag is not really needed at the moment, since we have not implemented
            // the internal sort. All sorting is done through external system commands.
            enough_mem_for_internal_sort = true;
        }
        System.out.println("\nThe programs that can sort parallel are:");
        for (int i=0; i<programs_that_sort_parallel.size(); i++){
            System.out.println(programs_that_sort_parallel.elementAt(i));
        }
        System.out.println("");
        if (programs_that_sort_parallel.size() > 0){
            // TODO - If more than one has --parallel option, how do we choose one?
            // The following just gets the first one in the list.
            sort_command = programs_that_sort_parallel.elementAt(0);
            can_sort_parallel = true;
        }
        else if (found_sort_programs.size() > 0){
            // TODO - If there are more than one sort programs, how do we choose one?
            // For now, just get the first found sort command.
            sort_command = found_sort_programs.elementAt(0);
        }
        else{ // (sort_command == null)
            // There is no sort command that was found.
            System.err.println("WARNING: No sort command with Unix API was found in the typical locations.");
        }
        System.out.println("The sort command is: " + sort_command + "\nEnough mem for internal sort is: "
            + enough_mem_for_internal_sort + "\ncan_sort_parallel is: " + can_sort_parallel);
        if (sort_command != null){ // && ((can_sort_parallel || !enough_mem_for_internal_sort)){
            // TODO - The other if () conditions (commented out) could be added, 
            // once we have an internal sort implemented.
            
            // Use the external sorting program that was found.
            int num_command_args = 0;
            int num_cores = 1;
            int avail_cores = Runtime.getRuntime().availableProcessors();
            if (os_is_windows){
                num_command_args = 1 + 1 + 1 + options.size() + 1;
            }
            else{
                num_command_args = 1 + 1 + options.size() + 1;
            }
            if (can_sort_parallel && (avail_cores >1)){
                num_command_args += 1;
            }
            arr_command = new String[num_command_args];
            arr_command[0] = sort_command;
            // Add the field/column seperator option.
            arr_command[1] = "-t" + Character.toString(separator);
            // Add the parallel option, if it is available.
            int index = 2;
	    System.out.println("Available cores for sorting: " + avail_cores + "\n");
            if (can_sort_parallel && (avail_cores > 1)){
                if (avail_cores>8)
                    num_cores = 8;
                else
                    num_cores = avail_cores;
                arr_command[index] = "--parallel=" + num_cores;
                index++;
            }
            // If on windows, specify the tmp directory for sort to use.
            if(os_name.indexOf("Windows") != -1){
                // The normal places that sort uses (/tmp or $TMPDIR) for temporaryy files
                // are not usually present in Windows.
                // Windows uses the directory stored in environment variable TEMP.
                File tmp_file = null;
                String tmp_dirname = null;
                
                // Try to use the value of TEMP.
                try{
                    tmp_dirname = System.getenv("TEMP");
                    if (tmp_dirname != null){
                        tmp_file = new File(tmp_dirname);
                    }
                }
                catch (SecurityException security_err){
	            System.err.println("WARNING - ColumnSorter: Could not get the value of %TEMP%.");
                }
                if (tmp_file == null){
                    // The value in TEMP did not work or a value for it did not exist.
                    // Create a tmp directory in the current working directory,
                    // if the file does not already exist.
                    tmp_dirname = "tmp";
                    tmp_file = new File(tmp_dirname);
                    try{
                        tmp_file.mkdir();
                    }
                    catch (SecurityException security_err){
                        System.err.println("Could not create temporary directory for the sort program.");
                        security_err.printStackTrace();
                    }
                }
                if ((tmp_file != null) && tmp_file.exists() && tmp_file.isDirectory()){
                    arr_command[index] = "-T" + tmp_dirname;
                    index++;
                }
                else {
                    // We could not find or create a temporary directory.
                    System.err.println("Could not find or create temporary directory (" +
                        tmp_dirname + ") for the sort program.");
                    num_command_args -= 1;
                }
            }
            // Loop through the options and append to the end of built_command.
            Iterator it = options.iterator();
            ColumnSortOption cso;
            String sort_option;
            while(it.hasNext()){
                cso = (ColumnSortOption)it.next();
                sort_option = "-k" + cso.getIndex() + "," + cso.getIndex();
                if(cso.getType() == ColumnSortOption.NUMERIC){
                    sort_option += "n";
                }  
                if(cso.getOrder() == ColumnSortOption.DESCENDING){
                    sort_option += "r";
                }
                arr_command[index] = sort_option;
                index++;
            }
            // Add the input file name.
            try{
                arr_command[num_command_args - 1] = input_file.getCanonicalPath();
            }
            catch(IOException ioe){
                // Getting canonical path failed, use normal getPath().
                arr_command[num_command_args - 1] = input_file.getPath();
            }
            // System.out.println("command_args are:");
            // for (int i=0; i<num_command_args; i++){
            //     System.out.println("arg " + i + ": " + arr_command[i]);
            // }
            try{
                FileOutputStream sorted_output_stream = new FileOutputStream(output_file);
                // Create a CmdLauncher object and give it the command to sort the file.
                CmdLauncher cl = new CmdLauncher(arr_command, sorted_output_stream);
                cl.runCommand();
                // TODO - Can the return status from running the command be checked,
                // rather than assuming that the command ran successfully?
                sorted_output_stream.close();
                success = true;
            }
            catch(FileNotFoundException fnfe){
                // If can't open the output stream at this stage, return signaling failure
                // since the later steps make no sense without a file from this step.
                System.err.println("ERROR: ColumnSorter.sortColumns() - "
                        + "The output file " + output_file.getPath()
                        + "was not able to be opened for writing.");
            } 
            catch (IOException e) {
                System.err.println("ERROR: ColumnSorter.sortColumns() - " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (!success){
            if (enough_mem_for_internal_sort){
               // Sort internally.
               System.err.println("ERROR: ColumnSorter.sortColumns() - \n"
                   + "\tEither the external sort did not succeed,"
                   + " or there is no external sort command available.\n"
                   + "\tWe would try to sort internally, but that is not implemented yet.");
               // TODO - Add internal sort method.
	    }
            else{
                // We cannot perform the sort.
                System.err.println("ERROR: ColumnSorter.sortColumns() - \n"
                    + "\tEither the external sort did not succeed,\n"
		    + "\tor there is no external sort command available and "
                    + "not enough memory is available to sort within the JVM.");
            }
        }
        return success;
    }
	
	private static Vector<ColumnSortOption> parseOptions(String[] args){
		// parse the column sorting options from the command line
		// and add them to the vector
		
		Vector<ColumnSortOption> v = new Vector<ColumnSortOption>();
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
		Vector<ColumnSortOption> opts = parseOptions(argv);
		
		if(s == '\n' || opts == null || opts.size() == 0){
			System.out.println("usage:\njava ColumnSorter -t <sep> -k<column>,<column><type><order> . . . <file>");
		} else {
		        File input_file = new File(argv[argv.length - 1]);

			// seems valid, create object and perform the sort
			System.out.println("creating column sorter object");
			System.out.println("starting sort");
			// ColumnSorter cs;
			boolean success = false;
			// FileOutputStream fos = new FileOutputStream("out.tmp");
			// cs = new ColumnSorter(s, opts, input_file, fos);
			File output_file = new File("out.txt");
                        success = ColumnSorter.sortColumns(s, opts, input_file, output_file);
			// cs = new ColumnSorter(s, opts, input_file, System.out);
			// success = ColumnSorter.sortColumns(s, opts, input_file, null);
			// cs.runSort();
		        System.out.println("sort finished with success value: " + success);
			
		}
		
		
	}
}
