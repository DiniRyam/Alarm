package org.example.despertador.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.despertador.theme.GlassBorder
import org.example.despertador.theme.GlassWhite

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        // Aplicamos a cor translúcida que definimos
        color = GlassWhite,
        // Bordas bem arredondadas (estilo orgânico)
        shape = RoundedCornerShape(32.dp),
        // A borda fininha que simula o reflexo no vidro
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}