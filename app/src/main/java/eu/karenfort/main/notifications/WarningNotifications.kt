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

        fun sendLoggedOutNotif() {

        } //todo add implementation
    }
}

/*
fun Context.sendLoggedOutNotif() {
    val pendingIntent = getOpenAlarmTabIntent()

    val label = getString(R.string.not_logged_in)

    val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setLegacyStreamType(AudioManager.STREAM_ALARM)
        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
        .build()

    val importance = NotificationManager.IMPORTANCE_HIGH
    NotificationChannel(NOT_LOGGED_IN_CHANNEL_ID, label, importance).apply {
        notificationManager.createNotificationChannel(this)
    }


    val loginIntent = Intent(this, WelcomeActivity::class.java)
    loginIntent.(Intent.FLAG_ACTIVITY_NEW_TASK)
    val loginPendingIntent = PendingIntent.get
    val builder = NotificationCompat.Builder(this, ALARM_NOTIFICATION_CHANNEL_ID)
        .setContentTitle(label)
        .setContentText(getString(R.string.you_are_currently_not_logged_in_please_login_again))
        .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setAutoCancel(true)
        .setChannelId(NOT_LOGGED_IN_CHANNEL_ID)
        .addAction(R.drawable.ic_login_vector, getString(R.string.cancel), loginPendingIntent)
        .addAction(R.drawable.ic_cancel_vector, getString(R.string.login), cancelPendingIntent)
        .setDeleteIntent(cancelPendingIntent)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    val notification = builder.build()
    notification.flags = notification.flags or Notification.FLAG_INSISTENT

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    try {
        notificationManager.notify(ALARM_CLOCK_ID, notification)
    } catch (e: Exception) {
        Log.i("Context", "uhhh")
    }
}*/