package org.regenstrief.linkage.matchresult;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;

/**
 * Class writes each incoming MatchResult to the Writer in the format of 
 * pipe delimited fields with the score printed first followed by the values of
 * the demographics in the two Records.
 * 
 * @author jegg
 *
 */

public class FlatFileMatchResultWriter extends MatchResultWriter {
	
	public FlatFileMatchResultWriter(Writer w){
		super(w);
	}
	
	public void acceptMatchResult(MatchResult mr) {
		// write this MatchResult's information to output Writer
		String s = new String();
		s += mr.getScore();
		Record r1 = mr.getRecord1();
		Record r2 = mr.getRecord2();
		Enumeration<String> demographics = r1.getDemographics().elements();
		while(demographics.hasMoreElements()){
			String demographic = demographics.nextElement();
			s += "|" + r1.getDemographic(demographic) + "|" + r2.getDemographic(demographic);
		}
		s += "\n";
		
		try{
			output.write(s);
		}
		catch(IOException ioe){
			
		}
	}

}
