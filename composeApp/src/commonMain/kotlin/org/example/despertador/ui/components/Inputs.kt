package org.example.despertador.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val trackColor by animateColorAsState(if (checked) Color(0xFF4ADE80) else Color.DarkGray.copy(alpha = 0.4f), label = "toggle_track_color")
    val thumbOffset by animateDpAsState(targetValue = if (checked) 27.dp else 3.dp, label = "toggle_thumb_offset")

    Box(
        modifier = Modifier
            .width(56.dp)
            .height(32.dp)
            .clip(CircleShape)
            .clickable { onCheckedChange(!checked) }
    ) {
        // Track (o fundo do switch)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(20.dp)
                .padding(horizontal = 6.dp)
                .clip(CircleShape)
                .background(trackColor)
        )
        // Thumb (a bolinha que se move)
        Surface(
            modifier = Modifier
                .size(26.dp)
                .offset(x = thumbOffset)
                .align(Alignment.CenterStart),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.45f), Color.Transparent))))
        }
    }
}
