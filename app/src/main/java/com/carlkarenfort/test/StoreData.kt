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


    suspend fun loadID(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[idKey]
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