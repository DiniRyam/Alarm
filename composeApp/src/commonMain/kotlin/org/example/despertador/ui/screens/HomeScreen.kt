package org.example.despertador.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
import org.example.despertador.ui.components.*
import org.example.despertador.utils.calculateNextAlarmTime
import org.example.despertador.utils.formatDuration

@Composable
fun MainHomeScreen(alarms: SnapshotStateList<AlarmData>, onTriggerAlarm: (AlarmData) -> Unit) {
    var showModal by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<AlarmData?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val blurRadius by animateDpAsState(targetValue = if (showModal) 15.dp else 0.dp, label = "blurAnimation")

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) { 
        Box(modifier = Modifier.blur(blurRadius)) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                floatingActionButton = {
                    val fabInteractionSource = remember { MutableInteractionSource() }
                    val isFabPressed by fabInteractionSource.collectIsPressedAsState()
                    val fabScale by animateFloatAsState(if (isFabPressed) 0.9f else 1.0f, label = "fab_scale")

                    Surface(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = fabScale
                                scaleY = fabScale
                            }
                            .size(65.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = fabInteractionSource,
                                indication = null,
                                enabled = !showModal
                            ) {
                                coroutineScope.launch {
                                    delay(120)
                                    editingAlarm = null
                                    showModal = true
                                }
                            },
                        color = Color(0xFFFF5252).copy(alpha = 0.4f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text("+", color = Color.White, fontSize = 40.sp) }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TimeWidget()
                    Text("Alarmes", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))

                    if (alarms.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhum alarme adicionado",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(alarms, key = { it.id }) { alarm ->
                                AlarmCard(
                                    alarm = alarm,
                                    onEnabledChange = { isEnabled ->
                                        val index = alarms.indexOfFirst { it.id == alarm.id }
                                        if (index != -1) alarms[index] = alarm.copy(isEnabled = isEnabled)
                                    },
                                    onDelete = { alarms.remove(alarm) },
                                    onEditClick = {
                                        editingAlarm = alarm
                                        showModal = true
                                    },
                                    onTriggerAlarm = onTriggerAlarm
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showModal,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            ConfigAlarmModal(
                alarm = editingAlarm,
                onSave = { name, days, snooze, vib, newTime, newPeriod ->
                    if (editingAlarm != null) {
                        val index = alarms.indexOfFirst { it.id == editingAlarm!!.id }
                        if (index != -1) {
                            alarms[index] = editingAlarm!!.copy(
                                label = name,
                                selectedDays = days,
                                snoozeTime = snooze,
                                vibrationEnabled = vib,
                                time = newTime,
                                period = newPeriod
                            )
                        }
                    } else {
                        alarms.add(AlarmData(
                            time = newTime,
                            period = newPeriod,
                            label = name.ifBlank { "Alarme" },
                            isEnabled = true,
                            selectedDays = days,
                            snoozeTime = snooze,
                            vibrationEnabled = vib
                        ))
                    }
                    showModal = false
                    editingAlarm = null
                },
                onCancel = {
                    showModal = false
                    editingAlarm = null
                }
            )
        }
    }
}

@Composable
fun ConfigAlarmModal(
    alarm: AlarmData?,
    onSave: (name: String, days: Set<Int>, snooze: String, vib: Boolean, newTime: String, newPeriod: String) -> Unit,
    onCancel: () -> Unit
) {
    val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val initialTime = alarm?.time?.split(":")
    val initialHour = initialTime?.getOrNull(0) ?: now.hour.let { h -> if (h == 0 || h == 12) 12 else h % 12 }.toString().padStart(2, '0')
    val initialMinute = initialTime?.getOrNull(1) ?: now.minute.toString().padStart(2, '0')
    val initialPeriod = alarm?.period ?: if (now.hour < 12) "AM" else "PM"

    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    var selectedPeriod by remember { mutableStateOf(initialPeriod) }

    var alarmName by remember { mutableStateOf(alarm?.label ?: "") }
    var selectedDays by remember { mutableStateOf(alarm?.selectedDays ?: emptySet()) }
    var snoozeTime by remember { mutableStateOf(alarm?.snoozeTime ?: "5 min") }
    var vibrationEnabled by remember { mutableStateOf(alarm?.vibrationEnabled ?: true) }

    var timeToAlarmText by remember { mutableStateOf("") }

    LaunchedEffect(selectedHour, selectedMinute, selectedPeriod, selectedDays) {
        val nextAlarmTime = calculateNextAlarmTime(selectedHour, selectedMinute, selectedPeriod, selectedDays)
        val nowInstant = kotlin.time.Clock.System.now()
        val duration = nextAlarmTime?.let { it - nowInstant }
        timeToAlarmText = formatDuration(duration)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onCancel() })

        GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                VibrationGlassButton(enabled = vibrationEnabled, onClick = { vibrationEnabled = !vibrationEnabled })
                Text("Configurar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    "OK",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            onSave(alarmName, selectedDays, snoozeTime, vibrationEnabled, "$selectedHour:$selectedMinute", selectedPeriod)
                        }
                        .padding(8.dp)
                )
            }
            Text(timeToAlarmText, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 12.dp))

            // SELETOR DE TEMPO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Surface(modifier = Modifier.fillMaxWidth(0.9f).height(50.dp), color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))) {}

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    InfiniteWheelColumn(
                        items = (1..12).map { it.toString().padStart(2, '0') },
                        initialValue = selectedHour,
                        onValueChange = { selectedHour = it },
                        modifier = Modifier.width(60.dp)
                    )

                    Text(":", color = Color.White, fontSize = 28.sp, modifier = Modifier.padding(horizontal = 8.dp))

                    InfiniteWheelColumn(
                        items = (0..59).map { it.toString().padStart(2, '0') },
                        initialValue = selectedMinute,
                        onValueChange = { selectedMinute = it },
                        modifier = Modifier.width(60.dp)
                    )

                    Spacer(modifier = Modifier.width(15.dp))

                    FiniteWheelColumn(
                        items = listOf("AM", "PM"),
                        initialValue = selectedPeriod,
                        onValueChange = { selectedPeriod = it },
                        modifier = Modifier.width(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = alarmName,
                onValueChange = { alarmName = it },
                placeholder = { Text("Nome do alarme...", color = Color.White.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth().animateContentSize().clip(RoundedCornerShape(12.dp)),
                maxLines = 4,
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White.copy(alpha = 0.1f), unfocusedContainerColor = Color.White.copy(alpha = 0.1f), focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text("Repetir", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            DaySelector(
                selectedDays = selectedDays,
                onDaysChange = { selectedDays = it }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text("Soneca", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            SnoozeSelector(
                selectedSnooze = snoozeTime,
                onSnoozeChange = { snoozeTime = it }
            )
        }
    }
}
