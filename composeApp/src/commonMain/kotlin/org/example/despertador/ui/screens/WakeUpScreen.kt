package org.example.despertador.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.despertador.models.AlarmData

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WakeUpScreen(
    alarm: AlarmData,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    // Animação de pulso para o relógio
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    val coroutineScope = rememberCoroutineScope()

    // Animações dos botões
    val snoozeInteractionSource = remember { MutableInteractionSource() }
    val isSnoozePressed by snoozeInteractionSource.collectIsPressedAsState()
    val snoozeScale by animateFloatAsState(if (isSnoozePressed) 0.95f else 1.0f, label = "snooze_scale")

    val dismissInteractionSource = remember { MutableInteractionSource() }
    val isDismissPressed by dismissInteractionSource.collectIsPressedAsState()
    val dismissScale by animateFloatAsState(if (isDismissPressed) 0.9f else 1.0f, label = "dismiss_scale")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight().padding(vertical = 80.dp, horizontal = 32.dp)
        ) {
            Text(
                text = alarm.label.ifBlank { "ALARME" }.uppercase(),
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold
            )

            // Relógio Pulsante
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
            ) {
                Text(text = alarm.time, color = Color.White, fontSize = 90.sp, fontWeight = FontWeight.ExtraLight)
                Text(text = alarm.period, color = Color.White.copy(alpha = 0.5f), fontSize = 20.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Botão de Soneca (Toque Simples)
                Surface(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = snoozeScale
                            scaleY = snoozeScale
                        }
                        .fillMaxWidth()
                        .height(65.dp)
                        .clickable(
                            interactionSource = snoozeInteractionSource,
                            indication = null
                        ) {
                            coroutineScope.launch {
                                delay(120)
                                onSnooze()
                            }
                        },
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Soneca ${alarm.snoozeTime}", color = Color.White, fontSize = 18.sp)
                    }
                }

                // Botão de Desligar (Segurar para Desligar)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Segure para desligar", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Surface(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = dismissScale
                                scaleY = dismissScale
                            }
                            .fillMaxWidth()
                            .height(65.dp)
                            .combinedClickable(
                                interactionSource = dismissInteractionSource,
                                indication = null,
                                onClick = {},
                                onLongClick = { onDismiss() }
                            ),
                        color = Color(0xFFFF5252).copy(alpha = 0.2f),
                        shape = CircleShape,
                        border = BorderStroke(2.dp, Color(0xFFFF5252).copy(alpha = 0.6f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("PARAR", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}
