package com.carlkarenfort.test

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService

class RunningApp: Application() {

    override fun onCreate() {
        super.onCreate()

        Intent(this, RunningService::class.java).also {
            it.action = RunningService.Actions.START.toString()
            startService(it)
        }

    }
}