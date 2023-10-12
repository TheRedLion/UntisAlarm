package com.carlkarenfort.test

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bytedream.untis4j.LoginException
import org.bytedream.untis4j.Session
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
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

    fun getSchools(searchString: String) {
        try {
            val url = URL("https://mobile.webuntis.com/ms/schoolquery2")


            val connection = url.openConnection() as HttpURLConnection

            val requestBody = """{"id": "wu_schulsuche-1697008128606", "method": "searchSchool", "params": [{"search": "$searchString"}], "jsonrpc": "2.0"}"""


            connection.requestMethod = "POST"
            connection.instanceFollowRedirects = true
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            connection.setRequestProperty("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7");
            connection.setRequestProperty("Content-Length", "115");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Origin", "https://webuntis.com");
            connection.setRequestProperty("Referer", "https://webuntis.com/");
            connection.setRequestProperty("Sec-Ch-Ua-Mobile", "?0");
            connection.setRequestProperty("Sec-Fetch-Dest", "empty");
            connection.setRequestProperty("Sec-Fetch-Mode", "cors");
            connection.setRequestProperty("Sec-Fetch-Site", "same-site");
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.writeBytes(requestBody)


            val input: BufferedReader = try {
                BufferedReader(InputStreamReader(connection.inputStream))
            } catch (var15: NullPointerException) {
                BufferedReader(InputStreamReader(connection.errorStream))
            }

            val stringBuilder = StringBuilder()

            var line: String?
            while (input.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }

            val jsonObject: JSONObject
            val result: JSONObject
            try {
                jsonObject = JSONObject(stringBuilder.toString())
                if (jsonObject.has("error")) {
                    result = jsonObject.getJSONObject("error")
                    val var10002 = result.getInt("errorObject")
                    throw LoginException(
                        "The response contains an error (" + var10002 + "): " + result.getString(
                            "message"
                        )
                    )
                }
            } catch (var16: JSONException) {
                throw IOException("An unexpected exception occurred: $stringBuilder")
            }

            result = jsonObject.getJSONObject("result")
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