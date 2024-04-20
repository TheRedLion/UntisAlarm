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
const val ALARM_NOTIFICATION_CHANNEL_ID = "Alarm_Channel"
const val INFO_NOTIFICATION_CHANNEL_ID = "Info_Notifs_Channel"
const val ALARM_CLOCK_NOTIFICATION_CHANNEL_ID = "alarm_clock_channel"

//codes
const val ALARM_REQUEST_CODE = 1000
const val OPEN_ALARM_TAB_INTENT_CODE = 1001

//ids
const val ALARM_NOTIFICATION_ID = 9998
const val ALARM_CLOCK_ID = 543

//settings default
const val DEFAULT_TBS_MIN = 60 //min
const val DEFAULT_DO_VIBRATE = false
const val DEFAULT_SNOOZE_MIN = 5 //min
const val DEFAULT_DO_IVG = false

const val MAX_SNOOZE_MIN = 30 //min
const val MAX_TBS_MIN = 12 * 60 //12h*60min

//not settable defaults
const val INCREASE_VOLUME_DELAY = 300L
const val MIN_ALARM_VOLUME_FOR_INCREASING_ALARMS = 1
const val MAX_ALARM_DURATION = 60
const val DISABLED_BUTTON_ALPHA_VALUE = .4F

const val NEW_ALARM_TIME_MILLIS = 60 * 60 * 1000 //1 hour
const val NEW_ALARM_TIME_MILLIS_WHEN_NO_ALARM_TODAY = 5 * 60 * 60 * 1000 //5 hours
const val NEW_ALARM_TIME_MILLIS_WHEN_NO_SCHOOL_FOUND = 3 * 24 * 60 * 60 * 1000 //3 days

//links
val ABOUT_US_PAGE: Uri = Uri.parse("https://github.com/TheRedLion/UntisAlarm")

//language
val SUPPORTED_LANGUAGES = arrayOf("System Default", "English", "German")
val SUPPORTED_LANGUAGES_TAG = arrayOf("system", "en", "de") //tag as defined in Locale

val ALARM_SOUND_DEFAULT_URI: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

const val SILENT = "silent"
val SILENT_URI: Uri = Uri.parse(SILENT)

val COROUTINE_EXCEPTION_HANDLER: CoroutineExceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
val ALLOW_NETWORK_ON_MAIN_THREAD: StrictMode.ThreadPolicy =
    StrictMode.ThreadPolicy.Builder().permitAll().build()
