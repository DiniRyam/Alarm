package org.example.despertador.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun InfiniteWheelColumn(
    items: List<String>,
    initialValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 50.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val repeatFactor = 1000 // Aumenta o número de itens para simular o infinito

    val initialIndex = items.indexOf(initialValue).let { if (it == -1) 0 else it }
    // Centraliza a lista inicial para permitir rolagem para cima e para baixo
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

    // Efeito de "snap" para centralizar o item mais próximo
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val floatIndex = (listState.firstVisibleItemIndex * itemHeightPx + listState.firstVisibleItemScrollOffset) / itemHeightPx
            val targetAbsoluteIndex = floatIndex.roundToInt()
            listState.animateScrollToItem(targetAbsoluteIndex)
        }
    }

    Box(
        modifier = modifier.height(itemHeight * 3), // Mostra 3 itens por vez (o selecionado, um acima, um abaixo)
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight), // Padding para centralizar o primeiro e último item
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

    // Efeito de "snap"
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
