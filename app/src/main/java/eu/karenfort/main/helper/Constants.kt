/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 */
package eu.karenfort.main.helper

import android.media.RingtoneManager
import android.net.Uri
import android.os.StrictMode
import kotlinx.coroutines.CoroutineExceptionHandler


//notification channels
const val ALARM_NOTIFICATION_CHANNEL_ID = "Alarm_Channel"
const val INFO_NOTIFICATION_CHANNEL_ID = "Info_Notifs_Channel"
const val ALARM_CLOCK_NOTIFICATION_CHANNEL_ID = "alarm_clock_channel"

//codes
const val ALARM_REQUEST_CODE = 73295871
const val OPEN_ALARM_TAB_INTENT_CODE = 9996

const val ALARM_NOTIFICATION_ID = 9998
const val ALARM_CLOCK_ID = 543

//settings default
const val TBS_DEFAULT = 60
const val VIBRATE_DEFAULT = true
const val SNOOZE_DEFAULT = 5
const val IVG_DEFAULT = false

//defaults
const val INCREASE_VOLUME_DELAY = 300L
const val MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS = 1
const val MAX_ALARM_DURATION = 60
const val DISABLED_BUTTON_ALPHA_VALUE = .4F

//links
val ABOUT_US_PAGE = Uri.parse("https://github.com/TheRedLion/UntisAlarm")

//language
val SUPPORTED_LANGUAGES = arrayOf("System Default", "English", "German")
val SUPPORTED_LANGUAGES_TAG = arrayOf("system", "en", "de")
/* tag as defined in Locale */

val ALARM_SOUND_DEFAULT_URI: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

const val SILENT = "content://silent"
val SILENT_URI: Uri = Uri.parse(SILENT)

val COROUTINE_EXCEPTION_HANDLER = CoroutineExceptionHandler{ _, throwable ->
    throwable.printStackTrace()
}
val ALLOW_NETWORK_ON_MAIN_THREAD: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
