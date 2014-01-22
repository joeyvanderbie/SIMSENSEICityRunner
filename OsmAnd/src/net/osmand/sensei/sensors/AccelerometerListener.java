package net.osmand.sensei.sensors;

import java.util.ArrayList;

import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.activities.RunFinishedActivity;
import net.osmand.sensei.data.AccelData;
import net.osmand.sensei.db.AccelDataSource;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.Toast;

public class AccelerometerListener implements SensorEventListener {

    private long startTime;
    private int numSamples;
    private boolean isActive = false;
    private double samplingRate = 0.0;
    private MapActivity accelerometerTest;
    private ArrayList<AccelData> samples;
    private AccelDataSource ads;
    private int run_id;
    
    public AccelerometerListener(MapActivity accelerometerTest) {
        this.accelerometerTest = accelerometerTest;
        
    }
    public double getSamplingRate() {
        return samplingRate;
    }
    
    public ArrayList<AccelData> getSamples(){
    	return samples;
    }
    
    public void startRecording(int run_id) {
        startTime = System.currentTimeMillis();
        numSamples = 0;
        isActive = true;
        this.samples = new ArrayList<AccelData>();
        ads = new AccelDataSource(accelerometerTest);
        this.run_id = run_id;
		
    }
    
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isActive) {
            numSamples++;
            long now = System.currentTimeMillis();
            if (numSamples % 1000 == 0) {
                samplingRate = numSamples / ((now - startTime) / 1000.0);                
              //  isActive = false;
                startTime = now;
                numSamples = 0;
                
                //accelerometerTest.displayRates();
                Log.d("AcceleromterTest", "displayrate: "+samplingRate);
                
                //add samples to database
                ads.open();
               // ads.addAccelDataList(samples, 0, run_id);
                ads.addAccelDataListFast(samples, 0, run_id);
    			ads.close();
                samples = new ArrayList<AccelData>();
                
                accelerometerTest.showToast(R.string.msg_sent_data, " Add 1000 accelerometer samplings to DB");
            }
            
            samples.add(new AccelData(event.timestamp, event.values[0], event.values[1], event.values[2], run_id));
        }
    }
    
    public void submitLastSensorData(){
    	ads.open();
        // ads.addAccelDataList(samples, 0, run_id);
         ads.addAccelDataListFast(samples, 0, run_id);
			ads.close();
         samples = new ArrayList<AccelData>();
    }
}
