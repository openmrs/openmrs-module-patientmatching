package org.regenstrief.linkage.depreciated;
import java.io.*;
import java.text.*;

import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.StringMatch;

/*
 * Class implements the score pairs functionality of the 
 * record match program written in C.
 */

public class ScorePairs {
	
	File pair_file, meta, output_file;
	int[] blocking_indexes;
	BufferedReader pairs_reader;
	BufferedWriter out_file, err_file;
	MatchingConfig matching_config;
	LinkDataSource lds;
	
	public static final String DEFAULT_OUTPUT_FILE = "scored_pairs";
	public static final String EXT = ".tmp";
	public static final String ERR_EXT = ".err";
	public static final String DECIMAL_OUTPUT_FORMAT = "0.00000";
	
	final static double META_ONE = 0.99999;
	final static double META_ZERO = 0.00001;
	
	public ScorePairs(File pair_file, MatchingConfig mc, LinkDataSource lds){
		this.pair_file = pair_file;
		this.matching_config = mc;
		this.lds = lds;
	}
	
	public void createScoreFile() throws IOException{
		String[] pairs_line;
		int line_count;
		double score;
		
		DecimalFormat df = new DecimalFormat(DECIMAL_OUTPUT_FORMAT);
		
		int[] included_columns = lds.getIncludeIndexesOfColumnNames(matching_config.getIncludedColumnsNames());
		int used_len = included_columns.length;
		int length = matching_config.getRowNames().length;
		
		String of, errf;
		if(meta == null){
			of = DEFAULT_OUTPUT_FILE + EXT;
			errf = DEFAULT_OUTPUT_FILE + ERR_EXT;
		} else {
			of = meta.getPath() + EXT;
			errf = meta.getPath() + ERR_EXT;
		}
		pairs_reader = new BufferedReader(new FileReader(pair_file));
		output_file = new File(of);
		out_file = new BufferedWriter(new FileWriter(output_file));
		err_file = new BufferedWriter(new FileWriter(errf));
		
		// initialize m and u lists
		double m[] = new double[used_len];
		double u[] = new double[used_len];
		for(int i = 0; i < used_len; i++){
			int incl_col = included_columns[i];
			double match_rate = matching_config.getAgreementValue(incl_col);
			double umatch_rate = matching_config.getNonAgreementValue(incl_col);
			if(match_rate > META_ONE || match_rate < META_ZERO){
				System.out.println("ERROR: m-value is out of range");
				System.exit(1);
			}
			if(umatch_rate > META_ONE || match_rate < META_ZERO){
				System.out.println("ERROR: u-value is out of range");
				System.exit(1);
			}
			// m[k] = log(mtx[used[k]].m  /    mtx[used[k]].u) / log(2);
			// u[k] = log( (1 - mtx[used[k]].m) / (1 - mtx[used[k]].u) ) / log(2);
			m[i] = Math.log(match_rate / umatch_rate) / Math.log(2);
			u[i] = Math.log( (1 - match_rate) / (1 - umatch_rate) ) / Math.log(2);

			// if scale weight flag is set, modify and store values
		}

		line_count = 0;
		while((pairs_line = pairFileReadLine()) != null){
			line_count++;
			score = 0;

			for(int i = 0; i < used_len; i++){
				int used_index = included_columns[i];
				String str1 = pairs_line[used_index];
				String str2 = pairs_line[used_index + length];

				// need to get the matching algorith used in mc row used_index
				// and use that method to compare the two strings
				int alg = matching_config.getAlgorithm(used_index);
				boolean match = false;
				if(alg == MatchingConfig.EXACT_MATCH){
					match = StringMatch.exactMatch(str1, str2);
				} else if(alg == MatchingConfig.JWC){
					match = StringMatch.JWCMatch(str1, str2);
				} else if(alg == MatchingConfig.LCS){
					match = StringMatch.LCSMatch(str1, str2);
				} else if(alg == MatchingConfig.LEV){
					match = StringMatch.LEVMatch(str1, str2);
				}

				if(match){
					if(false){
						// block for when scale weight is implemented
						// original C code:
						//	score += gdbm_scale(dbf[k],data[used[k]+length],mtx[used[k]].uniq,tot_recs);
					}
					score += m[i];
				} else {
					score += u[i];
				}

			}
			// write the score the output file with line
			out_file.write(df.format(score) + "|");
			for(int j = 0; j < pairs_line.length - 1; j++){
				out_file.write(pairs_line[j] + "|");
			}
			out_file.write(pairs_line[pairs_line.length - 1] + "\n");

		}
		// close readers and writers
		pairs_reader.close();
		out_file.close();
		err_file.close();
	
		
		
	}
	
	private String[] pairFileReadLine() throws IOException{
		String str = pairs_reader.readLine();
		if(str == null){
			return null;
		}
		String[] ret = str.split("\\|", -1);
		return ret;
	}
	
	/**
	 * @param args
	 */
	/*
	 * Method created to mimic the command line behaviour of the C program
	 * to use in porting and testing.
	 * 
	 * Command line no longer implemented when code was re-factored into
	 * a new Eclipsep roject
	 */
	public static void main(String[] args) {
		if(args.length != 2){
			// match error output with C version
			System.out.println();
			System.out.println("score_pairs ERROR:");
			System.out.println("Not enough arguments. Please enter:");
			System.out.println("\t1) RP file");
			System.out.println("\t2) MTX file");
			System.exit(0);
		}
		
	}
	
	/*
	 * Method used when the scale weight flag is set, as the total number of lines
	 * in the pairs file needs to be known before calculationg the score
	 */
	private static int calculateLineCount(File f) throws IOException{
		
		return 1;
	}
	
	public File getOutputFile(){
		return output_file;
	}

}
