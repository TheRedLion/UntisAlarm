/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 */
package eu.karenfort.untisAlarm.helper

import android.media.RingtoneManager
import android.net.Uri
import android.os.StrictMode
import kotlinx.coroutines.CoroutineExceptionHandler

//notification channels
const val ALARM_CLOCK_NOTIFICATION_CHANNEL_ID = "alarm_clock_channel"
const val INFO_NOTIFICATION_CHANNEL_ID = "info_notifs_channel"

//codes
const val ALARM_REQUEST_CODE = 73295871
const val OPEN_ALARM_TAB_INTENT_CODE = 9996

//ids
const val ALARM_CLOCK_NOTIFICATION_ID = 9998
const val ALARM_CLOCK_ID = 543

//settings default
const val DEFAULT_TBS = 60 //min
const val VIBRATE_DEFAULT = false
const val SNOOZE_DEFAULT = 5 //min
const val IVG_DEFAULT = false

const val MAX_SNOOZE = 30 //min
const val MAX_TBS = 12 * 60 //12h*60min

//not settable defaults
const val INCREASE_VOLUME_DELAY = 300L
const val MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS = 1
const val MAX_ALARM_DURATION = 60
const val DISABLED_BUTTON_ALPHA_VALUE = .4F

//links
val ABOUT_US_PAGE: Uri = Uri.parse("https://github.com/TheRedLion/UntisAlarm")

//language
val SUPPORTED_LANGUAGES = arrayOf("System Default", "English", "German")
val SUPPORTED_LANGUAGES_TAG = arrayOf("system", "en", "de") //tag as defined in Locale

val ALARM_SOUND_DEFAULT_URI: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

const val SILENT = "silent"
val SILENT_URI: Uri = Uri.parse(SILENT)

val COROUTINE_EXCEPTION_HANDLER = CoroutineExceptionHandler { _, throwable ->
    throwable.printStackTrace()
}
val ALLOW_NETWORK_ON_MAIN_THREAD: StrictMode.ThreadPolicy =
    StrictMode.ThreadPolicy.Builder().permitAll().build()
