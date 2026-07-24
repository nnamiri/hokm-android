package eu.amiri.hokm.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.amiri.hokm.engine.Card
import eu.amiri.hokm.engine.GameSnapshot
import eu.amiri.hokm.engine.Seat
import eu.amiri.hokm.engine.TrumpChoice
import kotlin.math.min

/**
 * The local player's hand as a real fanned arc: cards near the edges tilt and
 * dip, the playable ones lift up, illegal ones are dimmed (never hidden).
 * Port of the iOS `HandView`.
 */
@Composable
fun HandFan(
    cards: List<Card>,
    legalCards: Set<Card>,
    isMyTurn: Boolean,
    onPlay: (Card) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.BottomCenter) {
        val count = cards.size
        if (count == 0) return@BoxWithConstraints

        val overlap = 0.46f
        val available = maxWidth - 16.dp
        val cardWidth = Dp(min(84f, maxOf(50f, available.value / (1 + overlap * (count - 1).coerceAtLeast(0)))))
        val step = cardWidth * overlap
        val spread = step * (count - 1).coerceAtLeast(0).toFloat()
        val maxAngle = min((count - 1).coerceAtLeast(0) * 1.1f, 14f)

        cards.forEachIndexed { index, card ->
            val t = if (count > 1) index.toFloat() / (count - 1) - 0.5f else 0f
            val playable = isMyTurn && card in legalCards
            val dip = cardWidth * (t * t * 0.42f)
            val lift by animateFloatAsState(
                targetValue = if (playable) 24f else 0f,
                animationSpec = tween(200),
                label = "cardLift",
            )

            Box(
                Modifier
                    .offset(x = spread * t, y = dip - lift.dp)
                    .rotate(t * 2 * maxAngle)
                    .alpha(if (isMyTurn && !playable) 0.55f else 1f)
                    .clickable(enabled = playable) { onPlay(card) },
            ) {
                CardFace(card, cardWidth)
            }
        }
    }
}

/**
 * The middle of the table: the cards of the current trick, laid out towards
 * the seat that played them, on a dark oval. Shows the previous trick dimmed
 * while the next one has not started. Port of the iOS `TrickAreaView`.
 */
@Composable
fun TrickArea(snapshot: GameSnapshot, modifier: Modifier = Modifier) {
    val showLast = snapshot.currentTrick.plays.isEmpty() && snapshot.lastTrick != null
    val trick = if (showLast) snapshot.lastTrick!! else snapshot.currentTrick

    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val base = min(maxWidth.value, maxHeight.value)
        val cardWidth = Dp(min(108f, maxOf(60f, base * 0.32f)))
        val cardHeight = cardWidth * 1.45f
        val ovalWidth = minOf(maxWidth, cardWidth * 2.4f)
        val ovalHeight = minOf(maxHeight, cardHeight * 2.05f)

        Box(
            Modifier
                .size(ovalWidth, ovalHeight)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.14f)),
        )

        Box(Modifier.alpha(if (showLast) 0.45f else 1f)) {
            trick.plays.forEach { play ->
                val (dx, dy) = trickOffset(snapshot, play.seat, cardWidth, cardHeight)
                CardFace(play.card, cardWidth, Modifier.offset(x = dx, y = dy))
            }
        }
    }
}

private fun trickOffset(
    snapshot: GameSnapshot,
    seat: Seat,
    cardWidth: Dp,
    cardHeight: Dp,
): Pair<Dp, Dp> {
    val vert = cardHeight * 0.5f
    val side = cardWidth * 0.6f
    if (snapshot.playerCount == 2) {
        return if (seat == snapshot.seat) 0.dp to vert else 0.dp to -vert
    }
    return when (TablePosition.of(seat, snapshot.seat)) {
        TablePosition.BOTTOM -> 0.dp to vert
        TablePosition.RIGHT -> side to 0.dp
        TablePosition.TOP -> 0.dp to -vert
        TablePosition.LEFT -> -side to 0.dp
    }
}

/**
 * Name plate for one seat: hakem crown, turn highlight and – for the other
 * players – their remaining cards as a small fan of card backs.
 * Port of the iOS `PlayerBadgeView`.
 */
@Composable
fun PlayerBadge(
    seat: Seat,
    name: String,
    snapshot: GameSnapshot,
    modifier: Modifier = Modifier,
    showCards: Boolean = true,
) {
    val isTurn = snapshot.turn == seat
    val isHakem = snapshot.hakem == seat
    val cardCount = snapshot.handCounts[seat] ?: 0
    val isMyTeam = seat.team == snapshot.myTeam
    val teamTint = seat.team.tint(snapshot.myTeam)
    val position = TablePosition.of(seat, snapshot.seat)
    // In 4-player games the side seats fan vertically, which frees up
    // horizontal room for the central play area.
    val isSide = snapshot.playerCount == 4 &&
        (position == TablePosition.LEFT || position == TablePosition.RIGHT)

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (showCards) {
            CardBackFan(count = cardCount, rotated = isSide)
            Spacer(Modifier.height(6.dp))
        }

        Row(
            Modifier
                .clip(CircleShape)
                .background(if (isTurn) TableStyle.gold.copy(alpha = 0.92f) else teamTint.copy(alpha = 0.9f))
                .border(
                    width = if (isTurn) 2.5.dp else 1.5.dp,
                    color = if (isTurn) Color.White else Color.White.copy(alpha = 0.55f),
                    shape = CircleShape,
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (isHakem) Text("♛", color = TableStyle.gold, fontSize = 13.sp)
            Text(
                name,
                color = if (isTurn) Color.Black else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            if (isMyTeam && seat != snapshot.seat) {
                Text(
                    De.PARTNER,
                    color = if (isTurn) Color.Black else Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                )
            }
        }
    }
}

/** A small arc of card backs standing in for another player's hand. */
@Composable
private fun CardBackFan(count: Int, rotated: Boolean) {
    val shown = min(count, 13)
    val step = 6.dp
    val spread = step * (shown - 1).coerceAtLeast(0).toFloat()

    Box(
        Modifier
            .then(if (rotated) Modifier.size(44.dp, 70.dp) else Modifier.size(120.dp, 40.dp))
            .alpha(if (count == 0) 0f else 1f)
            .rotate(if (rotated) 90f else 0f),
        contentAlignment = Alignment.Center,
    ) {
        repeat(shown.coerceAtLeast(1)) { index ->
            if (shown > 0) {
                val t = if (shown > 1) index.toFloat() / (shown - 1) - 0.5f else 0f
                CardBack(
                    width = 24.dp,
                    modifier = Modifier
                        .offset(x = spread * t, y = (t * t * 10).dp)
                        .rotate(t * 20f),
                )
            }
        }
    }
}

/**
 * Compact top bar: score, tricks, declaration, hakem and round – one line,
 * nothing ever wraps. Port of the iOS `ScoreHeaderView`.
 */
@Composable
fun ScoreHeader(snapshot: GameSnapshot, hakemName: String, modifier: Modifier = Modifier) {
    val myTeam = snapshot.myTeam
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ScoreChip(
            De.POINTS,
            snapshot.scores[myTeam] ?: 0,
            snapshot.scores[myTeam.opponent] ?: 0,
        )
        ScoreChip(
            De.TRICKS,
            snapshot.trickCounts[myTeam] ?: 0,
            snapshot.trickCounts[myTeam.opponent] ?: 0,
        )

        Spacer(Modifier.width(4.dp))

        snapshot.trumpChoice?.let { choice ->
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                when (choice) {
                    is TrumpChoice.OfSuit -> {
                        Text(
                            choice.suit.symbol,
                            color = if (choice.suit.isRed) Color(1f, 0.45f, 0.45f) else Color.White,
                            fontSize = 12.sp,
                        )
                        Text(
                            choice.suit.germanName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                    }
                    TrumpChoice.High -> Text(
                        "↑ ${De.HIGH}",
                        color = Color(0.4f, 0.85f, 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TrumpChoice.Low -> Text(
                        "↓ ${De.LOW}",
                        color = Color(1f, 0.7f, 0.3f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        Row(
            Modifier
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("♛", color = TableStyle.gold, fontSize = 11.sp)
            Text(hakemName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }

        Text(
            "${snapshot.handNumber}",
            color = TableStyle.gold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}

/** A narrow score chip: caption on top, "mine : theirs" below. */
@Composable
private fun ScoreChip(title: String, mine: Int, theirs: Int) {
    Column(
        Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("$mine", color = TableStyle.gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(":", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("$theirs", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** Card size used inside overlays, where space is tight. */
val OverlayCardWidth: Dp = 52.dp

/** Full-bleed felt table background – the app's signature look. */
@Composable
fun FeltBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(TableStyle.feltGradient)) { content() }
}
