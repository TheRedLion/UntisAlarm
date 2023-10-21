package com.carlkarenfort.test.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.carlkarenfort.test.AlarmClock

class AndroidAlarmScheduler(
    private val context: Context
) {
    private val TAG = "AlarmScheduler"
    private var alarmManager = context.getSystemService(AlarmManager::class.java)
    private val ALARM_REQUEST_CODE = 73295871
     fun schedule(item: AlarmItem) {
        Log.i(TAG, "scheduled Alarm")
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent)

    }

    fun cancel(item: AlarmItem) {
        AlarmClock().cancelAlarm(context)
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

}