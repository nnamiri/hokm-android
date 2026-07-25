package eu.amiri.hokm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.amiri.hokm.SoloGameViewModel
import eu.amiri.hokm.data.GameStats
import eu.amiri.hokm.engine.BotDifficulty

/** The areas reachable from the home screen. */
private enum class MenuArea(val label: String, val icon: ImageVector) {
    HOME("Startseite", Icons.Default.Home),
    RULES("Spielregeln", Icons.Default.Info),
    STATISTICS("Statistik", Icons.Default.Info),
    SETTINGS("Einstellungen", Icons.Default.Settings),
}

@Composable
fun HokmApp(vm: SoloGameViewModel = viewModel()) {
    var area by remember { mutableStateOf(MenuArea.HOME) }

    // On the very first launch the tutorial opens by itself, as on iOS.
    if (vm.needsTutorial) {
        TutorialScreen(onFinish = vm::tutorialSeen)
        return
    }

    if (vm.started) {
        GameScreen(vm)
    } else {
        HokmNavigationShell(area, onAreaChange = { area = it }, vm = vm)
    }
}

@Composable
private fun HokmNavigationShell(
    activeArea: MenuArea,
    onAreaChange: (MenuArea) -> Unit,
    vm: SoloGameViewModel,
) {
    FeltBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                HokmNavigationBar(activeArea, onAreaChange, vm)
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
                when (activeArea) {
                    MenuArea.HOME -> MenuScreen(vm)
                    MenuArea.STATISTICS -> StatisticsScreen(vm)
                    MenuArea.RULES -> RulesScreen()
                    MenuArea.SETTINGS -> SettingsScreen(vm)
                }
            }
        }
    }
}

@Composable
private fun HokmNavigationBar(activeArea: MenuArea, onAreaChange: (MenuArea) -> Unit, vm: SoloGameViewModel) {
    val uiScale = vm.uiScale
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .height((84 * uiScale).dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(32.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MenuArea.entries.forEach { area ->
            val selected = activeArea == area
            HokmNavigationItem(
                label = area.label,
                icon = area.icon,
                selected = selected,
                uiScale = uiScale,
            ) { onAreaChange(area) }
        }
    }
}

@Composable
private fun RowScope.HokmNavigationItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    uiScale: Float = 1f,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size((26 * uiScale).dp),
            tint = if (selected) Color(0xFFC5EA70) else Color.White.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
            fontSize = (11 * uiScale).sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SettingsScreen(vm: SoloGameViewModel) {
    val uiScale = vm.uiScale
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier.widthIn(max = 600.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Einstellungen", color = Color.White, fontSize = (28 * uiScale).sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            SectionCard(De.BOT_STRENGTH) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotDifficulty.entries.forEach { level ->
                        SelectableChip(
                            level.germanName,
                            selected = vm.botDifficulty == level,
                            modifier = Modifier.weight(1f),
                            onClick = { vm.updateBotDifficulty(level) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            SectionCard(De.FONT_SIZE) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Normal" to 1.0f,
                        "Mittel" to 1.25f,
                        "Groß" to 1.5f
                    ).forEach { (label, scale) ->
                        SelectableChip(
                            label,
                            selected = vm.uiScale == scale,
                            modifier = Modifier.weight(1f),
                            onClick = { vm.updateUiScale(scale) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            
            Text(
                "Version 1.0.0",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = (12 * uiScale).sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/** Home screen: resume, new game. */
@Composable
private fun MenuScreen(vm: SoloGameViewModel) {
    var showTutorial by remember { mutableStateOf(value = false) }
    val uiScale = vm.uiScale

    if (showTutorial) {
        TutorialScreen(onFinish = { showTutorial = false })
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.widthIn(max = 600.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // iOS-style Logo Section
            Text(
                "حُکم",
                color = TableStyle.gold,
                fontSize = (110 * uiScale).sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Serif,
            )
            Text(
                "Hokm",
                color = Color.White,
                fontSize = (80 * uiScale).sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                De.TAGLINE,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (14 * uiScale).sp,
                textAlign = TextAlign.Center,
                lineHeight = (20 * uiScale).sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 40.dp),
            )

            if (vm.canResume) {
                ActionCard(
                    title = De.RESUME_GAME,
                    subtitle = De.RESUME_GAME_SUB,
                    containerColor = Color(0xFF2E9E5B),
                    icon = Icons.Default.PlayArrow,
                    showChevron = false,
                    uiScale = uiScale,
                    onClick = vm::resumeSavedGame
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    De.DISCARD_SAVED_GAME,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = (11 * uiScale).sp,
                    modifier = Modifier.clickable { vm.discardSavedGame() }.padding(4.dp),
                )
                Spacer(Modifier.height(16.dp))
            }

            val pagerState = rememberPagerState(pageCount = { 2 })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height((88 * uiScale).dp),
                beyondViewportPageCount = 1,
                pageSpacing = 16.dp,
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) { page ->
                if (page == 0) {
                    ActionCard(
                        title = De.NEW_SOLO_GAME,
                        subtitle = De.SOLO_SUB,
                        containerColor = Color.Black.copy(alpha = 0.3f),
                        icon = Icons.Default.Person,
                        uiScale = uiScale,
                        onClick = { /* Drag to selection */ }
                    )
                } else {
                    Surface(
                        color = Color.Black.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((88 * uiScale).dp)
                            .border(width = 1.dp, color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(24.dp))
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf(2, 4).forEach { count ->
                                Button(
                                    onClick = { vm.newGame(count) },
                                    modifier = Modifier.weight(1f).height((56 * uiScale).dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(De.playersCount(count), color = Color.White, fontWeight = FontWeight.Bold, fontSize = (14 * uiScale).sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Tutorial Chip
            Surface(
                onClick = { showTutorial = true },
                color = Color.White.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.height((36 * uiScale).dp)
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size((18 * uiScale).dp))
                    Text("Tutorial", color = Color.White, fontSize = (14 * uiScale).sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        
        Spacer(Modifier.height(100.dp)) // Padding to not be covered by navigation bar
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    containerColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    showChevron: Boolean = true,
    uiScale: Float = 1f,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height((88 * uiScale).dp)
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(24.dp))
    ) {
        Row(
            Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size((40 * uiScale).dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size((26 * uiScale).dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = (19 * uiScale).sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = (13 * uiScale).sp, maxLines = 1)
            }
            if (showChevron) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size((32 * uiScale).dp)
                )
            }
        }
    }
}

/** Statistics as its own area. */
@Composable
fun StatisticsScreen(vm: SoloGameViewModel) {
    val stats = vm.stats
    val uiScale = vm.uiScale
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.widthIn(max = 600.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(De.STATISTICS, color = Color.White, fontSize = (28 * uiScale).sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            StatisticsGrid(stats, uiScale = uiScale)

            if (stats.gamesPlayed > 0) {
                Spacer(Modifier.height(24.dp))
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.08f))
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Detail-Statistik", color = TableStyle.gold, fontSize = (14 * uiScale).sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        StatRow(De.GAMES, stats.gamesPlayed.toString(), uiScale = uiScale)
                        StatRow(De.WON, stats.gamesWon.toString(), uiScale = uiScale)
                        StatRow(De.LOST, stats.gamesLost.toString(), uiScale = uiScale)
                        StatRow(De.WIN_RATE, "${stats.winPercent} %", uiScale = uiScale)
                        StatRow(De.STREAK, stats.currentStreak.toString(), uiScale = uiScale)
                        StatRow(De.BEST_STREAK, stats.bestStreak.toString(), uiScale = uiScale)
                        StatRow(De.ROUNDS_WON, stats.handsWon.toString(), uiScale = uiScale)
                        StatRow(De.KOT_ROUNDS, stats.sweeps.toString(), uiScale = uiScale)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    De.RESET_STATS,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = (12 * uiScale).sp,
                    modifier = Modifier.clickable(onClick = vm::resetStats).padding(8.dp),
                )
            }
        }
    }
}

/** Compact statistics grid, reused by the pause menu. */
@Composable
fun StatisticsGrid(stats: GameStats, uiScale: Float = 1f) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (stats.gamesPlayed == 0) {
            Text(De.NO_GAMES_YET, color = Color.White.copy(alpha = 0.7f), fontSize = (14 * uiScale).sp)
            return@Column
        }
        // Two rows of three – LazyVGrid would be overkill for six fixed tiles.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(De.GAMES, "${stats.gamesPlayed}", Modifier.weight(1f), uiScale = uiScale)
            StatTile(De.WON, "${stats.gamesWon}", Modifier.weight(1f), uiScale = uiScale)
            StatTile(De.WIN_RATE, "${stats.winPercent} %", Modifier.weight(1f), uiScale = uiScale)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(De.STREAK, "${stats.currentStreak}", Modifier.weight(1f), uiScale = uiScale)
            StatTile(De.BEST_STREAK, "${stats.bestStreak}", Modifier.weight(1f), uiScale = uiScale)
            StatTile(De.KOT_ROUNDS, "${stats.sweeps}", Modifier.weight(1f), uiScale = uiScale)
        }
    }
}

@Composable
private fun StatTile(title: String, value: String, modifier: Modifier = Modifier, uiScale: Float = 1f) {
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(vertical = (8 * uiScale).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = TableStyle.teamMine, fontSize = (19 * uiScale).sp, fontWeight = FontWeight.Bold)
        Text(title, color = Color.White.copy(alpha = 0.7f), fontSize = (10 * uiScale).sp)
    }
}

@Composable
private fun StatRow(title: String, value: String, uiScale: Float = 1f) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Color.White.copy(alpha = 0.7f), fontSize = (14 * uiScale).sp)
        Text(value, color = Color.White, fontSize = (14 * uiScale).sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.22f))
            .padding(14.dp),
    ) {
        Text(title, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun SelectableChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) TableStyle.gold else Color.White.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = if (selected) TableStyle.gold else Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color.Black else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
