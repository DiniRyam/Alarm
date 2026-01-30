package org.example.despertador.models

/**
 * Representa as diferentes telas do aplicativo para navegação.
 */
sealed class Screen {
    object Home : Screen()
    data class WakeUp(val alarm: AlarmData) : Screen()
}
