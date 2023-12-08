package eu.karenfort.main.api

import android.util.Log
import eu.karenfort.main.helper.getNextDay
import kotlinx.coroutines.runBlocking
import org.bytedream.untis4j.Session
import org.bytedream.untis4j.responseObjects.Timetable
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


class UntisApiCalls(
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
            throw e // IO exeption throw is used to check if login data was valid
        }
    }

    fun getID(): Int? {
        var id: Int? = null
        //has to stop main thread since it is called during welcome activity

        try {
            //login to API
            id = session.infos.personId
        } catch (e: IOException) {
            Log.i(TAG, "error")
            e.printStackTrace()
        }

        return id
    }

    fun getSchoolStartForDay(id: Int): LocalDateTime? {
        Log.i(TAG, "getting schoolstart")
        try {
            val nextDay = getNextDay()
            val timetable: Timetable = session.getTimetableFromPersonId(
                nextDay,
                nextDay,
                id
            )

            if (timetable.isEmpty()) {
                Log.i(TAG, "timetable is empty")
                return null
            }

            timetable.sortByDate()
            timetable.sortByStartTime()
            for (i in timetable){
                if (!lessonIsCancelled(i)) {
                    val firstLessonStartTime: LocalDateTime = LocalDateTime.of(nextDay, i.startTime)
                    session.logout()
                    getNextDay()
                    return firstLessonStartTime
                }
            }
            return null
        } catch (e: IOException) {
            Log.i(TAG,e.toString())
            return null
        }
    }

    private fun lessonIsCancelled(lesson: Timetable.Lesson): Boolean {
        return lesson.teachers.isEmpty() && lesson.teachers != null
    }
}