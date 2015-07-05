package com.dealfaro.luca.serviceexample;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import de.greenrobot.event.EventBus;

/**
 * Created by luca on 7/5/2015.
 */
public class MyServiceTask implements Runnable {

    public AtomicLong T0 = new AtomicLong();
    public AtomicLong first_accel_time = null;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener listener;

    public static final String LOG_TAG = "MyService";
    private boolean running;
    private Context context;
    private boolean accel_set = false;
    private long minTime = 30000;
    private long shakeThreshold = 1;

    public MyServiceTask(Context _context) {
        context = _context;
        listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.i(LOG_TAG, "Current Time = " + ((System.currentTimeMillis() - T0.get()) / 1000));

                //By default the sensors are sensitive to movement and are always changing value
                //The post will not be sent unless the change is significant
                if(Math.abs(event.values[0]) > 1 || Math.abs(event.values[1]) > 1) {
                    //Here we check if the phone was moved after the first 30 seconds
                    if(System.currentTimeMillis() - T0.get() > minTime) {
                        if (!accel_set) {
                            first_accel_time = new AtomicLong(System.currentTimeMillis());
                            Log.i(LOG_TAG, "FIRST ACCEL TIME SET " + first_accel_time.get());
                            accel_set = true;
                            //relay the message of the first accel time to the main thread
                            EventBus.getDefault().post(first_accel_time);
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        // Put here what to do at creation.
    }



    @Override
    public void run() {
        T0.set(System.currentTimeMillis());
        running = true;
        Random rand = new Random();

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else{
            // Sorry, there are no accelerometers on your device.
            // You can't play this game.
        }

        mSensorManager.registerListener(listener, mSensor, mSensorManager.SENSOR_DELAY_NORMAL);

        while (running) {
            // Sleep a tiny bit.
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
        }
    }

    public void stopProcessing() {
        running = false;
    }

    public void setTaskState(boolean b) {
        // Do something with b.
    }

    //clear the T0 and first_accel_time variables in the task
    public void clear(){
        first_accel_time.set(0);
        accel_set = false;
        T0.set(System.currentTimeMillis());
        Log.i(LOG_TAG, "T0 CHANGED: " + T0);
    }


}
