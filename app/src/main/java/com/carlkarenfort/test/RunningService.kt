package com.carlkarenfort.test

import android.content.Intent
import android.os.IBinder
import android.os.StrictMode
import android.util.Log
import androidx.core.app.NotificationCompat
import com.carlkarenfort.test.alarm.AlarmItem
import com.carlkarenfort.test.alarm.AndroidAlarmScheduler
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.LocalTime


class RunningService: android.app.Service() {
    private val TAG = "RunningService"

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun stop() {
        Log.i(TAG, "called stop()")
        cancelAlarm()
        stopSelf()
    }

    private fun start() {
        Log.i(TAG, "called start()")
        val notification = NotificationCompat.Builder(this, "main_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Webuntis Alarm Setter")
            .setContentText("some text")
            .build()
        startForeground(261353, notification)
        scheduleAlarm()
    }

    private fun cancelAlarm() {
        Log.i(TAG, "cancelling")
        val scheduler = AndroidAlarmScheduler(this)
        var alarmItem: AlarmItem? = null
        alarmItem = AlarmItem(845746)
        alarmItem.let(scheduler::cancel)
    }

    private fun scheduleAlarm() {
        val scheduler = AndroidAlarmScheduler(this)
        var alarmItem: AlarmItem? = null
        alarmItem = AlarmItem(845746)
        alarmItem.let(scheduler::schedule)
    }

    enum class Actions {
        START, STOP
    }
}