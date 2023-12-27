/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 */
package eu.karenfort.main.helper

import android.annotation.SuppressLint
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.os.StrictMode
import androidx.annotation.ChecksSdkIntAtLeast
import kotlinx.coroutines.CoroutineExceptionHandler
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

//intent extra keys
const val NOTIFS_ALLOWED = "notifsAllowed"


//notification channels
const val ALARM_NOTIFICATION_CHANNEL_ID = "Alarm_Channel"
const val EARLY_ALARM_DISMISSAL_CHANNEL_ID = "Early Alarm Dismissal"
const val INFO_NOTIFICATION_CHANNEL_ID = "Info_Notifs_Channel"
const val ALARM_CLOCK_NOTIFICATION_CHANNEL_ID = "alarm_clock_channel"

//codes
const val ALARM_REQUEST_CODE = 73295871

const val ALARM_NOTIF_ID = 9998
const val ALARM_CLOCK_ID = 543

//settings default
const val TBS_DEFAULT = 60
const val VIBRATE_DEFAULT = true
const val SNOOZE_DEFAULT = 5
const val INCREASE_VOLUME_DELAY = 300L
const val MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS = 1
const val MAX_ALARM_DURATION = 60
const val IVG_DEFAULT = false

//language
val SUPPORTED_LANGUAGES = arrayOf("System Default", "English", "German")
val SUPPORTED_LANGUAGES_TAG = arrayOf("system", "en", "de")
/* tag as defined in RFC 4647, https://developer.android.com/reference/java/util/Locale
    for more info's
 */

val ALARM_SOUND_DEFAULT: String = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()

val ALLOW_NETWORK_ON_MAIN_THREAD: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()

const val SILENT_TITLE = "Silent"
const val SILENT = "content://silent"
val SILENT_URI: Uri = Uri.parse(SILENT)
val ALARM_SOUND_DEFAULT_URI: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

val COROUTINE_EXCEPTION_HANDLER = CoroutineExceptionHandler{ _, throwable ->
    throwable.printStackTrace()
}

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

@SuppressLint("ObsoleteSdkInt")
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
fun isTiramisuPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU


@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O_MR1)
fun isOreoMr1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
