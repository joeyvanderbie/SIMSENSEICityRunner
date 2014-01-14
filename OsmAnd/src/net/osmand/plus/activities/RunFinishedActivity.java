package net.osmand.plus.activities;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.sensei.data.RouteRunData;
import net.osmand.sensei.db.RouteRunDataSource;
import net.osmand.sensei.sense.MainActivity;
import nl.sense_os.service.constants.SenseDataTypes;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class RunFinishedActivity extends SherlockActivity {
	
	private OsmandApplication app;
	
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
		
		uploadDataToSense();
	}
	
	private void uploadDataToSense(){
		insertData();//test sense data upload
		flushData();
	}
	
	private void flushData() {
		Log.v("SENSE", "Flush buffers");
		getMyApplication().getSensePlatform().flushData();
		showToast(R.string.msg_flush_data);
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
