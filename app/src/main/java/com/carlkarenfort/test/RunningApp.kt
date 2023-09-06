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

class RunningApp: Application() {

    override fun onCreate() {
        super.onCreate()

        //create store data instance
        val storeData = StoreData(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "main_channel",
                "Webunitsalarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        var switch: Boolean?
        runBlocking {
            switch = storeData.loadAlarmActive()
        }

        if (switch == true) {
            val apiCalls: ApiCalls = ApiCalls()
            runBlocking {
                val
                apiCalls.getSchoolStartForDay()
            }
        }

        /*
        Log.i("RunningApp", "scheduling")
        val scheduler = AndroidAlarmScheduler(this)
        var alarmItem: AlarmItem? = null
        alarmItem = AlarmItem(
            id = 1,
            time = LocalDateTime.now()
        )
        alarmItem.let(scheduler::schedule)
         */
    }


    fun setAlarm(time: LocalDateTime) {
        Log.i("RunningApp", "scheduling")
        val scheduler = AndroidAlarmScheduler(this)
        var alarmItem: AlarmItem? = null
        alarmItem = AlarmItem(
            id = 1,
            time = LocalDateTime.now()
        )
        alarmItem.let(scheduler::schedule)
    }
}