package org.regenstrief.linkage.util;
/*
 * Class takes a string command and executes it in a new process.
 * The output stream from the process is an 
 * instance variable and might be stdout or a stream
 * to a file in the program that has this object
 * (to do output redirection to a file)
 * 
 * It starts a new thread to read the err stream of the
 * process.  This output is saved and can be read
 * if the owning object requests it.
 * 
 * WIN_PREFIX was set to "cmd.exe /c" before parsing the command to an
 * array was used.  Parsing the command to a valid array on Windows
 * removes the need for cmd.exe to interpret quotation marks on a
 * command in a string form
 */

import java.io.*;
import java.util.*;

public class CmdLauncher implements Runnable{
	public static final String WIN_PREFIX = "cmd.exe /c ";
	public static final String UNIX_PREFIX = "/bin/sh ";
	public static final String QUOTED_CMD_DELIM = "\"";
	
	String err;
	String cmd[];
	Process p;
	InputStream process_output, process_err;
	BufferedReader child_output, child_err;
	PrintStream out;
	
	int lines_read;
	
	public CmdLauncher(String c){
		// if no printstream is given, use System.out
		//cmd = cmdToArray(c);
		cmd = cmdToArray(c);
		out = System.out;
		lines_read = 0;
	}
	
	public CmdLauncher(String c, OutputStream os){
		//cmd = cmdToArray(c);
		cmd = cmdToArray(c);
		out = new PrintStream(os);
		lines_read = 0;
	}
	
	public CmdLauncher(String[] c){
		cmd = c;
		out = System.out;
		lines_read = 0;
	}
	
	public CmdLauncher(String[]c, OutputStream os){
		cmd = c;
		out = new PrintStream(os);
		lines_read = 0;
	}
	
	public void runCommand(){
		// starts a process for the command in cmd string variable
		String line;
		System.out.println("CmdLauncher: running " + cmdToString(cmd));
		
		try{
			//File process_directory = new File(".");
			p = Runtime.getRuntime().exec(cmd, null, null);
			process_output = p.getInputStream();
			process_err = p.getErrorStream();
			child_output = new BufferedReader(new InputStreamReader(process_output));
			child_err = new BufferedReader(new InputStreamReader(process_err));
			// start a new thread to read the err stream
			new Thread(this).start();
			while( (line = child_output.readLine()) != null){
				lines_read++;
				out.println(line);
			}
			p.waitFor();
			//System.out.println("exit val: " + p.exitValue());
		}
		catch(IOException ioe){
			System.out.println("unable to run script: " + ioe.getMessage());
		}
		catch(InterruptedException ie){
			System.out.println("process interrupted before finished");
			
		}
		
	}
	
	public String getErr(){
		// returns saved output from the process's error stream
		return err;
	}
	
	public int getLinesRead(){
		return lines_read;
	}
	
	public void run(){
		// reads from the err stream of the process so it does not hang
		String line;
		try{
			while((line = child_err.readLine()) != null){
				err += line;
				System.err.println(line);
			}
		}
		catch(IOException ioe){
			System.out.println("error reading err stream from child process");
		}
		
	}
	
	public static void main(String[] argv){
		// static invocation to run a command
		String prefix = getPrefix();
		/*
		String cmd = prefix;
		// build output stream string command
		for(int i = 0; i < argv.length; i++){
			cmd = cmd + argv[i] + " ";
		}*/
		String cmd;
		cmd = prefix + "win32\\score_pairs.exe";// \"C:\\Documents and Settings\\jegg\\My Documents\\a save test\\EM test.metasort1.sort\" \"C:\\Documents and Settings\\jegg\\My Documents\\a save test\\EM test.metasort2.sort\" \"C:\\Documents and Settings\\jegg\\My Documents\\a save test\\EM test.meta\"";
		//cmd = prefix + "dir";
		
		System.out.println("static invocation built command of: " + cmd);
		CmdLauncher cl = new CmdLauncher(cmd);
		cl.runCommand();
	}
	
	public static String cmdToString(String[] cmd){
		// reassemble the elements in the array to a signle string
		String ret = new String();
		for(int i = 0; i < cmd.length; i++){
			ret += cmd[i] + " ";
		}
		return ret;
	}
	
	public static String[] cmdToArray(String cmd){
		// split the command string into a suitable array of strings that will be executed correctly
		// need to split the command where spaces occur, but not if space is within quotes
		// also, remove quotes
		boolean in_quoted_string = false;
		Vector parsed_strings = new Vector();
		StringTokenizer stok = new StringTokenizer(cmd, " \"", true);
		String quoted_string  = new String();
		String token;
		
		while(stok.hasMoreTokens()){
			token = stok.nextToken();
			if(token.compareTo(QUOTED_CMD_DELIM) == 0){
				// switch whether the tokens are part of the same string
				if(!in_quoted_string){
					// not yet between quotes, but will be.  allocate new string
					quoted_string = new String();
				} else {
					// at the end of the quotes, add quoted_string to parsed_strings Vector
					// remove space at the end of string
					if(quoted_string.charAt(quoted_string.length() - 1) == ' '){
						quoted_string = quoted_string.substring(0, quoted_string.length() - 1);
					}
					parsed_strings.add(quoted_string);
				}
				in_quoted_string = !in_quoted_string;
			} else if(token.compareTo(" ") != 0){
				// is a word token that is either alone or inside quotes
				if(in_quoted_string){
					quoted_string += token + " ";
				} else {
					parsed_strings.add(token);
				}
			}
		}
		
		// the vector parsed_strings should now have correctly split strings
		// convert to a String[] and return
		String[] ret = new String[parsed_strings.size()];
		for(int i = 0; i < parsed_strings.size(); i++){
			ret[i] = (String)parsed_strings.elementAt(i);
		}
		
		return ret;
	}
	
	public static String getPrefix(){
		// return the command to use, depending on OS
		String p;
		String os_name = System.getProperties().getProperty("os.name");
		if(os_name.indexOf("Windows") == -1){
			// "Windows" not in OS name, assuming to be unix where shell scripting is supported
			p = UNIX_PREFIX;
		} else {
			// Windows-suitable command
			p = WIN_PREFIX;
		}
		
		return p;
		
	}
	
}
