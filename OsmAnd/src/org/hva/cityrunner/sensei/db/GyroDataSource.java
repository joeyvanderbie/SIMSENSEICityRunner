package org.hva.cityrunner.sensei.db;

import java.util.ArrayList;
import java.util.List;

import org.hva.cityrunner.sensei.data.AccelData;
import org.hva.cityrunner.sensei.data.GyroData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class GyroDataSource {
	// Database fields
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { Database.GYRO._ID,
			Database.GYRO.COLUMN_NAME_RUN_ID,
			Database.GYRO.COLUMN_NAME_DATETIME,
			Database.GYRO.COLUMN_NAME_GYRO_X,
			Database.GYRO.COLUMN_NAME_GYRO_Y,
			Database.GYRO.COLUMN_NAME_GYRO_Z };

	public GyroDataSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void addGyroDataList(ArrayList<GyroData> accel, int user_id,
			int route_id) {
		for (GyroData acd : accel) {
			addGyroSilent(acd, user_id, route_id);
		}
	}

	public void addGyroDataListFast(ArrayList<GyroData> accel, int user_id,
			int run_id) {
		try {
			database.beginTransaction();
			// insert huge data
			// get pre-compiled SQLiteStatement object
			SQLiteStatement statement = database
					.compileStatement("INSERT INTO "
							+ Database.GYRO.TABLE_NAME + "("
							+ Database.GYRO.COLUMN_NAME_RUN_ID + ","
							+ Database.GYRO.COLUMN_NAME_GYRO_X + ","
							+ Database.GYRO.COLUMN_NAME_GYRO_Y + ","
							+ Database.GYRO.COLUMN_NAME_GYRO_Z + ","
							+ Database.GYRO.COLUMN_NAME_DATETIME + ") "
							+ "values ("+run_id+",?,?,?,?)");
			for (GyroData acd : accel) {
				statement.bindDouble(1, acd.getX());
				statement.bindDouble(2, acd.getY());
				statement.bindDouble(3, acd.getZ());
				statement.bindString(4, acd.getTimestamp()+"");

				statement.execute();
			}
			database.setTransactionSuccessful();
		} catch(Exception e){
			Log.e("GYRODataSource", e.toString());
		}finally {
			database.endTransaction();
		}
	}

	public GyroData add(GyroData accel, int user_id, int route_id) {
		ContentValues values = new ContentValues();
		values.put(Database.GYRO.COLUMN_NAME_RUN_ID, route_id);
		values.put(Database.GYRO.COLUMN_NAME_GYRO_X, 0);
		values.put(Database.GYRO.COLUMN_NAME_GYRO_Y, 0);
		values.put(Database.GYRO.COLUMN_NAME_GYRO_Z, 0);
		values.put(Database.GYRO.COLUMN_NAME_DATETIME, accel.getTimestamp());

		long insertId = database.insert(Database.GYRO.TABLE_NAME, null,
				values);
		Cursor cursor = database.query(Database.GYRO.TABLE_NAME,
				allColumns, Database.GYRO._ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		GyroData newGyro = cursorToGyro(cursor);
		cursor.close();
		return newGyro;
	}

	public void addGyroSilent(GyroData accel, int user_id, int run_id) {
		ContentValues values = new ContentValues();
		values.put(Database.GYRO.COLUMN_NAME_RUN_ID, run_id);
		values.put(Database.GYRO.COLUMN_NAME_GYRO_X, 0);
		values.put(Database.GYRO.COLUMN_NAME_GYRO_Y, 0);
		values.put(Database.GYRO.COLUMN_NAME_GYRO_Z, 0);
		values.put(Database.GYRO.COLUMN_NAME_DATETIME, accel.getTimestamp());

		database.insert(Database.GYRO.TABLE_NAME, null, values);
	}

	public void deleteGyro(GyroData accel) {
		long id = accel.getId();
		System.out.println("Comment deleted with id: " + id);
		database.delete(Database.GYRO.TABLE_NAME, Database.GYRO._ID
				+ " = " + id, null);
	}
	
	public void deleteGyro(int run_id) {
		System.out.println("Comment deleting gyro with run_id: " + run_id);
		database.delete(Database.GYRO.TABLE_NAME, Database.GYRO.COLUMN_NAME_RUN_ID
				+ " = " + run_id, null);
	}
	
	public ArrayList<GyroData> getAllGyro(int run_id) {
		ArrayList<GyroData> accels = new ArrayList<GyroData>();
		String[] arguments = {""+run_id};
		
		Cursor cursor = database.query(Database.GYRO.TABLE_NAME,
				allColumns, Database.GYRO.COLUMN_NAME_RUN_ID+" = "+ run_id,null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			GyroData af = cursorToGyro(cursor);
			accels.add(af);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return accels;
	}
	
	public ArrayList<GyroData> getAllGyro(int run_id, int limit, int offset) {
		ArrayList<GyroData> accels = new ArrayList<GyroData>();
		String[] arguments = {""+run_id};
		
		Cursor cursor = database.query(Database.GYRO.TABLE_NAME,
				allColumns, Database.GYRO.COLUMN_NAME_RUN_ID+" = "+ run_id,null, null, null, null, offset+", "+limit);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			GyroData af = cursorToGyro(cursor);
			accels.add(af);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return accels;
	}
	
	public int getAllGyroCount(int run_id){
		String query = "SELECT count("+Database.GYRO._ID+") from "+Database.GYRO.TABLE_NAME+" WHERE "+Database.GYRO.COLUMN_NAME_RUN_ID+"="+run_id;
		Cursor c = database.rawQuery(query,null);
		int lastId = 0;
		if (c != null && c.moveToFirst()) {
		    lastId = c.getInt(0); //The 0 is the column index, we only have 1 column, so the index is 0
		}
		return lastId;
	}

	private GyroData cursorToGyro(Cursor cursor) {
		GyroData af = new GyroData();
		af.setId(cursor.getLong(0));
		af.setRun_id(cursor.getLong(1));
		af.setTimestamp(cursor.getLong(2));
		af.setX(cursor.getDouble(3));
		af.setY(cursor.getDouble(4));
		af.setZ(cursor.getDouble(5));
		
		return af;
	}
}
