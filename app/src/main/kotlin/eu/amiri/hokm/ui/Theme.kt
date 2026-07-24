package eu.amiri.hokm.ui

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import eu.amiri.hokm.engine.Seat
import eu.amiri.hokm.engine.Suit
import eu.amiri.hokm.engine.Team

/**
 * The table look, ported 1:1 from the iOS app's `TableStyle` / `Themes`
 * so both platforms share one visual identity.
 */
object TableStyle {
    val feltTop = Color(red = 0.10f, green = 0.42f, blue = 0.26f)
    val feltBottom = Color(red = 0.05f, green = 0.27f, blue = 0.17f)

    val feltGradient = Brush.verticalGradient(listOf(feltTop, feltBottom))

    val gold = Color(red = 0.94f, green = 0.78f, blue = 0.35f)

    /** Team accents: your own team is blue, the opponents are rose-red. */
    val teamMine = Color(red = 0.24f, green = 0.53f, blue = 0.92f)
    val teamOpponent = Color(red = 0.85f, green = 0.35f, blue = 0.40f)

    /** Card back (the free "blue" design from iOS). */
    val cardBack = listOf(
        Color(red = 0.16f, green = 0.28f, blue = 0.55f),
        Color(red = 0.10f, green = 0.16f, blue = 0.38f),
    )
}

/** Red suits print red, black suits near-black – as on a real card. */
val Suit.color: Color
    get() = if (isRed) Color(0.78f, 0.12f, 0.16f) else Color(0.1f, 0.1f, 0.1f)

fun Team.tint(myTeam: Team): Color =
    if (this == myTeam) TableStyle.teamMine else TableStyle.teamOpponent

/**
 * Where a seat is drawn on screen; the local player always sits at the bottom.
 * Play proceeds counter-clockwise: bottom → right → top → left.
 */
enum class TablePosition {
    BOTTOM, RIGHT, TOP, LEFT;

    companion object {
        fun of(seat: Seat, from: Seat): TablePosition =
            when ((seat.index - from.index + 4) % 4) {
                0 -> BOTTOM
                1 -> RIGHT
                2 -> TOP
                else -> LEFT
            }
    }
}
