package com.example.networkcellanalyzer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.telephony.TelephonyManager;

import com.example.networkcellanalyzer.ui.main.SectionsPagerAdapter;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private TelephonyManager telephonyManager;
    DBHelper DB;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        // request permissions when the user opens the app
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION,//causes all permission prompts to not appear to the user
                Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        DB = new DBHelper(this);

        //timer which refreshes every 40 second to add the data to the database
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    CellInfoUpdator cellInfoUpdator = new CellInfoUpdator(telephonyManager);
                }
                //generating snapshot and inserting data in database
                CaptureSnapshot captureSnapshot = new CaptureSnapshot(telephonyManager);
                String[] snapshot;
                Boolean Errors = true;
                snapshot = captureSnapshot.generateSnapshot();
                String op = snapshot[0];
                String pwr = snapshot[1];
                Integer ipwr = Integer.valueOf(pwr);
                String snr = snapshot[2];
                Integer isnr = Integer.valueOf(snr);
                String ntwrk = snapshot[3];
                String chnl = snapshot[4];
                Integer ichnl = Integer.valueOf(chnl);
                String id = snapshot[5];
                String time = snapshot[7];
                Long itime = Long.valueOf(time);
                DB.insertuserdata(itime, op, ntwrk, ipwr, isnr, ichnl, id);
            }
        }, 0, 40000); //40000 milliseconds=40second
    }
}
