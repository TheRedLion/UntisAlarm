package eu.karenfort.main.helper

import android.app.Activity
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.widget.Toast
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.carlkarenfort.test.R
import eu.karenfort.main.StoreData
import eu.karenfort.main.activities.MainActivity
import eu.karenfort.main.activities.WelcomeActivity
import eu.karenfort.main.alarmClock.AlarmClock
import eu.karenfort.main.alarmClock.AlarmClockReceiver
import eu.karenfort.main.alarmClock.DismissAlarmReceiver
import eu.karenfort.main.alarmClock.EarlyAlarmDismissalReceiver
import eu.karenfort.main.alarmClock.HideAlarmReceiver
import eu.karenfort.main.alarmClock.SnoozeAlarmReceiver
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes

fun getNextDay(): LocalDate {
    //Log.i(TAG, "called getNextDay()")
    var nextDay = LocalDate.now()
    nextDay = nextDay.plusDays(1)

    // Check if the next day is a weekend (Saturday or Sunday)
    while (nextDay.dayOfWeek == DayOfWeek.SATURDAY || nextDay.dayOfWeek == DayOfWeek.SUNDAY) {
        nextDay = nextDay.plusDays(1)
    }
    return nextDay
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
fun Context.isOnline(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return true
        }
    }
    return false
}
fun Context.isScreenOn() = (getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive

fun Context.areNotificationsEnabled(): Boolean {
    return NotificationManagerCompat.from(this).areNotificationsEnabled()
}
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
        Log.i("Context", e.toString())
    }
}
fun Context.showErrorToast(msg: String, length: Int = Toast.LENGTH_LONG) {
    toast(String.format("error", msg), length)
}
fun Context.showErrorToast(exception: Exception, length: Int = Toast.LENGTH_LONG) {
    showErrorToast(exception.toString(), length)
}
fun Context.getHideAlarmPendingIntent(): PendingIntent {
    val intent = Intent(this, HideAlarmReceiver::class.java)
    return PendingIntent.getBroadcast(this, ALARM_CLOCK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.getSnoozePendingIntent(): PendingIntent {
    val intent = Intent(this, SnoozeAlarmReceiver::class.java)
    return PendingIntent.getBroadcast(this, ALARM_CLOCK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.getLaunchIntent() = packageManager.getLaunchIntentForPackage("eu.karenfort.main")

fun Context.grantReadUriPermission(uri: Uri) {
    try {
        // ensure custom reminder sounds play well
        grantUriPermission("com.android.systemui", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (ignored: Exception) {
        Log.i("Context", "ERRRRRROR")
    }
}
fun Context.getAlarmNotification(pendingIntent: PendingIntent): Notification {
    var soundUri: Uri?
    var vibrate: Boolean
    runBlocking {
        val storeData = StoreData(applicationContext)
        val (_, newSoundUri) = storeData.loadSound()
        soundUri = newSoundUri
        if (soundUri == null) {
            soundUri = Uri.parse("content://silent")
        }
        vibrate = storeData.loadVibrate() ?: true
    }

    if (soundUri != Uri.parse("content://silent")) {
        grantReadUriPermission(soundUri!!)
    }
    val channelId = "simple_alarm_channel_${soundUri}_${vibrate}"
    val label = "Alarm"

    val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setLegacyStreamType(AudioManager.STREAM_ALARM)
        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
        .build()

    var darkmode = 0
    var isDark = false
    runBlocking {
        val storeData = StoreData(applicationContext)    // 0: System Default, 1: Off: 2: On
        if (storeData.loadDarkMode() == null) {
            storeData.storeDarkMode(0)
        }
        darkmode = storeData.loadDarkMode()!!
        if (darkmode == 0) {
           // check if system default is dark mode
            val currentNightMode = applicationContext.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            isDark = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
    }
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val importance = NotificationManager.IMPORTANCE_HIGH
    NotificationChannel(channelId, label, importance).apply {
        setBypassDnd(true)
        if (isDark or (darkmode == 2)) {
            enableLights(true)
            lightColor = 0
        } else {
            enableLights(false)
        }
        enableVibration(vibrate)
        setSound(soundUri, audioAttributes)
        notificationManager.createNotificationChannel(this)
    }

    val dismissIntent = getHideAlarmPendingIntent()
    val builder = NotificationCompat.Builder(this, ALARM_NOTIFICATION_CHANNEL_ID)
        .setContentTitle(label)
        .setContentText(getFormattedTime(getPassedSeconds(), false, false))
        .setSmallIcon(R.drawable.ic_alarm_vector)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationManager.IMPORTANCE_HIGH)
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setAutoCancel(true)
        .setChannelId(channelId)
        .addAction(R.drawable.ic_snooze_vector, getString(R.string.snooze), getSnoozePendingIntent())
        .addAction(R.drawable.ic_cross_vector, getString(R.string.dismiss_alarm), dismissIntent)
        .setDeleteIntent(dismissIntent)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    if (soundUri != Uri.parse("content://silent")) {
        builder.setSound(soundUri, AudioManager.STREAM_ALARM)
    }

    if (vibrate) {
        val vibrateArray = LongArray(2) { 1000 }
        builder.setVibrate(vibrateArray)
    }

    val notification = builder.build()
    notification.flags = notification.flags or Notification.FLAG_INSISTENT
    return notification
}
fun Context.getOpenAlarmTabIntent(): PendingIntent {
    val intent = getLaunchIntent() ?: Intent(this, MainActivity::class.java)
    return PendingIntent.getActivity(this, 9996, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.hideNotification(id: Int) {
    val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.cancel(id)
}
fun Context.showAlarmNotification() {
    val pendingIntent = getOpenAlarmTabIntent()
    val notification = getAlarmNotification(pendingIntent)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    try {
        notificationManager.notify(ALARM_CLOCK_ID, notification)
    } catch (e: Exception) {
        showErrorToast(e)
    }
}

fun Context.getFormattedTime(passedSeconds: Int, showSeconds: Boolean, makeAmPmSmaller: Boolean): SpannableString {
    val use24HourFormat = DateFormat.is24HourFormat(this)
    val hours = (passedSeconds / 3600) % 24
    val minutes = (passedSeconds / 60) % 60
    val seconds = passedSeconds % 60

    return if (use24HourFormat) {
        val formattedTime = formatTime(showSeconds, true, hours, minutes, seconds)
        SpannableString(formattedTime)
    } else {
        val formattedTime = formatTo12HourFormat(showSeconds, hours, minutes, seconds)
        val spannableTime = SpannableString(formattedTime)
        val amPmMultiplier = if (makeAmPmSmaller) 0.4f else 1f
        spannableTime.setSpan(RelativeSizeSpan(amPmMultiplier), spannableTime.length - 3, spannableTime.length, 0)
        spannableTime
    }
}

fun Context.formatTo12HourFormat(showSeconds: Boolean, hours: Int, minutes: Int, seconds: Int): String {
    val appendable = if (hours >= 12) "pm" else "am"
    val newHours = if (hours == 0 || hours == 12) 12 else hours % 12
    return "${formatTime(showSeconds, false, newHours, minutes, seconds)} $appendable"
}

fun Context.setupAlarmClock(triggerInSeconds: Int) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val targetMS = System.currentTimeMillis() + triggerInSeconds * 1000
    try {
        AlarmClock.setAlarm(triggerInSeconds, applicationContext)
        // show a notification to allow dismissing the alarm 10 minutes before it actually triggers
        val dismissalTriggerTime = if (targetMS - System.currentTimeMillis() < 10.minutes.inWholeMilliseconds) {
            System.currentTimeMillis() + 500
        } else {
            targetMS - 10.minutes.inWholeMilliseconds
        }
        AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, 0, dismissalTriggerTime, getEarlyAlarmDismissalIntent())
    } catch (e: Exception) {
        showErrorToast(e)
    }
}

fun Context.getAlarmIntent(): PendingIntent {
    val intent = Intent(this, AlarmClockReceiver::class.java)
    return PendingIntent.getBroadcast(this, ALARM_CLOCK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.getEarlyAlarmDismissalIntent(): PendingIntent {
    val intent = Intent(this, EarlyAlarmDismissalReceiver::class.java)
    return PendingIntent.getBroadcast(this, EARLY_ALARM_DISMISSAL_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.getDismissAlarmPendingIntent(): PendingIntent {
    val intent = Intent(this, DismissAlarmReceiver::class.java)
    return PendingIntent.getBroadcast(this, ALARM_CLOCK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.cancelAlarmClock() {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    AlarmClock.cancelAlarm(applicationContext)
    alarmManager.cancel(getEarlyAlarmDismissalIntent())
}

val Context.notificationManager: NotificationManager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
