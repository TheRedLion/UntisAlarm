/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This class is responsible for sending API calls to the WebUntis API.
 *      We are using the Untis4J library https://github.com/untisapi/org.bytedream.untis4j.
 */
package eu.karenfort.untisAlarm.api


import android.content.Context
import eu.karenfort.untisAlarm.helper.StoreData
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.bytedream.untis4j.Session
import org.bytedream.untis4j.UntisUtils
import org.bytedream.untis4j.responseObjects.Timetable
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * This class is responsible for sending API calls to the WebUntis API.
 */
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

    /**
     * This function returns the ID of the person that is logged in.
     */
    fun getID(): Int? {
        var id: Int? = null
        try {
            id = session.infos.personId
        } catch (_: IOException) {}
        return id
    }

    /**
     * This function returns the start of the next school day. Only if there is school
     * within the next 7 days, otherwise it returns null.
     */
    @Throws(IOException::class)
    fun getSchoolStartForDay(id: Int, context: Context): LocalDateTime? {
        //todo check if async is working
        var localDateTime: LocalDateTime? = null
        runBlocking {
            val nextDay = LocalDate.now().plusDays(1)

            val timetableAsync = async {
                session.getTimetableFromPersonId(
                    nextDay,
                    nextDay.plusDays(6),
                    id
                )
            }
            val cancellationMessageAsync = async { StoreData(context).loadCancelledMessage() }

            val timetable = timetableAsync.await()
            val cancellationMessage = cancellationMessageAsync.await()

            if (timetable.isEmpty()) return@runBlocking null

            timetable.sortByDate()
            timetable.sortByStartTime()
            for (i in timetable) {
                if (!lessonIsCancelled(i, cancellationMessage)) {
                    localDateTime = LocalDateTime.of(i.date, i.startTime)
                }
            }
            return@runBlocking null
        }
        return localDateTime
    }

    private fun lessonIsCancelled(
        lesson: Timetable.Lesson,
        storedCancelledMessage: String?
    ): Boolean {
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