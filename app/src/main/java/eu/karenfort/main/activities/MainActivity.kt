package eu.karenfort.main.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.datastore.preferences.core.intPreferencesKey
import com.carlkarenfort.test.R
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import eu.karenfort.main.StoreData
import eu.karenfort.main.alarm.AlarmScheduler
import eu.karenfort.main.alarmClock.AlarmClock
import eu.karenfort.main.helper.TBS_DEFAULT
import eu.karenfort.main.helper.areNotificationsEnabled
import eu.karenfort.main.helper.getNextDay
import eu.karenfort.main.helper.isTiramisuPlus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val TAG = "MainActivity"
    private lateinit var alarmPreview: TextView
    private lateinit var toggleAlarm: MaterialSwitch
    private lateinit var editAlarmToday: ImageView
    private lateinit var tbsPreview: TextView
    private lateinit var notifsDisabledTextView: TextView
    private val alarmClockHourKey = intPreferencesKey("alarmClockHour")
    private val alarmClockMinuteKey = intPreferencesKey("alarmClockMinute")

    companion object {
        var active = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        updateNotifsDisabledWarning()
    }


    override fun onStart() {
        super.onStart()
        active = true
    }

    override fun onStop() {
        super.onStop()
        active = false
    }

    @SuppressLint("SetTextI18n")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, string: String?) {
        if (string == null) return //dont know when this would happen
        if (!isAlarmClockTime(string)) return
        CoroutineScope(Dispatchers.IO).launch {
            val (alarmClockDateTime, edited) = StoreData(applicationContext).loadAlarmClock()
            if (alarmClockDateTime == null) {
                alarmPreview.text = ""
            } else {
                alarmPreview.text = "${alarmClockDateTime.hour}:${alarmClockDateTime.minute}"
            }
        }
    }

    private fun isAlarmClockTime(string: String) =
        string.equals(alarmClockHourKey) || string.equals(alarmClockMinuteKey)


    override fun onResume() {
        super.onResume()
        updateNotifsDisabledWarning()
        CoroutineScope(Dispatchers.IO).launch {
            loadAndDisplayAlarmClockPreview()
        }
    }

    private fun updateNotifsDisabledWarning() {
        if (areNotificationsEnabled()) {
            notifsDisabledTextView.visibility = INVISIBLE
        } else {
            notifsDisabledTextView.visibility = VISIBLE
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.i(TAG, "intent received")
        if (intent == null) {
            Log.i(TAG, "intent was null")
            return
        }
        val extras = intent.extras
        if (extras == null) {
            Log.i(TAG, "extras was null")
            return
        }
        val newAlarmClockTime = extras.getString("newAlarmClockTime")
        alarmPreview.text = newAlarmClockTime

        when (extras.getString("areNotifsAllowed")) {
            "true" -> notifsDisabledTextView.visibility = INVISIBLE
            "false" -> notifsDisabledTextView.visibility = VISIBLE
        }
    }

    private fun getLayoutObjectsByID() {
        alarmPreview = findViewById(R.id.alarmPreview)
        toggleAlarm = findViewById(R.id.toggleAlarm)
        editAlarmToday = findViewById(R.id.edit_alarm_tomorrow)
        tbsPreview = findViewById(R.id.current_tbs)
        notifsDisabledTextView = findViewById(R.id.disabled_notfs)
    }

    private fun setListener() {
        toggleAlarm.setOnCheckedChangeListener { _, isChecked -> handleToggleAlarm(isChecked) }
        editAlarmToday.setOnClickListener {
            val isSystem24Hour = is24HourFormat(this)
            val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
            val time = alarmPreview.text.split(":")
            var hour: Int
            var minute: Int
            try {
                hour = time[0].toInt()
                minute = time[1].toInt()
            } catch (e: NumberFormatException) {
                hour = 0
                minute = 0
            }
            val picker =
            MaterialTimePicker.Builder()
                    .setTimeFormat(clockFormat)
                    .setHour(hour)
                    .setMinute(minute)
                    .setTitleText("Edit Alarm Time")
                    .setInputMode(INPUT_MODE_CLOCK)
                    .build()

            picker.show(supportFragmentManager, TAG)

            picker.addOnPositiveButtonClickListener {
                AlarmClock.cancelAlarm(this)
                AlarmClock.setAlarm(LocalDateTime.of(getNextDay(), LocalTime.of(picker.hour, picker.minute)), this)
                StoreData(this).storeAlarmClock(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(picker.hour, picker.minute)), true)
                picker.dismiss()
            }
            picker.addOnNegativeButtonClickListener {
                picker.dismiss()
            }
            picker.addOnCancelListener {
                picker.dismiss()
            }
            picker.addOnDismissListener {
                picker.dismiss()
            }
        }
    }

    private fun createNotificationChannel() {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(NotificationChannel(
            "main_channel",
            "UntisAlarm Notifications", //todo: make notificcation channels right (view system settings/UntisWecker/notifs)
            NotificationManager.IMPORTANCE_DEFAULT
        ))
    }

    private fun disableClicking() {
        toggleAlarm.isClickable = false
        editAlarmToday.isClickable = false
    }

    @SuppressLint("SetTextI18n")
    private fun handleToggleAlarm(isChecked: Boolean) {
        StoreData(applicationContext).storeAlarmActive(isChecked)

        Log.i(TAG, "entered listener")
        if (isChecked) {
            Log.i(TAG, "should schedule")
            AlarmScheduler(this).schedule()
        } else {
            // TODO: Make sure the alarm clock is properly cancelled
            alarmPreview.text = "00:00"
            Log.i(TAG, "cancelling")
            AlarmScheduler(this).cancel()
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
        val (alarmClockDateTime, edited) = storeData.loadAlarmClock()
        val (alarmClockStrHour, alarmClockStrMinute) = reformatAlarmClockPreview(alarmClockDateTime)
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
            }
        }
    }

    private fun reformatAlarmClockPreview(alarmClock: LocalDateTime?): Pair<String, String> {
        if (alarmClock == null) {
            return Pair("00","00")
        }
        val alarmClockStrHour = if (alarmClock.hour < 10) {
            "0${alarmClock.hour}"
        } else {
            "${alarmClock.hour}"
        }
        val alarmClockStrMinute = if (alarmClock.minute < 10) {
            "0${alarmClock.minute}"
        } else {
            "${alarmClock.minute}"
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
                Log.i(TAG, "should go to welcomeActivity")
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }

            R.id.logout -> {
                startActivity(Intent(this@MainActivity, WelcomeActivity::class.java))
                deleteLoginData()
            }

            R.id.about_us -> {
                val uri = Uri.parse("https://github.com/TheRedLion/UntisAlarm")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteLoginData() {
        val storeData = StoreData(this)
        storeData.storeID(0)
        storeData.storeLoginData("", "", "", "")
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