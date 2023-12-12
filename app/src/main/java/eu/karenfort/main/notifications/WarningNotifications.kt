package eu.karenfort.main.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import com.carlkarenfort.test.R
import eu.karenfort.main.helper.INFO_NOTIFICARION_CHANNEL_ID

class WarningNotifications {
    companion object {
        fun sendNoInternetNotif(context: Context) {
            val builder = NotificationCompat.Builder(context, INFO_NOTIFICARION_CHANNEL_ID)
                .setContentTitle("You have no internet connection!")
                .setContentText(context.getString(R.string.your_alarms_might_not_be_set_properly_please_make_sure_that_they_are))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            builder.build()
        } //todo add implementation

    }
}
