package eu.karenfort.main.alarmClock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import eu.karenfort.main.StoreData
import eu.karenfort.main.helper.ALARM_CLOCK_ID
import eu.karenfort.main.helper.hideNotification
import eu.karenfort.main.helper.setupAlarmClock
import kotlinx.coroutines.runBlocking

class SnoozeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("SnoozeAlarmReceiver", "in onReceive")
        context.hideNotification(ALARM_CLOCK_ID)
        var snoozeTime = 5
        runBlocking {
            snoozeTime = StoreData(context).loadSnoozeTime() ?: return@runBlocking
        }
        context.setupAlarmClock(snoozeTime * 60)
    }
}