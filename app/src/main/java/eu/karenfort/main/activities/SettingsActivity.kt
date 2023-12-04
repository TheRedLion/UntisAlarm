package eu.karenfort.main.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.carlkarenfort.test.R
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import eu.karenfort.main.StoreData
import eu.karenfort.main.alarm.AlarmScheduler
import eu.karenfort.main.helper.ALARM_SOUND_DEFAULT
import eu.karenfort.main.helper.DARK_MODE_DEFAULT
import eu.karenfort.main.helper.IVG_DEFAULT
import eu.karenfort.main.helper.LANGUAGE_DEFAULT
import eu.karenfort.main.helper.PICKFILE_RESULT_CODE
import eu.karenfort.main.helper.SNOOZE_DEFAULT
import eu.karenfort.main.helper.TBS_DEFAULT
import eu.karenfort.main.helper.VIBRATE_DEFAULT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsActivity : AppCompatActivity() {
    private val TAG = "SettingsActivity"
    private lateinit var languageSettings: ConstraintLayout
    private lateinit var colorSchemeSettings: ConstraintLayout
    private lateinit var alarmSoundSettings: ConstraintLayout
    private lateinit var darkModeSettings: ConstraintLayout
    private lateinit var ivgToggle: MaterialCheckBox
    private lateinit var vibrateToggle: MaterialCheckBox
    private lateinit var tbsInputField: TextInputEditText
    private lateinit var snoozeInputField: TextInputEditText
    private lateinit var tbsInputLayout: TextInputLayout
    private lateinit var snoozeInputLayout: TextInputLayout
    private lateinit var cancellationMessageField: TextInputEditText
    private lateinit var cancellationMessageLayout: TextInputLayout
    private lateinit var alarmName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        getLayoutObjectsByID()
        disableClicking() //disabling clicks until everything was properly loaded
        loadAndDisplayStoredStates()
        setListener()
    }

    private fun getLayoutObjectsByID() {
        languageSettings = findViewById(R.id.language_settings)
        colorSchemeSettings = findViewById(R.id.color_scheme_settings)
        darkModeSettings = findViewById(R.id.dark_mode_settings)
        alarmSoundSettings = findViewById(R.id.alarm_settings)
        alarmName = findViewById(R.id.alarm_sound_name)
        ivgToggle = findViewById(R.id.ivgToggle)
        vibrateToggle = findViewById(R.id.vibrateToggle)
        tbsInputField = findViewById(R.id.tbs_input_field)
        snoozeInputField = findViewById(R.id.snooze_input_field)
        cancellationMessageField = findViewById(R.id.cancelled_message_input_field)
        tbsInputLayout = findViewById(R.id.tbs_input_layout)
        snoozeInputLayout = findViewById(R.id.snooze_input_layout)
        cancellationMessageLayout = findViewById(R.id.cancelled_message_input_layout)
    }

    private fun setListener() {
        cancellationMessageLayout.setEndIconOnClickListener { handleSetCancellationMessageEnd() }
        cancellationMessageLayout.setStartIconOnClickListener { handleCancellationMessageInfo() }
        snoozeInputLayout.setEndIconOnClickListener { _: View? -> handleSetSnooze() }
        tbsInputLayout.setEndIconOnClickListener { _: View? -> handleSetTBS() }
        vibrateToggle.addOnCheckedStateChangedListener { _, state -> handleToggleVibrate(state) }
        ivgToggle.addOnCheckedStateChangedListener { _, state -> handleToggleIVG(state) }
        darkModeSettings.setOnClickListener { darkModeDialog() }
        alarmSoundSettings.setOnClickListener { alarmSoundDialog() }
        languageSettings.setOnClickListener { languageDialog() }
        colorSchemeSettings.setOnClickListener { colorDialog() }
    }

    private fun handleCancellationMessageInfo() {
        startActivity(Intent(this@SettingsActivity, CancelledMessageInfo::class.java))
    }


    private fun enableClicking() {
        languageSettings.isClickable = true
        colorSchemeSettings.isClickable = true
        darkModeSettings.isClickable = true
        alarmSoundSettings.isClickable = true
        ivgToggle.isClickable = true
        vibrateToggle.isClickable = true
        tbsInputField.isClickable = true
        snoozeInputField.isClickable = true
    }

    private fun disableClicking() {
        languageSettings.isClickable = false
        colorSchemeSettings.isClickable = false
        darkModeSettings.isClickable = false
        alarmSoundSettings.isClickable = false
        ivgToggle.isClickable = false
        vibrateToggle.isClickable = false
        tbsInputField.isClickable = false
        snoozeInputField.isClickable = false
    }

    private fun loadAndDisplayStoredStates() {
        CoroutineScope(Dispatchers.IO).launch {
            val storeData = StoreData(applicationContext)

            val tbs: Int = storeData.loadTBS() ?: initTBS()
            val vibrate: Boolean = storeData.loadVibrate() ?: initVibrate()
            val snooze: Int = storeData.loadSnoozeTime() ?: initSnooze()
            val ivg: Boolean = storeData.loadIncreaseVolumeGradually() ?: initIVG()
            val (title, _) = storeData.loadSound()
            if (title == null) initSound()
            if (storeData.loadLanguage() == null) initLanguage()
            if (storeData.loadDarkMode() == null) initDarkMode()

            runOnUiThread {
                tbsInputLayout.hint =
                    "${getString(R.string.time_before_school_hint)} (${getString(R.string.currently)} $tbs${
                        getString(R.string.short_minute)
                    })"
                vibrateToggle.isChecked = vibrate
                snoozeInputLayout.hint =
                    "${getString(R.string.snooze_time)} (${getString(R.string.currently)} $snooze${
                        getString(R.string.short_minute)
                    })"
                ivgToggle.isChecked = ivg

            }
            enableClicking()
        }
    }

    private fun colorDialog() {
        val listItems = arrayOf("System Default", "Red", "Blue", "Green", "Orange")
        var checkedItem = 0

        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Alarm Sound")
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()
                Log.i(TAG, "Storing $checkedItem in StoreData")
                //todo: add implementation
            }
            .setSingleChoiceItems(listItems, checkedItem) { _, which ->
                checkedItem = which
                Log.i(TAG, "$which, $checkedItem")
            }.show()
    }

    private fun languageDialog() {
        val listItems = arrayOf("System Default", "English", "German")
        var checkedItem = 0

        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Alarm Sound")
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()
                Log.i(TAG, "Storing $checkedItem in StoreData")
                //todo: add implementation
            }
            .setSingleChoiceItems(listItems, checkedItem) { _, which ->
                checkedItem = which
                Log.i(TAG, "$which, $checkedItem")
            }.show()
    }

    private fun darkModeDialog() {
        val listItems = arrayOf("System Default", "Lite", "Dark")
        var checkedItem = 0

        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Alarm Sound")
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()
                Log.i(TAG, "Storing $checkedItem in StoreData")
                StoreData(this).storeDarkMode(checkedItem - 1)
            }
            .setSingleChoiceItems(listItems, checkedItem) { _, which ->
                checkedItem = which
                Log.i(TAG, "$which, $checkedItem")
            }.show()
    }

    private fun alarmSoundDialog() {
        val listItems = arrayOf("System Default", "Silent", "Custom Alarm")
        var checkedItem = 0

        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Alarm Sound")
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()
                when (checkedItem) {//todo: update the alarm that is currently set
                    0 -> {
                        StoreData(this).storeSound("Default", Uri.parse("content://settings/system/alarm_alert"))
                    }
                    1 -> {
                        StoreData(this).storeSound("Silent", Uri.parse("content://silent"))
                    }
                    2 -> {
                        intent = Intent(this@SettingsActivity, ChooseSoundFileActivity::class.java)
                        startActivity(intent)
                    }
                }
                Log.i(TAG, "Storing $checkedItem in StoreData")
                //todo: add implementation
            }
            .setSingleChoiceItems(listItems, checkedItem) { _, which ->
                checkedItem = which
                Log.i(TAG, "$which, $checkedItem")
            }.show()
    }

    private fun initDarkMode() {
        StoreData(this).storeDarkMode(DARK_MODE_DEFAULT)
    }

    private fun initLanguage(): String {
        StoreData(this).storeLanguage(LANGUAGE_DEFAULT)
        return LANGUAGE_DEFAULT
    }

    private fun initSound(): String {
        StoreData(this).storeSound(ALARM_SOUND_DEFAULT, Uri.parse("content://silent"))
        return ALARM_SOUND_DEFAULT
    }

    private fun initIVG(): Boolean {
        StoreData(this).storeIncreaseVolumeGradually(IVG_DEFAULT)
        return IVG_DEFAULT
    }

    private fun initSnooze(): Int {
        StoreData(this).storeSnoozeTime(SNOOZE_DEFAULT)
        return SNOOZE_DEFAULT
    }

    private fun initVibrate(): Boolean {
        StoreData(this).storeVibrate(VIBRATE_DEFAULT)
        return VIBRATE_DEFAULT
    }

    private fun initTBS(): Int {
        StoreData(this).storeTBS(TBS_DEFAULT)
        return TBS_DEFAULT
    }


    private fun handleToggleVibrate(state: Int) {
        val stateBool = state == MaterialCheckBox.STATE_CHECKED
        StoreData(this).storeVibrate(stateBool)
    }

    private fun handleToggleIVG(state: Int) {
        val stateBool = state == MaterialCheckBox.STATE_CHECKED
        StoreData(this).storeIncreaseVolumeGradually(stateBool)
    }


    private fun handleSetCancellationMessageEnd() {
        val newMessage = cancellationMessageField.text.toString()

        cancellationMessageField.setText("")

        cancellationMessageLayout.hint =
            "${getString(R.string.cancellation_text)} (${getString(R.string.currently)} \"$newMessage\")"

        StoreData(this).storeCancelledMessage(newMessage)
    }

    private fun handleSetSnooze() {
        val newSnoozeStr = snoozeInputField.text.toString()

        snoozeInputField.setText("")

        val newSnooze: Int
        try {
            newSnooze = Integer.parseInt(newSnoozeStr)
        } catch (e: NumberFormatException) {
            Toast.makeText(
                this,
                getString(R.string.please_only_enter_integers),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        snoozeInputLayout.hint =
            "${getString(R.string.snooze_time)} (${getString(R.string.currently)} $newSnooze${
                getString(R.string.short_minute)
            })"

        StoreData(this).storeTBS(newSnooze)
    }

    private fun handleSetTBS() {
        val storeData = StoreData(this)
        val newTBSStr = tbsInputField.text.toString()

        tbsInputField.setText("")

        val newTBS: Int
        try {
            newTBS = Integer.parseInt(newTBSStr)
        } catch (e: NumberFormatException) {
            Toast.makeText(this, getString(R.string.please_only_enter_integers), Toast.LENGTH_SHORT)
                .show()
            return
        }
        tbsInputLayout.hint =
            "${getString(R.string.time_before_school_hint)} (${getString(R.string.currently)} $newTBS${
                getString(R.string.short_minute)
            })"

        storeData.storeTBS(newTBS)

        CoroutineScope(Dispatchers.Default).launch {
            val aaStateNullable: Boolean? = storeData.loadAlarmActive()
            var aaState = false
            if (aaStateNullable != null) {
                aaState = aaStateNullable
            }
            if (aaState) {
                AlarmScheduler(applicationContext).cancel()
                AlarmScheduler(applicationContext).schedule()
            }
        }
    }
}