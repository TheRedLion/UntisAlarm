/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This Class is used to Manage Alarm Clocks
 */
package eu.karenfort.main.alarmClock

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import eu.karenfort.main.ui.MainActivity
import eu.karenfort.main.extentions.alarmClockPendingIntent
import eu.karenfort.main.extentions.alarmManager
import eu.karenfort.main.extentions.areNotificationsEnabled
import eu.karenfort.main.dataPass.DataPass
import eu.karenfort.main.helper.StoreData
import eu.karenfort.main.helper.isSnowConePlus
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar


class AlarmClock {

    companion object {
        private const val TAG = "AlarmClock"

        fun set(alarmClockDateTime: LocalDateTime, context: Context) {
            cancel(context) //only one alarm may be active at any time

            if (!context.areNotificationsEnabled) {
                if (isSnowConePlus()) { //isUiContext requires Android API 31
                    if (!context.isUiContext) {
                        //make notification warning disappear on main activity
                        DataPass.passNotificationsAllowed(context, false)
                    }
                } else if (MainActivity.active) {
                    //make notification warning disappear on main activity
                    DataPass.passNotificationsAllowed(context, false)
                }
            }//not returning just in case notifications are re-enabled

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

            val alarmManager = context.alarmManager
            val pendingIntent = context.alarmClockPendingIntent

            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(calendarMills, pendingIntent), pendingIntent)

            // this check is necessary because android automatically cancels alarms that happen in the past (back to the future vibes)
            val nextAlarmClock: AlarmManager.AlarmClockInfo = alarmManager.nextAlarmClock
            if (nextAlarmClock.triggerTime > System.currentTimeMillis()) {
                StoreData(context).storeAlarmClock(alarmClockDateTime)
            }
        }

        fun snooze(timeInM: Int, context: Context) {
            if (timeInM < 0) {
                return
            }
            if (!context.areNotificationsEnabled) {
                return
            }

            cancel(context) //only one alarm may be active at any time

            val pendingIntent = context.alarmClockPendingIntent

            val triggerTime = System.currentTimeMillis() + timeInM * 60 * 1000
            context.alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, pendingIntent), pendingIntent)

            val alarmClockTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(triggerTime), ZoneId.systemDefault())
            StoreData(context).storeAlarmClock(alarmClockTime, true) //edited so it does not get overridden

            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }, 1000)

        }


        fun cancel(context: Context) {
            context.alarmManager.cancel(context.alarmClockPendingIntent)
            StoreData(context).storeAlarmClock(null, false)
        }
    }
}