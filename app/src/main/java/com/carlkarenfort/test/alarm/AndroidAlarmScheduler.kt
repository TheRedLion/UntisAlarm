package com.carlkarenfort.test.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date

class AndroidAlarmScheduler(
    private val context: Context
): AlarmScheduler {
    private var alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(item: AlarmItem) {
        println("scheduled")
        val intent = Intent(context, AlarmReciever::class.java)
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            item.time.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!!,
            PendingIntent.getBroadcast(
                context,
                item.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            )
        )
    }

    override fun cancal(item: AlarmItem) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                item.id.hashCode(),
                Intent(context, AlarmReciever::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            )
        )
    }

}