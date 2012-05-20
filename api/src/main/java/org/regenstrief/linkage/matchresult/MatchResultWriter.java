package org.regenstrief.linkage.matchresult;

import java.io.IOException;
import java.io.Writer;

public abstract class MatchResultWriter implements MatchResultHandler {
	
	protected Writer output;
	protected boolean open;
	
	public MatchResultWriter(Writer w){
		output = w;
		open = true;
	}
	
	public void close() {
		open = false;
		try{
			output.flush();
			output.close();
		}
		catch(IOException ioe){
			
		}
	}
	
	public boolean isOpen(){
		return open;
	}
}
