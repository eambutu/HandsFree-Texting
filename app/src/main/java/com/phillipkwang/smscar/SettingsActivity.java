package com.phillipkwang.smscar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;

/**
 * Created by Phillip on 1/26/2016.
 */
public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = "PreferenceActivity";
    public static final String PREFS_NAME = "SMSCar_Prefs";
    @Override
    public void onContentChanged() {
        Log.d(TAG, "onContentChanged");
        stopService(new Intent(this, MainService.class));
        super.onContentChanged();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.commit();
        startService(new Intent(this, MainService.class));
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
