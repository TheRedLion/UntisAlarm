package eu.karenfort.main.alarmClock;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import eu.karenfort.main.helper.ALARM_ID
import eu.karenfort.main.helper.NOTIFICATION_ID
import eu.karenfort.main.helper.cancelAlarmClock
import eu.karenfort.main.helper.ensureBackgroundThread
import eu.karenfort.main.helper.hideNotification
import java.util.Calendar
import kotlin.math.pow


class DismissAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(ALARM_ID, -1)
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, -1)
        if (alarmId == -1) {
            return
        }

        context.hideNotification(notificationId)

        ensureBackgroundThread {
            context.cancelAlarmClock()

        }
    }
}
