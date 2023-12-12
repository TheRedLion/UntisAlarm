package eu.karenfort.main.alarmClock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import eu.karenfort.main.alarm.AlarmManager
import eu.karenfort.main.helper.EARLY_ALARM_DISMISSAL_CHANNEL_ID
import eu.karenfort.main.helper.EARLY_ALARM_NOTIF_ID
import eu.karenfort.main.helper.cancelAlarmClock
import eu.karenfort.main.helper.deleteNotificationChannel
import eu.karenfort.main.helper.ensureBackgroundThread
import eu.karenfort.main.helper.hideNotification


class DismissAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("DismissAlarmReceiver", "in onReceive")
        context.hideNotification(EARLY_ALARM_NOTIF_ID)
        context.deleteNotificationChannel(EARLY_ALARM_DISMISSAL_CHANNEL_ID)
        ensureBackgroundThread {
            context.cancelAlarmClock()
        }
        AlarmManager.main(context)
    }
}
