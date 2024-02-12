/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This class checks if the Alarm Clock is set properly and adjusts accordingly.
 */
package eu.karenfort.untisAlarm.alarmClock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import eu.karenfort.untisAlarm.alarm.AlarmReceiver
import eu.karenfort.untisAlarm.alarm.AlarmScheduler
import eu.karenfort.untisAlarm.api.UntisApiCalls
import eu.karenfort.untisAlarm.dataPass.DataPass
import eu.karenfort.untisAlarm.extentions.hasNetworkConnection
import eu.karenfort.untisAlarm.extentions.isScreenOn
import eu.karenfort.untisAlarm.extentions.sendLoggedOutNotif
import eu.karenfort.untisAlarm.extentions.sendNoInternetNotif
import eu.karenfort.untisAlarm.helper.ALARM_REQUEST_CODE
import eu.karenfort.untisAlarm.helper.DEFAULT_TBS_MIN
import eu.karenfort.untisAlarm.helper.NEW_ALARM_TIME_MILLIS
import eu.karenfort.untisAlarm.helper.NEW_ALARM_TIME_MILLIS_WHEN_NO_ALARM_TODAY
import eu.karenfort.untisAlarm.helper.NEW_ALARM_TIME_MILLIS_WHEN_NO_SCHOOL_FOUND
import eu.karenfort.untisAlarm.helper.StoreData
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
            val result = main2(context, isActive, isEdited)
            DataPass.passAlarmActive(context, result)
            return result
        }

        private suspend fun main2(
            context: Context,
            isActive: Boolean?,
            isEdited: Boolean?
        ): LocalDateTime? {
            Log.i(TAG, "Called alarmClockSetter")

            if (!context.hasNetworkConnection()) {
                context.sendNoInternetNotif()
                setNew(REASON_NORMAL, context)
                Log.i(TAG, "cancelling because phone is offline")
                return null
            }

            //load data
            val storeData = StoreData(context)

            val id = storeData.loadID()
            val loginData = storeData.loadLoginData()
            val tbs: Int = storeData.loadTBS()
                ?: DEFAULT_TBS_MIN //just setting default value in case stored value is lost
            val alarmActive: Boolean = isActive ?: (storeData.loadAlarmActive() ?: false)
            var (storedAlarmClockDateTime, storedAlarmClockEdited) = storeData.loadAlarmClock()
            if (isEdited != null) {
                storedAlarmClockEdited = isEdited
            }

            if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null) {
                if (!context.isScreenOn) {
                    context.sendLoggedOutNotif()
                }
                AlarmScheduler(context).cancel()
                Log.i(TAG, "cancelled because user was logged out")
                return null
            }
            var schoolStart: LocalDateTime? = null

            try {
                schoolStart = UntisApiCalls(
                    loginData[0]!!,
                    loginData[1]!!,
                    loginData[2]!!,
                    loginData[3]!!
                ).getSchoolStartForDay(id, context)
            } catch (_: IOException) {
            }


            if (!alarmActive) {
                //no further action needed since alarm are disabled
                return schoolStart?.minusMinutes(tbs.toLong())
            }

            if (storedAlarmClockEdited) {
                setNew(REASON_NO_ALARM_TODAY, context)
                return storedAlarmClockDateTime
            }

            if (schoolStart == null) {
                setNew(REASON_NO_SCHOOL_START_FOUND, context)
                Log.i(TAG, "no school start was found")
                return null
            }

            //!! since it was just checked
            val newAlarmClockTime = schoolStart!!.minusMinutes(tbs.toLong())

            if (storedAlarmClockDateTime == null) {
                AlarmClock.set(newAlarmClockTime, context)
                setNew(REASON_NORMAL, context)
                Log.i(TAG, "cancelled because couldn't load stored AlarmClockDateTime")
                return newAlarmClockTime
            }

            if (isAlarmClockSetProperly(newAlarmClockTime, storedAlarmClockDateTime)) {
                setNew(REASON_NORMAL, context)
                Log.i(TAG, "alarm clock is set properly")
                return newAlarmClockTime
            }

            AlarmClock.set(newAlarmClockTime, context)

            setNew(REASON_NORMAL, context)
            storeData.storeAlarmClock(newAlarmClockTime)
            Log.i(TAG, "updating alarm clock")
            return newAlarmClockTime
        }

        private fun isAlarmClockSetProperly(
            alarmClockTime: LocalDateTime?,
            storedAlarmClockDateTime: LocalDateTime?
        ) =
            if (alarmClockTime == null || storedAlarmClockDateTime == null)
                false
            else alarmClockTime.isEqual(
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
                REASON_NORMAL -> {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC,
                        System.currentTimeMillis() + NEW_ALARM_TIME_MILLIS, //1 hour
                        pendingIntent
                    )
                }

                REASON_NO_ALARM_TODAY -> {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC,
                        System.currentTimeMillis() + NEW_ALARM_TIME_MILLIS_WHEN_NO_ALARM_TODAY, //5 hours
                        pendingIntent
                    )
                }

                REASON_NO_SCHOOL_START_FOUND -> {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC,
                        System.currentTimeMillis() + NEW_ALARM_TIME_MILLIS_WHEN_NO_SCHOOL_FOUND, //3 days
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