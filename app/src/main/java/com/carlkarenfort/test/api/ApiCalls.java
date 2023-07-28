package com.carlkarenfort.test.api;

import android.util.Log;

import org.bytedream.untis4j.LoginException;
import org.bytedream.untis4j.Session;
import org.bytedream.untis4j.responseObjects.Timetable;

import java.io.IOException;
import java.time.LocalDate;


public class ApiCalls {
    public static void call(String username, String password) {
        try {
            Session session = Session.login(
                    "KarenfCar",
                    "Proscam15untis!",
                    "https://nessa.webuntis.com",
                    "gym-beskidenstrasse",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.75 Safari/537.36 Lenj94002@gmail.com"
            ); // create a new webuntis session

            // get the timetable and print every lesson
            Timetable timetable = session.getTimetableFromClassId(LocalDate.of(2023,6,21), LocalDate.of(2023,6,21), session.getInfos().getClassId());
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
}
