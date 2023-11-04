package eu.karenfort.main.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.carlkarenfort.test.R
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import eu.karenfort.main.StoreData
import eu.karenfort.main.alarm.AlarmItem
import eu.karenfort.main.alarm.AndroidAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsActivity : AppCompatActivity() {
    private val TAG = "SettingsActivity"
    private lateinit var languageSettings: ConstraintLayout
    private lateinit var colorSchemeSettings: ConstraintLayout
    private lateinit var defaultAlarmSettings: ConstraintLayout
    private lateinit var ivgToggle: com.google.android.material.checkbox.MaterialCheckBox
    private lateinit var darkModeToggle: com.google.android.material.checkbox.MaterialCheckBox
    private lateinit var vibrateToggle: com.google.android.material.checkbox.MaterialCheckBox
    private lateinit var tbsInputField: TextInputEditText
    private lateinit var snoozeInputField: TextInputEditText
    private lateinit var tbsInputLayout: TextInputLayout
    private lateinit var snoozeInputLayout: TextInputLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        languageSettings = findViewById(R.id.language_settings)
        colorSchemeSettings = findViewById(R.id.color_scheme_settings)
        darkModeToggle = findViewById(R.id.dark_mode_toggle)

        defaultAlarmSettings = findViewById(R.id.alarm_settings) //sound

        ivgToggle = findViewById(R.id.ivgToggle)
        vibrateToggle = findViewById(R.id.vibrateToggle)
        tbsInputField = findViewById(R.id.tbs_input_field)
        snoozeInputField = findViewById(R.id.snooze_input_field)
        tbsInputLayout = findViewById(R.id.tbs_input_layout)
        snoozeInputLayout = findViewById(R.id.snooze_input_layout)

        snoozeInputLayout.setEndIconOnClickListener { _: View? ->
            handleSetSnooze()
        }

        tbsInputLayout.setEndIconOnClickListener { _ : View? ->
            handleSetTBS()
        }

        vibrateToggle.addOnCheckedStateChangedListener { _, state ->
            handleToggleVibrate(state)
        }

        ivgToggle.addOnCheckedStateChangedListener { _, state ->
            handleToggleIVG(state)
        }


        darkModeToggle.addOnCheckedStateChangedListener { _, state ->
            handleToggleDarkMode(state)
        }

        defaultAlarmSettings.setOnClickListener {
            val listItems = arrayOf("Silent", "Set Custom Alarm", "Default Alarm Sound")
            val checkedItem = intArrayOf(-1)

            MaterialAlertDialogBuilder(this)
                .setTitle("Choose Alarm Sound")
                .setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                    Log.i(TAG, "canceled")
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.confirm)) { dialog, which ->

                    Log.i(TAG, "accepted chosen: ${listItems[checkedItem[0]]}")
                    dialog.dismiss()
                }
                .setSingleChoiceItems(listItems, checkedItem.get(0)) { dialog, which ->
                    checkedItem[0] = which
                    Log.i(TAG, "$which, ${checkedItem[0]}")
                }.show()
        }

        languageSettings.setOnClickListener {

        }

        colorSchemeSettings.setOnClickListener {

        }



    }

    private fun handleToggleDarkMode(state: Int) {
        val stateBool = state == MaterialCheckBox.STATE_CHECKED
        StoreData(this).storeDarkMode(stateBool)
    }

    private fun handleToggleVibrate(state: Int) {
        val stateBool = state == MaterialCheckBox.STATE_CHECKED
        StoreData(this).storeVibrate(stateBool)
    }

    private fun handleToggleIVG(state: Int) {
        val stateBool = state == MaterialCheckBox.STATE_CHECKED
        StoreData(this).storeIncreaseVolumeGradually(stateBool)
    }

    private fun handleSetSnooze() {
        val storeData = StoreData(this)
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

        storeData.storeTBS(newSnooze)
    }

    private fun handleSetTBS() {
        val storeData = StoreData(this)
        val alarmItem = AlarmItem(845746)
        val scheduler = AndroidAlarmScheduler(this)
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
            "${getString(R.string.snooze_time)} (${getString(R.string.currently)} $newTBS${
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
                alarmItem.let(scheduler::cancel)
                alarmItem.let(scheduler::schedule)
            }
        }
    }
}