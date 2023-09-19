package com.carlkarenfort.test.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import android.util.Log
import com.carlkarenfort.test.AlarmClock
import com.carlkarenfort.test.ApiCalls
import com.carlkarenfort.test.StoreData
import com.google.android.material.tabs.TabLayout.TabGravity
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

class AlarmReceiver: BroadcastReceiver() {
    private val TAG = "AlarmReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG ,"called onReceive()")

        //get loginData and ID
        val storeData = StoreData(context!!) //TODO("remove !! and add error exeption")
        var id: Int?
        var loginData: Array<String?>
        runBlocking {
            id = storeData.loadID()
            loginData = storeData.loadLoginData()
        }

        if (!(id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null)) {

            val apiCalls = ApiCalls()
            val schoolStart = apiCalls.getSchoolStartForDay(
                loginData[0]!!,
                loginData[1]!!,
                loginData[2]!!,
                loginData[3]!!,
                id!!,
                LocalDate.now()
            )

            val clock = AlarmClock()
            if (schoolStart != null) {
                clock.setAlarm(
                    schoolStart.hour, //TODO("remove !! and add errror exeption")
                    schoolStart.minute,
                    context
                )
                Log.i(TAG, "set Alarm")
            } else {
                Log.i(TAG, "school start returned null")
            }
        }
    }

    /*
    private fun setAlarm() {
        Log.i(TAG, "called setAlarm")

        //create store data instance
        val storeData = StoreData(applicationContext)


        var switch: Boolean?
        runBlocking {
            switch = storeData.loadAlarmActive()
        }

        if (switch == true) {
            val apiCalls = ApiCalls()
            var schoolStart: LocalTime? = null
            val day = apiCalls.getNextWorkingDay()
            val loginDataNullable: Array<String?>
            val id: Int?
            var tbs: Int?
            runBlocking {
                loginDataNullable = storeData.loadLoginData()
                id = storeData.loadID()
                tbs = storeData.loadTBS()
            }
            Log.i(TAG, "getting schoolstart with id: $id")
            Log.i(TAG, "tbs is $tbs")
            if (loginDataNullable[0] != null && loginDataNullable[1] != null && loginDataNullable[2] != null && loginDataNullable[3] != null && id != null) {
                StrictMode.setThreadPolicy(policy)
                schoolStart = apiCalls.getSchoolStartForDay(
                    loginDataNullable[0]!!,
                    loginDataNullable[1]!!,
                    loginDataNullable[2]!!,
                    loginDataNullable[3]!!,
                    id!!,
                    day
                )
            } else {
                Log.i(TAG, "loaded data is null, can't get school start")
            }


            if (schoolStart == null || tbs == null || tbs!! < 0) {
                Log.i(TAG, "schoolStart and tbs may not be null")
            } else {
                scheduleAlarm(LocalDateTime.of(day, schoolStart).minusMinutes(tbs!!.toLong()))
            }
        } else {
            //switch is off
        }
    }*/
}