package com.example.will.thedrain;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

    private static final String LOG_TAG = "MainActivity";
    public static SensorEventListener mListener;
    public static SensorManager mSensorManager;
    public static Sensor mSensor;

    public static float accelX = 0;
    public static float accelY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "Created");
        mListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //Log.i(LOG_TAG, "XPOS = " + (event.values[0]) + " YPOS = " + (event.values[1]));
                float phoneXAccel = event.values[0];
                float phoneYAccel = event.values[1];

                accelX = -phoneXAccel/50;
                accelY = phoneYAccel/50;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else{
            // Sorry, there are no accelerometers on your device.
            // You can't play this game.
        }

        mSensorManager.registerListener(mListener, mSensor, mSensorManager.SENSOR_DELAY_NORMAL);

        setContentView(R.layout.activity_main);
        final Screen screen= new Screen(this);
        //screen.setAnimation();
        setContentView(screen);
        screen.animateScreen();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
