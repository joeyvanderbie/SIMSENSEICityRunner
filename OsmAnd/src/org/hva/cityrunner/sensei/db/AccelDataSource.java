package org.hva.cityrunner.sensei.db;

import java.util.ArrayList;
import java.util.List;

import org.hva.cityrunner.sensei.data.AccelData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class AccelDataSource {
	// Database fields
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { Database.MOVEMENT._ID,
			Database.MOVEMENT.COLUMN_NAME_USER_ID,
			Database.MOVEMENT.COLUMN_NAME_RUN_ID,
			Database.MOVEMENT.COLUMN_NAME_DATETIME,
			Database.MOVEMENT.COLUMN_NAME_ACCEL_X,
			Database.MOVEMENT.COLUMN_NAME_ACCEL_Y,
			Database.MOVEMENT.COLUMN_NAME_ACCEL_Z};

	public AccelDataSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void addAccelDataList(ArrayList<AccelData> accel, int user_id,
			int route_id) {
		for (AccelData acd : accel) {
			addAccelSilent(acd, user_id, route_id);
		}
	}

	public void addAccelDataListFast(ArrayList<AccelData> accel, int user_id,
			int run_id) {
		try {
			database.beginTransaction();
			// insert huge data
			// get pre-compiled SQLiteStatement object
			SQLiteStatement statement = database
					.compileStatement("INSERT INTO "
							+ Database.MOVEMENT.TABLE_NAME + "("
							+ Database.MOVEMENT.COLUMN_NAME_USER_ID + ","
							+ Database.MOVEMENT.COLUMN_NAME_RUN_ID + ","
							+ Database.MOVEMENT.COLUMN_NAME_ACCEL_X + ","
							+ Database.MOVEMENT.COLUMN_NAME_ACCEL_Y + ","
							+ Database.MOVEMENT.COLUMN_NAME_ACCEL_Z + ","
							+ Database.MOVEMENT.COLUMN_NAME_DATETIME + ") "
							+ "values ("+user_id+","+run_id+",?,?,?,?)");
			for (AccelData acd : accel) {
				statement.bindDouble(1, acd.getX());
				statement.bindDouble(2, acd.getY());
				statement.bindDouble(3, acd.getZ());
				statement.bindString(4, acd.getTimestamp()+"");

				statement.execute();
			}
			database.setTransactionSuccessful();
		} catch(Exception e){
			Log.e("AccelDataSource", e.toString());
		}finally {
			database.endTransaction();
		}
	}

	public AccelData add(AccelData accel, int user_id, int route_id) {
		ContentValues values = new ContentValues();
		values.put(Database.MOVEMENT.COLUMN_NAME_USER_ID, user_id);
		values.put(Database.MOVEMENT.COLUMN_NAME_RUN_ID, route_id);
		values.put(Database.MOVEMENT.COLUMN_NAME_ACCEL_X, accel.getX());
		values.put(Database.MOVEMENT.COLUMN_NAME_ACCEL_Y, accel.getY());
		values.put(Database.MOVEMENT.COLUMN_NAME_ACCEL_Z, accel.getZ());
		values.put(Database.MOVEMENT.COLUMN_NAME_DATETIME, accel.getTimestamp());

		long insertId = database.insert(Database.MOVEMENT.TABLE_NAME, null,
				values);
		Cursor cursor = database.query(Database.MOVEMENT.TABLE_NAME,
				allColumns, Database.MOVEMENT._ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		AccelData newAccel = cursorToAccel(cursor);
		cursor.close();
		return newAccel;
	}

	public void addAccelSilent(AccelData accel, int user_id, int run_id) {
		ContentValues values = new ContentValues();
		values.put(Database.MOVEMENT.COLUMN_NAME_USER_ID, user_id);
		values.put(Database.MOVEMENT.COLUMN_NAME_RUN_ID, run_id);
		values.put(Database.MOVEMENT.COLUMN_NAME_ACCEL_X, accel.getX());
		values.put(Database.MOVEMENT.COLUMN_NAME_ACCEL_Y, accel.getY());
		values.put(Database.MOVEMENT.COLUMN_NAME_ACCEL_Z, accel.getZ());
		values.put(Database.MOVEMENT.COLUMN_NAME_DATETIME, accel.getTimestamp());

		database.insert(Database.MOVEMENT.TABLE_NAME, null, values);
	}

	public void deleteAccel(AccelData accel) {
		long id = accel.getId();
		System.out.println("Comment deleted with id: " + id);
		database.delete(Database.MOVEMENT.TABLE_NAME, Database.MOVEMENT._ID
				+ " = " + id, null);
	}
	
	public void deleteAccel(int run_id) {
		System.out.println("Comment deleting accels with run_id: " + run_id);
		database.delete(Database.MOVEMENT.TABLE_NAME, Database.MOVEMENT.COLUMN_NAME_RUN_ID
				+ " = " + run_id, null);
	}

	public List<AccelData> getAllAccel() {
		List<AccelData> accels = new ArrayList<AccelData>();

		Cursor cursor = database.query(Database.MOVEMENT.TABLE_NAME,
				allColumns, null, null, null, null, null, "10");

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			AccelData af = cursorToAccel(cursor);
			accels.add(af);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return accels;
	}
	
	public ArrayList<AccelData> getAllAccel(int run_id, int limit, int offset) {
		ArrayList<AccelData> accels = new ArrayList<AccelData>();
		String[] arguments = {""+run_id};
		
		Cursor cursor = database.query(Database.MOVEMENT.TABLE_NAME,
				allColumns, Database.MOVEMENT.COLUMN_NAME_RUN_ID+" = "+ run_id,null, null, null, null, offset+", "+limit);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			AccelData af = cursorToAccel(cursor);
			accels.add(af);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return accels;
	}
	
	public int getAllAccelCount(int run_id){
		String query = "SELECT count("+Database.MOVEMENT._ID+") from "+Database.MOVEMENT.TABLE_NAME+" WHERE "+Database.MOVEMENT.COLUMN_NAME_RUN_ID+"="+run_id;
		Cursor c = database.rawQuery(query,null);
		int lastId = 0;
		if (c != null && c.moveToFirst()) {
		    lastId = c.getInt(0); //The 0 is the column index, we only have 1 column, so the index is 0
		}
		return lastId;
	}

	private AccelData cursorToAccel(Cursor cursor) {
		AccelData af = new AccelData();
		af.setId(cursor.getLong(0));
		af.setRun_id(cursor.getLong(2));
		af.setTimestamp(cursor.getLong(3));
		af.setX(cursor.getDouble(4));
		af.setY(cursor.getDouble(5));
		af.setZ(cursor.getDouble(6));
		
		return af;
	}
}
