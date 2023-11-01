package eu.karenfort.main.alarmClock

import android.app.IntentService
import android.content.Intent
import eu.karenfort.main.StoreData
import eu.karenfort.main.helper.ALARM_CLOCK_ID
import eu.karenfort.main.helper.ALARM_ID
import eu.karenfort.main.helper.hideNotification
import eu.karenfort.main.helper.setupAlarmClock
import kotlinx.coroutines.runBlocking

class SnoozeService : IntentService("Snooze") {
    override fun onHandleIntent(intent: Intent?) {
        val id = intent!!.getIntExtra(ALARM_ID, -1)
        hideNotification(ALARM_CLOCK_ID)
        var snoozeTime = 5
        runBlocking {
            snoozeTime = StoreData(applicationContext).loadSnoozeTime() ?: return@runBlocking
        }
        setupAlarmClock(snoozeTime * 60)
    }
}