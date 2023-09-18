package com.carlkarenfort.test

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bytedream.untis4j.Session
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime


class ApiCalls {
    //tag for logging text
    private val TAG = "ApiCalls"

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        }
        return false
    }

    //this function returns the next working day
    fun getNextWorkingDay(): LocalDate {
        Log.i(TAG, "called getNextWorkingDay")
        var nextWorkingDay = LocalDate.now()

        // Check if the next day is a weekend (Saturday or Sunday)
        while (nextWorkingDay.dayOfWeek == DayOfWeek.SATURDAY || nextWorkingDay.dayOfWeek == DayOfWeek.SUNDAY) {
            nextWorkingDay = nextWorkingDay.plusDays(1)
        }

        return nextWorkingDay
    }


    //this function takes login data and creates a session to verify that it is valid
    //returns true if data is valid
    //returns false if data is invalid
    //returns null if there is no server connection
    fun verifyLoginData(
        username: String,
        password: String,
        server: String,
        schoolName: String
    ): Boolean {
        var state = false
        //use run blocking, to stop main thread so API call can be made
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                try {
                    Session.login(
                        username,
                        password,
                        server,
                        schoolName
                    )
                    //set return value to true after successful login
                    state = true
                } catch (_: IOException) {
                    Log.i(TAG, "invalid credentials")
                }
                // TODO: Proper error messages, for example distinguish between when there is no server connection and invalid credentials
            }
            job.join()
        }
        return state
    }

    //takes login data and name of user to determine the users webuntis ID
    //returns id of student or null if no student was found
    fun getID(
        username: String,
        password: String,
        server: String,
        schoolName: String,
        foreName: String,
        longName: String
    ): Int? {
        var id: Int? = null

        //has to stop main thread since it is called during welcome activity
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                try {
                    //login to API
                    val session = Session.login(
                        username,
                        password,
                        server,
                        schoolName
                    )

                    //get a list of all students and their IDs
                    val response = session.getCustomData("getStudents")
                    //check if request was wrong in some way
                    if (response.isError) {
                        Log.i(TAG, "invalid request")
                    } else {
                        //parse to json
                        val resultArray = response.response.getJSONArray("result")

                        //Iterate over every student and check if fore and long name match
                        for (i in 0 until resultArray.length()) {
                            val entry = resultArray.getJSONObject(i)
                            val entryForeName = entry.getString("foreName")
                            val entryLongName = entry.getString("longName")

                            // if so set return value as the current students id and stop loop
                            if (entryForeName.contains(foreName) && entryLongName.contains(longName)) {
                                id = entry.getInt("id")
                                break
                            }
                        }
                    }
                } catch (_: IOException) {
                    Log.i(TAG, "invalid login credentials")
                }
            }
            job.join()
        }
        return id
    }
    //TODO: add proper error handeling to getID function
    //TODO: check out possibility of getting id from username and password id getID function



    fun getSchoolStartForDay(
        username: String,
        password: String,
        server: String,
        schoolName: String,
        id: Int,
        day: LocalDate
    ): LocalTime? {
        try {
            //login to API
            val session = Session.login(
                username,
                password,
                server,
                schoolName
            )
            //get timetable from users id
            val timetable = session.getTimetableFromPersonId(
                day,
                day,
                id
            )
            //create return variable
            var firstLessonStartTime: LocalTime? = null

            //iterate over every lesson and keep the highest
            for (i in timetable.indices) {
                val startTime = timetable[i].startTime ?: continue
                if (firstLessonStartTime == null || startTime.isBefore(firstLessonStartTime)) {
                    firstLessonStartTime = startTime
                }
            }
            return firstLessonStartTime
        } catch (e: IOException) {
            return null
        }
    } //TODO: proper error exceptions in getSchoolStart
}