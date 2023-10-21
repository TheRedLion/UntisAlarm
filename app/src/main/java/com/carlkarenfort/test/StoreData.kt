package com.carlkarenfort.test

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("userData")
class StoreData (private val context: Context) {
    private val idKey = intPreferencesKey("id")
    private val usernameKey = stringPreferencesKey("username")
    private val passwordKey = stringPreferencesKey("password")
    private val serverKey = stringPreferencesKey("server")
    private val schoolKey = stringPreferencesKey("school")
    private val timeBeforeSchoolKey = intPreferencesKey("tbs")
    private val alarmActiveKey = booleanPreferencesKey("alarm-active")
    private val alarmClockHourKey = intPreferencesKey("alarmClockHour")
    private val alarmClockMinuteKey = intPreferencesKey("alarmClockMinute")

    suspend fun loadID(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[idKey]
    }

    suspend fun loadAlarmClock(): Array<Int?> {
        val preferences = context.dataStore.data.first()
        if (preferences[alarmClockHourKey] == 27 || preferences[alarmClockMinuteKey] == 69) {
            return arrayOf(null, null)
        }
        return arrayOf(
            preferences[alarmClockHourKey],
            preferences[alarmClockMinuteKey]
        )
    }

    suspend fun storeAlarmClock(hour: Int?, minute: Int?) {
        val storeHour: Int = hour ?: 27
        val storeMinute: Int = minute ?: 69

        context.dataStore.edit { settings ->
            settings[alarmClockHourKey] = storeHour
            settings[alarmClockMinuteKey] = storeMinute
        }
    }

    suspend fun storeID(id: Int) {
        context.dataStore.edit { settings ->
            settings[idKey] = id
        }
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

    suspend fun storeLoginData(username: String, password: String, server: String, school: String) {
        context.dataStore.edit { settings ->
            settings[usernameKey] = username
            settings[passwordKey] = password
            settings[serverKey] = server
            settings[schoolKey] = school
        }
    }

    suspend fun loadTBS(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[timeBeforeSchoolKey]
    }

    suspend fun storeTBS(timeBeforeSchool: Int) {
        context.dataStore.edit { settings ->
            settings[timeBeforeSchoolKey] = timeBeforeSchool
        }
    }

    suspend fun loadAlarmActive(): Boolean? {
        val preferences = context.dataStore.data.first()
        return preferences[alarmActiveKey]
    }

    suspend fun storeAlarmActive(aa: Boolean) {
        context.dataStore.edit { settings ->
            settings[alarmActiveKey] = aa
        }
    }

}