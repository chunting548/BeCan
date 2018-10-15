package com.hlxz.librarybecan;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;
import static java.lang.Math.round;

public class AdjustActivity extends AppCompatActivity {

    final private int default_RSSI_value = -81;
    public float parameter_RSSI=1;
    private ProximityManager proximityManager;
    private TextView difference_distance = null;
    private boolean has_fix = false;
    private boolean has_get_num = false;
    ArrayList sampling_RSSI = new ArrayList();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_adjust);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation_bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);
        difference_distance = (TextView)findViewById(R.id.fix_distance_difference_value);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_navigation:
                        Intent intent_navigation = new Intent(AdjustActivity.this, MainActivity.class);
                        if(has_fix){
                            intent_navigation.putExtra("P",parameter_RSSI);
                            setResult(2,intent_navigation);
                        }
                            startActivity(intent_navigation);
                        break;
                    case R.id.navigation_tutorial:
                        Intent intent_tutorial = new Intent(AdjustActivity.this, TutorialActivity.class);
                        startActivity(intent_tutorial);
                        break;
                    case R.id.navigation_function:
                        Intent intent_function = new Intent(AdjustActivity.this, FunctionActivity.class);
                        startActivity(intent_function);
                        break;
                    case R.id.navigation_adjust:
                        break;
                }
                return false;
            }
        });

        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,}, 101);
        KontaktSDK.initialize(this);
        proximityManager = ProximityManagerFactory.create(this);
        proximityManager.setIBeaconListener(createIBeaconListener());
        proximityManager.configuration()
                .scanMode(ScanMode.LOW_LATENCY)
                .scanPeriod(ScanPeriod.RANGING)
                .activityCheckConfiguration(ActivityCheckConfiguration.DEFAULT)
                .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(5));
    }

    public void startOnClick(View view) {
        final Toast toast_Wait = Toast.makeText(this, "請維持裝置固定不動約3秒", Toast.LENGTH_SHORT);
        final Toast toast_Done = Toast.makeText(this, "校正完成！請回到導航畫面。", Toast.LENGTH_LONG);
        final Toast toast_No_Get = Toast.makeText(this, "未有Beacon訊號收到，無法校正。", Toast.LENGTH_LONG);
        toast_Wait.show();
        onStart();
        new  CountDownTimer(3000,1000){
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                onStop();
                if(has_get_num){
                    parameter_RSSI= ((float) avgRSSI(sampling_RSSI))/((float) default_RSSI_value);
                    difference_distance.setText((round((abs(1.0-parameter_RSSI))*100.0)/100.0)+"m");
                    has_fix = true;
                    toast_Done.show();
                }
                else{
                    toast_No_Get.show();
                }
            }
        }.start();
    }
    @Override
    protected void onStart() {
        super.onStart();
        startScanning();
    }

    @Override
    protected void onStop() {
        proximityManager.stopScanning();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        proximityManager.disconnect();
        proximityManager = null;
        super.onDestroy();
    }
    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });
    }
    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
                Log.i("Sample", "IBeacon discovered: " + ibeacon.toString());
                if(ibeacon.getMajor()==11748 && ibeacon.getMinor()==10154 ){
                    sampling_RSSI.add(ibeacon.getRssi());
                    has_get_num=true;
                }
            }
        };
    }
    private int avgRSSI(ArrayList arrayList){
        int total=0;
        for(int i=0;i<arrayList.size();i++)
            total+=(int)arrayList.get(i);
        return total/arrayList.size();
    }
}


