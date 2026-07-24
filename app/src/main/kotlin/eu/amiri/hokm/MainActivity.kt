package eu.amiri.hokm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import eu.amiri.hokm.ui.HokmApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // The felt-green identity from the iOS app; a dark scheme reads well
            // on it. A full theme/design pass comes in a later milestone.
            @Suppress("UNUSED_VARIABLE")
            val dark = isSystemInDarkTheme()
            MaterialTheme(colorScheme = darkColorScheme(primary = Gold, background = FeltTop)) {
                Surface(color = FeltTop) {
                    HokmApp()
                }
            }
        }
    }
}

val FeltTop = Color(0xFF1A6B42)
val FeltBottom = Color(0xFF0D452B)
val Gold = Color(0xFFF0C759)
