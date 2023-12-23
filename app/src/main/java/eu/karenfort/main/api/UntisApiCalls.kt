/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This class is responsible for sending API calls to the WebUntis API.
 *      We are using the Untis4J library https://github.com/untisapi/untis4j.
 */
package eu.karenfort.main.api

import android.util.Log
import eu.karenfort.main.helper.getNextDay
import org.bytedream.untis4j.Session
import org.bytedream.untis4j.UntisUtils
import org.bytedream.untis4j.responseObjects.Timetable
import java.io.IOException
import java.time.LocalDateTime


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
            throw e // IO exception is used to check if login data was valid
        }
    }

    fun getID(): Int? {
        var id: Int? = null

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
        try {
            val nextDay = getNextDay()
            val timetable: Timetable = session.getTimetableFromPersonId(
                nextDay,
                nextDay,
                id
            )

            if (timetable.isEmpty()) {
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
        if (lesson.teachers.isEmpty() && lesson.teachers != null) {
            return true
        }
        if (lesson.code == UntisUtils.LessonCode.CANCELLED) {
            return true
        }
        return false
    }
}