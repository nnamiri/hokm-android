package eu.amiri.hokm.engine

import kotlinx.serialization.Serializable

/**
 * The complete, serializable state of a [HokmGame] – everything needed to
 * resume a game later, including the hidden information (all hands, the stock
 * and the undealt rest of the deck).
 *
 * The iOS app makes `HokmGame` itself `Codable`; Kotlin keeps the engine free
 * of serialization concerns by round-tripping through this value type instead.
 */
@Serializable
data class GameState(
    val rules: HokmRules,
    val phase: GamePhase,
    val hakem: Seat,
    val trumpChoice: TrumpChoice? = null,
    val hands: Map<Seat, List<Card>> = emptyMap(),
    val currentTrick: TrickState,
    val lastTrick: TrickState? = null,
    val playedCards: List<Card> = emptyList(),
    val trickCounts: Map<Team, Int> = emptyMap(),
    val scores: Map<Team, Int> = emptyMap(),
    val turn: Seat? = null,
    val handNumber: Int = 1,
    val revision: Int = 0,
    val undealt: List<Card> = emptyList(),
    val stock: List<Card> = emptyList(),
    val revealedCard: Card? = null,
    val pendingDiscards: List<Seat> = emptyList(),
    val lastDrawResult: DrawResult? = null,
)

/** A [Trick] reduced to its data (the class itself carries no other state). */
@Serializable
data class TrickState(val leader: Seat, val plays: List<Trick.Play> = emptyList())

fun Trick.state(): TrickState = TrickState(leader, plays)

fun TrickState.toTrick(): Trick {
    val trick = Trick(leader)
    for (play in plays) trick.add(play.card, play.seat)
    return trick
}
