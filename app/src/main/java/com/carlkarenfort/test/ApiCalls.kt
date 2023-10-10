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
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
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

    fun getSchools() {
        //NEED A FUCKING ACESS TOKEN
        val accessToken: String = ""

        try {
            val url = URL("https://api.webuntis.com/ims/oneroster/v1p1/schools")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
            connection.setRequestProperty("Authorization", "Bearer $accessToken")

            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val input = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (input.readLine().also { line = it } != null) {
                    response.append(line)
                }
                input.close()

                // Parse and handle the JSON response here
                val jsonResponse = response.toString()
                // Log the response
                Log.i(TAG, jsonResponse)
            } else {
                // Handle non-OK HTTP response (e.g., error response from the API)
                Log.e(TAG, "HTTP Response Code: $responseCode")
            }

            connection.disconnect()
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
    //TODO: check out possibility of getting id from username and password id getID function



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