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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.amiri.hokm.data.GameStats
import eu.amiri.hokm.engine.Card
import eu.amiri.hokm.engine.GameSnapshot
import eu.amiri.hokm.engine.Suit
import eu.amiri.hokm.engine.Team
import eu.amiri.hokm.engine.TrumpChoice

/** Dims the table and centres a card-like panel on top of it. */
@Composable
fun DimmedOverlay(content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** The light "material" panel every overlay sits on (iOS: `.regularMaterial`). */
@Composable
fun OverlayPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF23262B))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(24.dp),
    ) {
        content()
    }
}

/**
 * Shown to the hakem after the first cards are dealt: pick one of the four
 * suits as trump, or call high/low. Port of the iOS `TrumpPickerView`.
 */
@Composable
fun TrumpPicker(firstCards: List<Card>, onPick: (TrumpChoice) -> Unit) {
    OverlayPanel {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("♛", color = TableStyle.gold, fontSize = 26.sp)
            Text(De.YOU_ARE_HAKEM, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text(
                if (firstCards.size == 4) De.PICK_TRUMP_TEXT_2P else De.PICK_TRUMP_TEXT,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(Modifier.height(18.dp))

            // The dealt cards, slightly overlapping like a held hand.
            Row {
                firstCards.forEachIndexed { index, card ->
                    CardFace(
                        card,
                        OverlayCardWidth,
                        Modifier.offset(x = (-14).dp * index.toFloat()),
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Suit.entries.forEach { suit ->
                    Column(
                        Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable { onPick(TrumpChoice.OfSuit(suit)) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(suit.symbol, color = suit.onDarkColor, fontSize = 30.sp)
                        Text(suit.germanName, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeButton("↑", De.HIGH, Color(0.4f, 0.85f, 0.5f)) { onPick(TrumpChoice.High) }
                ModeButton("↓", De.LOW, Color(1f, 0.7f, 0.3f)) { onPick(TrumpChoice.Low) }
            }
        }
    }
}

@Composable
private fun ModeButton(icon: String, label: String, color: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .width(134.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(icon, color = color, fontSize = 22.sp)
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

/** Two-player: throw away two of the first four cards. */
@Composable
fun DiscardPicker(
    hand: List<Card>,
    hakemDeclaration: String?,
    onDiscard: (List<Card>) -> Unit,
) {
    var selected by remember { mutableStateOf(emptyList<Card>()) }

    OverlayPanel {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(De.DISCARD_PROMPT, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            hakemDeclaration?.let {
                Text(it, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                hand.forEach { card ->
                    val isSelected = card in selected
                    Box(
                        Modifier
                            .offset(y = if (isSelected) (-10).dp else 0.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.border(3.dp, TableStyle.gold, RoundedCornerShape(10.dp))
                                } else {
                                    Modifier
                                }
                            )
                            .clickable {
                                selected = when {
                                    isSelected -> selected - card
                                    selected.size < 2 -> selected + card
                                    else -> selected
                                }
                            },
                    ) {
                        CardFace(card, 60.dp)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = { onDiscard(selected); selected = emptyList() },
                enabled = selected.size == 2,
                colors = ButtonDefaults.buttonColors(containerColor = TableStyle.gold, contentColor = Color.Black),
            ) {
                Text("${De.DISCARD_CONFIRM} (${selected.size}/2)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Two-player draw turn: take the revealed card, or throw it away. */
@Composable
fun DrawPanel(
    revealed: Card,
    stockCount: Int,
    onTake: () -> Unit,
    onReject: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(De.DRAW_PROMPT, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        CardFace(revealed, 96.dp)
        Spacer(Modifier.height(6.dp))
        Text("${De.STOCK_LABEL}: $stockCount", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onTake,
                colors = ButtonDefaults.buttonColors(containerColor = TableStyle.gold, contentColor = Color.Black),
            ) { Text(De.DRAW_TAKE, fontWeight = FontWeight.Bold) }
            OutlinedButton(onClick = onReject) { Text(De.DRAW_REJECT, color = Color.White) }
        }
    }
}

/** Banner between hands: who won, the score and the button for the next deal. */
@Composable
fun HandOverBanner(winner: Team, snapshot: GameSnapshot, onNextHand: () -> Unit) {
    val weWon = winner == snapshot.myTeam
    val winnerTricks = snapshot.trickCounts[winner] ?: 0
    val loserTricks = snapshot.trickCounts[winner.opponent] ?: 0
    val resultText = when {
        loserTricks > 0 -> De.tricksScore(winnerTricks, loserTricks)
        winner == snapshot.hakem.team -> De.KOT_TEXT
        else -> De.HAKEM_KOT_TEXT
    }

    OverlayPanel {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (weWon) De.HAND_WON else De.HAND_LOST,
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            Text(resultText, color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(De.SCORE_LABEL, color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp)
                Text(
                    De.weVsOpp(
                        snapshot.scores[snapshot.myTeam] ?: 0,
                        snapshot.scores[snapshot.myTeam.opponent] ?: 0,
                    ),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onNextHand,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E9E5B), contentColor = Color.White),
            ) { Text(De.NEXT_ROUND, fontWeight = FontWeight.Bold) }
        }
    }
}

/** Final banner once a team reached the target points. */
@Composable
fun GameOverBanner(winner: Team, snapshot: GameSnapshot, onBackToMenu: () -> Unit) {
    val weWon = winner == snapshot.myTeam
    OverlayPanel {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (weWon) De.GAME_WON else De.GAME_LOST,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                De.finalScore(
                    snapshot.scores[snapshot.myTeam] ?: 0,
                    snapshot.scores[snapshot.myTeam.opponent] ?: 0,
                ),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 16.sp,
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onBackToMenu,
                colors = ButtonDefaults.buttonColors(containerColor = TableStyle.gold, contentColor = Color.Black),
            ) { Text(De.BACK_TO_MENU, fontWeight = FontWeight.Bold) }
        }
    }
}

/** Pause menu: statistics at a glance, resume or leave. */
@Composable
fun PauseMenu(stats: GameStats, onResume: () -> Unit, onLeave: () -> Unit) {
    OverlayPanel {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(De.PAUSE, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            StatisticsGrid(stats)
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TableStyle.gold, contentColor = Color.Black),
            ) { Text(De.RESUME_PLAY, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onLeave, modifier = Modifier.fillMaxWidth()) {
                Text(De.MAIN_MENU, color = Color.White)
            }
        }
    }
}

/** On the dark overlay panels the black suits need a lighter ink. */
private val Suit.onDarkColor: Color
    get() = if (isRed) Color(1f, 0.45f, 0.45f) else Color.White
