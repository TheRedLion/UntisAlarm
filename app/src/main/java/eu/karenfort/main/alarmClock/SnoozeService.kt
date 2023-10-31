package eu.karenfort.main.alarmClock

import android.app.IntentService
import android.content.Intent
import eu.karenfort.main.helper.ALARM_CLOCK_ID
import eu.karenfort.main.helper.ALARM_ID
import eu.karenfort.main.helper.hideNotification

class SnoozeService : IntentService("Snooze") {
    override fun onHandleIntent(intent: Intent?) {
        val id = intent!!.getIntExtra(ALARM_ID, -1)
        hideNotification(ALARM_CLOCK_ID)
        setupAlarmClock(alarm, config.snoozeTime * MINUTE_SECONDS)
    }
}