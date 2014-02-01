package org.hva.cityrunner.sensei.sensors;

import java.util.ArrayList;

import nl.sense_os.service.constants.SenseDataTypes;

import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.R;
import org.hva.cityrunner.sensei.data.AccelData;
import org.hva.cityrunner.sensei.data.QueueData;
import org.hva.cityrunner.sensei.data.RouteRunData;
import org.hva.cityrunner.sensei.db.AccelDataSource;
import org.hva.cityrunner.sensei.db.QueueDataSource;
import org.hva.cityrunner.sensei.db.RouteRunDataSource;
import org.json.JSONArray;

import alice.util.Sleep;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class SenseiBackupService extends IntentService{

	private Intent intent;
	private OsmandApplication app;
	private String TAG = "SENSEI BACKGROUND";
	private int run_id;
	private QueueDataSource qds ;
	

	int limit = 300; //limit for accel and gyro
	
	public SenseiBackupService() {
		super("SenseiBackupService");
		// TODO Auto-generated constructor stub
	}
	
	public SenseiBackupService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		submitRun();
		
	}
	
	public void setRun_id(int run_id){
		this.run_id = run_id;
	}
	
	//submit run
	//haal runid op uit queue
	//kijk of run is afgelopen (als eindtijd gezet is, of als tijd meer dan 1 uur is)
	//tel acceldata, tel gyrodata, tel gpsdata
	//set alle data die nog verstuurd moet worden
	//submit run
	//wacht voor 1,5 minuut
	//als run gesubmit sugmit run acceldata, run gyrodata en run gps data
	private void submitRun(){
		
		app = ((OsmandApplication) getApplication());
		qds =app.getQueueDataSource();
		qds.open();
		QueueData qd;
		try{
			qd = qds.getLastQueue();
		}catch(IndexOutOfBoundsException e){
			//No items in queue 
			Log.e(TAG, e.toString());
			return;
		}
		qds.close();
		

		Log.v(TAG, "Submit run run_id"+qd.getRun_id());
		
		AccelDataSource ads = app.getAccelDataSource();
		ads.open();
		int accelcount = ads.getAllAccelCount(qd.getRun_id());
		qd.setAccelleft(accelcount);
		ads.close();
		//same for gyro and gps
		
		qds.open();
		qds.update(qd);
		qds.close();
		
		//submit simple rundata to sense
		insertData(qd.getRun_id());
}
	
	
	
	//submit accel
	int accelItterate = 0;
	private void submitAccel(final int run_id){
		Log.v(TAG, "Submit Acceldata for run_id"+run_id);
		accelItterate++;
		
		qds.open();
		QueueData qd = qds.getQueueByRun_id(run_id);
		qds.close();
		

		Log.d(TAG,"Acceldatapoints left: "+qd.getAccelleft());
		
		if(qd.getAccelleft() <= 0){
			qd.setAccelleft(0);
			qds.open();
			qds.updateAccelleft(qd.getId(), qd.getAccelleft());
			qds.close();
			accelItterate = 0;
			flushData();

			//go to gyro with delay
			
		}else if(qd.getAccelleft() < limit){
			senseInsertAccelerometerData(run_id, 0);
			qd.setAccelleft(0);
			qds.open();
			qds.updateAccelleft(qd.getId(), qd.getAccelleft());
			qds.close();
			accelItterate = 0;
			flushData();
			//go to gyro with delay
		}else{
			qd.setAccelleft(qd.getAccelleft()-limit);
			senseInsertAccelerometerData(run_id, qd.getAccelleft());
			qds.open();
			qds.updateAccelleft(qd.getId(), qd.getAccelleft());
			qds.close();
			
			if(accelItterate % 5 == 0){
				flushData();
				 try {
						Thread.sleep(90000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    submitAccel(run_id);
			}else{
					submitAccel(run_id);
			}
		}
		
	}
	
	//submit gyro
	
	//submit gps
	
	//submit emotie
	
//Sense functions
	
	//add simplerun data to sense
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
		
		final String value = "{\"runid\":\""+rrd.getRoute_id()+"\",\"startdatetime\":\""+rrd.getStart_datetime()+"\",\"enddatetime\":\""+rrd.getEnd_datetime()+"\",\"id\":\""+rrd.getId()
				+"\",\"phone_position\":\""+rrd.getPhone_position()
				+"\",\"headphones\":\""+(rrd.isHeadphones()?1:0)
				+"\",\"number_people\":\""+rrd.getNumber_people()
				+"\",\"remarks\":\""+rrd.getRemarks()
				+"\"}";
		
		//timestamp is van toevoeging
		final long timestamp = System.currentTimeMillis();// estTimestamp;

		// start new Thread to prevent NetworkOnMainThreadException
//		new Thread() {
//
//			@Override
//			public void run() {
				app.getSensePlatform().addDataPoint(sensorName, displayName,
						description, dataType, value, timestamp);
//			}
//		}.start();

		if(flushData()){
			QueueDataSource qds =app.getQueueDataSource();
			qds.open();
			qds.updateSubmitted(rrd.getId(), 1);
			qds.close();
		}
		
		 try {
			Thread.sleep(90000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    submitAccel(run_id);
	}
	
	//add acceldata to sense
	private void senseInsertAccelerometerData(int run_id, int offset){
		AccelDataSource ads = new AccelDataSource(this);
		ads.open();
		ArrayList<AccelData> accels = ads.getAllAccel(run_id, limit, offset);
		ads.close();
		//while(accels.size() > 0){
		
		Log.v("SENSE", "Insert data point for accelerometer");

		final String sensorName = "sensei_accelerometer";
		final String dataType = SenseDataTypes.JSON;
		final int current_run_id = run_id;
		

		JSONArray accelRun = new JSONArray();


		for(AccelData acc : accels){
			accelRun.put("{\"x\":\""+acc.getX()+"\",\"y\":\""+acc.getY()+"\",\"z\":\""+acc.getZ()+"\",\"run_id\":\""+current_run_id+"\",\"timestamp\":\""+acc.getTimestamp()+"\"}");
		}		
		
		
		final String accelRunString = accelRun.toString();
		new Thread() {

				@Override
				public void run() {
					app.getSensePlatform().addDataPoint(sensorName, sensorName,
							sensorName, dataType, 
							//"{\"x-axis\":\""+acc.getX()+"\",\"y-axis\":\""+acc.getY()+"\",\"z-axis\":\""+acc.getZ()+"\",\"run_id\":\""+current_run_id+"\",\"timestamp\":\""+acc.getTimestamp()+"\"}"
							accelRunString
							, System.currentTimeMillis());
				}
			}.start();
		//}
	}
	
	
	//add gyrodata to sense
	
	//add gpsdata tot sense
	
	//add emotie datat to sense
	
	//flushdata
	private boolean flushData() {
		Log.v("SENSE", "Flush buffers");
		boolean returnStatement = ((OsmandApplication) getApplication()).getSensePlatform().flushData();
		
		
		Log.d(TAG,getString(R.string.msg_flush_data));
		return returnStatement;
	}
	
	

}
