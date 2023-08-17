package com.carlkarenfort.test

import android.annotation.SuppressLint
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
import kotlinx.coroutines.runBlocking


@SuppressLint("UseSwitchCompatOrMaterialCode")
class MainActivity : AppCompatActivity() {
    private var TAG = "MainActivity"
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
        //use proper layout
        setContentView(R.layout.activity_main)

        setNewUser = findViewById(R.id.addNewUser)
        timeBeforeSchool = findViewById(R.id.timeBeforeSchool)
        setTBS = findViewById(R.id.setTBS)
        updateTBS = findViewById(R.id.updateTBS)
        alarmPreview = findViewById(R.id.alarmPreview)
        toggleAlarm = findViewById(R.id.toggleAlarm)
        tempDisplay = findViewById(R.id.tempDisplay)

        val storeData = StoreData(applicationContext)

        //check if user has logged in, if so directly go to WelcomeActivity (at the top so the rest of the activity does not have to be built)
        runBlocking {
            if (storeData.loadLoginData()[0] == null) {
                //go to welcome activity when not logged in
                Log.i(TAG, "Not logged in tf")
                intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                startActivity(intent)
            }
        }


        //set TBS preview
        var tbs: Int?
        runBlocking {
            tbs = storeData.loadTBS()
        }

        //check if tbs was already set
        if (tbs == null) {
            //if not put 60 as default
            timeBeforeSchool.text = "60 min"
            runBlocking {
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
            //get new TBS as string
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
            runBlocking {
                storeData.storeTBS(newTBS)
            }

            //clear field afterwards
            setTBS.setText("")

            //temp code
            /*
                val scheduler = AndroidAlarmScheduler(this)
                var alarmItem: AlarmItem? = null
                alarmItem = AlarmItem(
                    id = 1,
                    time = LocalDateTime.of(2023,8,10,1,25)
                )
                alarmItem?.let(scheduler::schedule)
                */
            /*
            var intent = Intent(AlarmClock.ACTION_SET_ALARM)
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            intent.putExtra(AlarmClock.EXTRA_HOUR, 14)
            intent.putExtra(AlarmClock.EXTRA_MINUTES, 22)
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, "yoooo")
            startActivity(intent)
             */

        }

        // TODO: set alarm when button isn't clicked
        //set switch to proper state
        var aaStateNullable: Boolean?
        var aaState = false
        runBlocking {
            aaStateNullable = storeData.loadAlarmActive()
        }
        if (aaStateNullable != null) {
            aaState = aaStateNullable as Boolean
        }

        toggleAlarm.isChecked = aaState

        //create listener for switch
        toggleAlarm.setOnCheckedChangeListener { _, isChecked ->
            //store new state
            runBlocking {
                storeData.storeAlarmActive(isChecked)
            }
            //update alarm preview and alarm tomorrow
        }
    }
}