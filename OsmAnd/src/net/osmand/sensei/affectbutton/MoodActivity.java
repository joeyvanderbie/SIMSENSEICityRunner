package net.osmand.sensei.affectbutton;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import nl.sense_os.service.constants.SenseDataTypes;
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
//import com.askcs.android.affectbutton.Affect;

public class MoodActivity extends Activity implements OnTouchListener{
    private static final String TAG = "MoodMeter";
    private static final String DEMO_SENSOR_NAME = "affectbutton";
    private AffectButton ab;
    private Button cb;
    private OsmandApplication mApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_affect_ab);

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
		//commit data to sense
		insertData(af);
		//TO-DO afvangen als het versturen fout gaat
		
		 startActivity(new Intent(this, AffectSAMActivity.class));
	}
	
	  private void insertData(Affect af) {
	        Log.v(TAG, "Insert data point");

	        // Description of the sensor
	        final String name = DEMO_SENSOR_NAME;
	        final String displayName = "AffectAB";
	        final String dataType = SenseDataTypes.JSON;
	        final String description = name;
	        // the value to be sent, in json format
	        final String value = "{\"Pleasure\":\""+af.getPleasure()+"\",\"Dominance\":\""+af.getDominance()+"\",\"Arousal\":\""+af.getArousal()+"\"}";
	        final long timestamp = System.currentTimeMillis();

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
