package com.carlkarenfort.test;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ExtractURLData extends AppCompatActivity {
    private static final String TAG = "ExtractURLData";

    //return Server from inputted URL
    //return an empty string if none was found
    public static String returnServerFromURL(String urlStr) {
        Log.i(TAG, "called returnServerFromURL");
        //take login url and extract the Server address
        try {
            URL url = new URL(urlStr);
            String protocol = url.getProtocol();
            String host = url.getHost();

            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return protocol + "://" + host;
        } catch (MalformedURLException e) {
            return "";
        }
    }

    //return School from inputted URL
    //return an empty string if none was found
    public static String returnSchoolFromURL(String urlStr) {
        Log.i(TAG, "returnSchoolFromURL");
        try {
            URI uri = new URI(urlStr);
            String query = uri.getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("school")) {
                        Log.i(TAG, "found School " + keyValue[1]);
                        return keyValue[1];
                    }
                }
            }
            return "";
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.err.println("Invalid URL format: " + urlStr);
            return "";
        }
    }
}
