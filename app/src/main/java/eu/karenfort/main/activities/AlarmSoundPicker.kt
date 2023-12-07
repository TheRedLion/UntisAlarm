package eu.karenfort.main.activities

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.carlkarenfort.test.R
import eu.karenfort.main.StoreData
import eu.karenfort.main.helper.ALARM_SOUND_DEFAULT_URI

class AlarmSoundPicker : AppCompatActivity() {
    val TAG = "AlarmSoundPicker"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_sound_picker)

        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ALARM_SOUND_DEFAULT_URI)

        val ringtonePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {result ->
            if (result.resultCode == RESULT_OK) {
                // There are no request codes
                val data = result.data
                if (data != null) {
                    val uri = data.parcelable<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    val title = data.extras?.getString(RingtoneManager.EXTRA_RINGTONE_TITLE)
                    Log.i(TAG, "Title: $title")
                    if (uri != null) {
                        Log.i(TAG, uri.toString())
                        if (title != null) {
                            StoreData(this).storeSound(title, uri)
                        } else {
                            StoreData(this).storeSound(
                                getString(R.string.alarm_sound),
                                uri
                            )
                        }
                        finish()
                    } else {
                        Toast.makeText(this, getString(R.string.no_success), Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
        }
        ringtonePickerLauncher.launch(intent)
    }

    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }
}