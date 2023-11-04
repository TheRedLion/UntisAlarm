package eu.karenfort.main.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.carlkarenfort.test.R
import com.google.android.material.color.DynamicColors
import com.google.android.material.materialswitch.MaterialSwitch
import eu.karenfort.main.StoreData
import eu.karenfort.main.alarm.AlarmItem
import eu.karenfort.main.alarm.AndroidAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@SuppressLint("UseSwitchCompatOrMaterialCode")
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var alarmPreview: TextView
    private lateinit var toggleAlarm: MaterialSwitch

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivitiesIfAvailable(application);
        Log.i(TAG, "creating Main Activity")

        setContentView(R.layout.activity_main)

        alarmPreview = findViewById(R.id.alarmPreview)
        toggleAlarm = findViewById(R.id.toggleAlarm)

        toggleAlarm.isClickable = false

        val channel = NotificationChannel(
            "main_channel",
            "UntisAlarm Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        //Log.i(TAG, "requesting permission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.SCHEDULE_EXACT_ALARM
                ),
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
                    //timeBeforeSchool.text = "60 min"
                }

                storeData.storeTBS(60)
            } else {
                runOnUiThread {
                    //timeBeforeSchool.text = "$tbs ${getString(R.string.short_minute)}"
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
                alarmPreview.text = getString(R.string.no_alarm_tomorrow)
                Log.i(TAG, "cancelling")
                alarmItem.let(scheduler::cancel)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.top_right_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
            }

            R.id.logout -> {
                intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.about_us -> {
                val uri = Uri.parse("https://github.com/TheRedLion/UntisAlarm")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}