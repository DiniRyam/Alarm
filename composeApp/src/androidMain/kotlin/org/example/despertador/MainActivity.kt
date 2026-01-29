package org.example.despertador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

// --- MODELO DE DADOS ---
data class AlarmData(
    val id: String = UUID.randomUUID().toString(),
    val time: String,
    val period: String,
    val label: String,
    val isEnabled: Boolean,
    val selectedDays: Set<Int>,
    val snoozeTime: String = "5m", // Padrão agora é 5m
    val vibrationEnabled: Boolean = true
)

// --- CONFIGURAÇÃO VISUAL BASE ---
val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.15f)
val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.3f)

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.animateContentSize(), // Permite que o card cresça suavemente
        color = GlassWhite,
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(24.dp), content = content)
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun WheelItem(value: String, isSelected: Boolean) {
    Box(modifier = Modifier.height(50.dp).width(45.dp), contentAlignment = Alignment.Center) {
        Text(
            text = value,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
            fontSize = if (isSelected) 22.sp else 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun VibrationGlassButton(enabled: Boolean, onClick: () -> Unit) {
    val glowColor by animateColorAsState(if (enabled) Color(0xFFFFD700).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f))
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1.0f)

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
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
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
    val days = listOf("D", "S", "T", "Q", "Q", "S", "S")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEachIndexed { index, day ->
            val isSelected = selectedDays.contains(index)
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(if (isPressed) 0.9f else 1.0f)

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
                        val newSelection = if (isSelected) selectedDays - index else selectedDays + index
                        onDaysChange(newSelection)
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
    val options = listOf("5m", "10m", "15m", "20m", "25m", "30m")

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { time ->
            val isSelected = selectedSnooze == time
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(if (isPressed) 0.9f else 1.0f)

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .weight(1f).height(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFFBB86FC).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f))
                    .border(1.dp, if (isSelected) Color(0xFFBB86FC) else Color.Transparent, RoundedCornerShape(12.dp))
                    .clickable(interactionSource = interactionSource, indication = null) { onSnoozeChange(time) },
                contentAlignment = Alignment.Center
            ) {
                Text(time, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun GlassToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val trackColor by animateColorAsState(if (checked) Color(0xFF4ADE80) else Color.DarkGray.copy(alpha = 0.4f))
    val thumbOffset by animateDpAsState(targetValue = if (checked) 27.dp else 3.dp)
    Box(modifier = Modifier.width(56.dp).height(32.dp).clip(CircleShape).clickable { onCheckedChange(!checked) }) {
        Box(modifier = Modifier.align(Alignment.Center).fillMaxWidth().height(20.dp).padding(horizontal = 6.dp).clip(CircleShape).background(trackColor))
        Surface(
            modifier = Modifier.size(26.dp).offset(x = thumbOffset).align(Alignment.CenterStart),
            shape = CircleShape, color = Color.White.copy(alpha = 0.35f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
        ) { Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)))) }
    }
}

@Composable
fun TimeWidget() {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) { while (true) { currentTime = Calendar.getInstance(); delay(1000) } }
    val hour = currentTime.get(Calendar.HOUR).let { if (it == 0) 12 else it }
    val minute = currentTime.get(Calendar.MINUTE).toString().padStart(2, '0')
    val amPm = if (currentTime.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
    val day = currentTime.get(Calendar.DAY_OF_MONTH)
    val formattedDate = SimpleDateFormat("EEE, MMM yyyy", Locale.getDefault()).format(currentTime.time)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    GlassCard(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically // Centraliza a hora e a data
        ) {
            Column {
                Text("$day", color = Color.White, fontSize = 60.sp, fontWeight = FontWeight.Bold, lineHeight = 60.sp)
                Text(formattedDate, color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
            }
            Text("$hour:$minute $amPm", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Light)
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("☀️ 05:42 AM  |  🌙 18:15 PM", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AlarmCard(
    alarm: AlarmData,
    onEnabledChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEditClick: () -> Unit
) {
    val allDays = listOf("D", "S", "T", "Q", "Q", "S", "S")
    var isDeleteMode by remember { mutableStateOf(false) }
    val cardInteractionSource = remember { MutableInteractionSource() }
    val isCardPressed by cardInteractionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(if (isCardPressed && !isDeleteMode) 0.98f else 1.0f)

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
                onClick = { if (isDeleteMode) isDeleteMode = false else onEditClick() },
                onLongClick = { isDeleteMode = true }
            )
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                // LINHA DOS DIAS DA SEMANA (Acima da hora)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    allDays.forEachIndexed { index, day ->
                        val isSelected = alarm.selectedDays.contains(index)
                        Text(
                            text = day,
                            // ROXO se selecionado, BRANCO opaco se não
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
                val deleteScale by animateFloatAsState(if (isDeletePressed) 0.9f else 1.0f)

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
                    color = Color(0xFFFF5252).copy(alpha = 0.8f), shape = CircleShape, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                ) { Box(contentAlignment = Alignment.Center) { Text("✕", color = Color.White, fontWeight = FontWeight.Bold) } }
            } else {
                GlassToggle(checked = alarm.isEnabled, onCheckedChange = onEnabledChange)
            }
        }
    }
}

// --- MODAL DE CONFIGURAÇÃO ---

@Composable
fun ConfigAlarmModal(
    alarm: AlarmData?,
    onSave: (String, Set<Int>, String, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var alarmName by remember { mutableStateOf(alarm?.label ?: "") }
    var selectedDays by remember { mutableStateOf(alarm?.selectedDays ?: emptySet()) }
    var snoozeTime by remember { mutableStateOf(alarm?.snoozeTime ?: "5m") }
    var vibrationEnabled by remember { mutableStateOf(alarm?.vibrationEnabled ?: true) }

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
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSave(alarmName, selectedDays, snoozeTime, vibrationEnabled) }
                        .padding(8.dp)
                )
            }
            Text("Toca em 7h 30min", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 12.dp))

            // SELETOR SIMULADO
            Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.fillMaxWidth(0.9f).height(50.dp), color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))) {}
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column { WheelItem("05", false); WheelItem("06", true); WheelItem("07", false) }
                    Text(":", color = Color.White, fontSize = 28.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    Column { WheelItem("55", false); WheelItem("00", true); WheelItem("05", false) }
                    Spacer(modifier = Modifier.width(15.dp))
                    Column { WheelItem("", false); WheelItem("AM", true); WheelItem("PM", false) }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = alarmName, onValueChange = { alarmName = it },
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

// --- TELA PRINCIPAL ---

@Preview(showBackground = true)
@Composable
fun MainLayoutPreview() {
    var showModal by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<AlarmData?>(null) }
    val alarms = remember {
        mutableStateListOf(
            AlarmData(time = "06:00", period = "AM", label = "Academia", isEnabled = true, selectedDays = setOf(1, 2, 3, 4, 5)),
            AlarmData(time = "08:30", period = "AM", label = "Trabalho", isEnabled = false, selectedDays = setOf(0, 6))
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A),
        floatingActionButton = {
            if (!showModal) {
                val fabInteractionSource = remember { MutableInteractionSource() }
                val isFabPressed by fabInteractionSource.collectIsPressedAsState()
                val fabScale by animateFloatAsState(if (isFabPressed) 0.9f else 1.0f)
                val coroutineScope = rememberCoroutineScope()

                Surface(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = fabScale
                            scaleY = fabScale
                        }
                        .size(65.dp)
                        .clickable(interactionSource = fabInteractionSource, indication = null) {
                            coroutineScope.launch {
                                delay(120)
                                editingAlarm = null
                                showModal = true
                            }
                        },
                    color = Color(0xFFFF5252).copy(alpha = 0.4f), // Efeito de vidro com mais transparência
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("+", color = Color.White, fontSize = 40.sp) }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).then(if (showModal) Modifier.blur(15.dp) else Modifier)) {
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
                                onDelete = { alarms.removeIf { it.id == alarm.id } },
                                onEditClick = {
                                    editingAlarm = alarm
                                    showModal = true
                                }
                            )
                        }
                    }
                }
            }
            if (showModal) {
                ConfigAlarmModal(
                    alarm = editingAlarm,
                    onSave = { name, days, snooze, vib ->
                        if (editingAlarm != null) {
                            val index = alarms.indexOfFirst { it.id == editingAlarm!!.id }
                            if (index != -1) {
                                alarms[index] = editingAlarm!!.copy(
                                    label = name,
                                    selectedDays = days,
                                    snoozeTime = snooze,
                                    vibrationEnabled = vib
                                )
                            }
                        } else {
                            alarms.add(AlarmData(
                                time = "10:00", period = "AM", label = name,
                                isEnabled = true, selectedDays = days,
                                snoozeTime = snooze, vibrationEnabled = vib
                            ))
                        }
                        showModal = false
                        editingAlarm = null // Limpa o estado de edição
                    },
                    onCancel = {
                        showModal = false
                        editingAlarm = null // Limpa o estado de edição
                    }
                )
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { MainLayoutPreview() }
    }
}
