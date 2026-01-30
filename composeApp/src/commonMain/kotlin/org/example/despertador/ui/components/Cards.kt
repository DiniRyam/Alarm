package org.example.despertador.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.despertador.models.AlarmData

@Composable
fun TimeWidget() {
    var now by remember { mutableStateOf(kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) }

    LaunchedEffect(Unit) {
        while (true) {
            now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            delay(1000) // Atualiza a cada segundo
        }
    }

    val hour12 = now.hour.let { h -> if (h == 0 || h == 12) 12 else h % 12 }
    val minute = now.minute.toString().padStart(2, '0')
    val amPm = if (now.hour < 12) "AM" else "PM"

    // Formatando a data de forma mais compatível com KMM
    val day = now.dayOfMonth
    val month = now.month.name.lowercase().replaceFirstChar { it.titlecase() }.take(3)
    val dayOfWeek = now.dayOfWeek.name.lowercase().replaceFirstChar { it.titlecase() }.take(3)
    val year = now.year
    val formattedDate = "$dayOfWeek, $month $year"

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1.0f, label = "time_widget_scale")

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("$day", color = Color.White, fontSize = 60.sp, fontWeight = FontWeight.Bold, lineHeight = 60.sp)
                Text(formattedDate, color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
            }
            Text("$hour12:$minute $amPm", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Light)
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("☀️ 05:42 AM  |  🌙 18:15 PM", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlarmCard(
    alarm: AlarmData,
    onEnabledChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEditClick: () -> Unit,
    onTriggerAlarm: (AlarmData) -> Unit
) {
    val allDays = listOf("D", "S", "T", "Q", "Q", "S", "S")
    var isDeleteMode by remember { mutableStateOf(false) }
    val cardInteractionSource = remember { MutableInteractionSource() }
    val isCardPressed by cardInteractionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(if (isCardPressed && !isDeleteMode) 0.98f else 1.0f, label = "alarm_card_scale")

    GlassCard(
        modifier = Modifier
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(
                interactionSource = cardInteractionSource,
                indication = null, // Remove o ripple quadrado
                onClick = {
                    if (isDeleteMode) {
                        isDeleteMode = false
                    } else if (alarm.isEnabled) {
                        onTriggerAlarm(alarm)
                    } else {
                        onEditClick()
                    }
                },
                onLongClick = { isDeleteMode = true }
            )
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    allDays.forEachIndexed { index, day ->
                        val isSelected = alarm.selectedDays.contains(index)
                        Text(
                            text = day,
                            color = if (isSelected) Color(0xFFBB86FC) else Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(alarm.time, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(alarm.period, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp, start = 2.dp))
                }
                Text(alarm.label, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
            }

            if (isDeleteMode) {
                val coroutineScope = rememberCoroutineScope()
                val deleteInteractionSource = remember { MutableInteractionSource() }
                val isDeletePressed by deleteInteractionSource.collectIsPressedAsState()
                val deleteScale by animateFloatAsState(if (isDeletePressed) 0.9f else 1.0f, label = "delete_button_scale")

                Surface(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = deleteScale
                            scaleY = deleteScale
                        }
                        .size(45.dp)
                        .clickable(interactionSource = deleteInteractionSource, indication = null) {
                            coroutineScope.launch {
                                delay(120) // Espera a animação de clique acontecer
                                onDelete()
                            }
                        },
                    color = Color(0xFFFF5252).copy(alpha = 0.8f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                ) { Box(contentAlignment = Alignment.Center) { Text("✕", color = Color.White, fontWeight = FontWeight.Bold) } }
            } else {
                GlassToggle(checked = alarm.isEnabled, onCheckedChange = onEnabledChange)
            }
        }
    }
}
