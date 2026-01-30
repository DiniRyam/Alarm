package org.example.despertador.models

import kotlin.random.Random

// Uma função simples para gerar um ID único em KMM sem usar bibliotecas específicas da plataforma.
fun randomUUID(): String = "${Random.nextLong()}-${Random.nextLong()}"

data class AlarmData(
    val id: String = randomUUID(),
    val time: String,
    val period: String,
    val label: String,
    val isEnabled: Boolean,
    val selectedDays: Set<Int>,
    val snoozeTime: String = "5 min",
    val vibrationEnabled: Boolean = true
)
