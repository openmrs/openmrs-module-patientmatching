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
package org.regenstrief.linkage.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * StringMatchTest
 */
public class StringMatchTest {
	
	@Test
	public void getDiceMatchSimilarity_shouldBeCorrect() {
		runDice(0, "1", "0");
		runDice(1, "1", "1");
		runDice(0, "10", "01");
		runDice(1, "10", "10");
		runDice(1, "01", "01");
		runDice(1, "11", "11");
		runDice(2f / 3f, "10", "11");
		runDice(16f / 22f, "10101001100011001011", "10011001100111110011");
	}
	
	private void runDice(final float ex, final String s1, final String s2) {
		Assert.assertEquals(ex, StringMatch.getDiceMatchSimilarity(s1, s2), 0.01f);
	}
}
