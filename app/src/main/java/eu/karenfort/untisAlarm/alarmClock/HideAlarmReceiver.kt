/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Code originally from https://github.com/SimpleMobileTools/Simple-Clock
 * but modified.
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This is used to hide the Alarm Notification
 */
package eu.karenfort.untisAlarm.alarmClock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import eu.karenfort.untisAlarm.extentions.hideNotification
import eu.karenfort.untisAlarm.helper.ALARM_CLOCK_ID

class HideAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.hideNotification(ALARM_CLOCK_ID)
    }
}
