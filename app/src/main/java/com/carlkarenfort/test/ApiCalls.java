package com.carlkarenfort.test;

import android.util.Log;

import org.bytedream.untis4j.LoginException;
import org.bytedream.untis4j.Session;
import org.bytedream.untis4j.responseObjects.Timetable;

import java.io.IOException;
import java.time.LocalDate;

public class ApiCalls implements Runnable {
    private final String TAG = "ApiCalls";
    private String foreName;
    private String longName;
    private String username;
    private String password;
    private String server;
    private String schoolName;
    private Integer untisID;
    //private static (dataType for Time) time;

    @Override
    public void run() {
        /*try  {
            //code on this thread
            Log.i(TAG,"logging in");
            Log.i(TAG,"credentials: " + " " + username + " " + password + " " + server + " " + schoolName);
            Session session = Session.login(
                    username,
                    password,
                    server,
                    schoolName
            ); // create a new webuntis session
            Log.i(TAG,session.getSubjects().toString());
            */
        Log.i(TAG,"setting untisID with " + foreName + " " + longName);
        untisID = 423; //temp value
            //set untisID to 0 if no match was found

            // logout
            //session.logout();
        /*} catch (LoginException e) {
            // this exception get thrown if something went wrong with Session.login
            e.printStackTrace();
        } catch (IOException e) {
            // if an error appears this get thrown
            e.printStackTrace();
        }*/
    }


    public void setUsername(String usernameIn) {
        username = usernameIn;
    }

    public void setPassword(String passwordIn) {
        password = passwordIn;
    }

    public void setServer(String serverIn) {
        server = serverIn;
    }

    public void setSchoolName(String schoolNameIn) {
        schoolName = schoolNameIn;
    }

    public void setUntisID(Integer untisIDIn) {
        untisID = untisIDIn;
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getServer() {
        return server;
    }
    public String getSchoolName() {
        return schoolName;
    }
    public Integer getUntisID() {
        return untisID;
    }

    public String getForeName() {
        return foreName;
    }
    public void setForeName(String foreNameIn) {
        foreName = foreNameIn;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longNameIn) {
        longName = longNameIn;
    }
}
