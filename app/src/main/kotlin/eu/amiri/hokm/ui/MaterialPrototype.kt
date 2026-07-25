package eu.amiri.hokm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A refined prototype matching the iOS screenshot design.
 * Features: Large calligraphic logo, iOS-style cards, and a glassmorphism bottom bar.
 */
@Composable
fun M3HokmPrototype() {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Startseite", "Spielregeln", "Statistik", "Einstellungen")
    val icons = listOf(Icons.Default.Home, Icons.Default.Info, Icons.Default.Info, Icons.Default.Settings)

    FeltBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                // Glassmorphism Navigation Bar
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        tonalElevation = 0.dp
                    ) {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        icons[index],
                                        contentDescription = item,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text(item, fontSize = 10.sp) },
                                selected = selectedItem == index,
                                onClick = { selectedItem = index },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFFC5EA70), // Light green tint from screenshot
                                    selectedTextColor = Color.White,
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (selectedItem) {
                    0 -> M3HomeContent()
                    else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hier erscheinen die $items[selectedItem]", color = Color.White)
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
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo Section
        Text(
            "حُکم",
            color = TableStyle.gold,
            fontSize = 110.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.offset(y = 20.dp)
        )
        Text(
            "Hokm",
            color = Color.White,
            fontSize = 80.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif
        )
        
        Text(
            "Das persische Stichspiel – 2 Teams, 13 Karten,\n7 Stiche",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(Modifier.height(50.dp))

        // Action Cards
        IOSActionCard(
            title = "Spiel fortsetzen",
            subtitle = "Dein unterbrochenes Solo-Spiel wartet",
            containerColor = Color(0xFF2E9E5B),
            icon = Icons.Default.PlayArrow,
            iconColor = Color.White
        )

        Spacer(Modifier.height(12.dp))

        IOSActionCard(
            title = "Neues Solo-Spiel",
            subtitle = "2 Spieler",
            containerColor = Color.Black.copy(alpha = 0.3f),
            icon = Icons.Default.Person,
            iconColor = Color.White.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(24.dp))

        // Tutorial Chip
        Surface(
            onClick = { /* Tutorial */ },
            color = Color.White.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.height(36.dp)
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                Text("Tutorial", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun IOSActionCard(
    title: String,
    subtitle: String,
    containerColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Surface(
        onClick = { /* Action */ },
        color = containerColor,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Icon
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
            }

            Spacer(Modifier.width(16.dp))

            // Text
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            }

            // Right Chevron
            Icon(
                Icons.Default.KeyboardArrowRight,
                null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun M3HomePreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        M3HokmPrototype()
    }
}
