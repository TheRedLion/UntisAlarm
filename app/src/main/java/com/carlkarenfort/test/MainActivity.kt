package com.carlkarenfort.test

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
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

    //tag for logs
    private val TAG = "MainActivity"

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


        //make sure button is not clicked until properly set
        toggleAlarm.isClickable = false


        //create channel for notifications
        val channel = NotificationChannel(
            "main_channel",
            "UntisAlarm Notifications",
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
                arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.SCHEDULE_EXACT_ALARM),
                0
            )
        }

        //create store Data object to access user Data
        val storeData = StoreData(applicationContext)

        //creating alarm item for setting alarms
        val alarmItem = AlarmItem(845746)

        //check if user has not logged in yet
        //if so directly go to WelcomeActivity
        CoroutineScope(Dispatchers.Default).launch {
            //check if loginData is empty
            if (storeData.loadLoginData()[0] == null || storeData.loadID() == null) {
                //go to welcome activity when not logged in
                Log.i(TAG, "Not logged in tf")
                intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        //load all text on UI
        CoroutineScope(Dispatchers.IO).launch {

            //load from storedata
            val tbs: Int? = storeData.loadTBS()
            val aaStateNullable: Boolean? = storeData.loadAlarmActive()
            val alarmClock: Array<Int?> = storeData.loadAlarmClock()

            //check if tbs has not already been set
            if (tbs == null) {
                runOnUiThread {
                    //if not put 60 as default
                    timeBeforeSchool.text = "60 min"
                }

                //store 60 min in DataStore
                CoroutineScope(Dispatchers.IO).launch {
                    storeData.storeTBS(60)
                }
            } else {
                //else display loaded tbs
                runOnUiThread {
                    timeBeforeSchool.text = tbs.toString() + " min"
                }
            }

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
            runOnUiThread {
                alarmPreview.text = "${alarmClockStrHour}:${alarmClockStrMinute}"
            }

            //set switch
            //convert Boolean? to Boolean
            var aaState = false
            if (aaStateNullable != null) {
                aaState = aaStateNullable
            }

            //set state of switch and make clickable
            runOnUiThread {
                toggleAlarm.isChecked = aaState
                toggleAlarm.isClickable = true
            }
        }

        //listener for going to welcome Activity
        setNewUser.setOnClickListener { _ : View? ->
            intent = Intent(this@MainActivity, WelcomeActivity::class.java)
            startActivity(intent)
        }

        //listener for updating TBS
        updateTBS.setOnClickListener { _ : View? ->
            //alarm scheduler
            val scheduler = AndroidAlarmScheduler(this)

            //get user inputted TBS as string
            val newTBSStr = setTBS.text.toString()

            //clear field afterwards
            setTBS.setText("")


            //convert to int
            val newTBS: Int
            try {
                newTBS = Integer.parseInt(newTBSStr)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, getString(R.string.please_only_enter_integers), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            timeBeforeSchool.text = "$newTBS min"

            //store data
            CoroutineScope(Dispatchers.IO).launch {
                storeData.storeTBS(newTBS)
            }


            //restart alarm
            CoroutineScope(Dispatchers.Default).launch {
                val aaStateNullable: Boolean? = storeData.loadAlarmActive()
                var aaState = false
                if (aaStateNullable != null) {
                    aaState = aaStateNullable as Boolean
                }
                if (aaState) {
                    alarmItem.let(scheduler::cancel)
                    alarmItem.let(scheduler::schedule)
                }
            }
            //clear field afterwards
            setTBS.setText("")
        }

        //create listener for switch
        toggleAlarm.setOnCheckedChangeListener { _, isChecked ->
            //alarm scheduler
            val scheduler = AndroidAlarmScheduler(this)

            //store new state
            CoroutineScope(Dispatchers.IO).launch {
                storeData.storeAlarmActive(isChecked)
            }

            //start Scheduler
            Log.i(TAG, "entered listener")
            if (isChecked) {
                Log.i(TAG, "should schedule")
                alarmItem.let(scheduler::schedule)
            } else {
                Log.i(TAG, "cancelling")
                alarmItem.let(scheduler::cancel)
            }
        }
    }
}