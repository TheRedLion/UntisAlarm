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
import eu.karenfort.untisAlarm.alarmClock.AlarmClock
import eu.karenfort.untisAlarm.alarmClock.AlarmClockSetter
import eu.karenfort.untisAlarm.helper.ALARM_REQUEST_CODE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmScheduler(
    private val context: Context
) {
    private var alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            AlarmClockSetter.main(context, true) //is always active if alarm was just scheduled
        }
        //todo check if works without
        /*val intent = Intent(context, AlarmReceiver::class.java).also {
            it.action = Intent.ACTION_CALL
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 900000,
            pendingIntent
        )*/
    }

    fun cancel() {
        AlarmClock.cancel(context)
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                Intent(context, AlarmReceiver::class.java).also {
                    it.action = Intent.ACTION_CALL
                },
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}