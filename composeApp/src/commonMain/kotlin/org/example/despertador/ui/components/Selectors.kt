package org.example.despertador.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VibrationGlassButton(enabled: Boolean, onClick: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val glowColor by animateColorAsState(if (enabled) Color(0xFFFFD700).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f))
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1.0f, label = "vibration_button_scale")

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .size(45.dp)
            .clip(CircleShape)
            .background(glowColor)
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .clickable(interactionSource = interactionSource, indication = null) {
                coroutineScope.launch {
                    delay(120)
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text("VIB", color = if (enabled) Color.White else Color.White.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DaySelector(
    selectedDays: Set<Int>,
    onDaysChange: (Set<Int>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val days = listOf("D", "S", "T", "Q", "Q", "S", "S") // Domingo a Sábado
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEachIndexed { index, day ->
            val isSelected = selectedDays.contains(index)
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(if (isPressed) 0.9f else 1.0f, label = "day_selector_scale")

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFFBB86FC).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f))
                    .border(1.dp, if (isSelected) Color(0xFFBB86FC) else Color.Transparent, CircleShape)
                    .clickable(interactionSource = interactionSource, indication = null) {
                        coroutineScope.launch {
                            delay(120)
                            val newSelection = if (isSelected) selectedDays - index else selectedDays + index
                            onDaysChange(newSelection)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(day, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun SnoozeSelector(
    selectedSnooze: String,
    onSnoozeChange: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val options = listOf("5 min", "10 min", "15 min", "20 min", "25 min", "30 min")

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { time ->
            val isSelected = selectedSnooze == time
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(if (isPressed) 0.9f else 1.0f, label = "snooze_selector_scale")

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .weight(1f).height(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFFBB86FC).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f))
                    .border(1.dp, if (isSelected) Color(0xFFBB86FC) else Color.Transparent, RoundedCornerShape(12.dp))
                    .clickable(interactionSource = interactionSource, indication = null) {
                        coroutineScope.launch {
                            delay(120)
                            onSnoozeChange(time)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(time, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }
    }
}
