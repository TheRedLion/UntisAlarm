/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: Activity to change settings.
 */
package eu.karenfort.main.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.LocaleListCompat
import com.carlkarenfort.test.R
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import eu.karenfort.main.helper.StoreData
import eu.karenfort.main.alarmClock.AlarmClockSetter
import eu.karenfort.main.helper.ALARM_SOUND_DEFAULT_TITLE
import eu.karenfort.main.helper.ALARM_SOUND_DEFAULT_URI
import eu.karenfort.main.helper.COROUTINE_EXCEPTION_HANDLER
import eu.karenfort.main.helper.DarkMode
import eu.karenfort.main.helper.IVG_DEFAULT
import eu.karenfort.main.helper.SILENT_TITLE
import eu.karenfort.main.helper.SILENT_URI
import eu.karenfort.main.helper.SNOOZE_DEFAULT
import eu.karenfort.main.helper.SUPPORTED_LANGUAGES
import eu.karenfort.main.helper.SUPPORTED_LANGUAGES_TAG
import eu.karenfort.main.helper.TBS_DEFAULT
import eu.karenfort.main.helper.VIBRATE_DEFAULT
import eu.karenfort.main.extentions.changeDarkMode
import eu.karenfort.main.extentions.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class SettingsActivity : AppCompatActivity() {
    //layout objects
    private lateinit var languageSettings: ConstraintLayout
    private lateinit var alarmSoundSettings: ConstraintLayout
    private lateinit var darkModeSettings: ConstraintLayout
    private lateinit var ivgToggle: MaterialCheckBox
    private lateinit var vibrateToggle: MaterialCheckBox
    private lateinit var tbsInputField: TextInputEditText
    private lateinit var snoozeInputField: TextInputEditText
    private lateinit var tbsInputLayout: TextInputLayout
    private lateinit var snoozeInputLayout: TextInputLayout
    //private lateinit var cancellationMessageField: TextInputEditText
    //private lateinit var cancellationMessageLayout: TextInputLayout
    private lateinit var alarmName: TextView
    private lateinit var makeSilent: Button

    //these are used to preload settings
    private var storedLanguage: String? = null
    private var storedDarkMode: DarkMode? = null
    private var storedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        getLayoutObjectsByID()
        disableClicking() //disabling clicks until everything was properly loaded to prevent errors
        loadAndDisplayStoredStates()
        setListener()

    }

    private fun getLayoutObjectsByID() {
        languageSettings = findViewById(R.id.language_settings)
        //colorSchemeSettings = findViewById(R.id.color_scheme_settings)
        darkModeSettings = findViewById(R.id.dark_mode_settings)
        alarmSoundSettings = findViewById(R.id.alarm_settings)
        alarmName = findViewById(R.id.alarm_sound_name)
        ivgToggle = findViewById(R.id.ivgToggle)
        vibrateToggle = findViewById(R.id.vibrateToggle)
        tbsInputField = findViewById(R.id.tbs_input_field)
        snoozeInputField = findViewById(R.id.snooze_input_field)
        tbsInputLayout = findViewById(R.id.tbs_input_layout)
        snoozeInputLayout = findViewById(R.id.snooze_input_layout)
        //cancellationMessageLayout = findViewById(R.id.cancelled_message_input_layout)
        //cancellationMessageField = findViewById(R.id.cancelled_message_input_field)
        makeSilent = findViewById(R.id.makeSilent)
    }

    private fun setListener() {
        //cancellationMessageLayout.setEndIconOnClickListener { handleSetCancellationMessageEnd() }
        //cancellationMessageLayout.setStartIconOnClickListener { handleCancellationMessageInfo() }
        snoozeInputLayout.setEndIconOnClickListener { _: View? -> handleSetSnooze() }
        tbsInputLayout.setEndIconOnClickListener { _: View? -> handleSetTBS() }
        vibrateToggle.addOnCheckedStateChangedListener { _, state -> handleToggleVibrate(state) }
        ivgToggle.addOnCheckedStateChangedListener { _, state -> handleToggleIVG(state) }
        darkModeSettings.setOnClickListener { darkModeDialog() }
        alarmSoundSettings.setOnClickListener { alarmSoundDialog() }
        languageSettings.setOnClickListener { languageDialog() }
        //colorSchemeSettings.setOnClickListener { colorDialog() }
        makeSilent.setOnClickListener {
            StoreData(this).storeSound(SILENT_TITLE, SILENT_URI)
        }
    }

    /*
    private fun handleCancellationMessageInfo() {
        startActivity(Intent(this@SettingsActivity, CancelledMessageInfo::class.java))
    }
     */


    private fun enableClicking() {
        runOnUiThread {
            languageSettings.isClickable = true
            //colorSchemeSettings.isClickable = true
            darkModeSettings.isClickable = true
            alarmSoundSettings.isClickable = true
            ivgToggle.isClickable = true
            vibrateToggle.isClickable = true
            tbsInputField.isClickable = true
            snoozeInputField.isClickable = true
        }
    }

    private fun disableClicking() {
        languageSettings.isClickable = false
        //colorSchemeSettings.isClickable = false
        darkModeSettings.isClickable = false
        alarmSoundSettings.isClickable = false
        ivgToggle.isClickable = false
        vibrateToggle.isClickable = false
        tbsInputField.isClickable = false
        snoozeInputField.isClickable = false
    }

    @SuppressLint("SetTextI18n")
    private fun loadAndDisplayStoredStates() {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            val storeData = StoreData(this@SettingsActivity)

            val tbs: Int = storeData.loadTBS() ?: initTBS()
            val vibrate: Boolean = storeData.loadVibrate() ?: initVibrate()
            val snooze: Int = storeData.loadSnoozeTime() ?: initSnooze()
            val ivg: Boolean = storeData.loadIncreaseVolumeGradually() ?: initIVG()

            var alarmSoundPair = storeData.loadSound()
            if (alarmSoundPair.first == null || alarmSoundPair.second == null) {
                alarmSoundPair = initSound()
            }
            val alarmSoundTitle = alarmSoundPair.first
            storedUri = alarmSoundPair.second

            storedLanguage = storeData.loadLanguage()?: initLanguage()
            storedDarkMode = storeData.loadDarkMode()?: initDarkMode()

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
                if (alarmSoundTitle.isNullOrEmpty()) {
                    alarmName.text = getString(R.string.alarm_sound)
                } else {
                    alarmName.text = "${getString(R.string.alarm_sound)} ($alarmSoundTitle)"
                }
            }
            enableClicking()
        }
    }

    private fun languageDialog() {
        var checkedItem = 0

        if (storedLanguage != null) {
            try {
                checkedItem = SUPPORTED_LANGUAGES.indexOf(storedLanguage)
            } catch (_: Error) {}
        } else {
            runBlocking {
                val storeData = StoreData(this@SettingsActivity)
                val language = storeData.loadLanguage()
                if (language != null) {
                    checkedItem = SUPPORTED_LANGUAGES.indexOf(language)
                }
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.select_language))
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()

                // Change app language
                val languageTag = SUPPORTED_LANGUAGES_TAG[checkedItem]
                val appLocale = LocaleListCompat.forLanguageTags(languageTag)
                AppCompatDelegate.setApplicationLocales(appLocale)

                StoreData(this).storeLanguage(SUPPORTED_LANGUAGES[checkedItem])
            }
            .setSingleChoiceItems(SUPPORTED_LANGUAGES, checkedItem) { _, which ->
                if (checkedItem != which) {
                    checkedItem = which
                }
            }.show()
    }

    private fun darkModeDialog() {
        val listItems = arrayOf(
            getString(R.string.system_default),
            getString(R.string.disabled),
            getString(R.string.enabled)
        ) //matches the states of StoreData.DARK_MODE_..., should not be changed

        // load checkedItem from StoreData
        var checkedItem = DarkMode.DEFAULT.ordinal
        if (storedDarkMode != null) {
            checkedItem = storedDarkMode!!.ordinal //is never set to null
        } else {
            runBlocking {
                val storeData = StoreData(this@SettingsActivity)
                if (storeData.loadDarkMode() != null) {
                    checkedItem = storeData.loadDarkMode()!!.ordinal
                }
            }
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.change_app_theme))
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()
                // Change app theme to the selected theme
                this.changeDarkMode(DarkMode.values()[checkedItem])
                StoreData(this).storeDarkMode(checkedItem)
            }
            .setSingleChoiceItems(listItems, checkedItem) { _, which ->
                checkedItem = which
            }.show()
    }

    private fun alarmSoundDialog() {
        val intent = Intent(this, AlarmSoundPicker::class.java)
        intent.putExtra(AlarmSoundPicker.INTENT_ALARM_SOUND_URI, storedUri.toString())
        startActivity(intent)
    }

    private fun initDarkMode(): DarkMode {
        StoreData(this).storeDarkMode(DarkMode.DEFAULT)
        return DarkMode.DEFAULT
    }

    private fun initLanguage(): String {
        StoreData(this).storeLanguage(SUPPORTED_LANGUAGES[0])
        return SUPPORTED_LANGUAGES[0]
    }

    private fun initSound(): Pair<String, Uri> {
        StoreData(this).storeSound(ALARM_SOUND_DEFAULT_TITLE, ALARM_SOUND_DEFAULT_URI)
        return Pair(ALARM_SOUND_DEFAULT_TITLE, ALARM_SOUND_DEFAULT_URI)
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

    /*
    private fun handleSetCancellationMessageEnd() {
        val newMessage = cancellationMessageField.text.toString()

        cancellationMessageField.setText("")

        cancellationMessageLayout.hint =
            "${getString(R.string.cancellation_text)} (${getString(R.string.currently)} \"$newMessage\")"

        StoreData(this).storeCancelledMessage(newMessage)
    }
     */

    private fun handleSetSnooze() {
        val newSnoozeStr = snoozeInputField.text.toString()

        snoozeInputField.setText("")

        val newSnooze: Int
        try {
            newSnooze = Integer.parseInt(newSnoozeStr)
        } catch (e: NumberFormatException) { //this should never happen since the input is a number input
            this.toast(getString(R.string.please_only_enter_integers))
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
                AlarmClockSetter.main(this@SettingsActivity)
            }
        }
    }
}