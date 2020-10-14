package org.regenstrief.linkage.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.regenstrief.linkage.util.LongestCommonSubString;

import uk.ac.shef.wit.simmetrics.similaritymetrics.BlockDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.ChapmanLengthDeviation;
import uk.ac.shef.wit.simmetrics.similaritymetrics.ChapmanMatchingSoundex;
import uk.ac.shef.wit.simmetrics.similaritymetrics.ChapmanMeanLength;
import uk.ac.shef.wit.simmetrics.similaritymetrics.ChapmanOrderedNameCompoundSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.CosineSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.DiceSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.EuclideanDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Jaro;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.MatchingCoefficient;
import uk.ac.shef.wit.simmetrics.similaritymetrics.MongeElkan;
import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;
import uk.ac.shef.wit.simmetrics.similaritymetrics.OverlapCoefficient;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWatermanGotoh;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWatermanGotohWindowedAffine;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Soundex;

/**
 * Class created to test LCS, JWC, and LEV string comparators for accuracy and how they handle
 * different non alpha numeric characters
 * 
 * @author jegg
 */

public class ComparatorTesting {
	
	static String[] random_alpha = { "a", "b", "c" };
	
	static String[] random_alpha_numeric = { "a", "b", "1", "2", "3" };
	
	static String[] random_all = { "a", "b", "1", "2", "3", " ", "-", ".", "_", "/", "\\" };
	
	static Random rand;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		rand = new Random();
		/*
		int true_count = 0;
		for(int i = 0; true_count < 40; i++){
			String data1 = getRandomAllString(7);
			String data2 = getRandomAllString(7);
			
			int algorithm;
			if(true_count < 10){
				algorithm = MatchingConfig.EXACT_MATCH;
			} else if(true_count >= 10 && true_count < 20){
				algorithm = MatchingConfig.JWC;
			} else if(true_count >= 20 && true_count < 30){
				algorithm = MatchingConfig.LCS;
			} else {
				algorithm = MatchingConfig.LEV;
			}
			
			boolean match = false;
			String alg = "";
			double similarity = 0;
			
			switch(algorithm){
			case(MatchingConfig.EXACT_MATCH):
				match = StringMatch.exactMatch(data1, data2);
				alg = "exact match";
				similarity = exactMatch(data1, data2);
			break;
			case(MatchingConfig.JWC):
				match = StringMatch.JWCMatch(data1, data2);
				alg = "jwc";
				similarity = JWCMatch(data1, data2);
			break;
			case(MatchingConfig.LCS):
				match = StringMatch.LCSMatch(data1, data2);
				alg = "lcs";
				similarity = LCSMatch(data1, data2);
			break;
			case(MatchingConfig.LEV):
				match = StringMatch.LEVMatch(data1, data2);
				alg = "lev";
				similarity = LEVMatch(data1, data2);
			break;
			}
			
			if(match){
				true_count++;
				System.out.println(data1 + "|" + data2 + "|" + alg + "|" + match + "|" + similarity);
				if(true_count % 10 == 0){
					System.out.println();
				}
			}
			//System.out.println(i + "|" + data1 + "|" + data2 + "|" + alg + "|" + match + "|" + similarity);
			
		}
		
		*/
		
		File names = new File("C:\\Documents and Settings\\jegg\\Desktop\\census common names\\name_combinations.txt");
		File output = new File(
		        "C:\\Documents and Settings\\jegg\\Desktop\\census common names\\reduced_compare_results.txt");
		try {
			System.out.println("started at:\t" + new Date());
			writeComparisonFromFile(names, output);
			System.out.println("finished at:\t" + new Date());
		}
		catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}
	}
	
	public static void writeComparisonFromFile(File in, File out) throws IOException {
		BufferedReader fin = new BufferedReader(new FileReader(in));
		BufferedWriter fout = new BufferedWriter(new FileWriter(out));
		String line;
		
		// create list of StringMetrics
		List<InterfaceStringMetric> comparators = new ArrayList<InterfaceStringMetric>();
		comparators.add(new BlockDistance());
		comparators.add(new ChapmanLengthDeviation());
		comparators.add(new ChapmanMatchingSoundex());
		comparators.add(new ChapmanMeanLength());
		comparators.add(new ChapmanOrderedNameCompoundSimilarity());
		comparators.add(new CosineSimilarity());
		comparators.add(new DiceSimilarity());
		comparators.add(new EuclideanDistance());
		comparators.add(new JaccardSimilarity());
		comparators.add(new Jaro());
		comparators.add(new JaroWinkler());
		comparators.add(new Levenshtein());
		comparators.add(new MatchingCoefficient());
		comparators.add(new MongeElkan());
		comparators.add(new NeedlemanWunch());
		comparators.add(new OverlapCoefficient());
		comparators.add(new QGramsDistance());
		comparators.add(new SmithWaterman());
		comparators.add(new SmithWatermanGotoh());
		comparators.add(new SmithWatermanGotohWindowedAffine());
		comparators.add(new Soundex());
		//comparators.add(new TagLink());
		//comparators.add(new TagLinkToken());
		
		Iterator<InterfaceStringMetric> it = comparators.iterator();
		fout.write("name1,name2,lcs,lcs2");
		while (it.hasNext()) {
			InterfaceStringMetric ism = it.next();
			String desc = ism.getShortDescriptionString();
			fout.write("," + desc);
		}
		fout.write("\n");
		
		while ((line = fin.readLine()) != null) {
			String[] strings = line.split(" ");
			String str1 = strings[0];
			String str2 = strings[1];
			
			boolean match = false;
			float lcs = LCSMatch(str1, str2);
			float lcs2 = LCS2Match(str1, str2);
			//if(lcs > 0.8 || lcs2 > 0.8){
			//	match = true;
			//}
			String out_line = str1 + "," + str2 + "," + lcs + "," + lcs2;
			
			Iterator<InterfaceStringMetric> it2 = comparators.iterator();
			while (it2.hasNext()) {
				InterfaceStringMetric ism = it2.next();
				float sim = ism.getSimilarity(str1, str2);
				if (LEVMatch(str1, str2) > 0.5) {
					match = true;
				}
				out_line += "," + sim;
			}
			
			if (match) {
				fout.write(out_line + "\n");
			}
		}
		
		fout.flush();
		fout.close();
	}
	
	public static int getRandomInt(int min, int max) {
		return rand.nextInt(max - min) + min;
	}
	
	public static String getRandomString(int length) {
		String ret = new String();
		for (int i = 0; i < length; i++) {
			ret += random_alpha[getRandomInt(0, random_alpha.length)];
		}
		return ret;
	}
	
	public static String getRandomAlphaString(int length) {
		String ret = new String();
		for (int i = 0; i < length; i++) {
			ret += random_alpha_numeric[getRandomInt(0, random_alpha_numeric.length)];
		}
		return ret;
	}
	
	public static String getRandomAllString(int length) {
		String ret = new String();
		for (int i = 0; i < length; i++) {
			ret += random_all[getRandomInt(0, random_all.length)];
		}
		return ret;
	}
	
	public static float exactMatch(String str1, String str2) {
		if (str1.equals(str2) && str1.length() > 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public static float JWCMatch(String str1, String str2) {
		JaroWinkler jwc = new JaroWinkler();
		float thresh = jwc.getSimilarity(str1, str2);
		return thresh;
	}
	
	public static float LCSMatch(String str1, String str2) {
		float thresh = LongestCommonSubString.getSimilarity(str1, str2);
		return thresh;
	}
	
	public static float LCS2Match(String str1, String str2) {
		float thresh = LongestCommonSubString.getSimilarity2(str1, str2);
		return thresh;
	}
	
	public static float LEVMatch(String str1, String str2) {
		Levenshtein lev = new Levenshtein();
		float thresh = lev.getSimilarity(str1, str2);
		return thresh;
	}
}
