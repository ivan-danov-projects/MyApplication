package com.example.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import androidx.core.content.FileProvider;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import com.example.myapplication.BuildConfig;

public class SelfUpgradeService extends Service {
    private final String TAG = "XLIB SelfUpgrade service";
    public final int DownloadTimeoutError = 20;
    public final int DownloadTimeout = 2 * 60;
    private String serverURL;
    private final String jsonFile = "upgrade.json";
    private final String downloadFileName = "newVersion.apk";

    private File localFilesDir;
    private Intent intent;

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        Log.d(TAG, "onStartCommand(SelfUpgradeService)");
        intent = i;
        serverURL = i.getStringExtra("serverURL");

        //String destination = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/";
        File externalCacheDir = getExternalCacheDir();
        if (externalCacheDir == null)
            return Service.START_REDELIVER_INTENT;
        String destination = externalCacheDir.getAbsolutePath();
        Log.d(TAG, "onStartCommand() download dir '" + destination + "'");
        localFilesDir = new File(destination);

        new Thread(() -> {
            try {
                int timeout = DownloadTimeout;
                int res = downloadNewVersion(BuildConfig.VERSION_NAME);
                if (res == 1) {
                    //
                    realUpgrade(this);
                } else if (res < 0) {
                    timeout = DownloadTimeoutError;
                }
                Log.d(TAG, "onStartCommand() startService after " + timeout + " seconds");
                timeout *= 1000; // milliseconds

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        while(true) {
                            try {
                                startService(intent);
                                Log.d(TAG, "onStartCommand() startService");
                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException interruptedException) {
                                    interruptedException.printStackTrace();
                                }
                            }
                        }
                    }
                }, timeout);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        Log.d(TAG, "onStartCommand() done");
        return START_STICKY;
    }

    public int downloadNewVersion(String currentVersion) {

        Log.d(TAG, "downloadNewVersion() started");

        Log.d(TAG, "Delete file " + jsonFile);
        if (FileUtils.fileDeleteIfExists(jsonFile) != 0) {
            Log.e(TAG, "deleteFileIfExists(" + jsonFile + ") fail");
            return -1;
        }
        Log.d(TAG, "File " + jsonFile + " deleted");

        String downloadUrl = serverURL;

        Log.d(TAG, "Download " + jsonFile + " from " + downloadUrl);
        if (FileUtils.fileFromUrl(downloadUrl, jsonFile) != 0) {
            Log.e(TAG, "FileUtils.fileFromUrl(" + downloadUrl + "," + jsonFile + ") fail");
            return -1;
        }
        Log.d(TAG, "File '" + jsonFile + "' downloaded from " + downloadUrl);

        Log.d(TAG, "Convert to json");
        JSONObject json = FileUtils.fileAsJson(jsonFile);
        if (json == null) {
            Log.e(TAG, "File '" + jsonFile + "' downloaded from " + downloadUrl + " is not a json file");
            return -1;
        }

        String serverVersion;
        String serverVersionUrl;
        try {
            serverVersion = json.getString("version");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "No version in json: " + json);
            return -1;
        }
        Log.d(TAG, "currentVersion " + currentVersion + ", serverVersion " + serverVersion);

        if (Objects.equals(currentVersion, serverVersion)) {
            Log.d(TAG, "Delete file " + jsonFile);
            if (FileUtils.fileDeleteIfExists(jsonFile) != 0) {
                Log.e(TAG, "deleteFileIfExists(" + jsonFile + ") fail");
                return -1;
            }
            Log.d(TAG, "File " + jsonFile + " deleted");
            return 0;
        }

        Log.d(TAG, "NEW Version found");

        Log.d(TAG, "Delete old version file " + downloadFileName);
        if (FileUtils.fileDeleteIfExists(downloadFileName) != 0) {
            Log.e(TAG, "deleteFileIfExists(" + downloadFileName + ") fail");
            return -1;
        }
        Log.d(TAG, "File " + downloadFileName + " deleted");


        try {
            serverVersionUrl = json.getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "No url in json: " + json);
            return -1;
        }
        Log.d(TAG, "Download new version from  " + serverVersionUrl);

        String absDownloadFileName = localFilesDir + "/" + downloadFileName;

        // Download file
        Log.d(TAG, "Download new version '" + serverVersionUrl + "' to '" + absDownloadFileName + "'");
        if (FileUtils.fileFromUrl(serverVersionUrl, downloadFileName) != 0) {
            Log.e(TAG, "FileUtils.fileFromUrl(" + serverVersionUrl + ") fail");
            return -1;
        }
        Log.d(TAG, "New version '" + absDownloadFileName + "' downloaded from " + serverVersionUrl);
        return 1;
    }

    private void realUpgrade(Context context) {
        Uri fileURI;
        try {
            //File file = new File(localFilesDir + downloadFileName);
            fileURI = FileProvider.getUriForFile(this,
                                                 getApplicationContext().getPackageName() +
                                                 ".provider",
                                                 new File(localFilesDir + "/" + downloadFileName));
        } catch (Exception ex) {
            Log.d(TAG, "realUpgrade(FileProvider.getUriForFile) error");
            ex.printStackTrace();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        //Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setDataAndType(fileURI,//Uri.fromFile(file),
                //"application/android.com.app");
                "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);

        StrictMode.VmPolicy oldVmPolicy = null;

        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            oldVmPolicy = StrictMode.getVmPolicy();
            StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder()
                .penaltyLog()
                .build();
            StrictMode.setVmPolicy(policy);
        //}
        context.startActivity(intent);

        if (oldVmPolicy != null) {
            StrictMode.setVmPolicy(oldVmPolicy);
        }
    }
}
