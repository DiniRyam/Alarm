package org.example.despertador

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import org.example.despertador.models.AlarmData
import org.example.despertador.models.Screen
import org.example.despertador.ui.screens.MainHomeScreen
import org.example.despertador.ui.screens.WakeUpScreen

@Composable
fun AppNavigation() {
    // Estado que controla qual tela está ativa
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // A lista de alarmes agora vive aqui, no topo da hierarquia de UI
    val alarms: SnapshotStateList<AlarmData> = remember {
        mutableStateListOf(
            AlarmData(time = "06:00", period = "AM", label = "Academia", isEnabled = true, selectedDays = setOf(1, 2, 3, 4, 5)),
            AlarmData(time = "08:30", period = "AM", label = "Trabalho", isEnabled = false, selectedDays = setOf(0, 6))
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val screen = currentScreen) {
            is Screen.Home -> {
                MainHomeScreen(
                    alarms = alarms,
                    onTriggerAlarm = { alarm ->
                        currentScreen = Screen.WakeUp(alarm)
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
