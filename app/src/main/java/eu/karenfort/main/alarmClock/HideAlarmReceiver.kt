package eu.karenfort.main.alarmClock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import eu.karenfort.main.helper.ALARM_CLOCK_ID
import eu.karenfort.main.helper.ALARM_NOTIFICATION_CHANNEL_ID
import eu.karenfort.main.helper.deleteNotificationChannel
import eu.karenfort.main.helper.hideNotification

class HideAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.deleteNotificationChannel(ALARM_NOTIFICATION_CHANNEL_ID)
        context.hideNotification(ALARM_CLOCK_ID)

        /*
        ensureBackgroundThread {
            val alarm = context.dbHelper.getAlarmWithId(id)
            if (alarm != null && alarm.days < 0) {
                context.dbHelper.updateAlarmEnabledState(alarm.id, false)
                //context.updateWidgets()
            }
        }*/
    }
}
