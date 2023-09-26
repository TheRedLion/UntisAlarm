package com.carlkarenfort.test.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.carlkarenfort.test.AlarmClock
import java.time.ZoneOffset

class AndroidAlarmScheduler(
    private val context: Context
): AlarmScheduler {
    private var alarmManager = context.getSystemService(AlarmManager::class.java)
    private val ALARM_REQUEST_CODE = 73295871
    override fun schedule(item: AlarmItem) {
        Log.i("AlarmScheduler", "scheduled Alarm")
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis() + 2000, pendingIntent)
    }

    override fun cancel(item: AlarmItem) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                item.id.hashCode(),
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            )
        )
    }

}