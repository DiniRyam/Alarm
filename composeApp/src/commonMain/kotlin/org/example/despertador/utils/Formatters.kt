package org.example.despertador.utils

// kotlinx-datetime para classes de data, hora e fuso horário
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

// kotlin.time para classes de tempo como Clock e Instant
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Calcula a próxima data e hora em que um alarme deve tocar.
 * @return Um objeto Instant que representa o próximo momento do alarme.
 */
fun calculateNextAlarmTime(
    selectedHour: String,
    selectedMinute: String,
    selectedPeriod: String,
    selectedDays: Set<Int>
): Instant? {
    val timeZone = TimeZone.currentSystemDefault()
    // CORRETO: Usa o Clock padrão do Kotlin para obter a hora atual.
    val nowInstant = kotlin.time.Clock.System.now()
    val now = nowInstant.toLocalDateTime(timeZone)

    val hour12 = selectedHour.toIntOrNull() ?: return null
    val minute = selectedMinute.toIntOrNull() ?: return null
    val isPm = selectedPeriod.equals("PM", ignoreCase = true)

    val hour24 = when {
        isPm && hour12 < 12 -> hour12 + 12
        !isPm && hour12 == 12 -> 0 // Meia-noite
        else -> hour12
    }

    val alarmTime = LocalTime(hour24, minute)

    if (selectedDays.isEmpty()) {
        var alarmDateTime = LocalDateTime(now.date, alarmTime)
        if (alarmDateTime.toInstant(timeZone) <= nowInstant) {
            // Se a hora já passou hoje, agenda para o próximo dia
            val tomorrow = now.date.plus(1, DateTimeUnit.DAY)
            alarmDateTime = LocalDateTime(tomorrow, alarmTime)
        }
        return alarmDateTime.toInstant(timeZone)
    } else {
        // Mapeia os dias selecionados para o padrão ISO (1=Segunda, ..., 7=Domingo)
        val isoSelectedDays = selectedDays.map { if (it == 0) 7 else it }.toSet()

        // Itera pelos próximos 8 dias para encontrar o próximo dia de alarme
        for (i in 0..7) {
            val candidateDate = now.date.plus(i, DateTimeUnit.DAY)
            if (candidateDate.dayOfWeek.isoDayNumber in isoSelectedDays) {
                val alarmDateTime = LocalDateTime(candidateDate, alarmTime)
                // Se a data/hora do candidato for no futuro, encontramos o próximo alarme
                if (alarmDateTime.toInstant(timeZone) > nowInstant) {
                    return alarmDateTime.toInstant(timeZone)
                }
            }
        }
    }
    // Se nenhum horário futuro for encontrado, retorna nulo
    return null
}

/**
 * Formata uma Duração em um texto legível como "Toca em 1d 2h 3min".
 */
fun formatDuration(duration: Duration?): String {
    if (duration == null || duration.isNegative()) return ""

    return duration.toComponents { days, hours, minutes, _, _ ->
        // Se a duração total for menor que um minuto, retorna a mensagem especial.
        if (days == 0L && hours == 0 && minutes == 0) {
            // CORREÇÃO: Usa um retorno local para a lambda, o que é seguro para previews.
            return@toComponents "Toca em menos de um minuto"
        }

        val parts = mutableListOf<String>()
        if (days > 0) parts.add("${days}d")
        if (hours > 0) parts.add("${hours}h")
        if (minutes > 0) parts.add("${minutes}min")

        // Retorno padrão da lambda
        return@toComponents "Toca em " + parts.joinToString(" ")
    }
}
