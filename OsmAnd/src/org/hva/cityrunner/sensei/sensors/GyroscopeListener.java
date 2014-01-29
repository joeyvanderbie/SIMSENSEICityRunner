package org.hva.cityrunner.sensei.sensors;

import java.util.ArrayList;

import org.hva.cityrunner.plus.R;
import org.hva.cityrunner.plus.activities.MapActivity;
import org.hva.cityrunner.sensei.data.AccelData;
import org.hva.cityrunner.sensei.data.GyroData;
import org.hva.cityrunner.sensei.db.GyroDataSource;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class GyroscopeListener implements SensorEventListener {

    private long startTime;
    private int numSamples;
    private boolean isActive = false;
    private double samplingRate = 0.0;
    private MapActivity gyroscopeTest;
    private ArrayList<GyroData> samples;
    private GyroDataSource ads;
    private int run_id;
    
    public GyroscopeListener(MapActivity gyroscopeTest) {
        this.gyroscopeTest = gyroscopeTest;
        
    }
    public double getSamplingRate() {
        return samplingRate;
    }
    
    public ArrayList<GyroData> getSamples(){
    	return samples;
    }
    
    public void startRecording(int run_id) {
        startTime = System.currentTimeMillis();
        numSamples = 0;
        isActive = true;
        this.samples = new ArrayList<GyroData>();
        ads = new GyroDataSource(gyroscopeTest);
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
                
                //gyroscopeTest.displayRates();
                Log.d("GyroscopeListener", "displayrate: "+samplingRate);
                
                //add samples to database
                ads.open();
               // ads.addAccelDataList(samples, 0, run_id);
                ads.addGyroDataListFast(samples, 0, run_id);
    			ads.close();
                samples = new ArrayList<GyroData>();
                
               //gyroscopeTest.showToast(R.string.msg_sent_data, " Add 1000 gyroscope samplings to DB");
            }
            
            samples.add(new GyroData(event.timestamp, event.values[0], event.values[1], event.values[2], run_id));
        }
    }
    
    public void submitLastSensorData(){
    	ads.open();
         ads.addGyroDataListFast(samples, 0, run_id);
			ads.close();
         samples = new ArrayList<GyroData>();
    }
}
