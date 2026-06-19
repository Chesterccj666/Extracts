package com.lihao.extracts.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val WIDGET_REFRESH_HOUR = intPreferencesKey("widget_refresh_hour")
}

class SettingsManager(private val context: Context) {

    val widgetRefreshHour: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SettingsKeys.WIDGET_REFRESH_HOUR] ?: 8
    }

    suspend fun setWidgetRefreshHour(hour: Int) {
        context.dataStore.edit { preferences ->
            preferences[SettingsKeys.WIDGET_REFRESH_HOUR] = hour
        }
    }
}
