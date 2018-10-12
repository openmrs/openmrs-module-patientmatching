package org.regenstrief.linkage.analysis;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class MemoryBackedDataSourceFrequency extends DataSourceFrequency {
	
	Map<String,Map<String, Count>> frequencies;
	
	public MemoryBackedDataSourceFrequency() {
		frequencies = new Hashtable<String, Map<String, Count>>();
	}
	
	@Override
	public int getFrequency(final String field, final String token) {
		if (token != null) {
			final Map<String, Count> fieldFrequencies = frequencies.get(field);
			if (fieldFrequencies != null) {
				final Count freq = fieldFrequencies.get(token);
				if (freq != null) {
					return freq.i;
				}
			}
		}
		return 0;
	}
	
	@Override
	public final int removeFrequency(final String field, final String token) {
		if (token != null) {
			final Map<String, Count> fieldFrequencies = frequencies.get(field);
			if (fieldFrequencies != null) {
				final Count freq = fieldFrequencies.remove(token);
				if (freq != null) {
					return freq.i;
				}
			}
		}
		return 0;
	}
	
	private final Map<String, Count> getOrCreateFieldFrequencies(final String field) {
		Map<String, Count> fieldFrequencies = frequencies.get(field);
		if (fieldFrequencies == null) {
			fieldFrequencies = new Hashtable<String, Count>();
			frequencies.put(field, fieldFrequencies);
		}
		return fieldFrequencies;
	}

	@Override
	public void incrementCount(String field, String token) {
		if (token != null) {
			final Map<String, Count> fieldFrequencies = getOrCreateFieldFrequencies(field);
			final Count count = fieldFrequencies.get(token);
			if (count != null) {
				count.i++;
			} else {
				fieldFrequencies.put(token, new Count(1));
			}
		}
	}
	
	@Override
	public Set<String> getFields() {
		return frequencies.keySet();
	}
	
	@Override
	public void setFrequency(String field, String token, int freq) {
		final Map<String, Count> fieldFrequencies = getOrCreateFieldFrequencies(field);
		final Count count = fieldFrequencies.get(token);
		if (count != null) {
			count.i = freq;
		} else {
			fieldFrequencies.put(token, new Count(freq));
		}
	}
	
	@Override
	public Set<String> getTokens(String field) {
		return frequencies.get(field).keySet();
	}
}
