package org.hva.cityrunner.sensei.db;

import java.util.ArrayList;
import java.util.List;

import org.hva.cityrunner.sensei.data.LocationData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class LocationDataSource {

	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { Database.Location._ID,
			Database.Location.COLUMN_NAME_RUN_ID,
			Database.Location.COLUMN_NAME_DATETIME,
			Database.Location.COLUMN_NAME_LATITUDE,
			Database.Location.COLUMN_NAME_LONGITUDE};

	public LocationDataSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void add(LocationData location) {
		ContentValues values = new ContentValues();
		values.put(Database.Location.COLUMN_NAME_RUN_ID, location.getRun_id());
		values.put(Database.Location.COLUMN_NAME_DATETIME,
				location.getTime());
		values.put(Database.Location.COLUMN_NAME_LATITUDE,
				location.getLatitude());
		values.put(Database.Location.COLUMN_NAME_LONGITUDE,
				location.getLongitude());

		long insertId = database.insert(Database.Location.TABLE_NAME, null,
				values);
//		Cursor cursor = database.query(Database.Location.TABLE_NAME,
//				allColumns, Database.Location._ID + " = " + insertId, null,
//				null, null, null);
//		cursor.moveToFirst();
//		LocationData rrr = cursorToLocation(cursor);
//		cursor.close();
//		return rrr;
	}

	public void deleteLocation(LocationData Location) {
		long id = Location.getId();
		System.out.println("Comment deleted with id: " + id);
		database.delete(Database.Location.TABLE_NAME, Database.Location._ID
				+ " = " + id, null);
	}

	//for each run
	public List<LocationData> getAllLocation(int run_id) {
		List<LocationData> accels = new ArrayList<LocationData>();

		Cursor cursor = database.query(Database.Location.TABLE_NAME,
				allColumns, Database.Location.COLUMN_NAME_RUN_ID + " = " + run_id, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			LocationData af = cursorToLocation(cursor);
			accels.add(af);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return accels;
	}

	private LocationData cursorToLocation(Cursor cursor) {
		LocationData rrr = new LocationData();
		rrr.setId(cursor.getInt(0));
		rrr.setRun_id(cursor.getInt(1));
		rrr.setTime(cursor.getLong(2));
		rrr.setLatitude(cursor.getLong(3));
		rrr.setLongitude(cursor.getLong(4));

		return rrr;
	}
}
