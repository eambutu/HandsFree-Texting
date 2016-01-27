package com.phillipkwang.smscar;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private static boolean serviceOn;
    private static Button button;
    private static TextView textServiceStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        serviceOn = isMyServiceRunning(MainService.class);
        Log.d("MainActivity", "first onCreate: serviceOn is " + serviceOn);
        button = (Button)findViewById(R.id.button);
        textServiceStatus = (TextView)findViewById(R.id.serviceStatus);

        updateViews();

        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Log.d("MainActivity", " Button Pressed!!");
                if(!serviceOn) {
                    startMainService();
                    Toast.makeText(MainActivity.this, R.string.start_service, Toast.LENGTH_SHORT).show();
                }
                else {
                    stopMainService();
                    Toast.makeText(MainActivity.this, R.string.stop_service, Toast.LENGTH_SHORT).show();
                }
                serviceOn = !serviceOn;
                Log.d("MainActivity", "serviceOn is " + serviceOn);
                updateViews();
            }
        });
        /*Button startButton = (Button)findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startMainService();
                Toast.makeText(MainActivity.this, R.string.start_service, Toast.LENGTH_SHORT).show();
            }
        });
        Button stopButton = (Button)findViewById(R.id.stop);
        stopButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                stopMainService();
                Toast.makeText(MainActivity.this, R.string.stop_service, Toast.LENGTH_SHORT).show();
            }
        });
        Button runningButton = (Button)findViewById(R.id.running);
        runningButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                boolean isServiceRunning = isMyServiceRunning(MainService.class);
                Toast.makeText(MainActivity.this, isServiceRunning + "", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    @Override
    public void onResume() {
        Log.d("MainActivity", "onResume");
        super.onResume();
        updateViews();
    }

    @Override
    public void onStart() {
        Log.d("MainActivity", "onStart");
        super.onStart();
        updateViews();
    }

    private void startMainService(){
        startService(new Intent(this, MainService.class));
    }

    private void stopMainService(){
        stopService(new Intent(this, MainService.class));
    }

    private void updateViews(){
        if(!serviceOn) {
            button.setBackgroundResource(R.drawable.start_button);
            textServiceStatus.setText("Service is Off");
        }
        else {
            button.setBackgroundResource(R.drawable.stop_button);
            textServiceStatus.setText("Service is On");
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        else if (id == R.id.action_help) {
            Intent i = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
