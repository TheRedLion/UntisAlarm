package com.carlkarenfort.test.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReciever: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        println("recieved alarm")
    }
}