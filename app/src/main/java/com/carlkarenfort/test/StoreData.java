package com.carlkarenfort.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.foundation.gestures.ForEachGestureKt;
import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class StoreData extends AppCompatActivity {
    private static final String TAG = "StoreData";

    public static final Preferences.Key<Integer> ID = PreferencesKeys.intKey("id");
    public static final Preferences.Key<String> USERNAME = PreferencesKeys.stringKey("username");
    public static final Preferences.Key<String> PASSWORD = PreferencesKeys.stringKey("password");
    public static final Preferences.Key<String> SERVER = PreferencesKeys.stringKey("server");
    public static final Preferences.Key<String> SCHOOL = PreferencesKeys.stringKey("school");
    public static final Preferences.Key<Integer> TBS = PreferencesKeys.intKey("tbs");
    public static final Preferences.Key<Boolean> ACTIVE = PreferencesKeys.booleanKey("active");
    //store data in shared prefs
    public static void storeUntisData(Context context, Integer untisID, String untisUsername, String untisPassword, String untisServer, String untisSchool) {
        Log.i(TAG,"in store data");
        // check that no inputs are null
        if (untisID == null || untisUsername == null || untisPassword == null || untisServer == null || untisSchool == null) {
            Log.i(TAG, "in storeUntisData: no input may be null");
            return;
        }

        RxDataStore<Preferences> dataStore = new RxPreferenceDataStoreBuilder(context, /*name=*/ "settings").build();
        Single<Preferences> updateResult =  dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();

            mutablePreferences.set(ID, untisID);
            mutablePreferences.set(USERNAME, untisUsername);
            mutablePreferences.set(PASSWORD, untisPassword);
            mutablePreferences.set(SERVER, untisServer);
            mutablePreferences.set(SCHOOL, untisSchool);

            return Single.just(mutablePreferences);
        });
    }

    //retrieve data and store in class variables
    public static Integer loadID(Context context) {
        RxDataStore<Preferences> dataStore = new RxPreferenceDataStoreBuilder(context, /*name=*/ "settings").build();
        //Flowable<Integer> flowID = dataStore.data().map(preferences -> preferences.get(ID));
        Single<Preferences> updateResult =  dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            Integer untisID = prefsIn.get(ID);

            return Single.just(mutablePreferences);
        });
        //temp, should return untisID
        return null;
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
}
