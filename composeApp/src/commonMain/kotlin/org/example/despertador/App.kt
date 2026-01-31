package org.example.despertador

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.launch
import org.example.despertador.data.AlarmRepository
import org.example.despertador.models.AlarmData
import org.example.despertador.models.Screen
import org.example.despertador.ui.screens.MainHomeScreen
import org.example.despertador.ui.screens.WakeUpScreen

@Composable
fun AppNavigation(dataStore: DataStore<Preferences>) {
    val repository = remember { AlarmRepository(dataStore) }
    val coroutineScope = rememberCoroutineScope()

    // Estado que controla qual tela está ativa
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // Observa os alarmes salvos no DataStore
    val savedAlarms by repository.alarmsFlow.collectAsState(initial = emptyList())

    // A lista de alarmes que a UI vai usar e modificar
    val alarms = remember { mutableStateListOf<AlarmData>() }

    // Sincroniza a lista da UI com os dados guardados ao iniciar e quando houver mudanças externas
    LaunchedEffect(savedAlarms) {
        if (alarms.isEmpty() && savedAlarms.isNotEmpty()) {
            alarms.addAll(savedAlarms)
        }
    }

    // Função auxiliar para salvar a lista atual de alarmes no DataStore
    val saveChanges = {
        coroutineScope.launch {
            repository.saveAlarms(alarms.toList())
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val screen = currentScreen) {
            is Screen.Home -> {
                MainHomeScreen(
                    alarms = alarms,
                    onTriggerAlarm = { alarm ->
                        currentScreen = Screen.WakeUp(alarm)
                    },
                    onSaveAlarm = { alarmToSave ->
                        // Lógica para adicionar ou atualizar um alarme
                        val index = alarms.indexOfFirst { it.id == alarmToSave.id }
                        if (index != -1) {
                            alarms[index] = alarmToSave // Atualiza existente
                        } else {
                            alarms.add(alarmToSave) // Adiciona novo
                        }
                        saveChanges()
                    },
                    onDelete = { alarmToDelete ->
                        alarms.remove(alarmToDelete)
                        saveChanges()
                    }
                )
            }
            is Screen.WakeUp -> {
                WakeUpScreen(
                    alarm = screen.alarm,
                    onSnooze = {
                        // Lógica de soneca: volta para home
                        currentScreen = Screen.Home
                    },
                    onDismiss = {
                        // Desliga alarme: volta para home
                        currentScreen = Screen.Home
                    }
                )
            }
        }
    }
}
