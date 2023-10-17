package com.carlkarenfort.test

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmClock {
    private val TAG = "AlarmClock"

    suspend fun setAlarm(hour: Int, minute: Int, context: Context) {
        Log.i(TAG, "Setting an AlarmClock for $hour:$minute.")

        val intent = Intent(context, RunningService::class.java)
        intent.action = RunningService.Actions.START.toString()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startService(intent)



        //store time alarm was set for
        CoroutineScope(Dispatchers.IO).launch {
            val storeData = StoreData(context)
            storeData.storeAlarmClock(hour, minute)
        }
    }

    suspend fun cancelAlarm(context: Context) {
        Log.i(TAG, "Cancelling AlarmClock")
        val intent = Intent(AlarmClock.ACTION_DISMISS_ALARM)
        intent.putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_LABEL)
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, "Alarm set by UntisAlarm.")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}