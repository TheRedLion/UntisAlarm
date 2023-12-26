/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This activity is used to let the user select a custom ringtone.
 */
package eu.karenfort.main.activities

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.carlkarenfort.test.R
import eu.karenfort.main.StoreData
import eu.karenfort.main.helper.ALARM_SOUND_DEFAULT_URI
import eu.karenfort.main.helper.parcelable

class AlarmSoundPicker : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_sound_picker)

        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ALARM_SOUND_DEFAULT_URI)

        val ringtonePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val uri = data.parcelable<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    val title = data.extras?.getString(RingtoneManager.EXTRA_RINGTONE_TITLE)
                    if (uri != null) {
                        if (title != null) {
                            StoreData(this).storeSound(title, uri)
                        } else {
                            StoreData(this).storeSound(getString(R.string.alarm_sound), uri)
                        }
                        finish()
                    } else {
                        Toast.makeText(this, getString(R.string.no_success), Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.no_success), Toast.LENGTH_LONG).show()
                finish()
            }
        }
        ringtonePickerLauncher.launch(intent)
    }
}