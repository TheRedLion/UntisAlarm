package com.carlkarenfort.test

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.carlkarenfort.test.alarm.AlarmItem
import com.carlkarenfort.test.alarm.AndroidAlarmScheduler
import java.time.LocalDateTime

class RunningApp: Application() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "main_channel",
                "Webunitsalarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        Log.i("RunningApp","sceduling")
        val scheduler = AndroidAlarmScheduler(this)
        var alarmItem: AlarmItem? = null
        alarmItem = AlarmItem(
            id = 1,
            time = LocalDateTime.now()
        )
        alarmItem.let(scheduler::schedule)

    }
}