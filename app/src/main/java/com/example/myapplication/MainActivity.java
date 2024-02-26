package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public static final String newVersionServerURL = "https://a.danov.pro/MyApplication/version.json";
    public static Intent SelfUpgradeIntent;
    private static final String[] REQUIRED_PERMISSION_LIST = new String[] {
            Manifest.permission.VIBRATE, // Gimbal rotation
            Manifest.permission.INTERNET, // API requests
            Manifest.permission.ACCESS_WIFI_STATE, // WIFI connected products
            Manifest.permission.ACCESS_COARSE_LOCATION, // Maps
            Manifest.permission.ACCESS_NETWORK_STATE, // WIFI connected products
            Manifest.permission.ACCESS_FINE_LOCATION, // Maps
            Manifest.permission.CHANGE_WIFI_STATE, // Changing between WIFI and USB connection
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // Log files
            Manifest.permission.READ_EXTERNAL_STORAGE, // Log files
            Manifest.permission.READ_PHONE_STATE, // Device UUID accessed upon registration
            Manifest.permission.RECORD_AUDIO // Speaker accessory

            // //,Manifest.permission.RECEIVE_BOOT_COMPLETED
            // ,Manifest.permission.SYSTEM_ALERT_WINDOW
            // //, Manifest.permission.FOREGROUND_SERVICE
            // ,Manifest.permission.WAKE_LOCK
            ,Manifest.permission.REQUEST_INSTALL_PACKAGES
    };
    private static final int REQUEST_PERMISSION_CODE = 12345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.myapplication.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // initPermission();
        requestAppPermissions();
        // while (!checkAppPermissions()) { requestAppPermissions(); }

        startSelfUpgrade();
    }

    public void startSelfUpgrade() {
        Log.d("MainActivity", "startSelfUpgrade() SelfUpgradeService");
        FileUtils.localFilesDir = getFilesDir();
        SelfUpgradeIntent = new Intent(this, SelfUpgradeService.class);
        SelfUpgradeIntent.putExtra("localFilesDir", getFilesDir().toString());
        SelfUpgradeIntent.putExtra("serverURL", newVersionServerURL);
        startService(SelfUpgradeIntent);
        Log.d("MainActivity", "startSelfUpgrade() SelfUpgradeService done");
    }

    private List<String> getAppMissingPermissions() {
        // Check for permissions
        List<String> missingPermissions = new ArrayList<>();
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            Log.d(TAG, "Check perms " + eachPermission);
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Request perms " + eachPermission);
                missingPermissions.add(eachPermission);
            }
        }
        return missingPermissions;
    }

    public boolean checkAppPermissions() {
        return getAppMissingPermissions().isEmpty();
    }

    private void requestAppPermissions() {
        // Check for permissions
        List<String> missingPermission = getAppMissingPermissions();
        // Request for missing permissions
        if (!missingPermission.isEmpty()) {
            // if (UI_ != null) {
            //     UI_.showStatusText("Insufficient permissions!");
            // }
            String[] rp = missingPermission.toArray(new String[missingPermission.size()]);
            Log.d(TAG, "Request perms " + Arrays.toString(rp));

            ActivityCompat.requestPermissions(this, rp,
                    //missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }
    }




    private void initPermission() {
        String[] permissions = new String[] {
                Manifest.permission.VIBRATE, // Gimbal rotation
                Manifest.permission.INTERNET, // API requests
                Manifest.permission.ACCESS_WIFI_STATE, // WIFI connected products
                Manifest.permission.ACCESS_COARSE_LOCATION, // Maps
                Manifest.permission.ACCESS_NETWORK_STATE, // WIFI connected products
                Manifest.permission.ACCESS_FINE_LOCATION, // Maps
                Manifest.permission.CHANGE_WIFI_STATE, // Changing between WIFI and USB connection
                Manifest.permission.WRITE_EXTERNAL_STORAGE, // Log files
                Manifest.permission.READ_EXTERNAL_STORAGE, // Log files
                Manifest.permission.READ_PHONE_STATE, // Device UUID accessed upon registration
                Manifest.permission.RECORD_AUDIO // Speaker accessory


                //,Manifest.permission.INTERNET
                //,Manifest.permission.ACCESS_NETWORK_STATE
                // ,Manifest.permission.RECEIVE_BOOT_COMPLETED
                // ,Manifest.permission.SYSTEM_ALERT_WINDOW
                // ,Manifest.permission.FOREGROUND_SERVICE
                // ,Manifest.permission.WAKE_LOCK
                // ,Manifest.permission.REQUEST_INSTALL_PACKAGES

        };
        List<String> toApplyList = new ArrayList<>();
        for (String perm : permissions) {
            Log.d(TAG, "Check perms " + perm);
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                toApplyList.add(perm);
                Log.d(TAG, "Request perms " + perm);
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    toApplyList.toArray(new String[toApplyList.size()]),
                    12345);
        }
    }
    private void initPermission2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            ArrayList<String> toApplyList = new ArrayList<>();
            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(MainActivity.this, perm)) {
                    toApplyList.add(perm);
                }
            }
            String[] tmpList = new String[toApplyList.size()];
            if (!toApplyList.isEmpty()) {
                ActivityCompat.requestPermissions(MainActivity.this, toApplyList.toArray(tmpList), 100);
            }
        }
    }
}
