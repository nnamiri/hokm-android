package eu.amiri.hokm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.amiri.hokm.SoloGameViewModel
import eu.amiri.hokm.data.GameStats
import eu.amiri.hokm.engine.BotDifficulty

@Composable
fun HokmApp(vm: SoloGameViewModel = viewModel()) {
    var showStats by remember { mutableStateOf(false) }

    when {
        vm.started -> GameScreen(vm)
        showStats -> StatisticsScreen(vm.stats, onReset = vm::resetStats) { showStats = false }
        else -> MenuScreen(vm) { showStats = true }
    }
}

/** Home screen: resume, new game, bot strength, player count, statistics. */
@Composable
private fun MenuScreen(vm: SoloGameViewModel, onShowStats: () -> Unit) {
    var difficulty by remember { mutableStateOf(BotDifficulty.NORMAL) }
    var players by remember { mutableIntStateOf(4) }

    FeltBackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("حُکم", color = TableStyle.gold, fontSize = 62.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
            Text("Hokm", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black)
            Text(
                De.TAGLINE,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )

            Spacer(Modifier.height(28.dp))

            if (vm.canResume) {
                Button(
                    onClick = vm::resumeSavedGame,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TableStyle.gold,
                        contentColor = Color.Black,
                    ),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(De.RESUME_GAME, fontWeight = FontWeight.Bold)
                        Text(De.RESUME_GAME_SUB, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    De.DISCARD_SAVED_GAME,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    modifier = Modifier.clickable { vm.discardSavedGame() }.padding(4.dp),
                )
                Spacer(Modifier.height(18.dp))
            }

            SectionCard(De.PLAYERS_LABEL) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2, 4).forEach { count ->
                        SelectableChip(De.playersCount(count), selected = players == count) { players = count }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            SectionCard(De.BOT_STRENGTH) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotDifficulty.entries.forEach { level ->
                        SelectableChip(level.germanName, selected = difficulty == level) { difficulty = level }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { vm.newGame(players, difficulty) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E9E5B),
                    contentColor = Color.White,
                ),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(De.NEW_SOLO_GAME, fontWeight = FontWeight.Bold)
                    Text(De.SOLO_SUB, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(onClick = onShowStats, modifier = Modifier.fillMaxWidth()) {
                Text(De.STATISTICS, color = Color.White)
            }
        }
    }
}

/** Statistics as its own area (the iOS app shows it as a full section too). */
@Composable
fun StatisticsScreen(stats: GameStats, onReset: () -> Unit, onBack: () -> Unit) {
    FeltBackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(De.STATISTICS, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            StatisticsGrid(stats)

            if (stats.gamesPlayed > 0) {
                Spacer(Modifier.height(20.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatRow(De.GAMES, "${stats.gamesPlayed}")
                    StatRow(De.WON, "${stats.gamesWon}")
                    StatRow(De.LOST, "${stats.gamesLost}")
                    StatRow(De.WIN_RATE, "${stats.winPercent} %")
                    StatRow(De.STREAK, "${stats.currentStreak}")
                    StatRow(De.BEST_STREAK, "${stats.bestStreak}")
                    StatRow(De.ROUNDS_WON, "${stats.handsWon}")
                    StatRow(De.KOT_ROUNDS, "${stats.sweeps}")
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    De.RESET_STATS,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable(onClick = onReset).padding(8.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(De.BACK, color = Color.White)
            }
        }
    }
}

/** Compact statistics grid, reused by the pause menu. */
@Composable
fun StatisticsGrid(stats: GameStats) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (stats.gamesPlayed == 0) {
            Text(De.NO_GAMES_YET, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            return@Column
        }
        // Two rows of three – LazyVGrid would be overkill for six fixed tiles.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(De.GAMES, "${stats.gamesPlayed}", Modifier.weight(1f))
            StatTile(De.WON, "${stats.gamesWon}", Modifier.weight(1f))
            StatTile(De.WIN_RATE, "${stats.winPercent} %", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(De.STREAK, "${stats.currentStreak}", Modifier.weight(1f))
            StatTile(De.BEST_STREAK, "${stats.bestStreak}", Modifier.weight(1f))
            StatTile(De.KOT_ROUNDS, "${stats.sweeps}", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = TableStyle.teamMine, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(title, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
    }
}

@Composable
private fun StatRow(title: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
private fun SelectableChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) TableStyle.gold else Color.White.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = if (selected) TableStyle.gold else Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            color = if (selected) Color.Black else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
