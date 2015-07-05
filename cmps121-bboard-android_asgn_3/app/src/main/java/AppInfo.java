/**
 * Created by will on 5/4/15.
 */
package com.dealfaro.luca.clicker;

import java.security.SecureRandom;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.math.BigInteger;

public class AppInfo {

    public static final String PREF_USERID = "BulletinBoardUser";

    private static AppInfo instance = null;

    protected AppInfo() {
        // Exists only to defeat instantiation.
    }

    // Here are some values we want to keep global.
    public String userid = "user";

    public static AppInfo getInstance(Context context) {
        if (instance == null) {
            instance = new AppInfo();
            // Creates a userid, if I don't have one.
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            instance.userid = settings.getString(PREF_USERID, null);
            if (instance.userid == null) {
                // We need to create a userid.
                SecureRandom random = new SecureRandom();
                instance.userid = new BigInteger(130, random).toString(32);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREF_USERID, instance.userid);
                editor.commit();
            }
        }
        return instance;
    }
}

