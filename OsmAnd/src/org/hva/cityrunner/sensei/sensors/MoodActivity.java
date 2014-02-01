package org.hva.cityrunner.sensei.sensors;

import nl.sense_os.service.constants.SenseDataTypes;

import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.activities.OsmandIntents;
import org.hva.cityrunner.plus.activities.UsageDataActivity;
import org.hva.cityrunner.sensei.data.QueueData;
import org.hva.cityrunner.sensei.data.RouteRunData;
import org.hva.cityrunner.sensei.db.AffectDataSource;
import org.hva.cityrunner.sensei.db.QueueDataSource;
import org.hva.cityrunner.sensei.db.RouteRunDataSource;
import org.hva.createit.digitallife.sam.AffectDomain;
import org.hva.cityrunner.plus.R;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.askcs.android.affectbutton.Affect;
import com.askcs.android.widget.AffectButton;
//import org.hva.createit.digitallife.sam.Affect;

public class MoodActivity extends Activity implements OnTouchListener{
    private static final String TAG = "MoodMeter";
    private static final String DEMO_SENSOR_NAME = "affectbutton";
    private AffectButton ab;
    private Button cb;
    private OsmandApplication mApplication;
    private String nextActivity;
    private int tracknr;
    private int run_id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_affect_ab);
		
		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			tracknr = extras.getInt("track", 0);
			run_id = extras.getInt("run_id", 0);
			nextActivity = extras.getString("nextActivity", "finished");
		}
		
		mApplication = (OsmandApplication) this.getApplicationContext();
		
        ab = (AffectButton) this.findViewById(R.id.affectbutton);
        ab.setOnTouchListener(this);
        

        cb = (Button) this.findViewById(R.id.buttonConfirm);
 
	}
	
	  /**
     * Handles clicks on the UI
     * 
     * @param v
     */
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.buttonConfirm:
        		confirmMood();
        		break;
        default:
            Log.w(TAG, "Unexpected button pressed: " + v);
        }
    }
	
	public void confirmMood(){
		Affect af = ab.getAffect();
		Log.d(TAG, "P:"+af.getPleasure()+" D:"+af.getDominance()+" A:"+af.getArousal());;
		
		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			tracknr = extras.getInt("track", 0);
			run_id = extras.getInt("run_id", 0);
			nextActivity = extras.getString("nextActivity", "finished");
		}else{
			tracknr = 0;
			nextActivity = "finished";
		}
		
		//commit data to sense
		insertData(af);
		

		
//		 startActivity(new Intent(this, AffectSAMActivity.class));
		if(nextActivity.equals("map")){
	        OsmandApplication app = (OsmandApplication) getApplication();
	       QueueDataSource rrds = app.getQueueDataSource();
	        rrds.open();
	        QueueData qd = new QueueData();
	        qd.setRun_id(run_id);
	        rrds.add(qd);
	        rrds.close();
			
			Intent mapIntent = new Intent(this, OsmandIntents.getMapActivity());
			mapIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			mapIntent.putExtra("track",tracknr);
			mapIntent.putExtra("run_id", run_id);
			startActivity(mapIntent);
		}else{
			Intent intentSettings = new Intent(this,
					UsageDataActivity.class);
			intentSettings.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			intentSettings.putExtra("track",tracknr);
			intentSettings.putExtra("run_id", run_id);
					//OsmandIntents.getRunFinishedActivity());
			//intentSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			this.startActivity(intentSettings);
		}
	}
	
	  private void insertData(Affect af) {
	        Log.v(TAG, "Insert data point");
	        
	        long datetime = System.currentTimeMillis();
	        org.hva.createit.digitallife.sam.Affect afhva = new org.hva.createit.digitallife.sam.Affect(new AffectDomain(af.getPleasure(), datetime),new AffectDomain(af.getArousal(), datetime),new AffectDomain(af.getDominance(), datetime), datetime); 
	        
	        AffectDataSource afData = new AffectDataSource(this);
	        afData.open();
	        int userid = 0;//hall userid op
	        int runstate = 0;
	        if(nextActivity.equals("finished")){
	        	runstate = 1 ;
	        }else{
	        	runstate = 0;
	        }
	        afData.addAffect(0, run_id, userid, runstate, afhva);
	        afData.close();
	        
	        // Description of the sensor
	        final String name = DEMO_SENSOR_NAME;
	        final String displayName = "AffectAB";
	        final String dataType = SenseDataTypes.JSON;
	        final String description = name;
	        // the value to be sent, in json format
	        final String value = "{\"Pleasure\":\""+af.getPleasure()+"\",\"Dominance\":\""+af.getDominance()+"\",\"Arousal\":\""+af.getArousal()+"\",\"tracknr\":\""+tracknr+"\",\"run_id\":\""+run_id+"\",\"runstate\":\""+runstate+"\"}";
	        final long timestamp = datetime;

	        // start new Thread to prevent NetworkOnMainThreadException
	        new Thread() {

	            @Override
	            public void run() {
	                mApplication.getSensePlatform().addDataPoint(name, displayName, description,
	                        dataType, value, timestamp);
	            }
	        }.start();

	        // show message
	  }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		cb.setEnabled(true);
		return false;
	}

}
