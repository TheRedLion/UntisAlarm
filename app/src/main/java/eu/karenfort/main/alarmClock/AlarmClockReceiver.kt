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
import eu.karenfort.main.helper.ALARM_CLOCK_ID
import eu.karenfort.main.helper.ALARM_NOTIFICATION_CHANNEL_ID
import eu.karenfort.main.helper.ALARM_NOTIF_ID
import eu.karenfort.main.helper.EARLY_ALARM_NOTIF_ID
import eu.karenfort.main.helper.hideNotification
import eu.karenfort.main.helper.isScreenOn
import eu.karenfort.main.helper.showAlarmNotification
import eu.karenfort.main.helper.showErrorToast

class AlarmClockReceiver : BroadcastReceiver() {
    private val TAG = "AlarmClockReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "called alarmclock receiver")

        context.hideNotification(EARLY_ALARM_NOTIF_ID) // hide early dismissal notification if not already dismissed

        if (context.isScreenOn()) {
            Log.i(TAG, "showing screen notification alarm")
            context.showAlarmNotification()
            Handler(Looper.getMainLooper()).postDelayed({
                context.hideNotification(ALARM_CLOCK_ID)
                Log.i(TAG, "UN-showing screen notification alarm")
            }, 10000)
        } else {
            Log.i(TAG, "showing full activity view")
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(ALARM_NOTIFICATION_CHANNEL_ID) == null) {
                notificationManager.deleteNotificationChannel("Alarm") // cleans up previous notification channel that had sound properties
                NotificationChannel(
                    ALARM_NOTIFICATION_CHANNEL_ID,
                    "Alarm",
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

            val builder = NotificationCompat.Builder(context, ALARM_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.alarm_vector)
                .setContentTitle(context.getString(R.string.app_name))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)

            try {
                notificationManager.notify(ALARM_NOTIF_ID, builder.build())
            } catch (e: Exception) {
                context.showErrorToast(e)
            }
        }
    }
}