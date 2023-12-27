/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This Class is used to Manage Alarm Clocks
 */
package eu.karenfort.main.alarmClock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import eu.karenfort.main.StoreData
import eu.karenfort.main.activities.MainActivity
import eu.karenfort.main.helper.ALARM_CLOCK_ID
import eu.karenfort.main.helper.NOTIFS_ALLOWED
import eu.karenfort.main.helper.areNotificationsEnabled
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar


class AlarmClock {

    companion object {
        private const val TAG = "AlarmClock"

        fun setAlarmClock(alarmClockDateTime: LocalDateTime, context: Context) {
            //only one alarm may be active at any time
            cancelAlarmClock(context)

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!context.areNotificationsEnabled()) {
                Log.i(TAG, "this is bad")

                if (MainActivity.active) {
                    //make notification warning disappear on main activity
                    val intent1 = Intent(context, MainActivity::class.java)
                    intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    //intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent1.putExtra(NOTIFS_ALLOWED, false)
                    context.startActivity(intent1)
                }
            }

            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.YEAR, alarmClockDateTime.year)
                set(Calendar.MONTH, alarmClockDateTime.monthValue)
                set(Calendar.DAY_OF_YEAR, alarmClockDateTime.dayOfYear)
                set(Calendar.HOUR_OF_DAY, alarmClockDateTime.hour)
                set(Calendar.MINUTE, alarmClockDateTime.minute)
                set(Calendar.SECOND, 0)
            }
            val calendarMills = calendar.timeInMillis


            val intent = Intent(context, AlarmClockReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, ALARM_CLOCK_ID, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(calendarMills, pendingIntent), pendingIntent)

            val nextAlarmClock: AlarmManager.AlarmClockInfo = alarmManager.nextAlarmClock

            // this check is necessary because android automatically cancels alarms that happen in the past (back to the future vibes)
            if (nextAlarmClock.triggerTime > System.currentTimeMillis()) {
                StoreData(context).storeAlarmClock(alarmClockDateTime)

                //todo check if update of alarmPreview in main activity is necessary
            }
        }

        fun snoozeAlarmClock(timeInM: Int, context: Context) {
            if (timeInM < 0) return
            if (!context.areNotificationsEnabled()) {
                return
            }

            //only one alarm may be active at any time
            cancelAlarmClock(context)

            val intent = Intent(context, AlarmClockReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, ALARM_CLOCK_ID, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val triggerTime = System.currentTimeMillis() + timeInM * 60 * 1000
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, pendingIntent), pendingIntent)

            val alarmClockTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(triggerTime), ZoneId.systemDefault())
            StoreData(context).storeAlarmClock(alarmClockTime, true) //edited so it does not get overridden

            Handler(Looper.getMainLooper()).postDelayed({
                val intent2 = Intent(context, MainActivity::class.java)
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent2)
            }, 1000)

        }


        fun cancelAlarmClock(context: Context) {
            val intent = Intent(context, AlarmClockReceiver::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

            val pendingIntent = PendingIntent.getBroadcast(context,
                ALARM_CLOCK_ID,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(pendingIntent)

            val storeData = StoreData(context)
            storeData.storeAlarmClock(null, false)
        }
    }
}