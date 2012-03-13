package org.regenstrief.linkage.gui;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

/**
 * Class appends the given JTextArea with whatever is written to
 * this output stream
 * 
 * Code based on:  http://www.jcreator.com/forums/index.php?showtopic=773
 * @author jegg
 *
 */

public class OutputStreamTextArea extends OutputStream {
	private JTextArea jta;
	
	public OutputStreamTextArea(JTextArea jta){
		super();
		this.jta = jta;
	}
	
	public void write(int b) throws IOException{
		jta.append( String.valueOf( ( char )b ) );
	}
}
