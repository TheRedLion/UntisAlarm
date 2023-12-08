package eu.karenfort.main.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.StrictMode
import android.util.Log
import eu.karenfort.main.StoreData
import eu.karenfort.main.activities.MainActivity
import eu.karenfort.main.alarmClock.AlarmClock
import eu.karenfort.main.api.UntisApiCalls
import eu.karenfort.main.helper.ALARM_REQUEST_CODE
import eu.karenfort.main.helper.ALLOW_NETWORK_ON_MAIN_THREAD
import eu.karenfort.main.helper.isOnline
import eu.karenfort.main.helper.sendLoggedOutNotif
import eu.karenfort.main.notifications.WarningNotifications
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class AlarmManager {

    companion object {
        private val TAG = "AlarmManager"

        fun main(context: Context): LocalDateTime? {
            return main(context, null, null)
        }

        fun main(context: Context, isActive: Boolean?): LocalDateTime? {
            return main(context, isActive, null)
        }

        fun main(context: Context, isActive: Boolean?, edited: Boolean?): LocalDateTime? {
            Log.i(TAG ,"called main")


            if (!context.isOnline()) {
                Log.i(TAG, "Phone has no internet connectivity")
                WarningNotifications.sendNoInternetNotif(context)
                return null
            }

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
                //debug: alarmClockArray = arrayOf(6, 43)
            }

            if (edited != null) {
                storedAlarmClockEdited = edited
            }


            if (tbs == null) {
                //should never happen
                tbs = 60 //just settings default value, should fix itself next time user opens app
            }

            if (isActive != null) {
                alarmActive  = isActive
            }

            if (!alarmActive) {
                AlarmClock.cancelAlarm(context)
                Log.i(TAG, "alarm is not active (${MainActivity.active})")

                //the following will load and display the time an alarm would have
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (context.isUiContext) {
                        Log.i(TAG, "loading preview for alarmPreview in Main Activity")
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

                        return schoolStart!!.minusMinutes(tbs!!.toLong())

                    }
                } else {
                    if (MainActivity.active) {
                        Log.i(TAG, "loading preview for alarmPreview in Main Activity")
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

                        return schoolStart!!.minusMinutes(tbs!!.toLong())
                    }
                }
                return null
            }

            if (storedAlarmClockEdited) {
                Log.i(TAG, "Alarm was edited or snoozed")
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
                Log.i(TAG, "schoolStart is null")

                setNew("noAlarmToday", null, context)
                return null
            }

            val alarmClockDateTime = schoolStart.minusMinutes(tbs!!.toLong())

            if (storedAlarmClockDateTime == null) {
                Log.i(TAG, "No alarm clock set, setting a new one")

                AlarmClock.setAlarm(alarmClockDateTime, context)
                setNew("normal", schoolStart, context)
                return alarmClockDateTime
            }

            if (isAlarmClockSetProperly(alarmClockDateTime, storedAlarmClockDateTime)) {
                Log.i(TAG, "Alarm clock set properly")
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

                    Log.i(TAG, "setting new Alarm")
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
                            Log.i(TAG, "setting new Alarm for in 15 minutes to an hour.")
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC,
                                System.currentTimeMillis() + 900000,
                                pendingIntent
                            )
                        } else {
                            Log.i(TAG, "set new Alarm for in 15 minutes")
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC,
                                System.currentTimeMillis() + 900000,
                                pendingIntent
                            )
                        }
                    } else {
                        Log.i(TAG, "set new Alarm for in 15 minutes")
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC,
                            System.currentTimeMillis() + 900000,
                            pendingIntent
                        )
                    }
                }

                "error" -> {
                    Log.e(TAG, "error")
                    setNew("normal", null, context)
                }

                else -> {
                    Log.i(TAG, "no matich reason")
                    setNew("normal", null, context)
                }
            }
        }
    }
}