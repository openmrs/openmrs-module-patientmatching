/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientmatching;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.regenstrief.linkage.io.OpenMRSReader;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;

public class MatchingReportUtilsContextSensitiveTest extends BasePatientMatchingContextSensitiveTest {
	
	private static final Integer CONCURRENT_RUN_COUNT = 40;
	
	@Test
	public void createDataBaseRecordStore_shouldNotReCreateTheScratchTableDuringConcurrentPatientMatchingRuns()
	        throws Exception {
		
		final DataColumn dc = new DataColumn("gender");
		dc.setName("gender");
		final LinkDataSource lds = new LinkDataSource(null, null, null, 0);
		lds.addDataColumn(dc);
		Properties props = Context.getRuntimeProperties();
		String url = props.getProperty("connection.url");
		String user = props.getProperty("connection.username");
		String pass = props.getProperty("connection.password");
		
		final int N = CONCURRENT_RUN_COUNT;
		final Set<Thread> threads = new LinkedHashSet();
		final AtomicBoolean hasFailure = new AtomicBoolean();
		for (int i = 0; i < N; i++) {
			threads.add(new Thread(() -> {
				MatchingRunData.addTask(Thread.currentThread().getName());
				try {
					Context.openSession();
					Context.authenticate("admin", "test");
					try (Connection c = DriverManager.getConnection(url, user, pass)) {
						MatchingReportUtils.createDataBaseRecordStore(new OpenMRSReader(), c, lds, null, null, null, null);
						Thread.sleep(1000);
					}
				}
				catch (Exception se) {
					hasFailure.set(true);
					logger.error(se);
				}
				finally {
					MatchingRunData.removeTask(Thread.currentThread().getName());
					Context.closeSession();
				}
			}, "db-record-store-" + i));
		}
		
		for (Thread thread : threads) {
			thread.start();
		}
		
		for (Thread thread : threads) {
			thread.join();
		}
		
		Assert.assertFalse(hasFailure.get());
	}
	
}
