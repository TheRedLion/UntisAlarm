package com.carlkarenfort.test

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class StoreData (private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("userToken")
    val ID = intPreferencesKey("id")
    val USERNAME = stringPreferencesKey("username")
    val PASSWORD = stringPreferencesKey("password")
    val SERVER = stringPreferencesKey("server")
    val SCHOOL = stringPreferencesKey("school")
    val TBS = intPreferencesKey("tbs")
    val ALARMACTIVE = booleanPreferencesKey("alarmactive")


    public suspend fun loadID(): Int {
        val id: Int = 0
        val exampleCounterFlow: Flow<Int> = context.dataStore.data
            .map { preferences ->
                // No type safety.
                preferences[ID] ?: 0
            }
        return id
    }

    suspend fun storeID(id: Int) {
        context.dataStore.edit {settings ->
            settings[ID] = id
        }
    }

    suspend fun loadLoginData(): Array<String?> {

        /*
        val exampleCounterFlow: Flow<Unit> = context.dataStore.data
            .map { preferences ->
                // No type safety.
                preferences[USERNAME] ?: ""
                preferences[PASSWORD] ?: ""
                preferences[SERVER] ?: "ehile"
                preferences[SCHOOL] ?: ""
            }
        */

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
        val tbs: Int = 0
        val exampleCounterFlow: Flow<Int> = context.dataStore.data
            .map { preferences ->
                // No type safety.
                preferences[TBS] ?: 0
            }
        return tbs
    }

    suspend fun storeTBS(tbs: Int) {
        context.dataStore.edit {settings ->
            settings[TBS] = tbs
        }
    }

    suspend fun loadAlarmActive(): Boolean? {
        val alarmActive: Boolean = false
        val exampleCounterFlow: Flow<Boolean> = context.dataStore.data
            .map { preferences ->
                // No type safety.
                preferences[ALARMACTIVE] ?: false
            }
        return alarmActive
    }

    suspend fun storeAlarmActive(state: Boolean) {
        context.dataStore.edit {settings ->
            settings[ALARMACTIVE] = state
        }
    }

}