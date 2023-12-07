package eu.karenfort.main.activities

import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.carlkarenfort.test.R
import eu.karenfort.main.helper.ALARM_SOUND_DEFAULT_URI


class CancelledMessageInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancelled_message_info)
        //todo implement that lessons with messages that have "Tasks(Aufgaben)" or "Cancelled(Ausfall)" in them are shown and the user can check if they are actually cancelled
    }
}