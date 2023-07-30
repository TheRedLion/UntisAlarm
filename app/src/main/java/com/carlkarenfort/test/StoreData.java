package com.carlkarenfort.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.foundation.gestures.ForEachGestureKt;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class StoreData extends AppCompatActivity {
    private static String TAG = "StoreData";
    private static Integer untisID;
    private static String untisUsername;
    private static String untisPassword;
    private static String untisServer;
    private static String untisSchool;
    private static Integer timeBeforeSchool;
    private static Boolean alarmAcive;

    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String SERVER = "server";
    public static final String SCHOOL = "SCHOOL";
    public static final String TBS = "tbs";
    public static final String ACTIVE = "active";
    public static final String SHARED_PREFS = "sharedPrefs";
    //store data in shared prefs
    public static void storeData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(ID,untisID);
        editor.putString(USERNAME,untisUsername);
        editor.putString(PASSWORD,untisPassword);
        editor.putString(SERVER, untisServer);
        editor.putString(SCHOOL, untisSchool);
        editor.putInt(TBS,timeBeforeSchool);
        editor.putBoolean(ACTIVE,alarmAcive);

        Log.i(TAG, "in store data");
        Log.i(TAG, untisID.toString());
        Log.i(TAG, untisUsername);
        Log.i(TAG, untisPassword);
        Log.i(TAG, untisServer);
        Log.i(TAG, untisSchool);
        Log.i(TAG, timeBeforeSchool.toString());
        Log.i(TAG, alarmAcive.toString());
    }

    //retrieve data and store in class variables
    public static void loadData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        untisID = sharedPreferences.getInt(ID, 0);
        untisUsername = sharedPreferences.getString(USERNAME, "");
        untisPassword = sharedPreferences.getString(PASSWORD, "");
        untisServer = sharedPreferences.getString(SERVER, "");
        untisSchool = sharedPreferences.getString(SCHOOL, "");
        timeBeforeSchool = sharedPreferences.getInt(TBS, 60);
        alarmAcive = sharedPreferences.getBoolean(ACTIVE, false);

        //temp logs
        Log.i(TAG, "in load data");
        Log.i(TAG, untisID.toString());
        Log.i(TAG, untisUsername);
        Log.i(TAG, untisPassword);
        Log.i(TAG, untisServer);
        Log.i(TAG, untisSchool);
        Log.i(TAG, timeBeforeSchool.toString());
        Log.i(TAG, alarmAcive.toString());
    }

    //take login url and extract the Server adress
    public static String returnServerFromURL(String urlStr) {
        try {
            URL url = new URL(urlStr);
            String protocol = url.getProtocol();
            String host = url.getHost();

            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return protocol + "://" + host;
        } catch (MalformedURLException e) {
            System.err.println("Invalid URL format: " + urlStr);
            return null;
        }
    }

    //take login url and extract the school name
    public static String returnSchoolFromURL(String urlStr) {
        try {
            URI uri = new URI(urlStr);
            String query = uri.getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("school")) {
                        return keyValue[1];
                    }
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.err.println("Invalid URL format: " + urlStr);
            return null;
        }
        System.err.println("Invalid URL format: " + urlStr);
        return null;
    }

    public static Integer getUntisID() {
        return untisID;
    }

    public static void setUntisID(Integer untisID) {
        StoreData.untisID = untisID;
    }

    public static String getUntisUsername() {
        return untisUsername;
    }

    public static void setUntisUsername(String untisUsername) {
        StoreData.untisUsername = untisUsername;
    }

    public static String getUntisPassword() {
        return untisPassword;
    }

    public static void setUntisPassword(String untisPassword) {
        StoreData.untisPassword = untisPassword;
    }

    public static Integer getTimeBeforeSchool() {
        return timeBeforeSchool;
    }

    public static void setTimeBeforeSchool(Integer timeBeforeSchool) {
        StoreData.timeBeforeSchool = timeBeforeSchool;
    }

    public static Boolean getAlarmAcive() {
        return alarmAcive;
    }

    public static void setAlarmAcive(Boolean alarmAcive) {
        StoreData.alarmAcive = alarmAcive;
    }

    public static String getUntisServer() {
        return untisServer;
    }

    public static void setUntisServer(String untisServer) {
        StoreData.untisServer = untisServer;
    }

    public static String getUntisSchool() {
        return untisSchool;
    }

    public static void setUntisSchool(String untisSchool) {
        StoreData.untisSchool = untisSchool;
    }
}
