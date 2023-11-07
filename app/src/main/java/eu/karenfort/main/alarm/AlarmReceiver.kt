package eu.karenfort.main.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import android.util.Log
import android.widget.TextView
import eu.karenfort.main.StoreData
import eu.karenfort.main.alarmClock.AlarmClock
import eu.karenfort.main.api.UntisApiCalls
import eu.karenfort.main.helper.ALARM_REQUEST_CODE
import eu.karenfort.main.helper.ALLOW_NETWORK_ON_MAIN_THREAD
import eu.karenfort.main.helper.getNextDay
import eu.karenfort.main.helper.isOnline
import kotlinx.coroutines.runBlocking
import java.time.LocalTime


class AlarmReceiver: BroadcastReceiver() {
    private val TAG = "AlarmReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG ,"called onReceive() with intent:$intent")

        /*
        if (!intent.equals("Intent { flg=0x10 cmp=com.carlkarenfort.test/eu.karenfort.main.alarm.AlarmReceiver }") && !intent.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i(TAG, "intent $intent is not allowed")
            return
        }*/

        if (!context.isOnline()) {
            Log.i(TAG, "Phone has no internet connectivity")
            //todo: maybe send notif that alarm might not be set properly?
            return
        }

        val storeData = StoreData(context)
        var id: Int?
        var loginData: Array<String?>
        var tbs: Int?
        val alarmClockArray: Array<Int?>

        runBlocking {
            //todo: maybe need to move alarm receiver from main thread
            id = storeData.loadID()
            loginData = storeData.loadLoginData()
            tbs = storeData.loadTBS()
            alarmClockArray = storeData.loadAlarmClock()
            //debug: alarmClockArray = arrayOf(6, 43)
        }
        val alarmClockHour = alarmClockArray[0]
        val alarmClockMinute = alarmClockArray[1]

        if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null) {
            //todo: warn user that he got logged out
            setNew("error", null, context)
            return
        }

        if (tbs == null) {
            //should never happen
            tbs = 60 //just settings default value, should fix itself next time user opens app
        }

        StrictMode.setThreadPolicy(ALLOW_NETWORK_ON_MAIN_THREAD)
        val untisApiCalls = UntisApiCalls(
            loginData[0]!!,
            loginData[1]!!,
            loginData[2]!!,
            loginData[3]!!
        )

        val schoolStart = untisApiCalls.getSchoolStartForDay(id!!)

        Log.i(TAG, "Getting school start for day: ${getNextDay()}. Is $schoolStart")
        //debug: var schoolStart = LocalTime.of(7, 0)

        if (schoolStart == null) {
            //probably holiday or something
            setNew("noAlarmToday", null, context)
            return
        }

        val alarmClockTime = schoolStart.minusMinutes(tbs!!.toLong())

        //todo: test for edited but only if day is right
        if (isAlarmClockSetProperly(alarmClockTime, alarmClockHour, alarmClockMinute)) {
            //Log.i(TAG, "Alarm clock set properly")
            setNew("normal", alarmClockTime, context)
            return
        }

        if (isAnAlarmClockSet(alarmClockHour, alarmClockMinute)) {
            //Log.i(TAG, "No alarm clock set, setting a new one")

            AlarmClock.setAlarm(alarmClockTime, context)
            //context.showAlarmNotification() //todo: decide if we want that (maybe as setting)
            setNew("normal", schoolStart, context)
            return
        }

        AlarmClock.cancelAlarm(context)
        AlarmClock.setAlarm(alarmClockTime, context)
        //context.showAlarmNotification()

        setNew("normal", schoolStart, context)
    }

    private fun isAnAlarmClockSet(alarmClockHour: Int?, alarmClockMinute: Int?) =
        alarmClockHour == null || alarmClockMinute == null

    private fun isAlarmClockSetProperly(
        alarmClockTime: LocalTime,
        alarmClockHour: Int?,
        alarmClockMinute: Int?
    ) = alarmClockTime.hour == alarmClockHour && alarmClockTime.minute == alarmClockMinute

    private fun setNew(reason: String, schoolStart: LocalTime?, context: Context) {
        when (reason) {
            "noAlarmToday" -> {
                val alarmManager = context.getSystemService(AlarmManager::class.java)
                val intent = Intent(context, AlarmReceiver::class.java)
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
                    val intent = Intent(context, AlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        ALARM_REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    if (LocalTime.now().isBefore(schoolStart.minusHours(2))) {
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
                Log.i(TAG, "uhhhh") //todo: error
            }
        }
    }
}