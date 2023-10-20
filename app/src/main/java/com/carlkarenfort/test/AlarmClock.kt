package com.carlkarenfort.test

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.carlkarenfort.test.alarm.AlarmClockReceiver
import com.carlkarenfort.test.alarm.AlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
import java.util.Calendar

class AlarmClock {
    private val tag = "AlarmClock"

    @RequiresApi(Build.VERSION_CODES.S)
    fun setAlarm(schoolStart: LocalTime, context: Context) {
        Log.i(tag, "called setalarm")

        val intent2 = Intent(context, AlarmClockReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(context, 543, intent2, PendingIntent.FLAG_IMMUTABLE)

        val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 31)
        }
        val calendarMills = calendar.timeInMillis
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(calendarMills, pendingIntent), pendingIntent)

        val nextAlarmClock: AlarmManager.AlarmClockInfo = alarmManager.nextAlarmClock

        //store alarmClock time
        // this check is necessary because android automatically cancels alarms that happen in the past (back to the future vibes)
        if (nextAlarmClock.triggerTime > System.currentTimeMillis()) {
            CoroutineScope(Dispatchers.IO).launch {
                val storeData = StoreData(context)
                storeData.storeAlarmClock(schoolStart.hour, schoolStart.minute)
            }
        }


        //update main activity
        /*val intent3 = Intent(context, MainActivity::class.java)
        intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent3)*/
    }

    fun cancelAlarm(context: Context) {
        Log.i(tag, "called")
        val intent2 = Intent(context, AlarmReceiver::class.java)
        intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        val pendingIntent = PendingIntent.getBroadcast(context, 543, intent2,
            PendingIntent.FLAG_IMMUTABLE)

        val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(pendingIntent)

        //store alarmClock time
        runBlocking {
            val storeData = StoreData(context)
            storeData.storeAlarmClock(null, null)
        }

    }
}