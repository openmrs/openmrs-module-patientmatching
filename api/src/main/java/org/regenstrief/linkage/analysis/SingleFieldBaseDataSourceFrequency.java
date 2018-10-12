package org.regenstrief.linkage.analysis;

import java.util.Collections;
import java.util.Set;

public abstract class SingleFieldBaseDataSourceFrequency extends DataSourceFrequency {
	
	private final Set<String> fields;
	
	public SingleFieldBaseDataSourceFrequency(final String field) {
		fields = Collections.singleton(field);
	}
	
	@Override
	public final Set<String> getFields() {
		return fields;
	}
}
