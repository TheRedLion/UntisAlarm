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
import android.util.Log
import eu.karenfort.main.alarm.AlarmReceiver
import eu.karenfort.main.alarm.AlarmScheduler
import eu.karenfort.main.api.UntisApiCalls
import eu.karenfort.main.extentions.hasNetworkConnection
import eu.karenfort.main.extentions.sendLoggedOutNotif
import eu.karenfort.main.extentions.sendNoInternetNotif
import eu.karenfort.main.helper.ALARM_REQUEST_CODE
import eu.karenfort.main.helper.ALLOW_NETWORK_ON_MAIN_THREAD
import eu.karenfort.main.helper.StoreData
import eu.karenfort.main.helper.TBS_DEFAULT
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.time.LocalDateTime

class AlarmClockSetter {
    companion object {
        private const val TAG = "AlarmClockSetter"
        private const val REASON_NORMAL = "normal"
        private const val REASON_NO_ALARM_TODAY = "no_alarm_today"
        private const val REASON_NO_SCHOOL_START_FOUND = "no_school_start"

        /* isActive and isEdited is used to override stored data, necessary because storing is
            done on a different coroutine and thus takes time. Used when a new state is set and the
            AlarmClock needs to be adjusted right after that for example to update UI
         */

        suspend fun main(context: Context): LocalDateTime? {
            return main(context, null, null)
        }

        suspend fun main(context: Context, isActive: Boolean?): LocalDateTime? {
            return main(context, isActive, null)
        }

        suspend fun main(context: Context, isActive: Boolean?, isEdited: Boolean?): LocalDateTime? {
            Log.i(TAG, "Called alarmClockSetter")
            //rest unnecessary without being able to make API calls
            if (!context.hasNetworkConnection()) {
                context.sendNoInternetNotif()
                setNew(REASON_NORMAL, context)
                Log.i(TAG, "cancelling because phone is offline")
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
                alarmActive = storeData.loadAlarmActive() ?: false
            }

            if (isEdited != null) {
                storedAlarmClockEdited = isEdited
            }
            if (isActive != null) {
                alarmActive = isActive
            }

            if (tbs == null) {
                tbs = TBS_DEFAULT //just setting default value in case stored value is lost
            }

            if (!alarmActive) {
                AlarmClock.cancel(context)

                //the following will load and display the time an alarm would have, since alarms are disabled
                if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null) {
                    context.sendLoggedOutNotif()
                    AlarmScheduler(context).cancel()
                    Log.i(TAG, "cancelling because user is logged out")
                    return null //not setting a new one, since it wont do anything
                }
                var schoolStart: LocalDateTime? = null

                StrictMode.setThreadPolicy(ALLOW_NETWORK_ON_MAIN_THREAD)
                val untisApiCalls = UntisApiCalls(
                    loginData[0]!!,
                    loginData[1]!!,
                    loginData[2]!!,
                    loginData[3]!!
                )

                try {
                    schoolStart = untisApiCalls.getSchoolStartForDay(id!!, context)
                } catch (_: IOException) {}

                if (schoolStart == null) {
                    Log.i(TAG, "no schoolStart found")
                    return null
                }

                return schoolStart.minusMinutes(tbs!!.toLong())
            }

            if (storedAlarmClockEdited) {
                setNew(REASON_NO_ALARM_TODAY, context)
                return storedAlarmClockDateTime
            }

            if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null) {
                context.sendLoggedOutNotif()
                AlarmScheduler(context).cancel()
                Log.i(TAG, "cancelled because user was logged out")
                return null
            }

            var schoolStart: LocalDateTime? = null
            StrictMode.setThreadPolicy(ALLOW_NETWORK_ON_MAIN_THREAD)
            val untisApiCalls = UntisApiCalls(
                loginData[0]!!,
                loginData[1]!!,
                loginData[2]!!,
                loginData[3]!!
            )

            try {
                schoolStart = untisApiCalls.getSchoolStartForDay(id!!, context)
            } catch (_: IOException) {}

            if (schoolStart == null) {
                setNew(REASON_NO_SCHOOL_START_FOUND, context)
                Log.i(TAG, "no school start was found")
                return null
            }

            val alarmClockDateTime = schoolStart.minusMinutes(tbs!!.toLong())

            if (storedAlarmClockDateTime == null) {
                AlarmClock.set(alarmClockDateTime, context)
                setNew(REASON_NORMAL, context)
                Log.i(TAG, "cancelled because couldn't load stored AlarmClockDateTime")
                return alarmClockDateTime
            }

            if (isAlarmClockSetProperly(alarmClockDateTime, storedAlarmClockDateTime)) {
                setNew(REASON_NORMAL, context)
                Log.i(TAG, "alarm clock is set properly")
                return alarmClockDateTime
            }

            AlarmClock.cancel(context)
            AlarmClock.set(alarmClockDateTime, context)

            setNew(REASON_NORMAL, context)
            Log.i(TAG, "updating alarm clock")
            return alarmClockDateTime
        }

        private fun isAlarmClockSetProperly(
            alarmClockTime: LocalDateTime?,
            storedAlarmClockDateTime: LocalDateTime?
        ) =
            if (alarmClockTime == null || storedAlarmClockDateTime == null) false else alarmClockTime.isEqual(
                storedAlarmClockDateTime
            )

        private fun setNew(reason: String, context: Context) {
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

            when (reason) {
                REASON_NO_ALARM_TODAY -> {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC,
                        System.currentTimeMillis() + 18000000, //5 hours
                        pendingIntent
                    )
                }

                REASON_NORMAL -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC,
                        System.currentTimeMillis() + 900000, //15 min
                        pendingIntent
                    )
                }

                REASON_NO_SCHOOL_START_FOUND -> {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC,
                        System.currentTimeMillis() + 259200000, //3 days
                        pendingIntent
                    )
                }

                else -> {
                    throw Error("No matching Reason.")
                }
            }
        }
    }
}