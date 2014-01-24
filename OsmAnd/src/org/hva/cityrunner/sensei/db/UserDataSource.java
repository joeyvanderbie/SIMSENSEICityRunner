package org.hva.cityrunner.sensei.db;

import org.hva.cityrunner.sensei.data.UserData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class UserDataSource {
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { Database.User._ID,
			Database.User.COLUMN_NAME_NAME,
			Database.User.COLUMN_NAME_EMAIL,
			Database.User.COLUMN_NAME_PASSWORD,
			Database.User.COLUMN_NAME_TEAMID,
			Database.User.COLUMN_NAME_HEIGHT,
			Database.User.COLUMN_NAME_WEIGHT,
			Database.User.COLUMN_NAME_AGE,
			Database.User.COLUMN_NAME_GENDER};

	public UserDataSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public int add(String name, String email, String password, int teamid, double height, double weight, int age, String gender) {
		ContentValues values = new ContentValues();
		values.put(Database.User.COLUMN_NAME_NAME,
				name);
		values.put(Database.User.COLUMN_NAME_EMAIL,
				email);
		values.put(Database.User.COLUMN_NAME_PASSWORD,
				password);
		values.put(Database.User.COLUMN_NAME_TEAMID,
				teamid);
		values.put(Database.User.COLUMN_NAME_HEIGHT,
						height);
		values.put(Database.User.COLUMN_NAME_WEIGHT,
								weight);

		values.put(Database.User.COLUMN_NAME_AGE,
								age);

		values.put(Database.User.COLUMN_NAME_GENDER,
								gender);
		
		
		long insertId = database.insert(Database.User.TABLE_NAME, null,
				values);
		
		return (int) insertId;
	}

	public void add(UserData user){
		add(user.getName(), user.getEmail(), user.getPassword(), user.getTeamid(), user.getHeight(), user.getWeight(), user.getAge(), user.getGender());
	}
	
	public void update(UserData user){
		ContentValues values = new ContentValues();
		values.put(Database.User.COLUMN_NAME_NAME,
				user.getName());
		values.put(Database.User.COLUMN_NAME_EMAIL,
				user.getEmail());
		values.put(Database.User.COLUMN_NAME_PASSWORD,
				user.getPassword());
		values.put(Database.User.COLUMN_NAME_TEAMID,
				user.getTeamid());
		values.put(Database.User.COLUMN_NAME_HEIGHT,
				user.getHeight());
		values.put(Database.User.COLUMN_NAME_WEIGHT,
				user.getWeight());
		values.put(Database.User.COLUMN_NAME_AGE,
				user.getAge());
		values.put(Database.User.COLUMN_NAME_GENDER,
						user.getGender());
		
		database.update(Database.User.TABLE_NAME, values, Database.User._ID + " = "+user.getId(), null);
	}
	

	public UserData getUserData() {
		UserData accels  = new UserData();
		
		Cursor cursor = database.query(Database.User.TABLE_NAME,
				allColumns, null,null, null, null, null, "1");

		if(!cursor.moveToFirst()){
			return null;
		}
		while (!cursor.isAfterLast()) {
			accels = cursorToUserData(cursor);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return accels;
	}
	
	


	private UserData cursorToUserData(Cursor cursor) {
		UserData rrr = new UserData();
		rrr.setId(cursor.getInt(0));
		rrr.setName(cursor.getString(1));
		rrr.setEmail(cursor.getString(2));
		rrr.setPassword(cursor.getString(3));
		rrr.setTeamid(cursor.getInt(4));
		rrr.setHeight(cursor.getInt(5));
		rrr.setWeight(cursor.getInt(6));
		rrr.setAge(cursor.getInt(7));
		rrr.setGender(cursor.getString(8));

		return rrr;
	}
	
}