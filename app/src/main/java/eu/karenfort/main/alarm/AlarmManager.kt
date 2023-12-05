package eu.karenfort.main.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import android.util.Log
import eu.karenfort.main.StoreData
import eu.karenfort.main.alarmClock.AlarmClock
import eu.karenfort.main.api.UntisApiCalls
import eu.karenfort.main.helper.ALARM_REQUEST_CODE
import eu.karenfort.main.helper.ALLOW_NETWORK_ON_MAIN_THREAD
import eu.karenfort.main.helper.isOnline
import eu.karenfort.main.helper.showAlarmNotification
import eu.karenfort.main.notifications.WarningNotifications
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class AlarmManager {

    companion object {
        private val TAG = "AlarmManager"
        fun main(context: Context) {
            Log.i(TAG ,"called main")


            if (!context.isOnline()) {
                Log.i(TAG, "Phone has no internet connectivity")
                WarningNotifications.sendNoInternetNotif(context)
                return
            }

            val storeData = StoreData(context)
            var id: Int?
            var loginData: Array<String?>
            var tbs: Int?
            val storedAlarmClockDateTime: LocalDateTime?
            val storedAlarmClockEdited: Boolean

            runBlocking {
                id = storeData.loadID()
                loginData = storeData.loadLoginData()
                tbs = storeData.loadTBS()
                val pair: Pair<LocalDateTime?, Boolean> = storeData.loadAlarmClock()
                storedAlarmClockDateTime = pair.first
                storedAlarmClockEdited = pair.second
                //debug: alarmClockArray = arrayOf(6, 43)
            }

            if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null) {
                WarningNotifications.sendLoggedOutNotif()
                setNew("error", null, context)
                return
            }

            if (tbs == null) {
                //should never happen
                tbs = 60 //just settings default value, should fix itself next time user opens app
            }

            if (storedAlarmClockEdited) { //todo make sure that edited is turned back off
                setNew("noAlarmToday", null, context)
                return
            }

            StrictMode.setThreadPolicy(ALLOW_NETWORK_ON_MAIN_THREAD) //todo maybe use ensureBackgroundThread? does that work
            val untisApiCalls = UntisApiCalls(
                loginData[0]!!,
                loginData[1]!!,
                loginData[2]!!,
                loginData[3]!!
            )

            val schoolStart = untisApiCalls.getSchoolStartForDay(id!!)

            if (schoolStart == null) {
                //probably holiday or something
                setNew("noAlarmToday", null, context)
                return
            }

            val alarmClockDateTime = schoolStart.minusMinutes(tbs!!.toLong())

            if (isAlarmClockSetProperly(alarmClockDateTime, storedAlarmClockDateTime)) {
                //Log.i(TAG, "Alarm clock set properly")
                setNew("normal", alarmClockDateTime, context)
                return
            }

            if (storedAlarmClockDateTime == null) {
                //Log.i(TAG, "No alarm clock set, setting a new one")

                AlarmClock.setAlarm(alarmClockDateTime, context)
                setNew("normal", schoolStart, context)
                return
            }

            AlarmClock.cancelAlarm(context)
            AlarmClock.setAlarm(alarmClockDateTime, context)

            setNew("normal", schoolStart, context)
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
                    if (schoolStart != null) {
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
                        //school start was null so do error stuff
                        setNew("error", null, context)
                    }
                }

                "error" -> {
                    Log.i(TAG, "uhhhh") //todo: error handling
                }
            }
        }
    }
}