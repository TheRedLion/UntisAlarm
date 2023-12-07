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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.carlkarenfort.test.R
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import eu.karenfort.main.StoreData
import eu.karenfort.main.alarm.AlarmManager
import eu.karenfort.main.alarm.AlarmScheduler
import eu.karenfort.main.alarmClock.AlarmClock
import eu.karenfort.main.helper.ALARM_CLOCK_NOTIFICATION_CHANNEL_ID
import eu.karenfort.main.helper.INFO_NOTIFICARION_CHANNEL_ID
import eu.karenfort.main.helper.areNotificationsEnabled
import eu.karenfort.main.helper.isTiramisuPlus
import eu.karenfort.main.helper.reformatAlarmClockPreview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val TAG = "MainActivity"
    private lateinit var alarmPreview: TextView
    private lateinit var toggleAlarm: MaterialSwitch
    private lateinit var editAlarmToday: ImageView
    private lateinit var notifsDisabledTextView: TextView
    private val alarmClockYearKey = intPreferencesKey("alarmClockYear")
    private val alarmClockMonthKey = intPreferencesKey("alarmClockMonth")
    private val alarmClockDayKey = intPreferencesKey("alarmClockDay")
    private val alarmClockHourKey = intPreferencesKey("alarmClockHour")
    private val alarmClockMinuteKey = intPreferencesKey("alarmClockMinute")
    private val alarmClockEditedKey = booleanPreferencesKey("alarmClockEdited")
    private var currentAlarmClockDateTime: LocalDateTime? = null

    companion object {
        var active = false
    }

    //todo add pm am
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "in onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getLayoutObjectsByID()
        disableClicking() //disabling clicks until everything was properly loaded to stop errors
        createNotificationChannel()
        requestNotificationPermission()
        sendToWelcomeActivity()
        CoroutineScope(Dispatchers.IO).launch {
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
            val (alarmClockDateTime, _) = StoreData(applicationContext).loadAlarmClock()
            if (alarmClockDateTime == null) {
                alarmPreview.text = getString(R.string.error)
            } else {
                alarmPreview.text = "${alarmClockDateTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${alarmClockDateTime.hour}:${alarmClockDateTime.minute}"
                currentAlarmClockDateTime = alarmClockDateTime
            }
        }
    }

    private fun isAlarmClockTime(string: String) =
        string.equals(alarmClockHourKey) || string.equals(alarmClockMinuteKey) || string.equals(alarmClockEditedKey) || string.equals(alarmClockYearKey) || string.equals(alarmClockMonthKey) || string.equals(alarmClockDayKey)


    override fun onResume() {
        Log.i(TAG, "resuming")
        super.onResume()
        updateNotifsDisabledWarning()
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
            "true" -> {
                notifsDisabledTextView.visibility = INVISIBLE
            }
            "false" -> {
                requestNotificationPermission()
                notifsDisabledTextView.visibility = VISIBLE
            }
        }
    }

    private fun getLayoutObjectsByID() {
        alarmPreview = findViewById(R.id.alarmPreview)
        toggleAlarm = findViewById(R.id.toggleAlarm)
        editAlarmToday = findViewById(R.id.edit_alarm_tomorrow)
        notifsDisabledTextView = findViewById(R.id.disabled_notfs)
    }

    private fun setListener() {
        toggleAlarm.setOnCheckedChangeListener { _, isChecked -> handleToggleAlarm(isChecked) }
        editAlarmToday.setOnClickListener { handleEditAlarmToday() }
        notifsDisabledTextView.setOnClickListener {
            val intent = Intent()
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("android.provider.extra.APP_PACKAGE", packageName);
            startActivity(intent);
        }
    }

    private fun handleEditAlarmToday() {
        //todo does not work, just resets itself
        val clockFormat = if (is24HourFormat(this)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        val hour = currentAlarmClockDateTime?.hour ?: LocalDateTime.now().hour
        val minute = currentAlarmClockDateTime?.minute ?: LocalDateTime.now().minute

        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Edit Alarm Time")
                .setInputMode(INPUT_MODE_CLOCK)
                .build()

        timePicker.show(supportFragmentManager, TAG)

        timePicker.addOnPositiveButtonClickListener {
            timePicker.dismiss()

            val startDate = getStartDate()

            // Build constraints.
            val constraintsBuilder =
                CalendarConstraints.Builder()
                    .setStart(startDate)

            val selectedDateMilli = currentAlarmClockDateTime?.toInstant(
                ZoneOffset.UTC
            )?.toEpochMilli()
                ?: MaterialDatePicker.todayInUtcMilliseconds()

            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(selectedDateMilli)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build()
            datePicker.show(supportFragmentManager, "tag")

            datePicker.addOnPositiveButtonClickListener {

                val selectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                selectedCalendar.timeInMillis = it

                // Extract day, month, and year from the selected date
                val selectedDay = selectedCalendar.get(Calendar.DAY_OF_MONTH)
                val selectedMonth = selectedCalendar.get(Calendar.MONTH) + 1 // Month is zero-based
                val selectedYear = selectedCalendar.get(Calendar.YEAR)

                val selectedDateTime = LocalDateTime.of(
                    LocalDate.of(selectedYear, selectedMonth, selectedDay),
                    LocalTime.of(timePicker.hour, timePicker.minute)
                )

                if (selectedDateTime.isBefore(LocalDateTime.now())) {
                    Toast.makeText(
                        this,
                        "Selected date and time is before the current time",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (toggleAlarm.isChecked) {
                        AlarmClock.cancelAlarm(this)
                        AlarmClock.setAlarm(selectedDateTime, this)
                    } else {
                        Toast.makeText(this, "Alarms are disabled", Toast.LENGTH_SHORT).show()
                    }
                    StoreData(this).storeAlarmClock(selectedDateTime, true)
                }
            }
            datePicker.addOnNegativeButtonClickListener {
                datePicker.dismiss()
            }
            datePicker.addOnCancelListener {
                datePicker.dismiss()
            }
            datePicker.addOnDismissListener {
                datePicker.dismiss()
            }
        }
        timePicker.addOnNegativeButtonClickListener {
            timePicker.dismiss()
        }
        timePicker.addOnCancelListener {
            timePicker.dismiss()
        }
        timePicker.addOnDismissListener {
            timePicker.dismiss()
        }
    }

    private fun getStartDate(): Long {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        calendar.timeInMillis = today

        val startDate = calendar.timeInMillis
        return startDate
    }

    private fun createNotificationChannel() {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(NotificationChannel(
            ALARM_CLOCK_NOTIFICATION_CHANNEL_ID,
            getString(R.string.alarm_notifications_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ))

        val notificationManager2 =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager2.createNotificationChannel(NotificationChannel(
            INFO_NOTIFICARION_CHANNEL_ID,
            getString(R.string.info_notifications_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ))
    }

    private fun disableClicking() {
        toggleAlarm.isClickable = false
        editAlarmToday.isClickable = false
    }

    @SuppressLint("SetTextI18n")
    private fun handleToggleAlarm(isChecked: Boolean) {
        StoreData(this).storeAlarmActive(isChecked)

        Log.i(TAG, "entered listener")
        if (isChecked) {
            if (this.areNotificationsEnabled()) {
                Log.i(TAG, "should schedule")
                AlarmScheduler(this).schedule(this, true)
            } else {
                toggleAlarm.isChecked = false
                Toast.makeText(this, getString(R.string.enable_notifications), Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.i(TAG, "cancelling")
            AlarmClock.cancelAlarm(this)
            AlarmScheduler(this).cancel()
        }
    }


    private fun requestNotificationPermission() {
        Log.i(TAG, "requesting Notification permission")
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
        val (alarmClockDateTime, _) = storeData.loadAlarmClock()
        if (alarmClockDateTime == null) {
            runOnUiThread {
                alarmPreview.text = getString(R.string.error)
                AlarmManager.main(this)
                editAlarmToday.isClickable = true
            }
            return
        }
        currentAlarmClockDateTime = alarmClockDateTime
        val (alarmClockStrHour, alarmClockStrMinute) = this.reformatAlarmClockPreview(alarmClockDateTime)
        runOnUiThread {
            alarmPreview.text = "${alarmClockDateTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${alarmClockStrHour}:${alarmClockStrMinute}"
            editAlarmToday.isClickable = true
        }
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
