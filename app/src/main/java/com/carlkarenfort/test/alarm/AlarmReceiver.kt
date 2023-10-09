package com.carlkarenfort.test.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import android.util.Log
import com.carlkarenfort.test.AlarmClock
import com.carlkarenfort.test.ApiCalls
import com.carlkarenfort.test.MainActivity
import com.carlkarenfort.test.StoreData
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmReceiver: BroadcastReceiver() {
    private val TAG = "AlarmReceiver"
    private var policy: StrictMode.ThreadPolicy =  StrictMode.ThreadPolicy.Builder().permitAll().build()
    private val ALARM_REQUEST_CODE = 73295871

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG ,"called onReceive()")

        //check if we have a context
        if (context == null) {
            Log.i(TAG, "context from onReceieve is null")
            //TODO("not sure but might happen when foreground Activity has been stopped")
        } else {
            val apiCalls = ApiCalls()

            //check if phone has connectivity
            if (!apiCalls.isOnline(context)) {
                Log.i(TAG, "Phone has no internet connectivity")
            } else {
                //phone has connectivity:
                //load relevant data from storeData
                val storeData = StoreData(context)
                var id: Int?
                var loginData: Array<String?>
                var tbs: Int?
                val alarmClockArray: Array<Int?>
                runBlocking {
                    id = storeData.loadID()
                    loginData = storeData.loadLoginData()
                    tbs = storeData.loadTBS()
                    alarmClockArray = storeData.loadAlarmClock()
                    //debug: alarmClockArray = arrayOf(6, 43)
                }
                val alarmClockHour = alarmClockArray[0]
                val alarmClockMinute = alarmClockArray[1]


                if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null || tbs == null) {
                    Log.i(TAG, "id, loginData or TBS from StoreData was null. THIS SHOULD NEVER HAPPEN")
                } else {
                    //get schoolStart
                    StrictMode.setThreadPolicy(policy)
                    var schoolStart = apiCalls.getSchoolStartForDay(
                        loginData[0]!!,
                        loginData[1]!!,
                        loginData[2]!!,
                        loginData[3]!!,
                        id!!,
                        apiCalls.getNextDay()
                    )
                    Log.i(TAG, "Getting Schoolstart for day: ${apiCalls.getNextDay().toString()}. Is ${schoolStart.toString()}")
                    //debug: var schoolStart = LocalTime.of(7, 0)

                    if (schoolStart == null) {
                        //probably holliday or something
                        setNew("noAlarmToday", null, context)


                        //Log.i(TAG, "ApiCalls.getSchoolStartForDay returned null, ether there is no internet connectivity (maybe connected to a wifi network that is not online) or logindata was invalid")
                    } else {
                        schoolStart = schoolStart.minusMinutes(tbs!!.toLong())

                        if (schoolStart.hour == alarmClockHour && schoolStart.minute == alarmClockMinute) {
                            //alarm clock already set properly
                            setNew("normal", schoolStart, context)
                        } else if (alarmClockHour == null || alarmClockMinute == null) {
                            //no alarm clock set, setting a new one
                            val clock = AlarmClock()
                            clock.setAlarm(schoolStart.hour, schoolStart.minute, context)
                            setNew("normal", schoolStart, context)
                        } else {
                            //alarm set improperly
                            val clock = AlarmClock()
                            clock.cancelAlarm(context)
                            clock.setAlarm(schoolStart.hour, schoolStart.minute, context)
                            setNew("normal", schoolStart, context)
                        }
                    }
                }
            }
        }
    }

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
                    if (LocalTime.now().isBefore(schoolStart.minusHours(3))) {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC,
                            System.currentTimeMillis() + 900000,
                            pendingIntent
                        )
                        Log.i(TAG, "set new Alarm for in 15 minutes")
                    } else {
                        val schoolStartDate =
                            LocalDateTime.of(LocalDate.now(), schoolStart.minusHours(3))
                        var alarmClockDay = LocalDate.now()
                        if (schoolStart.isAfter(LocalTime.now())) {
                            alarmClockDay = alarmClockDay.plusDays(1)
                        }
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC,
                            LocalDateTime.of(LocalDate.now(), schoolStart.minusMinutes(15))
                                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                            pendingIntent
                        )
                    }
                } else {
                    //TODO("school start for normal returned normal, should not happen")
                }
            }

            "error" -> {

            }
        }
    }
}