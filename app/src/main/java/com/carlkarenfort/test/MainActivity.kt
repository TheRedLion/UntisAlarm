package com.carlkarenfort.test

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


@SuppressLint("UseSwitchCompatOrMaterialCode")
class MainActivity : AppCompatActivity() {
    //tag for logs
    private var TAG = "MainActivity"

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

        //create channel for foregroundactivity notification
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
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            0
        )

        //create store Data object to access user Data
        val storeData = StoreData(applicationContext)


        //check if user has not logged in yet
        //if so directly go to WelcomeActivity (must be at the top so the rest of the activity does not have to be built)
        //must block main thread to access a DataStore
        runBlocking {
            //check if loginData is empty
            if (storeData.loadLoginData()[0] == null) {
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

        //load time before school must stop main thread since it accesses DataStore
        var tbs: Int?
        runBlocking {
            tbs = storeData.loadTBS()
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

        //listener for going to welcome Activity
        setNewUser.setOnClickListener { _ : View? ->
            intent = Intent(this@MainActivity, WelcomeActivity::class.java)
            startActivity(intent)
        }

        //listener for updating TBS
        updateTBS.setOnClickListener { _ : View? ->
            //get user inputted TBS as string
            val newTBSStr = setTBS.text.toString()
            //convert to int
            var newTBS = 0
            try {
                newTBS = Integer.parseInt(newTBSStr)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, getString(R.string.please_only_enter_intergers), Toast.LENGTH_SHORT).show()
            }
            //update displayedTBS
            timeBeforeSchool.text = "$newTBS min"
            //store data
            CoroutineScope(Dispatchers.IO).launch {
                storeData.storeTBS(newTBS)
            }

            //clear field afterwards
            setTBS.setText("")
        }
        // TODO: set alarm when button isn't clicked

        //initialize values
        var aaStateNullable: Boolean?
        var aaState = false
        //load stored state of switch
        runBlocking {
            aaStateNullable = storeData.loadAlarmActive()
        }
        //convert Boolean? to Boolean
        if (aaStateNullable != null) {
            aaState = aaStateNullable as Boolean
        }

        toggleAlarm.isChecked = aaState

        //create listener for switch
        toggleAlarm.setOnCheckedChangeListener { _, isChecked ->
            //store new state
            CoroutineScope(Dispatchers.IO).launch {
                storeData.storeAlarmActive(isChecked)
            }

            //start foreground activity
            if (isChecked) {
                Log.i(TAG, "starting foreground activity")
                Intent(applicationContext, RunningService::class.java).also {
                    it.action = RunningService.Actions.START.toString()
                    startService(it)
                }
            } else {
                Log.i(TAG, "stopping foreground activity")
                Intent(applicationContext, RunningService::class.java).also {
                    it.action = RunningService.Actions.STOP.toString()
                    startService(it)
                }
            }
        }
    }
}