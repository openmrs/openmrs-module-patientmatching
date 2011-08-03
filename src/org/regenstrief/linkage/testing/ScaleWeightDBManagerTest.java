package org.regenstrief.linkage.testing;

import org.regenstrief.linkage.db.ScaleWeightDBManager;
import org.regenstrief.linkage.db.ScaleWeightDBManager.CountType;
import org.regenstrief.linkage.util.DataColumn;

public class ScaleWeightDBManagerTest {

	public static void main(String[] args) {
		ScaleWeightDBManager dbm = new ScaleWeightDBManager("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/patientmatching_datasource_analysis","root","");
		boolean connected = dbm.connect();
		DataColumn target_col = new DataColumn("isim");
		target_col.setName("name");
		int ds_id = 5;
		
		System.out.println(dbm.getCount(CountType.NonNull, target_col, ds_id));
		System.out.println(dbm.getCount(CountType.Null, target_col, ds_id));
		System.out.println(dbm.getCount(CountType.Unique, target_col, ds_id));
		System.out.println(dbm.getRecordCount(ds_id));
		
		// INSERT
		dbm.setCount(CountType.NonNull, target_col, ds_id, 9);
		dbm.setCount(CountType.Null, target_col, ds_id, 8);
		dbm.setRecordCount(ds_id, 17, "asdf");
		dbm.setCount(CountType.Unique, target_col, ds_id, 3);
		
		System.out.println("Non Null: " + dbm.getCount(CountType.NonNull, target_col, ds_id));
		System.out.println("Null: " + dbm.getCount(CountType.Null, target_col, ds_id));
		System.out.println("Unique Non Null: " + dbm.getCount(CountType.Unique, target_col, ds_id));
		System.out.println("Records: " + dbm.getRecordCount(ds_id));
		
		// UPDATE
		dbm.setCount(CountType.NonNull, target_col, ds_id, 10);
		dbm.setCount(CountType.Null, target_col, ds_id, 9);
		dbm.setCount(CountType.Unique, target_col, ds_id, 4);
		dbm.setRecordCount(ds_id, 18, "asdf");
		
		System.out.println("Non Null: " + dbm.getCount(CountType.NonNull, target_col, ds_id));
		System.out.println("Null: " + dbm.getCount(CountType.Null, target_col, ds_id));
		System.out.println("Unique Non Null: " + dbm.getCount(CountType.Unique, target_col, ds_id));
		System.out.println("Records: " + dbm.getRecordCount(ds_id));
		
		System.out.println(dbm.doesTableExist("patientmatching_analysis"));
		System.out.println(dbm.doesTableExist("patientmatching"));
		System.out.println(dbm.doesTableExist("a"));
	}
}
