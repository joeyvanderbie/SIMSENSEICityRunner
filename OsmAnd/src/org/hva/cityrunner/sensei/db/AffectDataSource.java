package org.hva.cityrunner.sensei.db;

import java.util.ArrayList;

import org.hva.cityrunner.sensei.data.AffectData;
import org.hva.createit.digitallife.sam.Affect;
import org.hva.createit.digitallife.sam.AffectDomain;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class AffectDataSource {
	// Database fields
	  private SQLiteDatabase database;
	  private DatabaseHelper dbHelper;
	  private String[] allColumns = { 
			  Database.Affect._ID,
			  Database.Affect.COLUMN_NAME_USER_ID,
			  Database.Affect.COLUMN_NAME_AFFECT_TOOL_ID,
			  Database.Affect.COLUMN_NAME_ROUTE_ID,
			  Database.Affect.COLUMN_NAME_RUN_STATE,
			  Database.Affect.COLUMN_NAME_AROUSAL,
			  Database.Affect.COLUMN_NAME_PLEASURE,
			  Database.Affect.COLUMN_NAME_DOMINANCE,
			  Database.Affect.COLUMN_NAME_DATETIME };
	  
	  public AffectDataSource(Context context) {
		    dbHelper = new DatabaseHelper(context);
		  }

		  public void open() throws SQLException {
		    database = dbHelper.getWritableDatabase();
		  }

		  public void close() {
		    dbHelper.close();
		  }

		  //add run id
		  public AffectData addAffect(int affect_tool_id, int run_id, int user_id, int run_state, Affect af) {
		    ContentValues values = new ContentValues();
		    values.put(Database.Affect.COLUMN_NAME_USER_ID, user_id);
		    values.put(Database.Affect.COLUMN_NAME_AFFECT_TOOL_ID, affect_tool_id);
		    values.put(Database.Affect.COLUMN_NAME_ROUTE_ID, run_id);
		    values.put(Database.Affect.COLUMN_NAME_RUN_STATE, run_state);
		    values.put(Database.Affect.COLUMN_NAME_AROUSAL, af.getArousal().getDomain_value());
		    values.put(Database.Affect.COLUMN_NAME_PLEASURE, af.getPleasure().getDomain_value());
		    values.put(Database.Affect.COLUMN_NAME_DOMINANCE, af.getDominance().getDomain_value());
		    values.put(Database.Affect.COLUMN_NAME_DATETIME, af.getDatetime());
		    
		    long insertId = database.insert(Database.Affect.TABLE_NAME, null,
		        values);
		    Cursor cursor = database.query(Database.Affect.TABLE_NAME,
		        allColumns, Database.Affect._ID + " = " + insertId, null,
		        null, null, null);
		    cursor.moveToFirst();
		    AffectData newAffect = cursorToAffect(cursor);
		    cursor.close();
		    return newAffect;
		  }

		  public void deleteAffect(Affect affect) {
		    long id = affect.getId();
		    System.out.println("Comment deleted with id: " + id);
		    database.delete(Database.Affect.TABLE_NAME, Database.Affect._ID
		        + " = " + id, null);
		  }

		  public ArrayList<AffectData> getAllAffects() {
			  ArrayList<AffectData> affects = new ArrayList<AffectData>();

		    Cursor cursor = database.query(Database.Affect.TABLE_NAME,
		        allColumns, null, null, null, null, null);

		    cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
		    	AffectData af = cursorToAffect(cursor);
		      affects.add(af);
		      cursor.moveToNext();
		    }
		    // make sure to close the cursor
		    cursor.close();
		    return affects;
		  }
		  
		  public ArrayList<AffectData> getAffects(int run_id) {
			  ArrayList<AffectData> affects = new ArrayList<AffectData>();

			    Cursor cursor = database.query(Database.Affect.TABLE_NAME,
			        allColumns, Database.Affect.COLUMN_NAME_ROUTE_ID +" = "+run_id, null, null, null, null);

			    cursor.moveToFirst();
			    while (!cursor.isAfterLast()) {
			    	AffectData af = cursorToAffect(cursor);
			      affects.add(af);
			      cursor.moveToNext();
			    }
			    // make sure to close the cursor
			    cursor.close();
			    return affects;
			  }

		  private AffectData cursorToAffect(Cursor cursor) {
			  AffectData af = new AffectData();
		    af.setId(cursor.getLong(0));
		    af.setPleasure(new AffectDomain(cursor.getDouble(6)));
		    af.setDominance(new AffectDomain(cursor.getDouble(7)));
		    af.setArousal(new AffectDomain(cursor.getDouble(5)));
		    af.setDatetime(cursor.getLong(8));
		    af.setRun_id(cursor.getInt(3));
		    af.setRunstate(cursor.getInt(4));
		    return af;
		  }
}
