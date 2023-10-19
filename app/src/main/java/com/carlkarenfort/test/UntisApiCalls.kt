package com.carlkarenfort.test

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bytedream.untis4j.Session
import org.bytedream.untis4j.responseObjects.Timetable
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

    private val session: Session

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
    //TODO: add proper error handling to getID function

    fun getSchoolStartForDay(
        id: Int
    ): LocalTime? {
        try {
            //get timetable from users id
            val today = LocalDate.now()
            val timetable: Timetable = session.getTimetableFromPersonId(
                today,
                today.plusDays(7),
                id
            )
            if (timetable.isEmpty()) {
                return null
            }
            Log.i(TAG, timetable.teachers.toString())
            //create return variable
            timetable.sortByDate()
            timetable.sortByStartTime()
            Log.i(TAG, timetable.toString())
            var i: Int = 0
            for (i in timetable){
            if (!i.teachers.isEmpty() && i.teachers != null) {
                var firstLessonStartTime: LocalTime = i.startTime
                session.logout()
                return firstLessonStartTime
            }}
            return null
            // TODO: Andere Schulen


            //iterate over every lesson and keep the highest
            /*for (i in timetable.indices) {
                //Log.i(TAG, timetable[i].teachers.toString())
                // if null continue
                val startTime = timetable[i].startTime ?: continue
                if (firstLessonStartTime == null || startTime.isBefore(firstLessonStartTime)) {
                    if (!timetable[i].originalTeachers.isEmpty()) {
                        firstLessonStartTime = startTime
                        //Log.i(TAG, startTime.toString())
                    }
                }
            }*/

        } catch (e: IOException) {
            Log.i(TAG,e.toString())
            return null
        }
    } //TODO: proper error exceptions in getSchoolStart

    fun timeTableTest(id: Int, day: LocalDate) {
        val timetable = session.getTimetableFromPersonId(
            day,//LocalDate.of(2023,10,10),
            day,//LocalDate.of(2023,10,10),
            id
        )

        for (i in timetable.indices) {
            Log.i(TAG, timetable[i].toString())
            Log.i(TAG, "{subjects: ${timetable[i].subjects}, original subjects: ${timetable[i].originalSubjects}, teachers: ${timetable[i].teachers}, original teachers:${timetable[i].originalTeachers}, classes: ${timetable[i].classes}, original classes: ${timetable[i].originalClasses}, rooms: ${timetable[i].rooms}, original rooms: ${timetable[i].originalRooms}, activity type: ${timetable[i].activityType}, code: ${timetable[i].code}, timeuntiObject:${timetable[i].timeUnitObject}}")
        }

    }
}