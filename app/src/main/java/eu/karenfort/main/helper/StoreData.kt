/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: Used to handel storage in Androids DataStore
 */
package eu.karenfort.main.helper

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("userData")

class StoreData(
    private val context: Context
) {
    companion object {
        val KEY_ID: Preferences.Key<Int> = intPreferencesKey("id")
        val KEY_USERNAME: Preferences.Key<String> = stringPreferencesKey("username")
        val KEY_PASSWORD: Preferences.Key<String> = stringPreferencesKey("password")
        val KEY_SCHOOL_SERVER: Preferences.Key<String> = stringPreferencesKey("server")
        val KEY_SCHOOL: Preferences.Key<String> = stringPreferencesKey("school")
        val KEY_TBS: Preferences.Key<Int> = intPreferencesKey("tbs")
        val KEY_ALARM_CLOCK_ACTIVE: Preferences.Key<Boolean> = booleanPreferencesKey("alarm-active")
        val KEY_ALARM_CLOCK: Preferences.Key<Int> = intPreferencesKey("alarmClockYear")
        val KEY_ALARM_CLOCK_MONTH: Preferences.Key<Int> = intPreferencesKey("alarmClockMonth")
        val KEY_ALARM_CLOCK_DAY: Preferences.Key<Int> = intPreferencesKey("alarmClockDay")
        val KEY_ALARM_CLOCK_HOUR: Preferences.Key<Int> = intPreferencesKey("alarmClockHour")
        val KEY_ALARM_CLOCK_MINUTE: Preferences.Key<Int> = intPreferencesKey("alarmClockMinute")
        val KEY_ALARM_CLOCK_EDITED: Preferences.Key<Boolean> =
            booleanPreferencesKey("alarmClockEdited")
        val KEY_VIBRATE: Preferences.Key<Boolean> = booleanPreferencesKey("vibrate")
        val KEY_SOUND_URI: Preferences.Key<String> = stringPreferencesKey("soundUri")
        val KEY_IVG: Preferences.Key<Boolean> = booleanPreferencesKey("increaseVolumeGradually")
        val KEY_SNOOZE_TIME: Preferences.Key<Int> = intPreferencesKey("snoozeTime")
        val KEY_DARK_MODE: Preferences.Key<Int> = intPreferencesKey("darkMode")
        val KEY_LANGUAGE: Preferences.Key<String> = stringPreferencesKey("language")
        //val KEY_CANCELLED_MESSAGE = stringPreferencesKey("cancelledMessage")
    }

    /*fun storeCancelledMessage(cancellationMessage: String) {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            context.dataStore.edit { settings ->
                settings[KEY_CANCELLED_MESSAGE] = cancellationMessage
            }
        }
    }
    suspend fun loadCancelledMessage(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[KEY_CANCELLED_MESSAGE]
    }*/
    fun storeDarkMode(darkMode: DarkMode) {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            context.dataStore.edit { settings ->
                settings[KEY_DARK_MODE] = darkMode.ordinal
            }
        }
    }

    fun storeDarkMode(darkMode: Int) {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            context.dataStore.edit { settings ->
                settings[KEY_DARK_MODE] = darkMode
            }
        }
    }

    suspend fun loadDarkMode(): DarkMode? {
        val preferences = context.dataStore.data.first()
        val darkModeOrdinal = preferences[KEY_DARK_MODE] ?: return null
        return DarkMode.values()[darkModeOrdinal]
    }

    fun storeLanguage(language: String) {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            context.dataStore.edit { settings ->
                settings[KEY_LANGUAGE] = language
            }
        }
    }

    suspend fun loadLanguage(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[KEY_LANGUAGE]
    }

    fun storeSnoozeTime(snoozeTime: Int) {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            context.dataStore.edit { settings ->
                settings[KEY_SNOOZE_TIME] = snoozeTime
            }
        }
    }

    suspend fun loadSnoozeTime(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[KEY_SNOOZE_TIME]
    }

    fun storeIncreaseVolumeGradually(vibrate: Boolean) {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            context.dataStore.edit { settings ->
                settings[KEY_IVG] = vibrate
            }
        }
    }

    suspend fun loadIncreaseVolumeGradually(): Boolean? {
        val preferences = context.dataStore.data.first()
        return preferences[KEY_VIBRATE]
    }

    fun storeVibrate(vibrate: Boolean) {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            context.dataStore.edit { settings ->
                settings[KEY_VIBRATE] = vibrate
            }
        }
    }

    suspend fun loadVibrate(): Boolean? {
        val preferences = context.dataStore.data.first()
        return preferences[KEY_VIBRATE]
    }

    suspend fun loadSound(): Uri? {
        val preferences = context.dataStore.data.first()
        val uriStr = preferences[KEY_SOUND_URI]
        val uri: Uri = if (uriStr == null) {
            ALARM_SOUND_DEFAULT_URI
        } else {
            try {
                Uri.parse(uriStr)
            } catch (_: Error) {
                return null
            }
        }

        return uri
    }

    fun storeSound(soundUri: Uri) {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            context.dataStore.edit { settings ->
                settings[KEY_SOUND_URI] = soundUri.toString()
            }
        }
    }

    fun deleteLoginData() {
        storeID(0)
        storeLoginData("", "", "", "")
    }

    suspend fun loadID(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[KEY_ID]
    }

    suspend fun loadLoginData(): Array<String?> {
        val preferences = context.dataStore.data.first()
        return arrayOf(
            preferences[KEY_USERNAME],
            preferences[KEY_PASSWORD],
            preferences[KEY_SCHOOL_SERVER],
            preferences[KEY_SCHOOL]
        )
    }

    suspend fun loadTBS(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[KEY_TBS]
    }

    suspend fun loadAlarmActive(): Boolean? {
        val preferences = context.dataStore.data.first()
        return preferences[KEY_ALARM_CLOCK_ACTIVE]
    }

    suspend fun loadAlarmClock(): Pair<LocalDateTime?, Boolean> {
        val preferences = context.dataStore.data.first()
        val year = preferences[KEY_ALARM_CLOCK]
        val month = preferences[KEY_ALARM_CLOCK_MONTH]
        val day = preferences[KEY_ALARM_CLOCK_DAY]
        val hour = preferences[KEY_ALARM_CLOCK_HOUR]
        val minute = preferences[KEY_ALARM_CLOCK_MINUTE]
        val edited: Boolean = preferences[KEY_ALARM_CLOCK_EDITED] == true

        if (year == -1) {
            return Pair(null, edited)
        }

        if (year == null || month == null || day == null || hour == null || minute == null) {
            return Pair(null, edited)
        }

        return Pair(LocalDateTime.of(year, month, day, hour, minute), edited)
    }

    fun storeAlarmClock(alarmClockDayTime: LocalDateTime?) {
        storeAlarmClock(alarmClockDayTime, false)
    }

    fun storeAlarmClock(edited: Boolean) {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            context.dataStore.edit { settings ->
                settings[KEY_ALARM_CLOCK_EDITED] = edited
            }
        }
    }

    fun storeAlarmClock(alarmClockDayTime: LocalDateTime?, edited: Boolean) {
        if (alarmClockDayTime == null) {
            CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
                context.dataStore.edit { settings ->
                    settings[KEY_ALARM_CLOCK] = -1
                    settings[KEY_ALARM_CLOCK_MONTH] = 0
                    settings[KEY_ALARM_CLOCK_DAY] = 0
                    settings[KEY_ALARM_CLOCK_HOUR] = 0
                    settings[KEY_ALARM_CLOCK_MINUTE] = 0
                    settings[KEY_ALARM_CLOCK_EDITED] = edited
                }
            }
        } else {
            CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
                context.dataStore.edit { settings ->
                    settings[KEY_ALARM_CLOCK] = alarmClockDayTime.year
                    settings[KEY_ALARM_CLOCK_MONTH] = alarmClockDayTime.monthValue
                    settings[KEY_ALARM_CLOCK_DAY] = alarmClockDayTime.dayOfMonth
                    settings[KEY_ALARM_CLOCK_HOUR] = alarmClockDayTime.hour
                    settings[KEY_ALARM_CLOCK_MINUTE] = alarmClockDayTime.minute
                    settings[KEY_ALARM_CLOCK_EDITED] = edited
                }
            }
        }
    }

    fun storeID(id: Int) {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            context.dataStore.edit { settings ->
                settings[KEY_ID] = id
            }
        }
    }

    fun storeLoginData(username: String, password: String, server: String, school: String) {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            context.dataStore.edit { settings ->
                settings[KEY_USERNAME] = username
                settings[KEY_PASSWORD] = password
                settings[KEY_SCHOOL_SERVER] = server
                settings[KEY_SCHOOL] = school
            }
        }
    }

    fun storeTBS(timeBeforeSchool: Int) {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            context.dataStore.edit { settings ->
                settings[KEY_TBS] = timeBeforeSchool
            }
        }
    }

    fun storeAlarmActive(alarmActive: Boolean) {
        CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
            context.dataStore.edit { settings ->
                settings[KEY_ALARM_CLOCK_ACTIVE] = alarmActive
            }
        }
    }
}