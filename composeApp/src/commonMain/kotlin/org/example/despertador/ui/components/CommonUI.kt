package org.example.despertador.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.despertador.ui.theme.GlassBorder
import org.example.despertador.ui.theme.GlassWhite

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
