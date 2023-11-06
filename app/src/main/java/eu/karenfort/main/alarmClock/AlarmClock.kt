package eu.karenfort.main.alarmClock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import eu.karenfort.main.StoreData
import java.time.LocalTime
import java.util.Calendar

import eu.karenfort.main.helper.ALARM_CLOCK_ID


class AlarmClock {
    private val TAG = "AlarmClock"

    companion object {
        private val TAG = "AlarmClock"

        fun setAlarm(schoolStart: LocalTime, context: Context) {
            Log.i(TAG, "called setalarm(Clock)")

            val intent = Intent(context, AlarmClockReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, ALARM_CLOCK_ID, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                Log.i(TAG, "this is bad")
            }

            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, schoolStart.hour) //should be schoolstart.hour and minute //tod: remove debug code
                set(Calendar.MINUTE, schoolStart.minute)
                set(Calendar.SECOND, 0)
            }

            val calendarMills = calendar.timeInMillis
            //val calendarMills = System.currentTimeMillis() + 5000 //tod: remove debug code
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(calendarMills, pendingIntent), pendingIntent)

            val nextAlarmClock: AlarmManager.AlarmClockInfo = alarmManager.nextAlarmClock

            // this check is necessary because android automatically cancels alarms that happen in the past (back to the future vibes)
            if (nextAlarmClock.triggerTime > System.currentTimeMillis()) {
                val storeData = StoreData(context)
                storeData.storeAlarmClock(schoolStart.hour, schoolStart.minute)
            }

            //todo: do with companion object instead or smth
            /*val intent3 = Intent(context, MainActivity::class.java)
            intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent3)*/
        }

        fun setAlarm(timeInS: Int, context: Context) {
            Log.i(TAG, "called setalarm(Clock)")

            val intent2 = Intent(context, AlarmClockReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, ALARM_CLOCK_ID, intent2, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                Log.i(TAG, "this is bad")
            }

            val triggerTime = System.currentTimeMillis() + timeInS * 1000 //todo: remove debug code
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, pendingIntent), pendingIntent)



            //todo: do with companion object instead or smth
            /*val intent3 = Intent(context, MainActivity::class.java)
            intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent3)*/
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