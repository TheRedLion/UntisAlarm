package eu.karenfort.main.alarmClock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import eu.karenfort.main.helper.ALARM_ID
import eu.karenfort.main.helper.ALARM_NOTIFICATION_CHANNEL_ID
import eu.karenfort.main.helper.deleteNotificationChannel
import eu.karenfort.main.helper.ensureBackgroundThread
import eu.karenfort.main.helper.hideNotification

class HideAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ALARM_ID, -1)
        val channelId = intent.getStringExtra(ALARM_NOTIFICATION_CHANNEL_ID)
        channelId?.let { context.deleteNotificationChannel(channelId) }
        context.hideNotification(id)

        ensureBackgroundThread {
            val alarm = context.dbHelper.getAlarmWithId(id)
            if (alarm != null && alarm.days < 0) {
                context.dbHelper.updateAlarmEnabledState(alarm.id, false)
                context.updateWidgets()
            }
        }
    }
}
