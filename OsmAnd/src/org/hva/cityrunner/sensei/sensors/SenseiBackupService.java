package org.hva.cityrunner.sensei.sensors;

import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.sensei.data.QueueData;
import org.hva.cityrunner.sensei.db.AccelDataSource;
import org.hva.cityrunner.sensei.db.QueueDataSource;

import android.app.IntentService;
import android.content.Intent;

public class SenseiBackupService extends IntentService{

	private Intent intent;
	
	public SenseiBackupService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
	}
	
	//submit run
	//haal runid op uit queue
	//kijk of run is afgelopen (als eindtijd gezet is, of als tijd meer dan 1 uur is)
	//tel acceldata, tel gyrodata, tel gpsdata
	//set alle data die nog verstuurd moet worden
	//submit run
	//wacht voor 1,5 minuut
	//als run gesubmit sugmit run acceldata, run gyrodata en run gps data
	private void submitRun(int run_id){
		final OsmandApplication app = ((OsmandApplication) getApplication());
		QueueDataSource qds =app.getQueueDataSource();
		qds.open();
		QueueData qd = qds.getLastQueue();
		qds.close();
		
		AccelDataSource ads = app.getAccelDataSource();
		ads.open();
		qd.setAccelleft(ads.getAllAccelCount(run_id));
		ads.close();
		
		//same for gyro and gps
		
}
	
	
	
	//submit accel
	
	//submit gyro
	
	//submit gps
	
	//submit emotie
	

}
