package org.regenstrief.linkage.util;
/*
 * Class created to implement longest common substring algorithm
 * for use in the EM program.
 */

public class LongestCommonSubString {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 2){
			System.out.println("usage: java LongestCommonSubString <str1> <str2>");
			System.exit(0);
		}
		
		String str2 = args[0];
		String str1 = args[1];
		
		LongestCommonSubString lcss = new LongestCommonSubString();
		float s = 0;
		
		//int iterations = 1000000;
		//System.out.println("starting " + iterations + " iterations at " + new java.util.Date());
		//for(int i = 0; i < iterations; i++){
			s = lcss.getSimilarity(str1, str2);
		//}
		//System.out.println("finished at " + new java.util.Date());
		
		System.out.println(s);
		
	}
	
	public LongestCommonSubString(){
		// instantiate nothing
	}
	
	/*
	 * Not a static method to mirror the method calls for the SimMetrics library.
	 * Does not implement the longest common sub-string algorithm Regenstrief uses.
	 * Instead implements algorithm by Lonnie Blevins.
	 * 
	 * Algorithm from:
	 * http://www.ics.uci.edu/~eppstein/161/960229.html
	 */
	float getLongestCommonSubSequence(String str1, String str2){
		char[] ch1 = str1.toCharArray();
		char[] ch2 = str2.toCharArray();
		int m = str1.length();
		int n = str2.length();
		
		// handle zero length strings
		if(m == 0 && n == 0){
			return 1;
		} else if(m == 0 || n == 0){
			return 0;
		}
		
		int[][] L = new int[m + 1][n + 1];
		for(int i = m; i >= 0; i--){
			for(int j = n; j >= 0; j--){
				if(i == m || j == n){
					L[i][j] = 0;
				} else if(ch1[i] == ch2[j]){
					L[i][j] = 1 + L[i + 1][j + 1];
				} else {
					L[i][j] = Math.max(L[i + 1][j], L[i][j + 1]);
				}
			}
		}
		
		int lcss_length =  L[0][0];
		if(m < n){
			return (float)lcss_length / m;
		} else {
			return (float)lcss_length / n;
		}
	}
	
	/*
	 * Not a static method to mirror the method calls for the SimMetrics library.
	 * Implements algorithm by Lonnie Blevins.
	 * 
	 */
	public float getSimilarity(String str1, String str2){
		String min, max;
		
		// set strings to lowercase for comparison purposes
		if(str1.length() <= str2.length()){
			min = str1.toLowerCase();
			max = str2.toLowerCase();
		} else {
			min = str2.toLowerCase();
			max = str1.toLowerCase();
		}
		
		int min_length;
		if(min.length() >= 6){
			min_length = 3;
		} else {
			min_length = 0;
		}
		
		int lcss_length = getLCS(min, max, min_length);
		
		if(lcss_length == 0){
			return 0;
		} else {
			return (float)lcss_length / min.length();
		}
	}
	
	public float getSimilarity2(String str1, String str2){
		String min, max;
		
		// set strings to lowercase for comparison purposes
		if(str1.length() <= str2.length()){
			min = str1.toLowerCase();
			max = str2.toLowerCase();
		} else {
			min = str2.toLowerCase();
			max = str1.toLowerCase();
		}
		
		int min_length;
		if(min.length() >= 6){
			min_length = 3;
		} else {
			min_length = 2;
		}
		
		int lcss_length = getLCS(min, max, min_length);
		
		if(lcss_length == 0){
			return 0;
		} else {
			return (float)lcss_length / min.length();
		}
	}
	
	/*
	 * An iterative version of the below method, written in an attempt to make
	 * it run faster.  It did not seem to improve when tested.
	 */
	private int getLCS2(String pattern, String str, int limit){
		boolean finished = true;
		int total = 0;
		int loops = 0;
		
		do{
			loops++;
			finished = true;
			for(int start = 0; start < pattern.length(); start++){
				for(int end = pattern.length(); end > start; end--){
					String s = pattern.substring(start, end);
					if(str.indexOf(s) != -1){
						// longest, first occurance of string this length
						// check ot see if it is at least as long as limit
						if(s.length() >= limit){
							//pattern = pattern.replaceFirst(s, "");
							pattern = replaceFirst(pattern, s, "");
							//str = str.replaceFirst(s, "");
							str = replaceFirst(str, s, "");
							total += s.length();
							finished = false;
							break;
						}
					}
					
				}
				
			}
			
		}while(!finished);
		//System.out.println("times executed for loops: " + loops);
		return total;
	}
	
	/*
	 * Recursive algorithm to find and remove the earliest, longest matching
	 * substring.
	 */
	private int getLCS(String pattern, String str, int limit){
		for(int start = 0; start < pattern.length(); start++){
			for(int end = pattern.length(); end > start; end--){
				String s = pattern.substring(start, end);
				if(str.indexOf(s) != -1){
					// longest, first occurance of string this length
					// check ot see if it is at least as long as limit
					if(s.length() >= limit){
						//String new_pattern = pattern.replaceFirst(s, "");
						String new_pattern = replaceFirst(pattern, s, "");
						//String new_str = str.replaceFirst(s, "");
						String new_str = replaceFirst(str, s, "");
						return s.length() + getLCS(new_pattern, new_str, limit);
					}
				}
				
			}
			
		}
		return 0;
	}
	
	/*
	 * Method replaces metacharacters in a string that will be used in
	 * the longest common substring methods.  Meta-characters should be
	 * prefixed with a backslash so that they are interpreted correctly
	 * as literals.
	 */
	private static String expandMetaCharacters(String str){
		String ret = new String(str);
		
		ret = ret.replaceAll("\\\\", "\\\\\\\\");
		ret = ret.replaceAll("\\?", "\\\\?");
		ret = ret.replaceAll("\\*", "\\\\*");
		ret = ret.replaceAll("\\-", "\\\\-");
		ret = ret.replaceAll("\\^", "\\\\^");
		ret = ret.replaceAll("\\$", "\\\\\\$");
		return ret;
	}
	
	/*
	 * Method replaces the first occurance of the String str within the String original
	 * with the String replacement.  This method was written since some of the
	 * data used contained regular expression meta-characters (? \ *) that would need
	 * to be modiefied to use the String class's built-in replaceFirst method.
	 * Since the LongestCommonSubString algorithm is dependant on string length
	 * this was written to perform the function of replacing the first occurance
	 * without having to modify the strings.
	 */
	private static String replaceFirst(String original, String str, String replacement){
		int index = original.indexOf(str);
		if(index == -1){
			return new String(original);
		}
		int str_length = str.length();
		String part1 = original.substring(0, index);
		String part2 = original.substring(index + str_length, original.length());
		String ret = part1 + replacement + part2;
		return ret;
	}
}
