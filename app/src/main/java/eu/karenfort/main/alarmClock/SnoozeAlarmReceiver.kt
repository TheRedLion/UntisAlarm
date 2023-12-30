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
package eu.karenfort.main.alarmClock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import eu.karenfort.main.helper.StoreData
import eu.karenfort.main.helper.ALARM_CLOCK_ID
import eu.karenfort.main.extentions.hideNotification
import kotlinx.coroutines.runBlocking

class SnoozeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.hideNotification(ALARM_CLOCK_ID)
        var snoozeTime = 5
        runBlocking {
            snoozeTime = StoreData(context).loadSnoozeTime() ?: return@runBlocking
        }
        AlarmClock.snooze(snoozeTime, context)
    }
}