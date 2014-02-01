package org.hva.cityrunner.sensei.db;

import java.util.ArrayList;
import java.util.List;

import org.hva.cityrunner.sensei.data.QueueData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class QueueDataSource {

	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { Database.Queue._ID,
			Database.Queue.COLUMN_NAME_RUN_ID,
			Database.Queue.COLUMN_NAME_SUBMITTED,
			Database.Queue.COLUMN_NAME_ACCELLEFT,
			Database.Queue.COLUMN_NAME_GYROLEFT,
			Database.Queue.COLUMN_NAME_GPSLEFT,
			Database.Queue.COLUMN_NAME_EMOTIE,
			Database.Queue.COLUMN_NAME_FINISHED};

	public QueueDataSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public QueueData add(QueueData queue) {
		ContentValues values = new ContentValues();
		values.put(Database.Queue.COLUMN_NAME_RUN_ID,
				queue.getRun_id());
		values.put(Database.Queue.COLUMN_NAME_SUBMITTED,
				queue.getSubmitted());
		values.put(Database.Queue.COLUMN_NAME_ACCELLEFT,
				queue.getAccelleft());
		values.put(Database.Queue.COLUMN_NAME_GYROLEFT,
				queue.getGyroleft());
		values.put(Database.Queue.COLUMN_NAME_GPSLEFT,
				queue.getGpsleft());
		values.put(Database.Queue.COLUMN_NAME_EMOTIE,
				queue.getEmotie());
		values.put(Database.Queue.COLUMN_NAME_EMOTIE,
				queue.getFinished());
		values.put(Database.Queue.COLUMN_NAME_FINISHED,
				queue.getFinished());
		
		long insertId = database.insert(Database.Queue.TABLE_NAME, null,
				values);
		Cursor cursor = database.query(Database.Queue.TABLE_NAME,
				allColumns, Database.Queue._ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		QueueData rrr = cursorToQueue(cursor);
		cursor.close();
		return rrr;
	}

	public void deleteQueue(QueueData Queue) {
		long id = Queue.getId();
		System.out.println("Comment deleted with id: " + id);
		database.delete(Database.Queue.TABLE_NAME, Database.Queue._ID
				+ " = " + id, null);
	}

	public List<QueueData> getAllQueue() {
		List<QueueData> accels = new ArrayList<QueueData>();

		Cursor cursor = database.query(Database.Queue.TABLE_NAME,
				allColumns, null, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			QueueData af = cursorToQueue(cursor);
			accels.add(af);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return accels;
	}
	
	public QueueData getLastQueue(){
		String query = "SELECT "+Database.Queue._ID+" from "+Database.Queue.TABLE_NAME+" WHERE "+Database.Queue.COLUMN_NAME_FINISHED+" !=1 order by "+Database.Queue._ID+" DESC limit 1";
		Cursor c = database.rawQuery(query,null);
		int lastId = 0;
		if (c != null && c.moveToFirst()) {
		    lastId = c.getInt(0); //The 0 is the column index, we only have 1 column, so the index is 0
		}
		
		Cursor cursor = database.query(Database.Queue.TABLE_NAME,
				allColumns, Database.Queue._ID + " = " + lastId, null,
				null, null, null);
		cursor.moveToFirst();
		QueueData rrr = cursorToQueue(cursor);
		cursor.close();
		return rrr;
	}
	
	public QueueData getQueue(int id){
		Cursor cursor = database.query(Database.Queue.TABLE_NAME,
				allColumns, Database.Queue._ID + " = " + id, null,
				null, null, null);
		cursor.moveToFirst();
		QueueData rrr = cursorToQueue(cursor);
		cursor.close();
		return rrr;
	}
	
	public QueueData getQueueByRun_id(int run_id){
		Cursor cursor = database.query(Database.Queue.TABLE_NAME,
				allColumns, Database.Queue.COLUMN_NAME_RUN_ID + " = " + run_id, null,
				null, null, null);
		cursor.moveToFirst();
		QueueData rrr = cursorToQueue(cursor);
		cursor.close();
		return rrr;
	}

	private QueueData cursorToQueue(Cursor cursor) {
		QueueData rrr = new QueueData();
		rrr.setId(cursor.getInt(0));
		rrr.setRun_id(cursor.getInt(1));
		rrr.setSubmitted(cursor.getInt(2));
		rrr.setAccelleft(cursor.getInt(3));
		rrr.setGyroleft(cursor.getInt(4));
		rrr.setGpsleft(cursor.getInt(5));
		rrr.setEmotie(cursor.getInt(6));
		rrr.setFinished(cursor.getInt(7));

		return rrr;
	}
	
	//updates all values, as long as id is given.
	public void update(QueueData queue){
		ContentValues values = new ContentValues();

		values.put(Database.Queue.COLUMN_NAME_RUN_ID,
				queue.getRun_id());
		values.put(Database.Queue.COLUMN_NAME_SUBMITTED,
				queue.getSubmitted());
		values.put(Database.Queue.COLUMN_NAME_ACCELLEFT,
				queue.getAccelleft());
		values.put(Database.Queue.COLUMN_NAME_GYROLEFT,
				queue.getGyroleft());
		values.put(Database.Queue.COLUMN_NAME_GPSLEFT,
				queue.getGpsleft());
		values.put(Database.Queue.COLUMN_NAME_EMOTIE,
				queue.getEmotie());
		values.put(Database.Queue.COLUMN_NAME_FINISHED,
				queue.getFinished());
		
		String whereClause = Database.Queue._ID +" = ?";
		String[] whereArgs = new String[] {String.valueOf(queue.getId())};
		
		database.update(Database.Queue.TABLE_NAME, values, whereClause, whereArgs);
	}
	
	//updates all values, as long as id is given.
	public void updateAccelleft(int id, int accelleft){
		ContentValues values = new ContentValues();
		
		values.put(Database.Queue.COLUMN_NAME_ACCELLEFT,
				accelleft);
		
		String whereClause = Database.Queue._ID +" = ?";
		String[] whereArgs = new String[] {String.valueOf(id)};
		
		database.update(Database.Queue.TABLE_NAME, values, whereClause, whereArgs);
	}
	
	public void updateGyroleft(int id, int gyroleft){
		ContentValues values = new ContentValues();
		
		values.put(Database.Queue.COLUMN_NAME_GYROLEFT,
				gyroleft);
		
		String whereClause = Database.Queue._ID +" = ?";
		String[] whereArgs = new String[] {String.valueOf(id)};
		
		database.update(Database.Queue.TABLE_NAME, values, whereClause, whereArgs);
	}
	
	public void updateGpsleft(int id, int gpsleft){
		ContentValues values = new ContentValues();
		
		values.put(Database.Queue.COLUMN_NAME_GPSLEFT,
				gpsleft);
		
		String whereClause = Database.Queue._ID +" = ?";
		String[] whereArgs = new String[] {String.valueOf(id)};
		
		database.update(Database.Queue.TABLE_NAME, values, whereClause, whereArgs);
	}
	
	public void updateEmotie(int id, int emotie){
		ContentValues values = new ContentValues();
		
		values.put(Database.Queue.COLUMN_NAME_EMOTIE,
				emotie);
		
		String whereClause = Database.Queue._ID +" = ?";
		String[] whereArgs = new String[] {String.valueOf(id)};
		
		database.update(Database.Queue.TABLE_NAME, values, whereClause, whereArgs);
	}
	
	public void updateSubmitted(int id, int submitted){
		ContentValues values = new ContentValues();
		
		values.put(Database.Queue.COLUMN_NAME_SUBMITTED,
				submitted);
		
		String whereClause = Database.Queue._ID +" = ?";
		String[] whereArgs = new String[] {String.valueOf(id)};
		
		database.update(Database.Queue.TABLE_NAME, values, whereClause, whereArgs);
	}
	
	public void updateFinished(int id, int finished){
		ContentValues values = new ContentValues();
		
		values.put(Database.Queue.COLUMN_NAME_FINISHED,
				finished);
		
		String whereClause = Database.Queue._ID +" = ?";
		String[] whereArgs = new String[] {String.valueOf(id)};
		
		database.update(Database.Queue.TABLE_NAME, values, whereClause, whereArgs);
	}
}
