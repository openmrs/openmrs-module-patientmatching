package org.regenstrief.linkage.util;

import java.io.*;

public class ComparisonException extends Exception {
	
	BufferedReader br;
	
	public ComparisonException() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public ComparisonException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
	public ComparisonException(String message, BufferedReader br) {
		super(message);
		this.br = br;
	}
	
	public ComparisonException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	
	public ComparisonException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * Returns the file that cause the comparison exception to occur
	 */
	public BufferedReader getReader() {
		return br;
	}
}
