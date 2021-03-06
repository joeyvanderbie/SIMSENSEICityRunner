package org.hva.cityrunner.sensei.sensors;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import nl.sense_os.service.constants.SenseDataTypes;

import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.R;
import org.hva.cityrunner.plus.activities.MainMenuActivity;
import org.hva.cityrunner.sensei.data.AccelData;
import org.hva.cityrunner.sensei.data.GyroData;
import org.hva.cityrunner.sensei.data.LocationData;
import org.hva.cityrunner.sensei.data.QueueData;
import org.hva.cityrunner.sensei.data.RouteRunData;
import org.hva.cityrunner.sensei.data.UserData;
import org.hva.cityrunner.sensei.db.AccelDataSource;
import org.hva.cityrunner.sensei.db.DatabaseHelper;
import org.hva.cityrunner.sensei.db.GyroDataSource;
import org.hva.cityrunner.sensei.db.LocationDataSource;
import org.hva.cityrunner.sensei.db.QueueDataSource;
import org.hva.cityrunner.sensei.db.RouteRunDataSource;
import org.hva.cityrunner.sensei.db.UserDataSource;
import org.json.JSONArray;

import android.app.IntentService;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class SenseiBackupService extends IntentService {

	private Intent intent;
	private OsmandApplication app;
	private String TAG = "SENSEI BACKGROUND";
	private QueueDataSource qds;
	private int sleepTime = 80000; // 80 seconden ipv 60 voor de zekerheid
	static final int UPLOAD_NOTIFICATION = 398300;

	private static final String BACKUPSETTINGS = "backupsettings";
	private static final String FULL_SUBMIT = "fullsensesubmit";

	int limit = 500; // limit for accel and gyro
	int packages = 50;
	private ArrayList<String> reUpload = new ArrayList<String>();
	private ArrayList<String> uploadData = new ArrayList<String>();

	public SenseiBackupService() {
		super("SenseiBackupService");
	}

	public SenseiBackupService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			submitRun();
		} catch (OutOfMemoryError e) {
			// final SharedPreferences preferences =
			// this.getSharedPreferences(BACKUPSETTINGS,
			// Activity.MODE_PRIVATE);
			// preferences.edit().putBoolean(FULL_SUBMIT, false);
			Log.e(TAG, "Using to much memory, will only submit SIM data");
			Log.e(TAG, e.toString());
		}

		// Backup Science data
		String backupLocation = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/osmand/backup"
				+ System.currentTimeMillis() + ".zip";
		uploadData.add(backupLocation);
		makeZip mz = new makeZip(backupLocation);
		mz.addZipFile(getDatabasePath(DatabaseHelper.DATABASE_NAME)
				.getAbsolutePath());
		mz.closeZip();

		// This code is temporary until
		// Remove the datapoints to save space
		DatabaseHelper dbHelper = new DatabaseHelper(SenseiBackupService.this);
		dbHelper.doSaveDelete(dbHelper.getWritableDatabase());

		new Upload().execute();

	}

	// submit run
	// haal runid op uit queue
	// kijk of run is afgelopen (als eindtijd gezet is, of als tijd meer dan 1
	// uur is)
	// tel acceldata, tel gyrodata, tel gpsdata
	// set alle data die nog verstuurd moet worden
	// submit run
	// wacht voor 1,5 minuut
	// als run gesubmit sugmit run acceldata, run gyrodata en run gps data
	private void submitRun() {

		app = ((OsmandApplication) getApplication());
		qds = app.getQueueDataSource();
		qds.open();
		QueueData qd;
		try {
			qd = qds.getLastQueue();
		} catch (IndexOutOfBoundsException e) {
			// No items in queue
			Log.e(TAG, e.toString());
			return;
		}
		qds.close();

		Log.v(TAG, "Submit run run_id" + qd.getRun_id());

//		AccelDataSource ads = app.getAccelDataSource();
//		ads.open();
//		int accelcount = ads.getAllAccelCount(qd.getRun_id());
//		qd.setAccelleft(accelcount);
//		ads.close();
//
//		// same for gyro and gps
//		GyroDataSource gds = app.getGyroDataSource();
//		gds.open();
//		int gyrocount = gds.getAllGyroCount(qd.getRun_id());
//		qd.setGyroleft(gyrocount);
//		gds.close();
//
//		LocationDataSource lds = app.getLocationDataSource();
//		lds.open();
//		int gpscount = lds.getAllGpsCount(qd.getRun_id());
//		qd.setGpsleft(gpscount);
//		lds.close();

		qds.open();
		qds.update(qd);
		qds.close();

		// submit simple rundata to sense
		insertData(qd.getRun_id());
	}

	// submit accel
	int accelItterate = 0;

	private void submitAccel(final int run_id) {
		Log.v(TAG, "Submit Acceldata for run_id" + run_id);
		accelItterate++;

		qds.open();
		QueueData qd = qds.getQueueByRun_id(run_id);
		qds.close();

		Log.d(TAG, "Acceldatapoints left: " + qd.getAccelleft());

		if (qd.getAccelleft() <= 0) {
			qd.setAccelleft(0);
			qds.open();
			qds.updateAccelleft(qd.getId(), qd.getAccelleft());
			qds.close();
			accelItterate = 0;
			if (flushData()) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				submitGyro(run_id);
			} else {
				Log.e(TAG, "Sending of data via Sense failed, stop sending");
			}

		} else if (qd.getAccelleft() < limit) {
			senseInsertAccelerometerData(run_id, 0);
			qd.setAccelleft(0);
			qds.open();
			qds.updateAccelleft(qd.getId(), qd.getAccelleft());
			qds.close();
			accelItterate = 0;
			if (flushData()) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				submitGyro(run_id);
			} else {
				Log.e(TAG, "Sending of data via Sense failed, stop sending");
			}

		} else {
			qd.setAccelleft(qd.getAccelleft() - limit);
			senseInsertAccelerometerData(run_id, qd.getAccelleft());
			qds.open();
			qds.updateAccelleft(qd.getId(), qd.getAccelleft());
			qds.close();

			if (accelItterate % packages == 0) { // 50 * limit (500) = 25000
				if (flushData()) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					submitAccel(run_id);
				} else {
					Log.e(TAG, "Sending of data via Sense failed, stop sending");
				}
			} else {
				submitAccel(run_id);
			}
		}

	}

	// submit gyro
	int gyroItterate = 0;

	private void submitGyro(final int run_id) {
		Log.v(TAG, "Submit Gyrodata for run_id" + run_id);
		gyroItterate++;

		qds.open();
		QueueData qd = qds.getQueueByRun_id(run_id);
		qds.close();

		Log.d(TAG, "Gyrodatapoints left: " + qd.getGyroleft());

		if (qd.getGyroleft() <= 0) {
			qd.setGyroleft(0);
			qds.open();
			qds.updateGyroleft(qd.getId(), qd.getGyroleft());
			qds.close();
			gyroItterate = 0;
			if (flushData()) {
				// go to gps with delay
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				submitGps(run_id);
			} else {
				Log.e(TAG, "Sending of data via Sense failed, stop sending");
			}

		} else if (qd.getGyroleft() < limit) {
			senseInsertGyroData(run_id, 0);
			qd.setGyroleft(0);
			qds.open();
			qds.updateGyroleft(qd.getId(), qd.getGyroleft());
			qds.close();
			gyroItterate = 0;
			if (flushData()) {
				// go to gps with delay
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				submitGps(run_id);
			} else {
				Log.e(TAG, "Sending of data via Sense failed, stop sending");
			}

		} else {
			qd.setGyroleft(qd.getGyroleft() - limit);
			senseInsertGyroData(run_id, qd.getGyroleft());
			qds.open();
			qds.updateGyroleft(qd.getId(), qd.getGyroleft());
			qds.close();

			if (gyroItterate % packages == 0) { // 50 * limit (500) = 25000
				if (flushData()) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					submitGyro(run_id);
				} else {
					Log.e(TAG, "Sending of data via Sense failed, stop sending");
				}
			} else {
				submitGyro(run_id);
			}
		}

	}

	// submit gps

	int locationItterate = 0;

	private void submitGps(final int run_id) {
		// submit gps
		Log.v(TAG, "Submit Locationdata for run_id" + run_id);
		locationItterate++;

		qds.open();
		QueueData qd = qds.getQueueByRun_id(run_id);
		qds.close();

		Log.d(TAG, "Locationdatapoints left: " + qd.getGpsleft());

		if (qd.getGpsleft() <= 0) {
			qd.setGpsleft(0);
			qds.open();
			qds.updateGpsleft(qd.getId(), qd.getGpsleft());
			qds.close();
			locationItterate = 0;
			if (flushData()) {
				// set run_id on finished
				setSubmitFinsihed(run_id);
			} else {
				Log.e(TAG, "Sending of data via Sense failed, stop sending");
			}

		} else if (qd.getGpsleft() < limit) {
			senseInsertGpsData(run_id, 0);
			qd.setGpsleft(0);
			qds.open();
			qds.updateGpsleft(qd.getId(), qd.getGpsleft());
			qds.close();
			locationItterate = 0;
			if (flushData()) {
				// set run_id on finished
				setSubmitFinsihed(run_id);
			} else {
				Log.e(TAG, "Sending of data via Sense failed, stop sending");
			}

		} else {
			qd.setGpsleft(qd.getGpsleft() - limit);
			senseInsertGpsData(run_id, qd.getGpsleft());
			qds.open();
			qds.updateGpsleft(qd.getId(), qd.getGpsleft());
			qds.close();

			if (locationItterate % packages == 0) { // 20 * limit (500) = 10000
				if (flushData()) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					submitGps(run_id);
				} else {
					Log.e(TAG, "Sending of data via Sense failed, stop sending");
				}
			} else {
				submitGps(run_id);
			}
		}

	}

	private void setSubmitFinsihed(int run_id) {
		qds.open();
		qds.updateFinished(run_id, 1);
		qds.close();
	}

	// submit emotie
	// hoeft niet, gaat automatisch

	// Sense functions

	// add simplerun data to sense
	private void insertData(final int run_id) {
		Log.v("SENSE", "Insert data point");

		final String sensorName = "runrecord";
		final String displayName = "runrecord";
		final String dataType = SenseDataTypes.JSON;
		final String description = sensorName;

		RouteRunDataSource rrds = app.getRouteRunDataSource();
		rrds.open();
		RouteRunData rrd = rrds.getRouteRun(run_id);
		rrds.close();

		UserDataSource uds = app.getUserDataSource();
		uds.open();
		UserData ud = uds.getUserData();
		uds.close();

		final String value = "{\"runid\":\"" + rrd.getRoute_id()
				+ "\",\"startdatetime\":\"" + rrd.getStart_datetime()
				+ "\",\"enddatetime\":\"" + rrd.getEnd_datetime()
				+ "\",\"id\":\"" + rrd.getId() + "\",\"phone_position\":\""
				+ rrd.getPhone_position() + "\",\"headphones\":\""
				+ (rrd.isHeadphones() ? 1 : 0) + "\",\"number_people\":\""
				+ rrd.getNumber_people() + "\",\"remarks\":\""
				+ rrd.getRemarks() + "\",\"team\":\"" + ud.getTeamid()
				+ "\",\"age\":\"" + ud.getAge() + "\",\"weight\":\""
				+ ud.getWeight() + "\",\"height\":\"" + ud.getHeight() + "\"}";

		// timestamp is van toevoeging
		final long timestamp = System.currentTimeMillis();// estTimestamp;

		// start new Thread to prevent NetworkOnMainThreadException
		// new Thread() {
		//
		// @Override
		// public void run() {
		app.getSensePlatform().addDataPoint(sensorName, displayName,
				description, dataType, value, timestamp);
		// }
		// }.start();

		if (flushData()) {
			QueueDataSource qds = app.getQueueDataSource();
			qds.open();
			qds.updateSubmitted(rrd.getId(), 1);
			qds.close();

			// try {
			// Thread.sleep(sleepTime);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//

			// final SharedPreferences preferences =
			// this.getSharedPreferences(BACKUPSETTINGS,
			// Activity.MODE_PRIVATE);
			// if(preferences.getBoolean(FULL_SUBMIT, true)){
			// submitAccel(run_id);
			// }else{
			setSubmitFinsihed(run_id);
			// }
		} else {
			Log.e(TAG, "Sending of data via Sense failed, stop sending");
		}
	}

	// add acceldata to sense
	private void senseInsertAccelerometerData(int run_id, int offset) {
		AccelDataSource ads = app.getAccelDataSource();
		ads.open();
		ArrayList<AccelData> accels = ads.getAllAccel(run_id, limit, offset);
		ads.close();

		Log.v("SENSE", "Insert data point for accelerometer");

		final String sensorName = "sensei_accelerometer";
		final String dataType = SenseDataTypes.JSON;
		final int current_run_id = run_id;

		JSONArray accelRun = new JSONArray();

		for (AccelData acc : accels) {
			accelRun.put("{x:" + acc.getX() + ",y:" + acc.getY() + ",z:"
					+ acc.getZ() + ",r:" + current_run_id + ",t:"
					+ acc.getTimestamp() + "}");
		}

		final String accelRunString = accelRun.toString();
		new Thread() {

			@Override
			public void run() {
				app.getSensePlatform().addDataPoint(sensorName, sensorName,
						sensorName, dataType, accelRunString,
						System.currentTimeMillis());
			}
		}.start();
	}

	// add gyrodata to sense
	private void senseInsertGyroData(int run_id, int offset) {
		GyroDataSource ads = app.getGyroDataSource();
		ads.open();
		ArrayList<GyroData> accels = ads.getAllGyro(run_id, limit, offset);
		ads.close();

		Log.v("SENSE", "Insert data point for gyrometer");

		final String sensorName = "sensei_gyrometer";
		final String dataType = SenseDataTypes.JSON;
		final int current_run_id = run_id;

		JSONArray accelRun = new JSONArray();

		for (GyroData acc : accels) {
			accelRun.put("{x:" + acc.getX() + ",y:" + acc.getY() + ",z:"
					+ acc.getZ() + ",r:" + current_run_id + ",t:"
					+ acc.getTimestamp() + "}");
		}

		final String accelRunString = accelRun.toString();
		new Thread() {

			@Override
			public void run() {
				app.getSensePlatform().addDataPoint(sensorName, sensorName,
						sensorName, dataType, accelRunString,
						System.currentTimeMillis());
			}
		}.start();
	}

	// add gpsdata tot sense
	private void senseInsertGpsData(int run_id, int offset) {
		LocationDataSource ads = app.getLocationDataSource();
		ads.open();
		ArrayList<LocationData> accels = ads.getAllLocation(run_id, limit,
				offset);
		ads.close();

		Log.v("SENSE", "Insert data point for location");

		final String sensorName = "sensei_location";
		final String dataType = SenseDataTypes.JSON;
		final int current_run_id = run_id;

		JSONArray accelRun = new JSONArray();

		for (LocationData acc : accels) {
			accelRun.put("{lat:" + acc.getLatitude() + ",lon:"
					+ acc.getLongitude() + ",r:" + current_run_id + ",t:"
					+ acc.getTime() + "}");
		}

		final String accelRunString = accelRun.toString();
		new Thread() {

			@Override
			public void run() {
				app.getSensePlatform().addDataPoint(sensorName, sensorName,
						sensorName, dataType, accelRunString,
						System.currentTimeMillis());
			}
		}.start();
	}

	// add emotie datat to sense
	// hoeft niet gaat automatisch

	// flushdata
	private boolean flushData() {
		Log.v("SENSE", "Flush buffers");
		boolean returnStatement = ((OsmandApplication) getApplication())
				.getSensePlatform().flushData();

		Log.d(TAG, getString(R.string.msg_flush_data));
		return returnStatement;
	}

	public class makeZip {
		static final int BUFFER = 2048;

		ZipOutputStream out;
		byte data[];

		public makeZip(String name) {
			FileOutputStream dest = null;
			try {
				dest = new FileOutputStream(name);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			out = new ZipOutputStream(new BufferedOutputStream(dest));
			data = new byte[BUFFER];
		}

		public void addZipFile(String name) {
			Log.v("addFile", "Adding: ");
			FileInputStream fi = null;
			try {
				fi = new FileInputStream(name);
				Log.v("addFile", "Adding: ");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("atch", "Adding: ");
			}
			BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(name);
			try {
				out.putNextEntry(entry);
				Log.v("put", "Adding: ");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int count;
			try {
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
					// Log.v("Write", "Adding: "+origin.read(data, 0, BUFFER));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("catch", "Adding: ");
			}
			try {
				origin.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void closeZip() {
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	ProgressDialog mProgressDialog;

	private class Upload extends AsyncTask<Void, Void, Void> {

		@SuppressWarnings("deprecation")
		protected void onPreExecute() {
			

			Intent intent = new Intent(SenseiBackupService.this,
					MainMenuActivity.class);
			PendingIntent pIntent = PendingIntent.getActivity(
					SenseiBackupService.this, 0, intent, 0);

			// Build notification
			// Actions are just fake
			Builder notificationBuilder = new Notification.Builder(
					SenseiBackupService.this)
					.setContentTitle("SIM SENSEI City Runner")
					.setContentText(
							"Uploading movement data to the scientists")
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentIntent(pIntent);
			 Notification noti;
			if(android.os.Build.VERSION.SDK_INT >= 16){
				//android version 16 and up
				 noti  = notificationBuilder.build();
			}else{
				//android versions below 16
				noti  =  notificationBuilder.getNotification();
			}
				
				
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			// hide the notification after its selected
			noti.flags |= Notification.FLAG_AUTO_CANCEL;

			notificationManager.notify(UPLOAD_NOTIFICATION, noti);
		}

		@Override
		protected Void doInBackground(Void... params) {
			//User foreach loop to upload everyBackup there is to backup
			for (String pathToOurFile : uploadData) {
				
				HttpURLConnection connection = null;
				DataOutputStream outputStream = null;
				DataInputStream inputStream = null;

				// String pathToOurFile = backupLocation;
				String urlServer = "http://95.85.23.66/upload/upload.php";
				String lineEnd = "\r\n";
				String twoHyphens = "--";
				String boundary = "*****";

				int bytesRead, bytesAvailable, bufferSize;
				byte[] buffer;
				int maxBufferSize = 1 * 1024 * 1024;

				try {
					FileInputStream fileInputStream = new FileInputStream(
							new File(pathToOurFile));

					URL url = new URL(urlServer);
					connection = (HttpURLConnection) url.openConnection();

					// Allow Inputs & Outputs
					connection.setDoInput(true);
					connection.setDoOutput(true);
					connection.setUseCaches(false);

					// Enable POST method
					connection.setRequestMethod("POST");

					connection.setRequestProperty("Connection", "Keep-Alive");
					connection.setRequestProperty("Content-Type",
							"multipart/form-data;boundary=" + boundary);

					outputStream = new DataOutputStream(
							connection.getOutputStream());
					outputStream.writeBytes(twoHyphens + boundary + lineEnd);
					outputStream
							.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
									+ pathToOurFile + "\"" + lineEnd);
					outputStream.writeBytes(lineEnd);

					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					buffer = new byte[bufferSize];

					// Read file
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

					while (bytesRead > 0) {
						outputStream.write(buffer, 0, bufferSize);
						bytesAvailable = fileInputStream.available();
						bufferSize = Math.min(bytesAvailable, maxBufferSize);
						bytesRead = fileInputStream.read(buffer, 0, bufferSize);
					}

					outputStream.writeBytes(lineEnd);
					outputStream.writeBytes(twoHyphens + boundary + twoHyphens
							+ lineEnd);

					// Responses from the server (code and message)
					int serverResponseCode = connection.getResponseCode();
					String serverResponseMessage = connection
							.getResponseMessage();

					Log.v(TAG, "Server response code " + serverResponseCode);
					Log.v(TAG, "Server response message "
							+ serverResponseMessage);
					if (serverResponseCode == 200) {
						//uploadData.remove(pathToOurFile);
						reUpload.remove(pathToOurFile);
					} else {
						// upload failed, retrigger upload of db.
						reUpload.add(pathToOurFile);
					}

					fileInputStream.close();
					outputStream.flush();
					outputStream.close();
				} catch (Exception ex) {
					// Exception handling
				}
			}
			uploadData = reUpload;
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.cancel(UPLOAD_NOTIFICATION);
			return null;
		}
	}

	protected void onPostExecute(Void result) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(UPLOAD_NOTIFICATION);
	}

}
