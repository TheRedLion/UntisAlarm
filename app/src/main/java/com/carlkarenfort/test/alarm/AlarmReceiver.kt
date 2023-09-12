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
    private val TAG = "AlarmReciever"
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG ,"called onRecieve()")

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
            val calendar = Calendar.getInstance()
            if (context != null) {
                clock.setAlarm(
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE) + 1,
                    context
                )
                Log.i("receiver", "I even reached this point but  no alarm set?")

            } else {
                Log.w("receiver", "couldnt start alarm due to missing context")
            }
            Log.i("receiver", "I even reached this point but  no alarm set? 2")
        }

    }
}