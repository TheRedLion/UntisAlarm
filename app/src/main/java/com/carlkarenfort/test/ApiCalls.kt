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
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime


class ApiCalls constructor(
    usernameC: String,
    passwordC: String,
    serverC: String,
    schoolNameC: String
){

    //tag for logging text
    private val TAG = "ApiCalls"


    private var username: String = ""
    private var password: String = ""
    private var server: String = ""
    private var schoolName: String = ""

    init {
        username = usernameC
        password = passwordC
        server = serverC
        schoolName = schoolNameC
        //Log.i(TAG, "created ApiCalls Object with login Data: $username, $password, $server, $schoolName.")
    }

    fun getSchools(searchString: String) {
        val header = mapOf(
            "Accept" to "application/json, text/plain, */*",
            "Accept-Encoding" to "gzip, deflate, br",
            "Accept-Language" to "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7",
            "Content-Length" to "115",
            "Content-Type" to "application/json",
            "Origin" to "https://webuntis.com",
            "Referer" to "https://webuntis.com",
            "Sec-Ch-Ua-Mobile" to "?0",
            "Sec-Fetch-Dest" to "empty",
            "Sec-Fetch-Mode" to "cors",
            "Sec-Fetch-Site" to "same-site"
        )

        val data_t = mapOf(
            "id" to "wu_schulsuche-1697008128606",
            "method" to "searchSchool",
            "params" to listOf(mapOf("search" to "werner von siemens")),
            "jsonrpc" to "2.0"
        )


        val response = post(
            url = "https://mobile.webuntis.com/ms/schoolquery2",
            headers = header,
            json = data_t
        )

        val jsonResponse = response.jsonObject
        Log.i(TAG, jsonResponse)
        } catch (e: Exception) {
            // Handle exceptions (e.g., network errors)
            Log.e(TAG, "Error: ${e.message}")
        }

    }

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
        //Log.i(TAG, "called getNextDay()")
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
    fun verifyLoginData(): Boolean {
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
    fun getID(): Int? {
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

                    id = session.infos.personId
                    Log.i(TAG, "person ID is : $id")

                } catch (e: IOException) {
                    Log.i(TAG, "error")
                    e.printStackTrace()
                }
            }
            job.join()
        }
        return id
    }
    //TODO: add proper error handeling to getID function

    fun getSchoolStartForDay(
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
                //Log.i(TAG, timetable[i].teachers.toString())
                val startTime = timetable[i].startTime ?: continue
                if (firstLessonStartTime == null || startTime.isBefore(firstLessonStartTime)) {
                    if (timetable[i].originalTeachers.isEmpty()) {
                        firstLessonStartTime = startTime
                        //Log.i(TAG, startTime.toString())
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