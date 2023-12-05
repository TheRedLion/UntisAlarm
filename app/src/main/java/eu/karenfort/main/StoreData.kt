package eu.karenfort.main

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import eu.karenfort.main.helper.ALARM_SOUND_DEFAULT
import eu.karenfort.main.helper.ALARM_SOUND_DEFAULT_URI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("userData")
class StoreData (
    private val context: Context
) {
    private val idKey = intPreferencesKey("id")
    private val usernameKey = stringPreferencesKey("username")
    private val passwordKey = stringPreferencesKey("password")
    private val serverKey = stringPreferencesKey("server")
    private val schoolKey = stringPreferencesKey("school")
    private val timeBeforeSchoolKey = intPreferencesKey("tbs")
    private val alarmActiveKey = booleanPreferencesKey("alarm-active")
    private val alarmClockYearKey = intPreferencesKey("alarmClockYear")
    private val alarmClockMonthKey = intPreferencesKey("alarmClockMonth")
    private val alarmClockDayKey = intPreferencesKey("alarmClockDay")
    private val alarmClockHourKey = intPreferencesKey("alarmClockHour")
    private val alarmClockMinuteKey = intPreferencesKey("alarmClockMinute")
    private val alarmClockEditedKey = booleanPreferencesKey("alarmClockEdited")
    private val vibrateKey = booleanPreferencesKey("vibrate")
    private val soundTitleKey = stringPreferencesKey("soundTitle")
    private val soundUriKey = stringPreferencesKey("soundUri")
    private val increaseVolumeGraduallyKey = booleanPreferencesKey("increaseVolumeGradually")
    private val snoozeTimeKey = intPreferencesKey("snoozeTime")
    private val darkModeKey = intPreferencesKey("darkMode")
    private val languageKey = stringPreferencesKey("language")
    private val cancelledMessageKey = stringPreferencesKey("cancelledMessage")

    fun storeCancelledMessage(cancellationMessage: String) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[cancelledMessageKey] = cancellationMessage
            }
        }
    }

    suspend fun loadCancelledMessage(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[cancelledMessageKey]
    }

    fun storeLanguage(language: String) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[languageKey] = language
            }
        }
    }

    suspend fun loadLanguage(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[languageKey]
    }

    // 0: System Default, 1: Off: 2: On
    fun storeDarkMode(isDarkModeEnabled: Int) {
        if (isDarkModeEnabled > 2 || isDarkModeEnabled <= -1) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[darkModeKey] = isDarkModeEnabled
            }
        }
    }

    // 0: System Default, 1: Off: 2: On
    suspend fun loadDarkMode(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[darkModeKey]
    }
    fun storeSnoozeTime(snoozeTime: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[snoozeTimeKey] = snoozeTime
            }
        }
    }

    suspend fun loadSnoozeTime(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[snoozeTimeKey]
    }
    fun storeIncreaseVolumeGradually(vibrate: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[increaseVolumeGraduallyKey] = vibrate
            }
        }
    }

    suspend fun loadIncreaseVolumeGradually(): Boolean? {
        val preferences = context.dataStore.data.first()
        return preferences[vibrateKey]
    }

    fun storeVibrate(vibrate: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[vibrateKey] = vibrate
            }
        }
    }
    suspend fun loadVibrate(): Boolean? {
        val preferences = context.dataStore.data.first()
        return preferences[vibrateKey]
    }

    suspend fun loadSound(): Pair<String?, Uri?> {
        val preferences = context.dataStore.data.first()
        val uriStr = preferences[soundUriKey]
        val uri: Uri = if (uriStr == null) {
            ALARM_SOUND_DEFAULT_URI
        } else {
            Uri.parse(uriStr)
        }

        return Pair(
            preferences[soundTitleKey],
            uri
        )
    }

    fun storeSound(soundTitle: String, soundUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[soundTitleKey] = soundTitle
                settings[soundUriKey] = soundUri.toString()
            }
        }
    }

    suspend fun loadID(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[idKey]
    }

    suspend fun loadLoginData(): Array<String?> {
        val preferences = context.dataStore.data.first()
        return arrayOf(
            preferences[usernameKey],
            preferences[passwordKey],
            preferences[serverKey],
            preferences[schoolKey]
        )
    }

    suspend fun loadTBS(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[timeBeforeSchoolKey]
    }

    suspend fun loadAlarmActive(): Boolean? {
        val preferences = context.dataStore.data.first()
        return preferences[alarmActiveKey]
    }


    suspend fun loadAlarmClock(): Pair<LocalDateTime?, Boolean> {
        val preferences = context.dataStore.data.first()
        val year = preferences[alarmClockYearKey]
        val month = preferences[alarmClockMonthKey]
        val day = preferences[alarmClockDayKey]
        val hour = preferences[alarmClockHourKey]
        val minute = preferences[alarmClockMinuteKey]
        val edited: Boolean = preferences[alarmClockEditedKey] == true

        if (year == -1) return Pair(null, edited)

        if (year == null || month == null || day == null || hour == null || minute == null) {
            return Pair(null, edited)
        }

        return Pair(LocalDateTime.of(year, month, day, hour, minute), edited)
    }
    fun storeAlarmClock(alarmClockDayTime: LocalDateTime) {
        storeAlarmClock(alarmClockDayTime, false)
    }
    fun storeAlarmClock(alarmClockDayTime: LocalDateTime, edited: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[alarmClockYearKey] = alarmClockDayTime.year
                settings[alarmClockMonthKey] = alarmClockDayTime.monthValue
                settings[alarmClockDayKey] = alarmClockDayTime.dayOfMonth
                settings[alarmClockHourKey] = alarmClockDayTime.hour
                settings[alarmClockMinuteKey] = alarmClockDayTime.minute
                settings[alarmClockEditedKey] = edited
            }
        }
    }

    fun storeID(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[idKey] = id
            }
        }
    }

    fun storeLoginData(username: String, password: String, server: String, school: String) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[usernameKey] = username
                settings[passwordKey] = password
                settings[serverKey] = server
                settings[schoolKey] = school
            }
        }
    }

    fun storeTBS(timeBeforeSchool: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[timeBeforeSchoolKey] = timeBeforeSchool
            }
        }
    }

    fun storeAlarmActive(aa: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[alarmActiveKey] = aa
            }
        }
    }

}