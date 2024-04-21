/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Code originally from https://github.com/SimpleMobileTools/Simple-Clock
 * but modified.
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: Is called when an alarm is snoozed.
 */
package eu.karenfort.untisAlarm.alarmClock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import eu.karenfort.untisAlarm.extentions.hideNotification
import eu.karenfort.untisAlarm.helper.ALARM_CLOCK_ID
import eu.karenfort.untisAlarm.helper.COROUTINE_EXCEPTION_HANDLER
import eu.karenfort.untisAlarm.helper.DEFAULT_SNOOZE_MIN
import eu.karenfort.untisAlarm.helper.StoreData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SnoozeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.hideNotification(ALARM_CLOCK_ID)
        CoroutineScope(Dispatchers.Default + COROUTINE_EXCEPTION_HANDLER).launch {
            val snoozeTime = StoreData(context).loadSnoozeTime() ?: DEFAULT_SNOOZE_MIN
            AlarmClock.snooze(snoozeTime, context)
        }
    }
}