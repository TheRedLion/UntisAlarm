package eu.karenfort.main.helper

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import eu.karenfort.main.activities.MainActivity
import eu.karenfort.main.alarmClock.AlarmClock
import eu.karenfort.main.alarmClock.HideAlarmReceiver

fun Context.isScreenOn() = (getSystemService(Context.POWER_SERVICE) as PowerManager).isScreenOn

fun Context.deleteNotificationChannel(channelId: String) {
    if (isOreoPlus()) {
        try {
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.deleteNotificationChannel(channelId)
        } catch (_: Throwable) {
        }
    }
}

private fun doToast(context: Context, message: String, length: Int) {
    if (context is Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            Toast.makeText(context, message, length).show()
        }
    } else {
        Toast.makeText(context, message, length).show()
    }
}
fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    try {
        if (isOnMainThread()) {
            doToast(this, msg, length)
        } else {
            Handler(Looper.getMainLooper()).post {
                doToast(this, msg, length)
            }
        }
    } catch (e: Exception) {
    }
}
fun Context.showErrorToast(msg: String, length: Int = Toast.LENGTH_LONG) {
    toast(String.format("error", msg), length)
}
fun Context.showErrorToast(exception: Exception, length: Int = Toast.LENGTH_LONG) {
    showErrorToast(exception.toString(), length)
}
fun Context.getHideAlarmPendingIntent(alarm: AlarmClock, channelId: String): PendingIntent {
    val intent = Intent(this, HideAlarmReceiver::class.java).apply {
        putExtra(ALARM_ID, alarm.id)
        putExtra(ALARM_NOTIFICATION_CHANNEL_ID, channelId)
    }
    return PendingIntent.getBroadcast(this, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}
fun Context.getLaunchIntent() = packageManager.getLaunchIntentForPackage("eu.karenfort.main")

fun Context.grantReadUriPermission(uriString: String) {
    try {
        // ensure custom reminder sounds play well
        grantUriPermission("com.android.systemui", Uri.parse(uriString), Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (ignored: Exception) {
    }
}
fun Context.getAlarmNotification(pendingIntent: PendingIntent, alarm: AlarmClock): Notification {
    val soundUri = alarm.soundUri
    if (soundUri != "silent") {
        grantReadUriPermission(soundUri)
    }
    val channelId = "simple_alarm_channel_${soundUri}_${alarm.vibrate}"
    val label = "Alarm"

    if (isOreoPlus()) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setLegacyStreamType(AudioManager.STREAM_ALARM)
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_HIGH
        NotificationChannel(channelId, label, importance).apply {
            setBypassDnd(true)
            enableLights(true)
            // todo: Proper black and light mode / preferences preferably in a new settings activity
            lightColor = 0
            enableVibration(alarm.vibrate)
            setSound(Uri.parse(soundUri), audioAttributes)
            notificationManager.createNotificationChannel(this)
        }
    }

    val dismissIntent = getHideAlarmPendingIntent(alarm, channelId)
    val builder = NotificationCompat.Builder(this)
        .setContentTitle(label)
        .setContentText(getFormattedTime(getPassedSeconds(), false, false))
        .setSmallIcon(R.drawable.ic_alarm_vector)
        .setContentIntent(pendingIntent)
        .setPriority(Notification.PRIORITY_HIGH)
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setAutoCancel(true)
        .setChannelId(channelId)
        .addAction(
            com.simplemobiletools.commons.R.drawable.ic_snooze_vector,
            getString(com.simplemobiletools.commons.R.string.snooze),
            getSnoozePendingIntent(alarm)
        )
        .addAction(com.simplemobiletools.commons.R.drawable.ic_cross_vector, getString(com.simplemobiletools.commons.R.string.dismiss), dismissIntent)
        .setDeleteIntent(dismissIntent)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    if (soundUri != SILENT) {
        builder.setSound(Uri.parse(soundUri), AudioManager.STREAM_ALARM)
    }

    if (alarm.vibrate) {
        val vibrateArray = LongArray(2) { 500 }
        builder.setVibrate(vibrateArray)
    }

    val notification = builder.build()
    notification.flags = notification.flags or Notification.FLAG_INSISTENT
    return notification
}
fun Context.getOpenAlarmTabIntent(): PendingIntent {
    val intent = getLaunchIntent() ?: Intent(this, MainActivity::class.java)
    intent.putExtra("open_tab", 1)
    return PendingIntent.getActivity(this, 9996, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.hideNotification(id: Int) {
    val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.cancel(id)
}
fun Context.showAlarmNotification(alarm: AlarmClock) {
    val pendingIntent = getOpenAlarmTabIntent()
    val notification = getAlarmNotification(pendingIntent, alarm)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    try {
        notificationManager.notify(alarm.id, notification)
    } catch (e: Exception) {
        showErrorToast(e)
    }
}
