package eu.karenfort.main

import android.content.Context
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
    private val alarmClockHourKey = intPreferencesKey("alarmClockHour")
    private val alarmClockMinuteKey = intPreferencesKey("alarmClockMinute")
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

    // -1: System Default, 0: Off: 1: On
    fun storeDarkMode(isDarkModeEnabled: Int) {
        if (isDarkModeEnabled >= 2 || isDarkModeEnabled < -1) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[darkModeKey] = isDarkModeEnabled
            }
        }
    }

    // -1: System Default, 0: Off: 1: On
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

    suspend fun loadSound(): Array<String?> {
        val preferences = context.dataStore.data.first()
        return arrayOf(
            preferences[soundTitleKey],
            preferences[soundUriKey]
        )
    }

    fun storeSound(soundTitle: String, soundUri: String) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[soundTitleKey] = soundTitle
                settings[soundUriKey] = soundUri
            }
        }
    }

    suspend fun loadID(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[idKey]
    }

    suspend fun loadAlarmClock(): Array<Int?> {
        val preferences = context.dataStore.data.first()

        //for explanation look ar storeAlarmClock()
        if (preferences[alarmClockHourKey] == 27 || preferences[alarmClockMinuteKey] == 69) {
            return arrayOf(null, null)
        }

        return arrayOf(
            preferences[alarmClockHourKey],
            preferences[alarmClockMinuteKey]
        )
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

    fun storeAlarmClock(hour: Int?, minute: Int?) {
        if (hour != null) {
            if (hour < 0 || hour > 23) {
                throw Exception("Hour must be 0..23")
            }
        }

        if (minute != null) {
            if (minute < 0 || minute > 60) {
                throw Exception("Hour must be 0..60")
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            //I am storing 27:69 because you can't store null, the load function returns null if 27:69 was stored
            val storeHour: Int = hour ?: 27
            val storeMinute: Int = minute ?: 69

            context.dataStore.edit { settings ->
                settings[alarmClockHourKey] = storeHour
                settings[alarmClockMinuteKey] = storeMinute
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