package com.example.komal.blue;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by komal on 16/1/17.
 */

public class ShakeListener {
    private Context shakeContext;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 20.25f;
    private static final int SHAKE_INTERVAL = 1000;
    private long lastShakeTime, currentTime;
    private float x, y, z;


    SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            currentTime = System.currentTimeMillis();
            if((currentTime - lastShakeTime) > SHAKE_INTERVAL){
                double acceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;
                Log.d("Acceleration ", Double.toString(acceleration));

                if(acceleration > SHAKE_THRESHOLD){
                    lastShakeTime = currentTime;
                    Log.d("Shook", "Shake");
                    Toast.makeText(shakeContext, "Shake!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public ShakeListener(Context context){
        this.shakeContext = context;
        listen();
    }

    private void listen() {
        sensorManager = (SensorManager)shakeContext.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public float getX(){
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
}
