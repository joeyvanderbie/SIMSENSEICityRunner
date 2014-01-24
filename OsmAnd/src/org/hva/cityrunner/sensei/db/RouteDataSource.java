package org.hva.cityrunner.sensei.db;

import java.util.ArrayList;
import java.util.List;

import org.hva.cityrunner.sensei.data.RouteNeighbourhood;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class RouteDataSource {
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { Database.Route._ID,
			Database.Route.COLUMN_NAME_ROUTE_NEIGHBOURHOOD,
			Database.Route.COLUMN_NAME_ROUTE_H_ID,
			Database.Route.COLUMN_NAME_ROUTE_V_ID,
			Database.Route.COLUMN_NAME_ROUTE_A_ID};

	public RouteDataSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public int add(String neighbourhood, int route_h, int route_v, int route_a) {
		ContentValues values = new ContentValues();
		values.put(Database.Route.COLUMN_NAME_ROUTE_H_ID,
				route_h);
		values.put(Database.Route.COLUMN_NAME_ROUTE_V_ID,
						route_v);
		values.put(Database.Route.COLUMN_NAME_ROUTE_A_ID,
				route_a);
		values.put(Database.Route.COLUMN_NAME_ROUTE_NEIGHBOURHOOD,
				neighbourhood);

		long insertId = database.insert(Database.Route.TABLE_NAME, null,
				values);
		
		return (int) insertId;
	}

	public void deleteRoute(int route_id) {
		System.out.println("Comment deleted with id: " + route_id);
		database.delete(Database.RouteRun.TABLE_NAME, Database.Route._ID
				+ " = " + route_id, null);
	}

	public RouteNeighbourhood getRouteForNeighbourhood(String neighbourhood) {
		RouteNeighbourhood accels = new RouteNeighbourhood();
		String[] where = {neighbourhood};
		
		Cursor cursor = database.query(Database.Route.TABLE_NAME,
				allColumns, Database.Route.COLUMN_NAME_ROUTE_NEIGHBOURHOOD +" = ?", where, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			RouteNeighbourhood af = cursorToRoute(cursor);
			accels = af ;
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return accels;
	}
	
	public List<RouteNeighbourhood> getAllRoutes() {
		List<RouteNeighbourhood> accels = new ArrayList<RouteNeighbourhood>();

		Cursor cursor = database.query(Database.Route.TABLE_NAME,
				allColumns, null, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			RouteNeighbourhood af = cursorToRoute(cursor);
			accels.add(af);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return accels;
	}
	
	public List<String> getAllRoutesString() {
		List<String> lables = new ArrayList<String>();
		String[] neighbourhoodsList = { Database.Route.COLUMN_NAME_ROUTE_NEIGHBOURHOOD };
		
		Cursor cursor = database.query(
				Database.Route.TABLE_NAME,
				neighbourhoodsList, null, null, null, null, Database.Route.COLUMN_NAME_ROUTE_NEIGHBOURHOOD , null);

		// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	lables.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
		// make sure to close the cursor
		cursor.close();
		return lables;
		
	}

	private RouteNeighbourhood cursorToRoute(Cursor cursor) {
		RouteNeighbourhood rrr = new RouteNeighbourhood();
		rrr.setId(cursor.getInt(0));
		rrr.setNeighbourhood(cursor.getString(1));
		rrr.setRoute_h(cursor.getInt(2));
		rrr.setRoute_v(cursor.getInt(3));
		rrr.setRoute_a(cursor.getInt(4));

		return rrr;
	}
	
}