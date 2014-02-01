package org.hva.cityrunner.sensei.db;

import android.provider.BaseColumns;

public final class Database {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public Database() {
	}

	/* Inner class that defines the table contents */
	public static abstract class User implements BaseColumns {
		public static final String TABLE_NAME = "user";
		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_EMAIL = "email";
		public static final String COLUMN_NAME_PASSWORD = "password";
		public static final String COLUMN_NAME_TEAMID = "teamid";
		public static final String COLUMN_NAME_HEIGHT = "height";
		public static final String COLUMN_NAME_WEIGHT = "weight";
		public static final String COLUMN_NAME_AGE = "age";
		public static final String COLUMN_NAME_GENDER = "gender";
	}

	/* Inner class that defines the table contents */
	public static abstract class Location implements BaseColumns {
		public static final String TABLE_NAME = "location";
		public static final String COLUMN_NAME_RUN_ID = "runid";
		public static final String COLUMN_NAME_DATETIME = "datetime";
		public static final String COLUMN_NAME_LATITUDE = "latitude";
		public static final String COLUMN_NAME_LONGITUDE = "longitude";
	}

	/* Inner class that defines the table contents */
	public static abstract class Affect implements BaseColumns {
		public static final String TABLE_NAME = "affect";
		public static final String COLUMN_NAME_USER_ID = "userid";
		public static final String COLUMN_NAME_AFFECT_TOOL_ID = "affecttoolid";
		public static final String COLUMN_NAME_ROUTE_ID = "routeid";
		public static final String COLUMN_NAME_RUN_STATE = "runstate";
		public static final String COLUMN_NAME_DATETIME = "datetime";
		public static final String COLUMN_NAME_PLEASURE = "pleasure";
		public static final String COLUMN_NAME_DOMINANCE = "dominance";
		public static final String COLUMN_NAME_AROUSAL = "arousal";
	}

	/* Inner class that defines the table contents */
	public static abstract class AffectTool implements BaseColumns {
		public static final String TABLE_NAME = "affecttool";
		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_DESCRIPTION = "description";
	}

	/* Inner class that defines the table contents */
	public static abstract class MOVEMENT implements BaseColumns {
		public static final String TABLE_NAME = "movement";
		public static final String COLUMN_NAME_USER_ID = "userid";
		public static final String COLUMN_NAME_RUN_ID = "routeid";
		public static final String COLUMN_NAME_DATETIME = "datetime";
		public static final String COLUMN_NAME_ACCEL_X = "accelx";
		public static final String COLUMN_NAME_ACCEL_Y = "accely";
		public static final String COLUMN_NAME_ACCEL_Z = "accelz";
	}
	
	/* Inner class that defines the table contents */
	public static abstract class GYRO implements BaseColumns {
		public static final String TABLE_NAME = "gyroscope";
		public static final String COLUMN_NAME_RUN_ID = "routeid";
		public static final String COLUMN_NAME_DATETIME = "datetime";
		public static final String COLUMN_NAME_GYRO_X = "gyrox";
		public static final String COLUMN_NAME_GYRO_Y = "gyroy";
		public static final String COLUMN_NAME_GYRO_Z = "gyroz";
	}
	
	/* Inner class that defines the table contents */
	public static abstract class RouteRun implements BaseColumns {
		public static final String TABLE_NAME = "routerun";
		public static final String COLUMN_NAME_TEAM_ID = "teamid";
		public static final String COLUMN_NAME_ROUTE_ID = "routeid";
		public static final String COLUMN_NAME_START_DATETIME = "startdatetime";
		public static final String COLUMN_NAME_END_DATETIME = "enddatetime";
		public static final String COLUMN_NAME_PHONE_POSITION = "phone_position";
		public static final String COLUMN_NAME_HEADPHONES = "headphones";
		public static final String COLUMN_NAME_NUMBER_PEOPLE = "number_people";
		public static final String COLUMN_NAME_REMARKS = "remarks";
	}
	
	/* Inner class that defines the table contents */
	public static abstract class Route implements BaseColumns {
		public static final String TABLE_NAME = "route";
		public static final String COLUMN_NAME_ROUTE_NEIGHBOURHOOD = "neighbourhood";
		public static final String COLUMN_NAME_ROUTE_H_ID = "routehid";
		public static final String COLUMN_NAME_ROUTE_V_ID = "routevid";
		public static final String COLUMN_NAME_ROUTE_A_ID = "routeaid";
	}
	
	/* Inner class that defines the table contents */
	public static abstract class Queue implements BaseColumns {
		public static final String TABLE_NAME = "queue";
		public static final String COLUMN_NAME_RUN_ID = "run_id";
		public static final String COLUMN_NAME_SUBMITTED = "submitted";
		public static final String COLUMN_NAME_ACCELLEFT = "accelleft";
		public static final String COLUMN_NAME_GYROLEFT = "gyroleft";
		public static final String COLUMN_NAME_GPSLEFT = "gpsleft";
		public static final String COLUMN_NAME_EMOTIE = "emotie";
		public static final String COLUMN_NAME_FINISHED = "finished";
		
		
	}
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String REAL_TYPE = " REAL";

	private static final String COMMA_SEP = ",";

	public static final String SQL_CREATE_USER = "CREATE TABLE "
			+ User.TABLE_NAME + " (" + User._ID + " INTEGER PRIMARY KEY,"
			+ User.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP
			+ User.COLUMN_NAME_EMAIL + TEXT_TYPE + COMMA_SEP
			+ User.COLUMN_NAME_PASSWORD + TEXT_TYPE + COMMA_SEP
			+ User.COLUMN_NAME_TEAMID + TEXT_TYPE + COMMA_SEP
			+ User.COLUMN_NAME_HEIGHT + TEXT_TYPE + COMMA_SEP
			+ User.COLUMN_NAME_WEIGHT + TEXT_TYPE + COMMA_SEP
			+ User.COLUMN_NAME_AGE + TEXT_TYPE + COMMA_SEP
			+ User.COLUMN_NAME_GENDER + TEXT_TYPE +" )";
	public static final String SQL_DELETE_USER = "DROP TABLE IF EXISTS "
			+ User.TABLE_NAME;

	public static final String SQL_CREATE_AFFECT = "CREATE TABLE "
			+ Affect.TABLE_NAME + " (" + Affect._ID + " INTEGER PRIMARY KEY,"
			+ Affect.COLUMN_NAME_USER_ID + INTEGER_TYPE + COMMA_SEP
			+ Affect.COLUMN_NAME_AFFECT_TOOL_ID + INTEGER_TYPE + COMMA_SEP
			+ Affect.COLUMN_NAME_ROUTE_ID+ INTEGER_TYPE + COMMA_SEP
			+ Affect.COLUMN_NAME_RUN_STATE + INTEGER_TYPE + COMMA_SEP
			+ Affect.COLUMN_NAME_DATETIME + TEXT_TYPE + COMMA_SEP
			+ Affect.COLUMN_NAME_PLEASURE + REAL_TYPE + COMMA_SEP
			+ Affect.COLUMN_NAME_DOMINANCE + REAL_TYPE + COMMA_SEP
			+ Affect.COLUMN_NAME_AROUSAL + REAL_TYPE + " )";
	public static final String SQL_DELETE_AFFECT = "DROP TABLE IF EXISTS "
			+ Affect.TABLE_NAME;

	public static final String SQL_CREATE_AFFECT_TOOL = "CREATE TABLE "
			+ AffectTool.TABLE_NAME + " (" + AffectTool._ID+ " INTEGER PRIMARY KEY," 
			+ AffectTool.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP 
			+ AffectTool.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + " )";
	
	public static final String SQL_DELETE_AFFECT_TOOL = "DROP TABLE IF EXISTS "
			+ AffectTool.TABLE_NAME;
	
	public static final String SQL_CREATE_MOVEMENT = "CREATE TABLE "
			+ MOVEMENT.TABLE_NAME + " (" + MOVEMENT._ID+ " INTEGER PRIMARY KEY," 
			+ MOVEMENT.COLUMN_NAME_USER_ID + TEXT_TYPE + COMMA_SEP 
			+ MOVEMENT.COLUMN_NAME_RUN_ID + TEXT_TYPE + COMMA_SEP
			+ MOVEMENT.COLUMN_NAME_DATETIME + TEXT_TYPE + COMMA_SEP 
			+ MOVEMENT.COLUMN_NAME_ACCEL_X + REAL_TYPE + COMMA_SEP 
			+ MOVEMENT.COLUMN_NAME_ACCEL_Y + REAL_TYPE + COMMA_SEP 
			+ MOVEMENT.COLUMN_NAME_ACCEL_Z + REAL_TYPE + " )";
	public static final String SQL_DELETE_MOVEMENT = "DROP TABLE IF EXISTS "
			+ MOVEMENT.TABLE_NAME;
	
	public static final String SQL_CREATE_GYRO = "CREATE TABLE "
			+ GYRO.TABLE_NAME + " (" + GYRO._ID+ " INTEGER PRIMARY KEY," 
			+ GYRO.COLUMN_NAME_RUN_ID + TEXT_TYPE + COMMA_SEP
			+ GYRO.COLUMN_NAME_DATETIME + TEXT_TYPE + COMMA_SEP 
			+ GYRO.COLUMN_NAME_GYRO_X + REAL_TYPE + COMMA_SEP 
			+ GYRO.COLUMN_NAME_GYRO_Y + REAL_TYPE + COMMA_SEP 
			+ GYRO.COLUMN_NAME_GYRO_Z + REAL_TYPE + " )";
	public static final String SQL_DELETE_GYRO = "DROP TABLE IF EXISTS "
			+ GYRO.TABLE_NAME;
	
	public static final String SQL_CREATE_ROUTE_RUN = "CREATE TABLE "
			+ RouteRun.TABLE_NAME + " (" + RouteRun._ID+ " INTEGER PRIMARY KEY," 
			+ RouteRun.COLUMN_NAME_TEAM_ID + TEXT_TYPE + COMMA_SEP 
			+ RouteRun.COLUMN_NAME_ROUTE_ID + TEXT_TYPE + COMMA_SEP 
			+ RouteRun.COLUMN_NAME_START_DATETIME + TEXT_TYPE + COMMA_SEP 
			+ RouteRun.COLUMN_NAME_END_DATETIME + TEXT_TYPE + COMMA_SEP 
			+ RouteRun.COLUMN_NAME_PHONE_POSITION + TEXT_TYPE+ COMMA_SEP 
			+ RouteRun.COLUMN_NAME_HEADPHONES + TEXT_TYPE+ COMMA_SEP 
			+ RouteRun.COLUMN_NAME_NUMBER_PEOPLE + TEXT_TYPE+ COMMA_SEP 
			+ RouteRun.COLUMN_NAME_REMARKS + TEXT_TYPE+ " )";
	
	public static final String SQL_DELETE_ROUTE_RUN = "DROP TABLE IF EXISTS "
			+ RouteRun.TABLE_NAME;
	
	public static final String SQL_CREATE_ROUTE = "CREATE TABLE "
			+ Route.TABLE_NAME + " (" + Route._ID+ " INTEGER PRIMARY KEY," 
			+ Route.COLUMN_NAME_ROUTE_NEIGHBOURHOOD + TEXT_TYPE + COMMA_SEP 
			+ Route.COLUMN_NAME_ROUTE_H_ID + TEXT_TYPE + COMMA_SEP 
			+ Route.COLUMN_NAME_ROUTE_V_ID + TEXT_TYPE + COMMA_SEP 
			+ Route.COLUMN_NAME_ROUTE_A_ID + TEXT_TYPE + " )";
	
	public static final String SQL_DELETE_ROUTE = "DROP TABLE IF EXISTS "
			+ Route.TABLE_NAME;

	public static final String SQL_CREATE_LOCATION = "CREATE TABLE "
			+ Location.TABLE_NAME + " (" + Location._ID+ " INTEGER PRIMARY KEY," 
			+ Location.COLUMN_NAME_RUN_ID + TEXT_TYPE + COMMA_SEP 
			+ Location.COLUMN_NAME_DATETIME+ TEXT_TYPE + COMMA_SEP 
			+ Location.COLUMN_NAME_LATITUDE + TEXT_TYPE + COMMA_SEP 
			+ Location.COLUMN_NAME_LONGITUDE + TEXT_TYPE + " )";
	
	public static final String SQL_DELETE_LOCATION = "DROP TABLE IF EXISTS "
			+ Location.TABLE_NAME;
	
	public static final String SQL_CREATE_QUEUE = "CREATE TABLE "
			+ Queue.TABLE_NAME + " (" + Queue._ID+ " INTEGER PRIMARY KEY," 
			+ Queue.COLUMN_NAME_RUN_ID + TEXT_TYPE + COMMA_SEP 
			+ Queue.COLUMN_NAME_SUBMITTED+ TEXT_TYPE+ COMMA_SEP 
			+ Queue.COLUMN_NAME_ACCELLEFT+ TEXT_TYPE+ COMMA_SEP 
			+ Queue.COLUMN_NAME_GYROLEFT+ TEXT_TYPE+ COMMA_SEP 
			+ Queue.COLUMN_NAME_GPSLEFT+ TEXT_TYPE+ COMMA_SEP 
			+ Queue.COLUMN_NAME_EMOTIE+ TEXT_TYPE+ COMMA_SEP 
			+ Queue.COLUMN_NAME_FINISHED+ TEXT_TYPE+  " )";
	
	public static final String SQL_DELETE_QUEUE = "DROP TABLE IF EXISTS "
			+ Queue.TABLE_NAME;

	
}
