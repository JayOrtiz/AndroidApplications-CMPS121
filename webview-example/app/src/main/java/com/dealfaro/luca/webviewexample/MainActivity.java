package com.dealfaro.luca.webviewexample;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    static final public String SFGATE = "http://www.sfgate.com/";
    static final public String SJMERCURY = "http://www.mercurynews.com";
    static final public String SCSENTINEL = "http://www.santacruzsentinel.com";
    private static final String LOG_TAG = "HW4 Log";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity2, menu);
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

    //sends the user to the appropriate paper depending on the button pressed
    public void clickButton(View v) {
        Button b = (Button) v;
        String paper = b.getText().toString();
        String dest;
        //String t = NUM_STRING;
        Log.i(LOG_TAG, paper);
        if(paper.equals("San Francisco Gate"))
            dest = SFGATE;
        else if(paper.equals("San Jose Mercury News"))
            dest = SJMERCURY;
        else
            dest = SCSENTINEL;
        TextView tv = (TextView) findViewById(R.id.textView);
        Log.i(LOG_TAG, dest);
        Intent intent = new Intent(MainActivity.this, ReaderActivity.class);
        intent.putExtra("destURL", dest);

        startActivity(intent);
    }
}
