package com.carlkarenfort.test

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.carlkarenfort.test.alarm.AlarmItem
import com.carlkarenfort.test.alarm.AndroidAlarmScheduler
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.LocalTime

class RunningApp: Application() {
    private val TAG = "RunningApp"

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "in onCreate()")

        val channel = NotificationChannel(
            "main_channel",
            "Webunitsalarm Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        setAlarm()
    }

    private fun setAlarm() {
        Log.i(TAG, "called setAlarm")

        //create store data instance
        val storeData = StoreData(applicationContext)


        var switch: Boolean?
        runBlocking {
            switch = storeData.loadAlarmActive()
        }

        if (switch == true) {
            val apiCalls = ApiCalls()
            var schoolStart: LocalTime? = null
            val day = apiCalls.getNextWorkingDay()
            val loginDataNullable: Array<String?>
            val id: Int?
            var tbs: Int?
            runBlocking {
                loginDataNullable = storeData.loadLoginData()
                id = storeData.loadID()
                tbs = storeData.loadTBS()
            }
            Log.i(TAG, "getting schoolstart with id: $id")
            if (loginDataNullable[0] != null || loginDataNullable[1] != null || loginDataNullable[2] != null || loginDataNullable[3] != null || id != null) {
                schoolStart = apiCalls.getSchoolStartForDay(
                    loginDataNullable[0]!!,
                    loginDataNullable[1]!!,
                    loginDataNullable[2]!!,
                    loginDataNullable[3]!!,
                    id!!,
                    day
                )
            } else {
                Log.i(TAG, "loaded data is null, can't get school start")
            }


            if (schoolStart == null || tbs == null || tbs!! < 0) {
                Log.i(TAG, "schoolStart and tbs may not be null")
            } else {
                scheduleAlarm(LocalDateTime.of(day, schoolStart).minusMinutes(tbs!!.toLong()))
            }
        } else {
            //switch is off
        }
    }

    private fun cancelAlarm() {
        Log.i("RunningApp", "cancelling")
        val scheduler = AndroidAlarmScheduler(this)
        var alarmItem: AlarmItem? = null
        alarmItem = AlarmItem(
            845746,
            null
        )
        alarmItem.let(scheduler::schedule)
    }
    private fun scheduleAlarm(time: LocalDateTime) {
        Log.i("RunningApp", "scheduling")
        val scheduler = AndroidAlarmScheduler(this)
        var alarmItem: AlarmItem? = null
        alarmItem = AlarmItem(
            845746,
            time
        )
        alarmItem.let(scheduler::schedule)
    }
}