package com.carlkarenfort.test

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat


class RunningService: android.app.Service() {
    private val TAG = "RunningService"
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        Log.i(TAG, "called start()")
        val notification = NotificationCompat.Builder(this, "main_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Webuntis Alarm Setter")
            .setContentText("some text")
            .build()
        startForeground(1, notification)
    }

    enum class Actions {
        START, STOP
    }
}