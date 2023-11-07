package eu.karenfort.main.api

import android.util.Log
import eu.karenfort.main.helper.getNextDay
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
            throw e // TODO: does that cancel the object creation?
        }
    }

    fun getID(): Int? {
        var id: Int? = null
        //has to stop main thread since it is called during welcome activity
        runBlocking {
            try {
                //login to API
                id = session.infos.personId
            } catch (e: IOException) {
                Log.i(TAG, "error")
                e.printStackTrace()
                return@runBlocking
            }
        }
        return id
    }

    fun getSchoolStartForDay(id: Int): LocalTime? {
        try {
            val today = LocalDate.now()
            //val today = LocalDate.of(2023, 11, 6) //tod: remove debugging string

            val timetable: Timetable = session.getTimetableFromPersonId(
                getNextDay(),
                getNextDay(),
                id
            )

            if (timetable.isEmpty()) {
                Log.i(TAG, "timetable is empty")
                return null
            }

            timetable.sortByDate()
            timetable.sortByStartTime()
            Log.i(TAG, timetable.toString())
            for (i in timetable){
                if (!lessonIsCancelled(i)) {
                    val firstLessonStartTime: LocalTime = i.startTime
                    session.logout()
                    return firstLessonStartTime
                }
            }
            return null
            // TODO: Other Implementations
        } catch (e: IOException) {
            Log.i(TAG,e.toString())
            return null
        }
    }

    private fun lessonIsCancelled(lesson: Timetable.Lesson): Boolean {
        Log.i(TAG, lesson.toString())
        if (lesson.teachers.isEmpty() && lesson.teachers != null) return true
        return false
    }
}