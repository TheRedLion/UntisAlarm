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
    fun getNextDay(): LocalDate {
        Log.i(TAG, "called getNextDay()")
        var nextDay = LocalDate.now()
        nextDay = nextDay.plusDays(1)

        // Check if the next day is a weekend (Saturday or Sunday)
        while (nextDay.dayOfWeek == DayOfWeek.SATURDAY || nextDay.dayOfWeek == DayOfWeek.SUNDAY) {
            nextDay = nextDay.plusDays(1)
        }

        return nextDay
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
                } catch (e: IOException) {
                    Log.i(TAG, "invalid credentials")
                    error(e)
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

                    //test
                    val map = mutableMapOf<String, Any>()
                    map["id"] = 433
                    val out = session.getCustomData("getStudent", map)

                    Log.i(TAG, out.toString())

                    //previous
                    Log.i(TAG, session.getCustomData("getStudents").toString())
                    Log.i(TAG, session.infos.toString())

                    //get a list of all students and their IDs
                    val response = session.getCustomData("getStudents")
                    //check if request was wrong in some way
                    if (response.isError) {
                        Log.i(TAG, "invalid request")
                    } else {
                        //parse to json
                        val resultArray = response.response.getJSONArray("result")
                    }
                } catch (e: IOException) {
                    Log.i(TAG, "invalid login credentials")
                    e.printStackTrace()
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
            ) ?: return null
            //get timetable from users id
            val timetable = session.getTimetableFromPersonId(
                day,//LocalDate.of(2023,9,5),
                day,//LocalDate.of(2023,9,5),
                id
            ) ?: return null
            //create return variable
            var firstLessonStartTime: LocalTime? = null

            //iterate over every lesson and keep the highest
            for (i in timetable.indices) {
                Log.i(TAG, timetable[i].teachers.toString())
                val startTime = timetable[i].startTime ?: continue
                if (firstLessonStartTime == null || startTime.isBefore(firstLessonStartTime)) {
                    if (timetable[i].originalTeachers.isEmpty()) {
                        firstLessonStartTime = startTime
                        Log.i(TAG, startTime.toString())
                    }
                }
            }
            session.logout()
            return firstLessonStartTime
        } catch (e: IOException) {
            return null
        }
    } //TODO: proper error exceptions in getSchoolStart
}