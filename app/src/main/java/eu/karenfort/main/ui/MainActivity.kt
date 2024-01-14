/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This Activity is the Main screen of the App and allows the user to activate and
 *      deactivate the alarm as well as edit the alarm set for tomorrow.
 */
package eu.karenfort.main.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat.is24HourFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.carlkarenfort.test.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import eu.karenfort.main.helper.StoreData
import eu.karenfort.main.alarm.AlarmScheduler
import eu.karenfort.main.alarmClock.AlarmClock
import eu.karenfort.main.alarmClock.AlarmClockSetter
import eu.karenfort.main.helper.ALARM_CLOCK_NOTIFICATION_CHANNEL_ID
import eu.karenfort.main.helper.COROUTINE_EXCEPTION_HANDLER
import eu.karenfort.main.helper.INFO_NOTIFICATION_CHANNEL_ID
import eu.karenfort.main.extentions.areNotificationsEnabled
import eu.karenfort.main.extentions.isDisabled
import eu.karenfort.main.extentions.getAlarmPreviewString
import eu.karenfort.main.helper.isTiramisuPlus
import eu.karenfort.main.extentions.showErrorToast
import eu.karenfort.main.extentions.toast
import eu.karenfort.main.helper.ABOUT_US_PAGE
import eu.karenfort.main.dataPass.OnDataPassedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Calendar
import java.util.TimeZone

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener,
    OnDataPassedListener {
    private lateinit var alarmPreview: Button
    private lateinit var toggleAlarm: MaterialSwitch
    private lateinit var notifsDisabledCard: MaterialCardView
    private lateinit var resetAlarm: Button
    private var currentAlarmClockDateTime: LocalDateTime? = null

    companion object {
        var active = false //used to check if app is active for API versions below 31
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getLayoutObjectsByID()
        disableClicking() //disabling clicks until everything was properly loaded to stop errors
        createNotificationChannel()
        sendToWelcomeActivityIfNotLoggedIn() //send user to welcomeActivity if they have not logged in yet
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            loadAndDisplayAlarmClockPreview()
            loadAndSetAlarmActive()
        }
        setListener()
        updateNotificationsDisabledWarning()

        /* request Notification Permission after 1 second if it was not granted yet
            adding the one second delay made the loading process feel better for
            the user
         */
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
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, string: String?) {
        if (string == null) return //don't know when this would happen

        if (!isAlarmClockRelated(string)) return //only respond if alarmClockTime was edited

        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            val (alarmClockDateTime, alarmClockEdited) = StoreData(this@MainActivity).loadAlarmClock()
            //update UI accordingly
            if (alarmClockDateTime == null) {
                currentAlarmClockDateTime = AlarmClockSetter.main(this@MainActivity)

                runOnUiThread{
                    if (currentAlarmClockDateTime != null) {
                        alarmPreview.text = getAlarmPreviewString(
                            currentAlarmClockDateTime!!
                        )
                    }
                    resetAlarm.isDisabled =
                        true //can never be edited if alarm clock time was just loaded
                }
                return@launch
            }

            currentAlarmClockDateTime = alarmClockDateTime
            runOnUiThread {
                alarmPreview.text = getAlarmPreviewString(alarmClockDateTime)
                resetAlarm.isDisabled = !alarmClockEdited
            }
        }
    }
    override fun onAlarmPreviewPassed(localDateTime: LocalDateTime) {
        alarmPreview.text = getAlarmPreviewString(localDateTime)
    }
    override fun onNotificationsAllowedPassed(areNotificationsAllowed: Boolean) {
        if (areNotificationsAllowed) {
            notifsDisabledCard.visibility = INVISIBLE
        } else {
            requestNotificationPermission()
            notifsDisabledCard.visibility = VISIBLE
        }
    }
    override fun onResume() {
        super.onResume()
        updateNotificationsDisabledWarning()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.top_right_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                val settingDialogFragment = SettingsDialogFragment()
                settingDialogFragment.show(supportFragmentManager, SettingsDialogFragment.TAG)
            }
            R.id.logout -> {
                startActivity(Intent(this@MainActivity, WelcomeActivity::class.java))
                StoreData(this).deleteLoginData()
            }
            R.id.about_us -> {
                val uri = ABOUT_US_PAGE
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun isAlarmClockRelated(string: String) =
        string.equals(StoreData.KEY_ALARM_CLOCK_HOUR) ||
                string.equals(StoreData.KEY_ALARM_CLOCK_MINUTE) ||
                string.equals(StoreData.KEY_ALARM_CLOCK_EDITED) ||
                string.equals(StoreData.KEY_ALARM_CLOCK) ||
                string.equals(StoreData.KEY_ALARM_CLOCK_MONTH) ||
                string.equals(StoreData.KEY_ALARM_CLOCK_DAY) ||
                string.equals(StoreData.KEY_ALARM_CLOCK_EDITED) ||
                string.equals(StoreData.KEY_ALARM_CLOCK_ACTIVE)

    private fun updateNotificationsDisabledWarning() {
        if (areNotificationsEnabled) {
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
        CoroutineScope(Dispatchers.Default).launch {
            if (toggleAlarm.isChecked) {
                currentAlarmClockDateTime = AlarmClockSetter.main(this@MainActivity, null, false)
                if (currentAlarmClockDateTime != null) alarmPreview.text = getAlarmPreviewString(
                    currentAlarmClockDateTime!!
                )
            } else {
                currentAlarmClockDateTime = AlarmClockSetter.main(this@MainActivity, null, false)
                if (currentAlarmClockDateTime != null) alarmPreview.text = getAlarmPreviewString(
                    currentAlarmClockDateTime!!
                )
            }
        }
        resetAlarm.isDisabled = true
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
                .setTitleText(getString(R.string.edit_alarm_time))
                .setInputMode(INPUT_MODE_CLOCK)
                .build()

        timePicker.show(supportFragmentManager, null)

        timePicker.addOnPositiveButtonClickListener {
            timePicker.dismiss()

            val startDate = getStartDate()

            val constraintsBuilder =
                CalendarConstraints.Builder()
                    .setStart(startDate)

            val selectedDateMilli = currentAlarmClockDateTime?.toInstant(
                ZoneOffset.UTC
            )?.toEpochMilli()
                ?: MaterialDatePicker.todayInUtcMilliseconds()

            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.select_date))
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
                    this.showErrorToast(getString(R.string.selected_date_and_time_is_before_the_current_time))
                } else {
                    if (toggleAlarm.isChecked) {
                        AlarmClock.cancel(this)
                        AlarmClock.set(selectedDateTime, this)
                        currentAlarmClockDateTime = selectedDateTime
                        if (currentAlarmClockDateTime != null) alarmPreview.text = getAlarmPreviewString(
                            currentAlarmClockDateTime!!
                        )
                    } else {
                        this.toast(getString(R.string.alarms_are_disabled))
                    }
                    StoreData(this).storeAlarmClock(selectedDateTime, true)
                    resetAlarm.isDisabled = false
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
            INFO_NOTIFICATION_CHANNEL_ID,
            getString(R.string.info_notifications_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ))
    }
    private fun disableClicking() {
        toggleAlarm.isClickable = false
        alarmPreview.isClickable = false
        resetAlarm.isClickable = false
    }
    private fun handleToggleAlarm(isChecked: Boolean) {
        StoreData(this).storeAlarmActive(isChecked)
        if (isChecked) {
            if (areNotificationsEnabled) {
                AlarmScheduler(this).schedule(this)
                CoroutineScope(Dispatchers.Default).launch {
                    currentAlarmClockDateTime = AlarmClockSetter.main(this@MainActivity, true)
                    if (currentAlarmClockDateTime != null) {
                        runOnUiThread{
                            alarmPreview.text = getAlarmPreviewString(currentAlarmClockDateTime!!)
                        }
                    }
                }
            } else {
                toggleAlarm.isChecked = false
                this.toast(getString(R.string.enable_notifications))
            }
        } else {
            AlarmClock.cancel(this)
            AlarmScheduler(this).cancel()
        }
    }
    private fun requestNotificationPermission() {
        if (!isTiramisuPlus()) return
        if (areNotificationsEnabled) return

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
        val storeData = StoreData(this)
        val aaState: Boolean = storeData.loadAlarmActive() ?: false
        runOnUiThread {
            toggleAlarm.isChecked = aaState
            toggleAlarm.isClickable = true
        }
    }
    private suspend fun loadAndDisplayAlarmClockPreview() {
        val storeData = StoreData(this)
        val (alarmClockDateTime, alarmClockEdited) = storeData.loadAlarmClock()

        if (alarmClockEdited) {
            runOnUiThread{
                resetAlarm.isDisabled = false
            }
        } else {
            runOnUiThread {
                resetAlarm.isDisabled = true
            }
        }

        if (alarmClockDateTime == null) {
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
        currentAlarmClockDateTime = alarmClockDateTime
        runOnUiThread {
            alarmPreview.text = getAlarmPreviewString(alarmClockDateTime)
            alarmPreview.isClickable = true
        }
    }
    private suspend fun hasLoggedIn() : Boolean {
        val storeData = StoreData(this)
        return storeData.loadLoginData()[0].isNullOrEmpty() || storeData.loadID() == null
    }
    private fun sendToWelcomeActivityIfNotLoggedIn() {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            if (!hasLoggedIn()) return@launch
            intent = Intent(this@MainActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
