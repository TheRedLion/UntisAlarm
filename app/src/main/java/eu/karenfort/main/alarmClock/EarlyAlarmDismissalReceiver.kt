package eu.karenfort.main.alarmClock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.carlkarenfort.test.R
import eu.karenfort.main.helper.*

class EarlyAlarmDismissalReceiver : BroadcastReceiver() {
    private val TAG = "EarlyAlarmDismissalReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "in onReceive")
        triggerEarlyDismissalNotification(context)
    }

    private fun triggerEarlyDismissalNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (isOreoPlus()) {
            NotificationChannel(
                EARLY_ALARM_DISMISSAL_CHANNEL_ID,
                "early alarm dismissal something",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setBypassDnd(true)
                setSound(null, null)
                notificationManager.createNotificationChannel(this)
            }
        }

        val dismissIntent = context.getDismissAlarmPendingIntent()
        val contentIntent = context.getOpenAlarmTabIntent()
        val notification = NotificationCompat.Builder(context)
            .setContentTitle(context.getString(R.string.upcoming_alarm))
            .setContentText("Upcoming Alarm. Press dismiss to dismiss the alarm early.")
            .setSmallIcon(R.drawable.ic_alarm_vector)
            .setPriority(Notification.PRIORITY_LOW)
            .addAction(0, context.getString(R.string.dismiss_alarm), dismissIntent)
            .setDeleteIntent(dismissIntent)
            .setContentIntent(contentIntent)
            .setSound(null)
            .setAutoCancel(true)
            .setChannelId(EARLY_ALARM_DISMISSAL_CHANNEL_ID)
            .build()

        notificationManager.notify(EARLY_ALARM_NOTIF_ID, notification)
    }

}
