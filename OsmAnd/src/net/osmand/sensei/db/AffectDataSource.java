package net.osmand.sensei.db;

import java.util.ArrayList;
import java.util.List;

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
		  public Affect addAffect(int affect_tool_id, int run_id, int user_id, int run_state, Affect af) {
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
		    Affect newAffect = cursorToAffect(cursor);
		    cursor.close();
		    return newAffect;
		  }

		  public void deleteAffect(Affect affect) {
		    long id = affect.getId();
		    System.out.println("Comment deleted with id: " + id);
		    database.delete(Database.Affect.TABLE_NAME, Database.Affect._ID
		        + " = " + id, null);
		  }

		  public List<Affect> getAllAffects() {
		    List<Affect> affects = new ArrayList<Affect>();

		    Cursor cursor = database.query(Database.Affect.TABLE_NAME,
		        allColumns, null, null, null, null, null);

		    cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
		      Affect af = cursorToAffect(cursor);
		      affects.add(af);
		      cursor.moveToNext();
		    }
		    // make sure to close the cursor
		    cursor.close();
		    return affects;
		  }

		  private Affect cursorToAffect(Cursor cursor) {
		    Affect af = new Affect();
		    af.setId(cursor.getLong(0));
		    af.setPleasure(new AffectDomain(cursor.getDouble(1)));
		    af.setDominance(new AffectDomain(cursor.getDouble(2)));
		    af.setArousal(new AffectDomain(cursor.getDouble(1)));
		    return af;
		  }
}
