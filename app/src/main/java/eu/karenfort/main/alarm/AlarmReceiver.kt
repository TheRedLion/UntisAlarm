package eu.karenfort.main.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.StrictMode
import android.util.Log
import androidx.annotation.RequiresApi
import eu.karenfort.main.alarmClock.AlarmClock
import eu.karenfort.main.Misc
import eu.karenfort.main.StoreData
import eu.karenfort.main.api.UntisApiCalls
import eu.karenfort.main.helper.showAlarmNotification
import kotlinx.coroutines.runBlocking
import java.time.LocalTime


class AlarmReceiver: BroadcastReceiver() {
    private val TAG = "AlarmReceiver"
    private var policy: StrictMode.ThreadPolicy =  StrictMode.ThreadPolicy.Builder().permitAll().build()
    private val alarmRequestCode = 73295871

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG ,"called onReceive()")

        //check if we have a context
        if (context == null) {
            Log.i(TAG, "context from onReceive is null, cancelling onReceive")
            return
        }
        Log.i(TAG, "has context")

        if (!Misc.isOnline(context)) {
            Log.i(TAG, "Phone has no internet connectivity")
            return
        }
        Log.i(TAG, "phone has internet")

        val storeData = StoreData(context)
        var id: Int?
        var loginData: Array<String?>
        var tbs: Int?
        val alarmClockArray: Array<Int?>

        //TODO("move alarm receiver from main thread")
        runBlocking {
            id = storeData.loadID()
            loginData = storeData.loadLoginData()
            tbs = storeData.loadTBS()
            alarmClockArray = storeData.loadAlarmClock()
            //debug: alarmClockArray = arrayOf(6, 43)
        }
        val alarmClockHour = alarmClockArray[0]
        val alarmClockMinute = alarmClockArray[1]
        Log.i(TAG, "loaded data from StoreData")

        if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null || tbs == null) {
            //TODO: warn user that he got logged out
            return
        }
        //none of the loaded data is null

        //get schoolStart
        StrictMode.setThreadPolicy(policy)
        val untisApiCalls = UntisApiCalls(
            loginData[0]!!,
            loginData[1]!!,
            loginData[2]!!,
            loginData[3]!!
        )
        val schoolStart = untisApiCalls.getSchoolStartForDay(
            id!!,
        )

        Log.i(TAG, "Getting school start for day: ${Misc.getNextDay()}. Is $schoolStart")
        //debug: var schoolStart = LocalTime.of(7, 0)

        if (schoolStart == null) {
            //probably holiday or something
            setNew("noAlarmToday", null, context)
            return
        }
        //there is school that day

        //calculate alarmclock time
        val alarmClockTime = schoolStart.minusMinutes(tbs!!.toLong())

        if (alarmClockTime.hour == alarmClockHour && alarmClockTime.minute == alarmClockMinute) {
            //alarm clock already set properly
            Log.i(TAG, "alarm clock set properly")
            setNew("normal", alarmClockTime, context)
        } else {
            if (alarmClockHour == null || alarmClockMinute == null) {
                //no alarm clock set, setting a new one
                Log.i(TAG, "no alarm clock set, setting a new one")

                AlarmClock.setAlarm(alarmClockTime, context)
                context.showAlarmNotification()


                setNew("normal", schoolStart, context)
            } else {
                //alarm set improperly
                Log.i(TAG, "removing old and setting new alarm")

                AlarmClock.cancelAlarm(context)
                AlarmClock.setAlarm(alarmClockTime, context)
                context.showAlarmNotification()

                setNew("normal", schoolStart, context)
            }
        }
    }

    private fun setNew(reason: String, schoolStart: LocalTime?, context: Context) {
        when (reason) {
            "noAlarmToday" -> {
                val alarmManager = context.getSystemService(AlarmManager::class.java)
                val intent = Intent(context, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmRequestCode,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                Log.i(TAG, "setting new Alarm")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC,
                    System.currentTimeMillis() + 10800000,
                    pendingIntent
                )
            }

            "normal" -> {
                if (schoolStart != null) {
                    val alarmManager = context.getSystemService(AlarmManager::class.java)
                    val intent = Intent(context, AlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        alarmRequestCode,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    if (LocalTime.now().isBefore(schoolStart.minusHours(2))) {
                        Log.i(TAG, "setting new Alarm for in 15 minutes to an hour.")
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC,
                            System.currentTimeMillis() + 900000,
                            pendingIntent
                        )
                    } else {
                        Log.i(TAG, "set new Alarm for in 15 minutes")
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC,
                            System.currentTimeMillis() + 900000,
                            pendingIntent
                        )
                    }
                } else {
                    //school start was null so do error stuff
                    setNew("error", null, context)
                }
            }

            "error" -> {

            }
        }
    }
}