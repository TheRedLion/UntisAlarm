package eu.karenfort.main.alarmClock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import eu.karenfort.main.StoreData
import eu.karenfort.main.activities.MainActivity
import java.time.LocalTime
import java.util.Calendar

import eu.karenfort.main.helper.ALARM_CLOCK_ID
import eu.karenfort.main.helper.areNotificationsEnabled
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale


class AlarmClock {
    private val TAG = "AlarmClock"

    companion object {
        private val TAG = "AlarmClock"

        fun setAlarm(schoolStart: LocalDateTime, context: Context) {
            Log.i(TAG, "setting alarm Clock for $schoolStart")

            //only one alarm may be active at any time
            cancelAlarm(context)

            val intent = Intent(context, AlarmClockReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, ALARM_CLOCK_ID, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!context.areNotificationsEnabled()) {
                Log.i(TAG, "this is bad")

                if (MainActivity.active) {
                    val intent1 = Intent(context, MainActivity::class.java)
                    intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent1.putExtra("areNotifsAllowed", "false")
                    context.startActivity(intent1)
                }
            }

            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.YEAR, schoolStart.year)
                set(Calendar.MONTH, schoolStart.monthValue)
                set(Calendar.DAY_OF_YEAR, schoolStart.dayOfYear)
                set(Calendar.HOUR_OF_DAY, schoolStart.hour)
                set(Calendar.MINUTE, schoolStart.minute)
                set(Calendar.SECOND, 0)
            }

            val calendarMills = calendar.timeInMillis

            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(calendarMills, pendingIntent), pendingIntent)

            val nextAlarmClock: AlarmManager.AlarmClockInfo = alarmManager.nextAlarmClock

            // this check is necessary because android automatically cancels alarms that happen in the past (back to the future vibes)
            if (nextAlarmClock.triggerTime > System.currentTimeMillis()) {
                StoreData(context).storeAlarmClock(schoolStart)

                //update UI in mainActivity

                if (MainActivity.active) {
                    val intent1 = Intent(context, MainActivity::class.java)
                    intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent1.putExtra(
                        "newAlarmClockTime", "${
                            schoolStart.dayOfWeek.getDisplayName(
                                TextStyle.SHORT, Locale.getDefault()
                            )
                        } ${schoolStart.hour}:${schoolStart.minute}"
                    )
                    context.startActivity(intent1)
                }
            }
        }

        fun snoozeAlarm(timeInS: Int, context: Context) {

            if (timeInS < 0) return

            Log.i(TAG, "setting alarm clock for in $timeInS seconds or ${timeInS/60}")

            //only one alarm may be active at any time
            cancelAlarm(context)

            val intent2 = Intent(context, AlarmClockReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, ALARM_CLOCK_ID, intent2, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!context.areNotificationsEnabled()) {
                Log.i(TAG, "notifications are not enabled")
            }

            val triggerTime = System.currentTimeMillis() + timeInS * 1000
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, pendingIntent), pendingIntent)

            StoreData(context).storeAlarmClock(LocalDateTime.ofInstant(Instant.ofEpochMilli(triggerTime), ZoneId.systemDefault()), true)

            if (MainActivity.active) {
                //update UI in mainActivity
                val intent1 = Intent(context, MainActivity::class.java)
                intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent1.putExtra(
                    "newAlarmClockTime",
                    "${LocalTime.now().plusSeconds(timeInS.toLong()).hour}:${
                        LocalTime.now().plusSeconds(timeInS.toLong()).minute
                    }"
                )
                context.startActivity(intent1)
            }
        }


        fun cancelAlarm(context: Context) {
            Log.i(TAG, "canceling")
            val intent2 = Intent(context, AlarmClockReceiver::class.java)
            intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

            val pendingIntent = PendingIntent.getBroadcast(context, ALARM_CLOCK_ID, intent2,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(pendingIntent)

            val storeData = StoreData(context)
            storeData.storeAlarmClock(LocalDateTime.of(-1,1,1,1,1), false)
        }
    }
}