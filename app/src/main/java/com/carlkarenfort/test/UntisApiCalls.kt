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
    private val tag = "UntisApiCalls"

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
                    Log.i(tag, "person ID is : $id")

                } catch (e: IOException) {
                    Log.i(tag, "error")
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
                Log.i("Untis api calls", "WHAT THE FUCK")
                return null
            }
            Log.i(tag, timetable.teachers.toString())
            //create return variable
            timetable.sortByDate()
            timetable.sortByStartTime()
            Log.i(tag, timetable.toString())
            for (i in timetable){
                if (!i.teachers.isEmpty() && i.teachers != null) {
                    val firstLessonStartTime: LocalTime = i.startTime
                    session.logout()
                    return firstLessonStartTime
                }
            }
            return null
            // TODO: Other Implementations


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
            Log.i(tag,e.toString())
            return null
        }
    } //TODO: proper error exceptions in getSchoolStart

}