/**

package org.example.despertador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
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
import kotlin.math.ceil
import kotlin.math.roundToInt

// --- MODELO DE DADOS ---
data class AlarmData(
    val id: String = UUID.randomUUID().toString(),
    val time: String,
    val period: String,
    val label: String,
    val isEnabled: Boolean,
    val selectedDays: Set<Int>,
    val snoozeTime: String = "5 min",
    val vibrationEnabled: Boolean = true
)

// Novo modelo para gerenciar as telas
sealed class Screen {
    object Home : Screen()
    data class WakeUp(val alarm: AlarmData) : Screen()
}

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
    val coroutineScope = rememberCoroutineScope()
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

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1.0f)

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
                onClick = {
                    if (isDeleteMode) {
                        isDeleteMode = false
                    } else if (alarm.isEnabled) {
                        // Se o alarme estiver ligado, clicar nele dispara a tela de despertar (para teste)
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

// --- SELETOR DE TEMPO (WHEEL PICKER) ---
@Composable
fun InfiniteWheelColumn(
    items: List<String>,
    initialValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 50.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val repeatFactor = 1000

    val initialIndex = items.indexOf(initialValue).let { if (it == -1) 0 else it }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex + (items.size * (repeatFactor / 2)))

    val selectedIndex by remember {
        derivedStateOf {
            val floatIndex = (listState.firstVisibleItemIndex * itemHeightPx + listState.firstVisibleItemScrollOffset) / itemHeightPx
            val index = floatIndex.roundToInt()
            index.coerceAtLeast(0) % items.size
        }
    }

    LaunchedEffect(selectedIndex) {
        onValueChange(items[selectedIndex])
    }

    // Efeito de Snap
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val floatIndex = (listState.firstVisibleItemIndex * itemHeightPx + listState.firstVisibleItemScrollOffset) / itemHeightPx
            val targetAbsoluteIndex = floatIndex.roundToInt()
            listState.animateScrollToItem(targetAbsoluteIndex)
        }
    }

    Box(
        modifier = modifier.height(itemHeight * 3),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(items.size * repeatFactor) { index ->
                val itemIndex = index % items.size
                WheelItem(value = items[itemIndex], isSelected = (itemIndex == selectedIndex))
            }
        }
    }
}

@Composable
fun FiniteWheelColumn(
    items: List<String>,
    initialValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 50.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    val initialIndex = items.indexOf(initialValue).let { if (it == -1) 0 else it }
    val listState = rememberLazyListState(initialIndex)

    val selectedIndex by remember {
        derivedStateOf {
            val totalOffset = (listState.firstVisibleItemIndex * itemHeightPx) + listState.firstVisibleItemScrollOffset
            val index = (totalOffset / itemHeightPx).roundToInt()
            index.coerceIn(0, items.size - 1)
        }
    }

    LaunchedEffect(selectedIndex) {
        onValueChange(items[selectedIndex])
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val totalOffset = (listState.firstVisibleItemIndex * itemHeightPx) + listState.firstVisibleItemScrollOffset
            val index = (totalOffset / itemHeightPx).roundToInt()
            val targetIndex = index.coerceIn(0, items.size - 1)
            listState.animateScrollToItem(targetIndex)
        }
    }

    Box(
        modifier = modifier.height(itemHeight * 3),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(items.size) { index ->
                WheelItem(value = items[index], isSelected = (index == selectedIndex))
            }
        }
    }
}


// --- MODAL DE CONFIGURAÇÃO ---
@Composable
fun ConfigAlarmModal(
    alarm: AlarmData?,
    onSave: (name: String, days: Set<Int>, snooze: String, vib: Boolean, newTime: String, newPeriod: String) -> Unit, // Assinatura atualizada
    onCancel: () -> Unit
) {
    // ESTADOS DO SELETOR
    val calendar = Calendar.getInstance()
    val initialTime = alarm?.time?.split(":")
    val initialHour = initialTime?.getOrNull(0) ?: calendar.get(Calendar.HOUR).let { if (it == 0) "12" else it.toString() }.padStart(2, '0')
    val initialMinute = initialTime?.getOrNull(1) ?: calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
    val initialPeriod = alarm?.period ?: if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"

    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    var selectedPeriod by remember { mutableStateOf(initialPeriod) }

    // OUTROS ESTADOS
    var alarmName by remember { mutableStateOf(alarm?.label ?: "") }
    var selectedDays by remember { mutableStateOf(alarm?.selectedDays ?: emptySet()) }
    var snoozeTime by remember { mutableStateOf(alarm?.snoozeTime ?: "5 min") }
    var vibrationEnabled by remember { mutableStateOf(alarm?.vibrationEnabled ?: true) }

    var timeToAlarmText by remember { mutableStateOf("") }

    LaunchedEffect(selectedHour, selectedMinute, selectedPeriod, selectedDays) {
        val now = Calendar.getInstance()
        // Define a hora base do alarme para o dia de hoje.
        val baseAlarmTime = Calendar.getInstance().apply {
            val hour12 = selectedHour.toIntOrNull() ?: 0
            val hour24 = if (selectedPeriod == "PM" && hour12 != 12) hour12 + 12 else if (selectedPeriod == "AM" && hour12 == 12) 0 else hour12

            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, selectedMinute.toIntOrNull() ?: 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val finalAlarmTime: Calendar?

        if (selectedDays.isEmpty()) {
            // Se a hora já passou hoje, agende para amanhã.
            if (baseAlarmTime.before(now)) {
                baseAlarmTime.add(Calendar.DAY_OF_YEAR, 1)
            }
            finalAlarmTime = baseAlarmTime
        } else {
            var foundTime: Calendar? = null
            // Verifique hoje e os próximos 7 dias para o próximo horário válido.
            for (i in 0..7) {
                val candidateTime = (baseAlarmTime.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, i)
                }
                // O seletor de dia é 0 (Dom) a 6 (Sáb), Calendar.DAY_OF_WEEK é 1 (Dom) a 7 (Sáb)
                val dayOfWeekForCandidate = candidateTime.get(Calendar.DAY_OF_WEEK) - 1

                if (dayOfWeekForCandidate in selectedDays && candidateTime.after(now)) {
                    foundTime = candidateTime
                    break // Encontrou o próximo horário válido mais cedo
                }
            }
            finalAlarmTime = foundTime
        }

        val durationMillis = if(finalAlarmTime != null) finalAlarmTime.timeInMillis - now.timeInMillis else -1L
        timeToAlarmText = formatDuration(durationMillis)
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

            // SELETOR FUNCIONAL
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

// --- TELA DE DESPERTAR ---
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

    // Scope para o delay
    val coroutineScope = rememberCoroutineScope()

    // Animações dos botões
    val snoozeInteractionSource = remember { MutableInteractionSource() }
    val isSnoozePressed by snoozeInteractionSource.collectIsPressedAsState()
    val snoozeScale by animateFloatAsState(if (isSnoozePressed) 0.95f else 1.0f)

    val dismissInteractionSource = remember { MutableInteractionSource() }
    val isDismissPressed by dismissInteractionSource.collectIsPressedAsState()
    val dismissScale by animateFloatAsState(if (isDismissPressed) 0.9f else 1.0f)


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

// --- NAVEGAÇÃO E TELA PRINCIPAL ---

@Composable
fun AppNavigation() {
    // Estado que controla qual tela está ativa
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // A lista de alarmes agora sobe para este nível para ser compartilhada
    val alarms = remember {
        mutableStateListOf(
            AlarmData(time = "06:00", period = "AM", label = "Academia", isEnabled = true, selectedDays = setOf(1, 2, 3, 4, 5)),
            AlarmData(time = "08:30", period = "AM", label = "Trabalho", isEnabled = false, selectedDays = setOf(0, 6))
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val screen = currentScreen) {
            is Screen.Home -> {
                // Sua tela principal atual
                MainHomeScreen(
                    alarms = alarms,
                    onTriggerAlarm = { alarm ->
                        currentScreen = Screen.WakeUp(alarm)
                    }
                )
            }
            is Screen.WakeUp -> {
                // A nova tela de despertar que criamos
                WakeUpScreen(
                    alarm = screen.alarm,
                    onSnooze = {
                        // Lógica de soneca: volta para home (o alarme tocaria de novo depois)
                        currentScreen = Screen.Home
                    },
                    onDismiss = {
                        // Desliga: volta para home
                        currentScreen = Screen.Home
                    }
                )
            }
        }
    }
}

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
    ) { // Box raiz para controlar a sobreposição (z-index)

        Box(modifier = Modifier.blur(blurRadius)) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent, // Scaffold fica transparente
                floatingActionButton = {
                    val fabInteractionSource = remember { MutableInteractionSource() }
                    val isFabPressed by fabInteractionSource.collectIsPressedAsState()
                    val fabScale by animateFloatAsState(if (isFabPressed) 0.9f else 1.0f)

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
                        .padding(innerPadding) // Aplica o padding do Scaffold para o FAB
                        .windowInsetsPadding(WindowInsets.statusBars) // Adiciona padding para a status bar/notch
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp)) // Mantem um espaço extra no topo
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
                                    },
                                    onTriggerAlarm = onTriggerAlarm
                                )
                            }
                        }
                    }
                }
            }
        }

        // O Modal agora é desenhado em cima do Scaffold
        AnimatedVisibility(
            visible = showModal,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            BackHandler {
                showModal = false
                editingAlarm = null
            }
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

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    AppNavigation()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { AppNavigation() }
    }
}

private fun formatDuration(millis: Long): String {
    if (millis < 0) return ""

    val totalMinutes = ceil(millis / 60000.0).toLong()
    val days = totalMinutes / (24 * 60)
    val hours = (totalMinutes % (24 * 60)) / 60
    val minutes = totalMinutes % 60

    val parts = mutableListOf<String>()
    if (days > 0) parts.add("${days}d")
    if (hours > 0) parts.add("${hours}h")
    if (minutes > 0 || parts.isEmpty()) parts.add("${minutes}min")

    return "Toca em " + parts.joinToString(" ")
}
**/