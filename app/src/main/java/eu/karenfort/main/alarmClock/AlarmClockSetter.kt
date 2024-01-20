/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This class checks if the Alarm Clock is set properly and adjusts accordingly.
 */
package eu.karenfort.main.alarmClock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import eu.karenfort.main.ui.MainActivity
import eu.karenfort.main.alarm.AlarmReceiver
import eu.karenfort.main.api.UntisApiCalls
import eu.karenfort.main.extentions.isOnline
import eu.karenfort.main.extentions.sendLoggedOutNotif
import eu.karenfort.main.extentions.sendNoInternetNotif
import eu.karenfort.main.helper.ALARM_REQUEST_CODE
import eu.karenfort.main.helper.ALLOW_NETWORK_ON_MAIN_THREAD
import eu.karenfort.main.helper.StoreData
import eu.karenfort.main.helper.TBS_DEFAULT
import eu.karenfort.main.helper.isSnowConePlus
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class AlarmClockSetter {
    companion object {
        private const val TAG = "AlarmClockSetter"
        private const val REASON_NORMAL = "normal"
        private const val REASON_NO_ALARM_TODAY = "noAlarmToday"

        /* isActive and isEdited is used to override stored data, necessary because storing is
            done on a different thread and thus takes time. Used when a new state is set and the
            AlarmClock needs to be adjusted right after that for example to update UI
         */

        suspend fun main(context: Context): LocalDateTime? {
            return main(context, null, null)
        }

        suspend fun main(context: Context, isActive: Boolean?): LocalDateTime? {
            return main(context, isActive, null)
        }

        suspend fun main(context: Context, isActive: Boolean?, isEdited: Boolean?): LocalDateTime? {

            //rest unnecessary without being able to make API calls
            if (!context.isOnline()) {
                context.sendNoInternetNotif()
                setNew(REASON_NORMAL, null, context)
                return null
            }

            //load data
            val storeData = StoreData(context)
            var id: Int?
            var loginData: Array<String?>
            var tbs: Int?
            val storedAlarmClockDateTime: LocalDateTime?
            var storedAlarmClockEdited: Boolean
            var alarmActive: Boolean
            runBlocking {
                id = storeData.loadID()
                loginData = storeData.loadLoginData()
                tbs = storeData.loadTBS()
                val pair: Pair<LocalDateTime?, Boolean> = storeData.loadAlarmClock()
                storedAlarmClockDateTime = pair.first
                storedAlarmClockEdited = pair.second
                alarmActive = storeData.loadAlarmActive()?: false
            }

            if (isEdited != null) {
                storedAlarmClockEdited = isEdited
            }
            if (isActive != null) {
                alarmActive  = isActive
            }

            if (tbs == null) {
                tbs = TBS_DEFAULT //just setting default value in case stored value is lost
            }

            if (!alarmActive) {
                AlarmClock.cancel(context)

                //the following will load and display the time an alarm would have
                if (isSnowConePlus()) { //isUiContext requires Android API 31
                    if (!context.isUiContext) return null
                }

                if (MainActivity.active) {
                    if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null) {
                        context.sendLoggedOutNotif()
                        return null //not setting a new one, since it wont do anything
                    }
                    val schoolStart: LocalDateTime?

                    StrictMode.setThreadPolicy(ALLOW_NETWORK_ON_MAIN_THREAD)
                    val untisApiCalls = UntisApiCalls(
                        loginData[0]!!,
                        loginData[1]!!,
                        loginData[2]!!,
                        loginData[3]!!
                    )

                    schoolStart = untisApiCalls.getSchoolStartForDay(id!!)

                    if (schoolStart == null) return null

                    return schoolStart.minusMinutes(tbs!!.toLong())
                }
                return null
            }

            if (storedAlarmClockEdited) {
                setNew(REASON_NO_ALARM_TODAY, context)
                return null
            }

            if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null) {
                context.sendLoggedOutNotif()
                //not setting a new one, since it wont do anything
                return null
            }

            val schoolStart: LocalDateTime?
            StrictMode.setThreadPolicy(ALLOW_NETWORK_ON_MAIN_THREAD)
            val untisApiCalls = UntisApiCalls(
                loginData[0]!!,
                loginData[1]!!,
                loginData[2]!!,
                loginData[3]!!
            )

            schoolStart = untisApiCalls.getSchoolStartForDay(id!!)

            if (schoolStart == null) {
                //probably holiday or something
                setNew(REASON_NO_ALARM_TODAY, context)
                return null
            }

            val alarmClockDateTime = schoolStart.minusMinutes(tbs!!.toLong())

            if (storedAlarmClockDateTime == null) {
                AlarmClock.set(alarmClockDateTime, context)
                setNew(REASON_NORMAL, schoolStart, context)
                return alarmClockDateTime
            }

            if (isAlarmClockSetProperly(alarmClockDateTime, storedAlarmClockDateTime)) {
                setNew(REASON_NORMAL, alarmClockDateTime, context)
                return alarmClockDateTime
            }

            AlarmClock.cancel(context)
            AlarmClock.set(alarmClockDateTime, context)

            setNew(REASON_NORMAL, schoolStart, context)
            return alarmClockDateTime
        }

        private fun isAlarmClockSetProperly(
            alarmClockTime: LocalDateTime?,
            storedAlarmClockDateTime: LocalDateTime?
        ) = if (alarmClockTime == null || storedAlarmClockDateTime == null) false else alarmClockTime.isEqual(storedAlarmClockDateTime)

        private fun setNew(reason: String, context: Context) {
            setNew(reason, null, context)
        }
        private fun setNew(reason: String, schoolStart: LocalDateTime?, context: Context) {
            when (reason) {
                REASON_NO_ALARM_TODAY -> {
                    val alarmManager = context.getSystemService(AlarmManager::class.java)
                    val intent = Intent(context, AlarmReceiver::class.java).also {
                        it.action = Intent.ACTION_CALL
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        ALARM_REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )

                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC,
                        System.currentTimeMillis() + 10800000,
                        pendingIntent
                    )
                }

                REASON_NORMAL -> {
                    val alarmManager = context.getSystemService(AlarmManager::class.java)
                    val intent = Intent(context, AlarmReceiver::class.java).also {
                        it.action = Intent.ACTION_CALL
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        ALARM_REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    if (schoolStart != null) {
                        if (LocalDateTime.now().isBefore(schoolStart.minusHours(2))) {
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC,
                                System.currentTimeMillis() + 900000,
                                pendingIntent
                            )
                        } else {
                            /* Since when using setAndAllowWileIdle the alarm is called during
                                a one hour time frame after the specified time. To make sure the
                                Alarm CLock time is checked shortly before the Alarm Clock goes of.
                                That is why setExact is used.
                             */
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC,
                                System.currentTimeMillis() + 900000,
                                pendingIntent
                            )
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC,
                            System.currentTimeMillis() + 900000,
                            pendingIntent
                        )
                    }
                }

                else -> {
                    throw Error("No matching Reason.")
                }
            }
        }
    }
}