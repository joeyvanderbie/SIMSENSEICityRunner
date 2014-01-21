package net.osmand.sensei.sensors;

import java.util.ArrayList;

import net.osmand.plus.activities.MapActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class AccelerometerListener implements SensorEventListener {

    private long startTime;
    private int numSamples;
    private boolean isActive = false;
    private double samplingRate = 0.0;
    private MapActivity accelerometerTest;
    private ArrayList<SensorEvent> samples;
    
    public AccelerometerListener(MapActivity accelerometerTest) {
        this.accelerometerTest = accelerometerTest;
        
    }
    public double getSamplingRate() {
        return samplingRate;
    }
    
    public ArrayList<SensorEvent> getSamples(){
    	return samples;
    }
    
    public void startRecording() {
        startTime = System.currentTimeMillis();
        numSamples = 0;
        isActive = true;
        this.samples = new ArrayList<SensorEvent>();
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
            if ((now - startTime) % 5000 == 0) {
                samplingRate = numSamples / ((now - startTime) / 1000.0);                
              //  isActive = false;
                
                //accelerometerTest.displayRates();
                Log.d("AcceleromterTest", "displayrate: "+samplingRate);
            }
            samples.add(event);
        }
    }
}
