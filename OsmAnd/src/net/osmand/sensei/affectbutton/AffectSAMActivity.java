package net.osmand.sensei.affectbutton;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.sensei.db.AffectDataSource;
import nl.sense_os.service.constants.SenseDataTypes;

import org.hva.createit.digitallife.sam.Affect;
import org.hva.createit.digitallife.sam.AffectEvent;
import org.hva.createit.digitallife.sam.AffectEventListener;
import org.hva.createit.digitallife.sam.SAM;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class AffectSAMActivity extends Activity implements AffectEventListener {
    private static final String TAG = "MoodMeter";
    private static final String DEMO_SENSOR_NAME = "affectbutton";
    private SAM sam;
    private Button cb;
    private OsmandApplication mApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_affect_sam);

		mApplication = (OsmandApplication) this.getApplicationContext();
		
        sam = (SAM) this.findViewById(R.id.sam);
        sam.setOnAffectEventListener(this);
        

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
		Affect af = sam.getAffect();
		Log.d(TAG, "SAM P:"+af.getPleasure()+" D:"+af.getDominance()+" A:"+af.getArousal());;
		//commit data to sense
		insertData(af);
		//TO-DO afvangen als het versturen fout gaat
		
		 //startActivity(new Intent(this, RunActivity.class));
	}
	
	  private void insertData(Affect af) {
	        Log.v(TAG, "Insert data point");

	        AffectDataSource afData = new AffectDataSource(this);
	        afData.open();
	       // afData.addAffect(af, 0, 0);
	        afData.close();
	        
	        // Description of the sensor
	        final String name = DEMO_SENSOR_NAME;
	        final String displayName = "AffectSAM";
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
	public void onAffectSetEvent(AffectEvent event, String message, Affect mAffect) {
		cb.setEnabled(true);
	}

}
