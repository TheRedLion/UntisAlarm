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
import android.os.Build
import android.os.StrictMode
import android.util.Log
import eu.karenfort.main.StoreData
import eu.karenfort.main.activities.MainActivity
import eu.karenfort.main.alarm.AlarmReceiver
import eu.karenfort.main.api.UntisApiCalls
import eu.karenfort.main.helper.ALARM_REQUEST_CODE
import eu.karenfort.main.helper.ALLOW_NETWORK_ON_MAIN_THREAD
import eu.karenfort.main.helper.isOnline
import eu.karenfort.main.helper.sendLoggedOutNotif
import eu.karenfort.main.notifications.WarningNotifications
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class AlarmClockSetter {

    companion object {
        private val TAG = "AlarmClockSetter"

        /* isActive and isEdited is used to override stored data, necessary because storing is
            done on a different thread and thus takes time. Used when a new state is set and the
            AlarmClock needs to be adjusted right after that for example to update UI
         */

        fun main(context: Context): LocalDateTime? {
            return main(context, null, null)
        }

        fun main(context: Context, isActive: Boolean?): LocalDateTime? {
            return main(context, isActive, null)
        }

        fun main(context: Context, isActive: Boolean?, isEdited: Boolean?): LocalDateTime? {

            //rest unnecessary without being able to make API calls
            if (!context.isOnline()) {
                WarningNotifications.sendNoInternetNotif(context)
                setNew("normal", null, context)
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
                //should never happen
                tbs = 60 //just settings default value, should fix itself next time user opens app
            }

            if (!alarmActive) {
                AlarmClock.cancelAlarm(context)

                //the following will load and display the time an alarm would have
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { //isUiContext requires Android API 31
                    if (!context.isUiContext) return null
                }

                if (MainActivity.active) {
                    if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null) {
                        context.sendLoggedOutNotif()
                        setNew("error", null, context)
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

                    if (schoolStart == null) return null

                    return schoolStart.minusMinutes(tbs!!.toLong())
                }
                return null
            }

            if (storedAlarmClockEdited) {
                setNew("noAlarmToday", null, context)
                return null
            }

            if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null) {
                context.sendLoggedOutNotif()
                Log.i(TAG, "not logged in")
                setNew("error", null, context)
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
                setNew("noAlarmToday", null, context)
                return null
            }

            val alarmClockDateTime = schoolStart.minusMinutes(tbs!!.toLong())

            if (storedAlarmClockDateTime == null) {
                AlarmClock.setAlarm(alarmClockDateTime, context)
                setNew("normal", schoolStart, context)
                return alarmClockDateTime
            }

            if (isAlarmClockSetProperly(alarmClockDateTime, storedAlarmClockDateTime)) {
                setNew("normal", alarmClockDateTime, context)
                return alarmClockDateTime
            }

            AlarmClock.cancelAlarm(context)
            AlarmClock.setAlarm(alarmClockDateTime, context)

            setNew("normal", schoolStart, context)
            return alarmClockDateTime
        }

        private fun isAlarmClockSetProperly(
            alarmClockTime: LocalDateTime?,
            storedAlarmClockDateTime: LocalDateTime?
        ) = if (alarmClockTime == null || storedAlarmClockDateTime == null) false else alarmClockTime.isEqual(storedAlarmClockDateTime)

        private fun setNew(reason: String, schoolStart: LocalDateTime?, context: Context) {
            when (reason) {
                "noAlarmToday" -> {
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

                "normal" -> {
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

                "error" -> {
                    Log.i(TAG, "error") //todo add implementation
                    setNew("normal", null, context)
                }

                else -> {
                    //there is an error in the code
                    Log.i(TAG, "no matching reason")
                    setNew("normal", null, context)
                }
            }
        }
    }
}