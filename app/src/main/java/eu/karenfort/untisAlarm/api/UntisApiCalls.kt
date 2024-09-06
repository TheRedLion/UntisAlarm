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
import java.time.LocalTime

/**
 * This class is responsible for sending API calls to the WebUntis API.
 *
 * @constructor Takes the users credentials and tries to login.
 * @throws IOException if the credentials are invalid.
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
        this.session = Session.login(
            username,
            password,
            server,
            schoolName
        )
    }

    /**
     * This function returns the ID of the person that is logged in.
     *
     * @return The users ID, null if there was an error.
     */
    fun getID(): Int {
        return session.infos.personId
    }

    /**
     * This function returns the start of the next school day. Only if there is school
     * within the next 7 days, otherwise it returns null.
     *
     * @throws IOException
     */
    suspend fun getSchoolStartForDay(id: Int, context: Context): LocalDateTime? {
        var localDateTime: LocalDateTime? = null

        runBlocking {
            val day = LocalDate.now() //todo: needs to account for updating at night but also not setting alarms multiple times a day
            val timetableAsync = async {
                session.getTimetableFromPersonId(
                    day,
                    day.plusDays(7),
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
                if (LocalDateTime.of(i.date, i.startTime).isAfter(LocalDateTime.now())) {
                    if (!lessonIsCancelled(i, cancellationMessage)) {
                        localDateTime = LocalDateTime.of(i.date, i.startTime)
                        break
                    }
                }
            }
            return@runBlocking null
        }
        return localDateTime
    }

    /**
     * This function checks weather a lesson is cancelled. It checks if there is no teacher assigned,
     * since in this case the lesson must be cancelled. It also checks if the lesson has a substText
     * that matches the stored cancelled message. It is not possible to just check for the lesson code
     * since lessons are often cancelled but not given the cancelled lesson code.
     */
    private fun lessonIsCancelled(
        lesson: Timetable.Lesson,
        storedCancelledMessage: String?
    ): Boolean {
        var isCancelled = false

        if (lesson.teachers.isEmpty() && lesson.teachers != null) {
            isCancelled = true
        } //if there is no teacher assigned there is no school

        if ((!storedCancelledMessage.isNullOrEmpty()) && !(lesson.substText.isNullOrEmpty())) {
            if (lesson.substText.contains(storedCancelledMessage)) {
                isCancelled = true
            }
        }

        if (lesson.code == UntisUtils.LessonCode.CANCELLED) {
            isCancelled = true
        }
        return isCancelled
    }

    fun getCancelledMessages(id: Int): ArrayList<String> {
        val timetable = session.getTimetableFromPersonId(
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            id
        )

        val resultList: ArrayList<String> = arrayListOf()

        for (lesson in timetable) {
            if (!lesson.substText.isNullOrEmpty()) {
                if (!resultList.contains(lesson.substText)) {
                    resultList.add(lesson.substText)
                }
            }
        }

        return resultList
    }
}