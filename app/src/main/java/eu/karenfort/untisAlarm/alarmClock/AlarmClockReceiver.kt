/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Code originally from https://github.com/SimpleMobileTools/Simple-Clock
 * but modified.
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This Receiver is called when the alarm is supposed to go off.
 */
package eu.karenfort.untisAlarm.alarmClock

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import eu.karenfort.untisAlarm.R
import eu.karenfort.untisAlarm.extentions.hideNotification
import eu.karenfort.untisAlarm.extentions.isScreenOn
import eu.karenfort.untisAlarm.extentions.showAlarmNotification
import eu.karenfort.untisAlarm.extentions.showErrorToast
import eu.karenfort.untisAlarm.helper.ALARM_CLOCK_ID
import eu.karenfort.untisAlarm.helper.ALARM_CLOCK_NOTIFICATION_CHANNEL_ID
import eu.karenfort.untisAlarm.helper.ALARM_CLOCK_NOTIFICATION_ID
import eu.karenfort.untisAlarm.helper.MAX_ALARM_DURATION
import eu.karenfort.untisAlarm.helper.StoreData
import eu.karenfort.untisAlarm.ui.ReminderActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmClockReceiver : BroadcastReceiver() {
    private val TAG = "AlarmClockReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "receiving alarm clock")
        StoreData(context).storeAlarmClock(false)
        if (context.isScreenOn) {
            context.showAlarmNotification()
            Handler(Looper.getMainLooper()).postDelayed({
                context.hideNotification(ALARM_CLOCK_ID)
                CoroutineScope(Dispatchers.Default).launch {
                    AlarmClockSetter.main(context, null, false)
                }
            }, MAX_ALARM_DURATION * 1000L)
            return
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (
            notificationManager.getNotificationChannel(ALARM_CLOCK_NOTIFICATION_CHANNEL_ID) == null
        ) {
            notificationManager.deleteNotificationChannel(ALARM_CLOCK_NOTIFICATION_CHANNEL_ID)
            NotificationChannel(
                ALARM_CLOCK_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.alarm_clock_notifications),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setBypassDnd(true)
                setSound(null, null)
                notificationManager.createNotificationChannel(this)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, ReminderActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, ALARM_CLOCK_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_vector)
            .setContentTitle(context.getString(R.string.app_name))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)

        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notificationManager.notify(ALARM_CLOCK_NOTIFICATION_ID, builder.build())
            Log.i(TAG, "showing fullscreen notification")
        } catch (e: Exception) {
            Log.i(TAG, "error")
            context.showErrorToast(e)
        }
    }
}