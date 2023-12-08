package eu.karenfort.main.helper

import android.annotation.SuppressLint
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.os.StrictMode
import androidx.annotation.ChecksSdkIntAtLeast
import com.carlkarenfort.test.R
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

//reminder Activity notifs / alarm notifs
const val ALARM_CLOCK_NOTIFICATION_CHANNEL_ID = "alarm_clock_channel"
const val INFO_NOTIFICATION_CHANNEL_ID = "info_channel"

const val ALARM_REQUEST_CODE = 73295871
const val ALARM_ID = "alarm_id"
const val ALARM_NOTIFICATION_CHANNEL_ID = "Alarm_Channel"
const val NOT_LOGGED_IN_CHANNEL_ID = "Not_logged_in_id"
const val ALARM_NOTIF_ID = 9998
const val EARLY_ALARM_NOTIF_ID = 10003
const val ALARM_CLOCK_ID = 543
const val EARLY_ALARM_DISMISSAL_INTENT_ID = 10002
const val EARLY_ALARM_DISMISSAL_CHANNEL_ID = "Early Alarm Dismissal"
const val NOTIFICATION_ID = "notification_id"
const val PICKFILE_RESULT_CODE = 7890
const val INFO_NOTIFICARION_CHANNEL_ID = "Info_Notifs_Channel"

const val SILENT_TITLE = "Silent"
const val SILENT = "content://silent"

const val LANGUAGE_SYSTEM_DEFAULT = "default"
const val LANGUAGE_DEFAULT = LANGUAGE_SYSTEM_DEFAULT
const val TBS_DEFAULT = 60
const val VIBRATE_DEFAULT = true
const val SNOOZE_DEFAULT = 5
const val DARK_MODE_DEFAULT = -1
const val INCREASE_VOLUME_DELAY = 300L
const val MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS = 1
const val MAX_ALARM_DURATION = 60

const val ALARM_SOUND_DEFAULT_TITLE = "Default Alarm"
val ALARM_SOUND_DEFAULT: String = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()

const val IVG_DEFAULT = false
val ALLOW_NETWORK_ON_MAIN_THREAD: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
val SILENT_URI: Uri = Uri.parse(SILENT)
val ALARM_SOUND_DEFAULT_URI: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}

fun formatTime(showSeconds: Boolean, use24HourFormat: Boolean, hours: Int, minutes: Int, seconds: Int): String {
    val hoursFormat = if (use24HourFormat) "%02d" else "%01d"
    var format = "$hoursFormat:%02d"

    return if (showSeconds) {
        format += ":%02d"
        String.format(format, hours, minutes, seconds)
    } else {
        String.format(format, hours, minutes)
    }
}

fun getPassedSeconds(): Int {
    val calendar = Calendar.getInstance()
    val isDaylightSavingActive = TimeZone.getDefault().inDaylightTime(Date())
    var offset = calendar.timeZone.rawOffset
    if (isDaylightSavingActive) {
        offset += TimeZone.getDefault().dstSavings
    }
    return ((calendar.timeInMillis + offset) / 1000).toInt()
}

@SuppressLint("ObsoleteSdkInt")
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
fun isTiramisuPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU


@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O_MR1)
fun isOreoMr1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
