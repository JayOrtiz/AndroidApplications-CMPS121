package com.dealfaro.luca.clicker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.location.LocationManager;
import android.location.LocationListener;

public class MainActivity extends ActionBarActivity {

    Location lastLocation;
    private double lastAccuracy = (double) 1e10;
    private long lastAccuracyTime = 0;

    private static final String LOG_TAG = "lclicker";

    private static final float GOOD_ACCURACY_METERS = 100;

    // This is an id for my app, to keep the key space separate from other apps.
    private static final String MY_APP_ID = "luca_bboard";

    private static final String SERVER_URL_PREFIX = "https://hw3n-dot-luca-teaching.appspot.com/store/default/";

    // To remember the favorite account.
    public static final String PREF_ACCOUNT = "pref_account";

    // To remember the post we received.
    public static final String PREF_POSTS = "pref_posts";

    // Uploader.
    private ServerCall uploader;

    // Remember whether we have already successfully checked in.
    private boolean checkinSuccessful = false;

    private ArrayList<String> accountList;

    private double longitude = -122.049921;
    private double latitude = 37.00152;
    private String globalmsg;
    private String globalid;
    private int unsyncedPost = 0;
    //private String dest = getIntent().getStringExtra("dest");

    private com.dealfaro.luca.clicker.AppInfo appinfo;

    private class ListElement {
        ListElement() {};

        public String textLabel;
        public String buttonLabel;
    }

    private ArrayList<Message> aList;

    private class MyAdapter extends ArrayAdapter<Message> {

        int resource;
        Context context;

        public MyAdapter(Context _context, int _resource, List<Message> items) {
            super(_context, _resource, items);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newView;

            final Message w = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource,  newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            // Fills in the view.
            TextView tv = (TextView) newView.findViewById(R.id.itemText);
            Button b = (Button) newView.findViewById(R.id.itemButton);
            String date = w.ts.substring(0,10);
            String time = w.ts.substring(11,19);
            tv.setText(w.msg +"\nGMT: "+ time + " " + date);
            //b.setText(w.buttonLabel);

            // Sets a listener for the button, and a tag for the button as well.
            b.setTag(new Integer(position));
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Reacts to a button press.
                    // Gets the integer tag of the button.

                    String s = v.getTag().toString();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, s, duration);
                    toast.show();
                }
            });

            // Set a listener for the whole list item.
            newView.setTag(w.msg);
            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
             //Send the intent and the userId to the ChatActivity*/
                    if(w.userid.equals(appinfo.userid))
                        return;
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("dest", w.userid);

                    context.startActivity(intent);
                }
            });

            return newView;
        }
    }

    private MyAdapter aa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aList = new ArrayList<Message>();
        aa = new MyAdapter(this, R.layout.list_element, aList);
        ListView myListView = (ListView) findViewById(R.id.listView);
        myListView.setAdapter(aa);
        aa.notifyDataSetChanged();

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(loc != null) {
            longitude = loc.getLongitude();
            latitude = loc.getLatitude();
        }

        appinfo = com.dealfaro.luca.clicker.AppInfo.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText("longitude: " + longitude + "\n latitude: " + latitude);
        //String dest = getIntent().getStringExtra("dest");
        //Log.v(LOG_TAG, dest);
    }


    @Override
    protected void onResume() {
        super.onResume();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        stopSpinner();

    }



    @Override
    protected void onPause() {
        // Stops the location updates.
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);

        // Stops the upload if any.
        if (uploader != null) {
            uploader.cancel(true);
            uploader = null;
        }
        super.onPause();

    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Do something with the location you receive.
            TextView tv = (TextView) findViewById(R.id.textView);
            if(location == null){
                tv.setText("Unable to get current location.");
            }else {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                tv.setText("longitude: " + longitude + "\n latitude: " + latitude);
                //Log.i(LOG_TAG, latitude + " " +longitude);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(LOG_TAG, "Provider Enabled " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(LOG_TAG, "Provider Disabled " + provider);
        }
    };

    public void clickRefresh(View v){
        PostMessageSpec myCallSpec = new PostMessageSpec();


        myCallSpec.url = SERVER_URL_PREFIX + "get_local.json";
        myCallSpec.context = MainActivity.this;

        Log.i(LOG_TAG, "REFRESH " + "longitude = " + longitude + " latitude = " + latitude );


        // Let's add the parameters.
        HashMap<String,String> m = new HashMap<String,String>();
        m.put("lng", String.valueOf(longitude));
        m.put("lat", String.valueOf(latitude));
        myCallSpec.setParams(m);

        startSpinner();

        if (uploader != null) {
            // There was already an upload in progress.
            uploader.cancel(true);
        }
        uploader = new ServerCall();
        uploader.execute(myCallSpec);
    }

    public void clickPost(View v){
        EditText et = (EditText) findViewById(R.id.editText);
        String msg = et.getText().toString();

        PostMessageSpec myCallSpec = new PostMessageSpec();

        myCallSpec.url = SERVER_URL_PREFIX + "put_local.json";
        myCallSpec.context = MainActivity.this;

        Random rand = new Random();
        String msgid = new BigInteger(32, rand).toString(32); //Create the message ID

        Log.i(LOG_TAG, "POST " + "longitude = " + longitude + " latitude = " + latitude + " msgid =" + msgid);

        HashMap<String,String> m = new HashMap<String,String>();
        m.put("lng", String.valueOf(longitude));
        m.put("lat", String.valueOf(latitude));
        m.put("msgid", msgid);
        m.put("msg", msg);
        myCallSpec.setParams(m);

        globalmsg = msg;  // put the msg and msgid in a global variable,
        globalid = msgid; // used if the server does not respond in time to update with first response

        startSpinner();

        if (uploader != null) {
            // There was already an upload in progress.
            uploader.cancel(true);
        }
        uploader = new ServerCall();
        uploader.execute(myCallSpec);

        et.setText("");
    }


    /*Old starter code - not used in my project*/
    public void clickButton(View v) {

        // Get the text we want to send.
        EditText et = (EditText) findViewById(R.id.editText);
        String msg = et.getText().toString();

        // Then, we start the call.
        PostMessageSpec myCallSpec = new PostMessageSpec();


        myCallSpec.url = SERVER_URL_PREFIX + "post_msg.json";
        myCallSpec.context = MainActivity.this;
        // Let's add the parameters.
        HashMap<String,String> m = new HashMap<String,String>();
        m.put("app_id", MY_APP_ID);
        m.put("msg", msg);
        myCallSpec.setParams(m);

        startSpinner();

        // Actual server call.
        if (uploader != null) {
            // There was already an upload in progress.
            uploader.cancel(true);
        }
        uploader = new ServerCall();
        //startSpinner();
        uploader.execute(myCallSpec);
    }

    public void startSpinner(){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    public void stopSpinner(){
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    /* Old starter code, not used */
    private String reallyComputeHash(String s) {
        // Computes the crypto hash of string s, in a web-safe format.
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(s.getBytes());
            digest.update("My secret key".getBytes());
            byte[] md = digest.digest();
            // Now we need to make it web safe.
            String safeDigest = Base64.encodeToString(md, Base64.URL_SAFE);
            return safeDigest;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * This class is used to do the HTTP call, and it specifies how to use the result.
     */
    class PostMessageSpec extends ServerCallSpec {
        @Override
        public void useResult(Context context, String result) {
            //startSpinner();
            if (result == null) {
                // Do something here, e.g. tell the user that the server cannot be contacted.
                Log.i(LOG_TAG, "The server call failed.");
                TextView tv = (TextView) findViewById(R.id.textView);
                tv.setText("Could not connect to server");
                stopSpinner();
            } else {
                // Translates the string result, decoding the Json.
                Log.i(LOG_TAG, "Received string: " + result);
                displayResult(result);
                // Stores in the settings the last messages received.
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREF_POSTS, result);
                editor.commit();
            }
        }
    }

/*Displays the 10 latest messages closes to you - public*/
    private void displayResult(String result) {
        Gson gson = new Gson();
        CallResult ml = gson.fromJson(result, CallResult.class);
        // Fills aList, so we can fill the listView.
        aList.clear();
/*
        if(!ml.messages[0].msgid.equals(globalid) && globalmsg != null) {   //in case the server
            Log.i(LOG_TAG, "not synced with server");  //does not respond w/ updated data (unsynced)
            ListElement ael = new ListElement();       //show the new message among the others
            ael.textLabel = globalmsg + " (Unsynced with server)";
            aList.add(ael);
            unsyncedPost = 1;
        }
*/
        for (int i = 0; i < ml.messages.length; i++) {
            if(i > 9) /*Prevents more than 10 messages from being posted*/
                break;
            if(i == 0 && unsyncedPost == 1) { //checks if an unsynced post was made & resets flag
                unsyncedPost = 0;
                continue;
            }
            //Message ael = new Message();
            //String date = ml.messages[i].ts.substring(0,10);
            //String time = ml.messages[i].ts.substring(11,19);
            //ael.textLabel = ml.messages[i].msg + "\n(GMT) Time: " + time + ", Date: " + date;
            //ael.buttonLabel = "Click";
            aList.add(ml.messages[i]);
            globalmsg = null;
            globalid = null;
        }
        aa.notifyDataSetChanged();
        stopSpinner();
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
