package com.carlkarenfort.test

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bytedream.untis4j.Session
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime


class UntisApiCalls constructor(
    username: String,
    password: String,
    server: String,
    schoolName: String
){

    //tag for logging text
    private val TAG = "UntisApiCalls"

    val session: Session

    init {
        try {
            this.session = Session.login(
                username,
                password,
                server,
                schoolName
            )
        } catch (e: IOException) {
            throw e // does that cancel the object creation?
        }
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