package com.dealfaro.luca.webviewexample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class ReaderActivity extends ActionBarActivity {

    static final public String MYPREFS = "myprefs";
    static final public String PREF_URL = "restore_url";
    static final public String WEBPAGE_NOTHING = "about:blank";
    static final public String MY_WEBPAGE = "http://users.soe.ucsc.edu/~luca/android.html";
    static final public String LOG_TAG = "webview_example";


    WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        myWebView = (WebView) findViewById(R.id.webView1);
        myWebView.setWebViewClient(new MyWebViewClient());

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // Binds the Javascript interface
        myWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        webSettings.setDomStorageEnabled(true);
        myWebView.setWebChromeClient(new WebChromeClient());
        if (extras != null) {
            String value = extras.getString("destURL");
            myWebView.loadUrl(value);
        }
        else{
            myWebView.loadUrl(MY_WEBPAGE);
        }
        //myWebView.loadUrl("javascript:alert(\"Hello\")");

    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            // If it wasn't the Back key or there's no web page history,
            // bubble up to the default
            // system behavior (probably exit the activity)
            super.onBackPressed();
        }
    }

    public class JavaScriptInterface {
        Context mContext; // Having the context is useful for lots of things,
        // like accessing preferences.

        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void myFunction(String args) {
            final String myArgs = args;
            Log.i(LOG_TAG, "I am in the javascript call.");
            runOnUiThread(new Runnable() {
                public void run() {
                    Button v = (Button) findViewById(R.id.button1);
                    v.setText(myArgs);
                }
            });

        }

    }


    @Override
    public void onPause() {

        Method pause = null;
        try {
            pause = WebView.class.getMethod("onPause");
        } catch (SecurityException e) {
            // Nothing
        } catch (NoSuchMethodException e) {
            // Nothing
        }
        if (pause != null) {
            try {
                pause.invoke(myWebView);
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e) {
            }
        } else {
            // No such method.  Stores the current URL.
            String suspendUrl = myWebView.getUrl();
            SharedPreferences settings = getSharedPreferences(ReaderActivity.MYPREFS, 0);
            SharedPreferences.Editor ed = settings.edit();
            ed.putString(PREF_URL, suspendUrl);
            ed.commit();
            // And loads a URL without any processing.
            myWebView.clearView();
            myWebView.loadUrl(WEBPAGE_NOTHING);
        }
        super.onPause();
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

    public void clickShare(View v){
        Intent intent = new Intent(Intent.ACTION_SEND);

        String url = myWebView.getUrl();
        intent.putExtra(Intent.EXTRA_TEXT, url);

        intent.setType("text/plain");
        startActivity(intent);
    }

    public class MyWebViewClient extends WebViewClient {


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i(ReaderActivity.LOG_TAG, ">>> Host: "+ Uri.parse(url).getHost());

            //We want to use substring to cut off the 'http://www' off of the
            //original URLs. This way the comparison works correctly.

            //I had to add 2 cases for sfgate, sometimes the host would be the regular URL and
            //other times it was the mobile version
            if (Uri.parse(url).getHost().equals(MainActivity.SFGATE.substring(11)) ||
                    Uri.parse(url).getHost().equals("m.sfgate.com") ||
                    Uri.parse(url).getHost().equals(MainActivity.SJMERCURY.substring(11)) ||
                    Uri.parse(url).getHost().equals(MainActivity.SCSENTINEL.substring(11))) {
                // This is my web site, so do not override; let my WebView load the page
                Log.i(ReaderActivity.LOG_TAG, ">>> FALSE, Same host, do not override ");
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            Log.i(ReaderActivity.LOG_TAG, ">>> TRUE, passing intent");
            startActivity(intent);

            return true;
        }
    }
}
