package com.carlkarenfort.test

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.carlkarenfort.test.alarm.AlarmItem
import com.carlkarenfort.test.alarm.AndroidAlarmScheduler
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.LocalTime

class RunningApp: Application() {
    private val TAG = "RunningApp"
    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            "main_channel",
            "Webunitsalarm Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        setAlarm()
    }

    fun setAlarm() {
        Log.i(TAG, "called setAlarm")

        //create store data instance
        val storeData = StoreData(applicationContext)


        var switch: Boolean?
        runBlocking {
            switch = storeData.loadAlarmActive()
        }

        if (switch == true) {
            val apiCalls = ApiCalls()
            var schoolStart: LocalTime?
            var tbs: Int?

            val day = apiCalls.getNextWorkingDay()
            runBlocking {
                val loginDataNullable = storeData.loadLoginData()
                val id = storeData.loadID()
                tbs = storeData.loadTBS()
                schoolStart = apiCalls.getSchoolStartForDay(
                    loginDataNullable[0]!!,
                    loginDataNullable[1]!!,
                    loginDataNullable[2]!!,
                    loginDataNullable[3]!!,
                    id!!,
                    day
                )
            }

            if (schoolStart == null || tbs == null || tbs!! < 0) {
                Log.i(TAG, "schoolStart and tbs may not be null")
            } else {
                scheduleAlarm(LocalDateTime.of(day, schoolStart).minusMinutes(tbs!!.toLong()))
            }
        } else {
            //switch is off
        }
    }

    private fun scheduleAlarm(time: LocalDateTime) {
        Log.i("RunningApp", "scheduling")
        val scheduler = AndroidAlarmScheduler(this)
        var alarmItem: AlarmItem? = null
        alarmItem = AlarmItem(
            845746,
            time
        )
        alarmItem.let(scheduler::schedule)
    }
}