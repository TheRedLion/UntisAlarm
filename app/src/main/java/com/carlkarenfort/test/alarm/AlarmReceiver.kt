package com.carlkarenfort.test.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.carlkarenfort.test.AlarmClock
import java.util.Calendar

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("receiver","I received it tho")
        var clock = AlarmClock()
        var calendar = Calendar.getInstance()
        if (context != null) {
            clock.setAlarm(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE) + 1, context)
            Log.i("receiver","I even reached this point but  no alarm set?")

        } else {
            Log.w("receiver", "couldnt start alarm due to missing context")
        }
        Log.i("receiver","I even reached this point but  no alarm set? 2")


    }
}