package com.carlkarenfort.test

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.carlkarenfort.test.alarm.AlarmItem
import com.carlkarenfort.test.alarm.AndroidAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


@SuppressLint("UseSwitchCompatOrMaterialCode")
class MainActivity : AppCompatActivity() {

    private var policy: StrictMode.ThreadPolicy =  StrictMode.ThreadPolicy.Builder().permitAll().build()
    //tag for logs
    private val TAG = "MainActivity"

    fun test() {

    }
    //objects from layout
    private lateinit var setNewUser: Button
    private lateinit var timeBeforeSchool: TextView
    private lateinit var setTBS: EditText
    private lateinit var updateTBS: Button
    private lateinit var alarmPreview: TextView
    private lateinit var toggleAlarm: Switch
    private lateinit var tempDisplay: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "creating Main Activity")

        //create channel for notifications
        val channel = NotificationChannel(
            "main_channel",
            "Webunitsalarm Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        //request permission for notifications
        Log.i(TAG, "requesting permission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }

        //create store Data object to access user Data
        val storeData = StoreData(applicationContext)

        //creating alarmitem for setting alarms
        val alarmItem = AlarmItem(845746)

        //check if user has not logged in yet
        //if so directly go to WelcomeActivity (must be at the top so the rest of the activity does not have to be built)
        //must block main thread to access a DataStore
        runBlocking {
            //check if loginData is empty
            if (storeData.loadLoginData()[0] == null || storeData.loadID() == null) {
                //go to welcome activity when not logged in
                Log.i(TAG, "Not logged in tf")
                intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                startActivity(intent)
            }
        }

        //use proper layout
        setContentView(R.layout.activity_main)

        //get objects from activity_main
        setNewUser = findViewById(R.id.addNewUser)
        timeBeforeSchool = findViewById(R.id.timeBeforeSchool)
        setTBS = findViewById(R.id.setTBS)
        updateTBS = findViewById(R.id.updateTBS)
        alarmPreview = findViewById(R.id.alarmPreview)
        toggleAlarm = findViewById(R.id.toggleAlarm)
        tempDisplay = findViewById(R.id.tempDisplay)

        //load time before school and switch
        var tbs: Int?
        var aaStateNullable: Boolean?
        var alarmClock: Array<Int?>
        var loginData: Array<String?>
        var id: Int?
        runBlocking {
            tbs = storeData.loadTBS()
            aaStateNullable = storeData.loadAlarmActive()
            alarmClock = storeData.loadAlarmClock()
            loginData = storeData.loadLoginData()
            id = storeData.loadID()
        }

        //check if tbs has not already been set
        if (tbs == null) {
            //if not put 60 as default
            timeBeforeSchool.text = "60 min"
            //store 60 min in DataStore
            CoroutineScope(Dispatchers.IO).launch {
                storeData.storeTBS(60)
            }
        } else {
            //else display loaded tbs
            timeBeforeSchool.text = tbs.toString() + " min"
        }

        //set switch
        //convert Boolean? to Boolean
        var aaState = false
        if (aaStateNullable != null) {
            aaState = aaStateNullable as Boolean
        }
        //set state of switch
        toggleAlarm.isChecked = aaState


        //display 0 when it is null
        if (alarmClock[0] == null || alarmClock[1] == null) {
            alarmClock[0] = 0
            alarmClock[1] = 0
        }

        //make 1:3 to 01:03
        val alarmClockStrHour = if (alarmClock[0]!! < 10) {
            "0${alarmClock[0]}"
        } else {
            "${alarmClock[0]}"
        }
        val alarmClockStrMinute = if (alarmClock[1]!! < 10) {
            "0${alarmClock[1]}"
        } else {
            "${alarmClock[1]}"
        }

        //set alarmPreview
        alarmPreview.text = "${alarmClockStrHour}:${alarmClockStrMinute}"

        //start alarm receiver if alarmActive is on, on new thread
        Log.i(TAG, "check if foreground service should be active")
        if (aaState) {
            val scheduler = AndroidAlarmScheduler(this)
            alarmItem.let(scheduler::schedule)
        }

        /*
        Log.i(TAG, "check if foreground service should be active")
        if (aaState) {
            CoroutineScope(Dispatchers.Default).launch {
                val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val serviceNameToCheck = "com.carlkarenfort.test"

                val runningServices = manager.runningAppProcesses

                Log.i(TAG, "check if foregroundservive is already running")
                for (processInfo in runningServices) {
                    for (serviceInfo in processInfo.pkgList) {
                        Log.i(TAG, serviceInfo)
                        if (serviceInfo == serviceNameToCheck) {
                            Log.i(TAG, "starting foreground activity")
                            Intent(applicationContext, RunningService::class.java).also {
                                it.action = RunningService.Actions.START.toString()
                                startService(it)
                            }
                        }
                    }
                }
            }
        }
         */


        //listener for going to welcome Activity
        setNewUser.setOnClickListener { _ : View? ->
            intent = Intent(this@MainActivity, WelcomeActivity::class.java)
            startActivity(intent)
        }

        //listener for updating TBS
        updateTBS.setOnClickListener { _ : View? ->
            /*
            //temp
            var policy: StrictMode.ThreadPolicy =  StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val day = LocalDate.of(2023,10,11)
            runBlocking {
                val id = storeData.loadID()
                val loginData = storeData.loadLoginData()
                val apiCalls = UntisApiCalls(loginData[0]!!, loginData[1]!!, loginData[2]!!, loginData[3]!!)
                apiCalls.timeTableTest(id!!, day)
            }
             */

            //get user inputted TBS as string
            val newTBSStr = setTBS.text.toString()

            //clear field afterwards
            setTBS.setText("")


            //convert to int
            var newTBS: Int? = null
            try {
                newTBS = Integer.parseInt(newTBSStr)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, getString(R.string.please_only_enter_integers), Toast.LENGTH_SHORT).show()
            }

            //check that newTBS was an Int
            if (newTBS != null) {
                timeBeforeSchool.text = "$newTBS min"

                //store data
                CoroutineScope(Dispatchers.IO).launch {
                    storeData.storeTBS(newTBS)
                }


                //restart alarm
                if (aaState) {
                    val scheduler = AndroidAlarmScheduler(this)


                    StrictMode.setThreadPolicy(policy)
                    val untisApiCalls = UntisApiCalls(
                        loginData[0]!!,
                        loginData[1]!!,
                        loginData[2]!!,
                        loginData[3]!!
                    )

                    val misc = Misc()

                    var schoolStart = untisApiCalls.getSchoolStartForDay(
                        id!!,
                        misc.getNextDay()
                    )
                    if (schoolStart != null) {
                        schoolStart = schoolStart.minusMinutes(tbs!!.toLong())

                        val alarmClock2 = AlarmClock()
                        alarmClock2.cancelAlarm(this)
                        alarmClock2.setAlarm(schoolStart, this)

                        alarmItem.let(scheduler::cancel)
                        alarmItem.let(scheduler::schedule)
                    } else {
                        alarmItem.let(scheduler::cancel)
                        alarmItem.let(scheduler::schedule)
                    }
                }
            }
            //clear field afterwards
            setTBS.setText("")
        }

        //create listener for switch
        toggleAlarm.setOnCheckedChangeListener { _, isChecked ->
            //store new state
            CoroutineScope(Dispatchers.IO).launch {
                storeData.storeAlarmActive(isChecked)
            }

            //start Scheduler
            Log.i(TAG, "entered listener")
            if (isChecked) {
                Log.i(TAG, "should schedule")
                val scheduler = AndroidAlarmScheduler(this)
                alarmItem.let(scheduler::schedule)
            } else {
                Log.i(TAG, "cancelling")
                val scheduler = AndroidAlarmScheduler(this)
                alarmItem.let(scheduler::cancel)
            }
        }
    }
}