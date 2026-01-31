package org.example.despertador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Ativa o layout de tela cheia (Edge-to-Edge)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // Chama a lógica de navegação principal definida no commonMain
            AppNavigation()
        }
    }
}

// Este preview permite ver o app funcionando diretamente no Android Studio
@Preview(showBackground = true)
@Composable
fun AppPreview() {
    AppNavigation()
}