/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This Activity is the Main screen of the App and allows the user to activate and
 *      deactivate the alarm as well as edit the alarm set for tomorrow.
 */
package eu.karenfort.main.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.carlkarenfort.test.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import eu.karenfort.main.StoreData
import eu.karenfort.main.alarmClock.AlarmClockSetter
import eu.karenfort.main.alarm.AlarmScheduler
import eu.karenfort.main.alarmClock.AlarmClock
import eu.karenfort.main.api.UntisApiCalls
import eu.karenfort.main.helper.ALARM_CLOCK_NOTIFICATION_CHANNEL_ID
import eu.karenfort.main.helper.ALLOW_NETWORK_ON_MAIN_THREAD
import eu.karenfort.main.helper.INFO_NOTIFICARION_CHANNEL_ID
import eu.karenfort.main.helper.NOTIFS_ALLOWED
import eu.karenfort.main.helper.areNotificationsEnabled
import eu.karenfort.main.helper.getAlarmPreviewString
import eu.karenfort.main.helper.isTiramisuPlus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Calendar
import java.util.TimeZone

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val TAG = "MainActivity"
    private lateinit var alarmPreview: Button
    private lateinit var toggleAlarm: MaterialSwitch
    private lateinit var notifsDisabledCard: MaterialCardView
    private lateinit var resetAlarm: Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getLayoutObjectsByID()
        disableClicking() //disabling clicks until everything was properly loaded to stop errors
        createNotificationChannel()
        sendToWelcomeActivity() //send user to welcomeActivity if they have not logged in yet
        CoroutineScope(Dispatchers.IO).launch {
            loadAndDisplayAlarmClockPreview()
            loadAndSetAlarmActive()
        }
        setListener()
        updateNotifsDisabledWarning()
        //request Notification Permission after 1 second if it was not granted yet
        Handler(Looper.getMainLooper()).postDelayed({
            requestNotificationPermission()
        }, 1000)
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
        Log.i(TAG, "shared Preferences Changed")

        if (string == null) return //don't know when this would happen

        if (!isAlarmClockTime(string)) return //only respond if alarmClockTime was edited

        CoroutineScope(Dispatchers.IO).launch {
            val (alarmClockDateTime, alarmClockEdited) = StoreData(this@MainActivity).loadAlarmClock()
            //update UI accordingly
            if (alarmClockDateTime == null) {
                currentAlarmClockDateTime = AlarmClockSetter.main(this@MainActivity)
                if (currentAlarmClockDateTime != null) alarmPreview.text = getAlarmPreviewString(
                    currentAlarmClockDateTime!!
                )
                Log.e(TAG, "Loaded alarmClockDateTime is null")
                resetAlarm.isClickable = false
                resetAlarm.alpha = 0.4F
            } else {
                runOnUiThread {
                    alarmPreview.text = getAlarmPreviewString(alarmClockDateTime)
                    Log.i(TAG, "setting curren alarm clock datetime to $alarmClockDateTime")
                    currentAlarmClockDateTime = alarmClockDateTime
                    if (alarmClockEdited) {
                        resetAlarm.isClickable = true
                        resetAlarm.alpha = 1F
                    } else {
                        resetAlarm.isClickable = false
                        resetAlarm.alpha = 0.4F
                    }
                }
            }
        }
    }

    override fun onResume() {
        Log.i(TAG, "resuming")
        super.onResume()
        updateNotifsDisabledWarning()
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

        if (extras.getBoolean(NOTIFS_ALLOWED)) {
            notifsDisabledCard.visibility = INVISIBLE
        } else {
            requestNotificationPermission()
            notifsDisabledCard.visibility = VISIBLE
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
                Log.i(TAG, "should go to welcomeActivity")
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
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


    private fun isAlarmClockTime(string: String) =
        string.equals(alarmClockHourKey) || string.equals(alarmClockMinuteKey) || string.equals(alarmClockEditedKey) || string.equals(alarmClockYearKey) || string.equals(alarmClockMonthKey) || string.equals(alarmClockDayKey)

    private fun updateNotifsDisabledWarning() {
        if (areNotificationsEnabled()) {
            notifsDisabledCard.visibility = INVISIBLE
        } else {
            notifsDisabledCard.visibility = VISIBLE
        }
    }

    private fun getLayoutObjectsByID() {
        alarmPreview = findViewById(R.id.alarmPreview)
        toggleAlarm = findViewById(R.id.toggleAlarm)
        resetAlarm = findViewById(R.id.reset_alarm_tomorrow)
        notifsDisabledCard = findViewById(R.id.disabled_notfs)
    }

    private fun setListener() {
        alarmPreview.setOnClickListener { handleEditAlarmToday() }
        toggleAlarm.setOnCheckedChangeListener { _, isChecked -> handleToggleAlarm(isChecked) }
        resetAlarm.setOnClickListener { handleResetAlarm() }
        notifsDisabledCard.setOnClickListener { handleNotifsDisabledCardClick() }
    }

    private fun handleNotifsDisabledCardClick() {
        //send user to notification settings of app
        val intent = Intent()
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
        startActivity(intent)
    }

    private fun handleResetAlarm() {
        StoreData(this).storeAlarmClock(false)
        if (toggleAlarm.isChecked) {
            currentAlarmClockDateTime = AlarmClockSetter.main(this, null, false)
            if (currentAlarmClockDateTime != null) alarmPreview.text = getAlarmPreviewString(
                currentAlarmClockDateTime!!
            )
        } else {
            currentAlarmClockDateTime = AlarmClockSetter.main(this, null, false)
            if (currentAlarmClockDateTime != null) alarmPreview.text = getAlarmPreviewString(
                currentAlarmClockDateTime!!
            )
        }
        resetAlarm.alpha = .4F
        resetAlarm.isClickable = false
    }

    private fun handleEditAlarmToday() {
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
                        currentAlarmClockDateTime = selectedDateTime
                        if (currentAlarmClockDateTime != null) alarmPreview.text = getAlarmPreviewString(
                            currentAlarmClockDateTime!!
                        )
                    } else {
                        Toast.makeText(this, "Alarms are disabled", Toast.LENGTH_SHORT).show()
                    }
                    StoreData(this).storeAlarmClock(selectedDateTime, true)
                    resetAlarm.alpha = 1F
                    resetAlarm.isClickable = true
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

        return calendar.timeInMillis
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
        alarmPreview.isClickable = false
        resetAlarm.isClickable = false
    }

    @SuppressLint("SetTextI18n")
    private fun handleToggleAlarm(isChecked: Boolean) {
        StoreData(this).storeAlarmActive(isChecked)

        Log.i(TAG, "entered listener")
        if (isChecked) {
            if (areNotificationsEnabled()) {
                Log.i(TAG, "should schedule")
                AlarmScheduler(this).schedule(this, true)
                currentAlarmClockDateTime = AlarmClockSetter.main(this, true)
                if (currentAlarmClockDateTime != null) alarmPreview.text = getAlarmPreviewString(
                    currentAlarmClockDateTime!!
                )
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
        if (areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are enabled. Should not request Notification Permission.")
            return
        }

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
        val (alarmClockDateTime, alarmClockEdited) = storeData.loadAlarmClock()

        if (alarmClockEdited) {
            runOnUiThread{
                resetAlarm.isClickable = true
                resetAlarm.alpha = 1F
            }
        } else {
            runOnUiThread {
                resetAlarm.isClickable = false
                resetAlarm.alpha = .4F
            }
        }

        if (alarmClockDateTime == null) {
            Log.e(TAG, "Loaded AlarmClockDateTime is null!")
            currentAlarmClockDateTime = AlarmClockSetter.main(this)
            runOnUiThread{
                if (currentAlarmClockDateTime != null) alarmPreview.text = getAlarmPreviewString(
                    currentAlarmClockDateTime!!
                )
            }
            runOnUiThread {
                alarmPreview.isClickable = true
            }
            return
        }
        Log.i(TAG, "setting current Alarm CLock Date time to $alarmClockDateTime")
        currentAlarmClockDateTime = alarmClockDateTime
        runOnUiThread {
            alarmPreview.text = getAlarmPreviewString(alarmClockDateTime)
            alarmPreview.isClickable = true
        }
    }

    private suspend fun hasLoggedIn() : Boolean {
        val storeData = StoreData(applicationContext)
        return storeData.loadLoginData()[0] == null || storeData.loadID() == null
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
