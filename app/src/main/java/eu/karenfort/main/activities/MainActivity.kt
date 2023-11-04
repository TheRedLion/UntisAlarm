package eu.karenfort.main.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.carlkarenfort.test.R
import com.google.android.material.color.DynamicColors
import com.google.android.material.materialswitch.MaterialSwitch
import eu.karenfort.main.StoreData
import eu.karenfort.main.alarm.AlarmItem
import eu.karenfort.main.alarm.AndroidAlarmScheduler
import eu.karenfort.main.helper.TBS_DEFAULT
import eu.karenfort.main.helper.areNotificationsEnabled
import eu.karenfort.main.helper.isTiramisuPlus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var alarmPreview: TextView
    private lateinit var toggleAlarm: MaterialSwitch
    private lateinit var editAlarmToday: ImageView
    private lateinit var tbsPreview: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DynamicColors.applyToActivitiesIfAvailable(application) //todo:check if necessary

        getLayoutObjectsByID()

        disableClicking() //disabling clicks until everything was properly loaded

        createNotificationChannel()

        requestNotificationPermission()

        sendToWelcomeActivity()

        CoroutineScope(Dispatchers.IO).launch {
            loadAndDisplayTBS()
            loadAndDisplayAlarmClockPreview()
            loadAndSetAlarmActive()
        }

        setListener()
    }

    private fun getLayoutObjectsByID() {
        alarmPreview = findViewById(R.id.alarmPreview)
        toggleAlarm = findViewById(R.id.toggleAlarm)
        editAlarmToday = findViewById(R.id.edit_alarm_tomorrow)
        tbsPreview = findViewById(R.id.current_tbs)
    }

    private fun setListener() {
        toggleAlarm.setOnCheckedChangeListener { _, isChecked -> handleToggleAlarm(isChecked) }
        editAlarmToday.setOnClickListener {
            //todo: implement action
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "main_channel",
            "UntisAlarm Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun disableClicking() {
        toggleAlarm.isClickable = false
        editAlarmToday.isClickable = false
    }

    private fun handleToggleAlarm(isChecked: Boolean) {
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


    private fun requestNotificationPermission() {
        if (!isTiramisuPlus()) return
        if (!areNotificationsEnabled()) return

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.SCHEDULE_EXACT_ALARM
            ),
            0
        )
    }

    private suspend fun loadAndSetAlarmActive() {
        val storeData = StoreData(applicationContext)
        val aaState: Boolean = storeData.loadAlarmActive() ?: false
        runOnUiThread {
            toggleAlarm.isChecked = aaState
            toggleAlarm.isClickable = true
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun loadAndDisplayAlarmClockPreview() {
        val storeData = StoreData(applicationContext)
        val alarmClock: Array<Int?> = storeData.loadAlarmClock()
        val (alarmClockStrHour, alarmClockStrMinute) = reformatAlarmClockPreview(alarmClock)
        runOnUiThread {
            alarmPreview.text = "${alarmClockStrHour}:${alarmClockStrMinute}"
            editAlarmToday.isClickable = true
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun loadAndDisplayTBS() {
        val storeData = StoreData(applicationContext)
        val tbs: Int? = storeData.loadTBS()
        if (tbs == null) {
            runOnUiThread {
                tbsPreview.text = getString(R.string.error_loading_tbs)
            }
            storeData.storeTBS(TBS_DEFAULT)
        } else {
            runOnUiThread {
                tbsPreview.text = "${getString(R.string.the_alarm_currently_goes_off)}$tbs${getString(R.string.minutes_n_before_your_first_lesson)}"
            } //todo: check if looks right
        }
    }

    private fun reformatAlarmClockPreview(alarmClock: Array<Int?>): Pair<String, String> {
        if (alarmClock[0] == null || alarmClock[1] == null) {
            alarmClock[0] = 0
            alarmClock[1] = 0
        }
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
        return Pair(alarmClockStrHour, alarmClockStrMinute)
    }

    private suspend fun hasLoggedIn() : Boolean {
        val storeData = StoreData(applicationContext)
        return storeData.loadLoginData()[0] == null || storeData.loadID() == null
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
                sendToWelcomeActivity()
            }

            R.id.about_us -> {
                val uri = Uri.parse("https://github.com/TheRedLion/UntisAlarm")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendToWelcomeActivity() {
        CoroutineScope(Dispatchers.IO).launch {
            if (!hasLoggedIn()) return@launch
            intent = Intent(this@MainActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}