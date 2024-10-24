/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This schedules the alarm that is responsible for settings Alarm Clocks.
 */
package eu.karenfort.untisAlarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import eu.karenfort.untisAlarm.helper.ALARM_REQUEST_CODE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AlarmScheduler(
    private val context: Context
) {
    private var alarmManager = context.getSystemService(AlarmManager::class.java)
    private val TAG = "AlarmScheduler"

    fun schedule() {
        Log.i(TAG, "scheduling alarms")

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 10000, //run again after 10 seconds
            AlarmManager.INTERVAL_HOUR,
            pendingIntent
        )
    }

    fun cancel() {
        Log.i(TAG, "canceling alarm")
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}