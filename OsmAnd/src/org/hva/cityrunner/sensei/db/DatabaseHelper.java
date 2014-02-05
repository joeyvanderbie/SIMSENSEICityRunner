package org.hva.cityrunner.sensei.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	// If you change the database schema, you must increment the database
	// version.
	public static final int DATABASE_VERSION = 17;
	public static final String DATABASE_NAME = "SenseiCityRunner.db";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Database.SQL_CREATE_USER);
		db.execSQL(Database.SQL_CREATE_AFFECT);
		db.execSQL(Database.SQL_CREATE_AFFECT_TOOL);
		db.execSQL(Database.SQL_CREATE_MOVEMENT);
		db.execSQL(Database.SQL_CREATE_GYRO);
		db.execSQL(Database.SQL_CREATE_ROUTE_RUN);
		db.execSQL(Database.SQL_CREATE_ROUTE);
		db.execSQL(Database.SQL_CREATE_LOCATION);
		db.execSQL(Database.SQL_CREATE_QUEUE);
		insertRoutes(db);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy
		// is
		// to simply to discard the data and start over
		db.execSQL(Database.SQL_DELETE_USER);
		db.execSQL(Database.SQL_DELETE_AFFECT);
		db.execSQL(Database.SQL_DELETE_AFFECT_TOOL);
		db.execSQL(Database.SQL_DELETE_MOVEMENT);
		db.execSQL(Database.SQL_DELETE_ROUTE_RUN);
		db.execSQL(Database.SQL_DELETE_ROUTE);
		db.execSQL(Database.SQL_DELETE_GYRO);
		db.execSQL(Database.SQL_DELETE_LOCATION);
		db.execSQL(Database.SQL_DELETE_QUEUE);
		onCreate(db);
	}
	
	public void doSaveDelete(SQLiteDatabase db) {
		// This database is only a cache for online data, so its upgrade policy
		// is
		// to simply to discard the data and start over
		db.execSQL(Database.SQL_DELETE_MOVEMENT);
		db.execSQL(Database.SQL_DELETE_GYRO);
		db.execSQL(Database.SQL_DELETE_LOCATION);
		
		db.execSQL(Database.SQL_CREATE_MOVEMENT);
		db.execSQL(Database.SQL_CREATE_GYRO);
		db.execSQL(Database.SQL_CREATE_LOCATION);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

	private void insertRoutes(SQLiteDatabase db) {
		addRoute(db, "Burgwallen-Oude Zijde", 1, 2, 3);
		addRoute(db, "Burgwallen-Nieuwe Zijde", 4, 5, 6);
		addRoute(db, "Grachtengordel", 7, 8, 9);
		addRoute(db, "Nieuwmarkt", 10, 11, 12);
		addRoute(db, "Haarlemmerbuurt", 13, 14, 15);
		addRoute(db, "Jordaan", 16, 17, 18);
		addRoute(db, "De Weteringschans", 19, 20, 21);
		addRoute(db, "Weesperbuurt", 22, 23, 24);
		addRoute(db, "Oostelijke Eilanden", 25, 26, 27);
		addRoute(db, "Kadijken", 28, 29, 30);
		addRoute(db, "Westelijkhavengebied", 31, 32, 33);
		addRoute(db, "Houthavens", 34, 35, 36);
		addRoute(db, "Zeeheldenbuurt", 37, 38, 39);
		addRoute(db, "Staatsliedenbuurt", 40, 41, 42);
		addRoute(db, "Frederik Hendrikbuurt", 43, 44, 45);
		addRoute(db, "Da Costabuurt", 46, 47, 48);
		addRoute(db, "Kinkerbuurt", 49, 50, 51);
		addRoute(db, "Van Lennepbuurt", 52, 53, 54);
		addRoute(db, "Helmersbuurt", 55, 56, 57);
		addRoute(db, "Vondelbuurt", 58, 59, 60);
		addRoute(db, "IndischeBuurt", 61, 62, 63);
		addRoute(db, "Oostelijk Havengebied", 64, 65, 66);
		addRoute(db, "Zeeburgereiland", 67, 68, 69);
		addRoute(db, "Ijburg", 70, 71, 72);
		addRoute(db, "Sloterdijk", 73, 74, 75);
		addRoute(db, "Erasmuspark", 76, 77, 78);
		addRoute(db, "De Kolenkit", 79, 80, 81);
		addRoute(db, "Van Galenbuurt", 82, 83, 84);
		addRoute(db, "Westindischebuurt", 85, 86, 87);
		addRoute(db, "IJpleinen Vogelbuurt", 88, 89, 90);
		addRoute(db, "Nieuwendam", 91, 92, 93);
		addRoute(db, "Tuindorp Oostzaan", 94, 95, 96);
		addRoute(db, "Kadoelen", 97, 98, 99);
		addRoute(db, "Buikslotermeer", 100, 101, 102);
		addRoute(db, "Slotermeer", 103, 104, 105);
		addRoute(db, "Geuzenveld", 106, 107, 108);
		addRoute(db, "Osdorp", 109, 110, 111);
		addRoute(db, "De Aker", 112, 113, 114);
		addRoute(db, "Slotervaart", 115, 116, 117);
		addRoute(db, "Overtoomse Veld", 118, 119, 120);
		addRoute(db, "Westlandgracht", 121, 122, 123);
		addRoute(db, "Bullewijk", 124, 125, 126);
		addRoute(db, "Bijlmer-Centrum", 127, 128, 129);
		addRoute(db, "Holendrecht", 130, 131, 132);
		addRoute(db, "Reigersbos", 133, 134, 135);
		addRoute(db, "Gein", 136, 137, 138);
		addRoute(db, "Weesperzijde", 139, 140, 141);
		addRoute(db, "Oosterparkbuurt", 142, 143, 144);
		addRoute(db, "Dapperbuurt", 145, 146, 147);
		addRoute(db, "Transvaalbuurt", 148, 149, 150);
		addRoute(db, "Frankendael", 151, 152, 153);
		addRoute(db, "Middenmeer", 156, 155, 154);
		addRoute(db, "Betondorp", 157, 158, 159);
		addRoute(db, "De Omval", 160, 161, 162);
		addRoute(db, "Oude Pijp", 163, 164, 165);
		addRoute(db, "Nieuwe Pijp", 166, 167, 168);
		addRoute(db, "Diamantbuurt", 169, 170, 171);
		addRoute(db, "Hoofddorppleinbuurt", 172, 173, 174);
		addRoute(db, "Schinkelbuurt", 175, 176, 177);
		addRoute(db, "Willemspark", 178, 179, 180);
		addRoute(db, "Museumkwartier", 181, 182, 183);
		addRoute(db, "Stadionbuurt", 184, 185, 186);
		addRoute(db, "Apollobuurt", 187, 188, 189);
		addRoute(db, "Scheldebuurt", 190, 191, 192);
		addRoute(db, "IJselbuurt", 193, 194, 195);
		addRoute(db, "Rijnbuurt", 196, 197, 198);
		addRoute(db, "Buitenveldert", 199, 200, 201);
	}

	public void addRoute(SQLiteDatabase db, String neighbourhood, int route_h,
			int route_v, int route_a) {
		ContentValues values = new ContentValues();
		values.put(Database.Route.COLUMN_NAME_ROUTE_H_ID, route_h);
		values.put(Database.Route.COLUMN_NAME_ROUTE_V_ID, route_v);
		values.put(Database.Route.COLUMN_NAME_ROUTE_A_ID, route_a);
		values.put(Database.Route.COLUMN_NAME_ROUTE_NEIGHBOURHOOD,
				neighbourhood);
		db.insert(Database.Route.TABLE_NAME, null, values);

	}

}
