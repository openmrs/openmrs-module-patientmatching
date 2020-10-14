package org.regenstrief.linkage.entropy;

import java.io.*;
import java.util.*;

public class FileEntropyProvider extends ColumnEntropyProvider {
	
	File f;
	
	RandomAccessFile raf;
	
	Random r;
	
	BufferedReader in;
	
	int col_index;
	
	char delim;
	
	boolean has_next;
	
	String next_element;
	
	EntropyProviderException epe;
	
	public FileEntropyProvider(File f, char delim, int index) {
		super();
		this.f = f;
		this.delim = delim;
		this.col_index = index;
		this.delim = delim;
		
		r = new Random();
		
		try {
			raf = new RandomAccessFile(f, "r");
			in = new BufferedReader(new FileReader(f));
		}
		catch (FileNotFoundException fnfe) {
			raf = null;
		}
		setNext();
	}
	
	public List<String> getRandomSet(int size) {
		List<String> set = new LinkedList<String>();
		while (set.size() < size) {
			try {
				String line = getRandomLine();
				String[] split_line;
				if (delim == '|') {
					split_line = line.split("\\|", -1);
				} else {
					split_line = line.split(Character.toString(delim), -1);
				}
				set.add(split_line[col_index]);
			}
			catch (IOException ioe) {
				break;
			}
		}
		
		return set;
	}
	
	public boolean hasNext() {
		return has_next;
	}
	
	/*
	 * method reads the next line from the file and sets the element that will be returned
	 * at the next nextElement() call.  it also sets the flag on whether the input stream
	 * is finished
	 */
	private void setNext() {
		try {
			String line = in.readLine();
			if (line == null) {
				next_element = null;
				has_next = false;
			} else {
				String[] split_line;
				if (delim == '|') {
					split_line = line.split("\\|", -1);
				} else {
					split_line = line.split(Character.toString(delim), -1);
				}
				
				next_element = split_line[col_index];
				has_next = true;
			}
			
		}
		catch (IOException ioe) {
			epe = new EntropyProviderException("IOException error reading file");
		}
		catch (ArrayIndexOutOfBoundsException aioobe) {
			epe = new EntropyProviderException("Array out of bounds error in file");
		}
	}
	
	public String nextElement() throws EntropyProviderException {
		if (!has_next) {
			throw new EntropyProviderException("Error trying to read more elements from impty provider");
		} else {
			if (epe != null) {
				throw epe;
			} else {
				String element = next_element;
				setNext();
				return element;
			}
		}
	}
	
	private String getRandomLine() throws IOException {
		long file_size = raf.length();
		long offset = (long) (r.nextDouble() * file_size);
		raf.seek(offset);
		int c;
		
		while ((c = (int) raf.readByte()) != '\n') {
			// in the middle of the line
			
		}
		String line = new String();
		while ((c = (int) raf.readByte()) != '\n') {
			char ch = (char) c;
			line = line + Character.toString(ch);
			
		}
		return line;
	}
	
	public boolean supportsEstimate() {
		return true;
	}
}
