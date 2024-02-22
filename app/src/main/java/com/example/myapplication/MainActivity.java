package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static final String newVersionServerURL = "https://a.danov.pro/MyApplication/version.json";
    public static Intent SelfUpgradeIntent;

    public void startSelfUpgrade() {
        Log.d("MainActivity", "startSelfUpgrade() SelfUpgradeService");
        FileUtils.localFilesDir = getFilesDir();
        SelfUpgradeIntent = new Intent(this, SelfUpgradeService.class);
        SelfUpgradeIntent.putExtra("localFilesDir", getFilesDir().toString());
        SelfUpgradeIntent.putExtra("serverURL", newVersionServerURL);
        startService(SelfUpgradeIntent);
        Log.d("MainActivity", "startSelfUpgrade() SelfUpgradeService done");
    }

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

        startSelfUpgrade();
    }

}
