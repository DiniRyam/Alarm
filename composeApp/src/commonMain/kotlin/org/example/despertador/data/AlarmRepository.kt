package org.example.despertador.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.despertador.models.AlarmData

class AlarmRepository(private val dataStore: DataStore<Preferences>) {
    private val ALARMS_KEY = stringPreferencesKey("alarms_list")

    // Guarda a lista de alarmes convertendo para String (JSON)
    suspend fun saveAlarms(alarms: List<AlarmData>) {
        val jsonString = Json.encodeToString(alarms)
        dataStore.edit { preferences ->
            preferences[ALARMS_KEY] = jsonString
        }
    }

    // Lê os alarmes guardados e converte de volta para Objetos
    val alarmsFlow: Flow<List<AlarmData>> = dataStore.data.map { preferences ->
        val jsonString = preferences[ALARMS_KEY] ?: return@map emptyList()
        try {
            Json.decodeFromString<List<AlarmData>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}