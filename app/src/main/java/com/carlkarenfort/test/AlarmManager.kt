package com.carlkarenfort.test

import android.content.Intent
import android.provider.AlarmClock
import androidx.core.content.ContextCompat.startActivity

class AlarmManager {
    fun setAlarm(hour: Int, minute: Int) {
        var intent = Intent(AlarmClock.ACTION_SET_ALARM)
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        intent.putExtra(AlarmClock.EXTRA_HOUR, hour)
        intent.putExtra(AlarmClock.EXTRA_MINUTES, minute)
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, "yoooo")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //startActivity(intent)
    }
}