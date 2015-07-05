package com.dealfaro.luca.serviceexample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.dealfaro.luca.serviceexample.MyService.MyBinder;

import java.util.concurrent.atomic.AtomicLong;

import de.greenrobot.event.EventBus;

public class MainActivity extends ActionBarActivity {


    private static final String LOG_TAG = "MainActivity";

    // Service connection variables.
    private boolean serviceBound;
    private MyService myService;

    //variables relating to timers and movement checks
    boolean moved = false;
    AtomicLong timeMoved = null;
    public long timeThresh = 30000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceBound = false;
        // Prevents the screen from dimming and going to sleep.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "Resuming... MOVED IS : " + moved);
        // Starts the service, so that the service will only stop when explicitly stopped.
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        bindMyService();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        //Checks three things:
        //1. If the phone was moved
        //2. If it was moved more than 30 seconds ago
        //3. That timeMoved is set and not a null value
        //If it passes, the message that the device has moved will be posted
        //There is a slight delay in the posting of the message
        if(moved && timeMoved!=null && (System.currentTimeMillis() - timeMoved.get() > timeThresh)){
            TextView tv = (TextView) findViewById(R.id.number_view);
            tv.setText("THE DEVICE WAS MOVED");
        }

    }

    private void bindMyService() {
        // We are ready to show images, and we should start getting the bitmaps
        // from the motion detection service.
        // Binds to the service.
        Log.i(LOG_TAG, "Starting the service");
        Intent intent = new Intent(this, MyService.class);
        Log.i("LOG_TAG", "Trying to bind");
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    // Service connection code.
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
            // We have bound to the service.
            MyBinder binder = (MyBinder) serviceBinder;
            myService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };

    @Override
    protected void onPause() {
        if (serviceBound) {
            Log.i("MyService", "Unbinding");
            unbindService(serviceConnection);
            serviceBound = false;
        }
        super.onPause();
    }

    public void onEventMainThread(ServiceResult result) {
        Log.i(LOG_TAG, "Displaying: " + result.intValue);
        TextView tv = (TextView) findViewById(R.id.number_view);
        tv.setText(Integer.toString(result.intValue));
    }

    //Handles messages passed containing the time that the phone was moved
    public void onEventMainThread(AtomicLong moveTime) {
        //long now = System.currentTimeMillis();
        Log.i(LOG_TAG, "moved = " + moved);
        //Log.i(LOG_TAG, "time now = " + now);
        Log.i(LOG_TAG, "time moved = " + moveTime);
        moved = true;
        timeMoved = new AtomicLong(moveTime.get());

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

    //clears the stats for the time/movement variables, clears textview, and clears variables
    //in the task by calling clearTask in the service
    public void clickClear(View v){
        moved = false;
        timeMoved = null;
        myService.clearTask();
        TextView tv = (TextView) findViewById(R.id.number_view);
        tv.setText("Everything is Quiet");

    }

    //stops the service and unbinds from it, then exits the app
    public void clickExit(View v){
        //serviceBound = false;
        if(serviceBound)
            unbindService(serviceConnection);
        serviceBound = false;
        Log.i(LOG_TAG, "Stopping application.");
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);
        Log.i(LOG_TAG, "Stopped application.");
        finish();
    }
}
