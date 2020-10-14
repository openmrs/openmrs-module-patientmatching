/**
 * The contents of this file are subject to the Regenstrief Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance with the License.
 * Please contact Regenstrief Institute if you would like to obtain a copy of the license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) Regenstrief Institute.  All Rights Reserved.
 */
package org.regenstrief.linkage.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import org.regenstrief.linkage.db.ManualReviewDBCombiner.PairKey;
import org.regenstrief.linkage.db.ManualReviewDBCombiner.PairResult;

/**
 * ManualReviewDBTieResolver
 */
public class ManualReviewDBTieResolver {
	
	private final static Pattern patPipe = Pattern.compile("\\|");
	
	public final static void main(final String[] args) throws Exception {
		run(args[0], args[1]);
	}
	
	private final static void run(final String discordantFile, final String tieBreakerDbFile) throws Exception {
		info("Starting");
		final String tieBreakerReviewer = new File(tieBreakerDbFile).getParentFile().getName();
		int trueCount = 0, falseCount = 0;
		final Map<PairKey, PairResult> cache = loadDbCache(tieBreakerReviewer, tieBreakerDbFile);
		final String resolvedFile = discordantFile + ".resolved.txt";
		final PrintStream out = new PrintStream(resolvedFile);
		try {
			final BufferedReader in = new BufferedReader(new FileReader(discordantFile));
			final String header = in.readLine();
			int h = 0, uid1Index = -1, uid2Index = -1;
			for (final String token : patPipe.split(header)) {
				if ("UID1".equalsIgnoreCase(token)) {
					uid1Index = h;
				} else if ("UID2".equalsIgnoreCase(token)) {
					uid2Index = h;
				}
				h++;
			}
			out.println(header + "|reviewer3");
			String line;
			int size = -1, statusIndex = -1;
			while ((line = in.readLine()) != null) {
				final String[] tokens = patPipe.split(line);
				if (size < 0) {
					size = tokens.length;
					statusIndex = size - 3;
				} else if (size != tokens.length) {
					throw new IllegalStateException("Found row with " + tokens.length + " fields but expected " + size);
				}
				for (int i = 0; i < size; i++) {
					if (i == statusIndex) {
						final PairKey key = new PairKey(Long.parseLong(tokens[uid1Index]),
						        Long.parseLong(tokens[uid2Index]));
						final PairResult result = cache.get(key);
						if (result.matchStatus.booleanValue()) {
							trueCount++;
						} else {
							falseCount++;
						}
						out.print(result.matchStatus);
					} else {
						out.print(tokens[i]);
					}
					out.print('|');
				}
				out.println(tieBreakerReviewer);
			}
			in.close();
		}
		finally {
			out.close();
		}
		info("Finished after finding " + trueCount + " matches and " + falseCount + " non-matches");
	}
	
	private final static Map<PairKey, PairResult> loadDbCache(final String tieBreakerReviewer, final String tieBreakerDbFile)
	        throws Exception {
		ManualReviewDBCombiner.runReviewer(tieBreakerReviewer, tieBreakerDbFile);
		return ManualReviewDBCombiner.pairs;
	}
	
	private final static void info(final Object s) {
		System.err.println(new Date() + " - " + s);
	}
}
