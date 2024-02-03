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
const val ALARM_CLOCK_NOTIFICATION_CHANNEL_ID: String = "alarm_clock_channel"
const val INFO_NOTIFICATION_CHANNEL_ID: String = "info_notifs_channel"

//codes
const val ALARM_REQUEST_CODE: Int = 73295871
const val OPEN_ALARM_TAB_INTENT_CODE: Int = 9996

//ids
const val ALARM_CLOCK_NOTIFICATION_ID: Int = 9998
const val ALARM_CLOCK_ID: Int = 543

//settings default
const val TBS_DEFAULT: Int = 60 //min
const val VIBRATE_DEFAULT: Boolean = false
const val SNOOZE_DEFAULT: Int = 5 //min
const val IVG_DEFAULT: Boolean = false

const val MAX_SNOOZE: Int = 30 //min
const val MAX_TBS: Int = 12 * 60 //12h*60min

//not settable defaults
const val INCREASE_VOLUME_DELAY: Long = 300L
const val MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS: Int = 1
const val MAX_ALARM_DURATION: Int = 60
const val DISABLED_BUTTON_ALPHA_VALUE: Float = .4F

//links
val ABOUT_US_PAGE: Uri = Uri.parse("https://github.com/TheRedLion/UntisAlarm")

//language
val SUPPORTED_LANGUAGES: Array<String> = arrayOf("System Default", "English", "German")
val SUPPORTED_LANGUAGES_TAG: Array<String> =
    arrayOf("system", "en", "de") //tag as defined in Locale

val ALARM_SOUND_DEFAULT_URI: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

const val SILENT: String = "content://silent"
val SILENT_URI: Uri = Uri.parse(SILENT)

val COROUTINE_EXCEPTION_HANDLER: CoroutineExceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
val ALLOW_NETWORK_ON_MAIN_THREAD: StrictMode.ThreadPolicy =
    StrictMode.ThreadPolicy.Builder().permitAll().build()
