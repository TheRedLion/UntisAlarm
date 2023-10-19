package com.carlkarenfort.test.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.StrictMode
import android.util.Log
import com.carlkarenfort.test.AlarmClock
import com.carlkarenfort.test.Misc
import com.carlkarenfort.test.StoreData
import com.carlkarenfort.test.UntisApiCalls
import kotlinx.coroutines.runBlocking
import java.time.LocalTime


class AlarmReceiver: BroadcastReceiver() {
    private val TAG = "AlarmReceiver"
    private var policy: StrictMode.ThreadPolicy =  StrictMode.ThreadPolicy.Builder().permitAll().build()
    private val alarmRequestCode = 73295871

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG ,"called onReceive()")
        //check if we have a context
        if (context == null) {
            Log.i(TAG, "context from onReceive is null")
            return
        } else {
            Log.i(TAG, "has context")

            val misc = Misc()

            //check if phone has connectivity
            if (!misc.isOnline(context)) {
                Log.i(TAG, "Phone has no internet connectivity")
            } else {
                //phone has connectivity:
                Log.i(TAG, "phone has internet")

                //load relevant data from storeData
                val storeData = StoreData(context)
                var id: Int?
                var loginData: Array<String?>
                var tbs: Int?
                val alarmClockArray: Array<Int?>
                //TODO("move alarm receiver from main thread")
                runBlocking {
                    id = storeData.loadID()
                    loginData = storeData.loadLoginData()
                    tbs = storeData.loadTBS()
                    alarmClockArray = storeData.loadAlarmClock()
                    //debug: alarmClockArray = arrayOf(6, 43)
                }
                val alarmClockHour = alarmClockArray[0]
                val alarmClockMinute = alarmClockArray[1]

                //check if any of the loaded data is null
                if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null || tbs == null) {
                    Log.i(TAG, "id, loginData or TBS from StoreData was null. THIS SHOULD NEVER HAPPEN")
                    //TODO add notification if user was logged out
                    //warn user that he got logged out
                    /*if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        //create notification
                        val notification = NotificationCompat.Builder(context, "main_channel")
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("Login again")
                            .setContentText("Your Login Data is invalid, please login Again.")
                            .build()

                    }*/

                } else {
                    //none of the loaded data is null

                    //get schoolStart
                    StrictMode.setThreadPolicy(policy)
                    val untisApiCalls = UntisApiCalls(
                        loginData[0]!!,
                        loginData[1]!!,
                        loginData[2]!!,
                        loginData[3]!!
                    )
                    var schoolStart = untisApiCalls.getSchoolStartForDay(
                        id!!,
                    )

                    Log.i(TAG, "Getting Schoolstart for day: ${misc.getNextDay()}. Is ${schoolStart.toString()}")
                    //debug: var schoolStart = LocalTime.of(7, 0)

                    if (schoolStart == null) {
                        //probably holiday or something
                        setNew("noAlarmToday", null, context)
                    } else {
                        schoolStart = schoolStart.minusMinutes(tbs!!.toLong())

                        if (schoolStart.hour == alarmClockHour && schoolStart.minute == alarmClockMinute) {
                            //alarm clock already set properly
                            Log.i(TAG, "alarm clock set properly")
                            setNew("normal", schoolStart, context)
                        } else if (alarmClockHour == null || alarmClockMinute == null) {
                            //no alarm clock set, setting a new one
                            Log.i(TAG, "no alarm clock set, setting a new one")

                            val alarmClock = AlarmClock()
                            alarmClock.setAlarm(schoolStart, context)

                            setNew("normal", schoolStart, context)
                        } else {
                            //alarm set improperly
                            Log.i(TAG, "removing old and setting new alarm")

                            val alarmClock = AlarmClock()
                            alarmClock.cancelAlarm(context)
                            alarmClock.setAlarm(schoolStart, context)

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
                    alarmRequestCode,
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
                        alarmRequestCode,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    if (LocalTime.now().isBefore(schoolStart.minusHours(2))) {
                        Log.i(TAG, "setting new Alarm for in 15 minutes to an hour.")
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC,
                            System.currentTimeMillis() + 90000,
                            pendingIntent
                        )
                    } else {
                        Log.i(TAG, "set new Alarm for in 15 minutes")
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC,
                            System.currentTimeMillis() + 90000,
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