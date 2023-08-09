package com.carlkarenfort.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.bytedream.untis4j.Session
import java.time.LocalDate

class ApiCalls {
    private val TAG = "ApiCalls"
    var username: String? = null
    var password: String? = null
    var server: String? = null
    var schoolName: String? = null

    private var scope = CoroutineScope(Job() + Dispatchers.Main)
    fun test() {
        scope.launch {
            var session = Session.login(
                "KarenfCar",
                "Mytimetable1!",
                "https://nessa.webuntis.com/",
                "gym-beskidenstrasse"
            )
            var timetable = session.getTimetableFromPersonId(LocalDate.of(2023, 6,14),LocalDate.of(2023, 6,14),436)

            for (i in 0 until timetable.size) {
                println("Lesson " + (i + 1) + ": " + timetable.get(i).getSubjects().toString())
            }
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