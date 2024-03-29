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
package eu.karenfort.untisAlarm.alarmClock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import eu.karenfort.untisAlarm.helper.ensureBackgroundCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DismissAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ensureBackgroundCoroutine {
            AlarmClock.cancel(context)
        }
        CoroutineScope(Dispatchers.Default).launch {
            AlarmClockSetter.main(context)
        }
    }
}
