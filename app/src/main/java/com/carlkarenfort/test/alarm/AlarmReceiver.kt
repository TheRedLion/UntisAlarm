package com.carlkarenfort.test.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.carlkarenfort.test.AlarmClock
import com.carlkarenfort.test.ApiCalls
import com.carlkarenfort.test.StoreData
import com.google.android.material.tabs.TabLayout.TabGravity
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
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
            val schoolStart = apiCalls.getSchoolStartForDay(loginData[0]!!, loginData[1]!!, loginData[2]!!, loginData[3]!!, id!!, LocalDate.now())

            val clock = AlarmClock()
            if (schoolStart != null) {
                clock.setAlarm(
                    schoolStart.hour,
                    schoolStart.minute,
                    context
                )
                Log.i(TAG, "set Alarm")
            } else {
                Log.i(TAG, "school start returned null")
            }
        }
    }
}