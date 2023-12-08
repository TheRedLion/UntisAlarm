package eu.karenfort.main.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carlkarenfort.test.R


class CancelledMessageInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancelled_message_info)
        //todo implement that lessons with messages that have "Tasks(Aufgaben)" or "Cancelled(Ausfall)" in them are shown and the user can check if they are actually cancelled
    }
}