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

private val TAG = "StoreData"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("userToken")
class StoreData (private val context: Context) {

    private val ID = intPreferencesKey("id")
    private val USERNAME = stringPreferencesKey("username")
    private val PASSWORD = stringPreferencesKey("password")
    private val SERVER = stringPreferencesKey("server")
    private val SCHOOL = stringPreferencesKey("school")
    private val TBS = intPreferencesKey("tbs")
    private val ALARMACTIVE = booleanPreferencesKey("alarmactive")


    suspend fun loadID(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[ID]
    }

    suspend fun storeID(id: Int) {
        context.dataStore.edit { settings ->
            settings[ID] = id
        }
    }

    suspend fun loadLoginData(): Array<String?> {
        val preferences = context.dataStore.data.first()
        return arrayOf(
            preferences[USERNAME],
            preferences[PASSWORD],
            preferences[SERVER],
            preferences[SCHOOL]
        )
    }

    suspend fun storeLoginData(username: String, password: String, server: String, school: String) {
        context.dataStore.edit { settings ->
            settings[USERNAME] = username
            settings[PASSWORD] = password
            settings[SERVER] = server
            settings[SCHOOL] = school
        }
    }

    suspend fun loadTBS(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[TBS]
    }

    suspend fun storeTBS(timeBeforeSchool: Int) {
        context.dataStore.edit { settings ->
            settings[TBS] = timeBeforeSchool
        }
    }

    suspend fun loadAlarmActive(): Boolean? {
        val preferences = context.dataStore.data.first()
        return preferences[ALARMACTIVE] 
    }

    suspend fun storeAlarmAcitive(aa: Boolean) {
        context.dataStore.edit { settings ->
            settings[ALARMACTIVE] = aa
        }
    }

}