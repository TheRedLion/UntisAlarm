package com.carlkarenfort.test;

import androidx.appcompat.app.AppCompatActivity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ExtractURLData extends AppCompatActivity {
    private static final String TAG = "StoreData";
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
