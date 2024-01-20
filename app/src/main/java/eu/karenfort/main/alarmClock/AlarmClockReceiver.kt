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
package eu.karenfort.main.alarmClock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.carlkarenfort.test.R
import eu.karenfort.main.helper.StoreData
import eu.karenfort.main.ui.ReminderActivity
import eu.karenfort.main.helper.ALARM_CLOCK_ID
import eu.karenfort.main.helper.ALARM_CLOCK_NOTIFICATION_ID
import eu.karenfort.main.helper.MAX_ALARM_DURATION
import eu.karenfort.main.extentions.hideNotification
import eu.karenfort.main.extentions.isScreenOn
import eu.karenfort.main.extentions.showAlarmNotification
import eu.karenfort.main.extentions.showErrorToast
import eu.karenfort.main.helper.ALARM_CLOCK_NOTIFICATION_CHANNEL_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmClockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        StoreData(context).storeAlarmClock(false)

        if (context.isScreenOn) {
            Log.i("test", "screen is on")
            context.showAlarmNotification()
            Handler(Looper.getMainLooper()).postDelayed({
                context.hideNotification(ALARM_CLOCK_ID)
                CoroutineScope(Dispatchers.Default).launch{
                    AlarmClockSetter.main(context, null, false)
                }
            }, MAX_ALARM_DURATION * 1000L)
            return
        }
        Log.i("test", "screen is ff")
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(ALARM_CLOCK_NOTIFICATION_CHANNEL_ID) == null) {
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
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
            notificationManager.notify(ALARM_CLOCK_NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            context.showErrorToast(e)
        }
    }
}