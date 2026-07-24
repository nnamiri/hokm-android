@file:OptIn(ExperimentalLayoutApi::class)

package eu.amiri.hokm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.amiri.hokm.Gold
import eu.amiri.hokm.SoloGameViewModel
import eu.amiri.hokm.engine.BotDifficulty
import eu.amiri.hokm.engine.Card
import eu.amiri.hokm.engine.GamePhase
import eu.amiri.hokm.engine.GameSnapshot
import eu.amiri.hokm.engine.Suit
import eu.amiri.hokm.engine.TrumpChoice

@Composable
fun HokmApp(vm: SoloGameViewModel = viewModel()) {
    if (!vm.started) MenuScreen(vm) else GameScreen(vm)
}

@Composable
private fun MenuScreen(vm: SoloGameViewModel) {
    var difficulty by remember { mutableStateOf(BotDifficulty.NORMAL) }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("حُکم", color = Gold, fontSize = 64.sp, fontWeight = FontWeight.Bold)
        Text("Hokm", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(32.dp))

        Text("Bot-Stärke", color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (d in BotDifficulty.entries) {
                val label = when (d) {
                    BotDifficulty.EASY -> "Leicht"; BotDifficulty.NORMAL -> "Mittel"; BotDifficulty.HARD -> "Schwer"
                }
                if (d == difficulty) {
                    Button(onClick = { difficulty = d }) { Text(label) }
                } else {
                    OutlinedButton(onClick = { difficulty = d }) { Text(label, color = Color.White) }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = { vm.newGame(4, difficulty) }, modifier = Modifier.fillMaxWidth()) {
            Text("Solo · 4 Spieler")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { vm.newGame(2, difficulty) }, modifier = Modifier.fillMaxWidth()) {
            Text("Solo · 2 Spieler")
        }
    }
}

@Composable
private fun GameScreen(vm: SoloGameViewModel) {
    val snap = vm.snapshot ?: return
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScoreBar(snap)
        Spacer(Modifier.height(12.dp))
        TrickView(snap)
        Spacer(Modifier.height(16.dp))
        Controls(snap, vm)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = vm::quit) { Text("Menü", color = Color.White) }
    }
}

@Composable
private fun ScoreBar(snap: GameSnapshot) {
    val me = snap.myTeam
    val opp = me.opponent
    val trump = when (val c = snap.trumpChoice) {
        is TrumpChoice.OfSuit -> c.suit.symbol
        TrumpChoice.High -> "Hoch"
        TrumpChoice.Low -> "Niedrig"
        null -> "–"
    }
    Text(
        "Wir ${snap.scores[me] ?: 0} : ${snap.scores[opp] ?: 0} Gegner   ·   " +
            "Stiche ${snap.trickCounts[me] ?: 0}:${snap.trickCounts[opp] ?: 0}   ·   Trumpf $trump",
        color = Color.White,
    )
    Text("Runde ${snap.handNumber}   ·   Hakem: ${seatName(snap, snap.hakem)}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
}

@Composable
private fun TrickView(snap: GameSnapshot) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (play in snap.currentTrick.plays) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CardChip(play.card)
                Text(seatName(snap, play.seat), color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
            }
        }
        if (snap.currentTrick.plays.isEmpty()) {
            Text("—", color = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun Controls(snap: GameSnapshot, vm: SoloGameViewModel) {
    when (val phase = snap.phase) {
        GamePhase.ChoosingTrump -> {
            if (snap.iAmHakem) {
                Text("Wähle die Ansage", color = Color.White)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (s in Suit.entries) {
                        Button(onClick = { vm.chooseMode(TrumpChoice.OfSuit(s)) }) { Text(s.symbol) }
                    }
                    Button(onClick = { vm.chooseMode(TrumpChoice.High) }) { Text("Hoch") }
                    Button(onClick = { vm.chooseMode(TrumpChoice.Low) }) { Text("Niedrig") }
                }
            } else {
                Text("Hakem wählt die Ansage …", color = Color.White)
            }
            Spacer(Modifier.height(12.dp)); Hand(snap, vm, enabled = false)
        }
        GamePhase.Discarding -> {
            if (snap.needsDiscard) DiscardPicker(snap, vm) else {
                Text("Warte auf den Gegner …", color = Color.White); Hand(snap, vm, enabled = false)
            }
        }
        GamePhase.Drawing -> {
            val revealed = snap.revealedCard
            if (snap.isMyTurn && revealed != null) {
                Text("Aufgedeckt:", color = Color.White); CardChip(revealed)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = vm::takeCard) { Text("Nehmen") }
                    OutlinedButton(onClick = vm::rejectCard) { Text("Wegwerfen", color = Color.White) }
                }
            } else {
                Text("Gegner zieht … (${snap.stockCount} im Stapel)", color = Color.White)
            }
            Spacer(Modifier.height(12.dp)); Hand(snap, vm, enabled = false)
        }
        GamePhase.Playing -> {
            Text(if (snap.isMyTurn) "Du bist am Zug" else "${seatName(snap, snap.turn)} ist am Zug", color = Color.White)
            Spacer(Modifier.height(8.dp)); Hand(snap, vm, enabled = snap.isMyTurn)
        }
        is GamePhase.HandOver -> {
            val won = phase.winner == snap.myTeam
            Text(if (won) "Runde gewonnen!" else "Runde verloren.", color = Gold, fontWeight = FontWeight.Bold)
            Button(onClick = vm::nextHand) { Text("Nächste Runde") }
            Spacer(Modifier.height(12.dp)); Hand(snap, vm, enabled = false)
        }
        is GamePhase.GameOver -> {
            val won = phase.winner == snap.myTeam
            Text(if (won) "Spiel gewonnen! 🏆" else "Spiel verloren.", color = Gold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Button(onClick = vm::quit) { Text("Neues Spiel") }
        }
    }
}

@Composable
private fun DiscardPicker(snap: GameSnapshot, vm: SoloGameViewModel) {
    val selected = remember { mutableStateListOf<Card>() }
    Text("Wirf 2 Karten ab (${selected.size}/2)", color = Color.White)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (card in snap.hand) {
            val isSel = card in selected
            CardButton(card, enabled = true, highlighted = isSel) {
                if (isSel) selected.remove(card) else if (selected.size < 2) selected.add(card)
            }
        }
    }
    Button(onClick = { vm.discard(selected.toList()); selected.clear() }, enabled = selected.size == 2) {
        Text("Abwerfen")
    }
}

@Composable
private fun Hand(snap: GameSnapshot, vm: SoloGameViewModel, enabled: Boolean) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (card in snap.hand) {
            val legal = !enabled || card in snap.legalCards
            CardButton(card, enabled = enabled && legal) { vm.play(card) }
        }
    }
}

@Composable
private fun CardButton(card: Card, enabled: Boolean, highlighted: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (highlighted) Gold else Color.White,
            contentColor = if (card.suit.isRed) Color(0xFFC63430) else Color(0xFF23262B),
        ),
    ) { Text("${card.rank.symbol}${card.suit.symbol}", fontWeight = FontWeight.Bold) }
}

@Composable
private fun CardChip(card: Card) {
    Box(
        Modifier.padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "${card.rank.symbol}${card.suit.symbol}",
            color = if (card.suit.isRed) Color(0xFFE0655F) else Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun seatName(snap: GameSnapshot, seat: eu.amiri.hokm.engine.Seat?): String = when (seat) {
    null -> "—"
    snap.seat -> "Du"
    snap.seat.partner -> "Partner"
    else -> "Gegner"
}
