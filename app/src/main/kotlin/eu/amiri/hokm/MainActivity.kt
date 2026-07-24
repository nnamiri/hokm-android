package eu.amiri.hokm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import eu.amiri.hokm.ui.HokmApp
import eu.amiri.hokm.ui.TableStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // The felt-green identity from the iOS app; a dark scheme reads
            // well on it, and every screen paints its own felt background.
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = TableStyle.gold,
                    background = TableStyle.feltTop,
                    surface = TableStyle.feltTop,
                ),
            ) {
                Surface(color = TableStyle.feltTop) {
                    HokmApp()
                }
            }
        }
    }
}
