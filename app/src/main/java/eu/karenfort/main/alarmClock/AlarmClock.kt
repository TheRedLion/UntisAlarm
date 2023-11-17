package eu.karenfort.main.alarmClock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import eu.karenfort.main.StoreData
import eu.karenfort.main.activities.MainActivity
import java.time.LocalTime
import java.util.Calendar

import eu.karenfort.main.helper.ALARM_CLOCK_ID
import eu.karenfort.main.helper.areNotificationsEnabled
import java.time.LocalDateTime


class AlarmClock {
    private val TAG = "AlarmClock"

    companion object {
        private val TAG = "AlarmClock"

        fun setAlarm(schoolStart: LocalDateTime, context: Context) {
            Log.i(TAG, "called setalarm(Clock)")

            val intent = Intent(context, AlarmClockReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, ALARM_CLOCK_ID, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!context.areNotificationsEnabled()) {
                Log.i(TAG, "this is bad")

                val intent1 = Intent(context, MainActivity::class.java)
                intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent1.putExtra("areNotifsAllowed", "false")
                context.startActivity(intent1)
            }

            //tod: debuging stuff in here
            //schoolStart = LocalTime.now().plusMinutes(1)
            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.DAY_OF_YEAR, schoolStart.dayOfYear)
                set(Calendar.HOUR_OF_DAY, schoolStart.hour)
                set(Calendar.MINUTE, schoolStart.minute)
                set(Calendar.SECOND, 0)
            }
            Log.i(TAG, "Hour: ")
            Log.i(TAG, schoolStart.hour.toString())
            Log.i(TAG, "Minute: ")
            Log.i(TAG, schoolStart.minute.toString())

            val calendarMills = calendar.timeInMillis

            //val calendarMills = System.currentTimeMillis() + 5000 //tod: remove debug code
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(calendarMills, pendingIntent), pendingIntent)

            val nextAlarmClock: AlarmManager.AlarmClockInfo = alarmManager.nextAlarmClock

            // this check is necessary because android automatically cancels alarms that happen in the past (back to the future vibes)
            if (nextAlarmClock.triggerTime > System.currentTimeMillis()) {
                val storeData = StoreData(context)
                storeData.storeAlarmClock(schoolStart.hour, schoolStart.minute)

                val intent1 = Intent(context, MainActivity::class.java)
                intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent1.putExtra("newAlarmClockTime", "${schoolStart.hour}:${schoolStart.minute}")
                context.startActivity(intent1)
            }
        }

        fun setAlarm(timeInS: Int, context: Context) {
            Log.i(TAG, "called setalarm(Clock)")

            val intent2 = Intent(context, AlarmClockReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, ALARM_CLOCK_ID, intent2, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!context.areNotificationsEnabled()) {
                Log.i(TAG, "notifications are not enabled")
            }

            val triggerTime = System.currentTimeMillis() + timeInS * 1000
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, pendingIntent), pendingIntent)
        }


        fun cancelAlarm(context: Context) {
            Log.i(TAG, "called")
            val intent2 = Intent(context, AlarmClockReceiver::class.java)
            intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

            val pendingIntent = PendingIntent.getBroadcast(context, ALARM_CLOCK_ID, intent2,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(pendingIntent)

            val storeData = StoreData(context)
            storeData.storeAlarmClock(null, null)
        }
    }
}