package com.carlkarenfort.test

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bytedream.untis4j.Session
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class ApiCalls {
    private val TAG = "ApiCalls"
    private var scope = CoroutineScope(Job() + Dispatchers.Main)

    fun verifyLoginData(
        username: String,
        password: String,
        server: String,
        schoolName: String
    ): Boolean {
        //Log.i(TAG, "inVerifyLoginData")
        var state = false
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                try {
                    var session = Session.login(
                        username,
                        password,
                        server,
                        schoolName
                    )
                    //Log.i(TAG, "logged in")
                    state = true
                } catch (_: IOException) {
                    Log.i(TAG, "invalid credentials")
                }
            }
            job.join()
        }
        return state
    }

    fun getID(
        username: String,
        password: String,
        server: String,
        schoolName: String,
        foreName: String,
        longName: String
    ): Int? {
        var id: Int? = null

        runBlocking {
            //Log.i(TAG, "inGetID")
            val job: Job = launch(context = Dispatchers.Default) {
                try {
                    val session = Session.login(
                        username,
                        password,
                        server,
                        schoolName
                    )
                    //Log.i(TAG, "logged in")
                    val response = session.getCustomData("getStudents")
                    if (response.isError) {
                        Log.i(TAG, "invalid request")
                    } else {
                        val resultArray = response.response.getJSONArray("result")
                        //Log.i(TAG, "$foreName $longName")
                        for (i in 0 until resultArray.length()) {
                            val entry = resultArray.getJSONObject(i)
                            val entryForeName = entry.getString("foreName")
                            val entryLongName = entry.getString("longName")

                            //temp
                            //val entryID = entry.getInt("id").toString()

                            //Log.i("SPAM", "Fore Name: $entryForeName Long Name: $entryLongName ID: $entryID")
                            if (entryForeName.contains(foreName) && entryLongName.contains(longName)) {
                                //Log.i(TAG, "found Match")
                                id = entry.getInt("id")
                                //Log.i(TAG, id.toString())
                                break
                            }
                        }
                    }
                } catch (_: IOException) {
                    Log.i(TAG, "invalid credentials")
                }
            }
            job.join()
        }

        //Log.i(TAG, "Returning id $id")
        return id
    }

    fun getSchoolStartForDay(
        username: String,
        password: String,
        server: String,
        schoolName: String,
        id: Int,
        day: LocalDate
    ): LocalTime? {
        try {
            val session = Session.login(
                username,
                password,
                server,
                schoolName
            )
            //Log.i(TAG, "")
            val timetable = session.getTimetableFromPersonId(
                day,
                day,
                id
            )
            //Log.i(TAG, timetable.toString())
            val formatter = DateTimeFormatter.ofPattern("HHmm")
            var earliestTime: LocalTime? = null

            for (i in timetable.indices) {
                //Log.i(TAG, timetable[i].toString())
                val startTime = timetable[i].startTime ?: continue
                //Log.i(TAG, startTime.toString())

                if (earliestTime == null || startTime.isBefore(earliestTime)) {
                    earliestTime = startTime
                    //Log.i(TAG, "new earliest: $earliestTime")
                }
            }
            return earliestTime
        } catch (e: IOException) {
            return null
        }
    }
}