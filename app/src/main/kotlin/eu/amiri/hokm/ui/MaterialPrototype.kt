package eu.amiri.hokm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A conceptual prototype showing how Hokm would look with a full Material 3 implementation.
 * It uses a Scaffold with a NavigationBar and modern M3 components.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3HokmPrototype() {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Spielen", "Statistik", "Regeln", "Tutorial")
    val icons = listOf(Icons.Default.PlayArrow, Icons.Default.BarChart, Icons.Default.Book, Icons.Default.Help)

    // Using the app's FeltBackground as the base to keep the identity
    FeltBackground {
        Scaffold(
            containerColor = Color.Transparent, // Let the felt background show through
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("HOKM", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = TableStyle.gold
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    contentColor = Color.White
                ) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = item) },
                            label = { Text(item) },
                            selected = selectedItem == index,
                            onClick = { selectedItem = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = TableStyle.gold,
                                indicatorColor = TableStyle.gold,
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (selectedItem) {
                    0 -> M3HomeContent()
                    1 -> M3StatisticsContent()
                    else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hier erscheinen die ${items[selectedItem]}", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun M3HomeContent() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Resume Card (if applicable)
        ElevatedCard(
            onClick = { /* Resume */ },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = TableStyle.gold)
        ) {
            Row(
                Modifier.padding(20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.History, "Resume", modifier = Modifier.size(32.dp), tint = Color.Black)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Spiel fortsetzen", variant = MaterialTheme.typography.titleMedium, color = Color.Black)
                    Text("Punkte: 12 : 9 (Hand 4)", variant = MaterialTheme.typography.bodySmall, color = Color.Black.copy(alpha = 0.7f))
                }
            }
        }

        Text("Neues Spiel", variant = MaterialTheme.typography.headlineSmall, color = Color.White)

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White.copy(alpha = 0.05f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Spielermodus", variant = MaterialTheme.typography.labelLarge, color = TableStyle.gold)
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = true, onClick = {}, label = { Text("2 Spieler") })
                    FilterChip(selected = false, onClick = {}, label = { Text("4 Spieler") })
                }
            }
        }

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White.copy(alpha = 0.05f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Bot-Schwierigkeit", variant = MaterialTheme.typography.labelLarge, color = TableStyle.gold)
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = false, onClick = {}, label = { Text("Leicht") })
                    FilterChip(selected = true, onClick = {}, label = { Text("Mittel") })
                    FilterChip(selected = false, onClick = {}, label = { Text("Schwer") })
                }
            }
        }

        Button(
            onClick = { /* Start */ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E9E5B))
        ) {
            Text("Solo-Spiel starten", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun M3StatisticsContent() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Deine Bilanz", variant = MaterialTheme.typography.headlineSmall, color = Color.White)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Siegquote", "68 %", Icons.Default.TrendingUp, Modifier.weight(1f))
            StatCard("Spiele", "142", Icons.Default.Numbers, Modifier.weight(1f))
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.1f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Details", variant = MaterialTheme.typography.titleMedium, color = TableStyle.gold)
                Spacer(Modifier.height(12.dp))
                StatRow("Gewonnene Runden", "842")
                StatRow("Kut / Hakem-Kut", "24")
                StatRow("Aktuelle Serie", "5 Siege")
                StatRow("Beste Serie", "12 Siege")
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = TableStyle.gold, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, variant = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(label, variant = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.7f))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Text(text: String, variant: androidx.compose.ui.text.TextStyle, color: Color, modifier: Modifier = Modifier) {
    Text(text, style = variant, color = color, modifier = modifier)
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun M3HomePreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        M3HokmPrototype()
    }
}
