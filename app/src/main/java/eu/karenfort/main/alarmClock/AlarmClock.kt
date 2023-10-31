package eu.karenfort.main.alarmClock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import eu.karenfort.main.StoreData
import eu.karenfort.main.alarm.AlarmReceiver
import java.time.LocalTime
import java.util.Calendar

import androidx.annotation.Keep
import androidx.versionedparcelable.VersionedParcelize
import eu.karenfort.main.helper.ALARM_ID
import java.io.Serializable


class AlarmClock () {
    private val TAG = "AlarmClock"
    val id = 543
    val label = "UntisAlarm"

    companion object {
        private val TAG = "AlarmClock"
        val id = 543
        @RequiresApi(Build.VERSION_CODES.S)
        fun setAlarm(schoolStart: LocalTime, context: Context) {
            Log.i(TAG, "called setalarm")

            val intent2 = Intent(context, AlarmClockReceiver::class.java)
            intent2.putExtra(ALARM_ID, this as Serializable)
            val pendingIntent = PendingIntent.getBroadcast(context, id, intent2, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                Log.i(TAG, "what the fuck is happening")
                if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                    Log.i(TAG, "this is normal")
                }
            }

            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 16)
                set(Calendar.MINUTE, 11)
                set(Calendar.SECOND, 0)
            }

            val calendarMills = calendar.timeInMillis
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(calendarMills, pendingIntent), pendingIntent)

            val nextAlarmClock: AlarmManager.AlarmClockInfo = alarmManager.nextAlarmClock

            // this check is necessary because android automatically cancels alarms that happen in the past (back to the future vibes)
            if (nextAlarmClock.triggerTime > System.currentTimeMillis()) {
                val storeData = StoreData(context)
                storeData.storeAlarmClock(schoolStart.hour, schoolStart.minute)
            }

            //don't know a different way to update the alarmPreview in main activity
            /*val intent3 = Intent(context, MainActivity::class.java)
            intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent3)*/
        }

        fun cancelAlarm(context: Context) {
            Log.i(TAG, "called")
            val intent2 = Intent(context, AlarmReceiver::class.java)
            intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

            val pendingIntent = PendingIntent.getBroadcast(context, id, intent2,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(pendingIntent)

            val storeData = StoreData(context)
            storeData.storeAlarmClock(null, null)
        }
    }
}