package org.hva.cityrunner.sensei.db;

import java.util.ArrayList;
import java.util.List;

import org.hva.cityrunner.sensei.data.RouteRunData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract.Contacts.Data;

public class RouteRunDataSource {

	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { Database.RouteRun._ID,
			Database.RouteRun.COLUMN_NAME_TEAM_ID,
			Database.RouteRun.COLUMN_NAME_ROUTE_ID,
			Database.RouteRun.COLUMN_NAME_START_DATETIME,
			Database.RouteRun.COLUMN_NAME_END_DATETIME,
			Database.RouteRun.COLUMN_NAME_PHONE_POSITION,
			Database.RouteRun.COLUMN_NAME_HEADPHONES,
			Database.RouteRun.COLUMN_NAME_NUMBER_PEOPLE,
			Database.RouteRun.COLUMN_NAME_REMARKS};

	public RouteRunDataSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public RouteRunData add(RouteRunData routeRun) {
		ContentValues values = new ContentValues();
		values.put(Database.RouteRun.COLUMN_NAME_TEAM_ID, routeRun.getTeam_id());
		values.put(Database.RouteRun.COLUMN_NAME_ROUTE_ID,
				routeRun.getRoute_id());
		values.put(Database.RouteRun.COLUMN_NAME_START_DATETIME,
				routeRun.getStart_datetime());
		values.put(Database.RouteRun.COLUMN_NAME_END_DATETIME,
				routeRun.getEnd_datetime());
		values.put(Database.RouteRun.COLUMN_NAME_PHONE_POSITION,
				routeRun.getPhone_position());
		values.put(Database.RouteRun.COLUMN_NAME_HEADPHONES,
				(routeRun.isHeadphones()? 1:0 ));
		values.put(Database.RouteRun.COLUMN_NAME_NUMBER_PEOPLE,
				routeRun.getNumber_people());
		values.put(Database.RouteRun.COLUMN_NAME_REMARKS,
				routeRun.getRemarks());
		long insertId = database.insert(Database.RouteRun.TABLE_NAME, null,
				values);
		Cursor cursor = database.query(Database.RouteRun.TABLE_NAME,
				allColumns, Database.RouteRun._ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		RouteRunData rrr = cursorToRouteRun(cursor);
		cursor.close();
		return rrr;
	}

	public void deleteRouteRun(RouteRunData routeRun) {
		long id = routeRun.getId();
		database.delete(Database.RouteRun.TABLE_NAME, Database.RouteRun._ID
				+ " = " + id, null);
	}

	public List<RouteRunData> getAllRouteRun() {
		List<RouteRunData> accels = new ArrayList<RouteRunData>();

		Cursor cursor = database.query(Database.RouteRun.TABLE_NAME,
				allColumns, null, null, null, null, null, "10");

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			RouteRunData af = cursorToRouteRun(cursor);
			accels.add(af);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return accels;
	}
	
	public RouteRunData getLastRouteRun(){
		String query = "SELECT "+Database.RouteRun._ID+" from "+Database.RouteRun.TABLE_NAME+" order by "+Database.RouteRun._ID+" DESC limit 1";
		Cursor c = database.rawQuery(query,null);
		int lastId = 0;
		if (c != null && c.moveToFirst()) {
		    lastId = c.getInt(0); //The 0 is the column index, we only have 1 column, so the index is 0
		}
		
		Cursor cursor = database.query(Database.RouteRun.TABLE_NAME,
				allColumns, Database.RouteRun._ID + " = " + lastId, null,
				null, null, null);
		cursor.moveToFirst();
		RouteRunData rrr = cursorToRouteRun(cursor);
		cursor.close();
		return rrr;
	}
	
	public RouteRunData getRouteRun(int id){
		Cursor cursor = database.query(Database.RouteRun.TABLE_NAME,
				allColumns, Database.RouteRun._ID + " = " + id, null,
				null, null, null);
		cursor.moveToFirst();
		RouteRunData rrr = cursorToRouteRun(cursor);
		cursor.close();
		return rrr;
	}

	private RouteRunData cursorToRouteRun(Cursor cursor) {
		RouteRunData rrr = new RouteRunData();
		rrr.setId(cursor.getInt(0));
		rrr.setTeam_id(cursor.getInt(1));
		rrr.setRoute_id(cursor.getInt(2));
		rrr.setStart_datetime(cursor.getLong(3));
		rrr.setEnd_datetime(cursor.getLong(4));
		rrr.setPhone_position(cursor.getString(5));
		rrr.setHeadphones(cursor.getInt(6) == 0? false:true);
		rrr.setNumber_people(cursor.getInt(7));
		rrr.setRemarks(cursor.getString(8));

		return rrr;
	}
	
	//updates all values, as long as id is given.
	public void update(RouteRunData routeRun){
		ContentValues values = new ContentValues();

		values.put(Database.RouteRun.COLUMN_NAME_TEAM_ID, routeRun.getTeam_id());
		values.put(Database.RouteRun.COLUMN_NAME_ROUTE_ID,
				routeRun.getRoute_id());
		values.put(Database.RouteRun.COLUMN_NAME_START_DATETIME,
				routeRun.getStart_datetime());
		values.put(Database.RouteRun.COLUMN_NAME_END_DATETIME,
				routeRun.getEnd_datetime());
		values.put(Database.RouteRun.COLUMN_NAME_PHONE_POSITION,
				routeRun.getPhone_position());
		values.put(Database.RouteRun.COLUMN_NAME_HEADPHONES,
				(routeRun.isHeadphones()? 1:0 ));
		values.put(Database.RouteRun.COLUMN_NAME_NUMBER_PEOPLE,
				routeRun.getNumber_people());
		values.put(Database.RouteRun.COLUMN_NAME_REMARKS,
				routeRun.getRemarks());
		
		String whereClause = Database.RouteRun._ID +" = ?";
		String[] whereArgs = new String[] {String.valueOf(routeRun.getId())};
		
		database.update(Database.RouteRun.TABLE_NAME, values, whereClause, whereArgs);
	}
}
