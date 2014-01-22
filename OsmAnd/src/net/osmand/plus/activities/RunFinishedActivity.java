package net.osmand.plus.activities;

import java.util.ArrayList;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.sensei.data.AccelData;
import net.osmand.sensei.data.AffectData;
import net.osmand.sensei.data.RouteRunData;
import net.osmand.sensei.db.AccelDataSource;
import net.osmand.sensei.db.AffectDataSource;
import net.osmand.sensei.db.RouteRunDataSource;
import net.osmand.sensei.sensors.StaticAffectButton;
import nl.sense_os.service.constants.SenseDataTypes;

import org.json.JSONArray;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.askcs.android.affectbutton.Affect;

public class RunFinishedActivity extends SherlockActivity {
	
	private OsmandApplication app;
	private int run_id;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		((OsmandApplication) getApplication()).applyTheme(this);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.run_finished_title);
		// R.drawable.tab_settings_screen_icon
	
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.run_finished);

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	    	Intent mainmenu = new Intent(this,
					OsmandIntents.getMainMenuActivity());
	    	mainmenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			
	        NavUtils.navigateUpTo(this, mainmenu);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	public OsmandApplication getMyApplication() {
		return ((OsmandApplication) getApplication());
	}
	
	@Override
	public void onResume(){
		super.onResume();
		updateScreen();
	}
	
	//something is not ok here, using both getLastRouteRun and app.currentRouteRun as route indicator
	private void updateScreen(){
		app = getMyApplication();
		RouteRunDataSource rrds = app.getRouteRunDataSource();
		rrds.open();
		RouteRunData rrd = rrds.getLastRouteRun();
		rrds.close();
		
		TextView duration = (TextView) findViewById(R.id.time_info);
		duration.setText(""+((rrd.getEnd_datetime() - rrd.getStart_datetime()) / 1000 / 60));
		
		TextView routeNr = (TextView) findViewById(R.id.routeNumber);
		routeNr.setText(""+rrd.getRoute_id());
		
		run_id = rrd.getId();
		
		StaticAffectButton abStart = (StaticAffectButton) this.findViewById(R.id.affect_start);
		StaticAffectButton abEnd = (StaticAffectButton) this.findViewById(R.id.affect_end);

		
		AffectDataSource afd = new AffectDataSource(this);
		afd.open();
		ArrayList<AffectData> afbd = afd.getAffects(run_id);
		afd.close();
		if(afbd.size() == 2){
			if(afbd.get(0).getRunstate() == 0){
				abStart.setPAD(afbd.get(0).getSimpleAffect());
				abEnd.setPAD(afbd.get(1).getSimpleAffect());
			}else{
				abStart.setPAD(afbd.get(1).getSimpleAffect());
				abEnd.setPAD(afbd.get(0).getSimpleAffect());
			}
		}else{
			RelativeLayout afo = (RelativeLayout) this.findViewById(R.id.affect_overview);
			afo.setVisibility(View.GONE);
		}
		
		
		//if(app.currentRouteRun != null){
			uploadDataToSense();
		//}
	}
	
	private void uploadDataToSense(){
		insertData();//test sense data upload
		senseInsertAccelerometerData();
		flushData();
	}
	
	private void flushData() {
		Log.v("SENSE", "Flush buffers");
		if(getMyApplication().getSensePlatform().flushData()){
			//is upload succesfull, clear current run information
			app.currentRouteRun = null;
		}
		
		showToast(R.string.msg_flush_data);
	}
	
	private void senseInsertAccelerometerData(){
		AccelDataSource ads = new AccelDataSource(this);
		ads.open();
		ArrayList<AccelData> accels = ads.getAllAccel(run_id);
		
		Log.v("SENSE", "Insert data point for accelerometer");

		final String sensorName = "sensei_accelerometer";
		final String dataType = SenseDataTypes.JSON;
		final int current_run_id = run_id;
		

		JSONArray accelRun = new JSONArray();

		showToast(R.string.msg_sent_data, "Acceldatapoints: "+accels.size());
		int a = 0;
		for(AccelData acc : accels){
			//submit each 1000 items
			if(a % 999 == 0){
				final String accelRunString = accelRun.toString();
				 accelRun = new JSONArray();
				
				new Thread() {

						@Override
						public void run() {
							getMyApplication().getSensePlatform().addDataPoint(sensorName, sensorName,
									sensorName, dataType, 
									//"{\"x-axis\":\""+acc.getX()+"\",\"y-axis\":\""+acc.getY()+"\",\"z-axis\":\""+acc.getZ()+"\",\"run_id\":\""+current_run_id+"\",\"timestamp\":\""+acc.getTimestamp()+"\"}"
									accelRunString
									, System.currentTimeMillis());
						}
					}.start();
			}
			accelRun.put("{\"x-axis\":\""+acc.getX()+"\",\"y-axis\":\""+acc.getY()+"\",\"z-axis\":\""+acc.getZ()+"\",\"run_id\":\""+current_run_id+"\",\"timestamp\":\""+acc.getTimestamp()+"\"}");
			a++;
		}		
		final String accelRunString = accelRun.toString();
		new Thread() {

				@Override
				public void run() {
					getMyApplication().getSensePlatform().addDataPoint(sensorName, sensorName,
							sensorName, dataType, 
							//"{\"x-axis\":\""+acc.getX()+"\",\"y-axis\":\""+acc.getY()+"\",\"z-axis\":\""+acc.getZ()+"\",\"run_id\":\""+current_run_id+"\",\"timestamp\":\""+acc.getTimestamp()+"\"}"
							accelRunString
							, System.currentTimeMillis());
				}
			}.start();
			// show message
			showToast(R.string.msg_sent_data, sensorName);
	}
	
	/**
	 * An example of how to upload data for a custom sensor.
	 */
	private void insertData() {
		Log.v("SENSE", "Insert data point");

		final String sensorName = "runrecord";
		final String displayName = "runrecord";
		final String dataType = SenseDataTypes.JSON;
		final String description = sensorName;

		RouteRunDataSource rrds = app.getRouteRunDataSource();
		rrds.open();
		RouteRunData rrd = rrds.getLastRouteRun();
		rrds.close();
		
		final String value = "{\"runid\":\""+rrd.getRoute_id()+"\",\"startdatetime\":\""+rrd.getStart_datetime()+"\",\"enddatetime\":\""+rrd.getEnd_datetime()+"\"}";
		
		//timestamp is van toevoeging
		final long timestamp = System.currentTimeMillis();// estTimestamp;

		// start new Thread to prevent NetworkOnMainThreadException
		new Thread() {

			@Override
			public void run() {
				getMyApplication().getSensePlatform().addDataPoint(sensorName, displayName,
						description, dataType, value, timestamp);
			}
		}.start();

		// show message
		showToast(R.string.msg_sent_data, sensorName);
	}
	
	private void showToast(final int resId, final Object... formatArgs) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				CharSequence msg = getString(resId, formatArgs);
				Toast.makeText(RunFinishedActivity.this, msg, Toast.LENGTH_LONG)
						.show();
			}
		});
	}


}
