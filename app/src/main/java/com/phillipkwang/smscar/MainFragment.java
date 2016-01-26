package com.phillipkwang.smscar;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Phillip on 1/24/2016.
 */
public class MainFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_fragment, parent, false);

        Button startButton = (Button)v.findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startMainService();
                Toast.makeText(getActivity(), R.string.start_service, Toast.LENGTH_SHORT).show();
            }
        });
        Button stopButton = (Button)v.findViewById(R.id.stop);
        stopButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                stopMainService();
                Toast.makeText(getActivity(), R.string.stop_service, Toast.LENGTH_SHORT).show();
            }
        });
        Button runningButton = (Button)v.findViewById(R.id.running);
        runningButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                boolean isServiceRunning = isMyServiceRunning(MainService.class);
                Toast.makeText(getActivity(), isServiceRunning + "", Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    private void startMainService(){
        getActivity().startService(new Intent(getActivity(), MainService.class));
    }

    private void stopMainService(){
        getActivity().stopService(new Intent(getActivity(), MainService.class));
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
