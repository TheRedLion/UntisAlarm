/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: Fragment to change settings.
 */
package eu.karenfort.main.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.carlkarenfort.test.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import eu.karenfort.main.alarmClock.AlarmClockSetter
import eu.karenfort.main.dataPass.DataPass
import eu.karenfort.main.extentions.changeDarkMode
import eu.karenfort.main.extentions.toast
import eu.karenfort.main.helper.ALARM_SOUND_DEFAULT_URI
import eu.karenfort.main.helper.COROUTINE_EXCEPTION_HANDLER
import eu.karenfort.main.helper.DarkMode
import eu.karenfort.main.helper.IVG_DEFAULT
import eu.karenfort.main.helper.MAX_SNOOZE
import eu.karenfort.main.helper.MAX_TBS
import eu.karenfort.main.helper.SILENT_URI
import eu.karenfort.main.helper.SNOOZE_DEFAULT
import eu.karenfort.main.helper.SUPPORTED_LANGUAGES
import eu.karenfort.main.helper.SUPPORTED_LANGUAGES_TAG
import eu.karenfort.main.helper.StoreData
import eu.karenfort.main.helper.TBS_DEFAULT
import eu.karenfort.main.helper.VIBRATE_DEFAULT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class SettingsDialogFragment : DialogFragment() {

    //layout objects

    //private lateinit var cancellationMessageField: TextInputEditText
    //private lateinit var cancellationMessageLayout: TextInputLayout
    private lateinit var languageSettings: ConstraintLayout
    private lateinit var alarmSoundSettings: ConstraintLayout
    private lateinit var darkModeSettings: ConstraintLayout
    private lateinit var ivgToggle: MaterialCheckBox
    private lateinit var vibrateToggle: MaterialCheckBox
    private lateinit var tbsInputField: TextInputEditText
    private lateinit var snoozeInputField: TextInputEditText
    private lateinit var tbsInputLayout: TextInputLayout
    private lateinit var snoozeInputLayout: TextInputLayout
    private lateinit var makeSilent: Button
    private lateinit var toolbar: MaterialToolbar

    //these are used to preload settings
    private var storedLanguage: String? = null
    private var storedDarkMode: DarkMode? = null
    private var storedUri: Uri? = null

    private lateinit var context: Context
    private lateinit var nonNullActivity: FragmentActivity

    companion object {
        const val TAG = "SettingsDialogFragment"
    }

    override fun onAttach(context1: Context) {
        super.onAttach(context1)
        context = context1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nonNullActivity = activity?: throw Error("Activity is Null")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_settings, container)

        //colorSchemeSettings = view.findViewById(R.id.color_scheme_settings)
        //cancellationMessageLayout = view.findViewById(R.id.cancelled_message_input_layout)
        //cancellationMessageField = view.findViewById(R.id.cancelled_message_input_field)
        languageSettings = view.findViewById(R.id.language_settings)
        darkModeSettings = view.findViewById(R.id.dark_mode_settings)
        alarmSoundSettings = view.findViewById(R.id.alarm_settings)
        ivgToggle = view.findViewById(R.id.ivgToggle)
        vibrateToggle = view.findViewById(R.id.vibrateToggle)
        tbsInputField = view.findViewById(R.id.tbs_input_field)
        snoozeInputField = view.findViewById(R.id.snooze_input_field)
        tbsInputLayout = view.findViewById(R.id.tbs_input_layout)
        snoozeInputLayout = view.findViewById(R.id.snooze_input_layout)
        makeSilent = view.findViewById(R.id.makeSilent)
        toolbar = view.findViewById(R.id.settings_toolar)

        setListener()
        disableClicking() //disabling clicks until everything was properly loaded to prevent errors
        loadAndDisplayStoredStates()
        setListener()

        return view
    }
    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    private fun setListener() {
        //cancellationMessageLayout.setEndIconOnClickListener { handleSetCancellationMessageEnd() }
        //cancellationMessageLayout.setStartIconOnClickListener { handleCancellationMessageInfo() }

        //listener when exiting field
        snoozeInputField.onFocusChangeListener = View.OnFocusChangeListener { _, b ->
            if(!b){
                handleSetSnooze()
            }
        }
        tbsInputField.onFocusChangeListener = View.OnFocusChangeListener { _, b ->
            if(!b) {
                handleSetTBS()
            }
        }
        //lister for 2sec after entering something
        val snoozeHandler = Handler(Looper.getMainLooper())
        snoozeInputField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                snoozeHandler.postDelayed({
                    handleSetSnooze()
                }, 2*1000L)
            }
        })
        val tbsHandler = Handler(Looper.getMainLooper())
        tbsInputField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                tbsHandler.postDelayed({
                    handleSetTBS()
                }, 2*1000L)
            }
        })

        //colorSchemeSettings.setOnClickListener { colorDialog() }
        vibrateToggle.addOnCheckedStateChangedListener { _, state -> handleToggleVibrate(state) }
        ivgToggle.addOnCheckedStateChangedListener { _, state -> handleToggleIVG(state) }
        darkModeSettings.setOnClickListener { darkModeDialog() }
        alarmSoundSettings.setOnClickListener { alarmSoundDialog() }
        languageSettings.setOnClickListener { languageDialog() }
        makeSilent.setOnClickListener {
            StoreData(context).storeSound(SILENT_URI)
        }
        toolbar.setNavigationOnClickListener { nonNullActivity.onBackPressedDispatcher.onBackPressed() }
    }

    /*
    private fun handleCancellationMessageInfo() {
        startActivity(Intent(this@SettingsActivity, CancelledMessageInfo::class.java))
    }
     */
    private fun enableClicking() {
        nonNullActivity.runOnUiThread {
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
            val storeData = StoreData(context)

            val tbs: Int = storeData.loadTBS() ?: initTBS()
            val vibrate: Boolean = storeData.loadVibrate() ?: initVibrate()
            val snooze: Int = storeData.loadSnoozeTime() ?: initSnooze()
            val ivg: Boolean = storeData.loadIncreaseVolumeGradually() ?: initIVG()

            storedUri = storeData.loadSound()?: initSound()
            storedLanguage = storeData.loadLanguage()?: initLanguage()
            storedDarkMode = storeData.loadDarkMode()?: initDarkMode()

            nonNullActivity.runOnUiThread {
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
    private fun languageDialog() {
        var checkedItem = 0

        if (storedLanguage != null) {
            try {
                checkedItem = SUPPORTED_LANGUAGES.indexOf(storedLanguage)
            } catch (_: Error) {}
        } else {
            runBlocking {
                val storeData = StoreData(context)
                val language = storeData.loadLanguage()
                if (language != null) {
                    checkedItem = SUPPORTED_LANGUAGES.indexOf(language)
                }
            }
        }

        MaterialAlertDialogBuilder(context)
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

                StoreData(context).storeLanguage(SUPPORTED_LANGUAGES[checkedItem])
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
                val storeData = StoreData(context)
                if (storeData.loadDarkMode() != null) {
                    checkedItem = storeData.loadDarkMode()!!.ordinal
                }
            }
        }
        MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.change_app_theme))
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()
                // Change app theme to the selected theme
                context.changeDarkMode(DarkMode.values()[checkedItem])
                StoreData(context).storeDarkMode(checkedItem)
            }
            .setSingleChoiceItems(listItems, checkedItem) { _, which ->
                checkedItem = which
            }.show()
    }

    private fun alarmSoundDialog() {
        val intent = Intent(context, AlarmSoundPicker::class.java)
        intent.putExtra(AlarmSoundPicker.INTENT_ALARM_SOUND_URI, storedUri.toString())
        startActivity(intent)
    }

    private fun handleToggleVibrate(state: Int) {
        val stateBool = state == MaterialCheckBox.STATE_CHECKED
        StoreData(context).storeVibrate(stateBool)
    }

    private fun handleToggleIVG(state: Int) {
        val stateBool = state == MaterialCheckBox.STATE_CHECKED
        StoreData(context).storeIncreaseVolumeGradually(stateBool)
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

        if (newSnoozeStr.isEmpty()) return
        snoozeInputField.setText("") //must be after isEmpty check to prevent afterTextChanged triggering

        val newSnooze: Int
        try {
            newSnooze = Integer.parseInt(newSnoozeStr)
        } catch (e: NumberFormatException) { //this should never happen since the input is a number input
            context.toast(getString(R.string.please_only_enter_integers))
            return
        }

        if (newSnooze > MAX_SNOOZE) {
            context.toast(getString(R.string.the_maximum_length_is) + MAX_SNOOZE + getString(R.string.minutes))
            return
        }

        snoozeInputLayout.hint =
            "${getString(R.string.snooze_time)} (${getString(R.string.currently)} $newSnooze${
                getString(R.string.short_minute)
            })"

        StoreData(context).storeTBS(newSnooze)
    }

    private fun handleSetTBS() {
        val newTBSStr = tbsInputField.text.toString()

        if (newTBSStr.isEmpty()) return
        tbsInputField.setText("") //must be after isEmpty check to prevent afterTextChanged triggering

        val newTBS: Int
        try {
            newTBS = Integer.parseInt(newTBSStr)
        } catch (e: NumberFormatException) {
            context.toast(getString(R.string.please_only_enter_integers))
            return
        }

        if (newTBS > MAX_TBS) {
            context.toast(getString(R.string.the_maximum_length_is) + MAX_TBS/12 + getString(R.string.hours))
            return
        }

        tbsInputLayout.hint =
            "${getString(R.string.time_before_school_hint)} (${getString(R.string.currently)} $newTBS${
                getString(R.string.short_minute)
            })"

        val storeData = StoreData(context)
        storeData.storeTBS(newTBS)
        CoroutineScope(Dispatchers.Default).launch {//aa is alarm active
            val aaStateNullable: Boolean? = storeData.loadAlarmActive()
            var aaState = false
            if (aaStateNullable != null) {
                aaState = aaStateNullable
            }
            if (aaState) {
                DataPass.passLocalDateTime(context,AlarmClockSetter.main(context)?: return@launch)
            }
        }
    }

    private fun initDarkMode(): DarkMode {
        StoreData(context).storeDarkMode(DarkMode.DEFAULT)
        return DarkMode.DEFAULT
    }
    private fun initLanguage(): String {
        StoreData(context).storeLanguage(SUPPORTED_LANGUAGES[0])
        return SUPPORTED_LANGUAGES[0]
    }
    private fun initSound(): Uri {
        StoreData(context).storeSound(ALARM_SOUND_DEFAULT_URI)
        return ALARM_SOUND_DEFAULT_URI
    }
    private fun initIVG(): Boolean {
        StoreData(context).storeIncreaseVolumeGradually(IVG_DEFAULT)
        return IVG_DEFAULT
    }
    private fun initSnooze(): Int {
        StoreData(context).storeSnoozeTime(SNOOZE_DEFAULT)
        return SNOOZE_DEFAULT
    }
    private fun initVibrate(): Boolean {
        StoreData(context).storeVibrate(VIBRATE_DEFAULT)
        return VIBRATE_DEFAULT
    }
    private fun initTBS(): Int {
        StoreData(context).storeTBS(TBS_DEFAULT)
        return TBS_DEFAULT
    }
}