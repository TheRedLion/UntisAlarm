package com.carlkarenfort.test

import android.content.Intent
import android.os.IBinder
import android.os.StrictMode
import android.util.Log
import androidx.core.app.NotificationCompat
import com.carlkarenfort.test.alarm.AlarmItem
import com.carlkarenfort.test.alarm.AndroidAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.LocalTime


class RunningService: android.app.Service() {
    private val TAG = "RunningService"
    private var hour: Int? = null
    private var minute: Int? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val notification = NotificationCompat.Builder(this, "main_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Webuntis Alarm Setter")
            .setContentText(getString(R.string.foregroundService_notification))
            .build()
        startForeground(261353, notification)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "on start")
        if (intent != null) {
            hour = intent.getIntExtra("hour", 100)
            minute = intent.getIntExtra("minute", 100)
        }
        when(intent?.action) {
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun stop() {
        Log.i(TAG, "called stop()")
        stopSelf()
    }

    private fun start() {
        val clock: AlarmClock = AlarmClock()
        clock.setAlarm(hour!!, minute!!, this)
        /*CoroutineScope(Dispatchers.IO).launch {
            if (hour != null && minute != null) {

            } else {
                Log.i(TAG, "something went wrong")
            }
        }*/
    }


    /*
    private fun cancelAlarm() {
        Log.i(TAG, "cancelling")
        val scheduler = AndroidAlarmScheduler(this)
        var alarmItem: AlarmItem? = null
        alarmItem = AlarmItem(845746)
        alarmItem.let(scheduler::cancel)
    }
     */

    /*
    private fun scheduleAlarm() {
        val scheduler = AndroidAlarmScheduler(this)
        var alarmItem: AlarmItem? = null
        alarmItem = AlarmItem(845746)
        alarmItem.let(scheduler::schedule)
    }
     */

    enum class Actions {
        START, STOP
    }
}