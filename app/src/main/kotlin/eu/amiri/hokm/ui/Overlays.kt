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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.draw.alpha
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
fun TrumpPicker(firstCards: List<Card>, onPick: (TrumpChoice) -> Unit, uiScale: Float = 1f) {
    OverlayPanel {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("♛", color = TableStyle.gold, fontSize = (26 * uiScale).sp)
            Text(De.YOU_ARE_HAKEM, color = Color.White, fontSize = (19 * uiScale).sp, fontWeight = FontWeight.Bold)
            Text(
                if (firstCards.size == 4) De.PICK_TRUMP_TEXT_2P else De.PICK_TRUMP_TEXT,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (13 * uiScale).sp, // Increased base from 12 to 13
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(Modifier.height(18.dp * uiScale))

            // The dealt cards, slightly overlapping like a held hand.
            Row {
                firstCards.forEachIndexed { index, card ->
                    CardFace(
                        card,
                        OverlayCardWidth * uiScale,
                        Modifier.offset(x = (-14).dp * index.toFloat() * uiScale),
                    )
                }
            }

            Spacer(Modifier.height(18.dp * uiScale))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp * uiScale)) {
                Suit.entries.forEach { suit ->
                    Column(
                        Modifier
                            .size(62.dp * uiScale)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable { onPick(TrumpChoice.OfSuit(suit)) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(suit.symbol, color = suit.onDarkColor, fontSize = (30 * uiScale).sp)
                        Text(suit.germanName, color = Color.White, fontSize = (10 * uiScale).sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(10.dp * uiScale))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp * uiScale)) {
                ModeButton("↑", De.HIGH, Color(0.4f, 0.85f, 0.5f), uiScale = uiScale) { onPick(TrumpChoice.High) }
                ModeButton("↓", De.LOW, Color(1f, 0.7f, 0.3f), uiScale = uiScale) { onPick(TrumpChoice.Low) }
            }
        }
    }
}

@Composable
private fun ModeButton(icon: String, label: String, color: Color, uiScale: Float = 1f, onClick: () -> Unit) {
    Row(
        Modifier
            .width(134.dp * uiScale)
            .height(46.dp * uiScale)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(icon, color = color, fontSize = (22 * uiScale).sp)
        Spacer(Modifier.width(6.dp * uiScale))
        Text(label, color = Color.White, fontSize = (15 * uiScale).sp, fontWeight = FontWeight.Bold)
    }
}

/** Two-player: throw away two of the first four cards. */
@Composable
fun DiscardPicker(
    hand: List<Card>,
    hakemDeclaration: String?,
    onDiscard: (List<Card>) -> Unit,
    uiScale: Float = 1f,
) {
    var selected by remember { mutableStateOf(emptyList<Card>()) }

    OverlayPanel {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(De.DISCARD_PROMPT, color = Color.White, fontSize = (17 * uiScale).sp, fontWeight = FontWeight.Bold)
            hakemDeclaration?.let {
                Text(it, color = Color.White.copy(alpha = 0.7f), fontSize = (13 * uiScale).sp, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(16.dp * uiScale))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp * uiScale)) {
                hand.forEach { card ->
                    val isSelected = card in selected
                    Box(
                        Modifier
                            .offset(y = if (isSelected) (-10).dp * uiScale else 0.dp)
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
                        CardFace(card, 60.dp * uiScale)
                    }
                }
            }

            Spacer(Modifier.height(18.dp * uiScale))

            Button(
                onClick = { onDiscard(selected); selected = emptyList() },
                enabled = selected.size == 2,
                colors = ButtonDefaults.buttonColors(containerColor = TableStyle.gold, contentColor = Color.Black),
            ) {
                Text("${De.DISCARD_CONFIRM} (${selected.size}/2)", fontWeight = FontWeight.Bold, fontSize = (14 * uiScale).sp)
            }
        }
    }
}

/**
 * The centre of the table during the two-player draw phase: the stock pile
 * plus the revealed card. The current drawer takes it – or rejects it and
 * must take the next, unseen card. Below, the last draw is shown openly:
 * which card came to the hand and which one went to the discard pile.
 *
 * Port of the iOS `DrawAreaView`.
 */
@Composable
fun DrawArea(
    snapshot: GameSnapshot,
    opponentName: String,
    onTake: () -> Unit,
    onReject: () -> Unit,
    uiScale: Float = 1f,
) {
    val revealed = snapshot.revealedCard
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isWide = configuration.screenWidthDp > 640
    val scaleFactor = if (isWide) 1.2f else 1.0f

    Column(
        Modifier.width(300.dp * scaleFactor * uiScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp * scaleFactor),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(26.dp * scaleFactor),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StockPile(snapshot.stockCount, scaleFactor = scaleFactor, uiScale = uiScale)

            if (revealed != null) {
                CardFace(revealed, 92.dp * scaleFactor * uiScale)
            } else {
                // Face down while the opponent is drawing: nothing to see yet.
                Box(Modifier.alpha(0.55f)) { CardBack(92.dp * scaleFactor * uiScale) }
            }
        }

        if (revealed != null) {
            Text(
                De.DRAW_PROMPT,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = (12 * uiScale).sp,
                textAlign = TextAlign.Center,
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DrawButton(De.DRAW_TAKE, "☝", Color(0xFF2E9E5B), Modifier.weight(1f), uiScale = uiScale, onClick = onTake)
                DrawButton(De.DRAW_REJECT, "🗑", Color(0xFFCC7A22), Modifier.weight(1f), uiScale = uiScale, onClick = onReject)
            }
        } else {
            Text(
                De.drawingTurn(opponentName),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = (12 * uiScale).sp,
            )
        }

        // Openly show what the last draw did – the engine only reveals this
        // for the player's own draw, never for the opponent's.
        snapshot.lastDrawResult?.let { result ->
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CardFace(result.taken, 44.dp)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "✓ ${De.DRAW_TAKEN}",
                        color = Color(0xFF4CD07D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.alpha(0.75f)) { CardFace(result.discarded, 44.dp) }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "🗑 ${De.DRAW_DISCARDED}",
                        color = Color(0xFFE8A24A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** The stock pile: a few stacked card backs with the remaining count on top. */
@Composable
private fun StockPile(count: Int, scaleFactor: Float = 1.0f, uiScale: Float = 1.0f) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            // A little stacked look under the top card.
            Box(Modifier.offset(x = 4.dp, y = 4.dp).alpha(0.5f)) { CardBack(82.dp * scaleFactor * uiScale) }
            Box(Modifier.offset(x = 2.dp, y = 2.dp).alpha(0.75f)) { CardBack(82.dp * scaleFactor * uiScale) }
            CardBack(82.dp * scaleFactor * uiScale)
            Text(
                "$count",
                color = Color.White,
                fontSize = (20 * uiScale).sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(De.STOCK_LABEL, color = Color.White.copy(alpha = 0.8f), fontSize = (12 * uiScale).sp)
    }
}

/**
 * A draw decision button: the icon sits faintly behind the label so the word
 * stays on one line and never gets truncated.
 */
@Composable
private fun DrawButton(
    title: String,
    icon: String,
    tint: Color,
    modifier: Modifier = Modifier,
    uiScale: Float = 1.0f,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 9.dp),
        colors = ButtonDefaults.buttonColors(containerColor = tint, contentColor = Color.White),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(icon, fontSize = (18 * uiScale).sp, color = Color.White.copy(alpha = 0.33f))
            Text(title, fontSize = (15 * uiScale).sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

/** Banner between hands: who won, the score and the button for the next deal. */
@Composable
fun HandOverBanner(winner: Team, snapshot: GameSnapshot, onNextHand: () -> Unit, uiScale: Float = 1.0f) {
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
                fontSize = (21 * uiScale).sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            Text(resultText, color = Color.White.copy(alpha = 0.75f), fontSize = (14 * uiScale).sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(De.SCORE_LABEL, color = Color.White.copy(alpha = 0.7f), fontSize = (15 * uiScale).sp)
                Text(
                    De.weVsOpp(
                        snapshot.scores[snapshot.myTeam] ?: 0,
                        snapshot.scores[snapshot.myTeam.opponent] ?: 0,
                    ),
                    color = Color.White,
                    fontSize = (15 * uiScale).sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onNextHand,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E9E5B), contentColor = Color.White),
            ) { Text(De.NEXT_ROUND, fontWeight = FontWeight.Bold, fontSize = (14 * uiScale).sp) }
        }
    }
}

/** Final banner once a team reached the target points. */
@Composable
fun GameOverBanner(winner: Team, snapshot: GameSnapshot, onBackToMenu: () -> Unit, uiScale: Float = 1.0f) {
    val weWon = winner == snapshot.myTeam
    OverlayPanel {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (weWon) De.GAME_WON else De.GAME_LOST,
                color = Color.White,
                fontSize = (30 * uiScale).sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                De.finalScore(
                    snapshot.scores[snapshot.myTeam] ?: 0,
                    snapshot.scores[snapshot.myTeam.opponent] ?: 0,
                ),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = (16 * uiScale).sp,
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onBackToMenu,
                colors = ButtonDefaults.buttonColors(containerColor = TableStyle.gold, contentColor = Color.Black),
            ) { Text(De.BACK_TO_MENU, fontWeight = FontWeight.Bold, fontSize = (14 * uiScale).sp) }
        }
    }
}

/** Pause menu: statistics at a glance, resume or leave. */
@Composable
fun PauseMenu(stats: GameStats, onResume: () -> Unit, onLeave: () -> Unit, uiScale: Float = 1.0f) {
    OverlayPanel {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(De.PAUSE, color = Color.White, fontSize = (21 * uiScale).sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            StatisticsGrid(stats, uiScale = uiScale)
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TableStyle.gold, contentColor = Color.Black),
            ) { Text(De.RESUME_PLAY, fontWeight = FontWeight.Bold, fontSize = (14 * uiScale).sp) }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onLeave, modifier = Modifier.fillMaxWidth()) {
                Text(De.MAIN_MENU, color = Color.White, fontSize = (14 * uiScale).sp)
            }
        }
    }
}

/** On the dark overlay panels the black suits need a lighter ink. */
private val Suit.onDarkColor: Color
    get() = if (isRed) Color(1f, 0.45f, 0.45f) else Color.White
