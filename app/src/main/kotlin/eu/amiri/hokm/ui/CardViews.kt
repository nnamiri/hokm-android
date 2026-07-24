package eu.amiri.hokm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import eu.amiri.hokm.engine.Card
import eu.amiri.hokm.engine.Rank
import kotlin.math.roundToInt

/**
 * A face-up playing card, drawn entirely in Compose (no assets needed) –
 * the Kotlin twin of the iOS `CardView`. Number cards show the classic pip
 * layout (a 5 shows five suit symbols), court cards a large serif letter,
 * the ace one big suit symbol.
 */
@Composable
fun CardFace(card: Card, width: Dp, modifier: Modifier = Modifier) {
    val height = width * 1.45f
    val corner = width * 0.14f

    Box(
        modifier
            .size(width, height)
            .shadow(2.dp, RoundedCornerShape(corner))
            .clip(RoundedCornerShape(corner))
            .background(Color.White)
            .border(1.dp, Color.Black.copy(alpha = 0.18f), RoundedCornerShape(corner)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(horizontal = width * 0.20f, vertical = width * 0.16f),
            contentAlignment = Alignment.Center,
        ) {
            CardCenter(card, width)
        }

        // Rank/suit in the top-left corner and mirrored in the bottom-right.
        Box(Modifier.fillMaxSize().padding(width * 0.06f)) {
            CornerLabel(card, width, Modifier.align(Alignment.TopStart))
            CornerLabel(card, width, Modifier.align(Alignment.BottomEnd).rotate(180f))
        }
    }
}

@Composable
private fun CardCenter(card: Card, width: Dp) {
    when (card.rank) {
        Rank.ACE -> Text(
            card.suit.symbol,
            color = card.suit.color,
            fontSize = (width.value * 0.62f).sp,
            textAlign = TextAlign.Center,
        )

        Rank.JACK, Rank.QUEEN, Rank.KING -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                card.rank.symbol,
                color = card.suit.color,
                fontSize = (width.value * 0.52f).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
            )
            Text(
                card.suit.symbol,
                color = card.suit.color,
                fontSize = (width.value * 0.26f).sp,
                // Negative padding is illegal in Compose; offset pulls the
                // suit up under the letter the way the iOS lockup does.
                modifier = Modifier.offset(y = width * -0.04f),
            )
        }

        else -> PipGrid(card, width)
    }
}

/** Classic pip arrangement for 2–10 – the count is readable at a glance. */
@Composable
private fun PipGrid(card: Card, width: Dp) {
    val pipSize = width.value * (if (card.rank.value >= 9) 0.17f else 0.20f)
    val positions = pipPositions(card.rank)
    val style = TextStyle(color = card.suit.color, fontSize = pipSize.sp)

    Layout(
        content = {
            positions.forEach { point ->
                // Pips in the lower half sit upside down, as on a real card.
                Text(card.suit.symbol, style = style, modifier = Modifier.rotate(if (point.y > 0.5f) 180f else 0f))
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val point = positions[index]
                placeable.place(
                    x = (constraints.maxWidth * point.x).roundToInt() - placeable.width / 2,
                    y = (constraints.maxHeight * point.y).roundToInt() - placeable.height / 2,
                )
            }
        }
    }
}

@Composable
private fun CornerLabel(card: Card, width: Dp, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            card.rank.symbol,
            color = card.suit.color,
            fontSize = (width.value * 0.26f).sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            card.suit.symbol,
            color = card.suit.color,
            fontSize = (width.value * 0.20f).sp,
            modifier = Modifier.offset(y = width * -0.03f),
        )
    }
}

/** The back of a card, used for the other players' hands. */
@Composable
fun CardBack(width: Dp, modifier: Modifier = Modifier) {
    val height = width * 1.45f
    val corner = width * 0.14f

    Box(
        modifier
            .size(width, height)
            .shadow(1.5.dp, RoundedCornerShape(corner))
            .clip(RoundedCornerShape(corner))
            .background(Brush.linearGradient(TableStyle.cardBack))
            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(corner)),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(width * 0.1f)
                .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(corner * 0.6f)),
        )
    }
}

/** Unit position (0…1) of one pip inside the card's inner area. */
private data class PipPoint(val x: Float, val y: Float)

private fun pipPositions(rank: Rank): List<PipPoint> {
    val l = 0.18f; val m = 0.5f; val r = 0.82f
    val top = 0.08f; val mid = 0.5f; val bottom = 0.92f
    val row2 = 0.36f; val row3 = 0.64f
    val quarterUp = 0.26f; val quarterDown = 0.74f

    return when (rank) {
        Rank.TWO -> listOf(PipPoint(m, top), PipPoint(m, bottom))
        Rank.THREE -> listOf(PipPoint(m, top), PipPoint(m, mid), PipPoint(m, bottom))
        Rank.FOUR -> listOf(
            PipPoint(l, top), PipPoint(r, top),
            PipPoint(l, bottom), PipPoint(r, bottom),
        )
        Rank.FIVE -> pipPositions(Rank.FOUR) + PipPoint(m, mid)
        Rank.SIX -> listOf(
            PipPoint(l, top), PipPoint(r, top),
            PipPoint(l, mid), PipPoint(r, mid),
            PipPoint(l, bottom), PipPoint(r, bottom),
        )
        Rank.SEVEN -> pipPositions(Rank.SIX) + PipPoint(m, quarterUp)
        Rank.EIGHT -> pipPositions(Rank.SIX) + listOf(PipPoint(m, quarterUp), PipPoint(m, quarterDown))
        Rank.NINE -> listOf(
            PipPoint(l, top), PipPoint(r, top),
            PipPoint(l, row2), PipPoint(r, row2),
            PipPoint(l, row3), PipPoint(r, row3),
            PipPoint(l, bottom), PipPoint(r, bottom),
            PipPoint(m, mid),
        )
        Rank.TEN -> listOf(
            PipPoint(l, top), PipPoint(r, top),
            PipPoint(l, row2), PipPoint(r, row2),
            PipPoint(l, row3), PipPoint(r, row3),
            PipPoint(l, bottom), PipPoint(r, bottom),
            PipPoint(m, 0.22f), PipPoint(m, 0.78f),
        )
        else -> emptyList()
    }
}
