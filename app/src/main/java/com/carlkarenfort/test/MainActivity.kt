package com.carlkarenfort.test

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking


class MainActivity : AppCompatActivity() {
    var TAG = "MainActivity"
    lateinit var setNewUser: Button
    lateinit var timeBeforeSchool: TextView
    lateinit var setTBS: EditText
    lateinit var updateTBS: Button
    lateinit var alarmPreview: TextView
    lateinit var toggleAlarm: Switch
    lateinit var tempDisplay: TextView
    lateinit var context: Context
    lateinit var updateTBSwarning: TextView

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
        updateTBSwarning = findViewById(R.id.updateTBSwarning)

        var storeData = StoreData(applicationContext)


        //listener for going to welcome Activity
        setNewUser.setOnClickListener { v: View? ->
            intent = Intent(this@MainActivity, WelcomeActivity::class.java)
            startActivity(intent)
        }

        //set TBS preview
        var tbs: Int?
        runBlocking {
            tbs = storeData.loadTBS()
        }
        timeBeforeSchool.setText(tbs.toString() + "min")

        //listener for updating TBS
        updateTBS.setOnClickListener { v: View? ->
            //get new TBS as string
            val newTBSStr = setTBS.text.toString()
            //convert to int
            var newTBS = 0
            try {
                newTBS = Integer.parseInt(newTBSStr)
            } catch (e: NumberFormatException) {
                updateTBSwarning.setTextColor(Color.rgb(244, 67, 54))
                updateTBSwarning.text = "Please only enter valid numbers"
            }
            //update displayedTBS
            timeBeforeSchool.setText(newTBS.toString() + "min")

            //store data
            runBlocking {
                storeData.storeTBS(newTBS)
            }

            //clear field afterwards
            setTBS.setText("")

            //update alarm preview and alarm tomorrow
        }

        //set switch to proper state
        var aaStateNullable: Boolean?
        var aaState = false
        runBlocking {
            aaStateNullable = storeData.loadAlarmActive()
        }
        if (aaStateNullable == null) {
            aaState = false
        } else {
            aaState = aaStateNullable as Boolean
        }

        toggleAlarm.isChecked = aaState

        //create listener for switch
        toggleAlarm.setOnCheckedChangeListener { _, isChecked ->
            //store new state
            runBlocking {
                storeData.storeAlarmAcitive(isChecked)
            }
            //update alarm preview and alarm tomorrow
        }
    }
}