/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Some functions from https://github.com/SimpleMobileTools/Simple-Clock
 * but modified.
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 */
package eu.karenfort.main.extentions

import android.app.Activity
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.carlkarenfort.test.R
import com.carlkarenfort.test.R.drawable.ic_login_vector
import com.carlkarenfort.test.R.string.not_logged_in
import com.carlkarenfort.test.R.string.you_are_currently_not_logged_in_please_login_again
import eu.karenfort.main.helper.StoreData
import eu.karenfort.main.activities.MainActivity
import eu.karenfort.main.activities.ReminderActivity.Companion.TAG
import eu.karenfort.main.alarmClock.AlarmClockReceiver
import eu.karenfort.main.alarmClock.HideAlarmReceiver
import eu.karenfort.main.alarmClock.SnoozeAlarmReceiver
import eu.karenfort.main.helper.ALARM_CLOCK_ID
import eu.karenfort.main.helper.ALARM_NOTIFICATION_CHANNEL_ID
import eu.karenfort.main.helper.ALARM_SOUND_DEFAULT_URI
import eu.karenfort.main.helper.DarkMode
import eu.karenfort.main.helper.INFO_NOTIFICATION_CHANNEL_ID
import eu.karenfort.main.helper.OPEN_ALARM_TAB_INTENT_CODE
import eu.karenfort.main.helper.SILENT_URI
import eu.karenfort.main.helper.isOnMainThread
import eu.karenfort.main.helper.isSnowConePlus
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale


val Context.notificationManager: NotificationManager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
val Context.alarmManager: AlarmManager get() = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
val Context.alarmClockPendingIntent: PendingIntent get() = PendingIntent.getBroadcast(
    this,
    ALARM_CLOCK_ID,
    Intent(this, AlarmClockReceiver::class.java),
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
)
val Context.areNotificationsEnabled: Boolean get() = NotificationManagerCompat.from(this).areNotificationsEnabled()
val Context.isScreenOn: Boolean get() = (this.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive
private val Context.snoozePendingIntent: PendingIntent get() = PendingIntent.getBroadcast(
    this,
    ALARM_CLOCK_ID,
    Intent(this, SnoozeAlarmReceiver::class.java),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)
private val Context.packageLaunchIntent get() = packageManager.getLaunchIntentForPackage("eu.karenfort.main")
private val Context.hideAlarmPendingIntent: PendingIntent get() = PendingIntent.getBroadcast(
    this,
    ALARM_CLOCK_ID,
    Intent(this, HideAlarmReceiver::class.java),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
private val Context.openAlarmTabIntent: PendingIntent get() = PendingIntent.getActivity(
    this,
    OPEN_ALARM_TAB_INTENT_CODE,
    packageLaunchIntent ?: Intent(this, MainActivity::class.java),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)

fun Context.changeDarkMode(checkedItem: Int) {
    if (isSnowConePlus()) {
        val uiManager = getSystemService(AppCompatActivity.UI_MODE_SERVICE) as UiModeManager
        when (checkedItem) {
            1 -> uiManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
            2 -> uiManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
        }
    } else {
        when (checkedItem) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}
fun Context.isOnline(): Boolean { //todo probably needs to be suspending
    if (hasNetworkConnection()) {
        try {
            val urlc = URL("http://www.google.com").openConnection() as HttpURLConnection
            urlc.setRequestProperty("User-Agent", "Test")
            urlc.setRequestProperty("Connection", "close")
            urlc.connectTimeout = 1500
            urlc.connect()
            return urlc.responseCode == 200
        } catch (e: IOException) {
            Log.e(TAG, "Error checking internet connection", e)
        }
    } else {
        Log.d(TAG, "No network available!")
    }
    return false
}
private fun Context.hasNetworkConnection(): Boolean {
    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

    return capabilities != null
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
    } catch (_: Exception) {}
}
fun Context.showErrorToast(msg: String, length: Int = Toast.LENGTH_LONG) {
    toast(String.format("error", msg), length)
}
fun Context.showErrorToast(exception: Exception, length: Int = Toast.LENGTH_LONG) {
    showErrorToast(exception.toString(), length)
}

fun Context.grantReadUriPermission(uri: Uri) {
    try {
        // ensure custom reminder sounds play well
        grantUriPermission("com.android.systemUI", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (ignored: Exception) {
        Log.i("Context", "Error")
    }
}
fun Context.sendNoInternetNotif() {
    val builder = NotificationCompat.Builder(this, INFO_NOTIFICATION_CHANNEL_ID)
        .setContentTitle(this.getString(R.string.you_have_no_internet_connection))
        .setContentText(this.getString(R.string.your_alarms_might_not_be_set_properly_please_make_sure_that_they_are))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    builder.build()
} //todo add implementation
fun Context.sendLoggedOutNotif() {
    NotificationChannel(
        INFO_NOTIFICATION_CHANNEL_ID,
        getString(R.string.info_notifications_channel_name),
        NotificationManager.IMPORTANCE_HIGH).apply {
        notificationManager.createNotificationChannel(this)
    }

    /* send user to MainActivity instead of WelcomeActivity in case the
        user logged in in between the notification was sent and the user
        clicking, the user will then be redirected to WelcomeActivity if
        he is not logged in
     */
    val loginIntent = Intent(this, MainActivity::class.java)
    val loginPendingIntent = PendingIntent.getActivity(
        this,
        0,
        loginIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(this, INFO_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(ic_login_vector)
        .setContentTitle(getString(not_logged_in))
        .setContentText(getString(you_are_currently_not_logged_in_please_login_again))
        .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setAutoCancel(true)
        .setContentIntent(loginPendingIntent)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    val notification = builder.build()
    notification.flags = notification.flags or Notification.FLAG_INSISTENT

    val notificationManager = this.notificationManager

    try {
        notificationManager.notify(ALARM_CLOCK_ID, notification)
    } catch (_: Exception) {}
}

fun Context.getAlarmNotification(pendingIntent: PendingIntent): Notification {
    var soundUri: Uri?
    var vibrate: Boolean
    runBlocking {
        val storeData = StoreData(this@getAlarmNotification)
        val (_, newSoundUri) = storeData.loadSound()
        soundUri = newSoundUri
        if (soundUri == null) {
            soundUri = Uri.parse("content://silent")
        }
        vibrate = storeData.loadVibrate() ?: true
    }

    if (soundUri == null) {
        soundUri = ALARM_SOUND_DEFAULT_URI
    }

    if (soundUri != SILENT_URI) {
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

    var darkMode: Int
    var isDark = false
    runBlocking {
        val storeData = StoreData(this@getAlarmNotification)    // 0: System Default, 1: Off: 2: On
        if (storeData.loadDarkMode() == null) {
            storeData.storeDarkMode(DarkMode.DEFAULT)
        }
        darkMode = storeData.loadDarkMode()?: 0
        if (darkMode == 1) {
            isDark = false
        }
        if (darkMode == 2) {
            isDark = true
        }
        if (darkMode == 0) {
           // check if system default is dark mode
            val currentNightMode = this@getAlarmNotification.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            isDark = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
    }
    val notificationManager = this.notificationManager
    val importance = NotificationManager.IMPORTANCE_HIGH
    NotificationChannel(channelId, label, importance).apply {
        setBypassDnd(true)
        if (isDark or (darkMode == 2)) {
            enableLights(true)
            lightColor = 0
        } else {
            enableLights(false)
        }
        enableVibration(vibrate)
        setSound(soundUri, audioAttributes)
        notificationManager.createNotificationChannel(this)
    }

    val dismissIntent = hideAlarmPendingIntent
    val builder = NotificationCompat.Builder(this, ALARM_NOTIFICATION_CHANNEL_ID)
        .setContentTitle(label)
        .setContentText(getAlarmPreviewString(LocalDateTime.now()))
        .setSmallIcon(R.drawable.ic_alarm_vector)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationManager.IMPORTANCE_HIGH)
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setAutoCancel(true)
        .setChannelId(channelId)
        .addAction(R.drawable.ic_snooze_vector, getString(R.string.snooze), snoozePendingIntent)
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
fun Context.hideNotification(id: Int) {
    val manager = this.notificationManager
    manager.cancel(id)
}
fun Context.showAlarmNotification() {
    val pendingIntent = openAlarmTabIntent
    val notification = getAlarmNotification(pendingIntent)
    val notificationManager = this.notificationManager
    try {
        notificationManager.notify(ALARM_CLOCK_ID, notification)
    } catch (e: Exception) {
        showErrorToast(e)
    }
}
fun Context.getAlarmPreviewString(alarmClockDateTime: LocalDateTime): String {
    if (DateFormat.is24HourFormat(this)) {
        val (alarmClockStrHour, alarmClockStrMinute) = reformatAlarmClockPreview(alarmClockDateTime)
        return "${
            alarmClockDateTime.dayOfWeek.getDisplayName(
                TextStyle.SHORT,
                Locale.getDefault()
            )
        } ${alarmClockStrHour}:${alarmClockStrMinute}"
    } else {
        val alarmClockHour = alarmClockDateTime.hour%12
        val (alarmClockStrHour, alarmClockStrMinute) = reformatAlarmClockPreview(alarmClockHour, alarmClockDateTime.minute)
        val ampm = if (alarmClockDateTime.hour >= 12) getString(R.string.pm) else getString(R.string.am)
        return "${
            alarmClockDateTime.dayOfWeek.getDisplayName(
                TextStyle.SHORT,
                Locale.getDefault()
            )
        } ${alarmClockStrHour}:${alarmClockStrMinute} $ampm"
    }
}

private fun reformatAlarmClockPreview(hour: Int, minute: Int): Pair<String, String> {
    val alarmClockStrHour = if (hour < 10) {
        "0$hour"
    } else {
        "$hour"
    }
    val alarmClockStrMinute = if (minute < 10) {
        "0$minute"
    } else {
        "$minute"
    }
    return Pair(alarmClockStrHour, alarmClockStrMinute)
}

private fun reformatAlarmClockPreview(alarmClock: LocalDateTime): Pair<String, String> {
    return reformatAlarmClockPreview(alarmClock.hour, alarmClock.minute)
}