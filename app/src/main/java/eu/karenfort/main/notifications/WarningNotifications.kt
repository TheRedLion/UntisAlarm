package eu.karenfort.main.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.carlkarenfort.test.R
import eu.karenfort.main.activities.MainActivity
import eu.karenfort.main.activities.WelcomeActivity
import eu.karenfort.main.helper.ALARM_CLOCK_ID
import eu.karenfort.main.helper.ALARM_NOTIFICATION_CHANNEL_ID
import eu.karenfort.main.helper.INFO_NOTIFICARION_CHANNEL_ID
import eu.karenfort.main.helper.NOT_LOGGED_IN_CHANNEL_ID
import eu.karenfort.main.helper.notificationManager

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
