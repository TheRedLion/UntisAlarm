/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Code originally from https://github.com/SimpleMobileTools/Simple-Clock
 * but modified.
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This Receiver is called when an alarm was dismissed.
 */
package eu.karenfort.main.alarmClock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import eu.karenfort.main.helper.EARLY_ALARM_DISMISSAL_CHANNEL_ID
import eu.karenfort.main.helper.EARLY_ALARM_NOTIF_ID
import eu.karenfort.main.helper.cancelAlarmClock
import eu.karenfort.main.helper.deleteNotificationChannel
import eu.karenfort.main.helper.ensureBackgroundThread
import eu.karenfort.main.helper.hideNotification


class DismissAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.hideNotification(EARLY_ALARM_NOTIF_ID)
        context.deleteNotificationChannel(EARLY_ALARM_DISMISSAL_CHANNEL_ID)
        ensureBackgroundThread {
            context.cancelAlarmClock()
        }
        AlarmClockSetter.main(context)
    }
}
