package com.phillipkwang.smscar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Phillip on 1/27/2016.
 */
public class BootBroadcastReceiver extends BroadcastReceiver{
    private static final String TAG = "BootBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean servicestart = preferences.getBoolean("pref_key_servicestart_preference", false);
        Log.d(TAG, "servicestart preference is: " + servicestart);
        if (servicestart) {
            Intent startServiceIntent = new Intent(context, MainService.class);
            context.startService(startServiceIntent);
        }
        Intent startServiceIntent = new Intent(context, MainService.class);
        context.startService(startServiceIntent);
    }
}
