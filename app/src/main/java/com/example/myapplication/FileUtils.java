package com.example.myapplication;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * XLIB Android File utils
 * NOTE: set from main activity before use!!!
 * example:
 * FileUtils.localFilesDir = getFilesDir();
 */
public class FileUtils {
    private static final String TAG = "XLIB FileUtils";
    static File localFilesDir;

    /**
     * Read stream to string
     * @param is input stream
     * @return String or null
     */
    static public String streamAsString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                sb.append(line);
                firstLine = false;
            } else {
                sb.append("\n").append(line);
            }
        }
        reader.close();
        return sb.toString();
    }

    /**
     * Read local json file to string
     * @param filename input file name
     * @return String or null
     */
    static public String fileAsString(String filename) {
        try {
            File fl = new File(localFilesDir, filename);
            FileInputStream fin = new FileInputStream(fl);
            String ret = streamAsString(fin);
            //Make sure you close all streams.
            fin.close();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Read local json file to json object
     * @param filename input file name
     * @return JSONObject or null
     */
    static public JSONObject fileAsJson(String filename) {
        String jsonString = fileAsString(filename);
        if (jsonString == null)
            return null;
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Download file from url to local app directory
     * @param url url
     * @param filename output file name
     * @return 0 if ok, -1 otherwise
     */
    static public int fileFromUrl(String url, String filename) {
        try {
            URL fUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) fUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            //urlConnection.addRequestProperty("User-Agent", Constants.SDK_USER_AGENT);
            //urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setDoOutput(true);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(5000);
            urlConnection.connect();

            File file = new File(localFilesDir, filename);
            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = urlConnection.getInputStream();

            byte[] buffer = new byte[1024];
            int bufferLength;
            int fileLength = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                fileLength += bufferLength;
            }
            Log.d(TAG, "File '" + file.getAbsolutePath() + "'(" + fileLength +
                  ") downloaded from '" + fUrl + "'");
            fileOutput.close();
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: address '" + url + "', filename '" +
                    localFilesDir + '/' + filename + "'");
            e.printStackTrace();
            return -1;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: address '" + url + "', filename '" +
                    localFilesDir + '/' + filename + "'");
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            Log.e(TAG, "IOException: address '" + url + "', filename '" +
                    localFilesDir + '/' + filename + "'");
            e.printStackTrace();
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "Exception: address '" + url + "', filename '" +
                    localFilesDir + '/' + filename + "'");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /**
     * Delete file if exists
     * @param filename file to delete
     * @return 0 if ok, -1 otherwise
     */
    static public int fileDeleteIfExists(String filename) {
        File file = new File(localFilesDir, filename);
        if (file.exists()) {
            if (file.delete()) {
                Log.d(TAG, "file Deleted :" + filename);
                return 0;
            } else {
                Log.d(TAG, "file not Deleted :" + filename);
                return -1;
            }
        }
        return 0;
    }

    /**
     * Write string to file
     * @param filename output file name
     * @param data string to write
     * @return 0 if ok, -1 otherwise
     */
    static public int fileFromString(String filename, String data) {
        try {
            File file = new File(localFilesDir, filename);
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(data.getBytes());
            } catch (IOException e) {
                stream.close();
                return -1;
            } finally {
                stream.close();
            }
            return 0;
        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e);
        }
        return -1;
    }

    /**
     * Show recursive list of files and directories of directory with Log.d
     * @param dir directory to delete
     */
    static public void dirListRecursive(File dir) {
        if (!dir.exists()) {
            Log.d(TAG, "NOTE: directory '" + dir.getAbsolutePath() + "' not found");
            return;
        }
        if (!dir.isDirectory()) {
            Log.d(TAG, "NOTE: directory '" + dir.getAbsolutePath() + "' is file");
            return;
        }
        Log.d(TAG, dir.getAbsolutePath() + ": DIRECTORY");
        for (File child : Objects.requireNonNull(dir.listFiles())) {
            if (child.isDirectory()) {
                dirListRecursive(child);
                continue;
            }
            Log.d(TAG, child.getAbsolutePath() + ": " + child.length());
        }
    }

    /**
     * Delete file or directory recursive (with directory)
     * @param fileOrDirectory file or directory to delete
     * @return 0 if ok, -1 otherwise
     */
    static public int deleteRecursive(File fileOrDirectory) {
        if (!fileOrDirectory.exists()) {
            return 0;
        }
        if (fileOrDirectory.isDirectory())
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles()))
                if (deleteRecursive(child) != 0)
                    return -1;
        if (!fileOrDirectory.delete()) {
            return -1;
        }
        return 0;
    }

    /**
     * Delete children of directory recursive (without directory)
     * @param dir directory to delete
     * @return 0 if ok, -1 otherwise
     */
    @SuppressWarnings("unused")
    static public int deleteRecursiveChildren(File dir) {
        if (!dir.exists()) {
            return 0;
        }
        if (!dir.isDirectory()) {
            return 0;
        }
        for (File child : Objects.requireNonNull(dir.listFiles())) {
            if (child.isDirectory()) {
                if (deleteRecursiveChildren(child) != 0)
                    return -1;
            }
            Log.d(TAG, "Delete " + child.getAbsolutePath());
            if (!child.delete()) {
                Log.d(TAG, "Error Deleting " + child.getAbsolutePath());
                return -1;
            }
        }
        return 0;
    }
}
