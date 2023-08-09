package com.carlkarenfort.test

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bytedream.untis4j.Session
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class ApiCalls {
    private val TAG = "ApiCalls"
    var username: String? = null
    var password: String? = null
    var server: String? = null
    var schoolName: String? = null

    var scope = CoroutineScope(Job() + Dispatchers.Main)
    fun test() {
        scope.launch {
            var session = Session.login(
                "KarenfCar",
                "Mytimetable1!",
                "https://nessa.webuntis.com/",
                "gym-beskidenstrasse"
            )
            Log.i(TAG, "yoooo")
        }
    }
    /*
    fun test() {
        val id = 0
        try {
            var session = Session.login(
                "KarenfCar",
                "Mytimetable1!",
                "https://nessa.webuntis.com/",
                "gym-beskidenstrasse"
            )
            Log.i(TAG, "yoooo")
            //Timetable timetable = session.getTimetableFromPersonId(LocalDate.of(2023. 07, 04),LocalDate.of(2023. 07, 04), )
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return id
    }

     */
}