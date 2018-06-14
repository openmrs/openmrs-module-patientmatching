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
package org.regenstrief.linkage;

public final class MatchItem {
	
	private final float similarity;
	
	private final boolean match;
	
	public MatchItem(final float similarity, final boolean match) {
		this.similarity = similarity;
		this.match = match;
	}
	
	public final float getSimilarity() {
		return similarity;
	}
	
	public final boolean isMatch() {
		return match;
	}
	
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		} else if (o == null) {
			return false;
		} else if (o.getClass() != MatchItem.class) {
			return false;
		}
		return ((MatchItem) o).match == match;
	}
	
	@Override
	public int hashCode() {
		return this.match ? 1 : 0;
	}
}
