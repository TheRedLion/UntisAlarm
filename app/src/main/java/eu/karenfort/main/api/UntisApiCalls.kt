/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This class is responsible for sending API calls to the WebUntis API.
 *      We are using the Untis4J library https://github.com/untisapi/org.bytedream.untis4j.
 */
package eu.karenfort.main.api


import android.content.Context
import eu.karenfort.main.helper.StoreData
import kotlinx.coroutines.runBlocking
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
            e.printStackTrace()
        }
        return id
    }

    fun getSchoolStartForDay(id: Int, context: Context): LocalDateTime? {
        try {
            var nextDay = LocalDate.now().plusDays(1)
            var timetable: Timetable = session.getTimetableFromPersonId(
                nextDay,
                nextDay,
                id
            )

            var c = 0
            while (timetable.isEmpty()) {//would have to be repeating to get days after holidays
                if (c > 3) return null //todo add proper handling for when there are no more lessons

                nextDay = nextDay.plusDays(1)
                timetable = session.getTimetableFromPersonId(
                    nextDay,
                    nextDay.plusDays(3), //getting a timeframe of 3 days since this would always include the day after a weekend
                    id
                )
                c += 1
            }

            var storedCancelledMessage: String? = null

            runBlocking{ storedCancelledMessage = StoreData(context).loadCancelledMessage() }

            timetable.sortByDate()
            timetable.sortByStartTime()
            for (i in timetable) {
                if (!lessonIsCancelled(i, storedCancelledMessage)) {
                    val firstLessonStartTime: LocalDateTime = LocalDateTime.of(nextDay, i.startTime)
                    session.logout()
                    return firstLessonStartTime
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun lessonIsCancelled(lesson: Timetable.Lesson, storedCancelledMessage: String?): Boolean {
        var isCancelled = false

        if (lesson.teachers.isEmpty() && lesson.teachers != null) {
            isCancelled = true
        } //if there is no teacher assigned there is no school

        if (!storedCancelledMessage.isNullOrEmpty()) {
            if (lesson.substText == storedCancelledMessage) {
                isCancelled = true
            }
        }

        if (lesson.code == UntisUtils.LessonCode.CANCELLED) {
            isCancelled = true
        }
        return isCancelled
    }
}