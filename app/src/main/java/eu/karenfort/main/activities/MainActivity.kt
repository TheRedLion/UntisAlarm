package eu.karenfort.main.activities

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
import eu.karenfort.main.StoreData
import eu.karenfort.main.alarm.AlarmItem
import eu.karenfort.main.alarm.AndroidAlarmScheduler
import com.carlkarenfort.test.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@SuppressLint("UseSwitchCompatOrMaterialCode")
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

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

        setContentView(R.layout.activity_main)

        setNewUser = findViewById(R.id.addNewUser)
        timeBeforeSchool = findViewById(R.id.timeBeforeSchool)
        setTBS = findViewById(R.id.setTBS)
        updateTBS = findViewById(R.id.updateTBS)
        alarmPreview = findViewById(R.id.alarmPreview)
        toggleAlarm = findViewById(R.id.toggleAlarm)
        tempDisplay = findViewById(R.id.tempDisplay)

        toggleAlarm.isClickable = false

        val channel = NotificationChannel(
            "main_channel",
            "UntisAlarm Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        //Log.i(TAG, "requesting permission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.SCHEDULE_EXACT_ALARM),
                0
            )
        }

        CoroutineScope(Dispatchers.Default).launch {
            val storeData = StoreData(applicationContext)

            if (storeData.loadLoginData()[0] == null || storeData.loadID() == null) {
                //go to welcome activity when not logged in
                Log.i(TAG, "Not logged in tf")
                intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val storeData = StoreData(applicationContext)

            val tbs: Int? = storeData.loadTBS()
            val aaStateNullable: Boolean? = storeData.loadAlarmActive()
            val alarmClock: Array<Int?> = storeData.loadAlarmClock()

            if (tbs == null) {
                //60 minutes is the default value for tbs
                runOnUiThread {
                    timeBeforeSchool.text = "60 min"
                }

                storeData.storeTBS(60)
            } else {
                runOnUiThread {
                    timeBeforeSchool.text = "$tbs ${getString(R.string.short_minute)}"
                }
            }

            if (alarmClock[0] == null || alarmClock[1] == null) {
                alarmClock[0] = 0
                alarmClock[1] = 0
            }

            //display time correctly for example make 1:3 to 01:03
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

            runOnUiThread {
                alarmPreview.text = "${alarmClockStrHour}:${alarmClockStrMinute}"
            }

            var aaState = false
            if (aaStateNullable != null) {
                aaState = aaStateNullable
            }

            runOnUiThread {
                toggleAlarm.isChecked = aaState
                toggleAlarm.isClickable = true
            }
        }

        setNewUser.setOnClickListener { _ : View? ->
            intent = Intent(this@MainActivity, WelcomeActivity::class.java)
            startActivity(intent)
        }

        updateTBS.setOnClickListener { _ : View? ->
            //temp
            //val alarmClock = AlarmClock()
            //alarmClock.setAlarm(LocalTime.now(), )

            val storeData = StoreData(applicationContext)

            val alarmItem = AlarmItem(845746)

            val scheduler = AndroidAlarmScheduler(this)

            val newTBSStr = setTBS.text.toString()

            setTBS.setText("")

            val newTBS: Int
            try {
                newTBS = Integer.parseInt(newTBSStr)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, getString(R.string.please_only_enter_integers), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            timeBeforeSchool.text = "$newTBS ${getString(R.string.short_minute)}"

            storeData.storeTBS(newTBS)

            CoroutineScope(Dispatchers.Default).launch {
                val aaStateNullable: Boolean? = storeData.loadAlarmActive()
                var aaState = false
                if (aaStateNullable != null) {
                    aaState = aaStateNullable
                }
                if (aaState) {
                    alarmItem.let(scheduler::cancel)
                    alarmItem.let(scheduler::schedule)
                }
            }

            setTBS.setText("")
        }

        //create listener for switch
        toggleAlarm.setOnCheckedChangeListener { _, isChecked ->
            val storeData = StoreData(applicationContext)

            val alarmItem = AlarmItem(845746)

            val scheduler = AndroidAlarmScheduler(this)

            storeData.storeAlarmActive(isChecked)

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