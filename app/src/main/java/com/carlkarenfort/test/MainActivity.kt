package com.carlkarenfort.test

import android.content.Context
import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setNewUser = findViewById(R.id.addNewUser);
        timeBeforeSchool = findViewById(R.id.timeBeforeSchool);
        setTBS = findViewById(R.id.setTBS);
        updateTBS = findViewById(R.id.updateTBS);
        alarmPreview = findViewById(R.id.alarmPreview);
        toggleAlarm = findViewById(R.id.toggleAlarm);
        tempDisplay = findViewById(R.id.tempDisplay);

        setNewUser.setOnClickListener { v: View? ->
            intent = Intent(this@MainActivity, WelcomeActivity::class.java)
            startActivity(intent)
        }

        updateTBS.setOnClickListener { v: View? ->
            //temp logging

            //when clicked
            Log.i(TAG, "updating TBS")
            var storeData = StoreData(applicationContext)
            val loginDataArr: Array<String?>
            runBlocking {
                loginDataArr = storeData.loadLoginData()
            }

            Log.i(TAG, loginDataArr[0].toString())
            Log.i(TAG, loginDataArr[1].toString())
            Log.i(TAG, loginDataArr[2].toString())
            Log.i(TAG, loginDataArr[3].toString())
            //get new TBS
            //something should be what is currently entered in TBS field, also clear field afterwards
            val newTBS = 60 //temp value

            //update displayedTBS

            //update alarm preview and alarm tomorrow

            //store data
        }
    }
}