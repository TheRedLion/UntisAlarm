package com.carlkarenfort.test.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.carlkarenfort.test.AlarmClock
import com.carlkarenfort.test.Misc
import com.carlkarenfort.test.RunningService
import com.carlkarenfort.test.StoreData
import com.carlkarenfort.test.UntisApiCalls
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
import java.util.Calendar


class AlarmReceiver: BroadcastReceiver() {
    private val TAG = "AlarmReceiver"
    private var policy: StrictMode.ThreadPolicy =  StrictMode.ThreadPolicy.Builder().permitAll().build()
    private val alarmRequestCode = 73295871

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG ,"called onReceive()")

        //check if we have a context
        if (context == null) {
            Log.i(TAG, "context from onReceieve is null")
            //TODO("not sure but might happen when foreground Activity has been stopped")
        } else {
            Log.i(TAG, "has context")

            val misc = Misc()

            //check if phone has connectivity
            if (!misc.isOnline(context)) {
                Log.i(TAG, "Phone has no internet connectivity")
            } else {
                //phone has connectivity:
                Log.i(TAG, "phone has internet")

                //load relevant data from storeData
                val storeData = StoreData(context)
                var id: Int?
                var loginData: Array<String?>
                var tbs: Int?
                val alarmClockArray: Array<Int?>
                //TODO("move alarm receiever from main thread")
                runBlocking {
                    id = storeData.loadID()
                    loginData = storeData.loadLoginData()
                    tbs = storeData.loadTBS()
                    alarmClockArray = storeData.loadAlarmClock()
                    //debug: alarmClockArray = arrayOf(6, 43)
                }
                val alarmClockHour = alarmClockArray[0]
                val alarmClockMinute = alarmClockArray[1]

                //check if any of the loaded data is null
                if (id == null || loginData[0] == null || loginData[1] == null || loginData[2] == null || loginData[3] == null || tbs == null) {
                    Log.i(TAG, "id, loginData or TBS from StoreData was null. THIS SHOULD NEVER HAPPEN")
                    //TODO add notification if user was logged out
                //warn user that he got logged out
                    /*if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        //create notification
                        val notification = NotificationCompat.Builder(context, "main_channel")
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("Login again")
                            .setContentText("Your Login Data is invalid, please login Again.")
                            .build()

                    }*/

                } else {
                    //none of the loaded data is null

                    //get schoolStart
                    StrictMode.setThreadPolicy(policy)
                    val untisApiCalls = UntisApiCalls(
                        loginData[0]!!,
                        loginData[1]!!,
                        loginData[2]!!,
                        loginData[3]!!
                    )
                    var schoolStart = untisApiCalls.getSchoolStartForDay(
                        id!!,
                        misc.getNextDay()
                    )

                    Log.i(TAG, "Getting Schoolstart for day: ${misc.getNextDay()}. Is ${schoolStart.toString()}")
                    //debug: var schoolStart = LocalTime.of(7, 0)

                    if (schoolStart == null) {
                        //probably holliday or something
                        setNew("noAlarmToday", null, context)
                    } else {
                        schoolStart = schoolStart.minusMinutes(tbs!!.toLong())

                        if (schoolStart.hour == alarmClockHour && schoolStart.minute == alarmClockMinute) {
                            //alarm clock already set properly
                            Log.i(TAG, "alarm clock set properly")
                            setNew("normal", schoolStart, context)
                        } else if (alarmClockHour == null || alarmClockMinute == null) {
                            //no alarm clock set, setting a new one
                            Log.i(TAG, "no alarm clock set, setting a new one")
                            val intent2 = Intent(context, AlarmReceiver::class.java)
                            intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            val pendingIntent = PendingIntent.getBroadcast(context, 123, intent2,
                                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                            var alarmMgr: AlarmManager? = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            val calendar: Calendar = Calendar.getInstance().apply {
                                timeInMillis = System.currentTimeMillis()
                                set(Calendar.HOUR_OF_DAY, 8)
                                set(Calendar.MINUTE, 30)
                            }
                            alarmMgr?.setAlarmClock(AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent), pendingIntent)
                            // TODO: FÜR MORGEN CODE AUFRÄUMEN ALARMCLOCK IST EIG UNNÖTIG WIR KÖNNEN NATÖRLCI HDIE OBEREN 11 ZEILEN DA REIN SCHREIBEN. DAS KLAPPT NÄMLICH. VLLT
                            // GEHT DAS AUCH IRGENDWIE CLEANER ABER SOLANGE ES KLAPPT.
                            // HABEN UM ZU TESTEN VIELES GELÖSCHT WAS WIEDER HINMUSS
                            // RUNNING APP IST UNNÖÄTIG RUNNING SERVICE IST UNNÖTIG
                            // AHH SO EINE ZEIT VERSXCHWEQRNUGN
                        } else {
                            //alarm set improperly
                            Log.i(TAG, "removing old and setting new alarm")
                            val clock = AlarmClock()
                            CoroutineScope(Dispatchers.IO).launch {
                                clock.cancelAlarm(context)
                                clock.setAlarm(schoolStart.hour, schoolStart.minute, context)
                            }

                            //reset view in Main Activity
                            //val intent = Intent(context, MainActivity::class.java)
                            //context.startActivity(intent)
                        }
                    }
                }
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
                    //TODO("school start for normal returned normal, should not happen")
                }
            }

            "error" -> {

            }
        }
    }
}