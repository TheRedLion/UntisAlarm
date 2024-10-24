/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This Receiver calls AlarmClockSetter.
 */
package eu.karenfort.untisAlarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import eu.karenfort.untisAlarm.alarmClock.AlarmClockSetter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "called onReceive")
        //intent check to prevent spoofed intents since this receiver is called upon phone restart
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_CALL
        ) {
            return
        }
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            AlarmScheduler(context).schedule()
        }
        CoroutineScope(Dispatchers.Default).launch {
            AlarmClockSetter.main(context)
        }
    }
}