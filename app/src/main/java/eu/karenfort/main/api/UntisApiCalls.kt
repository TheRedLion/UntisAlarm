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
import org.bytedream.untis4j.Session
import org.bytedream.untis4j.UntisUtils
import org.bytedream.untis4j.responseObjects.Timetable
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime


class UntisApiCalls(
    username: String,
    password: String,
    server: String,
    schoolName: String
) {
    private val session: Session

    companion object {
        private const val TAG = "UntisApiCalls"
    }

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
            id = session.infos.personId
        } catch (e: IOException) {
            Log.i(TAG, "error")
            e.printStackTrace()
        }
        return id
    }

    fun getSchoolStartForDay(id: Int): LocalDateTime? {
        try {
            var nextDay = LocalDate.now().plusDays(1)
            var timetable: Timetable = session.getTimetableFromPersonId(
                nextDay,
                nextDay,
                id
            )

            while (timetable.isEmpty()) {
                nextDay = nextDay.plusDays(1)
                timetable = session.getTimetableFromPersonId(
                    nextDay,
                    //NOTE: getting a timeframe of 3 days since this always includes the weekend
                    nextDay.plusDays(3),
                    id
                )
            }

            //TODO: tempcode

            val params: HashMap<String, String> = HashMap()
            params["id"] = "436"
            params["type"] = "5"
            params["startDate"] = "19012024"
            params["endDate"] = "19012024"
            //TODO: Remove log or make conditional
            Log.i(
                TAG,
                session.getTimetableFromPersonId(LocalDate.now(), LocalDate.now(),
                    436).toString()
            )

            timetable.sortByDate()
            timetable.sortByStartTime()
            for (i in timetable) {
                if (!lessonIsCancelled(i)) {
                    val firstLessonStartTime: LocalDateTime = LocalDateTime.of(nextDay, i.startTime)
                    session.logout()
                    return firstLessonStartTime
                }
            }
            return null
        } catch (e: IOException) {
            //TODO: Remove log or make conditional
            Log.i(TAG, e.toString())
            return null
        }
    }

    private fun lessonIsCancelled(lesson: Timetable.Lesson): Boolean {
        if (lesson.teachers.isEmpty() && lesson.teachers != null) {
            return true
        } //if there is no teacher assigned there is no school

        return lesson.code == UntisUtils.LessonCode.CANCELLED
        //tod implement that CancelledMessage is checked
    }
}