package eu.karenfort.main.alarmClock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.carlkarenfort.test.R
import eu.karenfort.main.helper.*

class EarlyAlarmDismissalReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(ALARM_ID, -1)
        if (alarmId == -1) {
            return
        }

        triggerEarlyDismissalNotification(context, alarmId)
    }

    private fun triggerEarlyDismissalNotification(context: Context, alarmId: Int) {
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

        val dismissIntent = context.getDismissAlarmPendingIntent(alarmId, EARLY_ALARM_NOTIF_ID)
        val contentIntent = context.getOpenAlarmTabIntent()
        val notification = NotificationCompat.Builder(context)
            .setContentTitle(context.getString(R.string.upcoming_alarm))
            .setContentText("test, here should be an alarm string")
            .setSmallIcon(R.drawable.ic_alarm_vector)
            .setPriority(Notification.PRIORITY_LOW)
            .addAction(0, context.getString(R.string.dismiss_alarm), dismissIntent)
            .setContentIntent(contentIntent)
            .setSound(null)
            .setAutoCancel(true)
            .setChannelId(EARLY_ALARM_DISMISSAL_CHANNEL_ID)
            .build()

        notificationManager.notify(EARLY_ALARM_NOTIF_ID, notification)
    }

}
