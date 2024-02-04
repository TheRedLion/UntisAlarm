/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This activity is used to let the user select a custom ringtone.
 */
package eu.karenfort.main.ui

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.carlkarenfort.test.R
import eu.karenfort.main.extentions.parcelable
import eu.karenfort.main.extentions.toast
import eu.karenfort.main.helper.ALARM_SOUND_DEFAULT_URI
import eu.karenfort.main.helper.StoreData
import java.lang.RuntimeException

class AlarmSoundPicker : AppCompatActivity() {
    companion object {
        const val TAG = "AlarmSoundPicker"
        const val INTENT_ALARM_SOUND_URI = "alarmSoundUri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_sound_picker)

        if (intent == null) {
            startDialog()
            return
        }

        val extras = intent.extras
        if (extras == null) {
            startDialog()
            return
        }

        try {
            val uri = Uri.parse(extras.getString(INTENT_ALARM_SOUND_URI))
            startDialog(uri)
        } catch (e: RuntimeException) {
            startDialog()
        }
    }

    private fun startDialog(uri: Uri = ALARM_SOUND_DEFAULT_URI) {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
        intent.putExtra(
            RingtoneManager.EXTRA_RINGTONE_TITLE,
            getString(R.string.select_alarm_sound)
        )
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri)

        val ringtonePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != RESULT_OK) {
                this.toast(getString(R.string.no_success))
                finish()
                return@registerForActivityResult
            }
            val data = result.data ?: return@registerForActivityResult
            val newUri = data.parcelable<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            val title = data.extras?.getString(RingtoneManager.EXTRA_RINGTONE_TITLE)
            if (newUri == null) {
                this.toast(getString(R.string.no_success))
                finish()
                return@registerForActivityResult
            }
            if (title == null) {
                StoreData(this).storeSound(newUri)
            }
            StoreData(this).storeSound(newUri)
            finish()
        }
        ringtonePickerLauncher.launch(intent)
    }
}