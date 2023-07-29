package com.carlkarenfort.test;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.carlkarenfort.test.MainActivity;

import org.bytedream.untis4j.LoginException;
import org.bytedream.untis4j.Session;
import org.bytedream.untis4j.responseObjects.Timetable;

import java.io.IOException;
import java.sql.Time;
import java.time.LocalDate;

public class ApiCalls {
    private static String TAG = "ApiCalls";
    private static String username;
    private static String password;
    private static String server;
    private static String schoolName;
    private static Integer untisID;

    //public method to start thread

    public static void APIcall() {
        Log.i(TAG,"makeAPIcall");
        APIthread.start();
    }

    //new thread for network activity

    static Thread APIthread = new Thread(new Runnable() {
        @Override
        public void run() {
            try  {
                //code on this thread
                Log.i(TAG,"inthread");
                call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    private static void call() {
        //currently testcode from untis4j
        try {
            Log.i(TAG,"logging in");
            Log.i(TAG,"credentials: " + " " + username + " " + password + " " + server + " " + schoolName);
            Session session = Session.login(
                    "KarenfCar",
                    "Mytimetable1!",
                    "https://nessa.webuntis.com",
                    "gym-beskidenstrasse"
            ); // create a new webuntis session
            Log.i(TAG,session.getSubjects().toString());
            // get the timetable and print every lesson
            Timetable timetable = session.getTimetableFromPersonId(LocalDate.of(2023,6,21), LocalDate.of(2023,6,21), untisID);
            for (int i = 0; i < timetable.size(); i++) {
                Log.i("apiCalls","Lesson " + (i+1) + ": " + timetable.get(i).getSubjects().toString());
            }

            // logout
            session.logout();
        } catch (LoginException e) {
            // this exception get thrown if something went wrong with Session.login
            System.out.println("Failed to login: " + e.getMessage());
        } catch (IOException e) {
            // if an error appears this get thrown
            e.printStackTrace();
        }
    }

    public static void setUsername(String username) {
        ApiCalls.username = username;
    }

    public static void setPassword(String password) {
        ApiCalls.password = password;
    }

    public static void setServer(String server) {
        ApiCalls.server = server;
    }

    public static void setSchoolName(String schoolName) {
        ApiCalls.schoolName = schoolName;
    }
    public static void setUntisID(Integer untisID) {
        ApiCalls.untisID = untisID;
    }


}
