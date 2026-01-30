package org.example.despertador.utils

import kotlinx.datetime.*
import kotlin.time.Duration

/**
 * Calcula a próxima data e hora em que um alarme deve tocar.
 */
fun calculateNextAlarmTime(
    selectedHour: String,
    selectedMinute: String,
    selectedPeriod: String,
    selectedDays: Set<Int>
): Instant? {
    // CORREÇÃO: Clock.System.now() é o caminho correto
    val nowInstant = Clock.System.now()
    val timeZone = TimeZone.currentSystemDefault()
    val now = nowInstant.toLocalDateTime(timeZone)

    val hour12 = selectedHour.toIntOrNull() ?: return null
    val minute = selectedMinute.toIntOrNull() ?: return null
    val isPm = selectedPeriod.equals("PM", ignoreCase = true)

    val hour24 = when {
        isPm && hour12 < 12 -> hour12 + 12
        !isPm && hour12 == 12 -> 0
        else -> hour12
    }

    var alarmDateTime = LocalDateTime(now.year, now.month, now.dayOfMonth, hour24, minute)

    if (selectedDays.isEmpty()) {
        if (alarmDateTime <= now) {
            // CORREÇÃO: Forma correta de adicionar 1 dia em LocalDateTime
            val tomorrow = nowInstant.plus(1, DateTimeUnit.DAY, timeZone).toLocalDateTime(timeZone)
            alarmDateTime = LocalDateTime(tomorrow.year, tomorrow.month, tomorrow.dayOfMonth, hour24, minute)
        }
    } else {
        // CORREÇÃO: Ajuste do mapeamento (kotlinx-datetime usa 1 para Segunda e 7 para Domingo)
        // Se seu Set<Int> é 0=Dom, 1=Seg...
        val isoSelectedDays = selectedDays.map { if (it == 0) 7 else it }.toSet()

        var found = false
        for (i in 0..7) {
            val candidateInstant = nowInstant.plus(i, DateTimeUnit.DAY, timeZone)
            val candidateDate = candidateInstant.toLocalDateTime(timeZone).date
            val candidateDateTime = LocalDateTime(candidateDate.year, candidateDate.month, candidateDate.dayOfMonth, hour24, minute)

            if (candidateDateTime > now && candidateDate.dayOfWeek.isoDayNumber in isoSelectedDays) {
                alarmDateTime = candidateDateTime
                found = true
                break
            }
        }
        if (!found) return null
    }

    return alarmDateTime.toInstant(timeZone)
}

/**
 * Formata uma Duração em um texto legível.
 */
fun formatDuration(duration: Duration?): String {
    if (duration == null || duration <= Duration.ZERO) return ""

    return duration.toComponents { days, hours, minutes, _, _ ->
        val parts = mutableListOf<String>()
        if (days > 0) parts.add("${days}d")
        if (hours > 0) parts.add("${hours}h")
        if (minutes > 0 || parts.isEmpty()) parts.add("${minutes}min")

        "Toca em " + parts.joinToString(" ")
    }
}