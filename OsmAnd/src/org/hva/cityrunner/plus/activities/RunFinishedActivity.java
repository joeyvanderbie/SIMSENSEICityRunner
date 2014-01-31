package org.hva.cityrunner.plus.activities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import nl.sense_os.service.constants.SenseDataTypes;

import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.R;
import org.hva.cityrunner.sensei.data.AccelData;
import org.hva.cityrunner.sensei.data.AffectData;
import org.hva.cityrunner.sensei.data.RouteRunData;
import org.hva.cityrunner.sensei.db.AccelDataSource;
import org.hva.cityrunner.sensei.db.AffectDataSource;
import org.hva.cityrunner.sensei.db.RouteRunDataSource;
import org.hva.cityrunner.sensei.db.UserDataSource;
import org.hva.cityrunner.sensei.sensors.StaticAffectButton;
import org.json.JSONArray;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class RunFinishedActivity extends SherlockActivity {
	
	private OsmandApplication app;
	private int run_id;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		((OsmandApplication) getApplication()).applyTheme(this);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setTitle(R.string.run_finished_title);
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.run_finished);
		
		invalidateOptionsMenu();
		Button share = (Button ) this.findViewById(R.id.shareButton);
		share.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String screenshotLocation = Environment.getExternalStorageDirectory().getAbsolutePath()+"/osmand/CityRunner"+System.currentTimeMillis()+".jpg";
				RunFinishedActivity.savePic(RunFinishedActivity.takeScreenShot(RunFinishedActivity.this), screenshotLocation);
				
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				Uri screenshotUri = Uri.parse("file://"+screenshotLocation);
				sharingIntent.setType("*/*");// shareIntent.setType("*/*");
				sharingIntent.putExtra(Intent.EXTRA_TEXT, "I ran and survived the SIM SENSEI City Runner!");
				sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
				startActivity(Intent.createChooser(sharingIntent, "Share run using"));
			}
		});

		
		//if(app.currentRouteRun != null){
			uploadDataToSense();
		//}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// if (getSupportActionBar().getSelectedNavigationIndex() == 0) {
			   menu.add("Share")
             .setIcon(android.R.drawable.ic_menu_share)
             .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
       //  }
         return true;
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
	    
	    if(item.getTitle().equals("Share")){
	    	String screenshotLocation = Environment.getExternalStorageDirectory().getAbsolutePath()+"/osmand/CityRunner"+System.currentTimeMillis()+".jpg";
			RunFinishedActivity.savePic(RunFinishedActivity.takeScreenShot(RunFinishedActivity.this), screenshotLocation);
			
			
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
			Uri screenshotUri = Uri.parse("file://"+screenshotLocation);
			sharingIntent.setType("*/*");// shareIntent.setType("*/*");
			sharingIntent.putExtra(Intent.EXTRA_TEXT, "I ran and survived the SIM SENSEI City Runner!");
			sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
			startActivity(Intent.createChooser(sharingIntent, "Share run using"));
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
		
		TextView teamnr = (TextView) findViewById(R.id.teamnr_text);
		UserDataSource uds = app.getUserDataSource();
		uds.open();
		teamnr.setText(""+
				uds.getUserData().getTeamid());
		uds.close();
		
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
		
	}
	
	private void uploadDataToSense(){
		  final Handler handler = new Handler();
	        handler.postDelayed(new Runnable() {
	            @Override
	            public void run() {
	            	insertData();//test sense data upload
	            	senseInsertAccelerometerData();

	            }
	        }, 500);
	        handler.postDelayed(new Runnable() {
	            @Override
	            public void run() {
	            	flushData();
	            }
	        }, 60000);//after 1 minute submit data
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
		int limit = 300;
		int offset = 0;
		ArrayList<AccelData> accels = ads.getAllAccel(run_id, limit, offset);
		
		while(accels.size() > 0){
		
		Log.v("SENSE", "Insert data point for accelerometer");

		final String sensorName = "sensei_accelerometer";
		final String dataType = SenseDataTypes.JSON;
		final int current_run_id = run_id;
		

		JSONArray accelRun = new JSONArray();

		Log.d("SENSE ACCELEROMETER","Acceldatapoints: "+accels.size());

		for(AccelData acc : accels){
			accelRun.put("{\"x-axis\":\""+acc.getX()+"\",\"y-axis\":\""+acc.getY()+"\",\"z-axis\":\""+acc.getZ()+"\",\"run_id\":\""+current_run_id+"\",\"timestamp\":\""+acc.getTimestamp()+"\"}");
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
			
			offset = offset+limit;
			accels = ads.getAllAccel(run_id, limit, offset);
		}
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
		
		final String value = "{\"runid\":\""+rrd.getRoute_id()+"\",\"startdatetime\":\""+rrd.getStart_datetime()+"\",\"enddatetime\":\""+rrd.getEnd_datetime()+"\",\"id\":\""+rrd.getId()+"\"}";
		
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

	
	private static Bitmap takeScreenShot(Activity activity)
	{
	    View view = activity.getWindow().getDecorView();
	    view.setDrawingCacheEnabled(true);
	    view.buildDrawingCache();
	    Bitmap b1 = view.getDrawingCache();
	    Rect frame = new Rect();
	    activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
	    int statusBarHeight = frame.top;
	    Point size = new Point();
	    activity.getWindowManager().getDefaultDisplay().getSize(size);
	    int width = size.x;
	    int height = size.y;
	    
	    Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height  - statusBarHeight);
	    view.destroyDrawingCache();
	    return b;
	}
	private static void savePic(Bitmap b, String strFileName)
	{
	    FileOutputStream fos = null;
	    try
	    {
	        fos = new FileOutputStream(strFileName);
	        if (null != fos)
	        {
	            b.compress(Bitmap.CompressFormat.PNG, 90, fos);
	            fos.flush();
	            fos.close();
	        }
	    }
	    catch (FileNotFoundException e)
	    {
	        e.printStackTrace();
	    }
	    catch (IOException e)
	    {
	        e.printStackTrace();
	    }
	}
	
	public void onBackPressed(){
	     // do something here and don't write super.onBackPressed()
		startActivity(new Intent(this, OsmandIntents.getMainMenuActivity()));
		finish();
	}

}
